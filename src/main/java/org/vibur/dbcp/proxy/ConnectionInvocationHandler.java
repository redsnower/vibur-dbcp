/**
 * Copyright 2013 Simeon Malchev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vibur.dbcp.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vibur.dbcp.ViburDBCPConfig;
import org.vibur.dbcp.cache.ConnMethodKey;
import org.vibur.dbcp.cache.StatementVal;
import org.vibur.dbcp.pool.ConnHolder;
import org.vibur.dbcp.pool.PoolOperations;
import org.vibur.dbcp.proxy.listener.ExceptionListenerImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.vibur.dbcp.cache.StatementVal.AVAILABLE;
import static org.vibur.dbcp.cache.StatementVal.IN_USE;

/**
 * @author Simeon Malchev
 */
public class ConnectionInvocationHandler extends AbstractInvocationHandler<Connection> implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionInvocationHandler.class);

    private final PoolOperations poolOperations;
    private final ConnHolder conn;

    private final ViburDBCPConfig config;
    private final ConcurrentMap<ConnMethodKey, StatementVal> statementCache;

    public ConnectionInvocationHandler(ConnHolder conn, ViburDBCPConfig config) {
        super(conn.value(), new ExceptionListenerImpl());
        this.poolOperations = config.getPoolOperations();
        this.conn = conn;
        this.config = config;
        this.statementCache = config.getStatementCache();
    }

    @SuppressWarnings("unchecked")
    protected Object doInvoke(Connection proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();

        boolean aborted = methodName == "abort";
        if (aborted || methodName == "close")
            return processCloseOrAbort(aborted, method, args);
        if (methodName == "isClosed")
            return isClosed();

        if (methodName == "isValid")
            return isClosed() ? false : targetInvoke(method, args);

        ensureNotClosed(); // all other Connection interface methods cannot work if the JDBC Connection is closed

        // Methods which results have to be proxied so that when getConnection() is called
        // on their results the return value to be the current JDBC Connection proxy.
        if (methodName == "createStatement") { // *3
            StatementVal statement = getUncachedStatement(method, args);
            return Proxy.newStatement(statement, null, proxy, config, getExceptionListener());
        }
        if (methodName == "prepareStatement") { // *6
            StatementVal statement = getCachedStatement(method, args);
            return Proxy.newPreparedStatement(statement, statementCache, proxy, config, getExceptionListener());
        }
        if (methodName == "prepareCall") { // *3
            StatementVal statement = getCachedStatement(method, args);
            return Proxy.newCallableStatement(statement, statementCache, proxy, config, getExceptionListener());
        }
        if (methodName == "getMetaData") { // *1
            DatabaseMetaData rawDatabaseMetaData = (DatabaseMetaData) targetInvoke(method, args);
            return Proxy.newDatabaseMetaData(rawDatabaseMetaData, proxy, getExceptionListener());
        }

        return super.doInvoke(proxy, method, args);
    }

    /**
     * Returns <i>a possible</i> cached StatementVal object for the given proxied Connection object and the
     * invoked on it prepareXYZ Method with the given args.
     *
     * @param method the invoked method
     * @param args the invoked method arguments
     * @return a Statement object wrapped in a StatementVal holder
     * @throws Throwable if the invoked underlying prepareXYZ method throws an exception
     */
    private StatementVal getCachedStatement(Method method, Object[] args) throws Throwable {
        if (statementCache != null) {
            Connection target = getTarget();
            ConnMethodKey key = new ConnMethodKey(target, method, args);
            StatementVal statement = statementCache.get(key);
            if (statement == null || !statement.state().compareAndSet(AVAILABLE, IN_USE)) {
                Statement rawStatement = (Statement) targetInvoke(method, args);
                if (statement == null) { // there was no entry for the key, so we'll try to put a new one
                    statement = new StatementVal(rawStatement, new AtomicInteger(IN_USE));
                    if (statementCache.putIfAbsent(key, statement) == null)
                        return statement; // the new entry was successfully put in the cache
                }
                return new StatementVal(rawStatement, null);
            } else { // the statement was in the cache and was available
                if (logger.isTraceEnabled())
                    logger.trace("Using cached statement for connection {}, method {}, args {}",
                        target, method, Arrays.toString(args));
                return statement;
            }
        } else
            return getUncachedStatement(method, args);
    }

    private StatementVal getUncachedStatement(Method method, Object[] args) throws Throwable {
        Statement rawStatement = (Statement) targetInvoke(method, args);
        return new StatementVal(rawStatement, null);
    }

    private Object processCloseOrAbort(boolean aborted, Method method, Object[] args) throws Throwable {
        if (aborted)
            targetInvoke(method, args); // executes the abort() call, which in turn may throw an exception
        if (!getAndSetClosed())
            poolOperations.restore(conn, aborted, getExceptionListener().getExceptions());
        return null;
    }
}

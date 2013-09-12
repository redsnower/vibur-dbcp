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

package org.vibur.dbcp;

import org.vibur.dbcp.cache.StatementKey;
import org.vibur.dbcp.cache.ValueHolder;
import org.vibur.objectpool.HolderValidatingPoolService;

import java.sql.Statement;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Simeon Malchev
 */
public class ViburDBCPConfig {

    /** Database driver class name. */
    private String driverClassName;
    /** Database JDBC Connection string. */
    private String jdbcUrl;
    /** User name to use. */
    private String username;
    /** Password to use. */
    private String password;


    /** If the connection has stayed in the pool for at least {@code connectionIdleLimitInSeconds},
     * it will be validated before being given to the application using the {@code testConnectionQuery}.
     * If set to zero, will validate the connection always when it is taken from the pool.
     * If set to a negative number, will never validate the taken from the pool connection. */
    private int connectionIdleLimitInSeconds = 60;
    /** Used to test the validity of the JDBC Connection. Should be set to a valid query if the
     * {@code connectionIdleLimitInSeconds} is set to a non-negative number. */
    private String testConnectionQuery = "SELECT 1";


    /** The pool initial size, i.e. the initial number of JDBC Connections allocated in this pool. */
    private int poolInitialSize = 1;
    /** The pool max size, i.e. the maximum number of JDBC Connections allocated in this pool. */
    private int poolMaxSize = 10;
    /** The pool's fairness setting with regards to waiting threads. */
    private boolean poolFair = true;
    /** If {@code true}, the pool will keep information for the current stack trace of every taken connection. */
    private boolean poolEnableConnectionTracking = false;


    /** For more details on the next 2 parameters see {@link org.vibur.objectpool.util.PoolReducer}
     *  and {@link org.vibur.objectpool.util.SamplingPoolReducer}.
     */
    /** The time period after which the {@code poolReducer} will try to possibly reduce the number of created
     * but unused JDBC Connections in this pool. */
    private long reducerTimeIntervalInSeconds = 30;
    /** How many times the {@code poolReducer} will wake up during the given
     * {@code reducerTimeIntervalInSeconds} period in order to sample various information from this pool. */
    private int reducerSamples = 10;


    /** Time to wait before a call to getConnection() times out and returns an error.
     * {@code 0} means forever. */
    private long createConnectionTimeoutInMs = 30000;
    /** After attempting to acquire a JDBC Connection and failing with an {@code SQLException},
     * wait for this long time before attempting to acquire a new JDBC Connection again. */
    private long acquireRetryDelayInMs = 1000;
    /** After attempting to acquire a JDBC Connection and failing with an {@code SQLException},
     * try to connect these many times before giving up. */
    private int acquireRetryAttempts = 3;


    /** Defines the maximum statement cache size. {@code 0} disables it, max values is {@code 1000}.
     * If the statement's cache is not enabled, the client application may safely exclude the dependency
     * on Google's ConcurrentLinkedCacheMap from its pom.xml file. */
    private int statementCacheMaxSize = 0;
    private ConcurrentMap<StatementKey, ValueHolder<Statement>> statementCache = null;

    private HolderValidatingPoolService<ConnState> connectionPool;

    /** If set to {@code true}, log all SQL statements being executed. */
    private boolean logStatementsEnabled = false;
    /** Queries taking longer than this limit to execute are logged. {@code 0} disables it. */
    private long queryExecuteTimeLimitInMs = 0;


    /** If set to {@code true}, will reset the connection default values below, always after the
     * connection is restored (returned) to the pool after use. If the calling application never changes
     * these default values, resetting them is not needed. */
    private boolean resetDefaultsAfterUse = false;
    /** The default auto-commit state of the created connections. */
    private Boolean defaultAutoCommit;
    /** The default read-only state of the created connections. */
    private Boolean defaultReadOnly;
    /** The default transaction isolation state of the created connections. */
    private String defaultTransactionIsolation;
    /** The default catalog state of the created connections. */
    private String defaultCatalog;
    /** The parsed transaction isolation value. Default = driver value. */
    private Integer defaultTransactionIsolationValue;


    //////////////////////// Getter & Setters ////////////////////////

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectionIdleLimitInSeconds() {
        return connectionIdleLimitInSeconds;
    }

    public void setConnectionIdleLimitInSeconds(int connectionIdleLimitInSeconds) {
        this.connectionIdleLimitInSeconds = connectionIdleLimitInSeconds;
    }

    public String getTestConnectionQuery() {
        return testConnectionQuery;
    }

    public void setTestConnectionQuery(String testConnectionQuery) {
        this.testConnectionQuery = testConnectionQuery;
    }

    public int getPoolInitialSize() {
        return poolInitialSize;
    }

    public void setPoolInitialSize(int poolInitialSize) {
        this.poolInitialSize = poolInitialSize;
    }

    public int getPoolMaxSize() {
        return poolMaxSize;
    }

    public void setPoolMaxSize(int poolMaxSize) {
        this.poolMaxSize = poolMaxSize;
    }

    public boolean isPoolFair() {
        return poolFair;
    }

    public void setPoolFair(boolean poolFair) {
        this.poolFair = poolFair;
    }

    public boolean isPoolEnableConnectionTracking() {
        return poolEnableConnectionTracking;
    }

    public void setPoolEnableConnectionTracking(boolean poolEnableConnectionTracking) {
        this.poolEnableConnectionTracking = poolEnableConnectionTracking;
    }

    public long getReducerTimeIntervalInSeconds() {
        return reducerTimeIntervalInSeconds;
    }

    public void setReducerTimeIntervalInSeconds(long reducerTimeIntervalInSeconds) {
        this.reducerTimeIntervalInSeconds = reducerTimeIntervalInSeconds;
    }

    public int getReducerSamples() {
        return reducerSamples;
    }

    public void setReducerSamples(int reducerSamples) {
        this.reducerSamples = reducerSamples;
    }

    public long getCreateConnectionTimeoutInMs() {
        return createConnectionTimeoutInMs;
    }

    public void setCreateConnectionTimeoutInMs(long createConnectionTimeoutInMs) {
        this.createConnectionTimeoutInMs = createConnectionTimeoutInMs;
    }

    public long getAcquireRetryDelayInMs() {
        return acquireRetryDelayInMs;
    }

    public void setAcquireRetryDelayInMs(long acquireRetryDelayInMs) {
        this.acquireRetryDelayInMs = acquireRetryDelayInMs;
    }

    public int getAcquireRetryAttempts() {
        return acquireRetryAttempts;
    }

    public void setAcquireRetryAttempts(int acquireRetryAttempts) {
        this.acquireRetryAttempts = acquireRetryAttempts;
    }

    public int getStatementCacheMaxSize() {
        return statementCacheMaxSize;
    }

    public void setStatementCacheMaxSize(int statementCacheMaxSize) {
        this.statementCacheMaxSize = statementCacheMaxSize;
    }

    public ConcurrentMap<StatementKey, ValueHolder<Statement>> getStatementCache() {
        return statementCache;
    }

    public void setStatementCache(ConcurrentMap<StatementKey, ValueHolder<Statement>> statementCache) {
        this.statementCache = statementCache;
    }

    public HolderValidatingPoolService<ConnState> getConnectionPool() {
        return connectionPool;
    }

    public void setConnectionPool(HolderValidatingPoolService<ConnState> connectionPool) {
        this.connectionPool = connectionPool;
    }

    public boolean isLogStatementsEnabled() {
        return logStatementsEnabled;
    }

    public void setLogStatementsEnabled(boolean logStatementsEnabled) {
        this.logStatementsEnabled = logStatementsEnabled;
    }

    public long getQueryExecuteTimeLimitInMs() {
        return queryExecuteTimeLimitInMs;
    }

    public void setQueryExecuteTimeLimitInMs(long queryExecuteTimeLimitInMs) {
        this.queryExecuteTimeLimitInMs = queryExecuteTimeLimitInMs;
    }

    public boolean isResetDefaultsAfterUse() {
        return resetDefaultsAfterUse;
    }

    public void setResetDefaultsAfterUse(boolean resetDefaultsAfterUse) {
        this.resetDefaultsAfterUse = resetDefaultsAfterUse;
    }

    public Boolean getDefaultAutoCommit() {
        return defaultAutoCommit;
    }

    public void setDefaultAutoCommit(Boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }

    public Boolean getDefaultReadOnly() {
        return defaultReadOnly;
    }

    public void setDefaultReadOnly(Boolean defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly;
    }

    public String getDefaultTransactionIsolation() {
        return defaultTransactionIsolation;
    }

    public void setDefaultTransactionIsolation(String defaultTransactionIsolation) {
        this.defaultTransactionIsolation = defaultTransactionIsolation;
    }

    public String getDefaultCatalog() {
        return defaultCatalog;
    }

    public void setDefaultCatalog(String defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
    }

    public Integer getDefaultTransactionIsolationValue() {
        return defaultTransactionIsolationValue;
    }

    public void setDefaultTransactionIsolationValue(Integer defaultTransactionIsolationValue) {
        this.defaultTransactionIsolationValue = defaultTransactionIsolationValue;
    }
}
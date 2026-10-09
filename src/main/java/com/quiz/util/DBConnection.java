package com.quiz.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JDBC connection provider with a small thread-safe connection pool.
 *
 * Servlets are multi-threaded, so opening a brand new TCP connection to MySQL on
 * every query would be slow. Connections are kept in a {@link BlockingQueue};
 * {@link #getConnection()} hands out a proxy whose {@code close()} returns the
 * real connection to the pool instead of closing it. That means every DAO can keep
 * using the normal try-with-resources pattern.
 *
 * Configuration comes from {@code db.properties} (classpath) and can be overridden
 * by environment variables (QUIZ_DB_URL, QUIZ_DB_USER, QUIZ_DB_PASSWORD).
 */
public final class DBConnection {

    private static final Properties CONFIG = loadConfig();
    private static final int POOL_SIZE = Integer.parseInt(setting("QUIZ_DB_POOL_SIZE", "db.pool.size", "10"));
    private static final BlockingQueue<Connection> IDLE = new ArrayBlockingQueue<>(POOL_SIZE);
    /** Number of physical connections created so far (idle + borrowed). */
    private static final AtomicInteger CREATED = new AtomicInteger();

    private DBConnection() {
    }

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // fall through to defaults / environment variables
        }
        return props;
    }

    private static String setting(String env, String key, String fallback) {
        String fromEnv = System.getenv(env);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return CONFIG.getProperty(key, fallback);
    }

    /** Borrow a connection. Always close it (try-with-resources) to give it back. */
    public static Connection getConnection() throws SQLException {
        Connection real = IDLE.poll();
        while (real != null && !isUsable(real)) {
            discard(real);
            real = IDLE.poll();
        }
        if (real == null) {
            real = openOrWait();
        }
        return wrap(real);
    }

    private static Connection openOrWait() throws SQLException {
        while (true) {
            int n = CREATED.get();
            if (n < POOL_SIZE) {
                // reserve a slot atomically so we never create more than POOL_SIZE connections
                if (CREATED.compareAndSet(n, n + 1)) {
                    try {
                        return open();
                    } catch (SQLException | RuntimeException e) {
                        CREATED.decrementAndGet();
                        throw e;
                    }
                }
                continue;
            }
            // pool exhausted: wait for another thread to return a connection
            try {
                Connection waited = IDLE.poll(10, TimeUnit.SECONDS);
                if (waited == null) {
                    throw new SQLException("Timed out waiting for a free database connection.");
                }
                if (isUsable(waited)) {
                    return waited;
                }
                discard(waited);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("Interrupted while waiting for a database connection.", e);
            }
        }
    }

    private static Connection open() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found on the classpath.", e);
        }
        return DriverManager.getConnection(
                setting("QUIZ_DB_URL", "db.url", "jdbc:mysql://localhost:3306/online_quiz"),
                setting("QUIZ_DB_USER", "db.user", "root"),
                setting("QUIZ_DB_PASSWORD", "db.password", ""));
    }

    private static boolean isUsable(Connection c) {
        try {
            return !c.isClosed() && c.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }

    private static void discard(Connection c) {
        CREATED.decrementAndGet();
        try {
            c.close();
        } catch (SQLException ignored) {
            // nothing useful to do
        }
    }

    private static Connection wrap(Connection real) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("close".equals(method.getName())) {
                release(real);
                return null;
            }
            if ("isClosed".equals(method.getName())) {
                return false;
            }
            try {
                return method.invoke(real, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (Connection) Proxy.newProxyInstance(
                DBConnection.class.getClassLoader(), new Class<?>[]{Connection.class}, handler);
    }

    /** Reset transaction state and return the connection to the idle queue. */
    private static void release(Connection real) {
        try {
            if (!real.getAutoCommit()) {
                real.rollback();
                real.setAutoCommit(true);
            }
        } catch (SQLException e) {
            discard(real);
            return;
        }
        if (!IDLE.offer(real)) {
            discard(real);
        }
    }

    /** Closes all idle connections (called when the web app stops). */
    public static void shutdown() {
        Connection c;
        while ((c = IDLE.poll()) != null) {
            discard(c);
        }
    }
}

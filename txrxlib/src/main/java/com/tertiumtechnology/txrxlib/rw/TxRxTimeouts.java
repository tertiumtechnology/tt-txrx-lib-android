package com.tertiumtechnology.txrxlib.rw;

/**
 * This is an utility class for managing timeouts used in device communication.
 */
public class TxRxTimeouts {

    /**
     * Default value for connection timeout
     */
    public static final long CONNECTION_TIMEOUT_DEFAULT_VALUE = 20000;

    /**
     * Default value for first read/notify timeout
     */
    public static final long FIRST_READ_TIMEOUT_DEFAULT_VALUE = 2000;

    /**
     * Default value for later read/notify timeout
     */
    public static final long LATER_READ_TIMEOUT_DEFAULT_VALUE = 200;

    /**
     * Default value for write timeout
     */
    public static final long WRITE_TIMEOUT_DEFAULT_VALUE = 1500;

    private static final TxRxTimeouts defaultTxRxTimeouts = new TxRxTimeouts(
            CONNECTION_TIMEOUT_DEFAULT_VALUE,
            WRITE_TIMEOUT_DEFAULT_VALUE,
            FIRST_READ_TIMEOUT_DEFAULT_VALUE,
            LATER_READ_TIMEOUT_DEFAULT_VALUE
    );

    /**
     * Returns a {@link TxRxTimeouts}, configured with default timeouts
     *
     * @return a TxRxTimeouts with default timeouts
     */
    public static TxRxTimeouts getDefaultTimeouts() {
        return defaultTxRxTimeouts;
    }

    private final long connectTimeout;
    private final long firstReadTimeout;
    private final long laterReadTimeout;
    private final long writeTimeout;

    /**
     * Create a new {@link TxRxTimeouts} in order to manage communication timeouts.
     *
     * @param connectTimeout   long the timeout used during connection to a device
     * @param writeTimeout     long the timeout used in write operation
     * @param firstReadTimeout long the timeout used for the first read/notify operaation
     * @param laterReadTimeout long the timeout used for subsequent read/norify operaations
     */
    public TxRxTimeouts(long connectTimeout, long writeTimeout, long firstReadTimeout, long laterReadTimeout) {
        this.connectTimeout = connectTimeout;
        this.writeTimeout = writeTimeout;
        this.firstReadTimeout = firstReadTimeout;
        this.laterReadTimeout = laterReadTimeout;
    }

    /**
     * Create a new {@link TxRxTimeouts}in order to manage communication timeouts, configured with default timeouts.
     */
    public TxRxTimeouts() {
        this.connectTimeout = CONNECTION_TIMEOUT_DEFAULT_VALUE;
        this.writeTimeout = WRITE_TIMEOUT_DEFAULT_VALUE;
        this.firstReadTimeout = FIRST_READ_TIMEOUT_DEFAULT_VALUE;
        this.laterReadTimeout = LATER_READ_TIMEOUT_DEFAULT_VALUE;
    }

    /**
     * Returns the connection timeout used during connection to a device
     *
     * @return a long representing the connection timeout
     */
    public long getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Returns the timeout used during the first read/notify operation
     *
     * @return a long representing the first read/notify timeout
     */
    public long getFirstReadTimeout() {
        return firstReadTimeout;
    }

    /**
     * Returns the timeout used during the subsequent read/notify operations, after the first read/notify operation
     *
     * @return a long representing the timeout for subsequent read/notify operations
     */
    public long getLaterReadTimeout() {
        return laterReadTimeout;
    }

    /**
     * Returns the write timeout used in write operations
     *
     * @return a long representing the write timeout
     */
    public long getWriteTimeout() {
        return writeTimeout;
    }
}

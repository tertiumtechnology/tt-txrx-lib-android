package com.tertiumtechnology.txrxlib.rw;

import android.bluetooth.BluetoothAdapter;

/**
 * Callback interface used to asynchronously notify results on each request to device.
 *
 * @see TxRxDeviceManager#TxRxDeviceManager(BluetoothAdapter, TxRxDeviceCallback, TxRxTimeouts)
 */
public interface TxRxDeviceCallback {
    /**
     * Callback on successful connection to a device.
     */
    void onDeviceConnected();

    /**
     * Callback on successful disconnect from a device.
     */
    void onDeviceDisconnected();

    /**
     * Callback when a connection error occurs.
     *
     * @param errorCode Error code for connection error
     */
    void onConnectionError(int errorCode);

    /**
     * Callback on connection timeout.
     */
    void onConnectionTimeout();

    /**
     * Callback when a TxRx service has been discovered.
     */
    void onTxRxServiceDiscovered();

    /**
     * Callback when no TxRx service has been found.
     */
    void onTxRxServiceNotFound();

    /**
     * Callback on a successful write request.
     *
     * @param data The data written
     */
    void onWriteData(String data);

    /**
     * Callback on a successful read request.
     *
     * @param data The data read
     */
    void onReadData(String data);

    /**
     * Callback on a successful setMode request.
     *
     * @param mode The current setMode value
     */
    void onSetMode(int mode);

    /**
     * Callback when a device send notification data.
     *
     * @param data The data notified
     */
    void onNotifyData(String data);

    /**
     * Callback when a read error occurs.
     *
     * @param errorCode Error code for read error
     */
    void onReadError(int errorCode);

    /**
     * Callback when a write error occurs.
     *
     * @param errorCode Error code for write error
     */
    void onWriteError(int errorCode);

    /**
     * Callback on write timeout.
     */
    void onWriteTimeout();

    /**
     * Callback when a setMode error occurs.
     *
     * @param errorCode Error code for setMode error
     */
    void onSetModeError(int errorCode);

    /**
     * Callback on setMode timeout.
     */
    void onSetModeTimeout();

    /**
     * Callback on read or notify timeout.
     */
    void onReadNotifyTimeout();
}

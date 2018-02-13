package com.tertiumtechnology.txrxlib.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * Callback interface used to deliver BLE scan results.
 *
 * @see TxRxScanner#TxRxScanner(BluetoothAdapter, TxRxScanCallback)
 */
public interface TxRxScanCallback {

    /**
     * Callback when a BLE device has been found.
     *
     * @param device {@link BluetoothDevice} Identifies the remote device found.
     */
    void onDeviceFound(BluetoothDevice device);

    /**
     * Callback immediately after the scan has been stopped.
     */
    void afterStopScan();
}

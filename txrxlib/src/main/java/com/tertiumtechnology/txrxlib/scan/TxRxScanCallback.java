package com.tertiumtechnology.txrxlib.scan;

import android.bluetooth.BluetoothAdapter;

/**
 * Callback interface used to deliver BLE scan results.
 *
 * @see TxRxScanner#TxRxScanner(BluetoothAdapter, TxRxScanCallback)
 */
public interface TxRxScanCallback {

    /**
     * Callback immediately after the scan has been stopped.
     */
    void afterStopScan();

    /**
     * Callback when a BLE device has been found.
     *
     * @param scanResult the result of a scan operation for BLE Devices
     */
    void onDeviceFound(TxRxScanResult scanResult);
}

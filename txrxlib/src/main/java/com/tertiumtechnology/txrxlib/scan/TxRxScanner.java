package com.tertiumtechnology.txrxlib.scan;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides methods to perform scan for BLE devices.
 * Needs an implementation of {@link TxRxScanCallback} to deliver scan results.
 * <p>
 * <b>Note:</b> Most of the scan methods here require
 * {@link android.Manifest.permission#BLUETOOTH_ADMIN} permission.
 *
 * @see TxRxScanCallback
 */
public class TxRxScanner {
    private static final String TAG = TxRxScanner.class.getSimpleName();

    private static final long DEFAULT_SCAN_TIMEOUT = 10000;

    private final BluetoothAdapter bluetoothAdapter;
    private final TxRxScanCallback txRxScanCallback;

    private final Handler handler;
    private final ScanCallback scanCallback;
    private boolean isScanning;
    private long scanTimeout;

    /**
     * Create a new {@link TxRxScanner} to perform scan for BLE devices.
     *
     * @param bluetoothAdapter {@link BluetoothAdapter} used to perform BLE task
     * @param txRxScanCallback {@link TxRxScanCallback} callback used to deliver scan results
     */
    public TxRxScanner(BluetoothAdapter bluetoothAdapter, final TxRxScanCallback txRxScanCallback) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.txRxScanCallback = txRxScanCallback;
        this.handler = new Handler(Looper.myLooper());
        isScanning = false;
        scanTimeout = DEFAULT_SCAN_TIMEOUT;

        scanCallback = new ScanCallback() {
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult scanResult : results) {
                    txRxScanCallback.onDeviceFound(getTxRxScanResult(scanResult));
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e(TAG, "Scan Failed with error Code: " + errorCode);
            }

            @Override
            public void onScanResult(int callbackType, ScanResult scanResult) {
                txRxScanCallback.onDeviceFound(getTxRxScanResult(scanResult));
            }

            private TxRxScanResult getTxRxScanResult(ScanResult scanResult) {
                ScanRecord scanRecord = scanResult.getScanRecord();

                byte[] scanRecordByte = new byte[0];
                int txPowerLevel = Integer.MIN_VALUE;
                List<ParcelUuid> serviceUuids = new ArrayList<>();

                if (scanRecord != null) {
                    txPowerLevel = scanRecord.getTxPowerLevel();
                    scanRecordByte = scanRecord.getBytes();
                    serviceUuids = scanRecord.getServiceUuids();
                }

                return new TxRxScanResult(scanResult.getDevice(), scanResult.getRssi(),
                        scanRecordByte,
                        txPowerLevel, serviceUuids);
            }
        };
    }

    /**
     * Retrieve the current scan timeout value, in milliseconds. Zero returns implies an infinite timeout
     *
     * @return the current scan timeout value, in milliseconds
     */
    public long getScanTimeout() {
        return scanTimeout;
    }

    /**
     * Set the scan timeout, in milliseconds. A timeout of zero is interpreted as an infinite timeout.
     *
     * @param scanTimeout the scan timeout, in milliseconds
     */
    public void setScanTimeout(long scanTimeout) {
        this.scanTimeout = scanTimeout;
    }

    /**
     * Returns whether it is currently scanning for BLE devices.
     *
     * @return true if it is currently scanning for devices, false otherwise.
     */
    public boolean isScanning() {
        return isScanning;
    }

    /**
     * Start BLE scan. The scan results will be delivered through
     * {@link TxRxScanCallback#onDeviceFound(TxRxScanResult)}.
     * <p>
     * Requires {@link android.Manifest.permission#BLUETOOTH_ADMIN} permission.
     * An app running on Android S or later requires {@link android.Manifest.permission#BLUETOOTH_SCAN} permission.
     * <p>
     * An app must have {@link android.Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_COARSE_LOCATION} permission
     * in order to get results.
     * An App targeting Android Q or later must have {@link android.Manifest.permission#ACCESS_FINE_LOCATION
     * ACCESS_FINE_LOCATION} permission in order to get results.
     */
    public void startScan() {
        startScan(null);
    }

    /**
     * Start BLE scan, looking for service uuids passed as parameter. The scan results will be delivered
     * through
     * {@link TxRxScanCallback#onDeviceFound(TxRxScanResult)}.
     * <p>
     * Requires {@link android.Manifest.permission#BLUETOOTH_ADMIN} permission.<br/>
     * An app running on Android S or later requires {@link android.Manifest.permission#BLUETOOTH_SCAN} permission.
     * <p>
     * An app must have {@link android.Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_COARSE_LOCATION} permission
     * in order to get results.
     * An App targeting Android Q or later must have {@link android.Manifest.permission#ACCESS_FINE_LOCATION
     * ACCESS_FINE_LOCATION} permission in order to get results.
     *
     * @param serviceUuids the service uuids to look for during scan
     */
    @SuppressLint("MissingPermission")
    public void startScan(List<String> serviceUuids) {
        Log.i(TAG, "Start scan for device");

        isScanning = true;

        if (scanTimeout > 0) {
            handler.postDelayed(() -> {
                Log.i(TAG, "Stop scanning after " + (scanTimeout / 1000) + " seconds");

                if (isScanning()) {
                    stopScan();
                }
            }, scanTimeout);
        }

        ArrayList<ScanFilter> filters = new ArrayList<>();

        if (serviceUuids != null && !serviceUuids.isEmpty()) {
            for (String serviceUuid : serviceUuids) {
                filters.add(
                        new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(serviceUuid))
                                .build
                                        ());
            }
        }

        ScanSettings settings = new ScanSettings.Builder().setScanMode(
                ScanSettings.SCAN_MODE_LOW_LATENCY).build();

        bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
    }

    /**
     * Stops an ongoing BLE scan.
     * <p>
     * Requires {@link android.Manifest.permission#BLUETOOTH_ADMIN} permission.
     * An app running on Android S or later requires {@link android.Manifest.permission#BLUETOOTH_SCAN} permission.
     */
    @SuppressLint("MissingPermission")
    public void stopScan() {
        Log.i(TAG, "Stop scan");

        isScanning = false;
        handler.removeCallbacksAndMessages(null);

        bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);

        txRxScanCallback.afterStopScan();
    }
}
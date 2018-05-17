package com.tertiumtechnology.txrxlib.scan;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
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

    private BluetoothAdapter bluetoothAdapter;
    private TxRxScanCallback txRxScanCallback;

    private Handler handler;
    private boolean isScanning;

    private ScanCallback fromLollipopScanCallback;
    private LeScanCallback preLollipopScanCallback;

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
        this.handler = new Handler();
        isScanning = false;
        scanTimeout = DEFAULT_SCAN_TIMEOUT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fromLollipopScanCallback =
                    new ScanCallback() {
                        @SuppressLint("NewApi")
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

                        @SuppressLint("NewApi")
                        @Override
                        public void onScanResult(int callbackType, ScanResult scanResult) {
                            txRxScanCallback.onDeviceFound(getTxRxScanResult(scanResult));
                        }

                        @SuppressLint("NewApi")
                        private TxRxScanResult getTxRxScanResult(ScanResult scanResult) {
                            ScanRecord scanRecord = scanResult.getScanRecord();

                            byte[] scanRecordByte =  new byte[0];
                            int txPowerLevel = Integer.MIN_VALUE;

                            if(scanRecord != null){
                                txPowerLevel = scanRecord.getTxPowerLevel();
                                scanRecordByte =  scanRecord.getBytes();
                            }

                            return new TxRxScanResult(scanResult.getDevice(), scanResult.getRssi(), scanRecordByte,
                                    txPowerLevel);
                        }
                    };
        }
        else {
            preLollipopScanCallback =
                    new LeScanCallback() {
                        @Override
                        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                            txRxScanCallback.onDeviceFound(new TxRxScanResult(device, rssi, scanRecord));
                        }
                    };
        }
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
     * <p>
     * An app must hold
     * {@link android.Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_COARSE_LOCATION} or
     * {@link android.Manifest.permission#ACCESS_FINE_LOCATION ACCESS_FINE_LOCATION} permission
     * in order to get results.
     */
    @SuppressLint("NewApi")
    public void startScan() {
        startScan(null);
    }

    /**
     * Start BLE scan, looking for service uuids passed as parameter. The scan results will be delivered
     * through
     * {@link TxRxScanCallback#onDeviceFound(TxRxScanResult)}.
     * <p>
     * Requires {@link android.Manifest.permission#BLUETOOTH_ADMIN} permission.
     * <p>
     * An app must hold
     * {@link android.Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_COARSE_LOCATION} or
     * {@link android.Manifest.permission#ACCESS_FINE_LOCATION ACCESS_FINE_LOCATION} permission
     * in order to get results.
     *
     * @param serviceUuids the service uuids to look for during scan
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void startScan(List<String> serviceUuids) {
        Log.i(TAG, "Start scan for device");

        isScanning = true;

        if(scanTimeout > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Stop scanning after " + (scanTimeout / 1000) + " seconds");

                    if (isScanning()) {
                        stopScan();
                    }
                }
            }, scanTimeout);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ArrayList<ScanFilter> filters = new ArrayList<>();

            if (serviceUuids != null && !serviceUuids.isEmpty()) {
                for (String serviceUuid : serviceUuids) {
                    filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(serviceUuid)).build
                            ());
                }
            }

            ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

            bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, fromLollipopScanCallback);
        }
        else {
            bluetoothAdapter.startLeScan(preLollipopScanCallback);
        }
    }

    /**
     * Stops an ongoing BLE scan.
     * <p>
     * Requires {@link android.Manifest.permission#BLUETOOTH_ADMIN} permission.
     */
    public void stopScan() {
        Log.i(TAG, "Stop scan");

        isScanning = false;
        handler.removeCallbacksAndMessages(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothAdapter.getBluetoothLeScanner().stopScan(fromLollipopScanCallback);
        }
        else {
            bluetoothAdapter.stopLeScan(preLollipopScanCallback);
        }
        txRxScanCallback.afterStopScan();
    }
}
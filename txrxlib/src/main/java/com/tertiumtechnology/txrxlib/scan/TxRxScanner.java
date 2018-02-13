package com.tertiumtechnology.txrxlib.scan;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
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

    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter bluetoothAdapter;
    private TxRxScanCallback txRxScanCallback;

    private Handler handler;
    private boolean isScanning;

    private ScanCallback fromLollipopScanCallback;
    private LeScanCallback preLollipopScanCallback;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fromLollipopScanCallback =
                    new ScanCallback() {
                        @SuppressLint("NewApi")
                        @Override
                        public void onScanResult(int callbackType, ScanResult scanResult) {
                            txRxScanCallback.onDeviceFound(scanResult.getDevice());
                        }

                        @SuppressLint("NewApi")
                        @Override
                        public void onBatchScanResults(List<ScanResult> results) {
                            for (ScanResult scanResult : results) {
                                txRxScanCallback.onDeviceFound(scanResult.getDevice());
                            }
                        }

                        @Override
                        public void onScanFailed(int errorCode) {
                            Log.e(TAG, "Scan Failed with error Code: " + errorCode);
                        }

                    };
        } else {
            preLollipopScanCallback =
                    new LeScanCallback() {
                        @Override
                        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                            txRxScanCallback.onDeviceFound(device);
                        }
                    };
        }

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
     * Start BLE scan. The scan results will be delivered through {@link TxRxScanCallback#onDeviceFound(BluetoothDevice)}.
     * <p>
     * Requires {@link android.Manifest.permission#BLUETOOTH_ADMIN} permission.
     * <p>
     * An app must hold
     * {@link android.Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_COARSE_LOCATION} or
     * {@link android.Manifest.permission#ACCESS_FINE_LOCATION ACCESS_FINE_LOCATION} permission
     * in order to get results.
     */
    public void startScan() {
        Log.i(TAG, "Start scan for device");

        isScanning = true;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Stop scanning after " + (SCAN_PERIOD / 1000) + " seconds");

                if (isScanning()) {
                    stopScan();
                }
            }
        }, SCAN_PERIOD);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            bluetoothAdapter.getBluetoothLeScanner().startScan(new ArrayList<ScanFilter>(), settings, fromLollipopScanCallback);
        } else {
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
        } else {
            bluetoothAdapter.stopLeScan(preLollipopScanCallback);
        }
        txRxScanCallback.afterStopScan();
    }
}
package com.tertiumtechnology.txrxlib.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * This class provides utility methods to check Bluetooth and BLE features availability.
 * <p>
 * <p>
 * <b>Note:</b> Most of this utility methods require
 * {@link android.Manifest.permission#BLUETOOTH_ADMIN} permission.
 */

public class BleChecker {

    /**
     * Returns the default {@link BluetoothAdapter} for this device.
     *
     * @param context The {@link Context} needed to retrieve the {@link BluetoothAdapter}
     * @return the default {@link BluetoothAdapter} for this device
     */
    public static BluetoothAdapter getBtAdapter(Context context) {
        BluetoothAdapter adapter = null;

        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager != null) {
            adapter = manager.getAdapter();
        }

        return adapter;
    }

    /**
     * Check if the device supports BLE features
     *
     * @param context The {@link Context} needed to check if BLE is supported
     * @return true if BLE features are supported on this device, false otherwise
     */
    public static boolean isBleSupported(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Check if Bluetooth is enabled and ready to use.
     *
     * @param context The {@link Context} needed to check if Bluetooth is enabled
     * @return true if Bluetooth is enabled, false otherwise
     */
    public static boolean isBluetoothEnabled(Context context) {
        BluetoothAdapter adapter = getBtAdapter(context);
        return adapter != null && adapter.isEnabled();
    }
}
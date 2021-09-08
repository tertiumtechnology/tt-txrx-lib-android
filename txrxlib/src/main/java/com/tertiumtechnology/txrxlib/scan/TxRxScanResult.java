package com.tertiumtechnology.txrxlib.scan;

import android.bluetooth.BluetoothDevice;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * This class represents the result of a scan operation for BLE Devices properties.
 */
public class TxRxScanResult {

    private BluetoothDevice bluetoothDevice;
    private int rssi;
    private int txPower;
    private byte[] scanRecord;

    /**
     * Create a new {@link TxRxScanResult} as a result of a scan operation.
     *
     * @param bluetoothDevice Identifies the remote LE device.
     * @param rssi            The received signal strength in dBm for the remote device. The valid range is [-127, 126].
     * @param scanRecord      The content of the scan record offered by the remote device, which is a
     *                        combination of advertisement and scan response.
     * @param txPower         The transmit power in dBm for the remote device.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public TxRxScanResult(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord, int txPower) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
        this.txPower = txPower;
    }

    /**
     * Create a new {@link TxRxScanResult} as a result of a scan operation.
     *
     * @param bluetoothDevice Identifies the remote LE device.
     * @param rssi            The received signal strength in dBm for the remote device. The valid range is [-127, 126].
     * @param scanRecord      The content of the scan record offered by the remote device, which is a
     *                        combination of advertisement and scan response.
     */
    public TxRxScanResult(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
        this.txPower = Integer.MIN_VALUE;
    }

    /**
     * Returns the remote LE device.
     *
     * @return the remote LE device identified.
     */
    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    /**
     * Returns the received signal strength in dBm for the remote device. The valid range is [-127, 126].
     *
     * @return the received signal strength in dBm for the remote device
     */
    public int getRssi() {
        return rssi;
    }

    /**
     * Returns the the content of the scan record offered by the remote device, which is a combination of
     * advertisement and scan response.
     *
     * @return the content of the scan record offered by the remote device.
     */
    public byte[] getScanRecord() {
        return scanRecord;
    }

    /**
     * Returns the transmit power in dBm for the remote device. A value of {@link Integer#MIN_VALUE} indicates that
     * the TX power is not present.
     *
     * @return the transmit power in dBm for the remote device.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public int getTxPower() {
        return txPower;
    }
}

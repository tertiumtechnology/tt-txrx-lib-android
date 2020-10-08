package com.tertiumtechnology.txrxlib.rw;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * This class provides methods to perform request and receive data from devices which offer TxRx services.
 * Needs an implementation of {@link TxRxDeviceCallback} to manage communication between application and device.
 * <p>
 * <b>Note:</b> Most of the request methods here require
 * {@link android.Manifest.permission#BLUETOOTH} permission.
 *
 * @see TxRxDeviceCallback
 */
public class TxRxDeviceManager {

    static class HandlerWrapper {

        Handler handler;
        HandlerThread handlerThread;

        public void clean() {
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
                handler = null;
            }

            if (handlerThread != null) {
                handlerThread.quitSafely();
                handlerThread = null;
            }
        }

        public void prepare() {
            handlerThread = new HandlerThread("TimeoutHandlerThread");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }

        public void safePostDelayed(Runnable r, long delayMillis) {
            if (handler != null) {
                handler.postDelayed(r, delayMillis);
            }
        }

        public void safeRemoveCallbacks(Runnable r) {
            if (handler != null) {
                handler.removeCallbacks(r);
            }
        }
    }

    /**
     * An Error occurred on connection request
     */
    public static final int ERROR_CONNECT = 1;

    /**
     * Device not found during connection request
     */
    public static final int ERROR_CONNECT_DEVICE_NOT_FOUND = 11;

    /**
     * Invalid connection request, {@link BluetoothAdapter} not initialized
     */
    public static final int ERROR_CONNECT_INVALID_BLUETOOTH_ADAPTER = 12;

    /**
     * Unspecified device address for connection request
     */
    public static final int ERROR_CONNECT_INVALID_DEVICE_ADDRESS = 13;

    /**
     * Invalid disconnection request, {@link BluetoothGatt} not initialized
     */
    public static final int ERROR_DISCONNECT_BLE_NOT_INITIALIZED = 14;

    /**
     * Invalid disconnection request, {@link BluetoothAdapter} not initialized
     */
    public static final int ERROR_DISCONNECT_INVALID_BLUETOOTH_ADAPTER = 15;

    /**
     * An Error occurred on read request
     */
    public static final int ERROR_READ = 2;

    /**
     * Unable to initiate read operation
     */
    public static final int ERROR_READ_BLE_DEVICE_ERROR = 21;

    /**
     * Invalid Tx characteristic for reading
     */
    public static final int ERROR_READ_INVALID_TX_CHARACTERISTIC = 22;

    /**
     * An Error occurred on write request
     */
    public static final int ERROR_WRITE = 3;

    /**
     * Unable to initiate write operation
     */
    public static final int ERROR_WRITE_BLE_DEVICE_ERROR = 31;

    /**
     * Invalid Rx characteristic for writing
     */
    public static final int ERROR_WRITE_INVALID_RX_CHARACTERISTIC = 32;

    /**
     * Invalid request, write operation already in progress
     */
    public static final int ERROR_WRITE_OPERATION_IN_PROGRESS = 33;

    /**
     * An Error occurred on setMode request
     */
    public static final int ERROR_SET_MODE = 4;

    /**
     * Unable to initiate setMode operation
     */
    public static final int ERROR_SET_MODE_BLE_DEVICE_ERROR = 41;

    /**
     * Invalid setMode characteristic
     */
    public static final int ERROR_SET_MODE_INVALID_CHARACTERISTIC = 42;

    /**
     * Invalid request, setMode operation already in progress
     */
    public static final int ERROR_SET_MODE_OPERATION_IN_PROGRESS = 43;

    private static final String TAG = TxRxDeviceManager.class.getSimpleName();

    private static final String TX_RX_TERTIUM_SERVICEUUID = "f3770001-1164-49bc-8f22-0ac34292c217";
    private static final String TX_RX_ACKME_SERVICEUUID = "175f8f23-a570-49bd-9627-815a6a27de2a";
    private static final String ZHAGA_SERVICEUUID = "3cc30001-cb91-4947-bd12-80d2f0535a30";

    private static ArrayList<TxRxDeviceProfile> txRxProfiles = new ArrayList<>();

    static {
        // TxRxTertium
        txRxProfiles.add(new TxRxDeviceProfile(
                TX_RX_TERTIUM_SERVICEUUID,
                "f3770002-1164-49bc-8f22-0ac34292c217",
                "f3770003-1164-49bc-8f22-0ac34292c217",
                "",
                TxRxDeviceProfile.TerminatorType.CRLF,
                TxRxDeviceProfile.TerminatorType.NONE,
                128, 20));

        // TxRxAckme
        txRxProfiles.add(new TxRxDeviceProfile(
                TX_RX_ACKME_SERVICEUUID,
                "1cce1ea8-bd34-4813-a00a-c76e028fadcb",
                "cacc07ff-ffff-4c48-8fae-a9ef71b75e26",
                "20b9794f-da1a-4d14-8014-a0fb9cefb2f7",
                TxRxDeviceProfile.TerminatorType.CRLF,
                TxRxDeviceProfile.TerminatorType.NONE,
                15, 20));

        // Zhaga
        txRxProfiles.add(new TxRxDeviceProfile(
                ZHAGA_SERVICEUUID,
                "3cc30002-cb91-4947-bd12-80d2f0535a30",
                "3cc30003-cb91-4947-bd12-80d2f0535a30",
                "",
                TxRxDeviceProfile.TerminatorType.CRLF,
                TxRxDeviceProfile.TerminatorType.NONE,
                240, 240));
    }

    private final Runnable connectionTimeoutRunnable;
    private final Runnable readTimeoutRunnable;
    private final Runnable successfulNotifyTimeoutRunnable;
    private final Runnable successfulReadTimeoutRunnable;
    private final Runnable writeTimeoutRunnable;
    private final Runnable setModeTimeoutRunnable;

    private TxRxDeviceProfile connectedProfile;
    private TxRxTimeouts txRxTimeouts;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private TxRxDeviceCallback deviceCallback;
    private HandlerWrapper handlerWrapper;

    private StringBuilder notifyAccumulator;

    private BluetoothGattCharacteristic readCharacteristic;
    private String readTerminator;
    private StringBuilder readAccumulator;

    private BluetoothGattCharacteristic writeCharacteristic;
    private String writeTerminator;
    private Iterator<byte[]> chunksIterator;
    private int writePacketSize;
    private boolean isWriting;

    private BluetoothGattCharacteristic setModeCharacteristic;
    private boolean isSettingMode;

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String uuid = characteristic.getUuid().toString();

            if (!connectedProfile.getSetModeCharacteristicUUID().equals(uuid)) {
                accumulateValues(characteristic.getStringValue(0), notifyAccumulator, successfulNotifyTimeoutRunnable);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                accumulateValues(characteristic.getStringValue(0), readAccumulator, successfulReadTimeoutRunnable);
            }
            else {
                Log.w(TAG, "Unable to read: " + status);
                readAccumulator.setLength(0);
                handlerWrapper.safeRemoveCallbacks(successfulReadTimeoutRunnable);
                deviceCallback.onReadError(ERROR_READ);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String uuid = characteristic.getUuid().toString();

            if (connectedProfile.getSetModeCharacteristicUUID().equals(uuid)) {
                // is setMode
                handlerWrapper.safeRemoveCallbacks(setModeTimeoutRunnable);

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Integer currentMode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Log.i(TAG,
                            "SetMode characteristic value written: " + currentMode);
                    isSettingMode = false;
                    deviceCallback.onSetMode(currentMode);
                }
                else {
                    Log.w(TAG, "Unable to setMode: " + status);
                    isSettingMode = false;
                    deviceCallback.onSetModeError(TxRxDeviceManager.ERROR_SET_MODE);
                }
            }
            else {// is write
                handlerWrapper.safeRemoveCallbacks(writeTimeoutRunnable);

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "Characteristic value written: " + characteristic.getStringValue(0));

                    if (chunksIterator.hasNext()) {

                        if (writeCharacteristic.setValue(chunksIterator.next())
                                && bluetoothGatt.writeCharacteristic(writeCharacteristic)) {
                            handlerWrapper.safePostDelayed(writeTimeoutRunnable, txRxTimeouts.getWriteTimeout());
                        }
                        else {
                            Log.w(TAG, "Unable to continue write operation");
                            isWriting = false;
                            deviceCallback.onWriteError(TxRxDeviceManager.ERROR_WRITE);
                        }
                    }
                    else {
                        isWriting = false;
                        deviceCallback.onWriteData(characteristic.getStringValue(0));
                    }
                }
                else {
                    Log.w(TAG, "Unable to write: " + status);
                    isWriting = false;
                    deviceCallback.onWriteError(TxRxDeviceManager.ERROR_WRITE);
                }
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            handlerWrapper.safeRemoveCallbacks(connectionTimeoutRunnable);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Connected to GATT server");

                    Log.i(TAG, "Attempting to start service discovery");
                    bluetoothGatt.discoverServices();

                    deviceCallback.onDeviceConnected();
                }
                else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Disconnected from GATT server");

                    deviceCallback.onDeviceDisconnected();
                }
                else {
                    Log.i(TAG, "Other connection state found: " + newState);

                    if (newState != BluetoothProfile.STATE_CONNECTING && newState != BluetoothProfile
                            .STATE_DISCONNECTING) {
                        deviceCallback.onConnectionError(TxRxDeviceManager.ERROR_CONNECT);
                    }
                }
            }
            else {
                deviceCallback.onConnectionError(TxRxDeviceManager.ERROR_CONNECT);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (TxRxDeviceProfile profile : txRxProfiles) {
                    //TxRxService
                    BluetoothGattService service = gatt.getService(UUID.fromString(profile.getTxRxServiceUuid()));
                    if (service != null) {
                        // TxCharacteristic - read
                        readCharacteristic = service.getCharacteristic(UUID.fromString(profile
                                .getTxCharacteristicUUID()));
                        // RxCharacteristic - write
                        writeCharacteristic = service.getCharacteristic(UUID.fromString(profile
                                .getRxCharacteristicUUID()));

                        boolean validSetMode = true;
                        String setModeCharacteristicUUID = profile.getSetModeCharacteristicUUID();

                        if (!TextUtils.isEmpty(setModeCharacteristicUUID)) {
                            // setModeCharacteristic
                            setModeCharacteristic =
                                    service.getCharacteristic(UUID.fromString(setModeCharacteristicUUID));
                            validSetMode = setModeCharacteristic != null;
                        }

                        if (readCharacteristic != null && writeCharacteristic != null && validSetMode) {

                            for (BluetoothGattDescriptor descriptor : readCharacteristic.getDescriptors()) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                bluetoothGatt.writeDescriptor(descriptor);
                            }

                            bluetoothGatt.setCharacteristicNotification(readCharacteristic, true);
                            bluetoothGatt.setCharacteristicNotification(writeCharacteristic, true);

//                            if (setModeCharacteristic != null) {
//                                bluetoothGatt.setCharacteristicNotification(setModeCharacteristic, true);
//
//                                for (BluetoothGattDescriptor descriptor : setModeCharacteristic.getDescriptors()) {
//                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
//                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                                    bluetoothGatt.writeDescriptor(descriptor);
//                                }
//                            }

                            readTerminator = profile.getTxTerminatorType().getValue();
                            writeTerminator = profile.getRxTerminatorType().getValue();

                            writePacketSize = profile.getRxPacketSize();

                            connectedProfile = profile;

                            deviceCallback.onTxRxServiceDiscovered();

                            return;
                        }
                    }
                }
            }
            else {
                Log.w(TAG, "No services discovered");
            }

            deviceCallback.onTxRxServiceNotFound();
        }

        private void accumulateValues(String currentValue, StringBuilder accumulator, Runnable successfulCallback) {
            handlerWrapper.safeRemoveCallbacks(readTimeoutRunnable);
            handlerWrapper.safeRemoveCallbacks(successfulCallback);

            Log.i(TAG, "Accumulating characteristic values, current is: " + currentValue);

            accumulator.append(currentValue);

            handlerWrapper.safePostDelayed(successfulCallback, txRxTimeouts.getLaterReadTimeout());
        }
    };

    /**
     * Create a new {@link TxRxDeviceManager} to handle communication with a device.
     * <br>
     * Use default {@link TxRxTimeouts} during device communication
     *
     * @param bluetoothAdapter {@link BluetoothAdapter} used to perform BLE task
     * @param deviceCallback   {@link TxRxDeviceCallback} callback used to notify data and request results
     */
    public TxRxDeviceManager(BluetoothAdapter bluetoothAdapter, TxRxDeviceCallback deviceCallback) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.deviceCallback = deviceCallback;

        handlerWrapper = new HandlerWrapper();

        this.isWriting = false;
        this.readAccumulator = new StringBuilder();
        this.notifyAccumulator = new StringBuilder();

        connectionTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                disconnect();
                TxRxDeviceManager.this.deviceCallback.onConnectionTimeout();
                Log.w(TAG, "Connection failed: timeout!");
            }
        };

        writeTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                isWriting = false;
                TxRxDeviceManager.this.deviceCallback.onWriteTimeout();
                Log.w(TAG, "Write failed: timeout!");
            }
        };

        readTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                TxRxDeviceManager.this.deviceCallback.onReadNotifyTimeout();
                Log.w(TAG, "Read/Notify failed: timeout!");
            }
        };

        setModeTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                isSettingMode = false;
                TxRxDeviceManager.this.deviceCallback.onSetModeTimeout();
                Log.w(TAG, "SetMode failed: timeout!");
            }
        };

        successfulReadTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                String completeReadValue = readAccumulator.toString();
                TxRxDeviceManager.this.readAccumulator.setLength(0);

                TxRxDeviceManager.this.deviceCallback.onReadData(completeReadValue + readTerminator);
                Log.i(TAG, "Read complete, characteristic value is: " + completeReadValue);
            }
        };

        successfulNotifyTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                String completeNotifyValue = notifyAccumulator.toString();
                TxRxDeviceManager.this.notifyAccumulator.setLength(0);

                TxRxDeviceManager.this.deviceCallback.onNotifyData(completeNotifyValue + readTerminator);
                Log.i(TAG, "Notify complete, characteristic value is: " + completeNotifyValue);
            }
        };

        this.txRxTimeouts = TxRxTimeouts.getDefaultTimeouts();
    }

    /**
     * Create a new {@link TxRxDeviceManager} to handle communication with a device.
     *
     * @param bluetoothAdapter {@link BluetoothAdapter} used to perform BLE task
     * @param deviceCallback   {@link TxRxDeviceCallback} callback used to notify data and request results
     * @param txRxTimeouts     {@link TxRxTimeouts} used during device communication
     */
    public TxRxDeviceManager(BluetoothAdapter bluetoothAdapter, TxRxDeviceCallback deviceCallback, TxRxTimeouts
            txRxTimeouts) {
        this(bluetoothAdapter, deviceCallback);
        this.txRxTimeouts = txRxTimeouts;
    }

    /**
     * Closes this {@link TxRxDeviceManager} when every communication ends.
     * <p>
     * This method should be called as early as possible when there is no need for any further communication.
     * <p>
     * <b>The method {@link TxRxDeviceManager#disconnect()} must be called before invoking this method.</b>
     */
    public synchronized void close() {
        Log.i(TAG, "Request close");

        cleanState();

        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    /**
     * Initiate a connection to a device and discover services on successful connection.
     * <p>
     * Invoking this method will close any connection previously opened and will stop any pending request to the device.
     * <p>
     * The connection may not be established right away, but will be
     * completed when the remote device is available.
     * <p>
     * A {@link TxRxDeviceCallback#onDeviceConnected()} callback will be
     * invoked on a successful connection.
     * <br/>
     * Otherwise a {@link TxRxDeviceCallback#onConnectionError(int)} callback will be invoked on connection error
     * or a {@link TxRxDeviceCallback#onConnectionTimeout()} callback on connection timeout.
     * <p>
     * Just after the connection has been established, a service discovery will start in order to verify that the
     * connected device supports TxRx services.
     * <p>
     * A {@link TxRxDeviceCallback#onTxRxServiceDiscovered()} callback will be
     * invoked if the device support TxRx services.
     * <br/>
     * Otherwise a {@link TxRxDeviceCallback#onTxRxServiceNotFound()} callback will be invoked.
     * <p>
     * Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     *
     * @param address The device Bluetooth address as a string
     * @param context The {@link Context} needed to start connection request
     * @return true if the connect operation was initiated successfully, false otherwise.
     */
    public synchronized boolean connect(String address, Context context) {
        close();

        handlerWrapper.prepare();

        handlerWrapper.safePostDelayed(connectionTimeoutRunnable, txRxTimeouts.getConnectTimeout());

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Invalid BluetoothAdapter");
            deviceCallback.onConnectionError(ERROR_CONNECT_INVALID_BLUETOOTH_ADAPTER);
            return false;
        }

        if (address == null) {
            Log.e(TAG, "Unspecified device address");
            deviceCallback.onConnectionError(ERROR_CONNECT_INVALID_DEVICE_ADDRESS);
            return false;
        }

        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found");
            deviceCallback.onConnectionError(ERROR_CONNECT_DEVICE_NOT_FOUND);
            return false;
        }

        bluetoothGatt = device.connectGatt(context, false, gattCallback);
        return true;
    }

    /**
     * Disconnects from a previously connected device, or cancels a connection attempt
     * in progress.
     * <p>
     * Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     */
    public synchronized void disconnect() {
        Log.i(TAG, "Request disconnect");

        if (bluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            deviceCallback.onConnectionError(ERROR_DISCONNECT_INVALID_BLUETOOTH_ADAPTER);
            return;
        }

        if (bluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            deviceCallback.onConnectionError(ERROR_DISCONNECT_BLE_NOT_INITIALIZED);
            return;
        }

        cleanState();

        bluetoothGatt.disconnect();
    }

    /**
     * Check if the connected device is a TxRxAckme device
     * <p>
     *
     * @return true if the connected device is a TxRxAckme, false otherwise.
     */

    public boolean isTxRxAckme() {
        return connectedProfile != null && TX_RX_ACKME_SERVICEUUID.equals(connectedProfile.getTxRxServiceUuid());
    }

    /**
     * Check if the device with the specified <b>address</b> is currently connected
     * <p>
     *
     * @param address The device Bluetooth address as a string
     * @param context The {@link Context} needed to check the device connection status
     * @return true if the device is connected, false otherwise.
     */
    public boolean isConnected(String address, Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        List<BluetoothDevice> connectedDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice device : connectedDevices) {
            if (device.getAddress().equals(address)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Send a read request to device.
     * <p>
     * A {@link TxRxDeviceCallback#onReadData(String)} callback will be invoked when the read operation will be
     * completed,
     * reporting the result of the read operation
     * <p>
     * Otherwise a {@link TxRxDeviceCallback#onReadError(int)} callback will be invoked on read error.
     * <p>
     * Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     *
     * @return true if the read operation was initiated successfully, false otherwise.
     */
    public synchronized boolean requestReadData() {
        Log.i(TAG, "Start read request");

        if (readCharacteristic == null) {
            Log.w(TAG, "Invalid read characteristic");
            deviceCallback.onReadError(ERROR_READ_INVALID_TX_CHARACTERISTIC);
            return false;
        }

        boolean readInitiated = bluetoothGatt.readCharacteristic(readCharacteristic);

        if (!readInitiated) {
            Log.w(TAG, "Unable to initiate read operation");
            deviceCallback.onReadError(ERROR_READ_BLE_DEVICE_ERROR);
        }

        return readInitiated;
    }

    /**
     * Set the operation mode to use during the communication with the device.
     * <p>
     * A {@link TxRxDeviceCallback#onSetMode(int)} callback will be invoked when the operation will be
     * completed,
     * reporting the result of the SetMode operation
     * <p>
     * Otherwise a {@link TxRxDeviceCallback#onSetModeError(int)} callback will be invoked on SetMode error.
     * <p>
     * Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     *
     * @param mode String the operation mode to apply
     * @return true if the operation mode can be set and the SetMode operation was initiated successfully, false
     * otherwise.
     */
    public synchronized boolean requestSetMode(int mode) {
        Log.i(TAG, "Start setMode request for operation: " + mode);

        if (setModeCharacteristic == null) {
            Log.w(TAG, "Invalid setMode characteristic");
            deviceCallback.onSetModeError(ERROR_SET_MODE_INVALID_CHARACTERISTIC);
            return false;
        }

        if (isSettingMode) {
            Log.w(TAG, "SetMode operation already initiated, currently in progress");
            deviceCallback.onSetModeError(ERROR_SET_MODE_OPERATION_IN_PROGRESS);
            return false;
        }

        //mode += writeTerminator;

        byte[] modeByte = {(byte) mode};

        boolean setModeInitiated = setModeCharacteristic.setValue(modeByte)
                && bluetoothGatt.writeCharacteristic(setModeCharacteristic);

        if (setModeInitiated) {
            isSettingMode = true;

            handlerWrapper.safePostDelayed(setModeTimeoutRunnable, txRxTimeouts.getWriteTimeout());
        }
        else {
            Log.w(TAG, "Unable to initiate setMode operation");
            deviceCallback.onSetModeError(ERROR_SET_MODE_BLE_DEVICE_ERROR);
        }

        return setModeInitiated;
    }

    /**
     * Send a write request to device.
     * <p>
     * A {@link TxRxDeviceCallback#onWriteData(String)} callback will be invoked when the write operation will be
     * completed,
     * reporting the result of the write operation
     * <p>
     * Otherwise a {@link TxRxDeviceCallback#onWriteError(int)} callback will be invoked on write error.
     * <p>
     * Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     *
     * @param data String data to write
     * @return true if the data can be set and the write operation was initiated successfully, false otherwise.
     */
    public synchronized boolean requestWriteData(String data) {
        Log.i(TAG, "Start write request for data: " + data);

        if (writeCharacteristic == null) {
            Log.w(TAG, "Invalid write characteristic");
            deviceCallback.onWriteError(ERROR_WRITE_INVALID_RX_CHARACTERISTIC);
            return false;
        }

        if (isWriting) {
            Log.w(TAG, "Write operation already initiated, currently in progress");
            deviceCallback.onWriteError(ERROR_WRITE_OPERATION_IN_PROGRESS);
            return false;
        }

        data += writeTerminator;

        initChunksIterator(data.getBytes());

        boolean writeInitiated = writeCharacteristic.setValue(chunksIterator.next())
                && bluetoothGatt.writeCharacteristic(writeCharacteristic);

        if (writeInitiated) {
            isWriting = true;
            handlerWrapper.safePostDelayed(writeTimeoutRunnable, txRxTimeouts.getWriteTimeout());
            handlerWrapper.safePostDelayed(readTimeoutRunnable, txRxTimeouts.getFirstReadTimeout());
        }
        else {
            Log.w(TAG, "Unable to initiate write operation");
            deviceCallback.onWriteError(ERROR_WRITE_BLE_DEVICE_ERROR);
        }

        return writeInitiated;
    }

    /**
     * Set the {@link TxRxTimeouts} used during device communication
     *
     * @param txRxTimeouts used during device communication
     */
    public void setTxRxTimeouts(TxRxTimeouts txRxTimeouts) {
        this.txRxTimeouts = txRxTimeouts;
    }

    private void cleanState() {
        handlerWrapper.clean();

        isWriting = false;
        readAccumulator.setLength(0);
        notifyAccumulator.setLength(0);

        connectedProfile = null;
    }

    private void initChunksIterator(byte[] dataBytes) {
        ArrayList<byte[]> chunksArray = new ArrayList<>();
        int numberOfChunks = (dataBytes.length + writePacketSize - 1) / writePacketSize;

        for (int i = 0; i < numberOfChunks; i++) {
            int start = i * writePacketSize;
            int limit = start + Math.min(writePacketSize, dataBytes.length - start);

            chunksArray.add(Arrays.copyOfRange(dataBytes, start, limit));
        }

        chunksIterator = chunksArray.iterator();
    }
}
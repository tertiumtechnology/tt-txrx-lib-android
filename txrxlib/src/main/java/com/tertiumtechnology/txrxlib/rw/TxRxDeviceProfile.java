package com.tertiumtechnology.txrxlib.rw;

/**
 * This class represents a BLE device profile, used to manage different devices properties.
 */
public class TxRxDeviceProfile {

    // package private enum
    enum TerminatorType {
        NONE(""), CR("\r"), LF("\n"), CRLF("\r\n"), ZERO("\0");

        private String value;

        TerminatorType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private String txRxServiceUuid;
    // read characteristic
    private String txCharacteristicUUID;
    // write characteristic
    private String rxCharacteristicUUID;
    // setMode characteristic
    private String setModeCharacteristicUUID;
    // event characteristic
    private String eventCharacteristicUUID;
    // read terminator
    private TerminatorType txTerminatorType;
    // write terminator
    private TerminatorType rxTerminatorType;
    // read packet maximum size
    private int txPacketSize;
    // write packet maximum size
    private int rxPacketSize;

    /**
     * Create a new {@link TxRxDeviceProfile} in order to manage device properties.
     *
     * @param txRxServiceUuid           String the device Service UUID
     * @param rxCharacteristicUUID      String the Rx characteristic UUID
     * @param txCharacteristicUUID      String the Tx characteristic UUID
     * @param setModeCharacteristicUUID String the SetMode characteristic UUID
     * @param eventCharacteristicUUID   String the Event characteristic UUID
     * @param rxTerminatorType          {@link TerminatorType} represents the terminator type appended at the end of
     *                                  the write request
     * @param txTerminatorType          {@link TerminatorType} represents the terminator type appended at the end of
     *                                  the read/notified message
     * @param rxPacketSize              int the maximum rx packet size, used in write operation
     * @param txPacketSize              int the maximum tx packet size, used in read operation
     */
    public TxRxDeviceProfile(String txRxServiceUuid, String rxCharacteristicUUID, String txCharacteristicUUID,
                             String setModeCharacteristicUUID, String eventCharacteristicUUID,
                             TerminatorType rxTerminatorType,
                             TerminatorType txTerminatorType, int rxPacketSize,
                             int txPacketSize) {
        this.txRxServiceUuid = txRxServiceUuid;
        this.txCharacteristicUUID = txCharacteristicUUID;
        this.rxCharacteristicUUID = rxCharacteristicUUID;
        this.setModeCharacteristicUUID = setModeCharacteristicUUID;
        this.eventCharacteristicUUID = eventCharacteristicUUID;
        this.txTerminatorType = txTerminatorType;
        this.rxTerminatorType = rxTerminatorType;
        this.txPacketSize = txPacketSize;
        this.rxPacketSize = rxPacketSize;
    }

    /**
     * Returns the Event characteristic UUID
     *
     * @return a String representing the Event characteristic UUID
     */
    public String getEventCharacteristicUUID() {
        return eventCharacteristicUUID;
    }

    /**
     * Returns the Rx characteristic UUID, used in write operations
     *
     * @return a String representing the Rx characteristic UUID
     */
    public String getRxCharacteristicUUID() {
        return rxCharacteristicUUID;
    }

    /**
     * Returns the maximum Rx packet size, used in write operation
     *
     * @return an int representing the maximum Tx packet size
     */
    public int getRxPacketSize() {
        return rxPacketSize;
    }

    /**
     * Returns a {@link TerminatorType}, appended at the end of the write request before sending it to the device
     *
     * @return the terminator type appended at the end of the write request
     */
    public TerminatorType getRxTerminatorType() {
        return rxTerminatorType;
    }

    /**
     * Returns the SetMode characteristic UUID, used for set the operation mode
     *
     * @return a String representing the SetMode characteristic UUID
     */
    public String getSetModeCharacteristicUUID() {
        return setModeCharacteristicUUID;
    }

    /**
     * Returns the Tx characteristic UUID, used in read operations
     *
     * @return a String representing the Tx characteristic UUID
     */
    public String getTxCharacteristicUUID() {
        return txCharacteristicUUID;
    }

    /**
     * Returns the maximum Tx packet size, used in read operation
     *
     * @return an int representing the maximum Rx packet size
     */
    public int getTxPacketSize() {
        return txPacketSize;
    }

    /**
     * Returns the device Service UUID
     *
     * @return a String representing the device Service UUID
     */
    public String getTxRxServiceUuid() {
        return txRxServiceUuid;
    }

    /**
     * Returns a {@link TerminatorType}, appended at the end of the read/notified message received from the device
     *
     * @return the terminator type appended at the end of the read/notified message
     */
    public TerminatorType getTxTerminatorType() {
        return txTerminatorType;
    }
}

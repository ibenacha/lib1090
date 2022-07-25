package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * BDS 1,7
 */
public class CommonUsageGICBCapabilityReport extends ModeSDownlinkMsg implements Serializable {

    // Fields
    // ------

    // Flight Status
    private byte flightStatus;
    // Downlink Request
    private byte downlinkRequest;
    // Utility Message
    private byte utilityMsg;
    // Altitude Code (if DF20 otherwise null)
    private Short altitudeCode;
    // Identity (if DF21 otherwise null)
    private Short identity;
    // Common Usage GICB Capability Report
    private Map<String, Boolean> commonUsageGICBCapabilityReport;

    // Constructors
    // ------------

    /** protected no-arg constructor e.g. for serialization with Kryo **/
    protected CommonUsageGICBCapabilityReport() {
    }

    public CommonUsageGICBCapabilityReport(String raw_message) throws UnspecifiedFormatError, BadFormatException {
        this(new ModeSDownlinkMsg(raw_message));
    }

    public CommonUsageGICBCapabilityReport(byte[] raw_message) throws UnspecifiedFormatError, BadFormatException {
        this(new ModeSDownlinkMsg(raw_message));
    }

    public CommonUsageGICBCapabilityReport(ModeSDownlinkMsg reply) throws BadFormatException {

        super(reply);
        //setType(subtype.COMMON_USAGE_GICB_CAPABILITY_REPORT);

        byte[] payload = getPayload();
        byte[] message = new byte[7];
        System.arraycopy(payload, 3, message, 0, 7);

        if (reply.getDownlinkFormat() == 20) {
            this.altitudeCode = extractAltitudeCode(payload);
            this.identity = null;
        } else if (reply.getDownlinkFormat() == 21) {
            this.altitudeCode = null;
            this.identity = (short) extractIdentity(payload);
        } else {
            throw new BadFormatException("Message is not an altitude reply or an identity reply !");
        }

        this.flightStatus = getFirstField();
        this.downlinkRequest = extractDownlinkRequest(payload);
        this.utilityMsg = extractUtilityMessage(payload);

        this.commonUsageGICBCapabilityReport = extractCommonGICBCapabilityReport(message);

    }

    // Getters
    // -------

    public byte getFlightStatus() {
        return flightStatus;
    }

    public byte getDownlinkRequest() {
        return downlinkRequest;
    }

    public byte getUtilityMsg() {
        return utilityMsg;
    }

    public Short getAltitudeCode() {
        return altitudeCode;
    }

    public Short getIdentity() {
        return identity;
    }

    public Map<String, Boolean> getCommonUsageGICBCapabilityReport() {
        return commonUsageGICBCapabilityReport;
    }

    // Public static methods
    // ---------------------

    public static Map<String, Boolean> extractCommonGICBCapabilityReport(byte[] message) {

        Map<String, Boolean> map = new HashMap<>();

        // BDS 0,5 Extended Squitter Airborne Position
        map.put("BDS05", ((message[0] >>> 7) & 0x1) == 1);
        // BDS 0,6 Extended Squitter Surface Position
        map.put("BDS06", ((message[0] >>> 6) & 0x1) == 1);
        // BDS 0,7 Extended Squitter Status
        map.put("BDS07", ((message[0] >>> 5) & 0x1) == 1);
        // BDS 0,8 Extended Squitter Identification and Category
        map.put("BDS08", ((message[0] >>> 4) & 0x1) == 1);
        // BDS 0,9 Extended Squitter Airborne Velocity Information
        map.put("BDS09", ((message[0] >>> 3) & 0x1) == 1);
        // BDS 0,A Extended Squitter Event-Driven Information
        map.put("BDS0A", ((message[0] >>> 2) & 0x1) == 1);
        // 2,0 Aircraft identification
        map.put("BDS20", ((message[0] >>> 1) & 0x1) == 1);
        // 2,1 Aircraft registration number
        map.put("BDS21", (message[0] & 0x1) == 1);
        // 4,0 Selected vertical intention
        map.put("BDS40", ((message[1] >>> 7) & 0x1) == 1);
        // 4,1 Next waypoint identifier
        map.put("BDS41", ((message[1] >>> 6) & 0x1) == 1);
        // 4,2 Next waypoint position
        map.put("BDS42", ((message[1] >>> 5) & 0x1) == 1);
        // 4,3 Next waypoint information
        map.put("BDS43", ((message[1] >>> 4) & 0x1) == 1);
        // 4,4 Meteorological routine report
        map.put("BDS44", ((message[1] >>> 3) & 0x1) == 1);
        // 4,5 Meteorological hazard report
        map.put("BDS45", ((message[1] >>> 2) & 0x1) == 1);
        // 4,8 VHF channel report
        map.put("BDS48", ((message[1] >>> 1) & 0x1) == 1);
        // 5,0 Track and turn report
        map.put("BDS50", (message[1] & 0x1) == 1);
        // 5,1 Position coarse
        map.put("BDS51", ((message[2] >>> 7) & 0x1) == 1);
        // 5,2 Position fine
        map.put("BDS52", ((message[2] >>> 6) & 0x1) == 1);
        // 5,3 Air-referenced state vector
        map.put("BDS53", ((message[2] >>> 5) & 0x1) == 1);
        // 5,4 Waypoint 1
        map.put("BDS54", ((message[2] >>> 4) & 0x1) == 1);
        // 5,5 Waypoint 2
        map.put("BDS55", ((message[2] >>> 3) & 0x1) == 1);
        // 5,6 Waypoint 3
        map.put("BDS56", ((message[2] >>> 2) & 0x1) == 1);
        // 5,F Quasi-static parameter monitoring
        map.put("BDS5F", ((message[2] >>> 1) & 0x1) == 1);
        // 6,0 Heading and speed report
        map.put("BDS60", (message[2] & 0x1) == 1);
        // E,1 Reserved for Mode S BITE (Built In Test Equipment)
        map.put("BDSE1", ((message[3] >>> 5) & 0x1) == 1);
        // E,2 Reserved for Mode S BITE (Built In Test Equipment)
        map.put("BDSE2", ((message[3] >>> 4) & 0x1) == 1);
        // F,1 Military applications
        map.put("BDSF1", ((message[3] >>> 3) & 0x1) == 1);

        return map;

    }

    // Private static methods
    // ----------------------

    private short extractIdentity(byte[] payload) {
        return (short) ((payload[1] << 8 | (payload[2] & 0xFF)) & 0x1FFF);
    }

    private static short extractAltitudeCode(byte[] payload) {
        return (short) ((payload[1] << 8 | payload[2] & 0xFF) & 0x1FFF);
    }

    private static byte extractUtilityMessage(byte[] payload) {
        return (byte) ((payload[0]&0x7)<<3 | (payload[1]>>>5)&0x7);
    }

    private static byte extractDownlinkRequest(byte[] payload) {
        return (byte) ((payload[0]>>>3) & 0x1F);
    }

    // Override
    // --------

    @Override
    public String toString() {
        return "CommonUsageGICBCapabilityReport{" +
                "flightStatus=" + flightStatus +
                ", downlinkRequest=" + downlinkRequest +
                ", utilityMsg=" + utilityMsg +
                ", altitudeCode=" + altitudeCode +
                ", identity=" + identity +
                ", commonUsageGICBCapabilityReport=" + commonUsageGICBCapabilityReport +
                '}';
    }

}
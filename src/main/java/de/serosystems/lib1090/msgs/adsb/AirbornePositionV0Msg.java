package de.serosystems.lib1090.msgs.adsb;

import de.serosystems.lib1090.CompactPositionReporting;
import de.serosystems.lib1090.Position;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.PositionMsg;
import de.serosystems.lib1090.msgs.modes.ExtendedSquitter;

import java.io.Serializable;

/*
 *  This file is part of de.serosystems.lib1090.
 *
 *  de.serosystems.lib1090 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  de.serosystems.lib1090 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with de.serosystems.lib1090.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Decoder for ADS-B airborne position messages version 0 and 1.
 * @author Matthias Schäfer (schaefer@sero-systems.de)
 */
public class AirbornePositionV0Msg extends ExtendedSquitter implements Serializable, PositionMsg {

	private static final long serialVersionUID = 5661463389938495220L;

	private boolean horizontal_position_available;
	private boolean altitude_available;
	private byte surveillance_status;
	private boolean nic_suppl_b;
	private short altitude_encoded;
	private boolean time_flag;
	private CompactPositionReporting.CPREncodedPosition position;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected AirbornePositionV0Msg() { }

	/**
	 * @param raw_message raw ADS-B airborne position message as hex string
	 * @param timestamp timestamp for this position message in milliseconds; will use {@link System#currentTimeMillis()} if null
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public AirbornePositionV0Msg(String raw_message, Long timestamp) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message), timestamp);
	}

	/**
	 * @param raw_message raw ADS-B airborne position message as byte array
	 * @param timestamp timestamp for this position message in milliseconds; will use {@link System#currentTimeMillis()} if null
	 * @throws BadFormatException if message has wrong format
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public AirbornePositionV0Msg(byte[] raw_message, Long timestamp) throws BadFormatException, UnspecifiedFormatError {
		this(new ExtendedSquitter(raw_message), timestamp);
	}

	/**
	 * @param squitter extended squitter containing the airborne position msg
	 * @param timestamp timestamp for this position message in milliseconds; will use {@link System#currentTimeMillis()} if null
	 * @throws BadFormatException if message has wrong format
	 */
	public AirbornePositionV0Msg(ExtendedSquitter squitter, Long timestamp) throws BadFormatException {
		super(squitter);

		setType(subtype.ADSB_AIRBORN_POSITION_V0);

		if (!(getFormatTypeCode() == 0 ||
				(getFormatTypeCode() >= 9 && getFormatTypeCode() <= 18) ||
				(getFormatTypeCode() >= 20 && getFormatTypeCode() <= 22)))
			throw new BadFormatException("This is not a position message! Wrong format type code ("+getFormatTypeCode()+").");

		byte[] msg = getMessage();

		horizontal_position_available = getFormatTypeCode() != 0;

		surveillance_status = (byte) ((msg[0]>>>1)&0x3);
		nic_suppl_b = (msg[0]&0x1) == 1;

		altitude_encoded = (short) (((msg[1]<<4)|((msg[2]>>>4)&0xF))&0xFFF);
		altitude_available = altitude_encoded != 0;

		time_flag = ((msg[2]>>>3)&0x1) == 1;

		boolean cpr_format = ((msg[2]>>>2)&0x1) == 1;
		int cpr_encoded_lat = (((msg[2]&0x3)<<15) | ((msg[3]&0xFF)<<7) | ((msg[4]>>>1)&0x7F)) & 0x1FFFF;
		int cpr_encoded_lon = (((msg[4]&0x1)<<16) | ((msg[5]&0xFF)<<8) | (msg[6]&0xFF)) & 0x1FFFF;
		position = new CompactPositionReporting.CPREncodedPosition(
				cpr_format, cpr_encoded_lat, cpr_encoded_lon, 17, false,
				timestamp == null ? System.currentTimeMillis() : timestamp);
	}

	/**
	 * The position error, i.e., 95% accuracy for the horizontal position. Values according to DO-260B Table N-4.
	 *
	 *  The horizontal containment radius is also known as "horizontal protection level".
	 *
	 * @return horizontal containment radius limit in meters. A return value of -1 means "unkown".
	 */
	public double getHorizontalContainmentRadiusLimit() {
		switch (getFormatTypeCode()) {
			case 0: case 18: case 22: return -1;
			case 9: case 20: return 7.5;
			case 10: case 21: return 25;
			case 11: return 185.2;
			case 12: return 370.4;
			case 13: return 926;
			case 14: return 1852;
			case 15: return 3704;
			case 16: return 18520;
			case 17: return 37040;
			default: return -1;
		}
	}

	/**
	 * Navigation accuracy category according to DO-260B Table N-7. In ADS-B version 1+ this information is contained
	 * in the operational status message. For version 0 it is derived from the format type code.
	 *
	 * For a value in meters, use {@link #getPositionUncertainty()}.
	 *
	 * @return NACp according value (no unit), comparable to NACp in {@link AirborneOperationalStatusV2Msg} and
	 * {@link AirborneOperationalStatusV1Msg}.
	 */
	public byte getNACp() {
		switch (getFormatTypeCode()) {
			case 0: case 18: case 22: return 0;
			case 9: case 20: return 11;
			case 10: case 21: return 10;
			case 11: return 8;
			case 12: return 7;
			case 13: return 6;
			case 14: return 5;
			case 15: return 4;
			case 16: case 17: return 1;
			default: return 0;
		}
	}

	/**
	 * Get the 95% horizontal accuracy bounds (EPU) derived from NACp value in meter, see table N-7 in RCTA DO-260B.
	 *
	 * The concept of NACp has been introduced in ADS-B version 1. For version 0 transmitters, a mapping exists which
	 * is reflected by this method.
	 * Values are comparable to those of {@link AirborneOperationalStatusV1Msg}'s and
	 * {@link AirborneOperationalStatusV2Msg}'s getPositionUncertainty method for aircraft supporting ADS-B
	 * version 1 and 2.
	 *
	 * @return the estimated position uncertainty according to the position NAC in meters (-1 for unknown)
	 */
	public double getPositionUncertainty() {
		switch (getFormatTypeCode()) {
			case 0: case 18: case 22: return -1;
			case 9: return 3;
			case 10: return 10;
			case 11: return 92.6;
			case 12: return 185.2;
			case 13: return 463;
			case 14: return 926;
			case 15: return 1852;
			case 16: return 9260;
			case 17: return 18520;
			default: return -1;
		}
	}

	/**
	 * @return Navigation integrity category. A NIC of 0 means "unkown".
	 */
	public byte getNIC() {
		switch (getFormatTypeCode()) {
			case 0: case 18: case 22: return 0;
			case 9: case 20: return 11;
			case 10: case 21: return 10;
			case 11: return 9;
			case 12: return 7;
			case 13: return 6;
			case 14: return 5;
			case 15: return 4;
			case 16: return 3;
			case 17: return 1;
			default: return 0;
		}
	}

	/**
	 * Source/Surveillance Integrity Level (SIL) according to DO-260B Table N-8.
	 *
	 * The concept of SIL has been introduced in ADS-B version 1. For version 0 transmitters, a mapping exists which
	 * is reflected by this method.
	 * Values are comparable to those of {@link AirborneOperationalStatusV1Msg}'s and
	 * {@link AirborneOperationalStatusV2Msg}'s getSIL method for aircraft supporting ADS-B
	 * version 1 and 2.
	 *
	 * @return the source integrity level (SIL) which indicates the propability of exceeding
	 *         the NIC containment radius.
	 */
	public byte getSIL() {
		switch (getFormatTypeCode()) {
			case 0: case 18: case 22: return 0;
			default: return 2;
		}
	}

	/**
	 * @see #getSurveillanceStatusDescription()
	 * @return the surveillance status
	 */
	public byte getSurveillanceStatus() {
		return surveillance_status;
	}

	/**
	 * This is a function of the surveillance status field in the position
	 * message.
	 *
	 * @return surveillance status description as defines in DO-260B
	 */
	public String getSurveillanceStatusDescription() {
		String[] desc = {
				"No condition information",
				"Permanent alert (emergency condition)",
				"Temporary alert (change in Mode A identity code oter than emergency condition)",
				"SPI condition"
		};

		return desc[surveillance_status];
	}

	/**
	 * @return for ADS-B version 0 and 1 messages true, iff transmitting system uses only one antenna.
	 */
	public boolean hasSingleAntenna() {
		return nic_suppl_b;
	}

	/**
	 * @return flag which will indicate whether or not the Time of Applicability of the message
	 *         is synchronized with UTC time. False will denote that the time is not synchronized
	 *         to UTC. True will denote that Time of Applicability is synchronized to UTC time.
	 */
	public boolean hasTimeFlag() {
		return time_flag;
	}

	@Override
	public CompactPositionReporting.CPREncodedPosition getCPREncodedPosition() {
		return position;
	}

	@Override
	public boolean hasValidPosition() {
		return horizontal_position_available;
	}

	@Override
	public boolean hasValidAltitude() {
		return altitude_available;
	}

	/**
	 * This method converts a gray code encoded int to a standard decimal int
	 * @param gray gray code encoded int of length bitlength
	 *        bitlength bitlength of gray code
	 * @return radix 2 encoded integer
	 */
	private static int grayToBin(int gray, int bitlength) {
		int result = 0;
		for (int i = bitlength-1; i >= 0; --i)
			result = result|((((0x1<<(i+1))&result)>>>1)^((1<<i)&gray));
		return result;
	}

	public static Integer decodeAltitude(short altitude_encoded) {
		boolean Qbit = (altitude_encoded&0x10)!=0;
		int N;
		if (Qbit) { // altitude reported in 25ft increments
			N = (altitude_encoded&0xF) | ((altitude_encoded&0xFE0)>>>1);
			return 25*N-1000;
		}
		else { // altitude is above 50175ft, so we use 100ft increments

			// it's decoded using the Gillham code
			int C1 = (0x800&altitude_encoded)>>>11;
			int A1 = (0x400&altitude_encoded)>>>10;
			int C2 = (0x200&altitude_encoded)>>>9;
			int A2 = (0x100&altitude_encoded)>>>8;
			int C4 = (0x080&altitude_encoded)>>>7;
			int A4 = (0x040&altitude_encoded)>>>6;
			int B1 = (0x020&altitude_encoded)>>>5;
			int B2 = (0x008&altitude_encoded)>>>3;
			int D2 = (0x004&altitude_encoded)>>>2;
			int B4 = (0x002&altitude_encoded)>>>1;
			int D4 = (0x001&altitude_encoded);

			// this is standard gray code
			int N500 = grayToBin(D2<<7|D4<<6|A1<<5|A2<<4|A4<<3|B1<<2|B2<<1|B4, 8);

			// 100-ft steps must be converted
			int N100 = grayToBin(C1<<2|C2<<1|C4, 3)-1;
			if (N100 == 6) N100=4;
			if (N500%2 != 0) N100=4-N100; // invert it

			return -1200+N500*500+N100*100;
		}
	}

	@Override
	public Integer getAltitude() {
		if (!altitude_available) return null;
		return decodeAltitude(altitude_encoded);
	}

	@Override
	public Position.AltitudeType getAltitudeType () {
		if (getFormatTypeCode() >= 9 && getFormatTypeCode() <= 18)
			return Position.AltitudeType.BAROMETRIC_ALTITUDE;
		else if (getFormatTypeCode() >= 20 && getFormatTypeCode() <= 22)
			return Position.AltitudeType.ABOVE_WGS84_ELLIPSOID;
		else return Position.AltitudeType.UNKNOWN;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tAirbornePositionV0Msg{" +
				"horizontal_position_available=" + horizontal_position_available +
				", altitude_available=" + altitude_available +
				", surveillance_status=" + surveillance_status +
				", single_antenna_flag=" + nic_suppl_b +
				", altitude_encoded=" + altitude_encoded +
				", time_flag=" + time_flag +
				", position=" + position +
				'}';
	}
}

package de.serosystems.lib1090.msgs.modes;

import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;

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
 * Decoder for Mode S surveillance Extended Length Messages (Comm-D ELM) (DF 24)
 * @author Matthias Schäfer (schaefer@sero-systems.de)
 */
public class CommDExtendedLengthMsg extends ModeSDownlinkMsg implements Serializable {

	private static final long serialVersionUID = 8539282448043078992L;

	private byte[] message;
	private boolean ack;
	private byte seqno;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected CommDExtendedLengthMsg() { }

	/**
	 * @param raw_message raw comm-d extended len msg as hex string
	 * @throws BadFormatException if message is not extended len msg or
	 * contains wrong values.
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public CommDExtendedLengthMsg(String raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ModeSDownlinkMsg(raw_message));
	}

	/**
	 * @param raw_message raw comm-d extended len msg as byte array
	 * @throws BadFormatException if message is not extended len msg or
	 * contains wrong values.
	 * @throws UnspecifiedFormatError if message has format that is not further specified in DO-260B
	 */
	public CommDExtendedLengthMsg(byte[] raw_message) throws BadFormatException, UnspecifiedFormatError {
		this(new ModeSDownlinkMsg(raw_message));
	}

	/**
	 * @param reply Mode S reply which contains this extended len msg
	 * @throws BadFormatException if message is not extended len msg or 
	 * contains wrong values.
	 */
	public CommDExtendedLengthMsg(ModeSDownlinkMsg reply) throws BadFormatException {
		super(reply);
		setType(subtype.COMM_D_ELM);

		if (getDownlinkFormat() < 24) {
			throw new BadFormatException("Message is not an extended length message!");
		}

		// extract Comm-D extended length message
		message = getPayload();
		ack = (getDownlinkFormat()&0x2)!=0;
		seqno = (byte) ((getDownlinkFormat()&0x1)<<3|getFirstField());
	}

	/**
	 * @return the 10-byte Comm-D extended length message
	 */
	public byte[] getMessage() {
		return message;
	}

	/**
	 * @return true if this is a uplink ELM acknowledgement
	 */
	public boolean isAck() {
		return ack;
	}

	/**
	 * @return the number of the message segment returned by {@link #getMessage()}
	 */
	public byte getSequenceNumber() {
		return seqno;
	}

	@Override
	public String toString() {
		return super.toString() + "\n\tCommDExtendedLengthMsg{" +
				"message=" + Tools.toHexString(message) +
				", ack=" + ack +
				", seqno=" + seqno +
				'}';
	}
}

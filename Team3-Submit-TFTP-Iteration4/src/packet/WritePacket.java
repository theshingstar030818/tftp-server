package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;

import types.*;

/**
 * @author Team 3
 * 
 *         This Builder class will enable building of entire DatagramPackets for
 *         the WRQ request type
 */

public class WritePacket extends ReadWritePacket {

	/**
	 * Used to create a packet from scratch by inputing the required parameters
	 * of the DatagramPacket class. This constructor will use the default
	 * encoding mode defined in the Configurations
	 * 
	 * @param addressOfHost
	 *            - InetAddress of the host
	 * @param destPort
	 *            - Destination port number
	 * @param fileName
	 *            - Filename to RRQ or WRQ
	 */
	public WritePacket(InetAddress addressOfHost, int destPort, String fileName) {
		super(addressOfHost, destPort, RequestType.WRQ, fileName);
	}

	/**
	 * Used to create a packet from scratch by inputing the required parameters
	 * of the DatagramPacket class. This constructor will use user defined
	 * encoding mode
	 * 
	 * @param addressOfHost
	 *            - InetAddress of the host
	 * @param destPort
	 *            - Destination port number
	 * @param fileName
	 *            - Filename to RRQ or WRQ
	 * @param mode
	 *            - The encoding mode of the data
	 */
	public WritePacket(InetAddress addressOfHost, int destPort, String fileName, ModeType mode) {
		super(addressOfHost, destPort, RequestType.WRQ, fileName, mode);
	}

	/**
	 * Used primary for de-construction of received packets
	 * 
	 * @param inDatagramPacket
	 */
	public WritePacket(DatagramPacket inDatagramPacket) {
		super(inDatagramPacket);
		deconstructPacket(inDatagramPacket);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see packet.ReadWritePacketPacket#getRequestTypeHeaderByteArray()
	 */
	@Override
	protected byte[] getRequestTypeHeaderByteArray() {
		return RequestType.WRQ.getHeaderByteArray();
	};

}

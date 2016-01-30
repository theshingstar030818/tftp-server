package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;

import types.*;

/**
 * @author Team 3
 * 
 * This class facilitates building of RRQ packets by determining which
 *block number it should be encapsulated in the Datagram with
 */

public class ReadPacketBuilder extends ReadWritePacketPacketBuilder {

	/**
	 * Used to create a packet from scratch by inputing the required parameters of the
	 * DatagramPacket class.
	 * This constructor will use the default encoding mode defined in the Configurations
	 * 
	 * @param addressOfHost - InetAddress of the host
	 * @param destPort 		- Destination port number
	 * @param fileName		- Filename to RRQ or WRQ
	 */
	public ReadPacketBuilder(InetAddress addressOfHost, int destPort, String fileName) {
		super(addressOfHost, destPort, RequestType.RRQ, fileName);
	}
	
	/**
	 * Used to create a packet from scratch by inputing the required parameters of the
	 * DatagramPacket class.
	 * This constructor will use user defined encoding mode
	 * 
	 * @param addressOfHost - InetAddress of the host
	 * @param destPort 		- Destination port number
	 * @param fileName		- Filename to RRQ or WRQ
	 * @param mode			- The encoding mode of the data
	 */
	public ReadPacketBuilder(InetAddress addressOfHost, int destPort, String fileName, ModeType mode) {
		super(addressOfHost, destPort, RequestType.RRQ, fileName, mode);
	}

	/**
	 * Used primary for de-construction of received packets
	 * 
	 * @param inDatagramPacket
	 */
	public ReadPacketBuilder(DatagramPacket inDatagramPacket) {
		super(inDatagramPacket);
		deconstructPacket(inDatagramPacket);
	}
	
	/* (non-Javadoc)
	 * @see packet.ReadWritePacketPacketBuilder#getRequestTypeHeaderByteArray()
	 */
	@Override 
	protected byte[] getRequestTypeHeaderByteArray() {
		return RequestType.RRQ.getHeaderByteArray();
	};

}

package packet;

import java.net.*;
import types.RequestType;

/**
 * @author Team 3
 * 
 * Converts a datagram packet into an appropriate Packet
 */
public class PacketBuilder {
	

	public PacketBuilder() {
		
	}

	/**
	 * This function takes in a datagram packet and de-construct it
	 * 
	 * @param inPacket
	 *            - passed in datagram packet
	 * @return receivePacketBuilder - new constructed datagram packet depends
	 *         different types of datagram packet passed in
	 */
	public Packet constructPacket(DatagramPacket inPacket) {
		byte[] buffer = inPacket.getData();
		Packet receivePacketBuilder=null;
		switch (buffer[1]) {
		case 1:
			receivePacketBuilder = new ReadPacket(inPacket);
			receivePacketBuilder.setRequestType(RequestType.RRQ);
			break;
		case 2:
			receivePacketBuilder = new WritePacket(inPacket);
			receivePacketBuilder.setRequestType(RequestType.WRQ);
			break;
		case 3:
			receivePacketBuilder = new DataPacket(inPacket);
			receivePacketBuilder.setRequestType(RequestType.DATA);
			break;
		case 4:
			receivePacketBuilder = new AckPacket(inPacket);
			receivePacketBuilder.setRequestType(RequestType.ACK);
			break;
		case 5:
			receivePacketBuilder = new ErrorPacket(inPacket);
			receivePacketBuilder.setRequestType(RequestType.ERROR);
			break;
		}
		return receivePacketBuilder;
	}
}

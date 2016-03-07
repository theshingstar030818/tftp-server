package packet;

import java.net.DatagramPacket;

import types.RequestType;

/**
 * @author Team 3
 *
 *         This class creates packets based on in comming datagram packets
 */
public class PacketFactory {

	public PacketFactory() {
	}

	/**
	 * Determines which Packet derived class to create based on the input header
	 * 
	 * @param inDatagram
	 *            - the packet that was received
	 * @return
	 */
	public Packet getBuilder(DatagramPacket inDatagram) {
		int packetNum = inDatagram.getData()[1] - 1;
		switch (packetNum) {
		case 0:
			return new ReadPacket(inDatagram);
		case 1:
			return new WritePacket(inDatagram);
		case 2:
			return new DataPacket(inDatagram);
		case 3:
			return new AckPacket(inDatagram);
		case 4:
			return new ErrorPacket(inDatagram);
		default:
			return null;
		}
	}
}

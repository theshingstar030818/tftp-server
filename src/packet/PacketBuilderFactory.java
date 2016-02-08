package packet;

import java.net.DatagramPacket;

import types.RequestType;

public class PacketBuilderFactory {

	public PacketBuilderFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public PacketBuilder getBuilder(DatagramPacket inDatagram) {
		int packetNum = inDatagram.getData()[1] - 1;
		switch(packetNum) {
			case 0:
				return new ReadPacketBuilder(inDatagram);
			case 1:
				return new WritePacketBuilder(inDatagram);
			case 2:
				return new DataPacketBuilder(inDatagram);
			case 3: 
				return new AckPacketBuilder(inDatagram);
			case 4:
				return new ErrorPacketBuilder(inDatagram);
			default:
				return null;
		}
	}
}

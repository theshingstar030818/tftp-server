package packet;

import java.net.DatagramPacket;

import types.RequestType;

public class PacketFactory {

	public PacketFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public Packet getBuilder(DatagramPacket inDatagram) {
		int packetNum = inDatagram.getData()[1] - 1;
		switch(packetNum) {
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

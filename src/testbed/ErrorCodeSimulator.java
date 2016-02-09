package testbed;

import java.net.*;
import packet.*;

public abstract class ErrorCodeSimulator {
	protected PacketBuilder receivePacketBuilder;

	public ErrorCodeSimulator(DatagramPacket inPacket) {
		this.constructPacketBuilder(inPacket);
	}

	/**
	 * This function takes in a datagram packet and de-construct it
	 * @param inPacket - passed in datagram packet
	 * @return receivePacketBuilder - new constructed datagram packet depends
	 * 		   different types of datagram packet passed in
	 */
	private PacketBuilder constructPacketBuilder(DatagramPacket inPacket) {
		byte[] buffer = inPacket.getData();

		switch (buffer[1]) {
		case 1:
			this.receivePacketBuilder = new ReadPacketBuilder(inPacket);
			break;
		case 2: 
			this.receivePacketBuilder = new WritePacketBuilder(inPacket);
			break;
		case 3: 
			this.receivePacketBuilder = new DataPacketBuilder(inPacket);
			break;
		case 4: 
			this.receivePacketBuilder = new AckPacketBuilder(inPacket);
			break;
		case 5: 
			this.receivePacketBuilder = new ErrorPacketBuilder(inPacket);
			break;
		}
		return this.receivePacketBuilder;
	}
	

}

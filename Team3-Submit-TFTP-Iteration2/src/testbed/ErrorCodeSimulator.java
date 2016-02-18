package testbed;

import java.net.*;
import packet.*;
import types.RequestType;

/**
 * @author Team 3
 * 
 *         Simulates corruption of TFTP packets
 */
public abstract class ErrorCodeSimulator {
	protected Packet receivePacketBuilder;

	public ErrorCodeSimulator(DatagramPacket inPacket) {
		this.constructPacketBuilder(inPacket);
	}

	/**
	 * This function takes in a datagram packet and de-construct it
	 * 
	 * @param inPacket
	 *            - passed in datagram packet
	 * @return receivePacketBuilder - new constructed datagram packet depends
	 *         different types of datagram packet passed in
	 */
	public void constructPacketBuilder(DatagramPacket inPacket) {
		byte[] buffer = inPacket.getData();

		switch (buffer[1]) {
		case 1:
			this.receivePacketBuilder = new ReadPacket(inPacket);
			this.receivePacketBuilder.setRequestType(RequestType.RRQ);
			break;
		case 2:
			this.receivePacketBuilder = new WritePacket(inPacket);
			this.receivePacketBuilder.setRequestType(RequestType.WRQ);
			break;
		case 3:
			this.receivePacketBuilder = new DataPacket(inPacket);
			this.receivePacketBuilder.setRequestType(RequestType.DATA);
			break;
		case 4:
			this.receivePacketBuilder = new AckPacket(inPacket);
			this.receivePacketBuilder.setRequestType(RequestType.ACK);
			break;
		case 5:
			this.receivePacketBuilder = new ErrorPacket(inPacket);
			this.receivePacketBuilder.setRequestType(RequestType.ERROR);
			break;
		}
	}
}

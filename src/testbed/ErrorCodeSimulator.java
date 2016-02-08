package testbed;

import java.net.*;
import packet.*;

public abstract class ErrorCodeSimulator {
	protected PacketBuilder receivePacketBuilder;
	private DatagramPacket receivePacket;

	public ErrorCodeSimulator(DatagramPacket receivePacket) {
		constructPacketBuilder(receivePacket);
	}

	private PacketBuilder constructPacketBuilder(DatagramPacket receivePacket) {
		byte[] buffer = receivePacket.getData();

		switch (buffer[1]) {
		case 1: {
			this.receivePacketBuilder = new ReadPacketBuilder(receivePacket);
		}
		case 2: {
			this.receivePacketBuilder = new WritePacketBuilder(receivePacket);
		}
		case 3: {
			this.receivePacketBuilder = new DataPacketBuilder(receivePacket);
		}
		case 4: {
			this.receivePacketBuilder = new AckPacketBuilder(receivePacket);
		}
		case 5: {
			this.receivePacketBuilder = new ErrorPacketBuilder(receivePacket);
		}
		}
		return (this.receivePacketBuilder);
	}

}

package testbed;

import java.net.DatagramPacket;

import testbed.errorcode.DatagramPacketToPacketBuilder;

public class ErrorPacketLost extends DatagramPacketToPacketBuilder{
	
	public ErrorPacketLost(DatagramPacket receivePacket){
		super(receivePacket);
		this.destroyPacket();
	}
	
	private void destroyPacket(){
		receivePacketBuilder.setBlockNumber((short)(receivePacketBuilder.getBlockNumber()+1));
	}

}

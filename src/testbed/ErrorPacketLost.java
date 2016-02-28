package testbed;
 
 import java.net.DatagramPacket;

import packet.Packet;
import packet.PacketBuilder;

public class ErrorPacketLost{
	
	private Packet receivePacket;
	
	public ErrorPacketLost(DatagramPacket receiveDatagramPacket){
 		this.destroyPacket(receiveDatagramPacket);
 	}
 	
 	private DatagramPacket destroyPacket(DatagramPacket receiveDatagramPacket){
 		receivePacket = (new PacketBuilder()).constructPacket(receiveDatagramPacket);
 		receivePacket.setBlockNumber((short)(receivePacket.getBlockNumber()+1));
 		return (receivePacket.buildPacket());
 	}
 
 }
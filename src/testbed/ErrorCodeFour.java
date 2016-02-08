package testbed;

import java.net.*;

import types.*;

public class ErrorCodeFour extends ErrorCodeSimulator{
	int subcode;
	DatagramPacket sendPacket;
	public ErrorCodeFour(DatagramPacket receivePacket,int subcode){
		super(receivePacket);
		this.subcode = subcode;
	}
	public DatagramPacket errorPacketCreator(){
		DatagramPacket sendPacket = null;
		RequestType rt = receivePacketBuilder.getRequestType();
		switch(subcode){
			case 1: {
				
			}
			case 2: {
				if(rt==RequestType.ACK || rt == RequestType.DATA){
					receivePacketBuilder.setBlockNumber((short)(receivePacketBuilder.getBlockNumber()+ 5));
				}
				
			}
			case 3: {
				
			}
			case 4: {
				
			}
				
			
		}
		return sendPacket;
	}

}

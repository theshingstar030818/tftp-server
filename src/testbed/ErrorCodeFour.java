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
				if(rt==RequestType.RRQ || rt == RequestType.WRQ){
					receivePacketBuilder.setFilename("abcd.txt");
					receivePacketBuilder.setMode(switchMode());
					sendPacket = receivePacketBuilder.getPacket();
				}
			}
			case 2: {
				if(rt==RequestType.ACK || rt == RequestType.DATA){
					receivePacketBuilder.setBlockNumber((short)(receivePacketBuilder.getBlockNumber()+ 1));
					sendPacket = receivePacketBuilder.getPacket();
				}
				
			}
			case 3: {
				
			}
			case 4: {
				
			}
				
			
		}
		return sendPacket;
	}
	
	private ModeType switchMode(){
		if(receivePacketBuilder.getMode() == ModeType.OCTET){
			return (ModeType.NETASCII);
		}
		return ModeType.OCTET;
	}

}

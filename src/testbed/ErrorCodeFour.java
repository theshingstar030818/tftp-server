package testbed;

import java.net.*;

import packet.AckPacketBuilder;
import packet.PacketBuilder;
import types.*;

public class ErrorCodeFour extends ErrorCodeSimulator{
	int mSubcode;
	DatagramPacket mSendPacket;
	
	public ErrorCodeFour(DatagramPacket receivePacket,int subcode){
		super(receivePacket);
		this.mSubcode = subcode;
	}
	
	public DatagramPacket errorPacketCreator(){
		this.mSendPacket = null;
		RequestType rt = super.receivePacketBuilder.getRequestType();
		switch(this.mSubcode){
			case 1: 
				if(rt==RequestType.RRQ || rt == RequestType.WRQ){
					super.receivePacketBuilder.setFilename("abcd.txt");
					super.receivePacketBuilder.setMode(switchMode());
					this.mSendPacket = super.receivePacketBuilder.getPacket();
				}
				break;
			
			case 2: 
				if(rt==RequestType.ACK || rt == RequestType.DATA){
					super.receivePacketBuilder.setBlockNumber((short)(super.receivePacketBuilder.getBlockNumber()+ 1));
					this.mSendPacket = super.receivePacketBuilder.getPacket();
				}
				break;
			case 3:
				this.changeHeader(super.receivePacketBuilder);
				break;
			case 4:
				this.invalidPacketSize(super.receivePacketBuilder);
				break;
			default:
				// TODO: default action for error creator
				break;
		}
		return this.mSendPacket;
	}
	
	/**
	 * This function takes in a datagram packet and change the header
	 * on the datagram packet to invalid
	 * @param inPacket
	 */
	private void changeHeader(PacketBuilder inPacket) {
		byte[] header = inPacket.getRequestType().getHeaderByteArray();
		byte[] data = inPacket.getDataBuffer();
		
		
		switch(header[1]) {
			case 1:
				// read request with hearder[0,1]
				data[1] = 4;
				this.mSendPacket = new DatagramPacket(data, data.length);
				break;
			case 2:
				// write request with header [0,2]
				data[1] = 3;
				this.mSendPacket = new DatagramPacket(data, data.length);
				break;
			case 3:
				// data datagram packet with header [0,3]
				data[1] = 4;
				this.mSendPacket = new DatagramPacket(data, data.length);
				break;
			case 4:
				// ack packet with [0,4]
				data[1] = 1;
				this.mSendPacket = new DatagramPacket(data, data.length);
				break;
			case 5:
				// error packet
				// should not do anything
				break;
			default:
				// TODO: 
				break;	
		}
	}
	
	/**
	 * This function double the size of passed in datagram packet to an invalid size
	 * @param inPacket
	 */
	private void invalidPacketSize(PacketBuilder inPacket) {
		byte[] data = inPacket.getDataBuffer();
		this.mSendPacket = new DatagramPacket(data, data.length*2);
	}
	
	private ModeType switchMode(){
		if(receivePacketBuilder.getMode() == ModeType.OCTET){
			return (ModeType.NETASCII);
		}
		return ModeType.OCTET;
	}

}

package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;

import types.*;

/**
 * @author Team 3
 * 
 */

public class ReadPacketBuilder extends ReadWritePacketPacketBuilder {

	public ReadPacketBuilder(InetAddress addressOfHost, int destPort, String fileName) {
		super(addressOfHost, destPort, RequestType.RRQ, fileName);
	}
	public ReadPacketBuilder(InetAddress addressOfHost, int destPort, String fileName, ModeType mode) {
		super(addressOfHost, destPort, RequestType.RRQ, fileName, mode);
	}

	public ReadPacketBuilder(DatagramPacket inDatagramPacket) {
		super(inDatagramPacket);
		decontructPacket(inDatagramPacket);
	}
	
	@Override
	public DatagramPacket buildPacket() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override 
	protected byte[] getRequestTypeHeaderByteArray() {
		return RequestType.RRQ.getHeaderByteArray();
	};

}

package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;

import types.RequestType;

/**
 * @author Team 3
 *
 * This class is used to construct the Error packets used for the TFTP system.
 */
public class ErrorPacketBuilder extends PacketBuilder {

	public ErrorPacketBuilder(InetAddress addressOfHost, int destPort) {
		super(addressOfHost, destPort, RequestType.ERROR);
		// TODO Auto-generated constructor stub
	}

	public ErrorPacketBuilder(DatagramPacket inDatagramPacket) {
		super(inDatagramPacket);
		// TODO Auto-generated constructor stub
	}

	@Override
	public DatagramPacket buildPacket() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deconstructPacket(DatagramPacket inDatagramPacket) {
		// TODO Auto-generated method stub

	}
	
	@Override 
	protected byte[] getRequestTypeHeaderByteArray() {
		return RequestType.ERROR.getHeaderByteArray();
	};

}

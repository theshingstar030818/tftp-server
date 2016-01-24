package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import types.RequestType;
import helpers.Conversion;
import resource.Configurations;

/**
 * @author Team 3
 *	
 *	This class facilitates building of ACK packets by determining which
 *	block number it should reply with
 */
public class AckPacketBuilder extends PacketBuilder {

	private short mBlockNumber;
	
	/**
	 * Used to create a packet from scratch by inputing the required parameters of the
	 * DatagramPacket class
	 * 
	 * @param addressOfHost - InetAddress of the host
	 * @param destPort 		- Destination port number
	 */
	public AckPacketBuilder(InetAddress addressOfHost, int destPort) {
		super(addressOfHost, destPort, RequestType.ACK);
		mBlockNumber = 0;
	}

	/**
	 * Used primary for de-construction of received packets
	 * 
	 * @param inDatagramPacket
	 */
	public AckPacketBuilder(DatagramPacket inDatagramPacket) {
		super(inDatagramPacket);
		mBlockNumber = 0;
		decontructPacket(inDatagramPacket);
	}

	/* (non-Javadoc)
	 * @see packet.PacketBuilder#buildPacket()
	 */
	@Override
	public DatagramPacket buildPacket() {
		this.mBuffer = new byte[Configurations.LEN_ACK_PACKET_BUFFET];
		byte[] udpHeader = getRequestTypeHeaderByteArray();
		byte[] blockNumber = Conversion.shortToBytes(this.mBlockNumber);
		
		// Copy everything into the new buffer
		System.arraycopy(udpHeader, 0, this.mBuffer, 0, udpHeader.length);
		System.arraycopy(blockNumber, 0, this.mBuffer, udpHeader.length, blockNumber.length);
		this.mDatagramPacket = new DatagramPacket(this.mBuffer, this.mBuffer.length, this.mInetAddress, this.mDestinationPort);
		return this.mDatagramPacket;
	}

	/* (non-Javadoc)
	 * @see packet.PacketBuilder#decontructPacket(java.net.DatagramPacket)
	 */
	@Override
	public void decontructPacket(DatagramPacket inDatagramPacket) {
		setRequestTypeFromBuffer(this.mBuffer);
		if(this.mRequestType == RequestType.DATA) {
			byte[] byteBlockNumber = Arrays.copyOfRange(this.mBuffer, 2, 4);
			this.mBlockNumber = Conversion.bytesToShort(byteBlockNumber);
		} else if(this.mRequestType == RequestType.RRQ) {
			this.mBlockNumber = 1;
		} else {
			this.mBlockNumber = 0;
		}
	}
	
	/* (non-Javadoc)
	 * @see packet.PacketBuilder#getRequestTypeHeaderByteArray()
	 */
	@Override 
	protected byte[] getRequestTypeHeaderByteArray() {
		return RequestType.ACK.getHeaderByteArray();
	};
	
	/**
	 * A public method to return the block number associated with the packet.
	 * Note: block number changes before building and after building the packet
	 * 
	 * @return a short - of the block number associated with the transfer
	 */
	public short getBlockNumber() {
		return this.mBlockNumber;
	}
}

package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

import types.ModeType;
import types.RequestType;
import helpers.Conversion;
import resource.Configurations;

/**
 * @author Team 3
 *	
 *	This class facilitates building of ACK packets by determining which
 *	block number it should reply with
 */
public class AckPacket extends Packet {
	
	/**
	 * Used to create a packet from scratch by inputing the required parameters of the
	 * DatagramPacket class.
	 * 
	 * @param addressOfHost - InetAddress of the host
	 * @param destPort 		- Destination port number
	 */
	public AckPacket(InetAddress addressOfHost, int destPort) {
		super(addressOfHost, destPort, RequestType.ACK);
		mBlockNumber = 0;
	}

	/**
	 * Used primary for de-construction of received packets. By passing in the 
	 * DatagramPacket to this constructor, we can create a packet to reply back
	 * the the sender.
	 * 
	 * IMPORTANT: Any other packet type can be passed through here
	 * but specifically, DATA packets are best passed through this constructor.
	 * When managing a stream of packet transmission, you can recycle the same
	 * instance of this class by loading new DATA packets through the 
	 * deconstructPacket(DatagramPacket inDatagramPacket) method
	 * 
	 * @param inDatagramPacket - the packet to reply to (or load info from)
	 */
	public AckPacket(DatagramPacket inDatagramPacket) {
		super(inDatagramPacket);
		mBlockNumber = 0;
		deconstructPacket(inDatagramPacket);
	}

	/* (non-Javadoc)
	 * @see packet.Packet#buildPacket()
	 */
	@Override
	public DatagramPacket buildPacket() {
		//this.mBlockNumber = (short) ((this.mBlockNumber) % Short.MAX_VALUE);
		this.mBuffer = new byte[Configurations.LEN_ACK_PACKET_BUFFER];
		byte[] udpHeader = getRequestTypeHeaderByteArray();
		byte[] blockNumber = Conversion.intToBytes(this.mBlockNumber);
		
		// Copy everything into the new buffer
		System.arraycopy(udpHeader, 0, this.mBuffer, 0, udpHeader.length);
		System.arraycopy(blockNumber, 0, this.mBuffer, udpHeader.length, blockNumber.length);
		this.mDatagramPacket = new DatagramPacket(this.mBuffer, this.mBuffer.length, this.mInetAddress, this.mDestinationPort);
		return this.mDatagramPacket;
	}

	/* (non-Javadoc)
	 * @see packet.Packet#decontructPacket(java.net.DatagramPacket)
	 */
	@Override
	public void deconstructPacket(DatagramPacket inDatagramPacket) {
		setRequestTypeFromBuffer(this.mBuffer);
		if(this.mRequestType == RequestType.DATA || this.mRequestType == RequestType.ACK) {
			byte[] byteBlockNumber = Arrays.copyOfRange(this.mBuffer, 2, 4);
			this.mBlockNumber = Conversion.bytesToInt(byteBlockNumber);
		} else if(this.mRequestType == RequestType.RRQ) {
			this.mBlockNumber = 1;
		} else {
			this.mBlockNumber = 0;
		}
	}
	
	/* (non-Javadoc)
	 * @see packet.Packet#getRequestTypeHeaderByteArray()
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
	public int getBlockNumber() {
		return this.mBlockNumber;
	}
	
	/**
	 * Allows the user to specifically override the block number for this transaction
	 * Note: buildPacket(byte[] payload) will always increment so adjust accordingly
	 * 
	 * @param blockNumber
	 */
	public void setBlockNumber(int blockNumber) {
		this.mBlockNumber = blockNumber;
	}
	
	/* (non-Javadoc)
	 * @see packet.Packet#getDataBuffer()
	 */
	public byte[] getDataBuffer() {
		return this.mBuffer;
	}

	/* (non-Javadoc)
	 * @see packet.Packet#setFilename(java.lang.String)
	 */
	@Override
	public void setFilename(String fileName) {
		throw new IllegalArgumentException("You cannot use filename with this type of packet.");
	}

	/* (non-Javadoc)
	 * @see packet.Packet#setMode(types.ModeType)
	 */
	@Override
	public void setMode(ModeType mode) {
		throw new IllegalArgumentException("You cannot use Mode with this type of packet.");
	}

	/* (non-Javadoc)
	 * @see packet.Packet#getMode()
	 */
	@Override
	public ModeType getMode() {
		throw new IllegalArgumentException("You cannot use Mode with this type of packet.");
	}
}

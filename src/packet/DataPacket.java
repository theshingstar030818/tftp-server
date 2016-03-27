package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

import helpers.Conversion;
import resource.Configurations;
import types.ModeType;
import types.RequestType;

/**
 * @author Team 3
 * 
 *         This class facilitates building of DATA packets by determining which
 *         block number it should be encapsulated in the Datagram with
 */
public class DataPacket extends Packet {

	private int mBlockNumber;
	private byte[] mDataBuffer;

	/**
	 * Used to create a packet from scratch by inputing the required parameters
	 * of the DatagramPacket class
	 * 
	 * @param addressOfHost
	 *            - InetAddress of the host
	 * @param destPort
	 *            - Destination port number
	 */
	public DataPacket(InetAddress addressOfHost, int destPort) {
		super(addressOfHost, destPort, RequestType.DATA);
		this.mBlockNumber = 0;
	}

	/**
	 * Used primary for de-construction of received packets. By passing in the
	 * DatagramPacket to this constructor, we can create a packet to reply back
	 * the the sender.
	 * 
	 * IMPORTANT: Any other packet type can be passed through here but
	 * specifically, ACK packets are best passed through this constructor. When
	 * managing a stream of packet transmission, you can recycle the same
	 * instance of this class by loading new ACK packets through the
	 * deconstructPacket(DatagramPacket inDatagramPacket) method
	 * 
	 * @param inDatagramPacket
	 *            - the packet to reply to (or load info from)
	 */
	public DataPacket(DatagramPacket inDatagramPacket) {
		super(inDatagramPacket);
		deconstructPacket(inDatagramPacket);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see packet.Packet#buildPacket()
	 */
	@Override
	public DatagramPacket buildPacket() {
		throw new IllegalArgumentException("You must provide a byte[] to build a DATA packet!");
	}

	/**
	 * This is the default build packet method for this class. The overridden
	 * inherited method is no use as we need to put the file contents inside the
	 * Datagram Note: This method will always increment the block number
	 * 
	 * @param payload
	 *            - a byte array of a chunk of a file
	 * @return the DatagramPacket, ready to be sent
	 */
	public DatagramPacket buildPacket(byte[] payload) {
		// Start the block number back at 1 if it over flows
		this.mBlockNumber = ((this.mBlockNumber + 1) % 65536);
		byte[] blockNumber = Conversion.intToBytes(this.mBlockNumber);
		byte[] udpHeader = getRequestTypeHeaderByteArray();
		int sizeOfPayload = 0;
		if (payload != null) {
			sizeOfPayload = payload.length;
		}
		this.mBuffer = new byte[sizeOfPayload + udpHeader.length + blockNumber.length];

		// Copy everything into the new buffer
		System.arraycopy(udpHeader, 0, this.mBuffer, 0, udpHeader.length);
		System.arraycopy(blockNumber, 0, this.mBuffer, udpHeader.length, blockNumber.length);
		if (payload != null) {
			System.arraycopy(payload, 0, this.mBuffer, udpHeader.length + blockNumber.length, payload.length);
		}
		this.mDatagramPacket = new DatagramPacket(this.mBuffer, this.mBuffer.length, this.mInetAddress,
				this.mDestinationPort);
		return this.mDatagramPacket;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see packet.Packet#decontructPacket(java.net.DatagramPacket)
	 */
	@Override
	public void deconstructPacket(DatagramPacket inDatagramPacket) {
		setRequestTypeFromBuffer(this.mBuffer);
		if (this.mRequestType == RequestType.ACK || this.mRequestType == RequestType.DATA) {
			byte[] byteBlockNumber = Arrays.copyOfRange(this.mBuffer, 2, 4);
			this.mBlockNumber = Conversion.bytesToInt(byteBlockNumber);
		} else {
			this.mBlockNumber = 0;
		}

		if (this.mBuffer.length >= Configurations.LEN_ACK_PACKET_BUFFER) {
			this.mDataBuffer = Arrays.copyOfRange(this.mBuffer, Configurations.LEN_ACK_PACKET_BUFFER,
					this.mBuffer.length);
		} else {
			this.mDataBuffer = null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see packet.Packet#getRequestTypeHeaderByteArray()
	 */
	@Override
	protected byte[] getRequestTypeHeaderByteArray() {
		return RequestType.DATA.getHeaderByteArray();
	};

	/**
	 * Allows the user to specifically override the block number for this
	 * transaction Note: buildPacket(byte[] payload) will always increment so
	 * adjust accordingly
	 * 
	 * @param blockNumber
	 */
	public void setBlockNumber(int blockNumber) {
		this.mBlockNumber = blockNumber;
	}

	/**
	 * A public method to return the block number associated with the packet.
	 * Note: block number changes before building and after building the packet
	 * 
	 * @return a short - of the block number associated with the transfer
	 */
	public int getBlockNumber() {
		return this.mBlockNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see packet.Packet#setFilename(java.lang.String)
	 */
	@Override
	public void setFilename(String fileName) {
		throw new IllegalArgumentException("You cannot use filename with this type of packet.");
	}

	@Override
	public void setMode(ModeType mode) {
		throw new IllegalArgumentException("You cannot use Mode with this type of packet.");
	}

	@Override
	public ModeType getMode() {
		throw new IllegalArgumentException("You cannot use Mode with this type of packet.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see packet.Packet#getDataBuffer()
	 */
	public byte[] getDataBuffer() {
		return this.mDataBuffer;
	}
}

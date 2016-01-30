package packet;

import java.net.*;
import java.util.ArrayList;
import types.RequestType;
import helpers.Conversion;

/**
 * @author Team 3
 * 
 * This class is an abstract base class of the PacketBuilder design pattern
 * implementation. It's primary responsibility is to initialize DatagramPacket
 * attributes and de-construction of received packets. 
 */

public abstract class PacketBuilder {

	protected RequestType mRequestType;
	protected InetAddress mInetAddress;
	protected int mDestinationPort;
	protected DatagramPacket mDatagramPacket;
	protected byte[] mBuffer;

	/**
	 * Used to create a packet from scratch by inputing the required parameters of the
	 * DatagramPacket class
	 * 
	 * @param addressOfHost - InetAddress of the host
	 * @param destPort 		- Destination port number
	 * @param requesType	- Request Type operation code
	 */
	public PacketBuilder(InetAddress addressOfHost, int destPort, RequestType requesType) {
		this.mInetAddress = addressOfHost;
		this.mDestinationPort = destPort;
		this.mRequestType = requesType;
		this.mDatagramPacket = null;
	}

	/**
	 * Used primary for de-construction of received packets
	 * 
	 * @param inDatagramPacket - packet that was received by the system
	 */
	public PacketBuilder(DatagramPacket inDatagramPacket) {
		this.mDatagramPacket = inDatagramPacket;
		byte[] tempBuffer = new byte[inDatagramPacket.getLength()];
		System.arraycopy(inDatagramPacket.getData(), 0, tempBuffer, 0, inDatagramPacket.getLength());
		this.mBuffer = tempBuffer;
		this.mInetAddress = inDatagramPacket.getAddress();
		this.mDestinationPort = inDatagramPacket.getPort();
	}

	/**
	 * This function takes care of building the DatagramPacket in its entirety.
	 * Each subclass of PacketBuilder must override this method to provide
	 * each specific packet building instructions
	 * 
	 * @return the finished DatagramPacket, ready to be sent
	 */
	public abstract DatagramPacket buildPacket();
	
	/**
	 * This function de-constructs the primary parameters of the DatagramPacket.
	 * The main use is to load the super class PacketBuilder, with essential packet 
	 * destination information for constructing a reply
	 * 
	 * @param inDatagramPacket - packet to retrieve all information from
	 */
	public abstract void deconstructPacket(DatagramPacket inDatagramPacket);
	
	/**
	 * This function will return the corresponding 2 byte array that is 
	 * associated to each RequestType enum. 
	 * 
	 * @return a length-ed 2 byte array representing the request type header
	 */
	protected abstract byte[] getRequestTypeHeaderByteArray();

	/**
	 * This function can be used to get the current packet that PacketBuilder
	 * has been working on. 
	 * If buildPacket() was not called before, a NullPointerException will be thrown
	 * 
	 * @return the product of buildPacket()
	 */
	public DatagramPacket getPacket() {
		if(this.mDatagramPacket == null) {
			throw new NullPointerException();
		} else {
			return this.mDatagramPacket;
		}
	}

	/**
	 * This function can be used to create a READ/WRITE message from the base 
	 * class
	 * 
	 * @param opcode 	- RRQ or WRQ
	 * @param filename 	- The filename associated with this request
	 * @param mode 		- the mode of the encoding
	 * @return the byte buffer that goes inside a DatagramPacket
	 */
	protected byte[] createMsg(byte[] opcode, String filename, String mode) {
		ArrayList<Byte> msg = new ArrayList<Byte>();
		for (byte b : opcode) {
			msg.add(b);
		}
		for (byte b : filename.getBytes()) {
			msg.add(b);
		}
		msg.add((byte) 0);
		for (byte b : mode.getBytes()) {
			msg.add(b);
		}
		msg.add((byte) 0);
		return Conversion.toByteArray(msg);
	}
	
	/**
	 * Returns the current type of the request
	 * 
	 * @return RequestType enum
	 */
	public RequestType getRequestType() {
		return this.mRequestType;
	}

	
	/**
	 * This function is used to set the current RequestType that the PacketBuilder
	 * and it's subclasses are working on.
	 * 
	 * @param buffer - the recently received buffer from DatagramPacket.getData()
	 */
	protected void setRequestTypeFromBuffer(byte[] buffer) {
		if (buffer[1] == 1) {
			this.mRequestType = RequestType.RRQ;
		}
		if (buffer[1] == 2) {
			this.mRequestType = RequestType.WRQ;
		}
		if (buffer[1] == 3) {
			this.mRequestType = RequestType.DATA;
		}
		if (buffer[1] == 4) {
			this.mRequestType = RequestType.ACK;
		}
		if (buffer[1] == 5) {
			this.mRequestType = RequestType.ERROR;
		}
	}
}
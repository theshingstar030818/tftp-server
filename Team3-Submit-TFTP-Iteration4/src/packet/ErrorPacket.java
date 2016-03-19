package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import helpers.Conversion;
import resource.Configurations;
import types.ErrorType;
import types.ModeType;
import types.RequestType;

/**
 * @author Team 3
 *
 *         This class is used to construct the Error packets used for the TFTP
 *         system.
 */
public class ErrorPacket extends Packet {

	private ErrorType mErrorType;
	private String mErrorMessage = "";

	public ErrorPacket(InetAddress addressOfHost, int destPort) {
		super(addressOfHost, destPort, RequestType.ERROR);
	}

	public ErrorPacket(DatagramPacket inDatagramPacket) {
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
		throw new IllegalArgumentException("You must provide a ErrorType to build a ERROR packet!");
	}

	/**
	 * This function will build a physical error packet based on the error type.
	 * 
	 * @param errorType
	 *            - ErrorType
	 * @return datagram packet
	 */
	public DatagramPacket buildPacket(ErrorType errorType) {
		this.mErrorType = errorType;
		byte[] errorHeaderBytes = RequestType.ERROR.getHeaderByteArray();
		byte[] errorCodeBytes = Conversion.shortToBytes(errorType.getErrorCodeShort());
		byte[] errorMessageBytes = errorType.getErrorMessageString().getBytes();
		int bufferLength = errorHeaderBytes.length + errorCodeBytes.length + errorMessageBytes.length + 1;
		this.mBuffer = new byte[bufferLength];
		System.arraycopy(errorHeaderBytes, 0, this.mBuffer, 0, errorHeaderBytes.length);
		System.arraycopy(errorCodeBytes, 0, this.mBuffer, errorHeaderBytes.length, errorCodeBytes.length);
		System.arraycopy(errorMessageBytes, 0, this.mBuffer, errorHeaderBytes.length + errorCodeBytes.length,
				errorMessageBytes.length);
		this.mBuffer[bufferLength - 1] = 0; // Null terminating zero
		return new DatagramPacket(this.mBuffer, this.mBuffer.length, this.mInetAddress, this.mDestinationPort);
	}

	/**
	 * This function will build an error packet based on the error type and the
	 * custom string message
	 * 
	 * @param errorType
	 *            - ErrorType
	 * @param customMessage
	 *            - String message
	 * @return datagram packet
	 */
	public DatagramPacket buildPacket(ErrorType errorType, String customMessage) {
		this.mErrorType = errorType;
		byte[] errorHeaderBytes = RequestType.ERROR.getHeaderByteArray();
		byte[] errorCodeBytes = Conversion.shortToBytes(errorType.getErrorCodeShort());
		byte[] errorMessageBytes = customMessage.getBytes();
		int bufferLength = errorHeaderBytes.length + errorCodeBytes.length + errorMessageBytes.length + 1;
		this.mBuffer = new byte[bufferLength];
		System.arraycopy(errorHeaderBytes, 0, this.mBuffer, 0, errorHeaderBytes.length);
		System.arraycopy(errorCodeBytes, 0, this.mBuffer, errorHeaderBytes.length, errorCodeBytes.length);
		System.arraycopy(errorMessageBytes, 0, this.mBuffer, errorHeaderBytes.length + errorCodeBytes.length,
				errorMessageBytes.length);
		this.mBuffer[bufferLength - 1] = 0; // Null terminating zero
		return new DatagramPacket(this.mBuffer, this.mBuffer.length, this.mInetAddress, this.mDestinationPort);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see packet.Packet#deconstructPacket(java.net.DatagramPacket)
	 */
	@Override
	public void deconstructPacket(DatagramPacket inDatagramPacket) {
		// Only using this to deconstruct to send back to sender
		this.mRequestType = RequestType.ERROR;
		if (this.mRequestType == RequestType.ERROR) {
			byte[] errorOpCode = new byte[2];
			System.arraycopy(this.mBuffer, 2, errorOpCode, 0, 2);
			int errorOpInt = Conversion.bytesToShort(errorOpCode);
			this.mErrorType = ErrorType.matchErrorByNumber(errorOpInt);
		}
		// Extract the error message
		byte[] errorMessageByte;
		if (this.mBuffer.length > Configurations.ERROR_PACKET_USELESS_VALUES) {
			errorMessageByte = new byte[this.mBuffer.length - Configurations.ERROR_PACKET_USELESS_VALUES];
			System.arraycopy(this.mBuffer, 4, errorMessageByte, 0, errorMessageByte.length);
			this.mErrorMessage = new String(errorMessageByte);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see packet.Packet#getRequestTypeHeaderByteArray()
	 */
	@Override
	protected byte[] getRequestTypeHeaderByteArray() {
		return RequestType.ERROR.getHeaderByteArray();
	}

	/**
	 * Returns the error type
	 * 
	 * @return ErrorType
	 */
	public ErrorType getErrorType() {
		return this.mErrorType;
	}

	/**
	 * Gets the current custom message if exists, else returns empty string
	 * 
	 * @return string error message
	 */
	public String getCustomPackageErrorMessage() {
		return this.mErrorMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see packet.Packet#getDataBuffer()
	 */
	public byte[] getDataBuffer() {
		return this.mBuffer;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see packet.Packet#setMode(types.ModeType)
	 */
	@Override
	public void setMode(ModeType mode) {
		throw new IllegalArgumentException("You cannot use Mode with this type of packet.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see packet.Packet#getMode()
	 */
	@Override
	public ModeType getMode() {
		throw new IllegalArgumentException("You cannot use Mode with this type of packet.");
	}

	/**
	 * Allows the user to specifically override the block number for this
	 * transaction Note: buildPacket(byte[] payload) will always increment so
	 * adjust accordingly
	 * 
	 * @param blockNumber
	 */
	public void setBlockNumber(short blockNumber) {
		this.mBlockNumber = blockNumber;
	}

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

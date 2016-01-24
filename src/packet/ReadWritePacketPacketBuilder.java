package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;

import types.*;
import resource.Configurations;

public class ReadWritePacketPacketBuilder extends PacketBuilder {

	protected String mFilename;
	protected ModeType mMode;
	
	public ReadWritePacketPacketBuilder(InetAddress addressOfHost, int destPort, RequestType requestType) {
		super(addressOfHost, destPort, requestType);
		this.mFilename = Configurations.DEFAULT_FILENAME;
		this.mMode = Configurations.DEFAULT_RW_MODE;
	}
	
	public ReadWritePacketPacketBuilder(InetAddress addressOfHost, int destPort, RequestType requestType, String fileName) {
		super(addressOfHost, destPort, requestType);
		this.mFilename = fileName;
		this.mMode = Configurations.DEFAULT_RW_MODE;
	}
	
	public ReadWritePacketPacketBuilder(InetAddress addressOfHost, int destPort, RequestType requestType, String fileName, ModeType mode) {
		super(addressOfHost, destPort, requestType);
		this.mFilename = fileName;
		this.mMode = mode;
	}

	public ReadWritePacketPacketBuilder(DatagramPacket inDatagramPacket) {
		super(inDatagramPacket);
		decontructPacket(inDatagramPacket);
	}

	/**
	 * This function will build a DatagramPacket by the specified packet format:
	 * 		REQUEST TYPE ~ FILENAME ~ 0 ~ MESSAGE ~ 0
	 * It will copy sections of the packet into a packet buffer before setting the 
	 * destination address of the packet 
	 * 
	 * @return the built DatagramPacket
	 */
	@Override
	public DatagramPacket buildPacket() {
		byte[] udpHeader = this.mRequestType.getHeaderByteArray();
		byte[] modeBody = this.mMode.getModeByteArray();
		byte[] message = this.mFilename.getBytes();
		byte zeroByte = 0;
		int currentBufferIndex = 0;
		// Create the buffer size with two zeros as padding between message and null terminator
		int bufferSize = message.length + udpHeader.length + modeBody.length + 2;
		this.mBuffer = new byte[bufferSize];
		
		// Copy our header and messages into the buffer
		System.arraycopy(udpHeader, 0, this.mBuffer, 0, udpHeader.length);
		System.arraycopy(message, 0, this.mBuffer, udpHeader.length, message.length);
		currentBufferIndex += udpHeader.length + message.length;
		
		// Create a 0 padding
		this.mBuffer[udpHeader.length + message.length] = zeroByte;
		++currentBufferIndex;
		
		//Copy the mode byte array into it 
		System.arraycopy(modeBody, 0, this.mBuffer, currentBufferIndex, modeBody.length);
		currentBufferIndex += modeBody.length;
		
		// Set the last trailing 0
		mBuffer[currentBufferIndex] = zeroByte; 
		this.mDatagramPacket = new DatagramPacket(mBuffer, mBuffer.length, this.mInetAddress, this.mDestinationPort);
		return this.mDatagramPacket;
	}

	@Override
	public void decontructPacket(DatagramPacket inDatagramPacket) {
		// Get the mode and filename from the buffer
		deconstructBuffer();
		setRequestTypeFromBuffer(this.mBuffer);
	} 
	
	private void deconstructBuffer() {
		StringBuilder fileName = new StringBuilder();
		StringBuilder modeName = new StringBuilder();
		boolean fileNameDone = false;
		boolean startDeconstructingMode = false;
		for(int i = 2; i < this.mBuffer.length - 1; ++i) {
			if(this.mBuffer[i] == 0) {
				fileNameDone = true;
				startDeconstructingMode = true;
			}
			if(!fileNameDone) {
				fileName.append(this.mBuffer[i]);
			}
			if(startDeconstructingMode) {
				modeName.append(this.mBuffer[i]);
			}
		}
		this.mFilename = fileName.toString();
		this.mMode = ModeType.matchModeFromString(modeName.toString());
	}
	
	/* (non-Javadoc)
	 * @see packet.PacketBuilder#getRequestTypeHeaderByteArray()
	 */
	@Override 
	protected byte[] getRequestTypeHeaderByteArray() {
		if (this.mRequestType == RequestType.RRQ) {
			return RequestType.RRQ.getHeaderByteArray();
		}
		if (this.mRequestType == RequestType.WRQ) {
			return RequestType.WRQ.getHeaderByteArray();
		}
		return new byte[] {0,0};
	};
	
	public void setFilename(String fileName) {
		this.mFilename = fileName;
	}
	
	public void setMode(ModeType mode) {
		this.mMode = mode;
	}

}

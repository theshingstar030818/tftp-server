package server;

import java.io.*;
import java.net.*;

import helpers.FileStorageService;
import types.RequestType;
import packet.*;
import resource.Configurations;

/**
 * @author Team 3
 *
 *	This class represents the core logic and functionality of the TFTP
 *	system.
 */
public class TFTPService implements Runnable {

	private DatagramSocket mSendReceiveSocket;
	private DatagramPacket mLastPacket;
	private Callback mClientFinishedCallback;
	
	/**
	 * This class is initialized by the server on a separate thread.
	 * It takes care of all client interactions, and provides file transfer
	 * service
	 * 
	 * @param packet
	 */
	public TFTPService(DatagramPacket packet, Callback finCallback) {
		this.mLastPacket = packet;
		this.mClientFinishedCallback = finCallback;
		try {
			this.mSendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * This function is called when a write request is sent by client. It will reply with ACK
	 * packets until the following packets sent by the client is less than MAX_BUFFER size
	 * 
	 * @param writeRequest - write request packet from client
	 */
	private void handleFileWriteOperation(WritePacketBuilder writeRequest) {
		// when we get a write request, we need to acknowledge client first (block 0)
		AckPacketBuilder vAckPacket = new AckPacketBuilder(this.mLastPacket);
		DatagramPacket vSendPacket = vAckPacket.buildPacket();
		String v_sFileName = writeRequest.getFilename();
		try {
			// Open a channel to the file
			FileStorageService vFileStorageService = new FileStorageService (v_sFileName);
			this.mSendReceiveSocket.send(vSendPacket);
			// start write operation
			byte[] vEmptyData = new byte[Configurations.MAX_BUFFER];
			boolean vHasMore = true;
			while ( vHasMore ){
				byte[] data = new byte[Configurations.MAX_MESSAGE_SIZE];
				this.mLastPacket = new DatagramPacket(data, data.length);
				
				this.mSendReceiveSocket.receive(this.mLastPacket);
				// Extract the data from the received packet with packet builder
				if(this.mLastPacket.getLength() < Configurations.MAX_MESSAGE_SIZE) {
					int realPacketSize = this.mLastPacket.getLength();
					byte[] packetBuffer = new byte[realPacketSize];
					System.arraycopy(this.mLastPacket.getData(), 0, packetBuffer, 0, realPacketSize);
					this.mLastPacket.setData(packetBuffer);
				}
				DataPacketBuilder vDataPacketBuilder = new DataPacketBuilder(this.mLastPacket);
				vEmptyData = vDataPacketBuilder.getDataBuffer();

				vHasMore = vFileStorageService.saveFileByteBufferToDisk(vEmptyData);
				// ACK this bit of data
				vAckPacket = new AckPacketBuilder(this.mLastPacket);
				vSendPacket = vAckPacket.buildPacket();
				this.mSendReceiveSocket.send(vSendPacket);
				// Validate if a DATA packet is given
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * This function takes care of read request from client. It replies with DATA packets
	 * until the file it has opened had had been fully read by 512 byte chunks
	 * 
	 * @param readRequest - the read request data gram packet getting from the client
	 */
	private void handleFileReadOperation(ReadPacketBuilder readRequest) {
		String vFileName = readRequest.getFilename();
		try {
			FileStorageService vFileStorageService = new FileStorageService( vFileName );
			byte[] vEmptyData = new byte[Configurations.MAX_BUFFER];

			while (vEmptyData != null && vEmptyData.length >= Configurations.MAX_BUFFER ){
				vEmptyData = vFileStorageService.getFileByteBufferFromDisk();
				// Building a data packet from the last packet ie. will increment block number
				DataPacketBuilder vDataPacket = new DataPacketBuilder(this.mLastPacket);
				DatagramPacket vSendPacket = vDataPacket.buildPacket(vEmptyData);
				mSendReceiveSocket.send(vSendPacket);
				// Receive ACK packets from the client, then we can proceed to send more DATA
				byte[] data = new byte[Configurations.LEN_ACK_PACKET_BUFFET];
				DatagramPacket vReceivePacket = new DatagramPacket(data, data.length);
				mSendReceiveSocket.receive(vReceivePacket);
				if(vReceivePacket.getData()[1] != 3) {
					// its not an ack
				}
				this.mLastPacket = vReceivePacket;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		ReadWritePacketPacketBuilder vClientRequestPacket = new ReadWritePacketPacketBuilder(this.mLastPacket);
		
		RequestType reqType = vClientRequestPacket.getRequestType();
		switch(reqType) {
			// handle each request type
			case WRQ:
				WritePacketBuilder vWritePacket = new WritePacketBuilder(this.mLastPacket);
				handleFileWriteOperation(vWritePacket);
				break;
			case RRQ:
				ReadPacketBuilder vReadPacket = new ReadPacketBuilder(this.mLastPacket);
				handleFileReadOperation(vReadPacket);
				break;
			case DATA:
				break;
			case ACK:
				break;
			case ERROR:
				break;
		}
		
		this.mSendReceiveSocket.close();
		synchronized(this.mClientFinishedCallback) {
			this.mClientFinishedCallback.callback(Thread.currentThread().getId());
		}
		
	}
}

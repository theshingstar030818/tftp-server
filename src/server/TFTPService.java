package server;

import java.io.*;
import java.net.*;

import helpers.BufferPrinter;
import helpers.FileStorageService;
import types.ErrorType;
import types.Logger;
import types.RequestType;
import packet.*;
import resource.Configurations;
import resource.Strings;
import testbed.ErrorChecker;
import testbed.TFTPError;

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
	private ErrorChecker errorChecker;
	private final String CLASS_TAG = "<Server Service Thread>";
	private Logger logger = Logger.VERBOSE;
	/**
	 * This class is initialized by the server on a separate thread.
	 * It takes care of all client interactions, and provides file transfer
	 * service
	 * 
	 * @param packet
	 */
	public TFTPService(DatagramPacket packet, Callback finCallback) {
		errorChecker = new ErrorChecker(new ReadWritePacketPacketBuilder(packet));
		this.mLastPacket = packet;
		this.mClientFinishedCallback = finCallback;
		try {
			this.mSendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		logger.setClassTag(CLASS_TAG);
	}
	
	/**
	 * This function is called when a write request is sent by client. It will reply with ACK
	 * packets until the following packets sent by the client is less than MAX_BUFFER size
	 * 
	 * @param writeRequest - write request packet from client
	 */
	private void handleFileWriteOperation(WritePacketBuilder writeRequest) { // check data
		
		logger.print(Logger.DEBUG, "Server initializing client's write request ...");
		
		logger.print(Logger.DEBUG, Strings.RECEIVED);
		BufferPrinter.printPacket(writeRequest, Logger.VERBOSE, RequestType.WRQ);
		
		// when we get a write request, we need to acknowledge client first (block 0)
		AckPacketBuilder vAckPacket = new AckPacketBuilder(this.mLastPacket);
		DatagramPacket vSendPacket = vAckPacket.buildPacket();
		String v_sFileName = writeRequest.getFilename();
		try {
			// First check for formatting errors and IO errors 
			TFTPError error = errorChecker.check(writeRequest, RequestType.WRQ);
			if (error.getType() != ErrorType.NO_ERROR) {
				// Will die here if the file name is invalid.
				// By invalid, means file name is not correct or valid
				if(errorHandle(error, this.mLastPacket)) {
					return;
				}
			}
			logger.print(Logger.DEBUG, Strings.SENDING);
			BufferPrinter.printPacket(new AckPacketBuilder(vSendPacket), Logger.VERBOSE,RequestType.ACK);
			
			this.mSendReceiveSocket.send(vSendPacket);
			// Open a channel to the file
			FileStorageService vFileStorageService = new FileStorageService (v_sFileName);

			// Since we don't have an error, we can expect block size 1 to come next.
			errorChecker.incrementExpectedBlockNumber();
			byte[] vEmptyData = new byte[Configurations.MAX_BUFFER];
			boolean vHasMore = true;
			while ( vHasMore ){
				byte[] data = new byte[Configurations.MAX_BUFFER];
				this.mLastPacket = new DatagramPacket(data, data.length);
				
				
				this.mSendReceiveSocket.receive(this.mLastPacket);
				DataPacketBuilder receivedPacket = new DataPacketBuilder(this.mLastPacket);
				
				logger.print(Logger.VERBOSE, Strings.RECEIVED);
				BufferPrinter.printPacket(receivedPacket, logger, RequestType.DATA);
				
				error = errorChecker.check(receivedPacket, RequestType.DATA);
				if (error.getType() != ErrorType.NO_ERROR) {
					if(errorHandle(error, this.mLastPacket)) {
						return;
					}
				}
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
				
				logger.print(Logger.VERBOSE, Strings.SENDING);
				BufferPrinter.printPacket(vAckPacket, logger, RequestType.ACK);
				
				this.mSendReceiveSocket.send(vSendPacket);
				// Validate if a DATA packet is given
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This function takes care of read request from client. It replies with DATA packets
	 * until the file it has opened had had been fully read by 512 byte chunks
	 * 
	 * @param readRequest - the read request data gram packet getting from the client
	 */
	private void handleFileReadOperation(ReadPacketBuilder readRequest) { // check ack
		
		logger.print(Logger.DEBUG, "Server initializing client's read request ...");
		
		logger.print(Logger.DEBUG, Strings.RECEIVED);
		BufferPrinter.printPacket(readRequest, Logger.VERBOSE, RequestType.RRQ);
		
		String vFileName = readRequest.getFilename();
		try {
			// First check for formatting errors and IO errors 
			TFTPError error = errorChecker.check(readRequest, RequestType.RRQ);
			if (error.getType() != ErrorType.NO_ERROR) {
				// Will die here if the file name is invalid.
				// By invalid, means file name is not correct or valid
				if(errorHandle(error, this.mLastPacket)) {
					return;
				}
			}
			FileStorageService vFileStorageService = new FileStorageService( vFileName );
			byte[] vEmptyData = new byte[Configurations.MAX_BUFFER];

			while (vEmptyData != null && vEmptyData.length >= Configurations.MAX_PAYLOAD_BUFFER ){
				vEmptyData = vFileStorageService.getFileByteBufferFromDisk();
				// Building a data packet from the last packet ie. will increment block number
				DataPacketBuilder vDataPacket = new DataPacketBuilder(this.mLastPacket);
				DatagramPacket vSendPacket = vDataPacket.buildPacket(vEmptyData);
				
				logger.print(Logger.DEBUG, Strings.SENDING);
				BufferPrinter.printPacket(vDataPacket, Logger.VERBOSE, RequestType.ACK);
				
				mSendReceiveSocket.send(vSendPacket);
				// Receive ACK packets from the client, then we can proceed to send more DATA
				byte[] data = new byte[Configurations.MAX_BUFFER];
				DatagramPacket vReceivePacket = new DatagramPacket(data, data.length);
				mSendReceiveSocket.receive(vReceivePacket);
				error = errorChecker.check(new AckPacketBuilder(vReceivePacket), RequestType.ACK);
				if (error.getType() != ErrorType.NO_ERROR) {
					if(errorHandle(error, vReceivePacket)) {
						return;
					}
				}

				this.mLastPacket = vReceivePacket;
			}
			byte[] data = new byte[Configurations.MAX_BUFFER];
			DatagramPacket vReceivePacket = new DatagramPacket(data, data.length);
			this.mSendReceiveSocket.receive(vReceivePacket);
			
			logger.print(Logger.DEBUG, Strings.RECEIVED);
			BufferPrinter.printPacket(new AckPacketBuilder(vReceivePacket), Logger.VERBOSE, RequestType.ACK);
			
			System.err.println("If the code reached here, the bug was fixed. Make sure the last ack packet was acked");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Handle the error cases. Will return boolean to indicate whether to terminate
	 * thread or carry on.
	 * 
	 * @param error
	 * @param packet
	 * @return
	 */
	public boolean errorHandle(TFTPError error, DatagramPacket packet) {
		ErrorPacketBuilder errorPacket = new ErrorPacketBuilder(packet);
		switch (error.getType()) {
		case ILLEGAL_OPERATION:
			DatagramPacket illegalOpsError = errorPacket.buildPacket(ErrorType.ILLEGAL_OPERATION,
					error.getString());
			try {
				mSendReceiveSocket.send(illegalOpsError);
			} catch (IOException e) { e.printStackTrace(); }
			System.err.println("Illegal operation caught, shutting down");
			return true;
		case UNKNOWN_TRANSFER:
			errorPacket = new ErrorPacketBuilder(packet);
			try {
				mSendReceiveSocket.send(errorPacket.getPacket());
			} catch (IOException e) { e.printStackTrace(); }
			return false;
		default:
			System.out.println("Unhandled Exception.");
			break;
		}			
		return true;
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
			default:
				logger.print(Logger.ERROR, Strings.SS_WRONG_PACKET);
				TFTPError error = new TFTPError(ErrorType.ILLEGAL_OPERATION, Strings.SS_WRONG_PACKET);
				errorHandle(error, vClientRequestPacket.getPacket());
				break;
		}
		
		this.mSendReceiveSocket.close();
		synchronized(this.mClientFinishedCallback) {
			this.mClientFinishedCallback.callback(Thread.currentThread().getId());
		}
		logger.print(Logger.VERBOSE, Strings.SS_TRANSFER_FINISHED);
		
	}
}

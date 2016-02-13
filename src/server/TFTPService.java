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
import testbed.TFTPErrorMessage;

/**
 * @author Team 3
 *
 *         This class represents the core logic and functionality of the TFTP
 *         system.
 */
public class TFTPService implements Runnable {

	private DatagramSocket mSendReceiveSocket;
	private DatagramPacket mLastPacket;
	private Callback mClientFinishedCallback;
	private ErrorChecker errorChecker;
	private final String CLASS_TAG = "<Server Service Thread>";
	private Logger logger = Logger.VERBOSE;

	/**
	 * This class is initialized by the server on a separate thread. It takes
	 * care of all client interactions, and provides file transfer service
	 * 
	 * @param packet
	 */
	public TFTPService(DatagramPacket packet, Callback finCallback) {
		errorChecker = new ErrorChecker(new ReadWritePacketPacket(packet));
		this.mLastPacket = packet;
		this.mClientFinishedCallback = finCallback;
		try {
			this.mSendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		logger.setClassTag(CLASS_TAG);
		logger.print(Logger.SILENT,
				"Server initializing client's write request on port " + this.mSendReceiveSocket.getLocalPort());
	}

	/**
	 * This function is called when a write request is sent by client. It will
	 * reply with ACK packets until the following packets sent by the client is
	 * less than MAX_BUFFER size
	 * 
	 * @param writeRequest
	 *            - write request packet from client
	 */
	private void handleFileWriteOperation(WritePacket writeRequest) { // check
																		// data

		logger.print(Logger.SILENT, Strings.RECEIVED);
		BufferPrinter.printPacket(writeRequest, Logger.VERBOSE, RequestType.WRQ);

		// when we get a write request, we need to acknowledge client first
		// (block 0)
		AckPacket vAckPacket = new AckPacket(this.mLastPacket);
		DatagramPacket vSendPacket = vAckPacket.buildPacket();
		String v_sFileName = writeRequest.getFilename();
		try {
			// First check for formatting errors and IO errors
			TFTPErrorMessage error = errorChecker.check(writeRequest, RequestType.WRQ);
			if (error.getType() != ErrorType.NO_ERROR) {
				// Will die here if the file name is invalid.
				// By invalid, means file name is not correct or valid
				if (errorHandle(error, this.mLastPacket)) {
					return;
				}
			}
			logger.print(Logger.SILENT, Strings.SENDING);
			BufferPrinter.printPacket(new AckPacket(vSendPacket), Logger.VERBOSE, RequestType.ACK);

			this.mSendReceiveSocket.send(vSendPacket);
			// Open a channel to the file
			FileStorageService vFileStorageService = new FileStorageService(v_sFileName);

			// Since we don't have an error, we can expect block size 1 to come
			// next.
			errorChecker.incrementExpectedBlockNumber();
			byte[] vEmptyData = new byte[Configurations.MAX_BUFFER];
			boolean vHasMore = true;
			while (vHasMore) {
				boolean unknownHostFound = false;
				do {
					byte[] data = new byte[Configurations.MAX_BUFFER];
					this.mLastPacket = new DatagramPacket(data, data.length);
					this.mSendReceiveSocket.receive(this.mLastPacket);
					DataPacket receivedPacket = new DataPacket(this.mLastPacket);
					logger.print(Logger.VERBOSE, Strings.RECEIVED);
					BufferPrinter.printPacket(receivedPacket, logger, RequestType.DATA);
					error = errorChecker.check(receivedPacket, RequestType.DATA);
					if (error.getType() != ErrorType.NO_ERROR) {
						if (errorHandle(error, this.mLastPacket)) {
							return;
						}
						unknownHostFound = true;
					} else {
						unknownHostFound = false;
					}
				} while (unknownHostFound);
				// Extract the data from the received packet with packet builder
				if (this.mLastPacket.getLength() < Configurations.MAX_MESSAGE_SIZE) {
					int realPacketSize = this.mLastPacket.getLength();
					byte[] packetBuffer = new byte[realPacketSize];
					System.arraycopy(this.mLastPacket.getData(), 0, packetBuffer, 0, realPacketSize);
					this.mLastPacket.setData(packetBuffer);
				}

				DataPacket vDataPacketBuilder = new DataPacket(this.mLastPacket);
				vEmptyData = vDataPacketBuilder.getDataBuffer();

				vHasMore = vFileStorageService.saveFileByteBufferToDisk(vEmptyData);
				// ACK this bit of data

				vAckPacket = new AckPacket(this.mLastPacket);
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
	 * This function takes care of read request from client. It replies with
	 * DATA packets until the file it has opened had had been fully read by 512
	 * byte chunks
	 * 
	 * @param readRequest
	 *            - the read request data gram packet getting from the client
	 */
	private void handleFileReadOperation(ReadPacket readRequest) { // check ack

		logger.print(Logger.SILENT, "Server initializing client's read request ...");

		logger.print(Logger.SILENT, Strings.RECEIVED);
		BufferPrinter.printPacket(readRequest, Logger.VERBOSE, RequestType.RRQ);
		DatagramPacket vReceivePacket;
		String vFileName = readRequest.getFilename();
		AckPacket ackPacket;
		try {
			// First check for formatting errors and IO errors
			TFTPErrorMessage error = errorChecker.check(readRequest, RequestType.RRQ);
			if (error.getType() != ErrorType.NO_ERROR) {
				// Will die here if the file name is invalid.
				// By invalid, means file name is not correct or valid
				if (errorHandle(error, this.mLastPacket)) {
					return;
				}
			}

			errorChecker.incrementExpectedBlockNumber();

			FileStorageService vFileStorageService = new FileStorageService(vFileName);
			byte[] vEmptyData = new byte[Configurations.MAX_BUFFER];

			while (vEmptyData != null && vEmptyData.length >= Configurations.MAX_PAYLOAD_BUFFER) {

				boolean receivedFromUnknownHost = false;
				vEmptyData = vFileStorageService.getFileByteBufferFromDisk();
				// Building a data packet from the last packet ie. will
				// increment block number
				DataPacket vDataPacket = new DataPacket(this.mLastPacket);
				DatagramPacket vSendPacket = vDataPacket.buildPacket(vEmptyData);
				logger.print(Logger.SILENT, Strings.SENDING);
				BufferPrinter.printPacket(vDataPacket, Logger.VERBOSE, RequestType.DATA);
				mSendReceiveSocket.send(vSendPacket);

				do {
					// Receive ACK packets from the client, then we can proceed
					// to send more DATA
					byte[] data = new byte[Configurations.MAX_BUFFER];
					vReceivePacket = new DatagramPacket(data, data.length);
					mSendReceiveSocket.receive(vReceivePacket);
					ackPacket = new AckPacket(vReceivePacket);
					error = errorChecker.check(ackPacket, RequestType.ACK);
					if (error.getType() != ErrorType.NO_ERROR) {
						if (errorHandle(error, vReceivePacket)) {
							return;
						}
						receivedFromUnknownHost = true;
					} else {
						receivedFromUnknownHost = false;
					}
				} while (receivedFromUnknownHost);
				this.mLastPacket = vReceivePacket;

			}
			byte[] data = new byte[Configurations.MAX_BUFFER];
			vReceivePacket = new DatagramPacket(data, data.length);
			this.mSendReceiveSocket.receive(vReceivePacket);

			logger.print(Logger.SILENT, Strings.RECEIVED);
			BufferPrinter.printPacket(new AckPacket(vReceivePacket), Logger.VERBOSE, RequestType.ACK);

			System.err.println("If the code reached here, the bug was fixed. Make sure the last ack packet was acked");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handle the error cases. Will return boolean to indicate whether to
	 * terminate thread or carry on.
	 * 
	 * @param error
	 *            - TFTPErrorMessage class with the request type and error string
	 * @param packet
	 *            - the datagram packet that resulted in the error
	 * @return - whether the thread should carry on or die
	 */
	public boolean errorHandle(TFTPErrorMessage error, DatagramPacket packet) {
		ErrorPacket errorPacket = new ErrorPacket(packet);
		switch (error.getType()) {
		case ILLEGAL_OPERATION:
			DatagramPacket illegalOpsError = errorPacket.buildPacket(ErrorType.ILLEGAL_OPERATION, error.getString());
			try {
				mSendReceiveSocket.send(illegalOpsError);
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.print(Logger.ERROR, Strings.ILLEGAL_OPERATION_HELP_MESSAGE);
			return true;
		case UNKNOWN_TRANSFER:
			DatagramPacket unknownError = errorPacket.buildPacket(ErrorType.ILLEGAL_OPERATION, error.getString());
			try {
				logger.print(Logger.ERROR, Strings.UNKNOWN_TRANSFER_HELP_MESSAGE);
				mSendReceiveSocket.send(unknownError);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		default:
			System.err.println("Unhandled Exception.");
			break;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		ReadWritePacketPacket vClientRequestPacket = new ReadWritePacketPacket(this.mLastPacket);

		RequestType reqType = vClientRequestPacket.getRequestType();
		switch (reqType) {
		// handle each request type
		case WRQ:
			WritePacket vWritePacket = new WritePacket(this.mLastPacket);
			handleFileWriteOperation(vWritePacket);
			break;
		case RRQ:
			ReadPacket vReadPacket = new ReadPacket(this.mLastPacket);
			handleFileReadOperation(vReadPacket);
			break;
		default:
			logger.print(Logger.ERROR, Strings.SS_WRONG_PACKET);
			TFTPErrorMessage error = new TFTPErrorMessage(ErrorType.ILLEGAL_OPERATION, Strings.SS_WRONG_PACKET);
			errorHandle(error, vClientRequestPacket.getPacket());
			break;
		}

		this.mSendReceiveSocket.close();
		synchronized (this.mClientFinishedCallback) {
			this.mClientFinishedCallback.callback(Thread.currentThread().getId());
		}
		logger.print(Logger.VERBOSE, Strings.SS_TRANSFER_FINISHED);

	}
}

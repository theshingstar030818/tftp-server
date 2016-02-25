package networking;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import helpers.BufferPrinter;
import helpers.FileStorageService;
import packet.AckPacket;
import packet.DataPacket;
import packet.ErrorPacket;
import packet.ReadWritePacket;
import resource.Configurations;
import resource.Strings;
import testbed.ErrorChecker;
import testbed.TFTPErrorMessage;
import types.ErrorType;
import types.Logger;
import types.RequestType;

public class TFTPNetworking {
	
	protected DatagramSocket socket;
	protected DatagramPacket lastPacket;
	protected ErrorChecker errorChecker; // This is a temporary measure.
	protected Logger logger = Logger.VERBOSE;
	protected String fileName;
	protected FileStorageService storage;
	
	public TFTPNetworking() {
		lastPacket = null;
		errorChecker = null;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public TFTPNetworking(ReadWritePacket p) {
		lastPacket = p.getPacket();
		errorChecker = new ErrorChecker(p);
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public TFTPNetworking(ReadWritePacket p, DatagramSocket s) {
		lastPacket = p.getPacket();
		errorChecker = new ErrorChecker(p);
		socket = s;
	}
	
	
	public TFTPErrorMessage receiveFile() {
		return receiveFile(socket);
	}
	
	
	public TFTPErrorMessage receiveFile(DatagramSocket vSocket) {
		
		// when we get a write request, we need to acknowledge client first (block 0)
		socket = vSocket;
		AckPacket vAckPacket = new AckPacket(lastPacket);
		DatagramPacket vSendPacket = vAckPacket.buildPacket();
		TFTPErrorMessage error;
		
		try {
			
			// socket.send(vSendPacket);
			// Open a channel to the file
			// Since we don't have an error, we can expect block size 1 to come next.
			// errorChecker.incrementExpectedBlockNumber();
			byte[] vEmptyData = new byte[Configurations.MAX_BUFFER];
			boolean vHasMore = true;
			
			while ( vHasMore ){
				while (true) {
					byte[] data = new byte[Configurations.MAX_BUFFER];
					lastPacket = new DatagramPacket(data, data.length);
					socket.receive(lastPacket);
					if (errorChecker == null) {
						errorChecker = new ErrorChecker(new DataPacket(lastPacket));
						errorChecker.incrementExpectedBlockNumber();
					}
					DataPacket receivedPacket = new DataPacket(lastPacket);
					logger.print(Logger.VERBOSE, Strings.RECEIVED);
					BufferPrinter.printPacket(receivedPacket, logger, RequestType.DATA);
					error = errorChecker.check(receivedPacket, RequestType.DATA);
					
					if (error.getType() == ErrorType.NO_ERROR) break;
					if (errorHandle(error, lastPacket)) return error;
				}
				
				// Extract the data from the received packet with packet builder
				if(lastPacket.getLength() < Configurations.MAX_MESSAGE_SIZE) {
					int realPacketSize = lastPacket.getLength();
					byte[] packetBuffer = new byte[realPacketSize];
					System.arraycopy(lastPacket.getData(), 0, packetBuffer, 0, realPacketSize);
					lastPacket.setData(packetBuffer);
				}
				
				DataPacket vDataPacketBuilder = new DataPacket(lastPacket);
				vEmptyData = vDataPacketBuilder.getDataBuffer();

				vHasMore = storage.saveFileByteBufferToDisk(vEmptyData);
				// ACK this bit of data
				
				vAckPacket = new AckPacket(lastPacket);
				vSendPacket = vAckPacket.buildPacket();
				
				logger.print(Logger.VERBOSE, Strings.SENDING);
				BufferPrinter.printPacket(vAckPacket, logger, RequestType.ACK);
				
				socket.send(vSendPacket);
				// Validate if a DATA packet is given
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new TFTPErrorMessage(ErrorType.NO_ERROR, Strings.NO_ERROR);
	}
	
	
	
	public TFTPErrorMessage sendFile(ReadWritePacket packet) {
		BufferPrinter.printPacket(packet, Logger.VERBOSE, RequestType.RRQ);
		fileName = packet.getFilename();
		return sendFile();
	}
	
	
	public TFTPErrorMessage sendFile() {

		DatagramPacket receivePacket;
		AckPacket ackPacket;
		
		try {
			//errorChecker.incrementExpectedBlockNumber();
			
			byte[] vEmptyData = new byte[Configurations.MAX_BUFFER];
			TFTPErrorMessage error;

			while (vEmptyData != null && vEmptyData.length >= Configurations.MAX_PAYLOAD_BUFFER ){
				
				vEmptyData = storage.getFileByteBufferFromDisk();
				// Building a data packet from the last packet ie. will increment block number
				DataPacket vDataPacket = new DataPacket(lastPacket);
				DatagramPacket vSendPacket = vDataPacket.buildPacket(vEmptyData);
				logger.print(Logger.SILENT, Strings.SENDING);
				BufferPrinter.printPacket(vDataPacket, Logger.VERBOSE, RequestType.ACK);
				socket.send(vSendPacket);
					
				while (true) {
					// Receive ACK packets from the client.
					byte[] data = new byte[Configurations.MAX_BUFFER];
					receivePacket = new DatagramPacket(data, data.length);
					socket.receive(receivePacket);
					ackPacket = new AckPacket(receivePacket);
					error = errorChecker.check(ackPacket, RequestType.ACK);
					
					if (error.getType() == ErrorType.NO_ERROR) break;
					
					if (errorHandle(error, receivePacket)) return error; 
				}
				
				lastPacket = receivePacket;
				
			}
//			byte[] data = new byte[Configurations.MAX_BUFFER];
//			receivePacket = new DatagramPacket(data, data.length);
//			System.out.println("Foos");
//			socket.receive(receivePacket);
//			System.out.println("Ball");
//			logger.print(Logger.SILENT, Strings.RECEIVED);
//			BufferPrinter.printPacket(new AckPacket(receivePacket), Logger.VERBOSE, RequestType.ACK);
//			
//			System.err.println("If the code reached here, the bug was fixed. Make sure the last ack packet was acked");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new TFTPErrorMessage(ErrorType.NO_ERROR, Strings.NO_ERROR);
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
				DatagramPacket illegalOpsError = errorPacket.buildPacket(ErrorType.ILLEGAL_OPERATION,
						error.getString());
				
				try {
					socket.send(illegalOpsError);
				} catch (IOException e) { e.printStackTrace(); }
				logger.print(Logger.ERROR, Strings.ILLEGAL_OPERATION_HELP_MESSAGE);
				
				return true;
				
			case UNKNOWN_TRANSFER:
				DatagramPacket unknownError = errorPacket.buildPacket(ErrorType.ILLEGAL_OPERATION,
						error.getString());
				
				try {
					socket.send(unknownError);
				} catch (IOException e) { e.printStackTrace(); }
				logger.print(Logger.ERROR, Strings.UNKNOWN_TRANSFER_HELP_MESSAGE);
				
				return false;
				
			default:
				System.out.println("Unhandled Exception.");
				break;
		}			
		return true;
	}
}

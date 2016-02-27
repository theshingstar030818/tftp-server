package networking;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

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
	protected ErrorChecker errorChecker;
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
		TFTPErrorMessage error;
		lastPacket = null;
		DatagramPacket recvPacket = new DatagramPacket(new byte[Configurations.MAX_BUFFER], Configurations.MAX_BUFFER);
		
		try {
			socket.setSoTimeout(Configurations.TRANMISSION_TIMEOUT);
			byte[] vEmptyData = new byte[Configurations.MAX_BUFFER];
			boolean vHasMore = true;
			
			while ( vHasMore ){
				while (true) {
					
					try { 
						socket.receive(recvPacket);
					} catch (SocketTimeoutException e) {
						if (lastPacket == null) {
							return null;
						}
						sendACK(lastPacket);
						continue;
					}
					
					lastPacket = recvPacket;
					if (errorChecker == null) {
						errorChecker = new ErrorChecker(new DataPacket(lastPacket));
						errorChecker.incrementExpectedBlockNumber();
					}
					DataPacket receivedPacket = new DataPacket(lastPacket);
					logger.print(Logger.VERBOSE, Strings.RECEIVED);
					BufferPrinter.printPacket(receivedPacket, logger, RequestType.DATA);
					error = errorChecker.check(receivedPacket, RequestType.DATA);
					
					if (error.getType() == ErrorType.NO_ERROR) break;
					if (errorHandle(error, lastPacket, RequestType.DATA)) return error;
				}
				
				// Extract the data from the received packet with packet builder
				if(lastPacket.getLength() < Configurations.MAX_MESSAGE_SIZE) {
					int realPacketSize = lastPacket.getLength();
					byte[] packetBuffer = new byte[realPacketSize];
					System.arraycopy(lastPacket.getData(), 0, packetBuffer, 0, realPacketSize);
					lastPacket.setData(packetBuffer);
				}
				
				errorChecker.incrementExpectedBlockNumber();
				
				DataPacket vDataPacketBuilder = new DataPacket(lastPacket);
				vEmptyData = vDataPacketBuilder.getDataBuffer();

				vHasMore = storage.saveFileByteBufferToDisk(vEmptyData);
				
				sendACK(lastPacket);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new TFTPErrorMessage(ErrorType.NO_ERROR, Strings.NO_ERROR);
	}
	
	private void sendACK(DatagramPacket packet) {
		
		logger.print(Logger.VERBOSE, Strings.SENDING);
		BufferPrinter.printPacket(new AckPacket(packet), logger, RequestType.ACK);
		
		try {
			socket.send(new AckPacket(packet).buildPacket());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			
			byte[] vEmptyData = new byte[Configurations.MAX_BUFFER];
			TFTPErrorMessage error;
			socket.setSoTimeout(Configurations.TRANMISSION_TIMEOUT);

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
					try {
						socket.receive(receivePacket);
					} catch (SocketTimeoutException e) {
						socket.send(vSendPacket);
						continue;
					}
					ackPacket = new AckPacket(receivePacket);
					error = errorChecker.check(ackPacket, RequestType.ACK);
					
					if (error.getType() == ErrorType.NO_ERROR) break;
					
					if (errorHandle(error, receivePacket, RequestType.ACK)) return error; 
				}
				errorChecker.incrementExpectedBlockNumber();
				lastPacket = receivePacket;
				
			}

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
		return errorHandle(error, packet, null);
	}
	
	public boolean errorHandle(TFTPErrorMessage error, DatagramPacket packet, RequestType recvType) {
		ErrorPacket errorPacket = new ErrorPacket(packet);
		switch (error.getType()) {
			case ILLEGAL_OPERATION:
				if(error.getString().equals(Strings.BLOCK_NUMBER_MISMATCH)) {
					if (recvType == RequestType.DATA)
						sendACK(packet);
					return false;
				}
				
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

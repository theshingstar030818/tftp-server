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
import packet.PacketBuilder;
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
	protected int retries = 0;
	
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
	
	
	public TFTPErrorMessage sendFile(ReadWritePacket packet) {
		BufferPrinter.printPacket(packet, Logger.VERBOSE, RequestType.RRQ);
		fileName = packet.getFilename();
		return sendFile();
	}
	
	public TFTPErrorMessage receiveFile(DatagramSocket vSocket) {
		
		// when we get a write request, we need to acknowledge client first (block 0)
		socket = vSocket;
		TFTPErrorMessage error;
		DatagramPacket receivePacket = new DatagramPacket(new byte[Configurations.MAX_BUFFER], Configurations.MAX_BUFFER);
		boolean retriesExceeded = false;
		try {
			byte[] vEmptyData = new byte[Configurations.MAX_BUFFER];
			boolean vHasMore = true;
			while ( vHasMore ){
				while (true) {
					try { 
						socket.receive(receivePacket);
					} catch (SocketTimeoutException e) {
						logger.print(Logger.ERROR, "Socket Timeout on received file! Resending Ack!");
						sendACK(lastPacket);
						System.err.println("Sent a timeout packet.");
						if(++retries == Configurations.RETRANMISSION_TRY) {
							if(vHasMore) {
								logger.print(Logger.ERROR, String.format("Retries exceeded on last packet. Last Packet was lost. Otherside must had gotten finished with blocks."));
							} else {
								logger.print(Logger.ERROR, String.format("Retransmission retried %d times, no reply, shutting down.", retries));
							}
							if (errorChecker.getExpectedBlockNumber() == 0) // Timeout on first block.
								return null;
							retriesExceeded = true;
							break;
						}
						continue;
					}
					
					lastPacket = receivePacket;
					if (errorChecker == null) {
						errorChecker = new ErrorChecker(new DataPacket(lastPacket));
						errorChecker.incrementExpectedBlockNumber();
					}
				//	System.err.println("Excepted block number = " + errorChecker.mExpectedBlockNumber);
					DataPacket receivedPacket = new DataPacket(lastPacket);
					error = errorChecker.check(receivedPacket, RequestType.DATA);
					logger.print(Logger.VERBOSE, Strings.RECEIVED);
					BufferPrinter.printPacket(receivedPacket, logger, RequestType.DATA);
					
					if (error.getType() == ErrorType.NO_ERROR) break;
					if (error.getType() == ErrorType.SORCERERS_APPRENTICE) sendACK(lastPacket);
					if (errorHandle(error, lastPacket, RequestType.DATA)) return error;
				}
				retries = 0;
				// Extract the data from the received packet with packet builder
				if(lastPacket.getLength() < Configurations.MAX_MESSAGE_SIZE) {
					int realPacketSize = lastPacket.getLength();
					byte[] packetBuffer = new byte[realPacketSize];
					System.arraycopy(lastPacket.getData(), 0, packetBuffer, 0, realPacketSize);
					lastPacket.setData(packetBuffer);
				}
				if(retriesExceeded) break;

				DataPacket vDataPacketBuilder = new DataPacket(lastPacket);
				vEmptyData = vDataPacketBuilder.getDataBuffer();

				vHasMore = storage.saveFileByteBufferToDisk(vEmptyData);
				if(vHasMore) errorChecker.incrementExpectedBlockNumber();
				sendACK(lastPacket);
			}
			// Wait on last DATA in case of the last data was lost.
			socket.setSoTimeout(Configurations.TRANMISSION_TIMEOUT * 2);
			while(true) {
				try{
					
					byte[] data = new byte[Configurations.MAX_BUFFER];
					receivePacket = new DatagramPacket(data, data.length);
					socket.receive(receivePacket);
					lastPacket = receivePacket;
					DataPacket receivedPacket = new DataPacket(lastPacket);
					error = errorChecker.check(receivedPacket, RequestType.DATA);
					logger.print(Logger.VERBOSE, Strings.RECEIVED);
					BufferPrinter.printPacket(receivedPacket, logger, RequestType.DATA);
					
					if (error.getType() == ErrorType.NO_ERROR) {
						sendACK(lastPacket);
						break;
					}
					
					if (error.getType() == ErrorType.SORCERERS_APPRENTICE) sendACK(lastPacket);
					if (errorHandle(error, lastPacket, RequestType.DATA)) return error;
				} catch (SocketTimeoutException e) {
					if(++retries == Configurations.RETRANMISSION_TRY) {
						logger.print(Logger.ERROR, String.format("Retransmission retried %d times, send file considered done.", retries));
						retriesExceeded = true;
						break;
					}
				}
			}
			
			socket.setSoTimeout(Configurations.TRANMISSION_TIMEOUT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new TFTPErrorMessage(ErrorType.NO_ERROR, Strings.NO_ERROR);
	}
	
	public TFTPErrorMessage sendFile() {

		DatagramPacket receivePacket;
		AckPacket ackPacket;
		short currentSendBlockNumber = 0;
		lastPacket = new DatagramPacket(new byte[Configurations.MAX_MESSAGE_SIZE], 
				Configurations.MAX_MESSAGE_SIZE, lastPacket.getAddress(), lastPacket.getPort());
		try {
			byte[] vEmptyData = new byte[Configurations.MAX_BUFFER];
			TFTPErrorMessage error;	
			boolean retriesExceeded = false;
			while (vEmptyData != null && vEmptyData.length >= Configurations.MAX_PAYLOAD_BUFFER ){
				
				vEmptyData = storage.getFileByteBufferFromDisk();
				// Building a data packet from the last packet ie. will increment block number
				DataPacket vDataPacket = new DataPacket(lastPacket);
				vDataPacket.setBlockNumber(currentSendBlockNumber);
				++currentSendBlockNumber; // For the next packet, never rely on the ACK block number to provide this
				DatagramPacket vSendPacket = vDataPacket.buildPacket(vEmptyData); // buildPacket increments val currentSendBlockNumber by 1
				logger.print(Logger.VERBOSE, Strings.SENDING);
				BufferPrinter.printPacket(vDataPacket, Logger.VERBOSE, RequestType.DATA);
				socket.send(vSendPacket);
					
				while (true) {
					// Receive ACK packets from the client.
					byte[] data = new byte[Configurations.MAX_BUFFER];
					receivePacket = new DatagramPacket(data, data.length);
					try {
						socket.receive(receivePacket);
					} catch (SocketTimeoutException e) {
						logger.print(Logger.ERROR, "Socket Timeout on send file! Resending Data!");
						BufferPrinter.printPacket(vDataPacket, Logger.VERBOSE, RequestType.DATA);
						socket.send(vSendPacket);
						if(++retries == Configurations.RETRANMISSION_TRY) {
							if(vEmptyData.length < Configurations.MAX_PAYLOAD_BUFFER ) {
								logger.print(Logger.ERROR, String.format("Retries exceeded on last packet. Last Packet was lost. Otherside must had gotten finished with blocks."));
							} else {
								logger.print(Logger.ERROR, String.format("Retransmission retried %d times, giving up due to network error.", retries));
							}
							retriesExceeded = true;
							break;
						}
						continue;
					}
					ackPacket = new AckPacket(receivePacket);
						
					logger.print(Logger.VERBOSE, Strings.RECEIVED);
					BufferPrinter.printPacket(ackPacket, Logger.VERBOSE, RequestType.ACK);
					
					error = errorChecker.check(ackPacket, RequestType.ACK);
					if (error.getType() == ErrorType.NO_ERROR) break; 
					if (error.getType() == ErrorType.ILLEGAL_OPERATION)
					if (error.getType() == ErrorType.SORCERERS_APPRENTICE) {
						continue;
					}
					if (errorHandle(error, receivePacket, RequestType.ACK)) return error; 
				}
				if(retriesExceeded) break;
				retries = 0;
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
	
	protected void sendACK(DatagramPacket packet) {
		logger.print(Logger.VERBOSE, Strings.SENDING);
		AckPacket ackPacket = new AckPacket(packet);
		ackPacket.buildPacket();
		BufferPrinter.printPacket(ackPacket, logger, RequestType.ACK);
		try {
			socket.send(ackPacket.getPacket());
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
				
				if (error.getString().equals(Strings.UNKNOWN_TRANSFER)) {
					System.out.println("Other host no longer connected.");
					return true;
				}
				
				DatagramPacket illegalOpsError = errorPacket.buildPacket(ErrorType.ILLEGAL_OPERATION,
						error.getString());
				
				try {
					socket.send(illegalOpsError);
				} catch (IOException e) { e.printStackTrace(); }
				logger.print(Logger.ERROR, Strings.ILLEGAL_OPERATION_HELP_MESSAGE);
				BufferPrinter.printPacket(new ErrorPacket(packet), Logger.ERROR, RequestType.ERROR);
				return true;
				
			case UNKNOWN_TRANSFER:
				DatagramPacket unknownError = errorPacket.buildPacket(ErrorType.ILLEGAL_OPERATION,
						error.getString());
				
				try {
					socket.send(unknownError);
				} catch (IOException e) { e.printStackTrace(); }
				logger.print(Logger.ERROR, Strings.UNKNOWN_TRANSFER_HELP_MESSAGE);
				BufferPrinter.printPacket(new ErrorPacket(packet), Logger.ERROR, RequestType.ERROR);
				return false;
			
			case SORCERERS_APPRENTICE:
				return false;
				
			default:
				System.out.println("Unhandled Exception.");
				break;
		}			
		return true;
	}
}

/**
 * 
 */
package client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

import helpers.BufferPrinter;
import helpers.FileStorageService;
import helpers.Keyboard;
import packet.*;
import resource.*;
import testbed.ErrorChecker;
import testbed.TFTPErrorMessage;
import types.*;

/**
 * @author Team 3
 *
 *         This class represents a TFTP console application for interfacing with
 */
public class TFTPClient {

	private DatagramSocket sendReceiveSocket;
	private boolean isClientAlive = true;
	private final String CLASS_TAG = "<TFTP Client>";
	private int mPortToSendTo;

	private int mode;

	// by default the logger is set to VERBOSE level
	private Logger logger = Logger.VERBOSE;

	// Error checker
	ErrorChecker errorChecker = null;

	public static void main(String[] args) {
		TFTPClient vClient = new TFTPClient();
		vClient.initialize();
	}

	/**
	 * This function initializes the client's functionality and block the rest
	 * of the program from running until a exit command was given.
	 */
	public void initialize() {
		logger.setClassTag(this.CLASS_TAG);
		Scanner scan = new Scanner(System.in);
		try {
			mode = getSendPort();
			if (mode == 1) {
				this.mPortToSendTo = Configurations.SERVER_LISTEN_PORT;
			} else {
				this.mPortToSendTo = Configurations.ERROR_SIM_LISTEN_PORT;
			}
			setLogLevel();

			int optionSelected = 0;

			while (isClientAlive) {
				System.out.println(UIStrings.MENU_CLIENT_SELECTION);

				try {
					optionSelected = Keyboard.getInteger();
				} catch (NumberFormatException e) {
					optionSelected = 0;
				}
				errorChecker = null;
				switch (optionSelected) {
				case 1:
					// Read file
					logger.print(logger, Strings.PROMPT_ENTER_FILE_NAME);
					String readFileName = Keyboard.getString();
					try {
						TFTPErrorMessage result = readRequestHandler(readFileName);
						if (result.getType() != ErrorType.NO_ERROR) {
							logger.print(Logger.ERROR, Strings.TRANSFER_FAILED);
							logger.print(Logger.ERROR, result.getString());
							if (!sendReceiveSocket.isClosed()) {
								sendReceiveSocket.close();
							}
						} else {
							logger.print(Logger.VERBOSE, Strings.TRANSFER_SUCCESSFUL);
						}
					} catch (Exception e) {
						if (logger == Logger.VERBOSE)
							e.printStackTrace();

						logger.print(Logger.ERROR, Strings.TRANSFER_FAILED);
					}
					break;
				case 2:
					// Write file
					logger.print(logger, Strings.PROMPT_FILE_NAME_PATH);
					String writeFileNameOrFilePath = Keyboard.getString();
					File f = new File(writeFileNameOrFilePath);
					if (!f.exists() || f.isDirectory()) {
						logger.print(logger, Strings.FILE_NOT_EXIST);
						break;
					}
					TFTPErrorMessage result = writeRequestHandler(writeFileNameOrFilePath);
					if (!(result.getType() == ErrorType.NO_ERROR)) {
						logger.print(Logger.ERROR, Strings.TRANSFER_FAILED);
						logger.print(Logger.ERROR, result.getString());
					} else {
						logger.print(Logger.VERBOSE, Strings.TRANSFER_SUCCESSFUL);
					}
					break;
				case 3:
					// shutdown client
					isClientAlive = !isClientAlive;
					logger.print(Logger.VERBOSE, Strings.EXIT_BYE);
					break;

				default:
					logger.print(Logger.ERROR, Strings.ERROR_INPUT);
					break;
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			scan.close();
		}
	}

	/**
	 * This function create the write request for a client and transfers the
	 * file from local disc to the server in 512 Byte blocks
	 * 
	 * @param writeFileName
	 *            - the name of the file that the client requests to send to
	 *            server
	 * @throws SocketException
	 */
	private TFTPErrorMessage writeRequestHandler(String writeFileNameOrFilePath) throws SocketException {

		logger.print(logger, Strings.CLIENT_INITIATE_WRITE_REQUEST);
		sendReceiveSocket = new DatagramSocket();
		ReadWritePacketPacket wpb;
		FileStorageService writeRequestFileStorageService;
		DataPacket dataPacket;
		AckPacket ackPacket;
		DatagramPacket lastPacket;
		byte[] fileData = new byte[Configurations.MAX_PAYLOAD_BUFFER];
		byte[] packetBuffer;
		try {
			logger.print(logger, Strings.CLIENT_INITIATING_FIE_STORAGE_SERVICE);
			writeRequestFileStorageService = new FileStorageService(writeFileNameOrFilePath, InstanceType.CLIENT);

			String actualFileName;

			actualFileName = writeRequestFileStorageService.getFileName();

			wpb = new WritePacket(InetAddress.getLocalHost(), this.mPortToSendTo, actualFileName,
					Configurations.DEFAULT_RW_MODE);

			lastPacket = wpb.buildPacket();
			logger.print(logger, Strings.SENDING);
			BufferPrinter.printPacket(wpb, logger, RequestType.WRQ);
			sendReceiveSocket.send(lastPacket);

			while (fileData != null && fileData.length >= Configurations.MAX_PAYLOAD_BUFFER) {

				boolean illegalTransferIDFound = false;

				do {
					// This packet has the block number to start on!
					packetBuffer = new byte[Configurations.MAX_BUFFER];
					lastPacket = new DatagramPacket(packetBuffer, packetBuffer.length);

					sendReceiveSocket.receive(lastPacket);
					logger.print(logger, "Recevied : ");

					if (!illegalTransferIDFound) {
						// get the first block of file to transfer
						fileData = writeRequestFileStorageService.getFileByteBufferFromDisk();
					}

					// Initialize DataPacket with block number n
					ackPacket = new AckPacket(lastPacket);
					BufferPrinter.printPacket(ackPacket, logger, RequestType.ACK);

					if (errorChecker == null) {
						errorChecker = new ErrorChecker(ackPacket);
					}
					TFTPErrorMessage currErrorType = errorChecker.check(ackPacket, RequestType.ACK);
					if (currErrorType.getType() != ErrorType.NO_ERROR) {

						if (errorHandle(currErrorType, ackPacket.getPacket())) {
							errorChecker = null;
							return currErrorType;
						}
						illegalTransferIDFound = true;
					} else {
						illegalTransferIDFound = false;
					}
				} while (illegalTransferIDFound);

				// Overwrite last packet
				dataPacket = new DataPacket(lastPacket);
				lastPacket = dataPacket.buildPacket(fileData);

				logger.print(logger, Strings.SENDING);
				BufferPrinter.printPacket(dataPacket, logger, RequestType.DATA);
				sendReceiveSocket.send(lastPacket);
			}
			// Receive the last ACK.
			packetBuffer = new byte[Configurations.MAX_BUFFER];
			lastPacket = new DatagramPacket(packetBuffer, packetBuffer.length);
			sendReceiveSocket.receive(lastPacket);

			BufferPrinter.printPacket(new AckPacket(lastPacket), logger, RequestType.ACK);
			logger.print(logger, "Recevied last packet.");
			sendReceiveSocket.close();

		} catch (Exception e) {
			e.printStackTrace();
			errorChecker = null;
			return new TFTPErrorMessage(ErrorType.NOT_DEFINED, e.getMessage());
		}
		errorChecker = null;
		return new TFTPErrorMessage(ErrorType.NO_ERROR, "no errors");
	}

	/**
	 * This function create a read request for the client and stores the file
	 * retrieved from the server on to the file system
	 * 
	 * @param readFileName
	 *            - the name of the file that the client requests from server
	 */
	private TFTPErrorMessage readRequestHandler(String readFileName) throws Exception {

		logger.print(logger, Strings.CLIENT_INITIATE_READ_REQUEST);
		sendReceiveSocket = new DatagramSocket();
		AckPacket ackPacket;
		DatagramPacket lastPacket;
		DataPacket dataPacket;
		boolean morePackets = true;
		FileStorageService readRequestFileStorageService;

		byte[] dataBuf = new byte[Configurations.MAX_MESSAGE_SIZE];

		try {
			logger.print(logger, Strings.CLIENT_INITIATING_FIE_STORAGE_SERVICE);
			readRequestFileStorageService = new FileStorageService(readFileName, InstanceType.CLIENT);
			// build read request packet

			ReadPacket rpb;

			rpb = new ReadPacket(InetAddress.getLocalHost(), this.mPortToSendTo, readFileName,
					Configurations.DEFAULT_RW_MODE);

			// now get the packet from the ReadPacket
			lastPacket = rpb.buildPacket();

			logger.print(logger, Strings.SENDING);
			BufferPrinter.printPacket(rpb, logger, RequestType.RRQ);
			// send the read packet over sendReceiveSocket
			sendReceiveSocket.send(lastPacket);

			// loop until no more packets to receive
			while (morePackets) {

				while (true) {

					dataBuf = new byte[Configurations.MAX_BUFFER];
					lastPacket = new DatagramPacket(dataBuf, dataBuf.length);

					// receive a data packet
					sendReceiveSocket.receive(lastPacket);
					logger.print(logger, "Recevied : ");

					// Use the packet builder class to manage and extract the
					// data
					dataPacket = new DataPacket(lastPacket);
					BufferPrinter.printPacket(dataPacket, logger, RequestType.DATA);

					if (errorChecker == null) {
						errorChecker = new ErrorChecker(dataPacket);
						errorChecker.incrementExpectedBlockNumber();
					}

					TFTPErrorMessage currErrorType = errorChecker.check(dataPacket, RequestType.DATA);
					if (currErrorType.getType() == ErrorType.NO_ERROR)
						break;

					if (errorHandle(currErrorType, dataPacket.getPacket()))
						return currErrorType;

				}
				byte[] fileData = dataPacket.getDataBuffer();
				// Save the last packet file buffer
				morePackets = readRequestFileStorageService.saveFileByteBufferToDisk(fileData);

				// Prepare to ACK the data packet
				ackPacket = new AckPacket(lastPacket);
				// Always send the ACK back to the error sim (BAD)

				lastPacket = ackPacket.buildPacket();

				logger.print(logger, Strings.SENDING);
				BufferPrinter.printPacket(ackPacket, logger, RequestType.ACK);

				sendReceiveSocket.send(lastPacket);
			}
			sendReceiveSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
			errorChecker = null;
			return new TFTPErrorMessage(ErrorType.NOT_DEFINED, "Exception thrown");
		}
		errorChecker = null;
		return new TFTPErrorMessage(ErrorType.NO_ERROR, "no errors");
	}

	/**
	 * This function sets the client to use the error simulator or not
	 * 
	 * @return mode 1 is do not use and 2 is use.
	 */
	private int getSendPort() {
		while (true) {
			System.out.println(UIStrings.CLIENT_MODE);

			int mode = Keyboard.getInteger();

			if (mode == 1) {
				return mode;
			} else if (mode == 2) {
				return mode;
			} else {
				logger.print(Logger.ERROR, Strings.ERROR_INPUT);
			}

		}
	}

	/**
	 * This function only prints the client side selection menu
	 */
	private void setLogLevel() {

		int optionSelected;

		while (true) {
			System.out.println(UIStrings.CLIENT_LOG_LEVEL_SELECTION);

			try {
				optionSelected = Keyboard.getInteger();
			} catch (NumberFormatException e) {
				optionSelected = 0;
			}

			if (optionSelected == 1) {
				this.logger = Logger.VERBOSE;
				break;
			} else if (optionSelected == 2) {
				this.logger = Logger.SILENT;
				break;
			} else {
				logger.print(Logger.ERROR, Strings.ERROR_INPUT);
			}
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
				sendReceiveSocket.send(illegalOpsError);
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.print(Logger.ERROR, Strings.ILLEGAL_OPERATION + " shutting down");
			byte[] string = error.getString().getBytes();
			if (string.length > Configurations.MAX_MESSAGE_SIZE) {
				int i = 0;
				for (i = 0; i < string.length; ++i) {
					if (string[i] == 0) {
						break;
					}
				}
				if (i != 0) {
					String message = "";
					byte[] trimmedString = new byte[i];

					System.arraycopy(string, 0, trimmedString, 0, i);
					message = new String(trimmedString);
					error.setString(message);
				}
			}
			return true;
		case UNKNOWN_TRANSFER:
			try {
				DatagramPacket unknownTransferID = errorPacket.buildPacket(ErrorType.UNKNOWN_TRANSFER,
						error.getString());
				sendReceiveSocket.send(unknownTransferID);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		default:
			logger.print(Logger.ERROR, Strings.NOT_DEFINED);
			break;
		}
		return true;
	}

}

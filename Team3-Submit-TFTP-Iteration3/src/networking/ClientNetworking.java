package networking;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import helpers.BufferPrinter;
import helpers.FileStorageService;
import packet.AckPacket;
import packet.DataPacket;
import packet.ReadPacket;
import packet.ReadWritePacket;
import packet.WritePacket;
import resource.Configurations;
import resource.Strings;
import testbed.ErrorChecker;
import testbed.TFTPErrorMessage;
import types.ErrorType;
import types.InstanceType;
import types.Logger;
import types.RequestType;

/**
 * @author Team 3
 * 
 *         This class is responsible for handling all network aspects for the
 *         client. ClientNetworking is a custom tailored version of
 *         TFTPNetworking class which defines a new set initialization
 *         functionality.
 */
public class ClientNetworking extends TFTPNetworking {

	/**
	 * See constructor from TFTPNetworking
	 */
	public ClientNetworking() {
		super();
	}

	/**
	 * See constructor from TFTPNetworking
	 */
	public ClientNetworking(ReadWritePacket p) {
		super(p);
	}

	/**
	 * See constructor from TFTPNetworking
	 */
	public ClientNetworking(ReadWritePacket p, DatagramSocket s) {
		super(p, s);
	}

	/**
	 * This function sets up the requirements for a write request. It takes care
	 * of creating a file, initializing a channel to it, and streaming the first
	 * byte block into the file. After connection is set up, this function
	 * delegates the task of receiving files back to TFTPNetworking
	 * 
	 * @param fn
	 *            - file name to process, this will be ensured a valid file name
	 * @param portToSendTo
	 *            - the port of send the request to
	 * @return - TFTPErrorMessage with error type and error string (possible no
	 *         error)
	 */
	public TFTPErrorMessage generateInitWRQ(String fn, int portToSendTo) {
		TFTPErrorMessage error = null;
		try {
			socket.setSoTimeout(Configurations.TRANMISSION_TIMEOUT);
			storage = new FileStorageService(fn, InstanceType.CLIENT);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		logger.print(logger, Strings.CLIENT_INITIATE_WRITE_REQUEST);
		ReadWritePacket wpb;
		lastPacket = null;
		AckPacket wrqFirstAck;
		try {
			logger.print(logger, Strings.CLIENT_INITIATING_FIE_STORAGE_SERVICE);

			wpb = new WritePacket(InetAddress.getLocalHost(), portToSendTo, storage.getFileName(),
					Configurations.DEFAULT_RW_MODE);
			fileName = storage.getFileName();
			DatagramPacket lastWritePacket = wpb.buildPacket();
			lastPacket = lastWritePacket;
			logger.print(logger, Strings.SENDING);
			BufferPrinter.printPacket(wpb, logger, RequestType.WRQ);
			int attempts = 0;
			while (true) {
				socket.send(lastPacket);
				try {
					lastPacket = new DatagramPacket(new byte[Configurations.MAX_MESSAGE_SIZE],
							Configurations.MAX_MESSAGE_SIZE, lastPacket.getAddress(), lastPacket.getPort());
					socket.receive(lastPacket);
					logger.print(Logger.VERBOSE, Strings.RECEIVED);
					wrqFirstAck = new AckPacket(lastPacket);
					BufferPrinter.printPacket(wrqFirstAck, Logger.VERBOSE, RequestType.ACK);
				} catch (SocketTimeoutException e) {

					if (++attempts == Configurations.RETRANMISSION_TRY) {
						System.out.println(Strings.CLIENT_CONNECTION_FAILURE);
						return null;
					}

					lastPacket = lastWritePacket;

					continue;
				}
				break;
			}
			super.lastPacket = this.lastPacket;
			// Trusts that the first response is from expected source.
			errorChecker = new ErrorChecker(wrqFirstAck);
			error = errorChecker.check(wrqFirstAck, RequestType.ACK);
			errorChecker.incrementExpectedBlockNumber();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return error;
	}

	/**
	 * This function create a initial read request for the client and stores the
	 * file retrieved from the server on to the file system. It takes care of
	 * creating a file, initializing a channel to it, and streaming the first
	 * byte block into the file. After connection is set up, this function
	 * delegates the task of receiving files back to TFTPNetworking
	 * 
	 * @param fn
	 *            - the name of the file that the client requests from server
	 * @param portToSendTo
	 *            - the port of send the request to
	 * @return - TFTPErrorMessage with error type and error string (possible no
	 *         error)
	 */
	public TFTPErrorMessage generateInitRRQ(String fn, int portToSendTo) {
		try {
			logger.print(logger, Strings.CLIENT_INITIATING_FIE_STORAGE_SERVICE);
			fileName = fn;
			socket.setSoTimeout(Configurations.TRANMISSION_TIMEOUT);
			try {
				storage = new FileStorageService(fileName, InstanceType.CLIENT);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			// build read request packet

			ReadPacket rpb = new ReadPacket(InetAddress.getLocalHost(), portToSendTo, fileName,
					Configurations.DEFAULT_RW_MODE);
			DatagramPacket lastReadPacket = rpb.buildPacket();
			// now get the packet from the ReadPacket
			lastPacket = lastReadPacket;
			while (true) {
				try {
					logger.print(logger, Strings.SENDING);
					BufferPrinter.printPacket(rpb, logger, RequestType.RRQ);
					// send the read packet over sendReceiveSocket
					socket.send(lastPacket);
					lastPacket = new DatagramPacket(new byte[Configurations.MAX_MESSAGE_SIZE],
							Configurations.MAX_MESSAGE_SIZE, lastPacket.getAddress(), lastPacket.getPort());
					socket.receive(lastPacket);
					break;
				} catch (SocketTimeoutException e) {
					lastPacket = lastReadPacket;
					if(++retries == Configurations.RETRANMISSION_TRY) {
						logger.print(Logger.ERROR, String.format(Strings.RETRANSMISSION, retries));
						return new TFTPErrorMessage(ErrorType.TRANSMISSION_ERROR, Strings.CLIENT_TRANSMISSION_ERROR);
					}
					logger.print(Logger.VERBOSE, Strings.CLIENT_TIME_OUT);
					continue;
				}
			}
			if (errorChecker == null) {
				errorChecker = new ErrorChecker(new DataPacket(lastPacket));
				errorChecker.incrementExpectedBlockNumber();
			}
			byte[] vEmptyData = new byte[Configurations.MAX_BUFFER];
			DataPacket receivedPacket = new DataPacket(lastPacket);
			TFTPErrorMessage error = errorChecker.check(receivedPacket, RequestType.DATA);
			logger.print(Logger.VERBOSE, Strings.RECEIVED);
			BufferPrinter.printPacket(receivedPacket, logger, RequestType.DATA);
			
			if (error.getType() == ErrorType.NO_ERROR) {
				vEmptyData = receivedPacket.getDataBuffer();
				storage.saveFileByteBufferToDisk(vEmptyData);
				return new TFTPErrorMessage(ErrorType.NO_ERROR, Strings.NO_ERROR);
			}
			if (error.getType() == ErrorType.SORCERERS_APPRENTICE) super.sendACK(lastPacket);
			if (errorHandle(error, lastPacket, RequestType.DATA)) return error;
			errorChecker.incrementExpectedBlockNumber();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		retries = 0;
		return new TFTPErrorMessage(ErrorType.NO_ERROR, Strings.NO_ERROR);
	}

}

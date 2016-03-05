package networking;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import helpers.BufferPrinter;
import helpers.FileStorageService;
import packet.AckPacket;
import packet.ReadWritePacket;
import resource.Configurations;
import resource.Strings;
import testbed.TFTPErrorMessage;
import types.ErrorType;
import types.InstanceType;
import types.Logger;
import types.RequestType;

/**
 * @author Team 3
 * 
 *         This class is responsible for handling all network aspects for the
 *         Server. ServerNetworking is a custom tailored version of
 *         TFTPNetworking class which defines a new set initialization
 *         functionality.
 */
public class ServerNetworking extends TFTPNetworking {

	/**
	 * See constructor from TFTPNetworking
	 */
	public ServerNetworking() {
		super();
	}

	/**
	 * See constructor from TFTPNetworking
	 */
	public ServerNetworking(ReadWritePacket p) {
		super(p);
	}

	/**
	 * See constructor from TFTPNetworking
	 */
	public ServerNetworking(ReadWritePacket p, DatagramSocket s) {
		super(p, s);
	}

	/**
	 * Handles the initial read request that the client has sent. It takes care
	 * of creating a file, initializing a channel to it, and streaming the first
	 * byte block into the file. After connection is set up, this function
	 * delegates the task of receiving files back to TFTPNetworking
	 * 
	 * @param wrq
	 *            - the read or write packet that comes in (in generality)
	 * @return - TFTPErrorMessage with error type and error string (possible no
	 *         error)
	 */
	public TFTPErrorMessage handleInitWRQ(ReadWritePacket wrq) {

		fileName = wrq.getFilename();
		TFTPErrorMessage error = errorChecker.check(wrq, RequestType.WRQ);
		if (error.getType() != ErrorType.NO_ERROR)
			if (errorHandle(error, wrq.getPacket()))
				return error;

		try {
			storage = new FileStorageService(fileName, InstanceType.SERVER);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		errorChecker.incrementExpectedBlockNumber();

		AckPacket vAckPacket = new AckPacket(wrq.getPacket());
		DatagramPacket vSendPacket = vAckPacket.buildPacket();

		logger.print(Logger.VERBOSE, Strings.SENDING);
		BufferPrinter.printPacket(new AckPacket(vSendPacket), Logger.VERBOSE, RequestType.ACK);

		try {
			super.socket.setSoTimeout(Configurations.TRANMISSION_TIMEOUT);
			socket.send(vSendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.lastPacket = vSendPacket;
		return new TFTPErrorMessage(ErrorType.NO_ERROR, Strings.NO_ERROR);
	}

	/**
	 * Handles the initial write request that the client has sent. It takes care
	 * of creating a file, initializing a channel to it, and streaming the first
	 * byte block into the file. After connection is set up, this function
	 * delegates the task of receiving files back to TFTPNetworking
	 * 
	 * @param rrq
	 *            - the read or write packet that comes in (in generality)
	 * @return - TFTPErrorMessage with error type and error string (possible no
	 *         error)
	 */
	public TFTPErrorMessage handleInitRRQ(ReadWritePacket rrq) {

		fileName = rrq.getFilename();
		TFTPErrorMessage error = errorChecker.check(rrq, RequestType.RRQ);
		if (error.getType() != ErrorType.NO_ERROR)
			if (errorHandle(error, rrq.getPacket()))
				return error;
		try {
			storage = new FileStorageService(fileName, InstanceType.SERVER);
			super.socket.setSoTimeout(Configurations.TRANMISSION_TIMEOUT);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		errorChecker.incrementExpectedBlockNumber();

		return new TFTPErrorMessage(ErrorType.NO_ERROR, Strings.NO_ERROR);
	}

}

package networking;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.AccessDeniedException;

import helpers.BufferPrinter;
import helpers.FileStorageService;
import packet.AckPacket;
import packet.ReadWritePacket;
import resource.Configurations;
import resource.Strings;
import testbed.TFTPErrorMessage;
import types.DirectoryAccessViolationException;
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
		super(InstanceType.SERVER);
	}

	/**
	 * See constructor from TFTPNetworking
	 */
	public ServerNetworking(ReadWritePacket p) {
		super(p, InstanceType.SERVER);
	}

	/**
	 * See constructor from TFTPNetworking
	 */
	public ServerNetworking(ReadWritePacket p, DatagramSocket s) {
		super(p, s, InstanceType.SERVER);
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
	 * @throws IOException 
	 */
	public TFTPErrorMessage handleInitWRQ(ReadWritePacket wrq, Logger log){

		this.logger = log;
		fileName = wrq.getFilename();
		TFTPErrorMessage error = errorChecker.check(wrq, RequestType.WRQ);
		if (error.getType() != ErrorType.NO_ERROR) {
			if (errorHandle(error, wrq.getPacket())) {
				//this.storage.deleteFileFromDisk();
				return error;
			}
		}
		if( FileStorageService.checkFileNameExists(Configurations.SERVER_ROOT_FILE_DIRECTORY+"/"+fileName) ){
			String message = String.format(Strings.PRE_FILE_NAME_EXIST + Strings.FILE_EXISTS, fileName);
			return new TFTPErrorMessage(ErrorType.FILE_EXISTS, message);
		}
		
		try {
			storage = new FileStorageService(fileName, InstanceType.SERVER, RequestType.WRQ);
			storage.lockFile();
			System.out.println("Locked the write file");
		} catch (DirectoryAccessViolationException e) {
			if(this.storage != null)
				this.storage.deleteFileFromDisk();
			return new TFTPErrorMessage(ErrorType.ACCESS_VIOLATION, Strings.MKDIR_FAIL);
		} catch (FileNotFoundException e) {
			if(this.storage != null)
				this.storage.deleteFileFromDisk();
			e.printStackTrace();
		} catch (AccessDeniedException e) {
			error = new TFTPErrorMessage(ErrorType.ACCESS_VIOLATION, e.getFile());
			if(this.storage != null)
				this.storage.deleteFileFromDisk();
			return error;
		}catch (IOException e) {
			error = new TFTPErrorMessage(ErrorType.NOT_DEFINED, "Unknown IO Exception occurred.");
			if(this.storage != null)
				this.storage.deleteFileFromDisk();
			return error;
		}

		errorChecker.incrementExpectedBlockNumber();

		AckPacket vAckPacket = new AckPacket(wrq.getPacket());
		DatagramPacket vSendPacket = vAckPacket.buildPacket();

		logger.print(Logger.VERBOSE, Strings.SENDING);
		BufferPrinter.printPacket(new AckPacket(vSendPacket), this.logger, RequestType.ACK);

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
	 * @throws IOException 
	 */
	public TFTPErrorMessage handleInitRRQ(ReadWritePacket rrq, Logger log){

		this.logger = log;
		fileName = rrq.getFilename();
		TFTPErrorMessage error = errorChecker.check(rrq, RequestType.RRQ);
		if (error.getType() != ErrorType.NO_ERROR)
				return error;
		if (!FileStorageService.checkFileNameExists(Configurations.SERVER_ROOT_FILE_DIRECTORY+"/"+fileName)){
			return new TFTPErrorMessage(ErrorType.FILE_NOT_FOUND, Strings.FILE_NOT_FOUND);
		}
		
		try {
			storage = new FileStorageService(fileName, InstanceType.SERVER, RequestType.RRQ);
			super.socket.setSoTimeout(Configurations.TRANMISSION_TIMEOUT);
		} catch (DirectoryAccessViolationException e) {
			//this.storage.deleteFileFromDisk();
			return new TFTPErrorMessage(ErrorType.ACCESS_VIOLATION, Strings.MKDIR_FAIL);
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (AccessDeniedException e) {
			return new TFTPErrorMessage(ErrorType.ACCESS_VIOLATION, e.getFile());
		} catch (SocketException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

		errorChecker.incrementExpectedBlockNumber();

		return new TFTPErrorMessage(ErrorType.NO_ERROR, Strings.NO_ERROR);
	}

}

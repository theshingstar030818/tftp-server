package networking;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import helpers.BufferPrinter;
import helpers.FileStorageService;
import packet.AckPacket;
import packet.ReadPacket;
import packet.ReadWritePacket;
import packet.WritePacket;
import resource.Configurations;
import resource.Strings;
import testbed.ErrorChecker;
import types.InstanceType;
import types.RequestType;

public class ClientNetworking extends TFTPNetworking {

	public ClientNetworking() {}

	public DatagramPacket generateInitWRQ(String fn, int portToSendTo) {
		
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
		try {
			logger.print(logger, Strings.CLIENT_INITIATING_FIE_STORAGE_SERVICE);
			
			wpb = new WritePacket(InetAddress.getLocalHost(), portToSendTo, storage.getFileName(),
					Configurations.DEFAULT_RW_MODE);
			fileName = storage.getFileName();

			lastPacket = wpb.buildPacket();
			logger.print(logger, Strings.SENDING);
			BufferPrinter.printPacket(wpb, logger, RequestType.WRQ);
			while (true) {
				socket.send(lastPacket);
				try {
					socket.receive(lastPacket);
				} catch (SocketTimeoutException e) {
					continue;
				}
				break;
			}
			
			// Trusts that the first response is from expected source.
			errorChecker = new ErrorChecker(new AckPacket(lastPacket)); 
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
		return lastPacket;
	}
	
	/**
	 * This function create a read request for the client and stores the file
	 * retrieved from the server on to the file system
	 * 
	 * @param readFileName
	 *            - the name of the file that the client requests from server
	 */
	public void generateInitRRQ(String fn, int portToSendTo) {
		try {
			logger.print(logger, Strings.CLIENT_INITIATING_FIE_STORAGE_SERVICE);
			fileName = fn;
			try {
				storage = new FileStorageService(fileName, InstanceType.CLIENT);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			// build read request packet
	
			ReadPacket rpb;
	
			rpb = new ReadPacket(InetAddress.getLocalHost(), portToSendTo, fileName,
					Configurations.DEFAULT_RW_MODE);
	
			// now get the packet from the ReadPacket
			lastPacket = rpb.buildPacket();
	
			logger.print(logger, Strings.SENDING);
			BufferPrinter.printPacket(rpb, logger, RequestType.RRQ);
			// send the read packet over sendReceiveSocket
			socket.send(lastPacket);
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

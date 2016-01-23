/**
 * 
 */
package server;
import java.net.DatagramPacket;

import helpers.FileStorageService;
import types.RequestType;

/**
 * @author Team 3
 *
 *	This class represents the core logic and functionality of the TFTP
 *	system.
 */
public class TFTPService implements Runnable {

	private DatagramPacket inPacket;
	
	/**
	 * 
	 */
	public TFTPService() {
		// TODO Auto-generated constructor stub
	}
	
	public TFTPService(DatagramPacket packet) {
		this.inPacket = packet;
	}
	
	//return type is wrong
	private void handleFileWriteOperation() {

	}
	
	private void handleFileReadOperation() {
		/*
		   byte[] data = new byte[4906];
		   //inPacket = new TFTP(data, data.length);
		   data = inPacket.getDataBody();
		   
		   
		 */
	}
	
	private void handleOnOperationError() {
		
	}
	
	private void handleConnectionAcknowledgement() {
		byte[] a = RequestType.WRQ.getHeaderByteArray();
	}
	
	private byte[] generateResponse(RequestType opCode) {
		byte[] a = new byte[1];
		a[0] = 1;
		return a;
	}
	
	public void run() {
		
	}
}

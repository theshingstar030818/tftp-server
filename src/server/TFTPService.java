package server;

import java.net.*;

import helpers.BufferPrinter;
import networking.ServerNetworking;
import types.ErrorType;
import types.Logger;
import types.RequestType;
import packet.*;
import resource.Strings;
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
	private final String CLASS_TAG = "<Server Service Thread>";
	private Logger logger = Logger.VERBOSE;

	/**
	 * This class is initialized by the server on a separate thread. It takes
	 * care of all client interactions, and provides file transfer service
	 * 
	 * @param packet
	 */
	public TFTPService(DatagramPacket packet, Callback finCallback) {
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

	
	public void run() {

		ReadWritePacket vClientRequestPacket = new ReadWritePacket(this.mLastPacket);
		RequestType reqType = vClientRequestPacket.getRequestType();
		ServerNetworking net;
		TFTPErrorMessage result;
		
		switch(reqType) {
			case WRQ:
				WritePacket vWritePacket = new WritePacket(this.mLastPacket);
				
				logger.print(Logger.SILENT, Strings.RECEIVED);
				BufferPrinter.printPacket(vWritePacket, Logger.VERBOSE, RequestType.WRQ);
				
				
				net = new ServerNetworking(vWritePacket, mSendReceiveSocket);
				do {
				net.handleInitWRQ(vWritePacket);
				result = net.receiveFile(mSendReceiveSocket);
				} while (result == null);
				
				break;
				
			case RRQ:
				ReadPacket vReadPacket = new ReadPacket(this.mLastPacket);

				logger.print(Logger.SILENT, "Server initializing client's read request ...");
				logger.print(Logger.SILENT, Strings.RECEIVED);
				BufferPrinter.printPacket(vReadPacket, Logger.VERBOSE, RequestType.RRQ);
						
				net = new ServerNetworking(vReadPacket);
				do {
					net.handleInitRRQ(vReadPacket);
					result = net.sendFile(vReadPacket);	
				} while(result == null);
				
				break;
				
			default:
				logger.print(Logger.ERROR, Strings.SS_WRONG_PACKET);
				TFTPErrorMessage error = new TFTPErrorMessage(ErrorType.ILLEGAL_OPERATION, Strings.SS_WRONG_PACKET);
				new ServerNetworking(vClientRequestPacket).errorHandle(error, vClientRequestPacket.getPacket());
				// While it might not be a WRQ we're expecting, the effect is the same.
				break;
		}

		this.mSendReceiveSocket.close();
		synchronized (this.mClientFinishedCallback) {
			this.mClientFinishedCallback.callback(Thread.currentThread().getId());
		}
		logger.print(Logger.VERBOSE, Strings.SS_TRANSFER_FINISHED);
	
	}
}
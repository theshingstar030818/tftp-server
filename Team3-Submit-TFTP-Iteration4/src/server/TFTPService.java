package server;

import java.io.IOException;
import java.net.*;
import java.nio.file.AccessDeniedException;

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
		logger.print(logger,
				"Server initializing client's write request on port " + this.mSendReceiveSocket.getLocalPort());
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		ReadWritePacket vClientRequestPacket = new ReadWritePacket(this.mLastPacket);
		RequestType reqType = vClientRequestPacket.getRequestType();
		ServerNetworking net;
		TFTPErrorMessage result = null;

		switch (reqType) {
		case WRQ:
			WritePacket vWritePacket = new WritePacket(this.mLastPacket);

			logger.print(logger, Strings.RECEIVED);
			BufferPrinter.printPacket(vWritePacket, logger, RequestType.WRQ);

			net = new ServerNetworking(vWritePacket, mSendReceiveSocket);
			result = net.handleInitWRQ(vWritePacket);
			if (!result.getString().equals(Strings.NO_ERROR)) {
				net.errorHandle(result, vWritePacket.getPacket(), RequestType.WRQ);
				break;
			}
			result = net.receiveFile(mSendReceiveSocket);

			break;

		case RRQ:
			ReadPacket vReadPacket = new ReadPacket(this.mLastPacket);

			logger.print(logger, "Server initializing client's read request ...");
			logger.print(logger, Strings.RECEIVED);
			BufferPrinter.printPacket(vReadPacket, logger, RequestType.RRQ);

			net = new ServerNetworking(vReadPacket);

			result = net.handleInitRRQ(vReadPacket);
			if (!result.getString().equals(Strings.NO_ERROR)) {
				net.errorHandle(result, vReadPacket.getPacket(), RequestType.RRQ);
				break;
			}
			result = net.sendFile(vReadPacket);

			break;

		default:
			logger.print(Logger.ERROR, Strings.SS_WRONG_PACKET);
			TFTPErrorMessage error = new TFTPErrorMessage(ErrorType.ILLEGAL_OPERATION, Strings.SS_WRONG_PACKET);
			new ServerNetworking(vClientRequestPacket).errorHandle(error, vClientRequestPacket.getPacket());
			// While it might not be a WRQ we're expecting, the effect is the
			// same.
			break;
		}

		this.mSendReceiveSocket.close();
		synchronized (this.mClientFinishedCallback) {
			this.mClientFinishedCallback.callback(Thread.currentThread().getId());
		}
		logger.print(logger, Strings.SS_TRANSFER_FINISHED);

	}
}
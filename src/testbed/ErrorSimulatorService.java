/**
 * 
 */
package testbed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import helpers.BufferPrinter;
import helpers.Keyboard;
import resource.Configurations;
import resource.Strings;
import resource.Tuple;
import resource.UIStrings;
import server.Callback;
import testbed.errorcode.ErrorCodeFive;
import testbed.errorcode.ErrorCodeFour;
import types.ErrorType;
import types.InstanceType;
import types.Logger;
import types.RequestType;

/**
 * @author Team 3
 *
 *         This class serves as the intermediate host between our client server.
 *         The primary object of this class is to simulate UDP errors in order
 *         to test the soundness of our TFTP system
 */
public class ErrorSimulatorService implements Runnable {

	// by default set the log level to debug
	private Logger logger = Logger.VERBOSE;

	private final String CLASS_TAG = "<Error Simulator Thread>";

	private int mForwardPort;
	private final int mClientPort;
	private final InetAddress mClientHostAddress;
	private final int mServerListenPort;
	private InetAddress mServerHostAddress;

	private Callback mCallback;
	private Tuple<ErrorType, Integer> mErrorSettings;
	private DatagramPacket mLastPacket;
	private DatagramSocket mSendReceiveSocket;
	private RequestType mInitialRequestType;
	private InstanceType mMessUpThisTransfer;

	private byte[] mBuffer = null;
	
	/* Section of uninitialized Error Producers */
	private ErrorCodeFour mEPFour = null;
	private ErrorCodeFive mEPFive = null;
	/* Lazy initialization for Error Producers */

	/**
	 * This thread manages the facilitation of packets from the client to the
	 * server. It remembers where the packet comes from and also fixes the
	 * destination of the packet. The configurations for this handler is in the
	 * resource/Configurations class.
	 * 
	 * @param inDatagram
	 *            - last packet received, first instance will tell use who to
	 *            reply to
	 * @param cb
	 *            - call back to tell main thread this runnable finished
	 * @param errorSetting
	 *            - configurable to determine which error to produce for this
	 *            thread
	 */
	@SuppressWarnings("unused")
	public ErrorSimulatorService(DatagramPacket inDatagram, Callback cb, Tuple<ErrorType, Integer> errorSetting, InstanceType instance) {
		this.mLastPacket = inDatagram;
		this.mErrorSettings = errorSetting;
		this.mCallback = cb;
		this.mClientHostAddress = inDatagram.getAddress();
		this.mClientPort = inDatagram.getPort();
		this.mServerListenPort = Configurations.SERVER_LISTEN_PORT;
		this.mForwardPort = this.mServerListenPort;
		this.mInitialRequestType = RequestType.matchRequestByNumber((int) this.mLastPacket.getData()[1]);
		try {
			if (Configurations.SERVER_INET_HOST == "localhost") {
				this.mServerHostAddress = InetAddress.getLocalHost();
			} else {
				this.mServerHostAddress = InetAddress.getByName(Configurations.SERVER_INET_HOST);
			}
			this.mSendReceiveSocket = new DatagramSocket();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.mMessUpThisTransfer = instance;
		logger.setClassTag(CLASS_TAG);
		logger.print(Logger.SILENT, "Initalized error sim service on port " + this.mSendReceiveSocket.getLocalPort() + "\n");
	}

	/**
	 * This function will mediate traffic between the client and the server In
	 * coming packets from the client will be repackaged into a new
	 * DatagramPacket and sent to the server. In coming response packets from
	 * the server will be directly forwarded back to the client
	 * 
	 * @throws IOException
	 */
	public void run() {
		DatagramPacket serverPacket = null;
		boolean transferNotFinished = true;
		boolean ackServerOnLastBlockByClient = false;
		boolean errorSentToClient = false;
		boolean errorSendToServer = false;
		int wrqPacketSize = -1;

		while (transferNotFinished) {
			try {
				// On first iteration mForwardPort = Server Listen port
				// Proceeding iterations, mForwardPort will change to Server
				// Thread Port
				this.mLastPacket.setPort(this.mForwardPort);
				this.mLastPacket.setAddress(this.mServerHostAddress);
				if(this.mMessUpThisTransfer == InstanceType.SERVER) {
					this.createSpecifiedError(this.mLastPacket);
				}
				
				logger.print(Logger.SILENT,"Preparing to send packet to server at port " + this.mForwardPort);
				// Send off this that is directed to the server
				forwardPacketToSocket(this.mLastPacket);
				
				if(!transferNotFinished) {
					// Break here is for the last ACK packet from client 
					// during a read request. 
					break;
				}
				
				logger.print(Logger.SILENT, Strings.ES_RETRIEVE_PACKET_SERVER);
				// Wait/block for a server reply
				serverPacket = retrievePacketFromSocket();
				
				// This following block, checks if we are on the last packet to
				// be sent
				if (this.mInitialRequestType == RequestType.RRQ) {
					if (serverPacket.getLength() < Configurations.MAX_MESSAGE_SIZE) {
						// Coming into this block means that on a RRQ, the last
						// data block
						// must be ACK'd by the client to the server. Taking us
						// one extra operation
						// If this was a WRQ, then what we forward down
						// following this if statement
						// is the server's last ACK
						transferNotFinished = false;
						ackServerOnLastBlockByClient = true;
					}
				} else if (this.mInitialRequestType == RequestType.WRQ && wrqPacketSize > 0) {
					// Must test if this was the first transfer wrqPacketSize =
					// 0
					if (wrqPacketSize < Configurations.MAX_MESSAGE_SIZE) {
						// We have finished the transfer
						logger.print(Logger.SILENT, Strings.ES_GOT_LAST_PACKET_WRQ);
						transferNotFinished = false;
					}
				}

				this.mLastPacket = serverPacket;
				// Set the mForwardPort to the Server's Thread Port
				this.mForwardPort = serverPacket.getPort();

				// Redirect the packet back to the client address
				this.mLastPacket.setPort(this.mClientPort);
				this.mLastPacket.setAddress(this.mClientHostAddress);
				
				if(this.mMessUpThisTransfer == InstanceType.CLIENT) {
					this.createSpecifiedError(this.mLastPacket);
				}
				
				logger.print(Logger.SILENT, Strings.ES_SEND_PACKET_CLIENT);
				// Send that packet back to the client
				forwardPacketToSocket(this.mLastPacket);
				
				if(this.mLastPacket.getData()[1] == 5) {
					errorSentToClient = true;
					break;
				}
				
				
				logger.print(Logger.SILENT, Strings.ES_RETRIEVE_PACKET_CLIENT);
				// Receiving from client
				this.mLastPacket = retrievePacketFromSocket();
				// Set the write packet size in order to determine the end
				wrqPacketSize = this.mLastPacket.getLength();
				

				if (ackServerOnLastBlockByClient) {
					// This extra process is needed on a read request
					// Gets a client reply - last ACK
					// Send the last ACK to the server
					logger.print(Logger.SILENT, "Sending last ACK packet to server (RRQ) " + this.mClientPort);
					this.mLastPacket.setPort(this.mForwardPort);
					this.mLastPacket.setAddress(this.mServerHostAddress);
					forwardPacketToSocket(this.mLastPacket);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(errorSentToClient || errorSendToServer) {
			logger.print(Logger.ERROR, Strings.ES_TRANSFER_ERROR);
		} else {
			logger.print(Logger.SILENT, Strings.ES_TRANSFER_SUCCESS);
		}
		
		this.mSendReceiveSocket.close();
		this.mCallback.callback(Thread.currentThread().getId());
	}

	/**
	 * Determines where or not corrupt a datagram packet
	 * 
	 * @param inPacket - a packet to corrupt or not
	 */
	private void createSpecifiedError(DatagramPacket inPacket) {
		ErrorType vErrType = this.mErrorSettings.first;
		int subOpt = this.mErrorSettings.second;
		switch (vErrType) {
		case FILE_NOT_FOUND:
			// error code 1
			break;
		case ACCESS_VIOLATION:
			// error code 2
			break;
		case ALLOCATION_EXCEED:
			// error code 3
			break;
		case ILLEGAL_OPERATION:
			// error code 4
			if(this.mEPFour == null) {
				this.mEPFour = new ErrorCodeFour(inPacket, subOpt);
			} else {
				this.mEPFour.constructPacketBuilder(inPacket);
			}
			this.mLastPacket = mEPFour.errorPacketCreator();
			break;
		case UNKNOWN_TRANSFER:
			//Thread codeFiveThread = new Thread(new ErrorCodeFive(inPacket), "Error Code 5 thread");
			//codeFiveThread.start();
			if(this.mEPFive == null) {
				this.mEPFive = new ErrorCodeFive(inPacket);
			}
			this.mEPFive.run();
			// error code 5 thread
			break;
		case FILE_EXISTS:
			// error code 6
			break;
		case NO_SUCH_USER:
			// error code 5
			break;
		default:
			// Don't create an error
			break;
		}
	}

	/**
	 * This function takes care of sending the packet to any address that is
	 * identifies by the DatagramPacket
	 * 
	 * @param inUDPPacket
	 *            describes a packet that requires to be sent
	 * @throws IOException
	 */
	private void forwardPacketToSocket(DatagramPacket inUDPPacket) throws IOException {
		sendPacket(new DatagramPacket(inUDPPacket.getData(), inUDPPacket.getLength(), inUDPPacket.getAddress(),
				inUDPPacket.getPort()));
	}

	/**
	 * This function takes care of sending the packet to any address that is
	 * identifies by the DatagramPacket and uses the DatagramSocket parameter to
	 * send to the host
	 * 
	 * @param inUDPPacket
	 *            describes a packet that requires to be sent
	 * @param socket
	 *            describes a socket that the packet is sent on
	 * @throws IOException
	 */
	private void forwardPacketToSocket(DatagramPacket inUDPPacket, DatagramSocket socket) throws IOException {
		sendPacket(new DatagramPacket(inUDPPacket.getData(), inUDPPacket.getLength(), inUDPPacket.getAddress(),
				inUDPPacket.getPort()), socket);
	}

	/**
	 * This function will use the initialized DatagramSocket to send off the
	 * incoming packet and print the packet buffer to console. This method will
	 * not close the socket it is meant to send on
	 * 
	 * @param packet
	 *            represents the DatagramPacket that requires to be sent
	 * @param sendSocket
	 *            represents the DatagramSocket to use
	 * @throws IOException
	 */
	private void sendPacket(DatagramPacket packet, DatagramSocket sendSocket) throws IOException {
		sendSocket.send(packet);
		BufferPrinter.printBuffer(packet.getData(), CLASS_TAG, logger);
	}

	/**
	 * Deprecated. Cannot be used to get a reply as the socket will be closed
	 * right after a send happens. This function will use the initialized
	 * DatagramSocket to send off the incoming packet and print the packet
	 * buffer to console
	 * 
	 * @param packet
	 *            represents the DatagramPacket that requires to be sent
	 * @throws IOException
	 */
	private void sendPacket(DatagramPacket packet) throws IOException {
		this.mSendReceiveSocket.send(packet);
		BufferPrinter.printBuffer(packet.getData(), CLASS_TAG, logger);
	}

	/**
	 * This function handles the retrieval of a response, sent back to the
	 * client. This function also trims the packet received from any unwanted
	 * trailing zeros.
	 * 
	 * @param socket
	 *            to receive from
	 * @return returns the DatagramPacket that the socket as received
	 * @throws IOException
	 */
	private DatagramPacket retrievePacketFromSocket() throws IOException {
		mBuffer = new byte[Configurations.MAX_BUFFER];
		DatagramPacket receivePacket = new DatagramPacket(mBuffer, mBuffer.length);
		this.mSendReceiveSocket.receive(receivePacket);

		int realPacketSize = receivePacket.getLength();
		byte[] packetBuffer = new byte[realPacketSize];
		System.arraycopy(receivePacket.getData(), 0, packetBuffer, 0, realPacketSize);
		receivePacket.setData(packetBuffer);

		BufferPrinter.printBuffer(receivePacket.getData(), CLASS_TAG, logger);
		return receivePacket;
	}
}

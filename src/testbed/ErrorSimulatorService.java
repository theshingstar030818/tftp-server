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
import java.util.LinkedList;

import helpers.BufferPrinter;
import resource.Configurations;
import resource.Strings;
import server.Callback;
import testbed.errorcode.ErrorCodeFive;
import testbed.errorcode.ErrorCodeFour;
import testbed.errorcode.TransmissionError;
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

	/* Networking Variables */
	private int mForwardPort;
	private final int mClientPort;
	private final InetAddress mClientHostAddress;
	private final int mServerListenPort;
	private InetAddress mServerHostAddress;

	/* Core logic variables */
	private LinkedList<DatagramPacket> mPacketSendQueue;
	private Callback mCallback;
	private ErrorCommand mErrorSettings;
	private DatagramPacket mLastPacket;
	private DatagramSocket mSendReceiveSocket;
	private RequestType mInitialRequestType;
	private InstanceType mMessUpThisTransfer;
	private byte[] mBuffer = null;
	private int mPacketsProcessed;
	private int mWRQPacketSize;
	private int mRRQPacketSize;

	/* Section of uninitialized Error Producers */
	private ErrorCodeFour mEPFour = null;
	private ErrorCodeFive mEPFive = null;
	private boolean mCanAddToQueue = true;
	private int mNumLostPackets = 0;
	private int mFrequencyLostPackets = 0;
	// private TransmissionError mTransmissionError = null;
	/* Lazy initialization for Error Producers */

	private boolean frequencySwitch(){
		if(mFrequencyLostPackets==0){
			mFrequencyLostPackets = this.mErrorSettings.getTransmissionErrorFrequency();
			return false;
		}
		return true;
	}
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
	public ErrorSimulatorService(DatagramPacket inDatagram, Callback cb, ErrorCommand errorSetting,
			InstanceType instance) {
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
		logger.print(Logger.VERBOSE,
				"Initalized error sim service on port " + this.mSendReceiveSocket.getLocalPort() + "\n");
		this.mPacketSendQueue = new LinkedList<>();
		this.mPacketsProcessed = 0;
		this.mWRQPacketSize = -1;
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
		// Initialize useful members
		boolean isTransfering = true;
		boolean errorSentToClient = false;
		boolean errorSendToServer = false;
		DatagramPacket receivedPacket = null;
		int packetLength = 0;

		// Facilitate the first WRQ/RRQ request and set server thread port
		this.mPacketSendQueue.addLast(this.mLastPacket);
		this.simulateError(this.mLastPacket); // Adds a packet into the work Q
		this.mLastPacket.setPort(this.mForwardPort); // Always 69 at this point,
														// changes later
		this.mLastPacket.setAddress(this.mServerHostAddress);
		try {
			this.forwardPacketToSocket(this.mPacketSendQueue.pop());
			
			receivedPacket = this.retrievePacketFromSocket();
			if (packetIsError(receivedPacket)) {
				isTransfering = false;
				receivedPacket.setAddress(this.mClientHostAddress);
				receivedPacket.setPort(this.mClientPort);
				this.mLastPacket = receivedPacket;
				this.mPacketSendQueue.addLast(this.mLastPacket);
			} else {
				this.mForwardPort = receivedPacket.getPort();
				this.mServerHostAddress = receivedPacket.getAddress();
				this.mLastPacket = receivedPacket;
				this.mPacketSendQueue.addLast(this.mLastPacket);
			}
		} catch (IOException e) {
			System.err.println("Sending the first RRQ and WRQ was an issue!");
		}
		
		mNumLostPackets = this.mErrorSettings.getTransmissionErrorOccurences();
		mFrequencyLostPackets = this.mErrorSettings.getTransmissionErrorFrequency();
		// Main while loop to facilitate transfer and create error
		// First packet will be from client
		while (isTransfering) {
			try {
				// The following function adds the packet into the work Q
				while(frequencySwitch()){
				isTransfering = continueHandlingPacket(receivedPacket);
				directPacketToDestination();
				forwardPacketToSocket(this.mPacketSendQueue.pop());
				receivedPacket = retrievePacketFromSocket();
				this.mPacketSendQueue.addLast(receivedPacket);
				}
	
			} catch (IOException e) {
				System.err.println("Something bad happened while transfering files");
			}
		}
		
		// End ACK based on request type.
		try {
			if (this.mInitialRequestType == RequestType.WRQ) {
				// Send the last ACK to client
				this.mLastPacket = this.mPacketSendQueue.pop();
				this.mLastPacket.setPort(this.mClientPort);
				this.mLastPacket.setAddress(this.mClientHostAddress);
				this.forwardPacketToSocket(this.mLastPacket);
			} else if (this.mInitialRequestType == RequestType.RRQ) {
				// Send the last ACK to server
				this.mLastPacket = this.mPacketSendQueue.pop();
				this.mLastPacket.setPort(this.mForwardPort);
				this.mLastPacket.setAddress(this.mServerHostAddress);
				logger.print(Logger.VERBOSE, "Preparing to send packet to server at port " + this.mForwardPort);
				this.forwardPacketToSocket(this.mLastPacket);
			}
		} catch (IOException e) {
			System.err.println("Something bad happened while transfering files");
		}
		if (errorSentToClient || errorSendToServer) {
			logger.print(Logger.ERROR, Strings.ES_TRANSFER_ERROR);
		} else {
			logger.print(Logger.VERBOSE, Strings.ES_TRANSFER_SUCCESS);
		}

		// Closing Logic
		this.mSendReceiveSocket.close();
		this.mCallback.callback(Thread.currentThread().getId());
	}

	/****This adds packets onto the work Q****/
	private boolean continueHandlingPacket(DatagramPacket inPacket) {
		this.mLastPacket = inPacket;
		if (inPacket.getPort() == this.mClientPort) {
			// From Client
			if (packetIsError(inPacket)) {
				inPacket.setAddress(this.mServerHostAddress);
				inPacket.setPort(this.mForwardPort);
				this.mPacketSendQueue.addFirst(inPacket); // May cause issue to be determined later
				logger.print(Logger.VERBOSE, String.format("Client sent a error packet, now forwarding it to the server!"));
				return false; 
			} 
			if (this.mMessUpThisTransfer == InstanceType.SERVER) {
				this.simulateError(inPacket); // Adds packet into Q
				++this.mPacketsProcessed;
			}
			if (this.mInitialRequestType == RequestType.RRQ) {
				logger.print(Logger.VERBOSE, String.format("An ack packet was received by the client, forwarding to server!"));
				return true; // This will be an ACK
			} else {
				logger.print(Logger.VERBOSE, String.format("Check WRQ to server if last block: %s.", 
						inPacket.getLength() == Configurations.MAX_MESSAGE_SIZE));
				return inPacket.getLength() == Configurations.MAX_MESSAGE_SIZE;
			}
		} else {
			// From Server
			if (packetIsError(inPacket)) {
				inPacket.setAddress(this.mClientHostAddress);
				inPacket.setPort(this.mClientPort);
				this.mPacketSendQueue.addFirst(inPacket); // May cause issue to be determined later
				logger.print(Logger.VERBOSE, String.format("Server sent a error packet, now forwarding it to the client!"));
				return false; 
			} 
			if (this.mMessUpThisTransfer == InstanceType.CLIENT) {
				this.simulateError(inPacket); // Adds packet into Q
				++this.mPacketsProcessed;
			}
			if (this.mInitialRequestType == RequestType.RRQ) {
				logger.print(Logger.VERBOSE, String.format("Check RRQ to cloient if last block: %s.", 
						inPacket.getLength() == Configurations.MAX_MESSAGE_SIZE));
				return inPacket.getLength() == Configurations.MAX_MESSAGE_SIZE;
			} else {
				logger.print(Logger.VERBOSE, String.format("An ack packet was reived by the server, forwarding it to client!"));
				return true; // This will be an ACK
			}
		}
	}

	/**
	 * This function is used to apply the rules of the simulator to make sure
	 * arbitrary packets get forwarded to the correct destination
	 */
	private void directPacketToDestination() {
		switch (this.mInitialRequestType) {
		case RRQ:
			if (this.mLastPacket.getData()[1] == 4) {
				// This is an ACK, an ACK always go to the server
				//logger.print(Logger.VERBOSE, "Preparing to send packet to server at port " + this.mForwardPort);
				this.mLastPacket.setPort(this.mForwardPort);
				this.mLastPacket.setAddress(this.mServerHostAddress);
			} else if (this.mLastPacket.getData()[1] == 3) {
				// This is a DATA packet, a DATA packet always goes to the
				// client
				//logger.print(Logger.VERBOSE, "Preparing to send packet to CLIENT at port " + this.mClientPort);
				this.mLastPacket.setPort(this.mClientPort);
				this.mLastPacket.setAddress(this.mClientHostAddress);
				this.mRRQPacketSize = this.mLastPacket.getLength();
			} else {
				// Possible Error Packet
				if (this.mLastPacket.getPort() == this.mServerListenPort) {
					// It is from the server, so we send it to the client
					//logger.print(Logger.VERBOSE, "Preparing to send packet to CLIENT at port " + this.mClientPort);
					this.mLastPacket.setPort(this.mClientPort);
					this.mLastPacket.setAddress(this.mClientHostAddress);
				} else {
					//logger.print(Logger.VERBOSE, "Preparing to send packet to server at port " + this.mForwardPort);
					// It is from the client, so we send it to the server
					this.mLastPacket.setPort(this.mForwardPort);
					this.mLastPacket.setAddress(this.mServerHostAddress);
				}
				logger.print(logger, "Unable to determine which entity to forward the packet on RRQ.");
			}
			break;
		case WRQ:
			if (this.mLastPacket.getData()[1] == 4) {
				// This is an ACK, an ACK always go to the client
				//logger.print(Logger.VERBOSE, "Preparing to send packet to CLIENT at port " + this.mClientPort);
				this.mLastPacket.setPort(this.mClientPort);
				this.mLastPacket.setAddress(this.mClientHostAddress);
			} else if (this.mLastPacket.getData()[1] == 3) {
				// This is a DATA, a DATA always go to the server
				//logger.print(Logger.VERBOSE, "Preparing to send packet to server at port " + this.mForwardPort);
				this.mLastPacket.setPort(this.mForwardPort);
				this.mLastPacket.setAddress(this.mServerHostAddress);
				this.mWRQPacketSize = this.mLastPacket.getLength();
			} else {
				// Possible Error Packet
				if (this.mLastPacket.getPort() == this.mServerListenPort) {
					// It is from the server, so we send it to the client
					//logger.print(Logger.VERBOSE, "Preparing to send packet to CLIENT at port " + this.mClientPort);
					this.mLastPacket.setPort(this.mClientPort);
					this.mLastPacket.setAddress(this.mClientHostAddress);
				} else {
					// It is from the client, so we send it to the server
					//logger.print(Logger.VERBOSE, "Preparing to send packet to server at port " + this.mForwardPort);
					this.mLastPacket.setPort(this.mForwardPort);
					this.mLastPacket.setAddress(this.mServerHostAddress);
				}
				logger.print(logger, "Unable to determine which entity to forward the packet on WRQ.");
			}

			break;
		default:
			logger.print(logger, "The packet forwarded was not a RRQ or WRQ.");
		}
	}

	/**
	 * Determines where or not corrupt a datagram packet
	 * 
	 * @param inPacket
	 *            - a packet to corrupt or not
	 */
	private void simulateError(DatagramPacket inPacket) {
		if (inPacket == null) {
			return;
		}

		ErrorType vErrType = this.mErrorSettings.getMainErrorFamily();
		int subOpt = this.mErrorSettings.getSubErrorFromFamily();
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
			if (this.mEPFour == null) {
				this.mEPFour = new ErrorCodeFour(inPacket, subOpt);
			} else {
				this.mEPFour.setReceivePacket(inPacket);
			}
			this.mLastPacket = mEPFour.errorPacketCreator();
			this.mPacketSendQueue.pop();
			this.mPacketSendQueue.addLast(this.mLastPacket);
			break;
		case UNKNOWN_TRANSFER:
			// Thread codeFiveThread = new Thread(new ErrorCodeFive(inPacket),
			// "Error Code 5 thread");
			// codeFiveThread.start();
			if (this.mEPFive == null) {
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
		case TRANSMISSION_ERROR:
			if (this.mErrorSettings.getTransmissionErrorType() != RequestType.NONE
					&& this.mErrorSettings.getTransmissionErrorType().getOptCode() != inPacket.getData()[1]) {
				logger.print(Logger.ERROR,
						String.format(
								"Not making delay error because the type we want is %s and header we compare is %s",
								this.mErrorSettings.getTransmissionErrorType() != RequestType.NONE,
								this.mErrorSettings.getTransmissionErrorType().getOptCode() != inPacket.getData()[1]));
				break;
			}
			
			// COMMENT: We will need to move this custom "skip" login inside
			// each case statement. They behave differently
			// if ((this.mPacketsProcessed %
			// (this.mErrorSettings.getTransmissionErrorFrequency() + 1)) != 0
			// || this.mErrorSettings.getTransmissionErrorOccurences() <
			// this.mPacketsProcessed) {
			// logger.print(Logger.ERROR,
			// String.format(
			// "Dont panic! We stoped making transmission errors. Heres why
			// frequency hop: %d and %d/%d occurence/processed",
			// this.mPacketsProcessed %
			// (this.mErrorSettings.getTransmissionErrorFrequency() + 1),
			// this.mErrorSettings.getTransmissionErrorOccurences(),
			// this.mPacketsProcessed / 2));
			// return;
			// }
			switch (this.mErrorSettings.getSubErrorFromFamily()) {
			case 1:
				// Lose a packet
				System.err.println("Losing packet");
				while(mNumLostPackets!=0 && !frequencySwitch()){
					mNumLostPackets--;
				}
				
				
				
				break;
			case 2:
				// We check this condition since this type packet.
				// mPacketsProcessed is always ahead of ErrorOccurrences by 1
				// only gets incremented one way -> messing with client or
				// server bound packets (set in ES)
				this.mLastPacket.setPort(this.mForwardPort);
				this.mLastPacket.setAddress(this.mServerHostAddress);
				if ((this.mPacketsProcessed) != this.mErrorSettings.getTransmissionErrorOccurences())
					return;
				logger.print(Logger.ERROR,
						String.format("Attempting to delay a packet with op code %d.", inPacket.getData()[1]));
				// Delay a packet
				TransmissionError transmissionError = new TransmissionError(this.mPacketSendQueue.pop(),
						this.mErrorSettings.getTransmissionErrorFrequency(), this);
				Thread delayPacketThread = new Thread(transmissionError);
				delayPacketThread.start();
				break;
			case 3:
				// Duplicate a packet
				break;
			default:
				System.err.println("WRONG Transmission suberror");
			}

			break;
		default:
			// Don't create an error
			break;
		}
	}

	private boolean packetIsError(DatagramPacket inPacket) {
		return inPacket.getData()[1] == 5;
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
		if(inUDPPacket.getPort() == this.mClientPort) {
			logger.print(Logger.VERBOSE, Strings.ES_RETRIEVE_PACKET_CLIENT);
		} else {
			logger.print(Logger.VERBOSE, Strings.ES_RETRIEVE_PACKET_SERVER);
		}
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
		if(receivePacket.getPort() == this.mClientPort) {
			logger.print(Logger.VERBOSE, Strings.ES_RETRIEVE_PACKET_CLIENT);
		} else {
			logger.print(Logger.VERBOSE, Strings.ES_RETRIEVE_PACKET_SERVER);
		}
		BufferPrinter.printBuffer(receivePacket.getData(), CLASS_TAG, logger);
		return receivePacket;
	}

	public void addWorkToFrontOfQueue(DatagramPacket inPacket) {
		logger.print(Logger.ERROR, "Inject delayed packet back into work queue!");
		this.mPacketSendQueue.addFirst(inPacket);
	}
	
}
// public void run() {
// DatagramPacket serverPacket = null;
// boolean transferNotFinished = true;
// boolean ackServerOnLastBlockByClient = false;
// boolean errorSentToClient = false;
// boolean errorSendToServer = false;
//
// // Add the first packet to the queue
// this.mPacketSendQueue.addFirst(this.mLastPacket);
// // Always forward the first packet to port 69
// this.mLastPacket.setPort(this.mForwardPort);
// this.mLastPacket.setAddress(this.mServerHostAddress);
// boolean hasTransmissionError = false; // This flag tells the program run
// // loop to skip a client or
// // server transaction
// while (transferNotFinished) {
// try {
//
// // Send off this that is directed to the server
// if (this.mMessUpThisTransfer == InstanceType.SERVER) {
// this.createSpecifiedError(this.mPacketSendQueue.peekFirst());
// ++this.mPacketsProcessed;
// }
// if ((this.mPacketSendQueue.peekFirst() != null
// && this.mPacketSendQueue.peekFirst().getPort() == this.mForwardPort) ||
// hasTransmissionError) {
// if (!hasTransmissionError) {
// // This "if" block is for sending to the server
// logger.print(Logger.VERBOSE, "Preparing to send packet to server at port
// " + this.mForwardPort);
// // Send the next packet in the work queue
// forwardPacketToSocket(this.mPacketSendQueue.pop());
//
// if (!transferNotFinished) {
// // Break here is for the last ACK packet from client
// // WRQ
// break;
// }
// }
// logger.print(Logger.VERBOSE, Strings.ES_RETRIEVE_PACKET_SERVER);
// serverPacket = retrievePacketFromSocket();
// this.mPacketSendQueue.addLast(serverPacket);
// // This following block, checks if we are on the last packet
// if (this.mInitialRequestType == RequestType.RRQ) {
// if (serverPacket.getLength() < Configurations.MAX_MESSAGE_SIZE) {
// // Coming into this block means that on a RRQ, the
// // last data block
// transferNotFinished = false;
// ackServerOnLastBlockByClient = true;
// }
// } else if (this.mInitialRequestType == RequestType.WRQ && mWRQPacketSize
// > 0) {
// // Must test if this was the first transfer
// // mWRQPacketSize = 0
// if (mWRQPacketSize < Configurations.MAX_MESSAGE_SIZE) {
// // We have finished the transfer
// logger.print(Logger.VERBOSE, Strings.ES_GOT_LAST_PACKET_WRQ);
// transferNotFinished = false;
// }
// }
//
// this.mLastPacket = serverPacket;
// // Set the mForwardPort to the Server's Thread Port
// this.mForwardPort = serverPacket.getPort();
// // Redirect the packet back to the client address
// directPacketToDestination();
// hasTransmissionError = false;
//
// } else {
// hasTransmissionError = true;
// }
//
//
// if (this.mMessUpThisTransfer == InstanceType.CLIENT) {
// this.createSpecifiedError(this.mPacketSendQueue.peekFirst());
// ++this.mPacketsProcessed;
// }
// if ((this.mPacketSendQueue.peekFirst() != null
// && this.mPacketSendQueue.peekFirst().getPort() == this.mClientPort) ||
// hasTransmissionError) {
//
// if (!hasTransmissionError) {
// logger.print(Logger.VERBOSE, Strings.ES_SEND_PACKET_CLIENT);
// logger.print(Logger.VERBOSE, "Preparing to send packet to CLIENT at port
// " + this.mClientPort);
// // Send the next packet in the work queue
// forwardPacketToSocket(this.mPacketSendQueue.pop());
//
// if (this.mLastPacket.getData()[1] == 5) {
// errorSentToClient = true;
// break;
// }
// }
//
// logger.print(Logger.VERBOSE, Strings.ES_RETRIEVE_PACKET_CLIENT);
// // Receiving from client
// this.mLastPacket = retrievePacketFromSocket();
// // At this point, the packet could pretty much be from ANYONE, client or
// server
// this.mPacketSendQueue.addLast(this.mLastPacket);
// // Set the write packet size in order to determine the end
// //mWRQPacketSize = this.mLastPacket.getLength();
// directPacketToDestination();
//
// if (ackServerOnLastBlockByClient) {
// // This extra process is needed on a read request to
// // send the last ACK to the server
// logger.print(Logger.VERBOSE, "Sending last ACK packet to server (RRQ) " +
// this.mClientPort);
// this.mLastPacket.setPort(this.mForwardPort);
// this.mLastPacket.setAddress(this.mServerHostAddress);
// forwardPacketToSocket(this.mLastPacket);
// }
// hasTransmissionError = false;
// } else {
// hasTransmissionError = true;
// }
//
// } catch (IOException e) {
// e.printStackTrace();
// }
// }
// if (errorSentToClient || errorSendToServer) {
// logger.print(Logger.ERROR, Strings.ES_TRANSFER_ERROR);
// } else {
// logger.print(Logger.VERBOSE, Strings.ES_TRANSFER_SUCCESS);
// }
//
// this.mSendReceiveSocket.close();
// this.mCallback.callback(Thread.currentThread().getId());
// }


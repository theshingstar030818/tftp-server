/**
 * 
 */
package testbed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import helpers.BufferPrinter;
import packet.Packet;
import packet.PacketBuilder;
import resource.Configurations;
import resource.Strings;
import server.Callback;
import testbed.errorcode.ErrorCodeFive;
import testbed.errorcode.ErrorCodeFour;
import testbed.errorcode.TransmissionConcurrentSend;
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
	private int mTransmissionRetries;

	/* Section of uninitialized Error Producers */
	private ErrorCodeFour mEPFour = null;
	private ErrorCodeFive mEPFive = null;
	private RequestType mPacketOpCode = null;
	private int mPacketBlock = 0;
	private boolean mLostPacketPerformed = false; // if already lost packet
	private boolean mDelayPacketPerformed = false; // has this been performed?
	private boolean mDuplicatePacketPerformed = false; // just need to happen
	private boolean mSkipInitSettings = false; // This is an edge case issue
	
	/* Special flags */
	private boolean END_THREAD = false;

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
				Strings.ERROR_SERVICE_PORT + this.mSendReceiveSocket.getLocalPort() + "\n");
		this.mPacketSendQueue = new LinkedList<>();
		mTransmissionRetries = 0;
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
		
		try {
			// Facilitate the first WRQ/RRQ request and set server thread port
			this.mPacketSendQueue.addLast(this.mLastPacket);
			if(this.mMessUpThisTransfer == InstanceType.SERVER)
				this.simulateError(this.mLastPacket); // only can mess with server packs on first go.
			if(this.mLastPacket == null || this.END_THREAD) {
				this.mSendReceiveSocket.close();
				logger.print(Logger.ERROR, Strings.ERROR_SERVICE_ENDING); 
				return;
			}
			if(!this.mSkipInitSettings) {
				this.mLastPacket.setPort(this.mForwardPort); // Always 69 at this point
				this.mLastPacket.setAddress(this.mServerHostAddress);
			}
			this.mSendReceiveSocket.setSoTimeout(0);
			while(this.mPacketSendQueue.size() == 0) {}
			this.forwardPacketToSocket(this.mPacketSendQueue.pop());
			receivedPacket = this.retrievePacketFromSocket();
			if (packetIsError(receivedPacket)) {
				isTransfering = false;
				receivedPacket.setAddress(this.mClientHostAddress);
				receivedPacket.setPort(this.mClientPort);
				this.mLastPacket = receivedPacket;
				this.mPacketSendQueue.addLast(this.mLastPacket);
			} else {
				if(!this.mSkipInitSettings) {
					this.mForwardPort = receivedPacket.getPort();
					this.mServerHostAddress = receivedPacket.getAddress();
				}
				this.mLastPacket = receivedPacket;
				this.mPacketSendQueue.addLast(this.mLastPacket);
			}

		} catch (IOException e) {
			System.err.println(Strings.ERROR_SERVICE_ERROR);
		}

		// Main while loop to facilitate transfer and create error
		// First packet will be from client
		while (isTransfering) {
			try {
				// The following function adds the packet into the work Q
				isTransfering = continueHandlingPacket(this.mPacketSendQueue.peek());
				if (this.mPacketSendQueue.size() > 0) {
					directPacketToDestination();
					forwardPacketToSocket(this.mPacketSendQueue.pop());
				}
				this.mLastPacket = retrievePacketFromSocket();
				this.mPacketSendQueue.addLast(this.mLastPacket);

			} catch (IOException e) {
				System.err.println(Strings.ERROR_SERVICE_ERROR_TRANS);
			}
		}
		// Possibly want to mess with the last packet
		continueHandlingPacket(this.mPacketSendQueue.peek());
		// End ACK based on request type.
		try {
			// Wait on last ACK in case of the last data was lost.

			System.out.println("Preparing to handle the last packet.");
			this.mTransmissionRetries = 0;
			this.mSendReceiveSocket.setSoTimeout(Configurations.TRANMISSION_TIMEOUT * 2);
			while(true) {
				try{	
					if (this.mInitialRequestType == RequestType.WRQ) {
						// Send the last ACK to client
						this.mLastPacket.setPort(this.mClientPort);
						this.mLastPacket.setAddress(this.mClientHostAddress);
						this.forwardPacketToSocket(this.mLastPacket);
					} else if (this.mInitialRequestType == RequestType.RRQ) {
						// Send the last ACK to server
						this.mLastPacket.setPort(this.mForwardPort);
						this.mLastPacket.setAddress(this.mServerHostAddress);
						this.forwardPacketToSocket(this.mLastPacket);
					}
					byte[] data = new byte[Configurations.MAX_BUFFER];
					DatagramPacket receivePacket = new DatagramPacket(data, data.length);
					this.mSendReceiveSocket.receive(receivePacket);
					this.mLastPacket = receivePacket;
					break;
				} catch (SocketTimeoutException e) {
					if(++this.mTransmissionRetries == Configurations.RETRANMISSION_TRY - 1) {
						logger.print(Logger.VERBOSE, String.format(Strings.RETRANSMISSION,
								this.mTransmissionRetries));
						break;
					}
				}
			}
			this.mSendReceiveSocket.setSoTimeout(0);
		} catch(NullPointerException e) {
			logger.print(Logger.VERBOSE, Strings.ERROR_SERVICE_SUCCESS);
		}catch (IOException e) {
		
			System.err.println(Strings.ERROR_SERVICE_ERROR_TRANS);
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

	/**
	 * This function will provide packet handling and error creation
	 * 
	 * @param inPacket
	 * @return true if we continue to listen for packets, false otherwise
	 */
	private boolean continueHandlingPacket(DatagramPacket inPacket) {
		if (inPacket == null)
			return true;
		if(inPacket.getData()[1] == 5) 
			return false;
		this.mLastPacket = inPacket;
		if (inPacket.getPort() == this.mClientPort) {
			// From Client
			if (packetIsError(inPacket)) {
				inPacket.setAddress(this.mServerHostAddress);
				inPacket.setPort(this.mForwardPort);
				this.mPacketSendQueue.addFirst(inPacket); // May cause issue to
															// be determined
															// later
				logger.print(Logger.VERBOSE,
						String.format(Strings.ERROR_SERVICE_FORWARD_CLI_ERR));
				return false;
			}
			if (this.mMessUpThisTransfer == InstanceType.SERVER) {
				this.simulateError(inPacket); // Adds packet into Q
			}
			if (this.mInitialRequestType == RequestType.RRQ) {
				logger.print(Logger.VERBOSE,
						String.format(Strings.ERROR_SERVICE_FORWARD_CLI_ACK));
				return true; // This will be an ACK
			} else {
				return inPacket.getLength() == Configurations.MAX_MESSAGE_SIZE;
			}
		} else {
			// From Server
			if (packetIsError(inPacket)) {
				inPacket.setAddress(this.mClientHostAddress);
				inPacket.setPort(this.mClientPort);
				this.mPacketSendQueue.addFirst(inPacket); // May cause issue to
															// be determined
															// later
				logger.print(Logger.VERBOSE,
						String.format(Strings.ERROR_SERVICE_FORWARD_SER_ERR));
				return false;
			}
			if (this.mMessUpThisTransfer == InstanceType.CLIENT) {
				this.simulateError(inPacket); // Adds packet into Q
			}
			if (this.mInitialRequestType == RequestType.RRQ) {
				return inPacket.getLength() == Configurations.MAX_MESSAGE_SIZE;
			} else {
				logger.print(Logger.VERBOSE,
						String.format(Strings.ERROR_SERVICE_FORWARD_SER_ACK));
				return true; // This will be an ACK
			}
		}
	}

	/**
	 * This function is used to apply the rules of the simulator to make sure
	 * arbitrary packets get forwarded to the correct destination
	 */
	private void directPacketToDestination() {
		if(this.mLastPacket.getPort() != this.mClientPort && this.mLastPacket.getPort() != this.mForwardPort) {
			// Only for an initial
			logger.print(Logger.ERROR, Strings.ERROR_SERVICE_ERR_CLI);
			this.mLastPacket.setPort(this.mClientPort);
			this.mLastPacket.setAddress(this.mClientHostAddress);
			ErrorCodeFive errCodeFive = new ErrorCodeFive(this.mLastPacket, true);
			Thread t = new Thread(errCodeFive, "Make-your-wish-come-true-foundation");
			t.start();
			this.mLastPacket = this.retrievePacketFromSocket();
			//if()
		}
		switch (this.mInitialRequestType) {
		case RRQ:
			if (this.mLastPacket.getData()[1] == 4) {
				// This is an ACK, an ACK always go to the server
				logger.print(Logger.VERBOSE, Strings.ERROR_SERVICE_ADD_SER + this.mForwardPort);
				this.mLastPacket.setPort(this.mForwardPort);
				this.mLastPacket.setAddress(this.mServerHostAddress);
			} else if (this.mLastPacket.getData()[1] == 3) {
				// This is a DATA packet, a DATA packet always goes to the
				// client
				logger.print(Logger.VERBOSE, Strings.ERROR_SERVICE_ADD_CLI + this.mClientPort);
				this.mLastPacket.setPort(this.mClientPort);
				this.mLastPacket.setAddress(this.mClientHostAddress);
			} else {
				// Possible Error Packet
				if (this.mLastPacket.getPort() == this.mForwardPort) {
					// It is from the server, so we send it to the client
					logger.print(Logger.VERBOSE, Strings.ERROR_SERVICE_ADD_CLI + this.mClientPort);
					this.mLastPacket.setPort(this.mClientPort);
					this.mLastPacket.setAddress(this.mClientHostAddress);
				} else {
					// It is from the client, so we send it to the server

					
					if(this.mLastPacket.getData()[1] == 1 || this.mLastPacket.getData()[1] == 2) {
						if(this.mMessUpThisTransfer == InstanceType.CLIENT) {
							logger.print(Logger.VERBOSE, "Tweaked the address to go to client: " + this.mClientPort);
							this.mLastPacket.setPort(this.mClientPort);
							this.mLastPacket.setAddress(this.mClientHostAddress);
						} else {
							logger.print(Logger.VERBOSE, "Tweaked the address to go to server: " + this.mForwardPort);
							this.mLastPacket.setPort(this.mForwardPort);
							this.mLastPacket.setAddress(this.mServerHostAddress);
						}
					} else {
						logger.print(Logger.VERBOSE, "Tweaked the address to go to server: " + this.mForwardPort);
						this.mLastPacket.setPort(this.mForwardPort);
						this.mLastPacket.setAddress(this.mServerHostAddress);
					}
				}
				logger.print(logger, Strings.ERROR_SERVICE_ADD_UNCLEAR_WRQ);
			}
			break;
		case WRQ:
			if (this.mLastPacket.getData()[1] == 4) {
				// This is an ACK, an ACK always go to the client
				logger.print(Logger.VERBOSE, Strings.ERROR_SERVICE_ADD_CLI + this.mClientPort);
				this.mLastPacket.setPort(this.mClientPort);
				this.mLastPacket.setAddress(this.mClientHostAddress);
			} else if (this.mLastPacket.getData()[1] == 3) {
				// This is a DATA, a DATA always go to the server
				logger.print(Logger.VERBOSE, Strings.ERROR_SERVICE_ADD_SER + this.mForwardPort);
				this.mLastPacket.setPort(this.mForwardPort);
				this.mLastPacket.setAddress(this.mServerHostAddress);
			} else {
				// Possible Error Packet
				if (this.mLastPacket.getPort() == this.mForwardPort) {
					// It is from the server, so we send it to the client
					logger.print(Logger.VERBOSE, Strings.ERROR_SERVICE_ADD_CLI + this.mClientPort);
					this.mLastPacket.setPort(this.mClientPort);
					this.mLastPacket.setAddress(this.mClientHostAddress);
				} else {
					// It is from the client, so we send it to the server
					if(this.mLastPacket.getData()[1] == 1 || this.mLastPacket.getData()[1] == 2) {
						if(this.mMessUpThisTransfer == InstanceType.CLIENT) {
							logger.print(Logger.VERBOSE, "Tweaked the address to go to client: " + this.mClientPort);
							this.mLastPacket.setPort(this.mClientPort);
							this.mLastPacket.setAddress(this.mClientHostAddress);
						} else {
							logger.print(Logger.VERBOSE, "Tweaked the address to go to server: " + this.mForwardPort);
							this.mLastPacket.setPort(this.mForwardPort);
							this.mLastPacket.setAddress(this.mServerHostAddress);
						}
					} else {
						logger.print(Logger.VERBOSE, "Tweaked the address to go to server: " + this.mForwardPort);
						this.mLastPacket.setPort(this.mForwardPort);
						this.mLastPacket.setAddress(this.mServerHostAddress);
					}
				}
				logger.print(logger, Strings.ERROR_SERVICE_ADD_UNCLEAR_RRQ);
			}

			break;
		default:
			logger.print(logger, Strings.ERROR_SERVICE_UNCLEAR);
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
			System.err.println("Simulate error called on null packet!");
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
		case ALLOCATION_EXCEEDED:
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
								Strings.ERROR_SERVICE_ERROR_TYPE,
								this.mErrorSettings.getTransmissionErrorType() != RequestType.NONE,
								this.mErrorSettings.getTransmissionErrorType().getOptCode() != inPacket.getData()[1]));
				break;
			}
			Packet mInPacket;
			switch (this.mErrorSettings.getSubErrorFromFamily()) {
			case 1:
				// Lose a packet
				//System.err.println("Testing to lose.");
				this.mPacketBlock = this.mErrorSettings.getTransmissionErrorOccurences();
				this.mPacketOpCode = this.mErrorSettings.getTransmissionErrorType();
				mInPacket = (new PacketBuilder()).constructPacket(mLastPacket);
				
				if (mInPacket.getBlockNumber() != this.mPacketBlock || mInPacket.getRequestType() != this.mPacketOpCode
						|| this.mLostPacketPerformed) {
					logger.print(this.logger, Strings.ERROR_SERVICE_NO_ERROR);
					return;
				}
				System.err.println(Strings.ERROR_SERVICE_LOST);
				this.mPacketSendQueue.pop();
				
				if (this.mPacketOpCode == RequestType.ERROR) {
					byte[] data = this.mLastPacket.getData();
					this.directPacketToDestination();
					data[1]+=10;
					this.mLastPacket.setData(data);
					try {
						
						forwardPacketToSocket(this.mLastPacket);
						this.mLastPacket = this.retrievePacketFromSocket();
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if (mInPacket.getBlockNumber() == -1 && (mInPacket.getRequestType() == this.mInitialRequestType)) {
					logger.print(Logger.VERBOSE, Strings.ERROR_SERVICE_UNSATISFICATION);
					this.END_THREAD = true;
					return;
				}
				this.mLostPacketPerformed = true;
				try {
					this.mTransmissionRetries = 0;
					this.mSendReceiveSocket.setSoTimeout(Configurations.TRANMISSION_TIMEOUT * 2);
					this.mLastPacket = this.retrievePacketFromSocket();
					this.mSendReceiveSocket.setSoTimeout(0);
					if(this.mLastPacket == null) {
						logger.print(Logger.ERROR, Strings.ERROR_SERVICE_SHUST_DOWN_THREAD);
						break;
					}
				} catch (SocketException e1) {
					e1.printStackTrace();
				}
				directPacketToDestination();
				try {
					forwardPacketToSocket(this.mLastPacket);
				} catch (IOException e) {
					System.err.println(Strings.ERROR_SERVICE_TIMEOUT_DELAY);
				}

				break;
			case 2:
				// We check this condition since this type packet.
				// mPacketsProcessed is always ahead of ErrorOccurrences by 1
				// only gets incremented one way -> messing with client or
				// server bound packets (set in ES)
				mInPacket = (new PacketBuilder()).constructPacket(mLastPacket);
				if (mInPacket.getBlockNumber() != this.mErrorSettings.getTransmissionErrorOccurences()
						|| mInPacket.getRequestType() != this.mErrorSettings.getTransmissionErrorType()
						|| this.mDelayPacketPerformed) {
					System.err.println(String.format("%d =? %d %d =? %d", mInPacket.getBlockNumber(),this.mPacketBlock, 
							mInPacket.getRequestType().getOptCode(), this.mErrorSettings.getTransmissionErrorType().getOptCode() ));
					return;
				}
				logger.print(Logger.ERROR,
						String.format(Strings.ERROR_SERVICE_DELAY_ATTEMP, inPacket.getData()[1]));
				
				// Delay a packet
				if (this.mPacketOpCode == RequestType.ERROR) {
					byte[] data = this.mLastPacket.getData();
					this.directPacketToDestination();
					data[1]+=10;
					this.mLastPacket.setData(data);
					try {
						forwardPacketToSocket(this.mLastPacket);
						this.mLastPacket = this.retrievePacketFromSocket();
						System.out.println(Strings.ERROR_SERVICE_DELAY);
						TransmissionError transmissionError = new TransmissionError(this.mPacketSendQueue.pop(),
								this.mErrorSettings.getTransmissionErrorFrequency(), this);
						Thread delayPacketThread = new Thread(transmissionError);
						delayPacketThread.start();
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if(mInPacket.getBlockNumber() == -1 && (mInPacket.getRequestType() == this.mInitialRequestType)) {
					TransmissionConcurrentSend transmissionError = new TransmissionConcurrentSend(this.mPacketSendQueue.pop(),
							this.mErrorSettings.getTransmissionErrorFrequency(), this, logger, this.mServerHostAddress, this.mClientHostAddress,
							this.mClientPort);
					Thread delayPacketThread = new Thread(transmissionError);
					delayPacketThread.start();

					System.out.println(Strings.ERROR_SERVICE_HANDLE_DELAY);
					
					this.mSkipInitSettings = true;

					// This packet has been provided us by a synchronized method addWorkToFrontOfQueue
					System.out.println(Strings.ERROR_SERVICE_FIRST_CONTACT);
					this.mLastPacket = this.retrievePacketFromSocket();
					System.out.println("We're on track!");
					// We are sure that this is the correct packet.
					this.mForwardPort = this.mLastPacket.getPort();
					this.mServerHostAddress = this.mLastPacket.getAddress();
					this.directPacketToDestination();
					this.mPacketSendQueue.addLast(this.mLastPacket);
				} else {
					TransmissionError transmissionError = new TransmissionError(this.mPacketSendQueue.pop(),
							this.mErrorSettings.getTransmissionErrorFrequency(), this);
					Thread delayPacketThread = new Thread(transmissionError);
					delayPacketThread.start();
					System.out.println("Delay has been started.");
					this.mLastPacket = this.retrievePacketFromSocket();
					directPacketToDestination();
					System.out.println("Forwarding timed out packet.");
					try {
						forwardPacketToSocket(this.mLastPacket);
					} catch (IOException e) {
						System.err.println("Error catch entity timeouts form both sides during a delay.");
					}
				
				}

				this.mDelayPacketPerformed = true;
				break;
			case 3:
				//System.err.println("Testing to duplicate.");
				mInPacket = (new PacketBuilder()).constructPacket(this.mLastPacket);
				if (mInPacket.getBlockNumber() != this.mErrorSettings.getTransmissionErrorOccurences()
						|| mInPacket.getRequestType() != this.mErrorSettings.getTransmissionErrorType()
						|| this.mDuplicatePacketPerformed)
					return;
				logger.print(Logger.ERROR,
						String.format("Attempting to duplicate a packet with op code %d.", inPacket.getData()[1]));
				System.out.println("Redirection happened before pop");

				if (this.mPacketOpCode == RequestType.ERROR) {
					byte[] data = this.mLastPacket.getData();
					this.directPacketToDestination();
					data[1]+=10;
					this.mLastPacket.setData(data);
					try {
						forwardPacketToSocket(this.mLastPacket);
						this.mLastPacket = this.retrievePacketFromSocket();
						this.directPacketToDestination();
						//DatagramPacket newPacket = new DatagramPacket(this.mLastPacket.getData(), this.mLastPacket.getLength(), 
						//		this.mLastPacket.getAddress(), this.mLastPacket.getPort());
						forwardPacketToSocket(this.mLastPacket);
						forwardPacketToSocket(this.mLastPacket);
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
			
					this.mLastPacket = this.mPacketSendQueue.pop();
					System.out.println("Redirection happening after pop");
					directPacketToDestination();
					
					DatagramPacket duplicatePacket = new DatagramPacket(this.mLastPacket.getData(),
							this.mLastPacket.getLength(), this.mLastPacket.getAddress(), this.mLastPacket.getPort());
					this.forwardPacketToSocket(duplicatePacket);
					// This one is the correct one we want to save. We will take
					// care of this one and add to Q
					this.mTransmissionRetries = 0;
					this.mSendReceiveSocket.setSoTimeout(Configurations.TRANMISSION_TIMEOUT);
					this.mLastPacket = this.retrievePacketFromSocket();
					this.mSendReceiveSocket.setSoTimeout(0);
					System.out.println("Got a reply from the host from a duplicate.");
					if(mInPacket.getBlockNumber() == -1 && (mInPacket.getRequestType() == this.mInitialRequestType)) {
						duplicatePacket.setPort(Configurations.SERVER_LISTEN_PORT);
						TransmissionConcurrentSend transmissionError = new TransmissionConcurrentSend(duplicatePacket,
								0, this, logger, this.mServerHostAddress, this.mClientHostAddress,
								this.mClientPort);
						Thread duplicatePacketThread = new Thread(transmissionError);
						duplicatePacketThread.start();
						this.mForwardPort = this.mLastPacket.getPort();
						this.mServerHostAddress = this.mLastPacket.getAddress();
						directPacketToDestination();
						this.mPacketSendQueue.addLast(this.mLastPacket);
						this.mSkipInitSettings = true;
					}else if(this.mLastPacket != null) {
						directPacketToDestination();
						this.mPacketSendQueue.addLast(this.mLastPacket);

						// This one is a duplicate Packet, we don't need to forward
						// this one over.
						this.forwardPacketToSocket(this.mLastPacket);
						// This one is the duplicated packet response, we will just
						// forget about this one lol
						this.mTransmissionRetries = 0;
						this.mSendReceiveSocket.setSoTimeout(Configurations.TRANMISSION_TIMEOUT);
						this.mLastPacket = this.retrievePacketFromSocket();
						this.mTransmissionRetries = 0;
						// Set our correct ref to the one first correct response.
						this.mLastPacket = this.mPacketSendQueue.peek();
					}
					this.mDuplicatePacketPerformed = true;
				} catch (IOException e) {
					e.printStackTrace();
				}

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

	/**
	 * A quick function to check if the current packet is an error
	 * 
	 * @param inPacket
	 *            - the packet to check
	 * @return true if it is an error, false otherwise
	 */
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
		if (inUDPPacket.getPort() == this.mClientPort) {
			logger.print(Logger.VERBOSE, Strings.ES_SEND_PACKET_CLIENT);
		} else {
			logger.print(Logger.VERBOSE, Strings.ES_SEND_PACKET_SERVER);
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
	private DatagramPacket retrievePacketFromSocket() {
		mBuffer = new byte[Configurations.MAX_BUFFER];
		DatagramPacket receivePacket = new DatagramPacket(mBuffer, mBuffer.length);
		while (true) {
			try {
				this.mSendReceiveSocket.receive(receivePacket);
				break;
			} catch (SocketTimeoutException e) {
				if (++this.mTransmissionRetries == Configurations.RETRANMISSION_TRY) {
					logger.print(Logger.ERROR, String.format(
							Strings.ERROR_SERVICE_RETRY, this.mTransmissionRetries));
					return null;
				}
				System.out.println("Time out caught.");
			} catch (IOException e) {
				logger.print(Logger.ERROR, "IOException during receive of packet");
			}
		}

		int realPacketSize = receivePacket.getLength();
		byte[] packetBuffer = new byte[realPacketSize];
		System.arraycopy(receivePacket.getData(), 0, packetBuffer, 0, realPacketSize);
		receivePacket.setData(packetBuffer);
		if (receivePacket.getPort() == this.mClientPort) {
			logger.print(Logger.VERBOSE, Strings.ES_RETRIEVE_PACKET_CLIENT);
		} else {
			logger.print(Logger.VERBOSE, Strings.ES_RETRIEVE_PACKET_SERVER);
		}
		BufferPrinter.printBuffer(receivePacket.getData(), CLASS_TAG, logger);
		return receivePacket;
	}

	/**
	 * This function is used to synchronize with the delay thread so that we use
	 * the same socket to send the packet
	 * 
	 * @param inPacket
	 *            - the packet to synchronize and send
	 */
	public void addWorkToFrontOfQueue(DatagramPacket inPacket) {
		logger.print(Logger.ERROR, "Inject delayed packet back into work queue!");
		this.mLastPacket = inPacket;
		directPacketToDestination();
		try {
			forwardPacketToSocket(this.mLastPacket);
		} catch (IOException e) {
			logger.print(Logger.ERROR,
					String.format(
							Strings.ERROR_SERVICE_TOO_LONG
									+ " Current time out is %dms and you choose to delay for %dms.",
							Configurations.TRANMISSION_TIMEOUT, this.mErrorSettings.getTransmissionErrorFrequency()));
		}
	}
	


}

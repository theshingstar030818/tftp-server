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
import resource.UIManager;
import types.Logger;
import types.RequestType;

/**
 * @author Team 3
 *
 *         This class serves as the intermediate host between our client server.
 *         The primary object of this class is to simulate UDP errors in order
 *         to test the soundness of our TFTP system
 */
public class ErrorSimulator {
	
	//by default set the log level to debug
	private static Logger logger = Logger.DEBUG;

	private final int MAX_BUFFER = 4096;
	private final String CLASS_TAG = "Error Simulator";

	private int mForwardPort;
	private final int RECEIVE_PORT;
	private final String INET_ADDRESS;
	private int mUserErrorOption;
	private int mUserErrorSubOption;

	private DatagramSocket mUDPListenSocket = null;
	private DatagramSocket mServerCommunicationSocket = null;
	private DatagramSocket mClientCommunicationSocket = null;
	private InetAddress mServerHostAddress = null;

	private byte[] mBuffer = null;
	
	private Scanner mScan;

	/**
	 * Main Error Simulator entry
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ErrorSimulator mMediatorHost = new ErrorSimulator(Configurations.ERROR_SIM_LISTEN_PORT,
				Configurations.SERVER_LISTEN_PORT, "localhost");
		mMediatorHost.initializeErrorSimulator();
	}

	/**
	 * @param recvPort
	 *            specifies the port that this host will received at
	 * @param fwdPort
	 *            specifies the port that this host will send towards
	 * @param host
	 *            specifies the host in which the host is located
	 */
	public ErrorSimulator(int recvPort, int fwdPort, String host) {
		this.mForwardPort = fwdPort;
		this.RECEIVE_PORT = recvPort;
		this.INET_ADDRESS = host;
		this.printMainMenu();
	}

	/**
	 * This public function will start up the error simulator server It will
	 * take care of initializing ports and start the main traffic mediation
	 * functionality
	 */
	public void initializeErrorSimulator() {
		try {			
			// Initialization tasks
			initiateInetAddress();
			initializeUDPSocket();
			
			// Start main functionality
			startTrafficMediation();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.mScan.close();
			this.mUDPListenSocket.close();
		}
	}
	
	private void printMainMenu() {
		int optionSelected = 0;
		this.mScan = new Scanner(System.in);
		boolean validInput = false;
		
		while(!validInput){
			printSelectLogLevelMenu();
			
			try {
				optionSelected = Keyboard.getInteger();
			} catch (NumberFormatException e) {
				optionSelected = 0;
			}
			
			switch (optionSelected) {
			case 1:
				logger = Logger.VERBOSE;
				getErrorCodeFromUser();
				validInput = true;
				break;
			case 2:
				logger = Logger.DEBUG;
				getErrorCodeFromUser();
				validInput = true;
				break;
			default:
				System.out.println(Strings.ERROR_INPUT);
				break;
			}					
		}
	}
	
	
	private void getErrorCodeFromUser() {
		int optionSelected = 0;
		boolean validInput = false;
		
		while(!validInput){
			System.out.println(UIManager.MENU_ERROR_SIMULATOR_ERROR_SELECTION);
			try {
				optionSelected = Keyboard.getInteger();
			} catch (NumberFormatException e) {
				optionSelected = 0;
			}
			
			switch (optionSelected) {
			case 1:
				// file not found
				logger.print(Logger.DEBUG,Strings.OPERATION_NOT_SUPPORTED);
				validInput = true;
				this.printMainMenu();
				break;
			case 2:
				// Access violation
				logger.print(Logger.DEBUG,Strings.OPERATION_NOT_SUPPORTED);
				validInput = true;
				this.printMainMenu();
				break;
			case 3:
				// Disk full or allocation exceeded
				logger.print(Logger.DEBUG,Strings.OPERATION_NOT_SUPPORTED);
				validInput = true;
				this.printMainMenu();
				break;
			case 4:
				// illegal TFTP operation option
				this.mUserErrorOption = 4;
				//printIllegalTFTPOperation();
				this.getSubOption(UIManager.MENU_ERROR_SIMULATOR_ILLEGAL_TFTP_OPERATION, 5);
				if (this.mUserErrorSubOption == 5) {
					// go back to the previous level
					this.mUserErrorSubOption = 0;
					validInput = false;
				}else{
					validInput = true;
				}
				break;
			case 5:
				// unknown transfer ID operation option
				this.mUserErrorOption = 5;
				this.mUserErrorSubOption = 0;
				validInput = true;
				break;
			case 6:
				// File already exists
				logger.print(Logger.DEBUG,Strings.OPERATION_NOT_SUPPORTED);
				validInput = true;
				this.printMainMenu();
				break;
			case 7:
				// No such user
				logger.print(Logger.DEBUG,Strings.OPERATION_NOT_SUPPORTED);
				validInput = true;
				this.printMainMenu();
				break;
			case 8:
				// No error
				System.out.println(Strings.EXIT_BYE);
				validInput = true;
				this.printMainMenu();
				break;
			case 9:
				// Go back to the previous menu
				this.printMainMenu();
				validInput = true;
				break;
			default:
				System.out.println(Strings.ERROR_INPUT);
				break;
			}
		}
	}

	/**
	 * This function will mediate traffic between the client and the server In
	 * coming packets from the client will be repackaged into a new
	 * DatagramPacket and sent to the server. In coming response packets from
	 * the server will be directly forwarded back to the client
	 * 
	 * @throws IOException
	 */
	private void startTrafficMediation() throws IOException {
		DatagramPacket serverPacket = null;
		DatagramPacket clientPacket = null;
		InetAddress clientAddress = null;
		InetAddress serverAddress = null; // Not exactly needed cause server host does not change
		int clientPort = 0;
		RequestType clientRequestType = RequestType.NONE;
		boolean receiveReads = true;
		
		while (true) {
			// Receiving packets from the client, remembering where the packets
			// came from
			logger.print(Logger.DEBUG, CLASS_TAG + " preparing to retrieve packet from client. ");
			clientPacket = retrievePacketFromSocket(this.mUDPListenSocket);
			clientAddress = clientPacket.getAddress();
			clientPort = clientPacket.getPort();
			logger.print(Logger.DEBUG, "... received on " + clientPort);

			// We redirect the packet to a new port
			RequestType passByHeader = RequestType.matchRequestByNumber((int) clientPacket.getData()[1]);
			if (passByHeader == RequestType.RRQ || passByHeader == RequestType.WRQ) {
				// This setting completes the case where all RRQ and WRQ's go to
				// the server
				// This means this is a new file transfer request.
				this.mForwardPort = Configurations.SERVER_LISTEN_PORT;
				clientRequestType = passByHeader;
				receiveReads = true;
			}
			DatagramPacket toServerPacket = new DatagramPacket(clientPacket.getData(), clientPacket.getLength(),
					serverAddress, this.mForwardPort);
			clientPacket.setPort(this.mForwardPort);
			logger.print(Logger.DEBUG, CLASS_TAG + " preparing to send packet to server at port " + this.mForwardPort);
			forwardPacketToSocket(toServerPacket, this.mServerCommunicationSocket);
			
			if (receiveReads) {
				// Waits for a response from the server
				logger.print(Logger.DEBUG, CLASS_TAG + " preparing to retrieve packet from server.");
				serverPacket = retrievePacketFromSocket(this.mServerCommunicationSocket);
		
				// We set this forward port so the client can contact the thread
				this.mForwardPort = serverPacket.getPort();
				serverAddress = serverPacket.getAddress();
				// Redirect the packet back to the client address
				DatagramPacket toClientPacket = new DatagramPacket(serverPacket.getData(), serverPacket.getLength(),
						clientAddress, clientPort);

				logger.print(Logger.DEBUG, CLASS_TAG + " preparing to send packet to client.");
				forwardPacketToSocket(toClientPacket, this.mClientCommunicationSocket);
			}

			if (clientRequestType == RequestType.RRQ) {
				if (serverPacket.getLength() < Configurations.MAX_MESSAGE_SIZE) {
					receiveReads = false;
				}
			}
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
	 * right after a send happens.
	 * This function will use the initialized DatagramSocket to send off the
	 * incoming packet and print the packet buffer to console
	 * 
	 * @param packet
	 *            represents the DatagramPacket that requires to be sent
	 * @throws IOException
	 */
	private void sendPacket(DatagramPacket packet) throws IOException {
		DatagramSocket mUDPSendSocket = new DatagramSocket();
		mUDPSendSocket.send(packet);
		BufferPrinter.printBuffer(packet.getData(), CLASS_TAG, logger);
		mUDPSendSocket.close();
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
	private DatagramPacket retrievePacketFromSocket(DatagramSocket socket) throws IOException {
		mBuffer = new byte[MAX_BUFFER];
		DatagramPacket receivePacket = new DatagramPacket(mBuffer, mBuffer.length);
		socket.receive(receivePacket);

		int realPacketSize = receivePacket.getLength();
		byte[] packetBuffer = new byte[realPacketSize];
		System.arraycopy(receivePacket.getData(), 0, packetBuffer, 0, realPacketSize);
		receivePacket.setData(packetBuffer);
		
		BufferPrinter.printBuffer(receivePacket.getData(), CLASS_TAG, logger);
		return receivePacket;
	}

	/**
	 * This function initializes the DatagramSocket that the client will use to
	 * send and receive messages
	 * 
	 * @param port
	 *            represents the port to bind and listen on
	 * @throws SocketException
	 */
	private void initializeUDPSocket() throws SocketException {
		this.mUDPListenSocket = new DatagramSocket(this.RECEIVE_PORT);
		this.mClientCommunicationSocket = new DatagramSocket();
		this.mServerCommunicationSocket = new DatagramSocket();
	}

	/**
	 * This function will initialize the InetAddress host for the DatagramPacket
	 * destination
	 * 
	 * @throws UnknownHostException
	 */
	private void initiateInetAddress() throws UnknownHostException {
		if (this.INET_ADDRESS == "localhost") {
			this.mServerHostAddress = InetAddress.getLocalHost();
		} else {
			this.mServerHostAddress = InetAddress.getByName(this.INET_ADDRESS);
		}
		
		logger.print(Logger.DEBUG, CLASS_TAG + " initalized destination to host: " + this.mServerHostAddress + "\n");
	}
	
	private static void printSelectLogLevelMenu() {
		System.out.println(UIManager.MENU_ERROR_SIMULATOR_LOG_LEVEL);
	}
	
	/**
	 * This function prints out error selections for client
	 */
	private void printErrorSelectMenu() {
		System.out.println(UIManager.MENU_ERROR_SIMULATOR_ERROR_SELECTION);
	}

	/**
	 * This function get user's sub-option for sub-error menu
	 * @param s - the string you want to prompt user
	 * @param max - the maximum valid input 
	 */
	private void getSubOption(String s, int max) {
		int subOpt;
		boolean validInput = false;
			
		while (!validInput) {
			// print out the message
			System.out.println(s);
			try {
				// get input
				subOpt = Keyboard.getInteger();
			} catch (NumberFormatException e) {
				subOpt = 0;
			}
			for(int i=1; i<=max; i++) {
				if(subOpt == i) {
					// validate the input
					validInput = true;
					this.mUserErrorSubOption = subOpt;
				}
			}
		}
	}
}

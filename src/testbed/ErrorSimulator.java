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

import helpers.BufferPrinter;
import resource.Configurations;
import types.RequestType;

/**
 * @author Team 3
 *
 * This class serves as the intermediate host between our client
 * server. The primary object of this class is to simulate UDP errors
 * in order to test the soundness of our TFTP system
 */
public class ErrorSimulator {

	private final int MAX_BUFFER = 4096;
	private final String CLASS_TAG = "Error Simulator";
	
	private int mForwardPort;
	private final int RECEIVE_PORT;
	private final String INET_ADDRESS;
	
	private DatagramSocket mUDPListenSocket = null;
	private DatagramSocket mUDPSendSocket = null;
	private InetAddress mInetAddress = null;
	
	private byte[] mBuffer = null;
	
	/**
	 * Main Error Simulator entry
	 * @param args
	 */
	public static void main(String[] args) {
		ErrorSimulator mMediatorHost = new ErrorSimulator(Configurations.ERROR_SIM_LISTEN_PORT,
				Configurations.SERVER_LISTEN_PORT, "localhost");
		mMediatorHost.initializeErrorSimulator();
	}
	
	/**
	 * @param recvPort specifies the port that this host will received at
	 * @param fwdPort specifies the port that this host will send towards
	 * @param host specifies the host in which the host is located
	 */
	public ErrorSimulator(int recvPort, int fwdPort, String host) {
		this.mForwardPort = fwdPort;
		this.RECEIVE_PORT = recvPort;
		this.INET_ADDRESS = host;
	}
	
	/**
	 * This public function will start up the error simulator server
	 * It will take care of initializing ports and start the main traffic mediation
	 * functionality
	 */
	public void initializeErrorSimulator() {
		try {
			// Initialization tasks
			initiateInetAddress();
			initializeUDPSocket(this.RECEIVE_PORT);
			
			
			// Start main functionality
			startTrafficMediation();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			this.mUDPListenSocket.close();
		}
	}
	
	/**
	 * This function will mediate traffic between the client and the server
	 * In coming packets from the client will be repackaged into a new DatagramPacket and
	 * sent to the server. 
	 * In coming response packets from the server will be directly forwarded back to the 
	 * client
	 * @throws IOException
	 */
	private void startTrafficMediation() throws IOException {
		DatagramPacket serverPacket = null;
		DatagramPacket clientPacket = null;
		InetAddress clientAddress = null;
		int clientPort = 0;
		RequestType clientRequestType = RequestType.NONE;
		boolean receiveReads = true;
		int serverThreadPort = 0;
		while(true) {
			// Receiving packets from the client, remembering where the packets came from
			System.out.println(CLASS_TAG + " preparing to retrieve packet from client. ");
			clientPacket = retrievePacketFromSocket(this.mUDPListenSocket);
			clientAddress = clientPacket.getAddress();
			clientPort = clientPacket.getPort();
			System.out.print("... received on " + clientPort);
			
			// We redirect the packet to a new port
			RequestType passByHeader = RequestType.matchRequestByNumber((int)clientPacket.getData()[1]);
			if(passByHeader == RequestType.RRQ || passByHeader == RequestType.WRQ) {
				// This setting completes the case where all RRQ and WRQ's go to the server
				// This means this is a new file transfer request.
				this.mForwardPort = Configurations.SERVER_LISTEN_PORT;
				clientRequestType = passByHeader;
				receiveReads = true;
			}
			DatagramPacket toServerPacket = new DatagramPacket(clientPacket.getData(),
					clientPacket.getLength(), InetAddress.getLocalHost(), this.mForwardPort);
			clientPacket.setPort(this.mForwardPort);
			System.out.println(CLASS_TAG + " preparing to send packet to server at port " + this.mForwardPort);
			forwardPacketToSocket(toServerPacket);
			
			
			if(receiveReads) {
				// Waits for a response from the server
				System.out.println(CLASS_TAG + " preparing to retrieve packet from server.");
				serverPacket = retrievePacketFromSocket(this.mUDPSendSocket);
				this.mUDPSendSocket.close();
				// We set this forward port so the client can contact the thread
				this.mForwardPort = serverPacket.getPort();
				// Redirect the packet back to the client address
				DatagramPacket toClientPacket = new DatagramPacket(serverPacket.getData(),
						serverPacket.getLength(), clientAddress,clientPort);
				
				System.out.println(CLASS_TAG + " preparing to send packet to client.");
				forwardPacketToSocket(toClientPacket);
				this.mUDPSendSocket.close();
			}

			if(clientRequestType == RequestType.RRQ) {
				if(serverPacket.getLength() < Configurations.MAX_MESSAGE_SIZE) {
					receiveReads = false;
				}
			}
		}
	}
	
	/**
	 * This function takes care of sending the packet to any address that is identifies by the
	 * DatagramPacket 
	 * 
	 * @param inUDPPacket describes a packet that requires to be sent 
	 * @throws IOException
	 */
	private void forwardPacketToSocket(DatagramPacket inUDPPacket) throws IOException {
		sendPacket(new DatagramPacket(inUDPPacket.getData(), inUDPPacket.getLength(), inUDPPacket.getAddress(), inUDPPacket.getPort()));
	}
	
	/**
	 * This function takes care of sending the packet to any address that is identifies by the
	 * DatagramPacket and uses the DatagramSocket parameter to send to the host
	 * 
	 * @param inUDPPacket describes a packet that requires to be sent 
	 * @param socket describes a socket that the packet is sent on
	 * @throws IOException
	 */
	private void forwardPacketToSocket(DatagramPacket inUDPPacket, DatagramSocket socket) throws IOException {
		sendPacket(new DatagramPacket(inUDPPacket.getData(), inUDPPacket.getLength(), inUDPPacket.getAddress(), inUDPPacket.getPort()),
				socket);
	}
	
	/**
	 * This function will use the initialized DatagramSocket to send off the incoming 
	 * packet and print the packet buffer to console. This method will not close the
	 * socket it is meant to send on
	 * 
	 * @param packet represents the DatagramPacket that requires to be sent
	 * @param sendSocket represents the DatagramSocket to use
	 * @throws IOException
	 */
	private void sendPacket(DatagramPacket packet, DatagramSocket sendSocket) throws IOException {
		sendSocket.send(packet);
		BufferPrinter.printBuffer(packet.getData(), CLASS_TAG);
	}
	
	/**
	 * This function will use the initialized DatagramSocket to send off the incoming 
	 * packet and print the packet buffer to console
	 * 
	 * @param packet represents the DatagramPacket that requires to be sent
	 * @throws IOException
	 */
	private void sendPacket(DatagramPacket packet) throws IOException {
		this.mUDPSendSocket = new DatagramSocket();
		this.mUDPSendSocket.send(packet);
		BufferPrinter.printBuffer(packet.getData(), CLASS_TAG);	
	}
	
	/**
	 * This function handles the retrieval of a response, sent back to the client.
	 * This function also trims the packet received from any unwanted trailing zeros.
	 * 
	 * @param socket to receive from
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
		
		BufferPrinter.printBuffer(receivePacket.getData(), CLASS_TAG);
		return receivePacket;
	}
	
	/**
	 * This function initializes the DatagramSocket that the client will use to send and
	 * receive messages
	 * 
	 * @param port represents the port to bind and listen on
	 * @throws SocketException
	 */
	private void initializeUDPSocket(int port) throws SocketException {
		this.mUDPListenSocket = new DatagramSocket(port);
	}
	
	/**
	 * This function will initialize the InetAddress host for the DatagramPacket destination
	 * 
	 * @throws UnknownHostException
	 */
	private void initiateInetAddress() throws UnknownHostException {
		if(this.INET_ADDRESS == "localhost") {
			this.mInetAddress = InetAddress.getLocalHost();
		} else {
			this.mInetAddress = InetAddress.getByName(this.INET_ADDRESS);
		}
		System.out.println(CLASS_TAG + " initalized destination to host: " + this.mInetAddress);
		System.out.println();
	}

}

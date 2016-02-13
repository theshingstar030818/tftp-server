package testbed.errorcode;

import java.io.IOException;
import java.net.*;

import helpers.BufferPrinter;
import packet.ErrorPacket;
import resource.Configurations;
import types.Logger;
import types.RequestType;

/**
 * @author Team 3
 *
 *         Error code five consists of creating a copy of the datagram packet to
 *         be sent then opening up a new port to send it off to its destination.
 *         This class be created as a thread or also used in a single thread
 *         envrionment
 */
public class ErrorCodeFive implements Runnable {
	public int packetCount = 0;
	private DatagramPacket mSendPacket;
	private DatagramSocket errorSocket;
	private InetAddress clientAddress;

	public ErrorCodeFive(DatagramPacket sendPacket) {
		this.mSendPacket = new DatagramPacket(sendPacket.getData(), sendPacket.getLength(), sendPacket.getAddress(),
				sendPacket.getPort());
		clientAddress = sendPacket.getAddress();
	}

	/**
	 * Check to see if we need to create this every 3rd packet ISSUE: this does
	 * not work well on serving multiple clients on one machine at the same time
	 * 
	 * @return true or false
	 */
	private boolean checkToCreateErrorSocket() {
		if (this.mSendPacket.getAddress() == clientAddress && packetCount >= 3) {
			return true;
		} else {
			System.out.println("The recieved packet is not sent through an error socket \n");
		}
		return false;
	}

	/**
	 * Initializes the Datagram Socket
	 */
	private void createErrorSocket() {
		this.errorSocket = null;
		try {
			this.errorSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		packetCount++;
		if (checkToCreateErrorSocket()) {
			createErrorSocket();
			// Setting this time out so when it does time out, then we can
			// simply shut the thread down

			try {
				System.err.println("Creating an error packet for unknown host to sent to server.");
				// this.errorSocket.setSoTimeout(1000);
				this.errorSocket.send(this.mSendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// receive error message from server
			byte data[] = new byte[Configurations.MAX_BUFFER];
			DatagramPacket receivedPacket = new DatagramPacket(data, data.length);
			try {
				this.errorSocket.receive(receivedPacket);
				ErrorPacket errorPacket = new ErrorPacket(receivedPacket);
				BufferPrinter.printPacket(errorPacket, Logger.VERBOSE, RequestType.ERROR);
				System.err.println("Uknown host error packet received from server \n");
			} catch (SocketTimeoutException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.errorSocket.close();
			packetCount = 0;
		}

	}

}

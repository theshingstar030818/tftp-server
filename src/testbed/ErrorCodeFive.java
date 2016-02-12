package testbed;

import java.io.IOException;
import java.net.*;

import helpers.BufferPrinter;
import packet.ErrorPacketBuilder;
import resource.Configurations;
import types.Logger;
import types.RequestType;

public class ErrorCodeFive implements Runnable {
	private static int packetCount = 0;
	private DatagramPacket mSendPacket;
	private DatagramSocket errorSocket;
	private InetAddress clientAddress;

	public ErrorCodeFive(DatagramPacket sendPacket) {
		this.mSendPacket = new DatagramPacket(sendPacket.getData(), sendPacket.getLength(), sendPacket.getAddress(), sendPacket.getPort());
		ErrorCodeFive.packetCount++;
		clientAddress = sendPacket.getAddress();
	}

	private boolean checkToCreateErrorSocket() {
		if (this.mSendPacket.getAddress() == clientAddress && ErrorCodeFive.packetCount >= 3) {
			return true;
		}
		else{
			System.out.println("The recieved packet is not sent through an error socket \n");
		}
		return false;
	}

	private void createErrorSocket() {
		this.errorSocket = null;
		try {
			this.errorSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void run() {
		if (checkToCreateErrorSocket()) {
			createErrorSocket();
			// Setting this time out so when it does time out, then we can simply shut the thread down
			
			try {
				System.err.println("Creating an error packet for unknown host to sent to server.");
				//newSocket.setSoTimeout(10000);
				this.errorSocket.send(this.mSendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// receive error message from server
			byte data[] = new byte[Configurations.MAX_BUFFER];
			DatagramPacket receivedPacket = new DatagramPacket(data, data.length);
			try {
				this.errorSocket.receive(receivedPacket);
				ErrorPacketBuilder errorPacket = new ErrorPacketBuilder(receivedPacket);
				BufferPrinter.printPacket(errorPacket, Logger.VERBOSE, RequestType.ERROR);
				System.err.println("Uknown host error packet received from server \n");
			} catch(SocketTimeoutException e) { 
			} catch (IOException e) {
				e.printStackTrace();
			}
			ErrorCodeFive.packetCount = 0;
		}

	}

}

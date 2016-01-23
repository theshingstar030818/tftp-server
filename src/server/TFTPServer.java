/**
 * 
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


class Console implements Runnable {
	Console() {}

	public void run() {
		DatagramSocket commandSock = null;
		try {
			commandSock = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("Unable to create command console socket.");
			e.printStackTrace();
			System.exit(1);
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String s = "";
		try {
			s = br.readLine();
		} catch (IOException e) {
			System.out.println("Failed to read line on command console.");
			e.printStackTrace();
		}
		
		InetAddress addr = null;
		
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			System.out.println("Could not get localhost.");
			e1.printStackTrace();
		}
		
		DatagramPacket commandPacket = new DatagramPacket(s.getBytes(), s.getBytes().length, addr, 5000);
		try {
			commandSock.send(commandPacket);
		} catch (IOException e) {
			System.out.println("Unable to send packet from command console.");
			e.printStackTrace();
		}	
		
	}
}


/**
 * @author Team 3
 *
 */
public class TFTPServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		// Create the socket.
		DatagramSocket serverSock = null;
		try {
			serverSock = new DatagramSocket(5000);
		} catch (SocketException e) {
			System.out.println("Failed to make main socket.");
			e.printStackTrace();
			System.exit(1);
		}
		
		Thread console = new Thread(new Console(), "command console");
		console.start();
		
		// Create the packet.
		byte[] buffer = new byte[1024]; // Temporary. Will be replaced with exact value soon.
		DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
		
		while (true) {
			try {
				serverSock.receive(receivePacket);
			} catch (IOException e) {
				System.out.println("Failed to receive packet on main thread.");
				e.printStackTrace();
			}
			
			if (receivePacket.getAddress().equals(addr)) {
				break;
			}
			
			Thread service = new Thread(new TFTPService(receivePacket), "Service");
			service.start();
		}
		
		serverSock.close();

	}

}

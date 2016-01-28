package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Console implements Runnable {
	Console() {}

	public void run() {

		try {
			 (new BufferedReader(new InputStreamReader(System.in))).readLine();
		} catch (IOException e) {
			System.out.println("Failed to read line on command console.");
			e.printStackTrace();
		}
		
		TFTPServer.active.set(false);
	}
}



public class TFTPServer {
	
	// main function that starts the server.
	public static void main(String[] args) {
		TFTPServer listener = new TFTPServer();
		listener.start();
	}
	
	static AtomicBoolean active = new AtomicBoolean(true);
	Vector<Thread> threads;
	
	final Lock lock = new ReentrantLock();
	final Condition notEmpty = lock.newCondition();

	public TFTPServer() {
		threads = new Vector<Thread>();
	}
	
public void start() {
		
		// Create the socket.
		DatagramSocket serverSock = null;
		try {
			serverSock = new DatagramSocket(5000);
			serverSock.setSoTimeout(30);
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
		
		while (active.get()) {
			try {
				serverSock.receive(receivePacket);
			} catch (SocketTimeoutException e) {
				continue;
			} catch (IOException e) {
				System.out.println("Failed to receive packet on main thread.");
				e.printStackTrace();
			}
			// You are calling this in main(), you can't pass public static void main into a class
			Thread service = new Thread(new TFTPService(receivePacket, this), "Service");
			threads.addElement(service);
			service.start();
		}
		
		serverSock.close();
		
		while (!threads.isEmpty()) {
			try {
				notEmpty.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public void callback(long id) {
		for (Thread t : threads) {
			if (t.getId() == id) {
				threads.remove(t);
				notEmpty.signal();
				break;
			}
		}
	}
}

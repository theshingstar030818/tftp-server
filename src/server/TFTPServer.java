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

import resource.Configurations;
import resource.Strings;


/**
 * The Console class will allow someone (presumably an admin) to manage the server
 * from a local machine. Currently its only functionality is to close the server,
 * but this can be expanded later.
 */
class Console implements Runnable {

	public void run() {
		try {
			 (new BufferedReader(new InputStreamReader(System.in))).readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		TFTPServer.active.set(false);
	}
}


public class TFTPServer implements Callback {
	
	/**
	 * Main function that starts the server.
	 */
	public static void main(String[] args) {
		TFTPServer listener = new TFTPServer();
		listener.start();
	}
	
	// Some class attributes.
	static AtomicBoolean active = new AtomicBoolean(true);
	Vector<Thread> threads;
	
	final Lock lock = new ReentrantLock();
	final Condition notEmpty = lock.newCondition();

	
	/**
	 * Constructor for TFTPServer that initializes the thread container 'threads'.
	 */
	public TFTPServer() {
		threads = new Vector<Thread>();
	}
	
	
	/**
	 * Handles operation of the server.
	 */
	public void start() {
		
		// Create the socket.
		DatagramSocket serverSock = null;
		try {
			serverSock = new DatagramSocket(Configurations.SERVER_LISTEN_PORT);
			serverSock.setSoTimeout(30);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// Create and start a thread for the command console.
		Thread console = new Thread(new Console(), "command console");
		console.start();
		
		// Create the packet for receiving.
		byte[] buffer = new byte[1024]; // Temporary. Will be replaced with exact value soon.
		DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
		
		/*
		 * - Receive packets until the admin console gives the shutdown signal.
		 * - Since receiving a packet is a blocking operation, timeouts have been set to loop back
		 *   and check if the signal to close has been given.
		 */
		while (active.get()) {
			try {
				serverSock.receive(receivePacket);
			} catch (SocketTimeoutException e) {
				continue;
			} catch (IOException e) {
				System.out.println(Strings.SERVER_RECEIVE_ERROR);
				e.printStackTrace();
			}
			// You are calling this in main(), you can't pass public static void main into a class
			Thread service = new Thread(new TFTPService(receivePacket, this), "Service");
			threads.addElement(service);
			service.start();
		}
		
		serverSock.close();
		
		/*
		 * Wait for all service threads to close before completely exiting.
		 */
		while (!threads.isEmpty()) {
			try {
				notEmpty.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public synchronized void callback(long id) {
		for (Thread t : threads)
			if (t.getId() == id) {
				threads.remove(t);
				notEmpty.signal();
				break;
			}
	}
}

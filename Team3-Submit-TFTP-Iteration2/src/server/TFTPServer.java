package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import resource.Configurations;
import resource.Strings;
import types.Logger;
import helpers.BufferPrinter;
import helpers.Keyboard;

/**
 * The Console class will allow someone (presumably an admin) to manage the
 * server from a local machine. Currently its only functionality is to close the
 * server, but this can be expanded later.
 */
class Console implements Runnable {

	private TFTPServer mMonitorServer;

	public Console(TFTPServer monitorServer) {
		this.mMonitorServer = monitorServer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		String quitCommand = Keyboard.getString();
		while (!quitCommand.equalsIgnoreCase("q")) {
			quitCommand = Keyboard.getString();
		}
		System.out.println(Strings.EXITING);
		TFTPServer.active.set(false);
		this.mMonitorServer.interruptSocketAndShutdown();
	}
}

/**
 * @author Team 3
 *
 * The server main thread will listen on port 69 for incoming requests
 * then spawn a handler thread to handle file upload
 */
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
	private Vector<Thread> threads;
	private DatagramSocket serverSock = null;
	private Logger logger = Logger.VERBOSE;
	private String CLASS_TAG = "<TFTP Server>";

	/**
	 * Constructor for TFTPServer that initializes the thread container
	 * 'threads'.
	 */
	public TFTPServer() {
		threads = new Vector<Thread>();
		logger.setClassTag(CLASS_TAG);
	}

	/**
	 * Handles operation of the server.
	 */
	public void start() {
		DatagramPacket receivePacket = null;
		try {
			serverSock = new DatagramSocket(Configurations.SERVER_LISTEN_PORT);
			System.out.println("Server initiated on port " + Configurations.SERVER_LISTEN_PORT);
			serverSock.setSoTimeout(30000);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Create and start a thread for the command console.
		Thread console = new Thread(new Console(this), "command console");
		console.start();

		/*
		 * - Receive packets until the admin console gives the shutdown signal.
		 * - Since receiving a packet is a blocking operation, timeouts have
		 * been set to loop back and check if the close signal has been given.
		 */
		while (active.get()) {
			try {
				// Create the packet for receiving.
				byte[] buffer = new byte[Configurations.MAX_BUFFER];
				receivePacket = new DatagramPacket(buffer, buffer.length);
				serverSock.receive(receivePacket);
				System.out.println("Received packet from server (right socket)");
			} catch (SocketTimeoutException e) {
				continue;
			} catch (SocketException e) {
				continue;
			} catch (IOException e) {
				System.out.println(Strings.SERVER_RECEIVE_ERROR);
				e.printStackTrace();
			}
			System.out.println(BufferPrinter.acceptConnectionMessage(Strings.SERVER_ACCEPT_CONNECTION,
					receivePacket.getSocketAddress().toString()));
			Thread service = new Thread(new TFTPService(receivePacket, this), "Service");
			threads.addElement(service);
			service.start();
		}
		this.serverSock.close();
		// Wait for all service threads to close before completely exiting.
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This interrupt will stop the socket from receiving.
	 */
	public void interruptSocketAndShutdown() {
		this.serverSock.close();
	}

	/**
	 * - Check if the server is still active. If it is not then the join
	 * operation is running, making this unnecessary. - Loop through all the
	 * threads, checking if that thread's ID is the ID of the thread that just
	 * ended. - If it is, remove it from the vector of threads and break out of
	 * the loop.
	 */
	public synchronized void callback(long id) {
		if (active.get()) {
			for (Thread t : threads) {
				if (t.getId() == id) {
					threads.remove(t);
					break;
				}
			}
		}
	}
}

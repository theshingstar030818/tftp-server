package testbed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import resource.Configurations;
import resource.Strings;
import resource.Tuple;
import server.Callback;
import types.ErrorType;
import types.InstanceType;
import types.Logger;
import helpers.BufferPrinter;

/**
 * @author Team 3
 *
 * The error server hosted at port 68 that listens to client traffic.
 * When traffic is received, the server will generate another thread to 
 * handle the error simulation.
 */
public class ErrorSimulatorServer implements Callback {
	
	/**
	 * Main function that starts the server.
	 */
	public static void main(String[] args) {
		ErrorSimulatorServer listener = new ErrorSimulatorServer();
		listener.start();
	}
	
	private Logger logger = Logger.VERBOSE;
	private TFTPUserInterface mErrorUI;
	private Tuple<ErrorType, Integer> mErrorOptionSettings;
	private final String CLASS_TAG = "<Error Simulator Server>";
	private InstanceType testInstance; 
	
	public static AtomicBoolean active = new AtomicBoolean(true);
	Vector<Thread> threads;
	
	DatagramSocket errorSimulatorSock = null;
	
	/**
	 * Constructor for ErrorSimulatorServer that initializes the thread container 'threads'.
	 */
	public ErrorSimulatorServer() {
		threads = new Vector<Thread>();
		this.mErrorUI = new TFTPUserInterface();
		testInstance = this.mErrorUI.printTestableProcess();
		logger.setClassTag(CLASS_TAG);
	}
	
	/**
	 * Handles operation of the error simulator server.
	 */
	public void start() {
		
		DatagramPacket receivePacket = null;
		try {
			errorSimulatorSock = new DatagramSocket(Configurations.ERROR_SIM_LISTEN_PORT);
			//errorSimulatorSock.setSoTimeout(30000);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		/*
		 * - Receive packets until the admin console gives the shutdown signal.
		 * - Since receiving a packet is a blocking operation, timeouts have been set to loop back
		 *   and check if the close signal has been given.
		 */
		while (active.get()) {
			try {
				// Create the packet for receiving.
				byte[] buffer = new byte[Configurations.MAX_BUFFER]; 
				this.mErrorOptionSettings = this.mErrorUI.getErrorCodeFromUser(testInstance);
				if(this.mErrorOptionSettings.first == ErrorType.EXIT) {
					active.set(false);
					break;
				}
				receivePacket = new DatagramPacket(buffer, buffer.length);
				logger.print(Logger.VERBOSE, String.format(Strings.ES_INITIALIZED, Configurations.ERROR_SIM_LISTEN_PORT));
				logger.print(Logger.VERBOSE, Strings.ES_START_LISTENING);
				
				errorSimulatorSock.receive(receivePacket);
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
			
			Thread service = new Thread(new ErrorSimulatorService(receivePacket, this, this.mErrorOptionSettings, testInstance), CLASS_TAG);
			threads.addElement(service);
			service.start();
		}
		this.errorSimulatorSock.close();
		// Wait for all service threads to close before completely exiting.
		for(Thread t : threads) {
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
		this.errorSimulatorSock.close();
	}
	
	/**
	 * - Check if the error simulator is still active. If it is not then the join operation is running, making this unnecessary.
	 * - Loop through all the threads, checking if that thread's ID is the ID of the thread that just ended.
	 * - If it is, remove it from the vector of threads and break out of the loop.
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


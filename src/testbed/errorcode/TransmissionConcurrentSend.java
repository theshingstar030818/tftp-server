package testbed.errorcode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import helpers.BufferPrinter;
import resource.Configurations;
import testbed.ErrorSimulatorService;
import types.Logger;
/**
 * @author Team 3
 *
 *	The idea behind this class is to have this thread freeze for a given
 *	amount of time before synchronizing with the service class and adding the 
 *	packet back into the work queue
 */	
public class TransmissionConcurrentSend extends TransmissionError implements Runnable {

		protected InetAddress mServerHostAddress;
		protected InetAddress mClientHostAddress;
		protected int mClientPort;
		protected Logger logger;
		private final String CLASS_TAG = "<Concurrent Send Thread>";
		
		
		/**
		 * This class constructor will set up a custom concurrent send operation 
		 * 
		 * @param inPacket
		 * @param ms
		 * @param monitor
		 * @param logger
		 * @param sAd
		 * @param cAd
		 * @param cPort
		 */
		public TransmissionConcurrentSend(DatagramPacket inPacket, int ms, ErrorSimulatorService monitor,
				Logger logger, InetAddress sAd, InetAddress cAd, int cPort) {
			super(inPacket, ms, monitor);
			this.mServerHostAddress = sAd;
			this.mClientPort = cPort;
			this.mClientHostAddress = cAd;
			this.logger = logger;
		}

		/* (non-Javadoc)
		 * @see testbed.errorcode.TransmissionError#run()
		 */
		@Override
		public void run() {
			try {
				System.err.println(String.format("Delaying a packet for %d ms", (long)this.mFrozenMillis));
				Thread.sleep((long)this.mFrozenMillis);
				sendConcurrentPacket(this.mPacket);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Handles one concurrent send activity which will generate an error code 5, unknown
		 * transfer ID if the transfer has been started between the calling class
		 * 
		 * @param inPacket
		 */
		public void sendConcurrentPacket(DatagramPacket inPacket) {
			logger.print(Logger.ERROR, "Preparing to handle delayed first packet.");
		
			inPacket.setPort(Configurations.SERVER_LISTEN_PORT);
			inPacket.setAddress(this.mServerHostAddress);
			try {
				
				if(inPacket.getData()[1] != 1 || inPacket.getData()[1] != 2) {
					System.out.println("Bro... you got the wrong packet!");
				}
				BufferPrinter.printBuffer(inPacket.getData(), CLASS_TAG, logger);
				// First send off the delayed packet to server
				DatagramSocket tempSocket = new DatagramSocket();
				tempSocket.send(inPacket);
				byte[] buf = new byte[Configurations.MAX_MESSAGE_SIZE];
				DatagramPacket tempPacket = new DatagramPacket(buf, buf.length);
				
				// Get the server's response for the delayed packet (it created a new thread)
				tempSocket.receive(tempPacket);
				int serverPort = tempPacket.getPort();

				// Forward this to client to generate a Unknown Host
				tempPacket.setPort(this.mClientPort);
				tempPacket.setAddress(this.mClientHostAddress);
				tempSocket.send(tempPacket);
				
				
				buf = new byte[Configurations.MAX_MESSAGE_SIZE];
				tempPacket = new DatagramPacket(buf, buf.length);
				// Get the unknown host msg from client
				tempSocket.receive(tempPacket);
				
				//Shut down server thread by fwding the message to it
				tempPacket.setPort(serverPort);
				tempPacket.setAddress(this.mServerHostAddress);
				if(tempPacket.getData()[1] != 5) {
					System.out.println("You may want to check out whats happened to that first delay you made bro...");
				}
				BufferPrinter.printBuffer(tempPacket.getData(), CLASS_TAG, logger);
				tempSocket.send(tempPacket);
				tempSocket.close();
				System.out.println("Just released the simulation of delayed initiating request and relayed server response back to client.");
			} catch (IOException e) {
				logger.print(Logger.ERROR,
						String.format(
								"Oops, you might have set your delay time for too long."
										+ " Current time out is %dms and you choose to delay for %dms.",
								Configurations.TRANMISSION_TIMEOUT));
			}
		}

	}


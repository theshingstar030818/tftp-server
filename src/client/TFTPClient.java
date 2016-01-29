/**
 * 
 */
package client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

import helpers.FileStorageService;
import packet.*;
import resource.*;
import types.*;

/**
 * @author Team 3
 *
 */
public class TFTPClient {

	private static DatagramSocket sendReceiveSocket;
	private static boolean isClientAlive = true;

	public static void main(String[] args) {

		try {
			sendReceiveSocket = new DatagramSocket();

			while (isClientAlive) {
				printSelectMenu();

				Scanner scan = new Scanner(System.in);
				int optionSelected = scan.nextInt();

				switch (optionSelected) {
				case 1:
					// Read file
					System.out.println("Please enter file name : ");
					scan.nextLine();
					String readFileName = scan.nextLine();
					try {
						readRequestHandler(readFileName);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case 2:
					// Write file
					System.out.println("Please enter file name or file path : ");
					scan.nextLine();
					String writeFileNameOrFilePath = scan.nextLine();
					writeRequestHandler(writeFileNameOrFilePath);
					break;
				case 3:
					// shutdown client
					isClientAlive = false;
					break;

				default:
					System.out.println("ERROR : Please select a valid option.");
					break;
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function create the write request for a client and transfers the
	 * file from local disc to the server in 512 Byte blocks
	 * 
	 * @param writeFileName
	 *            - the name of the file that the client requests to send to
	 *            server
	 */
	private static void writeRequestHandler(String writeFileNameOrFilePath) {

		ReadWritePacketPacketBuilder wpb;
		FileStorageService writeRequestFileStorageService;
		DataPacketBuilder dataPacket; 
		DatagramPacket lastPacket;
		byte[] fileData;
		byte[] ackBuff = new byte[Configurations.LEN_ACK_PACKET_BUFFET];
		boolean moreData = true;

		try {
			writeRequestFileStorageService = new FileStorageService(writeFileNameOrFilePath,InstanceType.CLIENT);
			wpb = new WritePacketBuilder(InetAddress.getLocalHost(), Configurations.ERROR_SIM_LISTEN_PORT,
					writeFileNameOrFilePath, Configurations.DEFAULT_RW_MODE);
			lastPacket = wpb.buildPacket();
			sendReceiveSocket.send(lastPacket);
			
			lastPacket = new DatagramPacket(ackBuff, ackBuff.length);
			
			// This packet has the block number to start on!
			sendReceiveSocket.receive(lastPacket);
			
			while (moreData) {
				fileData = new byte[Configurations.MAX_BUFFER];
				// get the first block of file to transfer
				moreData = writeRequestFileStorageService.getFileByteBufferFromDisk(fileData);
				
				// Initialize DataPacket with block number n
				dataPacket = new DataPacketBuilder(lastPacket);
				
				// Overwrite last packet
				lastPacket = dataPacket.buildPacket(fileData);

				// receive a data packet
				sendReceiveSocket.receive(lastPacket);

				ackBuff = new byte[Configurations.LEN_ACK_PACKET_BUFFET];
				lastPacket = new DatagramPacket(ackBuff,ackBuff.length);
				
				// Last packet now has the ACK block number, ready to be incremented
				sendReceiveSocket.receive(lastPacket);
				// Check if the receiving packet is an ACK
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function create a read request for the client and stores the file
	 * retrieved from the server on to the file system
	 * 
	 * @param readFileName
	 *            - the name of the file that the client requests from server
	 */
	private static void readRequestHandler(String readFileName) throws Exception {

		AckPacketBuilder ackPacketBuilder;
		DatagramPacket lastPacket;
		DataPacketBuilder dataPacket;
		AckPacketBuilder ackPacket;
		boolean morePackets = true;
		FileStorageService readRequestFileStorageService;

		// +4 for the opcode and block#
		byte[] dataBuf = new byte[Configurations.MAX_BUFFER + 4];

		try {
			readRequestFileStorageService = new FileStorageService(readFileName);
			// build read request packet
			ReadPacketBuilder rpb = new ReadPacketBuilder(InetAddress.getLocalHost(), Configurations.SERVER_LISTEN_PORT,
					readFileName, Configurations.DEFAULT_RW_MODE);

			// now get the packet from the ReadPacketBuilder
			lastPacket = rpb.buildPacket();

			// send the read packet over sendReceiveSocket
			sendReceiveSocket.send(lastPacket);

			// loop until no more packets to receive
			while (morePackets) {

				// create a datagramPacket for receiving dataPacket
				lastPacket = new DatagramPacket(dataBuf, dataBuf.length);

				// receive a data packet
				sendReceiveSocket.receive(lastPacket);

				// build the dataPacketBuilder
				dataPacket = new DataPacketBuilder(lastPacket);

				if (dataPacket.getRequestType() != RequestType.DATA) {
					// handle error and quit here with exception
				}

				// now store the data using file service and store the boolean
				// if more packets are to be received
				morePackets = readRequestFileStorageService.saveFileByteBufferToDisk(dataPacket.getDataBuffer());

				// create the ACK packet from the data packet last received
				ackPacket = new AckPacketBuilder(lastPacket);

				// send the ACK packet
				sendReceiveSocket.send(ackPacket.buildPacket());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * This function only prints the client side selection menu
	 * 
	 * @param
	 */
	public static void printSelectMenu() {
		System.out.println("----------------------");
		System.out.println("| Client Select Menu |");
		System.out.println("----------------------");
		System.out.println("Options : ");
		System.out.println("\t 1. Read File");
		System.out.println("\t 2. Write File");
		System.out.println("\t 3. Exit File\n\n");
		System.out.println("Select option : ");
	}
}

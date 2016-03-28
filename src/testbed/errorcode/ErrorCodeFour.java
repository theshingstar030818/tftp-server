package testbed.errorcode;

import java.net.*;

import packet.AckPacket;
import packet.Packet;
import packet.PacketBuilder;
import resource.Configurations;
import packet.DataPacket;
import types.*;

/**
 * @author Team 3
 *
 *         This class corrupts a piece of the datagram buffer to simulate a
 *         corrupt packet
 */
public class ErrorCodeFour {
	private RequestType mBlockType;
	private int mBlocknumber;
	private DatagramPacket mSendPacket;
	private RequestType rt;
	private boolean readWriteCheck;
	private byte[] readWriteBuffer;
	private int packetCount = 0;
	private PacketBuilder pb;
	private Packet mreceivePacket;

	public ErrorCodeFour(Packet receiveDatagramPacket) {
		this.mreceivePacket = receiveDatagramPacket;
		readWriteBuffer = this.mreceivePacket.getDataBuffer();
		this.packetCount++;
		// this.BlockTypeErrorCreator(SelectBlockType, selectSuboption);
	}

	public void setReceivePacket(DatagramPacket receiveDatagramPacket) {
		pb = new PacketBuilder();
		mreceivePacket = pb.constructPacket(receiveDatagramPacket);
	}

	/**
	 * Initializes Block type of the block to be corrupted
	 */

	public DatagramPacket BlockTypeErrorCreator(int SelectBlockType, int subOption) {
		switch (SelectBlockType) {
		case 1: // First Packet
			if (this.mreceivePacket.getBlockNumber() == -1) {
				this.mBlockType = this.mreceivePacket.getRequestType();
			}
			return this.FirstPacketErrorCreator(subOption);
		case 2: // ACK
			this.mBlockType = RequestType.ACK;
			return this.ackPacketErrorCreator(subOption);
		case 3: // DATA
			this.mBlockType = RequestType.DATA;
			return this.dataPacketErrorCreator(subOption);
		case 4: // ERROR
			this.mBlockType = RequestType.ERROR;
			return this.errorPacketCreator(subOption);
		}
		return null;
	}

	public DatagramPacket FirstPacketErrorCreator(int subOption) {
		switch (subOption) {
		case 1: // Invalid file name
			return this.errorPacketCreator(1);
		case 2: // Invalid packet header during transfer
			return this.errorPacketCreator(5);
		case 3: // Invalid zero padding bytes
			return this.errorPacketCreator(3);
		case 4: // Invalid mode
			return this.errorPacketCreator(2);
		default:
			return null;
		}
	}

	public DatagramPacket ackPacketErrorCreator(int subOption) {
		switch (subOption) {
		case 1: // Invalid block number
			return this.errorPacketCreator(4);
		case 2: // Invalid packet header during transfer
			return this.errorPacketCreator(5);
		case 3: // Invalid packet size
			return this.errorPacketCreator(6);
		default:
			return null;
		}
	}

	public DatagramPacket dataPacketErrorCreator(int subOption) {
		switch (subOption) {
		case 1: // Invalid block number
			return this.errorPacketCreator(4);
		case 2: // Invalid packet header during transfer
			return this.errorPacketCreator(5);
		case 3: // Invalid packet size
			return this.errorPacketCreator(6);
		default:
			return null;
		}
	}

	public DatagramPacket errorPacketErrorCreator(int subOption) {
		switch (subOption) {
		case 1: // Invalid error number
			break;
		case 2: // Invalid packet header during transfer
			return this.errorPacketCreator(5);
		default:
			return null;
		}
		return null;
	}

	/**
	 * Create an error packet by first checking a condition whether or not
	 * first.
	 * 
	 * @return
	 */
	public DatagramPacket errorPacketCreator(int errorCase) {
		this.mSendPacket = null;
		rt = this.mreceivePacket.getRequestType();
		readWriteCheck = (rt == RequestType.RRQ || rt == RequestType.WRQ);
		switch (errorCase) {
		case 1: // Change filename
			if (readWriteCheck) {
				// Settings and invalid file name
				this.mreceivePacket.setFilename("*:?.txt");
				this.mSendPacket = this.mreceivePacket.buildPacket();
			}
			break;
		case 2: // Change mode
			if (readWriteCheck) {
				this.mreceivePacket.setMode(switchMode());
				this.mSendPacket = this.mreceivePacket.buildPacket();
			}
			break;
		case 3: // 0 Padding
			if (readWriteCheck) {
				this.mreceivePacket.setDataBuffer(addZerosToBuffer());
				this.mreceivePacket.getPacket().setData(this.mreceivePacket.getDataBuffer());
				this.mSendPacket = this.mreceivePacket.getPacket();
			}
		case 4: // Change block number
			if (rt == RequestType.DATA) {
				// The following build packet will automatically increment block
				// number by 1, which
				// effectively mismatches the block number

				this.mSendPacket = ((DataPacket) this.mreceivePacket).buildPacket(this.mreceivePacket.getDataBuffer());
			} else if (rt == RequestType.ACK) {
				int currentBlockNumber = this.mreceivePacket.getBlockNumber();
				this.mreceivePacket.setBlockNumber(currentBlockNumber + 10);
				this.mSendPacket = ((AckPacket) this.mreceivePacket).buildPacket();
			} else {
				this.mSendPacket = this.mreceivePacket.getPacket();
			}
			break;
		case 5:// Change header request type on the first expected DATA/ACK to
				// a WRQ or RRQ
			this.changeHeader(this.mreceivePacket);
			break;
		case 6:// Change packet size
			this.invalidPacketSize(this.mreceivePacket);
			break;
		case 7: // changing header of first packet received from client
			if (this.packetCount == 1) {
				this.changeHeader(this.mreceivePacket);
			} else {
				this.mSendPacket = this.mreceivePacket.getPacket();
			}
			break;

		default:
			// TODO: default action for error creator
			break;
		}
		this.packetCount++;
		return this.mSendPacket;
	}

	/**
	 * This function takes in a datagram packet and change the header on the
	 * datagram packet to invalid
	 * 
	 * @param inPacket
	 */
	private void changeHeader(Packet inPacket) {
		byte[] header = inPacket.getRequestType().getHeaderByteArray();
		byte[] data = inPacket.getPacketBuffer();
		DatagramPacket packet = inPacket.getPacket();
		switch (header[1]) {
		case 1:
			// read request with header[0,1]
			data[1] = 3;
			this.mSendPacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
			break;
		case 2:
			// write request with header [0,2]
			data[1] = 3;
			this.mSendPacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
			break;
		case 3:
			// data datagram packet with header [0,3]
			data[1] = 4;
			this.mSendPacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
			break;
		case 4:
			// ack packet with [0,4]
			data[1] = 3;
			this.mSendPacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());

			break;
		case 5:
			// error packet
			// should not do anything
			data[1] = 1;
			this.mSendPacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
			break;
		default:
			this.mSendPacket = inPacket.getPacket();
			break;
		}
	}

	/**
	 * This function double the size of passed in datagram packet to an invalid
	 * size
	 * 
	 * @param inPacket
	 */
	private void invalidPacketSize(Packet inPacket) {
		int length = inPacket.getPacketLength();
		int maxMultipleLengthGreaterThanMessageSize = (Configurations.MAX_MESSAGE_SIZE / length) + 1;
		int blowMaxMessageBuffer = maxMultipleLengthGreaterThanMessageSize * length;
		byte[] data = new byte[blowMaxMessageBuffer];
		int currentLength = 0;
		do {
			System.arraycopy(inPacket.getPacketBuffer(), 0, data, currentLength, length);
			currentLength += length;
		} while (currentLength < blowMaxMessageBuffer);

		this.mSendPacket = new DatagramPacket(data, data.length, inPacket.getPacket().getAddress(),
				inPacket.getPacket().getPort());
	}

	private ModeType switchMode() {
		return ModeType.INVALID;
	}

	private byte[] addZerosToBuffer() {
		if (readWriteCheck) {
			readWriteBuffer[readWriteBuffer.length - 1] = 65;
		}
		return (readWriteBuffer);
	}

}

package testbed.errorcode;

import java.net.*;

import packet.AckPacket;
import packet.Packet;
import resource.Configurations;
import testbed.ErrorCodeSimulator;
import packet.DataPacket;
import types.*;

/**
 * @author Team 3
 *
 * This class corrupts a piece of the datagram buffer to simulate a corrupt packet
 */
public class ErrorCodeFour extends ErrorCodeSimulator {
	private int mSubcode;
	private DatagramPacket mSendPacket;
	private RequestType rt;
	private boolean readWriteCheck;
	private byte[] readWriteBuffer = super.receivePacketBuilder.getDataBuffer();
	private int packetCount = 0;

	public ErrorCodeFour(DatagramPacket receivePacket, int subcode) {
		super(receivePacket);
		this.packetCount++;
		this.mSubcode = subcode;
	}

	/**
	 * Create an error packet by first checking a condition whether or not first. 
	 * 
	 * @return
	 */
	public DatagramPacket errorPacketCreator() {
		this.mSendPacket = null;
		rt = super.receivePacketBuilder.getRequestType();
		readWriteCheck = (rt == RequestType.RRQ || rt == RequestType.WRQ);
		switch (this.mSubcode) {
		case 1: // Change filename
			if (readWriteCheck) {
				// Settings and invalid file name
				super.receivePacketBuilder.setFilename("*:?.txt");
				this.mSendPacket = super.receivePacketBuilder.buildPacket();
			}
			break;
		case 2: // Change mode
			if (readWriteCheck) {
				super.receivePacketBuilder.setMode(switchMode());
				this.mSendPacket = super.receivePacketBuilder.buildPacket();
			}
			break;
		case 3: // Change number of 0s in the header
			if (readWriteCheck) {
				super.receivePacketBuilder.setDataBuffer(addZerosToBuffer());
				super.receivePacketBuilder.getPacket().setData(super.receivePacketBuilder.getDataBuffer());
				this.mSendPacket = super.receivePacketBuilder.getPacket();
			}
		case 4: // Change block number
			if ( rt == RequestType.DATA) {
				// The following build packet will automatically increment block
				// number by 1, which
				// effectively mismatches the block number
			
				this.mSendPacket = ((DataPacket) receivePacketBuilder).buildPacket(receivePacketBuilder.getDataBuffer());
			} else if(rt == RequestType.ACK) {
				int currentBlockNumber = super.receivePacketBuilder.getBlockNumber();
				super.receivePacketBuilder.setBlockNumber((short) (currentBlockNumber + 5));
				this.mSendPacket = ((AckPacket) receivePacketBuilder).buildPacket();
			} else {
				this.mSendPacket = super.receivePacketBuilder.getPacket();
			}
			break;
		case 5:// Change header request type on the first expected DATA/ACK to
				// a WRQ or RRQ
			this.changeHeader(super.receivePacketBuilder);
			break;
		case 6:// Change packet size
			this.invalidPacketSize(super.receivePacketBuilder);
			break;
		case 7: // changing header of first packet received from client
			if (this.packetCount == 1) {
				this.changeHeader(super.receivePacketBuilder);
			} else {
				this.mSendPacket = super.receivePacketBuilder.getPacket();
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
			if (this.packetCount == 1) {
				data[1] = 3;
				this.mSendPacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
			} else {
				this.mSendPacket = inPacket.getPacket();
			}
			break;
		case 2:
			// write request with header [0,2]
			if (this.packetCount == 1) {
				data[1] = 3;
				this.mSendPacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
			} else {
				this.mSendPacket = inPacket.getPacket();
			}
			break;
		case 3:
			// data datagram packet with header [0,3]
			if (this.packetCount == 2) {
				data[1] = 4;
				this.mSendPacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
			} else {
				this.mSendPacket = inPacket.getPacket();
			}
			break;
		case 4:
			// ack packet with [0,4]
			
			if (this.packetCount == 2) {
				data[1] = 3;
				this.mSendPacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
			} else {
				this.mSendPacket = inPacket.getPacket();
			}
			break;
		case 5:
			// error packet
			// should not do anything
			data[1] = 3;
			if (this.packetCount > 2) {
				data[1] = 4;
				this.mSendPacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
			} else {
				this.mSendPacket = inPacket.getPacket();
			}
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
		} while(currentLength < blowMaxMessageBuffer);
		
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

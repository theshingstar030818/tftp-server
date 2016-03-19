package testbed;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import packet.AckPacket;
import packet.DataPacket;
import packet.ErrorPacket;
import packet.Packet;
import resource.Configurations;
import resource.Strings;
import types.ErrorType;
import types.Logger;
import types.RequestType;

/**
 * @author Team 3
 * 
 *         Error checking class to determine if errors occur
 */
public class ErrorChecker {

	private InetAddress mPacketOriginatingAddress;
	private int mPacketOriginatingPort;
	public short mExpectedBlockNumber;
	private Logger logger = Logger.ERROR;
	private final String CLASS_TAG = "<Error Checker>";
	
	public ErrorChecker(Packet packet) {
		mPacketOriginatingAddress = packet.getPacket().getAddress();
		mPacketOriginatingPort = packet.getPacket().getPort();
		mExpectedBlockNumber = 0;
		logger.setClassTag(CLASS_TAG);
	}
	
	public int getExpectedBlockNumber() {
		return mExpectedBlockNumber;
	}

	public void incrementExpectedBlockNumber() {
		mExpectedBlockNumber++;
		if(this.mExpectedBlockNumber == 32767) {
			this.mExpectedBlockNumber = 0;
		}
	}

	/**
	 * General error checking function that determines every possible error that
	 * could occur to a packet 1.File not found 2.Access violation 3.Disk full
	 * or allocation exceeded 4.Illegal TFTP operation 5.Unknown transfer ID
	 * 6.File already exists 7.No such user (obsolete)
	 * 
	 * @param packet
	 *            - in comming packet to check
	 * @param expectedCommunicationType
	 *            - the expected request type to check against
	 * @return TFTPErrorMessage message
	 */
	public TFTPErrorMessage check(Packet packet, RequestType expectedCommunicationType) {
		
		if (packet.getRequestType() == RequestType.ERROR) {
			// We found an error packet, now print out the message.
			ErrorPacket errorPacket = new ErrorPacket(packet.getPacket());
			return new TFTPErrorMessage(errorPacket.getErrorType(), errorPacket.getCustomPackageErrorMessage());
		}

		// Check if address and port match the expected address and port.
		if (!mPacketOriginatingAddress.equals(packet.getPacket().getAddress())
				|| mPacketOriginatingPort != packet.getPacket().getPort())
			return new TFTPErrorMessage(ErrorType.UNKNOWN_TRANSFER, Strings.UNKNOWN_TRANSFER);

		// Check that the packet format is correct.
		String formatErrorMessage = formatError(packet, expectedCommunicationType);
		if(formatErrorMessage.equals("-1")) {
			logger.print(logger, String.format("Dealing with the special sorcerors apprentice bug. Not going to reply with a duplicate DATA..."));
			return new TFTPErrorMessage(ErrorType.SORCERERS_APPRENTICE, Strings.SORCERERS_APPRENTICE);
		}
		
		if (!formatErrorMessage.isEmpty())
			return new TFTPErrorMessage(ErrorType.ILLEGAL_OPERATION, formatErrorMessage);

		// No error occurred.
		return new TFTPErrorMessage(ErrorType.NO_ERROR, Strings.NO_ERROR);

	}

	/**
	 * Checks packet format errors for all packets: DATA,RRQ,WRQ,ERROR,ACK
	 * 
	 * @param packet 	- incoming packet
	 * @param comType	- the expected communication type
	 * @return error string
	 */
	private String formatError(Packet packet, RequestType comType) {
		int currentBlockNumber = 0;
		byte[] data = packet.getPacketBuffer();
		if (data[0] != 0) {
			logger.print(logger, String.format("Packet validation found invalid zero byte in the begining!"));
			return Strings.NON_ZERO_FIRST_BYTE;
		}

		if (RequestType.matchRequestByNumber(data[1]) != comType) {
			logger.print(logger, String.format("Packet validation found op code %d when expecting %d!",
					RequestType.matchRequestByNumber(data[1]).getOptCode(), comType.getOptCode()));
			return Strings.COMMUNICATION_TYPE_MISMATCH;
		}

		if (data.length > Configurations.MAX_MESSAGE_SIZE) {
			logger.print(logger, String.format("Packet validation found the packet was too large!"));
			return Strings.PACKET_TOO_LARGE;
		}

		switch (comType) {
		case RRQ:
		case WRQ:
			if (data[2] == 0) {
				logger.print(logger, String.format("RRQ/WRQ Packet validation found missing filename!"));
				return Strings.MISSING_FILENAME;
			}
			if (data[data.length - 1] != 0) {
				logger.print(logger, String.format("RRQ/WRQ Packet validation found missing last byte!"));
				return Strings.NON_ZERO_LAST_BYTE;
			}
			int secondZeroIndex = -1, thirdZeroIndex = -1;

			for (int i = 3; i < data.length; ++i) {
				if (data[i] == 0) {
					if (secondZeroIndex == -1) {
						secondZeroIndex = i;
					} else if (thirdZeroIndex == -1) {
						thirdZeroIndex = i;
						break;
					}

				}
			}
			if (data[secondZeroIndex] != 0) {
				logger.print(logger, String.format("RRQ/WRQ Packet validation found invalid zero bytes in message!"));
				return Strings.NON_ZERO_PADDING;
			}
			byte[] filenameBytes = new byte[secondZeroIndex - 2];
			byte[] modeBytes = new byte[thirdZeroIndex - secondZeroIndex - 1];
			System.arraycopy(data, 2, filenameBytes, 0, filenameBytes.length);
			System.arraycopy(data, secondZeroIndex + 1, modeBytes, 0, modeBytes.length);
			String filename = new String(filenameBytes);
			String mode = new String(modeBytes);
			if (!mode.equalsIgnoreCase("octet") && !mode.equalsIgnoreCase("netascii")) {
				logger.print(logger, String.format("RRQ/WRQ Packet validation found invalid mode!"));
				return Strings.INVALID_MODE;
			}

			if (!isValidFilename(filename)) {
				logger.print(logger, String.format("RRQ/WRQ Packet validation found invalid file name!"));
				return Strings.INVALID_FILENAME;
			}
			break;

		case DATA:
			currentBlockNumber = ((DataPacket) packet).getBlockNumber();
			if(this.mExpectedBlockNumber > currentBlockNumber) {
				logger.print(logger, String.format("We received a delayed DATA packet with blk %d when expected %d, going to ack this packet.", 
						currentBlockNumber, this.mExpectedBlockNumber));
				return "-1";
			}else if (mExpectedBlockNumber != currentBlockNumber) {
				logger.print(logger, String.format("ACK block number mismatch on expected %d from actual %d",
						this.mExpectedBlockNumber, currentBlockNumber));
				return Strings.BLOCK_NUMBER_MISMATCH;
			}
			break;

		case ACK:
			currentBlockNumber = ((AckPacket) packet).getBlockNumber();
			if (packet.getPacketLength() != 4) {
				logger.print(logger, String.format("Invalid packet size on ACK of %d with expected %d", packet.getPacketLength(), 4));
				return Strings.INVALID_PACKET_SIZE;
			}
			if(this.mExpectedBlockNumber > currentBlockNumber) {
				logger.print(logger, String.format("We received a delayed ACK packet with blk %d when expected %d. We should not resend a DATA to correspond to this ACK.", 
						currentBlockNumber, this.mExpectedBlockNumber));
				return "-1";
			}
			else if (mExpectedBlockNumber != currentBlockNumber) {
				logger.print(logger, String.format("ACK block number mismatch on expected %d from actual %d (op code %d)", 
						this.mExpectedBlockNumber, currentBlockNumber, packet.getRequestType().getOptCode()));
				return Strings.BLOCK_NUMBER_MISMATCH;
			}
			break;

		case ERROR:
			if (data.length < 6) {
				logger.print(logger, String.format("Error Packet validation but the length is too small!"));
				return Strings.PACKET_TOO_SMALL;
			}

			if (data[data.length - 1] != 0) {
				logger.print(logger, String.format("Error Packet validation but the last byte was not zero!"));
				return Strings.NON_ZERO_LAST_BYTE;
			}

			if (data[2] != 0) {
				logger.print(logger, String.format("Error Packet validation found invalid op code format!"));
				return Strings.INVALID_ERROR_CODE_FORMAT;
			}

			if (data[3] < 0 || data[3] > 8) {
				logger.print(logger, String.format("Error Packet validation found unknown error code"));
				return Strings.UNKOWN_ERROR_CODE;
			}
			break;

		case NONE:
			logger.print(logger, String.format("None Packet validation, programming error!"));
			return Strings.INVALID_PACKET_NONE_TYPE;
		}

		return "";
	}

	/**
	 * Regular expression to valid file names on windows system
	 * 	Reference: http://stackoverflow.com/questions/6730009/validate-a-file-name-on-windows
	 * 
	 * @param text	- filename
	 * @return true or false
	 */
	public static boolean isValidFilename(String text) {
		Pattern pattern = Pattern.compile(
				"# Match a valid Windows filename (unspecified file system).          \n"
						+ "^                                # Anchor to start of string.        \n"
						+ "(?!                              # Assert filename is not: CON, PRN, \n"
						+ "  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n"
						+ "    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n"
						+ "    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n"
						+ "  )                              # LPT6, LPT7, LPT8, and LPT9...     \n"
						+ "  (?:\\.[^.]*)?                  # followed by optional extension    \n"
						+ "  $                              # and end of string                 \n"
						+ ")                                # End negative lookahead assertion. \n"
						+ "[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n"
						+ "[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n"
						+ "$                                # Anchor to end of string.            ",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);
		Matcher matcher = pattern.matcher(text);
		boolean isMatch = matcher.matches();
		return isMatch;
	}
}

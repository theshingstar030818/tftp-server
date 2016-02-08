package testbed;

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import packet.AckPacketBuilder;
import packet.DataPacketBuilder;
import packet.PacketBuilder;
import resource.Configurations;
import resource.Strings;
import types.ErrorType;
import types.RequestType;

public class ErrorChecker {
    
    private InetAddress otherAddress; // Temporary name.
    private int otherPort; // Temporary name.
    private int expectedBlockNumber;
    
    public ErrorChecker(PacketBuilder packet) {
        otherAddress = packet.getPacket().getAddress();
        otherPort = packet.getPacket().getPort();
        expectedBlockNumber = 0;
    }
    
    public void incrementexpectedBlockNumber() {
    	expectedBlockNumber++;
    }
    
    public TFTPError check(PacketBuilder packet, RequestType expectedCommunicationType) {

        // Check if address and port match the expected address and port.
        if (!otherAddress.equals(packet.getPacket().getAddress()) || otherPort != packet.getPacket().getPort())
            return new TFTPError(ErrorType.UNKNOWN_TRANSFER, Strings.UNKNOWN_TRANSFER);
                
        // Check that the packet format is correct.
        String formatErrorMessage = formatError(packet, expectedCommunicationType);
        if (!formatErrorMessage.isEmpty())
            return new TFTPError(ErrorType.ILLEGAL_OPERATION, formatErrorMessage);

        // No error occurred.
        return new TFTPError(ErrorType.NO_ERROR, Strings.NO_ERROR);
        
    }
    
    private String formatError(PacketBuilder packet, RequestType comType) {
    	
    	byte[] data = packet.getPacket().getData();
    	if (data[0] != 0) return Strings.NON_ZERO_FIRST_BYTE;
    	if (RequestType.matchRequestByNumber(data[1]) != comType) 
    		return Strings.COMMUNICATION_TYPE_MISMATCH;
    	
    	switch (comType) {
    		case RRQ:
    		case WRQ:
    			if (data[2] == 0) return Strings.MISSING_FILENAME;
    			int secondZeroIndex = -1, thirdZeroIndex = -1;    			
    			
    			for (int i = 3; i < data.length; ++i) {
    				if (data[i] == 0) {
    					if (secondZeroIndex == -1) {
    						secondZeroIndex = i;
    					}
    					else if (thirdZeroIndex == -1) {
    						thirdZeroIndex = i;
    						break;
    					}
    					
    				}
    			}
    			byte[] filename = new byte[secondZeroIndex - 2];
    			byte[] mode = new byte[thirdZeroIndex - secondZeroIndex];
    			System.arraycopy(data, 2, filename, 0, filename.length);
    			System.arraycopy(data, secondZeroIndex, mode, 0, mode.length);
    			
    			if (!mode.toString().equalsIgnoreCase("octet") && !mode.toString().equalsIgnoreCase("netascii"))
    				return Strings.INVALID_MODE;
    			if (!isValidFilename(filename.toString()))
    				return Strings.INVALID_FILENAME;
    				
    			break;
    			
    		case DATA:
    			if (packet.getPacket().getData().length > Configurations.MAX_MESSAGE_SIZE) 
    				return Strings.PACKET_TOO_LARGE;
    			if (expectedBlockNumber != ((DataPacketBuilder) packet).getBlockNumber()) 
    				return Strings.BLOCK_NUMBER_MISMATCH; 
    			break;
    			
    		case ACK:
    			if (packet.getPacket().getData().length != 4) 
    				return Strings.INVALID_PACKET_SIZE;
    			if (expectedBlockNumber != ((AckPacketBuilder) packet).getBlockNumber()) 
    				return Strings.BLOCK_NUMBER_MISMATCH;
    			break;
    			
    		case ERROR:
    			if (data.length < 6) return Strings.PACKET_TOO_SMALL;
    			if (data[data.length-1] != 0) return Strings.NON_ZERO_LAST_BYTE;
    			if (data[2] != 0) return Strings.INVALID_ERROR_CODE_FORMAT;
    			if (data[3] < 0 || data[3] > 8) return Strings.UNKOWN_ERROR_CODE;
    			break;
    			
    		case NONE:
    			return Strings.INVALID_PACKET_NONE_TYPE;
    	}
    	
    	return "";
    }
    
    public static boolean isValidFilename(String text)
    {
        Pattern pattern = Pattern.compile(
            "# Match a valid Windows filename (unspecified file system).          \n" +
            "^                                # Anchor to start of string.        \n" +
            "(?!                              # Assert filename is not: CON, PRN, \n" +
            "  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n" +
            "    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n" +
            "    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n" +
            "  )                              # LPT6, LPT7, LPT8, and LPT9...     \n" +
            "  (?:\\.[^.]*)?                  # followed by optional extension    \n" +
            "  $                              # and end of string                 \n" +
            ")                                # End negative lookahead assertion. \n" +
            "[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n" +
            "[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n" +
            "$                                # Anchor to end of string.            ", 
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);
        Matcher matcher = pattern.matcher(text);
        boolean isMatch = matcher.matches();
        return isMatch;
    }
}

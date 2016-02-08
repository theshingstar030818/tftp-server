package helpers;

import java.util.Arrays;
import types.Logger;
/**
 * @author Team 3
 *
 * This is a printing class helper for the TFTP system
 */
public class BufferPrinter {
	
	// this metthod now only prints if the client/Error simulator/server was initialized
	// with a LogLevel VERBOSE 
	public static void printBuffer(byte[] buffer, String entity, Logger logLevel) {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(entity + " prints contents of the UDP buffer:\n");
		strBuilder.append(Arrays.toString(buffer) + "\n");
		strBuilder.append(entity + " prints contents of UDP buffer as string: \n");
		strBuilder.append(new String(buffer) + "\n");
		logLevel.print(Logger.VERBOSE, strBuilder.toString());
	}
	
	public static String bufferToString(byte[] buffer) {
		StringBuilder strBuilder = new StringBuilder();
		for(int i = 0; i< buffer.length; i++) {
			char value = (char)buffer[i];
			if (Character.isLetter(value)) {
				strBuilder.append(value);
			}
			else if (Character.isDigit(value)) {
				strBuilder.append(value);
			}
			else {
				strBuilder.append(buffer[i]);
			}
		}
		return strBuilder.toString();
	}
	
	public static String acceptConnectionMessage(String message, String senderAddress) {
		return message + " " + senderAddress + ".";
	}
}

package helpers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * @author Team 3
 *	
 *	This class has a static collection of useful conversion functions used throughout
 *	the TFTP system
 */
public class Conversion {

	/**
	 * Converts a sized two byte array into a short. 
	 * It's important to note that the byte order used here is LITTLE_ENDIAN,
	 * therefore, if shortToBytes() was used to convert your short in to a byte,
	 * you must bytesToShort() to convert it back. This is because the socket
	 * API of Java has a default BIG_ENDIAN byte order
	 * 
	 * @param bytes - a two lengthed byte array representing a short
	 * @return short that was converted from bytes
	 */
	public static int bytesToInt(byte[] bytes) {
		return ((bytes[1] & 0xFF)<< 8) | (bytes[0] & 0xFF);
	}
	
	/**
	 * Converts a sized short into a two byte array. 
	 * It's important to note that the byte order used here is LITTLE_ENDIAN,
	 * therefore, if shortToBytes() was used to convert your short in to a byte,
	 * you must bytesToShort() to convert it back. This is because the socket
	 * API of Java has a default BIG_ENDIAN byte order
	 * 
	 * @param value - the two byte short converted into an array
	 * @return a two byte array representation of your short
	 */
	public static byte[] intToBytes(int value) {
	    return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putChar((char)value).array();
	}
	
	/**
	 * Flattens an array list of bytes into a byte array
	 * 
	 * @param msg - an ArrayList<Byte>
	 * @return a byte array where list.toBytes()
	 */
	public static byte[] toByteArray(ArrayList<Byte> msg) {

		byte bArray[] = new byte[msg.size()];
		for (int i = 0; i < msg.size(); i++) {
			bArray[i] = msg.get(i);
		}
		return bArray;
	}

}

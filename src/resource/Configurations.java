/**
 * 
 */
package resource;

import types.ModeType;

/**
 * @author Team 3
 *
 *	This class is used to define some static configurations of the TFTP system
 */
public class Configurations {
	
	public static final String ROOT_FILE_DIRECTORY = System.getProperty("user.home") + "/TFTP-Storage-Folder";
	public static final int MAX_BUFFER = 512;
	public static final int ERROR_SIM_LISTEN_PORT = 5001;   // Change this to something your Unix systems like
	public static final int SERVER_LISTEN_PORT = 5000;		// Ditto line above
	public static final ModeType DEFAULT_RW_MODE = ModeType.OCTET;
	public static final String DEFAULT_FILENAME = "file";
	public static final int LEN_ACK_PACKET_BUFFET = 4;
}

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
	public static final String USER_HOME =  System.getProperty("user.home");
	public static final String CLIENT_ROOT_FILE_DIRECTORY = Configurations.USER_HOME + "/TFTP-Client-Storage-Folder";
	public static final String SERVER_ROOT_FILE_DIRECTORY = Configurations.USER_HOME + "/TFTP-Server-Storage-Folder";
	public static final int MAX_BUFFER = 512; 	// Max buffer size is 4092 (Disk block size lulz)
	public static final int MAX_MESSAGE_SIZE = 516;
	public static final int ERROR_SIM_LISTEN_PORT = 5001;   // Change this to something your Unix systems like
	public static final int SERVER_LISTEN_PORT = 5000;		// Ditto line above
	public static final ModeType DEFAULT_RW_MODE = ModeType.OCTET;
	public static final String DEFAULT_FILENAME = "file";
	public static final int LEN_ACK_PACKET_BUFFET = 4;
}

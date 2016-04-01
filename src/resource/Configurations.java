
package resource;

import types.ModeType;

/**
 * @author Team 3
 *
 *         This class is used to define some static configurations of the TFTP
 *         system
 */
public class Configurations {
	public static final String USER_HOME = System.getProperty("user.home");
	public static final String CLIENT_ROOT_FILE_DIRECTORY = Configurations.USER_HOME + "/TFTP-Client-Storage-Folder";
	public static final String SERVER_ROOT_FILE_DIRECTORY = Configurations.USER_HOME + "/TFTP-Server-Storage-Folder";
	public static final int MAX_BUFFER = 1024;
	public static final int MAX_MESSAGE_SIZE = 516;
	public static final int MAX_PAYLOAD_BUFFER = 512;
	public static final int ERROR_SIM_LISTEN_PORT = 68;
	public static final int SERVER_LISTEN_PORT = 69;   
	public static final ModeType DEFAULT_RW_MODE = ModeType.OCTET;
	public static final String DEFAULT_FILENAME = "file";
	public static final int LEN_ACK_PACKET_BUFFER = 4;
	public static final String SERVER_INET_HOST = "localhost";
	public static final int ERROR_PACKET_USELESS_VALUES = 5;
	public static final int TRANMISSION_TIMEOUT = 1000; // 1000 ms
	public static final int RETRANMISSION_TRY = 4;
	
	/* Important keys for Error Simulator command */
	public static final String MAIN_ERROR = "MAIN_ERROR_SELECTION";
	public static final String SUB_ERROR = "SUB_ERROR_SELECTION";
	public static final String TE_NUM_PACKETS = "NUM_PACKETS_SELECTION";
	public static final String TE_FREQ_ERROR = "FREQ_ERROR_SELECTION";
	public static final String TE_TYPE_ERROR = "TYPE_ERROR_SELECTION";
	public static final String TE_ERROR_PACKET = "ERROR_BLOCK_NUMBER";
	public static final String TE_SUB_SUB_OPTION = "SUB_SUB_OPTION";
}

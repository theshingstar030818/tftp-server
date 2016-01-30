/**
 * 
 */
package resource;

/**
 * @author Team 3
 *
 *	This class is used to create static string messages for the TFTP 
 *	system.
 */
public class Strings {
	// File and directory related messages.
	public static final String FILE_WRITE_ERROR = "An error occurred while writing the file.";
	public static final String FILE_READ_ERROR = "An error occurred while reading the file.";
	public static final String DIRECTORY_MAKE_ERROR = "An error occured while making a new directory.";
	public static final String FILE_WRITE_COMPLETE = "Server received a block size zero, terminating write procedure.";
	public static final String FILE_CHANNEL_CLOSE_ERROR = "Closing file channel failed.";
	public static final String FILE_NOT_EXIST = "File does not exist.";
	
	// Server messages.
	public static final String SERVER_RECEIVE_ERROR = "Failed to receive packet on main thread.";
	public static final String SERVER_ACCEPT_CONNECTION = "Server has accepted a connection!";
	public static final String EXITING = "Server listening port is closing, connected threads ending after transfer completes.";
	
	// Client messages.
	public static final String PROMPT_ENTER_FILE_NAME = "Please enter file name:";
	public static final String PROMPT_FILE_NAME_PATH = "Please enter file name or file path:";
	public static final String ERROR_INPUT = "ERROR : Please select a valid option.";
	public static final String EXIT_BYE = "Bye bye.";
	public static final String TRANSFER_SUCCESSFUL = "File transfer was successful.";
	public static final String TRANSFER_FAILED = "File transfer failed.";
}

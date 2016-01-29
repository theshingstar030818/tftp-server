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
	
	// Server messages.
	public static final String SERVER_RECEIVE_ERROR = "Failed to receive packet on main thread.";
	public static final String SERVER_ACCEPT_CONNECTION = "Server has accepted a connection!";
}

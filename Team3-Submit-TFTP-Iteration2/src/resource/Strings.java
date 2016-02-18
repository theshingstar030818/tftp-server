package resource;

/**
 * @author Team 3
 *
 *         This class is used to create static string messages for the TFTP
 *         system.
 */
public class Strings {
	// File and directory related messages.
	public static final String FILE_WRITE_ERROR = "An error occurred while writing the file.";
	public static final String FILE_READ_ERROR = "An error occurred while reading the file.";
	public static final String DIRECTORY_MAKE_ERROR = "An error occured while making a new directory.";
	public static final String FILE_WRITE_COMPLETE = "Received a block size zero, terminating write procedure.";
	public static final String FILE_CHANNEL_CLOSE_ERROR = "Closing file channel failed.";
	public static final String FILE_NOT_EXIST = "File does not exist.";

	// Server messages.
	public static final String SERVER_RECEIVE_ERROR = "Failed to receive packet on main thread.";
	public static final String SERVER_ACCEPT_CONNECTION = "Server has accepted a connection!";
	public static final String EXITING = "Server listening port is closing, connected threads ending after transfer completes.";
	public static final String SS_TRANSFER_FINISHED = "Service thread finished work, exiting.";
	public static final String SS_WRONG_PACKET = "Server cannot cannot accept a service other than write or read request.";

	// Client messages.
	public static final String PROMPT_ENTER_FILE_NAME = "Please enter file name:";
	public static final String PROMPT_FILE_NAME_PATH = "Please enter file name or file path:";
	public static final String ERROR_INPUT = "ERROR : Please select a valid option.";
	public static final String EXIT_BYE = "Bye bye.";
	public static final String TRANSFER_SUCCESSFUL = "File transfer was successful.";
	public static final String TRANSFER_FAILED = "File transfer failed.";
	public static final String CLIENT_INITIATE_WRITE_REQUEST = "Client initiating write request . . .";
	public static final String CLIENT_INITIATING_FIE_STORAGE_SERVICE = "Client initiating file storage service . . .";
	public static final String FILE_NAME = "File name : ";
	public static final String SENDING = "Sending : ";
	public static final String RECEIVED = "Received : ";
	public static final String CLIENT_INITIATE_READ_REQUEST = "Client initiating read request . . .";

	// TFTP error messages.
	public static final String NOT_DEFINED = "Not defined, see error message (if any).";
	public static final String FILE_NOT_FOUND = "File not found.";
	public static final String ACCESS_VIOLATION = "Access violation.";
	public static final String ALLOCATION_EXCEED = "Disk full or allocation exceeded.";
	public static final String ILLEGAL_OPERATION = "Illegal TFTP operation.";
	public static final String ILLEGAL_OPERATION_HELP_MESSAGE = ILLEGAL_OPERATION + " Shutting down server thread.";
	public static final String UNKNOWN_TRANSFER = "Unknown transfer ID.";
	public static final String UNKNOWN_TRANSFER_HELP_MESSAGE = UNKNOWN_TRANSFER
			+ " Sending error message to the unknown client.";
	public static final String FILE_EXISTS = "File already exists.";
	public static final String NO_SUCH_USER = "No such user.";
	public static final String NO_ERROR = "No error.";
	public static final String OPERATION_NOT_SUPPORTED = "This option is not supported right now. Not generating any errors.";
	public static final String EXCEPTION_ERROR = "Exception Error please see console for details.";

	// TFTP error message templates.
	public static final String NON_ZERO_FIRST_BYTE = "First byte is not 0.";
	public static final String NON_ZERO_LAST_BYTE = "Last byte is not 0.";
	public static final String NON_ZERO_PADDING = "Filename and mode did not have a 0 padding.";
	public static final String COMMUNICATION_TYPE_MISMATCH = "Unexpected communication type.";
	public static final String MISSING_FILENAME = "Missing filename.";
	public static final String INVALID_FILENAME = "Invalid filename.";
	public static final String INVALID_MODE = "Invalid mode.";
	public static final String INVALID_PACKET_NONE_TYPE = "Invalid packet of 'none' type.";
	public static final String PACKET_TOO_LARGE = "Packet too large.";
	public static final String PACKET_TOO_SMALL = "Packet too small.";
	public static final String BLOCK_NUMBER_MISMATCH = "Block number mismatch.";
	public static final String INVALID_PACKET_SIZE = "Invalid packet size.";
	public static final String UNKOWN_ERROR_CODE = "Unknown error code.";
	public static final String INVALID_ERROR_CODE_FORMAT = "Error code does not begin with 0.";

	// Error simulator messages
	public static final String ES_START_LISTENING = "Starting to listen for traffic";
	public static final String ES_INITIALIZED = "Error simulator initiated on port %d.\n";
	public static final String ES_TRANSFER_ERROR = "Transfered an unrecoverable error packet, shutting down. \n --- Press enter key to continue to menu ---";
	public static final String ES_TRANSFER_SUCCESS = "Finished transfer and shutting down. \n --- Press enter key to continue to menu ---";
	public static final String ES_RETRIEVE_PACKET_CLIENT = "Preparing to retrieve packet from client.";
	public static final String ES_SEND_PACKET_CLIENT = "Preparing to send packet to client.";
	public static final String ES_GOT_LAST_PACKET_WRQ = "Got last write packet, fwding ACK to client";
	public static final String ES_RETRIEVE_PACKET_SERVER = "Preparing to retrieve packet from server.";

	public static final String ACK_PACKET = "ACK PACKET";
	public static final String DATA_PACKET = "DATA PACKET";
	public static final String RRQ = "READ PACKET";
	public static final String WRQ = "WRITE PACKET";
	public static final String ERROR = "ERROR PACKET";
	public static final String NONE = "PACKET NOT DEFINED";

}

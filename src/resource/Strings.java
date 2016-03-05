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
	public static final String TRANSMISSION_ERROR = "Transmission error, dropped, duplicated, or lost packets.";
	public static final String SORCERERS_APPRENTICE = "Caught a magical moment. Sorcerer's apprentice bug.";

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
	public static final String ES_SEND_PACKET_SERVER = "Preparing to send packet to the server.";
	public static final String ES_GOT_LAST_PACKET_WRQ = "Got last write packet, fwding ACK to client";
	public static final String ES_RETRIEVE_PACKET_SERVER = "Preparing to retrieve packet from server.";

	public static final String ACK_PACKET = "ACK PACKET";
	public static final String DATA_PACKET = "DATA PACKET";
	public static final String RRQ = "READ PACKET";
	public static final String WRQ = "WRITE PACKET";
	public static final String ERROR = "ERROR PACKET";
	public static final String NONE = "PACKET NOT DEFINED";
	
	//client networking
	public static final String CLIENT_CONNECTION_FAILURE = "Unable to connect to server.";
	public static final String RETRANSMISSION = "Retransmission retried %d times, send file considered done.";
	public static final String CLIENT_TRANSMISSION_ERROR = "Network error, could not connect to server.";
	public static final String CLIENT_TIME_OUT = "Time out occured, resending RRQ.";
	
	//TFTP networking
	public static final String TFTPNETWORKING_SOCKET_TIMEOUT = "Socket Timeout on received file! Resending Ack!";
	public static final String TFTPNETWORKING_RETRY = "Retries exceeded on last packet. Last Packet was lost. Otherside must had gotten finished with blocks.";
	public static final String TFTPNETWORKING_TIMEOUT_PACKET = "Sent a timeout packet.";
	public static final String TFTPNETWORKING_RE_TRANSMISSION = "Re-transmission retried %d times, giving up due to network error.";
	public static final String TFTPNETWORKING_RE_TRAN_SHUT_DOWN = "Retransmission retried %d times, no reply, shutting down.";
	public static final String TFTPNETWORKING_RE_TRAN_SUCCEED = "Retransmission retried %d times, transmission successful.";
	public static final String TFTPNETWORKING_TIME_OUT = "Socket Timeout on send file! Resending Data!";
	public static final String TFTPNETWORKING_LOSE_CONNECTION = "Other host no longer connected.";
	public static final String UNHANDLED_EXCEPTION = "Unhandled Exception.";
	
	// Error Simulator Server
	public static final String ERROR_SERVER_WAITING_INIT = "Waiting on timeout from client during delayed initiating packet.";
	public static final String ERROR_SERVER_WAITING_LOST =  "Waiting on timeout from client during lost first packet";
	
	// Error Simulator Service
	public static final String ERROR_SERVICE_PORT ="Initalized error sim service on port ";
	public static final String ERROR_SERVICE_ENDING ="Ending thread for a lost initiating (RRQ/WRQ) packet.";
	public static final String ERROR_SERVICE_ERROR = "Sending the first RRQ and WRQ was an issue!";
	public static final String ERROR_SERVICE_ERROR_TRANS = "Something bad happened while transfering files";
	public static final String ERROR_SERVICE_SUCCESS = "Success full simulation of last packet.";
	public static final String ERROR_SERVICE_FORWARD_CLI_ERR = "Client sent a error packet, now forwarding it to the server!";
	public static final String ERROR_SERVICE_FORWARD_CLI_ACK = "An ack packet was received from the client, forwarding to server!";
	public static final String ERROR_SERVICE_FORWARD_SER_ERR = "Server sent a error packet, now forwarding it to the client!";
	public static final String ERROR_SERVICE_FORWARD_SER_ACK = "An ack packet was received from the server, forwarding it to client!";
	public static final String ERROR_SERVICE_ERR_CLI = "Client accidently created more than 1 server thread. 2nd thread has replied with message to the client.";
	public static final String ERROR_SERVICE_ADD_SER ="Tweaked the address to go to server: ";
	public static final String ERROR_SERVICE_ADD_CLI = "Tweaked the address to go to client: ";
	public static final String ERROR_SERVICE_ADD_UNCLEAR_WRQ = "Unable to determine which entity to forward the packet on RRQ.";
	public static final String ERROR_SERVICE_ADD_UNCLEAR_RRQ = "Unable to determine which entity to forward the packet on WRQ.";
	public static final String ERROR_SERVICE_UNCLEAR = "The packet forwarded was not a RRQ or WRQ.";
	public static final String ERROR_SERVICE_ERROR_TYPE ="Not making delay error because the type we want is %s and header we compare is %s";
	public static final String ERROR_SERVICE_TOO_LONG = "Oops, you might have set your delay time for too long.";
	public static final String ERROR_SERVICE_RETRY = "Retransmission retried %d times, simulator operation considered done.";
	public static final String ERROR_SERVICE_DELAY = "Delaying error packet.";
	public static final String ERROR_SERVICE_NO_ERROR = "Not going to simulate error on this packet";
	public static final String ERROR_SERVICE_LOST ="Attempting to lose packet.";
	public static final String ERROR_SERVICE_UNSATISFICATION = "Lost first request, unable to satisfy client on this thread due it's connection was from port 68.";
	public static final String ERROR_SERVICE_SHUST_DOWN_THREAD = "After losing a packet, the thread had no replies after 3 retries. Shutting down thread.";
	public static final String ERROR_SERVICE_TIMEOUT_DELAY = "Error catch entity timeouts form both sides during a delay.";
	public static final String ERROR_SERVICE_DELAY_ATTEMP = "Attempting to delay a packet with op code %d.";
	public static final String ERROR_SERVICE_HANDLE_DELAY ="Handling delay for first packet. Awaiting for first glimpse of the server thread port.";
	public static final String ERROR_SERVICE_FIRST_CONTACT ="We have made our first contact with the server for delayed initial packet";
	
}

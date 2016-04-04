package resource;

/**
 * @author Team 3
 *
 * Collection of user interface strings used throughout the TFTP system
 */
public class UIStrings {

	public static final String MENU_ERROR_SIMULATOR_ERROR_SELECTION 
							   = "----------------------\n"
							   + "| Error Selection Menu |\n"
							   + "----------------------\n"
							   + "Please select which error to generate\n"
							   + "Options : \n"
							   + "\t 1.Illegal TFTP operation \n"
							   + "\t 2.Unknown transfer ID \n"
							   + "\t 3.No errors please \n"
							   + "\t 4.Transmission Error \n"
							   + "\t 5.Exit \n"
							   + "Select option : ";
	public static final String MENU_ERROR_SIMULATOR_ILLEGAL_TFTP_OPERATION 
							   = "----------------------\n"
			   				   + "| Illegal TFTP Operation Menu |\n"
			   				   + "----------------------\n"
			   				   + "Please select the packet type to simulate on:\n"
			   				   + "Options : \n"
			   				   + "\t 0.Go back to the Error Selection Menu \n"
			   				   + "\t 1.First packet\n"
			   				   + "\t 2.ACK packet\n"
			   				   + "\t 3.DATA packet\n"
			   				   + "\t 4.ERROR packet\n"
			   				   + "Select option : ";
	public static final String MENU_ERROR_SIMULATOR_ILLEGAL_TFTP_OPERATION_FIRST
							   = "----------------------\n"
			   				   + "| Corrupt first packet Menu |\n"
			   				   + "----------------------\n"
			   				   + "Please select which error to generate\n"
			   				   + "Options : \n"
			   				   + "\t 0.Go back to the Error Selection Menu \n"
			   				   + "\t 1.Invalid file name (WRQ/RRQ) \n"
			   				   + "\t 2.Invalid packet header during transfer \n"
			   				   + "\t 3.Invalid zero padding bytes (WRQ/RRQ) \n"
			   				   + "\t 4.Invalid mode (WRQ/RRQ) \n"
			   				   + "Select option : ";
	public static final String MENU_ERROR_SIMULATOR_ILLEGAL_TFTP_OPERATION_ACK
							   = "----------------------\n"
							   + "| Ack packet Menu |\n"
				               + "----------------------\n"
			   				   + "Please select which error to generate\n"
			   				   + "Options : \n"
			   				   + "\t 0.Go back to the Error Selection Menu \n"
			   				   + "\t 1.Invalid block number \n"
			   				   + "\t 2.Invalid packet header during transfer \n"
			   				   + "\t 3.Invalid packet size \n"
			   				   + "Select option : ";
	public static final String MENU_ERROR_SIMULATOR_ILLEGAL_TFTP_OPERATION_DATA
							   = "----------------------\n"		   
							   + "| Data packet Menu |\n"
				               + "----------------------\n"
			   				   + "Please select which error to generate\n"
			   				   + "Options : \n"
			   				   + "\t 0.Go back to the Error Selection Menu \n"
			   				   + "\t 1.Invalid block number \n"
			   				   + "\t 2.Invalid packet header during transfer \n"
			   				   + "\t 3.Invalid packet size \n"
			   				   + "Select option : ";
	public static final String MENU_ERROR_SIMULATOR_ILLEGAL_TFTP_OPERATION_ERROR
							   = "----------------------\n"		     
							   + "| Error packet Menu |\n"
				               + "----------------------\n"
			   				   + "Please select which error to generate\n"
			   				   + "Options : \n"
			   				   + "\t 0.Go back to the Error Selection Menu \n"
			   				   + "\t 1.Invalid error number \n"
			   				   + "\t 2.Invalid packet header during transfer \n"
			   				   + "Select option : ";
	
	 public static final String MENU_ERROR_SIMULATOR_UNKNOWN_TID
	 						   = "----------------------\n"		
							   + "| Unknown TID Menu |\n"
				               + "----------------------\n"
				               + "Please select the block type to corrupt\n"
			   				   + "Options : \n"
			   				   + "\t 0.Go back to the Error Selection Menu \n"
			   				   + "\t 1.Initiate on ACK packet\n"
			   				   + "\t 2.Initiate on DATA packet\n"
			   				   + "Select option : ";
	 public static final String MENU_ERROR_SIMULATOR_LOG_LEVEL 
							   = "----------------------------------\n"
			   				   + "|    Error Simulator Test Menu    |\n"
			   				   + "----------------------------------\n"
			   				   +"Please select logging level for this session\n"
			   				   + "Options : \n"
			   				   + "\t 1. Interfere with packets going to the Client\n"
			   				   + "\t 2. Interfere with packets going to the Server\n"
			   				   + "Select option : ";
	 public static final String MENU_CLIENT_SELECTION 
							   = "\n----------------------\n"
							   +"| Client Select Menu |\n"
							   +"----------------------\n"
							   +"Options : \n"
							   +"\t 1. Read File\n"
							   +"\t 2. Write File\n"
							   +"\t 3. Exit \n\n\n"
							   +"Select option : \n";
	 public static final String CLIENT_LOG_LEVEL_SELECTION 
	 						   = "-------------------------------\n"
	 						   +"| Client Select Logging Level |\n"
	 						   +"-------------------------------\n"	   
	 						   +"Options : \n"
	 						   +"\t 1. Verbose\n"
	 						   +"\t 2. Silent\n"
	 						   +"Select option : \n";
	 public static final String CLIENT_MODE 
	 						  = "--------------------------------\n"
	 						  +"| Select Client operation Mode |\n"
	 						  +"--------------------------------\n"
	 						  +"Options : \n"
	 						  +"\t 1. Normal (No Error Simulator)\n"
	 						  +"\t 2. Test (With Error Simulator)\n"
	 						  +"Select option : \n";
	 public static final String MENU_ERROR_SIMULATOR_TRANSMISSION_MENU 
	 						  = "--------------------------------\n"
	 						  +"|   Select Transmission Error    |\n"
	 						  +"--------------------------------\n"
	 						  +"Options : \n"
	 						  +"\t 0. Go back to main menu\n"
	 						  +"\t 1. Lose Packet\n"
	 						  +"\t 2. Delay Packet\n"
	 						  +"\t 3. Duplicate Packet\n"
	 						  +"Select option : \n";
	 
	 public static final String MENU_ERROR_SIMULATOR_PROMPT_FREQUENCY 
	 						  = "How frequent do you want to simulate this error? (-1 just once, otherwise other numbers mean every other ie, 2 for every 2 good packets)";
	 public static final String MENU_ERROR_SIMULATOR_PROMPT_DELAY_AMOUNT  
			 				  = "How long do you want to delay a packet for (milliseconds)?";
	 public static final String MENU_ERROR_SIMULATOR_PROMPT_AMOUNT
	 						  = "How many transmission errors do you want to create?";
	 public static final String MENU_ERROR_SIMULATOR_PROMPT_NUM_PACKET
	 						  = "Enter the block number (Please enter -1 to corrupt the RRQ/WRQ):";
	 public static final String MENU_ERROR_SIMULATOR_PROMPT_TYPE
	  						  = "Enter the op code of the packet to simulate error on:";
	 public static final String MENU_ERROR_SIMULATOR_PROMPT_BLOCK_NUMBER
	 						  = "Please enter the block number simulate error on:";
}


package testbed;

import java.util.HashSet;
import java.util.Set;

import helpers.Keyboard;
import resource.Configurations;
import resource.Strings;
import resource.UIStrings;
import types.ErrorType;
import types.InstanceType;
import types.Logger;
import resource.Tuple;

/**
 * @author Team 3
 *
 *         This class encapsulates some interface behaviors for the error
 *         simulator such as prompting the user for different error to simulate
 */
public class TFTPUserInterface {

	// by default set the log level to debug
	private static Logger logger = Logger.VERBOSE;
	private ErrorType mUserErrorOption;
	private int mUserErrorSubOption = 0;
	private int mNumPktToFkWit = 0;
	private int mSpaceOfDelay = 0;
	private int mOpCodeToMessWith = 0;
	private int mFirstSubOption = 0;
	private int mAckSubOption = 0;
	private int mDataSubOption = 0;
	private int mErrorSubOption = 0;
	private int mBlockNumber = 0;
	private int mHeaderType = 0;

	public TFTPUserInterface() {
		this.mUserErrorOption = ErrorType.NO_ERROR;
		this.mUserErrorSubOption = 0;
	}

	/**
	 * This function will determine the exact errors to simulate by prompting
	 * the user
	 * 
	 * @param instance
	 *            - to corrupt errors going to the client or going to the server
	 *            instance
	 * @return tuple - first is the error type - second is the error sub code to
	 *         produce
	 */
	public ErrorCommand getErrorCodeFromUser() {
		// this.mInstanceSelected = instance;
		int optionSelected = 0;
		boolean validInput = false;

		// Check below for how this get set
		ErrorCommand errorToProduce = new ErrorCommand();
		while (!validInput) {
			System.out.println(UIStrings.MENU_ERROR_SIMULATOR_ERROR_SELECTION);
			try {
				optionSelected = Keyboard.getInteger();
			} catch (NumberFormatException e) {
				optionSelected = 0;
			}

			switch (optionSelected) {
			case 1:
				// illegal TFTP operation option
				this.mUserErrorOption = ErrorType.ILLEGAL_OPERATION;
				while (true) {
					System.out.println(UIStrings.MENU_ERROR_SIMULATOR_ILLEGAL_TFTP_OPERATION);
					this.mUserErrorSubOption = Keyboard.getInteger();

					if (this.mUserErrorSubOption <= 4 && this.mUserErrorSubOption > -1) {	
						break;
					}
					System.out.println("Please select a valid option");
				}
				if (this.mUserErrorSubOption == 0) {
					// go back to the previous level
					validInput = false;
				} else {
					int illegalTransferType = getIllegalTFTPerrorMenu(this.mUserErrorSubOption);
					if(illegalTransferType == 0) {
						validInput = false;
					} else {
						errorToProduce.setIllegalTransferCase(illegalTransferType);
						errorToProduce.setSubErrorFromFamily(illegalTransferType);
						errorToProduce.setTransmissionErrorType(this.mHeaderType);
						errorToProduce.setSimulatedBlocknumber(this.mBlockNumber);
						validInput = true;
					}
				}
				break;
			case 2:
				// unknown transfer ID operation option
				this.mUserErrorOption = ErrorType.UNKNOWN_TRANSFER;
				while (true) {
					System.out.println(UIStrings.MENU_ERROR_SIMULATOR_UNKNOWN_TID);
					this.mUserErrorSubOption = Keyboard.getInteger();
					if (this.mUserErrorSubOption <= 3 && this.mUserErrorSubOption > -1) {
						break;
					}
					System.out.println("Please select a valid option");
				}
				if (this.mUserErrorSubOption == 0) {
					// go back to the previous level
					validInput = false;
				} else {
					int blockNumber = this.getBlocknumberPrompt();
					if(this.mUserErrorSubOption == 1) {
						// an ack was selected
						errorToProduce.setTransmissionErrorType(4);
					} else if(this.mUserErrorSubOption == 2) {
						// an data was selected
						errorToProduce.setTransmissionErrorType(3);
					}
					errorToProduce.setSimulatedBlocknumber(blockNumber);
					this.mUserErrorSubOption = 0;
					validInput = true;
				}
				break;
			case 3:
				// No error
				errorToProduce.setTransmissionErrorType(0);
				System.out.println("Alright boss.");
				validInput = true;
				this.mUserErrorOption = ErrorType.NO_ERROR;
				break;
			case 4:
				// Transmission Error
				this.mUserErrorOption = ErrorType.TRANSMISSION_ERROR;
				while (true) {
					System.out.println(UIStrings.MENU_ERROR_SIMULATOR_ILLEGAL_TFTP_OPERATION);
					//this.mUserErrorSubOption = Keyboard.getInteger();
					this.mOpCodeToMessWith = Keyboard.getInteger();
					if (this.mOpCodeToMessWith <= 4 && this.mOpCodeToMessWith > -1) {
						if(this.mOpCodeToMessWith == 1) {
							errorToProduce.setTransmissionErrorType(this.mOpCodeToMessWith);
							//errorToProduce.setTransmissionErrorOccurrences(-1); // legacy code support
							errorToProduce.setSimulatedBlocknumber(-1);
						} else if (this.mOpCodeToMessWith == 2) {
							errorToProduce.setTransmissionErrorType(4);
						} else if (this.mOpCodeToMessWith == 3) {
							errorToProduce.setTransmissionErrorType(3);
						} else if (this.mOpCodeToMessWith == 4) {
							errorToProduce.setTransmissionErrorType(5);
							errorToProduce.setSimulatedBlocknumber(-1);
						}
						break;
					}
					System.out.println("Please select a valid option");
				}
				
				if (this.mOpCodeToMessWith == 0) {
					// go back to the previous level
					this.mUserErrorSubOption = 0;
					this.mOpCodeToMessWith = 0;
					validInput = false;
				} else {
					
					while(true) {
						System.out.println(UIStrings.MENU_ERROR_SIMULATOR_TRANSMISSION_MENU);
						this.mUserErrorSubOption = Keyboard.getInteger();
						if (this.mUserErrorSubOption <= 3 && this.mUserErrorSubOption > -1) {
							errorToProduce.setSubErrorFromFamily(this.mUserErrorSubOption);
							break;
						}
					}
					if(this.mUserErrorSubOption == 0) {
						validInput = false;
					} else {
						if(this.mOpCodeToMessWith != 1 && this.mOpCodeToMessWith != 4) {
							this.mBlockNumber = this.getBlocknumberPrompt();
							errorToProduce.setSimulatedBlocknumber(this.mBlockNumber);
						}
							
						validInput = true;
						//this.getTransmissionMenu(this.mUserErrorSubOption);
						if(this.mUserErrorSubOption == 2) {
							while (true) {
								System.out.println(UIStrings.MENU_ERROR_SIMULATOR_PROMPT_DELAY_AMOUNT);
								this.mSpaceOfDelay = Keyboard.getInteger();
								if (mSpaceOfDelay > (Configurations.TRANMISSION_TIMEOUT + 50) && this.mSpaceOfDelay > -1) {
									break;
								}
								System.out.print("Invalid delay, please enter a delay that is greater than "
										+ (Configurations.TRANMISSION_TIMEOUT + 50) + "\n");
							}
						}
						
						errorToProduce.setTransmissionErrorFrequency(this.mSpaceOfDelay);
					}
				}
				break;
			case 5:
				// Exit
				System.out.println(Strings.EXIT_BYE);
				validInput = true;
				this.mUserErrorOption = ErrorType.EXIT;
				break;
			default:
				System.out.println(Strings.ERROR_INPUT);
				break;
			}
		}
		errorToProduce.setMainErrorFamily(this.mUserErrorOption.getErrorCodeShort());
		return errorToProduce;
	}

	/**
	 * Prompts the user to choose a verbose or silent option
	 * 
	 * @return
	 */
	public Logger printLoggerSelection() {
		int optionSelected = 0;
		boolean validInput = false;

		while (!validInput) {
			printSelectLogLevelMenu();

			try {
				optionSelected = Keyboard.getInteger();
			} catch (NumberFormatException e) {
				optionSelected = 0;
			}

			switch (optionSelected) {
			case 1:
				logger = Logger.VERBOSE;
				validInput = true;
				break;
			case 2:
				logger = Logger.SILENT;
				validInput = true;
				break;
			default:
				System.out.println(Strings.ERROR_INPUT);
				break;
			}
		}
		return logger;
	}

	public InstanceType printTestableProcess() {
		int optionSelected = 0;
		boolean validInput = false;
		while (!validInput) {
			printSelectLogLevelMenu();

			try {
				optionSelected = Keyboard.getInteger();
			} catch (NumberFormatException e) {
				optionSelected = 0;
			}

			switch (optionSelected) {
			case 1:
				return InstanceType.CLIENT;
			case 2:
				return InstanceType.SERVER;
			default:
				System.out.println(Strings.ERROR_INPUT);
				break;
			}
		}
		return InstanceType.SERVER;
	}

	/**
	 * Prints the most basic error sim log level
	 */
	private static void printSelectLogLevelMenu() {
		System.out.println(UIStrings.MENU_ERROR_SIMULATOR_LOG_LEVEL);
	}

	private int getBlocknumberPrompt() {
		int i = 0;
		while (true) {
			System.out.println(UIStrings.MENU_ERROR_SIMULATOR_PROMPT_BLOCK_NUMBER);
			i = Keyboard.getInteger();
			if (i > -1) {
				return i;
			}
			System.out.println("Please select a valid option");
		}
	}

	/**
	 * Gets the Illegal TFTP error menu
	 * 
	 * @param Packet
	 *            type
	 */
	private int getIllegalTFTPerrorMenu(int packetType) {
		switch (packetType) {
		case 1: // First Packet
			while (true) {
				System.out.println(UIStrings.MENU_ERROR_SIMULATOR_ILLEGAL_TFTP_OPERATION_FIRST);
				this.mFirstSubOption = Keyboard.getInteger();
				if (this.mFirstSubOption <= 4 && this.mFirstSubOption > -1) {
					this.mBlockNumber = -1;
					this.mHeaderType = 1;
					return this.mFirstSubOption;
				}
				System.out.println("Please select a valid option");
			}
		case 2:// ACK packet
			while (true) {
				System.out.println(UIStrings.MENU_ERROR_SIMULATOR_ILLEGAL_TFTP_OPERATION_ACK);
				this.mAckSubOption = Keyboard.getInteger();
				if (this.mAckSubOption <= 3 && this.mAckSubOption > -1) {
					if(this.mAckSubOption ==0) {
						return 0;
					}
					this.mBlockNumber = getBlocknumberPrompt();
					this.mHeaderType = 4;
					return this.mAckSubOption;
				}
				System.out.println("Please select a valid option");
			}
		case 3:// DATA packet
			while (true) {
				System.out.println(UIStrings.MENU_ERROR_SIMULATOR_ILLEGAL_TFTP_OPERATION_DATA);
				this.mDataSubOption = Keyboard.getInteger();
				if (this.mDataSubOption <= 3 && this.mDataSubOption > -1) {
					if(this.mDataSubOption == 0) {
						return 0;
					}
					this.mBlockNumber = getBlocknumberPrompt();
					this.mHeaderType = 3;
					return this.mDataSubOption;
				}
				System.out.println("Please select a valid option");
			}
		case 4: // ERROR packet
			while (true) {
				System.out.println(UIStrings.MENU_ERROR_SIMULATOR_ILLEGAL_TFTP_OPERATION_ERROR);
				this.mErrorSubOption = Keyboard.getInteger();
				if (this.mErrorSubOption <= 2 && this.mErrorSubOption > -1) {
					if(this.mErrorSubOption == 0) {
						return 0;
					}
					this.mBlockNumber = getBlocknumberPrompt();
					this.mHeaderType = 5;
					return this.mErrorSubOption;
				}
				System.out.println("Please select a valid option");
			}
		}
		// Flag exception
		return 0;
	}

	/**
	 * Gets the main transmission error menu
	 * 
	 * @param transmissionError
	 *            settings
	 */
	private void getTransmissionMenu(int transmissionError) {
		switch (transmissionError) {
		case 1:
			// lose packet
//			while (true) {
//				System.out.println(String.format(UIStrings.MENU_ERROR_SIMULATOR_PROMPT_NUM_PACKET, "lose"));
//				this.mNumPktToFkWit = Keyboard.getInteger();
//				// if(this.mInstanceSelected == InstanceType.SERVER ||
//				// (this.mInstanceSelected == InstanceType.CLIENT &&
//				// mNumPktToFkWit!=-1)){
//				// break;
//				// }
//				if (this.mNumPktToFkWit >= -1) {
//					break;
//				}
//				System.out.println("Please select a block that is not a RRQ/WRQ");
//			}
//
//			while (true) {
//				System.out.println(UIStrings.MENU_ERROR_SIMULATOR_PROMPT_TYPE);
//				this.mOpCodeToMessWith = Keyboard.getInteger();
//				if (checkBlockOpcode()) {
//					break;
//				}
//			}
			break;
		case 2:
			// delay packet
			// Picks i-th packet to delay
			while (true) {
				System.out.println(UIStrings.MENU_ERROR_SIMULATOR_PROMPT_NUM_PACKET);
				this.mNumPktToFkWit = Keyboard.getInteger();
				// if(this.mInstanceSelected == InstanceType.SERVER ||
				// (this.mInstanceSelected == InstanceType.CLIENT &&
				// mNumPktToFkWit!=-1)){
				// break;
				// }
				if (this.mNumPktToFkWit >= -1) {
					break;
				}
				System.out.println("Please select a block that is not a RRQ/WRQ");
			}
			// Picks the delay in milliseconds
			while (true) {
				System.out.println(UIStrings.MENU_ERROR_SIMULATOR_PROMPT_DELAY_AMOUNT);
				this.mSpaceOfDelay = Keyboard.getInteger();
				if (mSpaceOfDelay > (Configurations.TRANMISSION_TIMEOUT + 50)) {
					break;
				}
				System.out.print("The delay entered is too small. \n Please enter a delay that is greater than "
						+ (Configurations.TRANMISSION_TIMEOUT + 50) + "\n");
			}

			while (true) {
				System.out.println(UIStrings.MENU_ERROR_SIMULATOR_PROMPT_TYPE);
				this.mOpCodeToMessWith = Keyboard.getInteger();
				if (checkBlockOpcode()) {
					break;
				}
			}
			break;
		case 3:
			// duplicate
			while (true) {
				System.out.println(String.format(UIStrings.MENU_ERROR_SIMULATOR_PROMPT_NUM_PACKET, "duplicate"));
				this.mNumPktToFkWit = Keyboard.getInteger();
				// if(this.mInstanceSelected == InstanceType.SERVER ||
				// (this.mInstanceSelected == InstanceType.CLIENT &&
				// mNumPktToFkWit!=-1)){
				// break;
				// }
				if (this.mNumPktToFkWit >= -1) {
					break;
				}
				System.out.println("Please select a block that is not a RRQ/WRQ");
			}
			while (true) {
				System.out.println(UIStrings.MENU_ERROR_SIMULATOR_PROMPT_TYPE);
				this.mOpCodeToMessWith = Keyboard.getInteger();
				if (checkBlockOpcode()) {
					break;
				}
			}
			break;
		}
	}

	private boolean checkBlockOpcode() {
		// if(this.mInstanceSelected==InstanceType.SERVER && mNumPktToFkWit==-1
		// && (mOpCodeToMessWith==1 || mOpCodeToMessWith==2)){
		// return true;
		// }
		if (mNumPktToFkWit >= 0 && (mOpCodeToMessWith >= 1 && mOpCodeToMessWith <= 5)) {
			return true;
		}
		// if(this.mInstanceSelected==InstanceType.CLIENT && mNumPktToFkWit>=0
		// && (mOpCodeToMessWith>2 && mOpCodeToMessWith<=5)){
		// return true;
		// }

		System.out.println("Please enter an appropriate opcode for your selected block number\n");
		return false;
	}

	/**
	 * This function gets the user's sub-option for sub-error menu
	 * 
	 * @param s
	 *            - the string you want to prompt user
	 * @param max
	 *            - the maximum valid input
	 */
	private void getSubOption(String s, int max) {
		int subOpt;
		boolean validInput = false;
		while (!validInput) {
			// print out the message
			System.out.println(s);
			try {
				// get input
				subOpt = Keyboard.getInteger();
				if (subOpt > max) {
					System.err.println("Sorry but the client doesn't support that option.");
					validInput = false;
				} else {
					validInput = true;
				}
			} catch (NumberFormatException e) {
				subOpt = 0;
			}
			// for (int i = 1; i <= max; i++) {
			// if (subOpt == i) {
			// // validate the input
			// validInput = nonValidChoices.contains(subOpt) ? false : true;
			// // FLAG ERROR if non valid choice
			// if (!validInput) {
			// System.err.println("Sorry but the client doesn't support that.");
			// }
			// this.mUserErrorSubOption = subOpt;
			// }
			// }
		}
	}
}

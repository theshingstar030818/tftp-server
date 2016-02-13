package testbed;

import java.util.HashSet;
import java.util.Set;

import helpers.Keyboard;
import resource.Strings;
import resource.UIStrings;
import types.ErrorType;
import types.InstanceType;
import types.Logger;
import resource.Tuple;

/**
 * @author Team 3
 *
 *         This class encapsulates some interface behaviours for the error
 *         simulator such as prompting the user for different error to simulate
 */
public class TFTPUserInterface {

	// by default set the log level to debug
	private static Logger logger = Logger.VERBOSE;
	private ErrorType mUserErrorOption;
	private int mUserErrorSubOption = 0;

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
	public Tuple<ErrorType, Integer> getErrorCodeFromUser(InstanceType instance) {
		int optionSelected = 0;
		boolean validInput = false;

		while (!validInput) {
			System.out.println(UIStrings.MENU_ERROR_SIMULATOR_ERROR_SELECTION);
			try {
				optionSelected = Keyboard.getInteger();
			} catch (NumberFormatException e) {
				optionSelected = 0;
			}

			switch (optionSelected) {
			case 1:
				// file not found
				logger.print(Logger.SILENT, Strings.OPERATION_NOT_SUPPORTED);
				validInput = true;
				this.mUserErrorOption = ErrorType.FILE_NOT_FOUND;
				break;
			case 2:
				// Access violation
				logger.print(Logger.SILENT, Strings.OPERATION_NOT_SUPPORTED);
				validInput = true;
				this.mUserErrorOption = ErrorType.ACCESS_VIOLATION;
				break;
			case 3:
				// Disk full or allocation exceeded
				logger.print(Logger.SILENT, Strings.OPERATION_NOT_SUPPORTED);
				validInput = true;
				this.mUserErrorOption = ErrorType.ALLOCATION_EXCEED;
				break;
			case 4:
				// illegal TFTP operation option
				this.mUserErrorOption = ErrorType.ILLEGAL_OPERATION;
				// printIllegalTFTPOperation();
				this.getSubOption(UIStrings.MENU_ERROR_SIMULATOR_ILLEGAL_TFTP_OPERATION, 8, instance);
				if (this.mUserErrorSubOption == 8) {
					// go back to the previous level
					this.mUserErrorSubOption = 0;
					validInput = false;
				} else {
					validInput = true;
				}
				break;
			case 5:
				// unknown transfer ID operation option
				this.mUserErrorOption = ErrorType.UNKNOWN_TRANSFER;
				this.mUserErrorSubOption = 0;
				validInput = true;
				break;
			case 6:
				// File already exists
				logger.print(Logger.SILENT, Strings.OPERATION_NOT_SUPPORTED);
				validInput = true;
				this.mUserErrorOption = ErrorType.FILE_EXISTS;
				break;
			case 7:
				// No such user
				logger.print(Logger.SILENT, Strings.OPERATION_NOT_SUPPORTED);
				validInput = true;
				this.mUserErrorOption = ErrorType.NO_SUCH_USER;
				break;
			case 8:
				// No error
				System.out.println("Alright boss.");
				validInput = true;
				this.mUserErrorOption = ErrorType.NO_ERROR;
				break;
			case 9:
				// No error
				System.out.println(Strings.EXIT_BYE);
				validInput = true;
				this.mUserErrorOption = ErrorType.EXIT;
				break;
			default:
				System.out.println(Strings.ERROR_INPUT);
				break;
			}
		}
		return new Tuple<ErrorType, Integer>(this.mUserErrorOption, this.mUserErrorSubOption);
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

	private static void printSelectLogLevelMenu() {
		System.out.println(UIStrings.MENU_ERROR_SIMULATOR_LOG_LEVEL);
	}

	/**
	 * This function gets the user's sub-option for sub-error menu
	 * 
	 * @param s
	 *            - the string you want to prompt user
	 * @param max
	 *            - the maximum valid input
	 */
	private void getSubOption(String s, int max, InstanceType instance) {
		int subOpt;
		boolean validInput = false;
		Set<Integer> nonValidChoices = new HashSet<>();
		if (instance == InstanceType.CLIENT) {
			nonValidChoices.add(1);
			nonValidChoices.add(2);
			nonValidChoices.add(3);
			nonValidChoices.add(7);
		}
		while (!validInput) {
			// print out the message
			System.out.println(s);
			try {
				// get input
				subOpt = Keyboard.getInteger();
			} catch (NumberFormatException e) {
				subOpt = 0;
			}
			for (int i = 1; i <= max; i++) {
				if (subOpt == i) {
					// validate the input
					validInput = nonValidChoices.contains(subOpt) ? false : true;
					// FLAG ERROR if non valid choice
					if (!validInput) {
						System.err.println("Sorry but the client doesn't support that.");
					}
					this.mUserErrorSubOption = subOpt;
				}
			}
		}
	}
}

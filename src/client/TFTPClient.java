package client;

import java.io.File;
import java.util.Scanner;

import helpers.Keyboard;
import networking.ClientNetworking;
import resource.*;
import testbed.ErrorChecker;
import testbed.TFTPErrorMessage;
import types.*;

/**
 * @author Team 3
 *
 *         This class represents a TFTP console application for interfacing with
 */
public class TFTPClient {

	private boolean isClientAlive = true;
	private final String CLASS_TAG = "<TFTP Client>";
	private int mPortToSendTo;

	private int mode;

	// by default the logger is set to VERBOSE level
	private Logger logger = Logger.VERBOSE;

	// Error checker
	ErrorChecker errorChecker = null;

	public static void main(String[] args) {
		TFTPClient vClient = new TFTPClient();
		vClient.initialize();
	}

	/**
	 * This function initializes the client's functionality and block the rest
	 * of the program from running until a exit command was given.
	 */
	public void initialize() {
		logger.setClassTag(this.CLASS_TAG);
		Scanner scan = new Scanner(System.in);
		ClientNetworking net = null;
		try {
			mode = getSendPort();
			if (mode == 1) {
				this.mPortToSendTo = Configurations.SERVER_LISTEN_PORT;
			} else {
				this.mPortToSendTo = Configurations.ERROR_SIM_LISTEN_PORT;
			}
			setLogLevel();

			int optionSelected = 0;

			while (isClientAlive) {
				System.out.println(UIStrings.MENU_CLIENT_SELECTION);

				try {
					optionSelected = Keyboard.getInteger();
				} catch (NumberFormatException e) {
					optionSelected = 0;
				}
				errorChecker = null;
				switch (optionSelected) {
				case 1:
					// Read file
					net = new ClientNetworking();
					String readFileName;
					while(true){
						logger.print(logger, Strings.PROMPT_ENTER_FILE_NAME);
						readFileName = Keyboard.getString();
					
						if(ErrorChecker.isValidFilename(readFileName)){
							break;
						}
						System.out.println("Invalid entry. So, re-prompting\n");	
					}
						
					try {
						TFTPErrorMessage result;
						do {
							result = net.generateInitRRQ(readFileName, this.mPortToSendTo);
							if(result.getType() != ErrorType.NO_ERROR) break;
							result = net.receiveFile();
						} while(result == null);
						
						if (result.getType() != ErrorType.NO_ERROR) {
							logger.print(Logger.ERROR, Strings.TRANSFER_FAILED);
							logger.print(Logger.ERROR, result.getString());
						} else {
							logger.print(Logger.VERBOSE, Strings.TRANSFER_SUCCESSFUL);
						}
					} catch (Exception e) {
						if (logger == Logger.VERBOSE)
							e.printStackTrace();

						logger.print(Logger.ERROR, Strings.TRANSFER_FAILED);
					}
					break;
				case 2:
					// Write file
					net = new ClientNetworking();

					logger.print(logger, Strings.PROMPT_FILE_NAME_PATH);
					String writeFileNameOrFilePath = Keyboard.getString();
					
					TFTPErrorMessage result = null;
					File f = new File(writeFileNameOrFilePath);
					if (!f.exists() || f.isDirectory()) {
						logger.print(logger, Strings.FILE_NOT_EXIST);
						break;
					}
					result= net.generateInitWRQ(writeFileNameOrFilePath, this.mPortToSendTo);
					if (result == null) break;
					if ((result.getType() == ErrorType.NO_ERROR) || 
							(result.getType() == ErrorType.SORCERERS_APPRENTICE)) {
						result = net.sendFile();
						logger.print(Logger.VERBOSE, Strings.TRANSFER_SUCCESSFUL);
					} else {
						logger.print(Logger.ERROR, Strings.TRANSFER_FAILED);
						logger.print(Logger.ERROR, result.getString());
						
					}
					break;
				case 3:
					// shutdown client
					isClientAlive = !isClientAlive;
					logger.print(Logger.VERBOSE, Strings.EXIT_BYE);
					break;

				default:
					logger.print(Logger.ERROR, Strings.ERROR_INPUT);
					break;
				}
			}
		} finally {
			scan.close();
		}
	}

	/**
	 * This function sets the client to use the error simulator or not
	 * 
	 * @return mode 1 is do not use and 2 is use.
	 */
	private int getSendPort() {
		while (true) {
			System.out.println(UIStrings.CLIENT_MODE);

			int mode = Keyboard.getInteger();

			if (mode == 1) {
				return mode;
			} else if (mode == 2) {
				return mode;
			} else {
				logger.print(Logger.ERROR, Strings.ERROR_INPUT);
			}

		}
	}

	/**
	 * This function only prints the client side selection menu
	 */
	private void setLogLevel() {

		int optionSelected;

		while (true) {
			System.out.println(UIStrings.CLIENT_LOG_LEVEL_SELECTION);

			try {
				optionSelected = Keyboard.getInteger();
			} catch (NumberFormatException e) {
				optionSelected = 0;
			}

			if (optionSelected == 1) {
				this.logger = Logger.VERBOSE;
				break;
			} else if (optionSelected == 2) {
				this.logger = Logger.SILENT;
				break;
			} else {
				logger.print(Logger.ERROR, Strings.ERROR_INPUT);
			}
		}
	}
}

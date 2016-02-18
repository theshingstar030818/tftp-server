Sysc 3303 Iteration 2
=====================
Ananth Akhila     - 100894838
Chen Yike         - 100921653
Li Ziqiao Charlie - 100832579
Rana Tanzeel      - 100835165
Thompson Kyle     - 100936817

Files included
--------------
/client
    + TFTPClient.java
/helpers
    + BufferPrinter.java
    + Conversion.java
    + FileStorageService.java
    + Keyboard.java
/packet
    + AckPacket.java
    + DataPacket.java
    + ErrorPacket.java
    + Packet.java
    + PacketFactory.java
    + ReadPacket.java
    + ReadWritePacket.java
    + WritePacket.java
/resource
    + Configurations.java
    + Strings.java
    + Tuple.java
    + UIStrings.java
/server
    + Callback.java
    + TFTPServer.java
    + TFTPService.java
/testbed
    + ErrorChecker.java
    + ErrorCodeFive.java
    + ErrorCodeFour.java
    + ErrorCodeSimulator.java
    + ErrorSimulatorServer.java
    + ErrorSimulatorService.java
    + TFTPError.java
    + TFTPUserInterface.java
/types
    + ErrorType.java
    + InstanceType.java
    + Logger.java
    + ModeType.java
    + RequestType.java

Set up Instructions
-------------------
1. Extract this folder into a location on your computer
2. Open Eclipse (Mars.1) and select the workspace path
   of where you extracted the assignment
3. Open 3 console windows in Eclipse where you will see the print
   out of the three classes
4. Execute TFTPServer.java and ErrorSimulatorServer.java before you execute
   TFTPClient.java
5. Folders "/TFTP-Server-Storage-Folder" and "/TFTP-Client-Storage-Folder"
   will be created automatically under your home. This is the location where
   the resulting files will be saved under. 
   Server folder has files uploaded from the client to the server.
   Client folder has files downloaded from the server to the client
   If you're on windows it is:
   		C:\Users\<user name>\TFTP-Server-Storage-Folder
		C:\Users\<user name>\TFTP-Client-Storage-Folder
   If you're on linux it is
   	    /Users/username/TFTP-Server-Storage-Folder
   	    /Users/username/TFTP-Client-Storage-Folder

Usage Instructions (Client)
---------------------------
You'll be primarily interfacing with TDTPClient.java.
1. Select whether to run the program with the error simulator, or without.
      --------------------------------
      | Select Client operation Mode |
      --------------------------------
      Options : 
         1. Normal (No Error Simulator)
         2. Test (With Error Simulator)
2. Select whether detailed information about the program's execution should be given (Verbose) or omitted (Silent)
      -------------------------------
      | Client Select Logging Level |
      -------------------------------
      Options : 
         1. Verbose
         2. Silent
3. Enter a number to select an option will take you to a section where.
			----------------------
			| Client Select Menu |
			----------------------
			Options : 
				 1. Read File
				 2. Write File
				 3. Exit
4. Enter an absolute file path to the file you want to transfer.
   We have included some test files in the TestFiles/ directory.
   	+ 512bytes.txt
   	+ lessthan512bytex.txt
   	+ 513bytes.txt
   	+ 1025bytes.txt
5. Hit enter then you will see:
			File transfer was successful.
			
6. This will then loop back to the main menu (3). Try write/read 
   (Reading and writing the same file will overwrite your previous)
   
Usage Instructions (Error Simulator)
------------------------------------
1. Select which host (Client or Server) will have to deal with errors.
   If you want to change this option, you must restart the program.
      ----------------------------------
      |    Error Simulator Test Menu    |
      ----------------------------------
      Please select logging level for this session
      Options : 
         1. Interfere with packets going to the Client
         2. Interfere with packets going to the Server
         
2. Select which type of error to use. (Currently only 4 and 5 are available.)
      ----------------------
      | Error Selection Menu |
      ----------------------
      Please select which error to generate
      Options : 
         1.File not found 
         2.Access violation 
         3.Disk full or allocation exceeded 
         4.Illegal TFTP operation 
         5.Unknown transfer ID 
         6.File already exists 
         7.No such user (obsolete) 
         8.No errors please 
         9.Exit 
3. If 5 (Unknown Transfer ID) is selected, the Error Simulator will begin 
   listening for a new connection. Otherwise the menu for 4 (Illegal TFTP 
   Operation) will be shown.
      ----------------------
      | Illegal TFTP Operation Menu |
      ----------------------
      Please select which error to generate
      Options : 
         1.Invalid file name
         2.Invalid mode
         3.Invalid zero bytes
         4.Invalid block number
         5.Invalid packet header during transfer 
         6.Invalid packet size
         7.Invalid initiating packet
         8.Go back to the previous menu
4. After the transfer completetes or fails, press enter to be returned to 
   2 (Error Simulator Menu)

Usage Instructions (Server)
---------------------------
1. After launch, the server shutdown sequence can be initiated at any time 
   by pressing 'q' on the command line.

Responsibilities
----------------
 - Ananth Akhila:
  Worked with Kyle on UCM. Simulated unknown host error and illegal TFTP 
  operations.
    
 - Chen Yike:
	All UMLs. Simulated illegal TFTP operations. UI for error simulator.
   
 - Li Ziqiao Charlie:
	Made error simulator multithreaded. Extensive debugging on all classes.
   
 - Rana Tanzeel:
	Team leader for the week. Wrote client code. Helped with debugging.
	Made a logger for all messages.
   
 - Thompson Kyle:
	Created the error checker. Made timing diagrams. Worked with Akhila on 
  UCM diagram. Tested for errors extensively.
Sysc 3303 Assignment 1
======================
Ananth Akhila - 100894838
Chen Yike - 100921653
Li Ziqiao Charlie - 100832579
Rana Tanzeel - 100835165
Thompson Kyle - 100936817



Files included
--------------
/client
    + TFTPClient.java
/helpers
    + BufferPrinter.java
    + Conversion.java
    + FileStorageService.java
/packet
    + AckPacketBuilder.java
    + DataPacketBuilder.java
    + ErrorPacketBuilder.java
    + PacketBuilder.java
    + ReadPacketBuilder.java
    + ReadWritePacketBuilder.java
    + WritePacketBuilder.java
/resource
    + Configurations.java
    + Strings.java
/server
    + Callback.java
    + TFTPServer.java
    + TFTPService.java
/testbed
    + ErrorSimulator.java
/types
    + ModeType.java
    + RequestType.java



Set up Instructions
-------------------
1. Extract sysc3303TFTP-i1 into a location on your computer
2. Open Eclipse (Mars.1) and select the workspace path
   of where you extracted the assignment
3. Open 3 console windows in Eclipse where you will see the print
   out of the three classes
4. Execute TFTPServer.java and ErrorSimulator.java before you execute
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



Usage Instructions
------------------
You'll be primarily interfacing with TDTPClient.java. 

			----------------------
			| Client Select Menu |
			----------------------
			Options : 
				 1. Read File
				 2. Write File
				 3. Exit File

Select option : 

1. Enter a number to select an option will take you to a section where.

			Select option : 
			2
			Please enter file name or file path:

2. Enter an absolute file path to the file you want to transfer.
   We have included some test files in the TestFiles/ directory.
   	+ 512bytes.txt
   	+ lessthan512bytex.txt
   	+ 513bytes.txt
   	+ 1025bytes.txt

3. Hit enter then you will see:

			File transfer was successful.
			
4. Try write/read (Reading and writing the same file will overwrite 
   your previous)



Responsibilities
----------------
 - Ananth Akhila:
    Made custom packet functions to wrap TFTP information around the data
    in a UDP packet. Also make the client UCM.
 - Chen Yike:
	Created server side service handler to facilitate a file transfer
	between a single client. Also did the UCM for Error Simulator and UML.
 - Li Ziqiao Charlie:
	Team leader. Handled many design decisions. Managed github repository.
	Miscellaneous work on all classes providing general help and quality
	assurance to other team members.
 - Rana Tanzeel:
	Created the client class. Made UCMs for client.
 - Thompson Kyle:
	Created the main server that manages all service threads and accepts
	incoming initial requests.
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


Instructions
------------
1. Extract sysc3303TFTPi1 into a location on your computer
2. Open Eclipse (Mars.1) and select the workspace path
   of where you extracted the assignment
3. Open 3 console windows in Eclipse where you will see the print
   out of the three classes
4. Execute TFTPServer.java and ErrorSimulator.java before you execute
   TFTPClient.java
5.


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
**************************************************************************************************************
What's new? 

Transmission Errors! 

Here are some quick instructions, we understand the UI may be confusing so if the UI doesn't make sense to you, then read forward.

These are instruction for the Error Simulator UI to test all 3 transmission error possiblities

To test interfering to the server.
  Start error simulator.
  Select interfere with server.
  Select 4 (transmission error)
  Select 1 to 3 for the transmission error 
    case 1 losing a packet
      Enter a block number -1 to n 
          You cannot enter a zero in this case
          -1 means interfering with the first packet
          n mean interfering with the last
      Enter the op code 
          If you entered -1 for block number
              For client RRQ, enter 1 here
              For client WRQ, enter 2 here
          If you want an error
              Enter 5 here
          Otherwise if you enter 1 to n for block number
              For client RRQ, enter 4 here
              For client WRQ, enter 3 here

    case 2 delaying a packet
      Enter a block mumber -1 to n
          You cannot enter a zero in this case
          -1 means interfering with the first packet
          n mean interfering with the last
      Enter a delay time
          We suggest 1051 -> which measn 1051ms
      Enter the op code 
          If you entered -1 for block number
              For client RRQ, enter 1 here
              For client WRQ, enter 2 here
          If you want an error
              Enter 5 here
          Otherwise if you enter 1 to n for block number
              For client RRQ, enter 4 here
              For client WRQ, enter 3 here

    case 3 duplicating a packet
      Enter a block number -1 to n 
          You cannot enter a zero in this case
          -1 means interfering with the first packet
          n mean interfering with the last
      Enter the op code 
          If you entered -1 for block number
              For client RRQ, enter 1 here
              For client WRQ, enter 2 here
          If you want an error
              Enter 5 here
          Otherwise if you enter 1 to n for block number
              For client RRQ, enter 4 here
              For client WRQ, enter 3 here


To test interfering to the client.
  Start error simulator.
  Select interfere with client.
  Select 4 (transmission error)
  Select 1 to 3 for the transmission error 
    case 1 losing a packet
      Enter a block number 0 to n 
          0 means lose the first ACK to a RRQ 
          n mean interfering with the last
      Enter the op code 
          If you entered 0 for block number
              Enter 4 here 
          If you want an error
              Enter 5 here
          Otherwise if you enter 1 to n for block number
              For client RRQ, enter 3 here
              For client WRQ, enter 4 here

    case 2 delaying a packet
      Enter a block mumber 0 to n
          0 means delay the first ACK to a RRQ 
          n mean interfering with the last
      Enter a delay time
          We suggest 1051 -> which measn 1051ms
      Enter the op code 
          If you entered 0 for block number
              Enter 4 here 
          If you want an error
              Enter 5 here
          Otherwise if you enter 1 to n for block number
              For client RRQ, enter 3 here
              For client WRQ, enter 4 here

    case 3 duplicating a packet
      Enter a block number -1 to n 
          You cannot enter a zero in this case
          -1 means interfering with the first packet
          n mean interfering with the last
      Enter the op code 
          Enter a block number 0 to n 
          0 means lose the first ACK to a RRQ 
          n mean interfering with the last
        If you want an error
              Enter 5 here
      Enter the op code 
          If you entered 0 for block number
              Enter 4 here 
          Otherwise if you enter 1 to n for block number
              For client RRQ, enter 3 here
              For client WRQ, enter 4 here

We know there may be a UI bug some where. If enter did not work here 
  --- Press enter key to continue to menu ---
please restart the error simulator.
**************************************************************************************************************

Sysc 3303 Iteration 3
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
/Networking
    + ClientNetworking.java
    + ServerNetworking.java
    + TFTPNetworking.java
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
    + ErrorCodeSimulator.java
    + ErrorSimulatorServer.java
    + ErrorSimulatorService.java
    + TFTPErrorMessage.java
    + TFTPUserInterface.java
/testbed.errorcode
    + ErrorCodeFive.java
    + ErrorCodeFour.java
    + TransmissionConcurrentSend.java
    + TransmissionError.java
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
You'll be primarily interfacing with TFTPClient.java.
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
    + lessthan512byte.txt
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
      -----------------------------------
      |    Error Simulator Test Menu    |
      -----------------------------------
      Please select logging level for this session
      Options : 
         1. Interfere with packets going to the Client
         2. Interfere with packets going to the Server
         
2. Select which type of error to use. (Currently only 4 and 5 are available.)
      ------------------------
      | Error Selection Menu |
      ------------------------
      Please select which error to generate
      Options : 
         1. Illegal TFTP operation 
         2. Unknown transfer ID  
         3. No errors please 
         4. Transmission Error
         5. Exit 
         
3. If 4 (Transmission Error) is selected, you will be prompted to either:
        1. Lose Packet
        2. Delay Packet
        3. Duplicate Packet
        4. Go back
  - If 'Lose Packet' is selected, you will enter the block number of the packet
    you wish to lose, followed by the opcode of that packet. (To differentiate 
    between ACKs and DATAs that may have the same opcode)
  - If 'Delay Packet' is selected, you will enter the block number of the packet
    you wish to delay, followed by the length of the delay in milliseconds, then
    the opcode.
  - If 'Duplicate Packet' is selected, you will enter the block number of the
    packet you wish to duplicate, followed by the opcode of that packet.
  Note that when sending errors to client, opcodes 1 and 2 will not be available.
4. If 2 (Unknown Transfer ID) is selected, the Error Simulator will begin 
   listening for a new connection. Otherwise the menu for 4 (Illegal TFTP 
   Operation) will be shown.
      -------------------------------
      | Illegal TFTP Operation Menu |
      -------------------------------
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
5. After the transfer completetes or fails, press enter to be returned to 
   2 (Error Simulator Menu)
Note that to switch from interfering with packets going to the client to interfering
with packets going to the server and vice versa, you must quit the error simulator.



Usage Instructions (Server)
---------------------------
1. After launch, the server shutdown sequence can be initiated at any time 
   by pressing 'q' on the command line.
Responsibilities
----------------
 - Ananth Akhila:
   Tested code. Debugging. Added functionality to delete files where transfer was
   unsuccessful. Lost packet.
    
 - Chen Yike:
     Timing diagrams. UML. Mild refactoring. Debugging.
   
 - Li Ziqiao Charlie:
     Did so much it would be hard to fit it all in here.
   
 - Rana Tanzeel:
     Timing diagrams and miscellaneous testing.
   
 - Thompson Kyle:
     Refactored all networking code. Testing and extensive debugging. Duplicate packets.
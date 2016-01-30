package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

import resource.*;
import types.InstanceType;

/**
 * @author Team 3
 * 
 *	This file encapsulates all disk IO operations that is required to 
 *	read and write files. This classes should be created and destroyed 
 *	each time the client class needs to operate on one file
 */

public class FileStorageService {
	
	private String mFilePath = "";
	private String mFileName = "";
	private long mBytesProcessed = 0;
	private String mDefaultStorageFolder = "";
	
	// File utility classes
	RandomAccessFile mFile = null;
	FileChannel mFileChannel = null;

	/**
	 *  This file encapsulates all disk IO operations that is required to 
	 *	read and write files. This classes should be created and destroyed 
	 *	each time the client class needs to operate on one file
	 *
	 * @param fileName - given to initialize this class for use on one file
	 * @throws FileNotFoundException
	 */
	public FileStorageService(String fileNameOrFilePath) throws FileNotFoundException {
		this.mDefaultStorageFolder = Configurations.SERVER_ROOT_FILE_DIRECTORY;
		initializeFileServiceStorageLocation();
		initializeNewFileChannel(fileNameOrFilePath);
	}
	
	public FileStorageService(String fileNameOrFilePath, InstanceType instanceType) throws FileNotFoundException {
		
		this.mDefaultStorageFolder = instanceType == InstanceType.CLIENT ? Configurations.CLIENT_ROOT_FILE_DIRECTORY : 
			Configurations.SERVER_ROOT_FILE_DIRECTORY;
		
		initializeFileServiceStorageLocation();
		initializeNewFileChannel(fileNameOrFilePath);
	}
	
	/**
	 * This function checks if the default folder to save TFTP files exists, if 
	 * not, creates one.
	 */
	private void initializeFileServiceStorageLocation() {
		File storageDirectory = new File(this.mDefaultStorageFolder);
		if(!storageDirectory.exists()) {
			if(!storageDirectory.mkdir()) {
				// Flag error for File IO
				System.out.println(Strings.DIRECTORY_MAKE_ERROR);
			}
		}
	}
	
	/**
	 * Initializer for this class. It takes care of creating a default directory in system home
	 * if not made, also opens the file stream channels to perform non-blocking IO towards the 
	 * context file. This function will support handling multiple files only if a previous file
	 * operation has fully completed without error.
	 * 
	 * Two operation types:
	 * 	+ Given Full Path
	 * 		This class will overwrite or create the full valid file name. Client class uses this
	 * 		option to open a channel to the file they want to READ. 
	 * 		The client class should NOT give a full path if they want to WRITE because this 
	 * 		operation should write to the default Configurations.ROOT_FILE_DIRECTORY
	 * 	+ Given File Name
	 * 		This class will only search for the file name inside the Configurations.ROOT_FILE_DIRECTORY
	 * 		folder. If the file exists, it will read it, or overwrite it. 
	 * 		The client classes using this service should only give file name if they want to 
	 * 		operate on the default directory. 
	 * 
	 * @param fileName - passed in through the constructor
	 * @throws FileNotFoundException
	 */
	public void initializeNewFileChannel(String filePathOrFileName) throws FileNotFoundException {
		if(checkFileNameExists(filePathOrFileName)) {
			this.mFileName = Paths.get(filePathOrFileName).getFileName().toString();
			if(this.mFileName == "") {
				// No filename in the path!
				throw new FileNotFoundException();
			}
			this.mFilePath = Paths.get(filePathOrFileName).toString();
		} else {
			// So its not a file path, maybe its a file name, so we try it out
			this.mFileName = filePathOrFileName;
			this.mFilePath = Paths.get(this.mDefaultStorageFolder, this.mFileName).toString();
		}
		this.mBytesProcessed = 0;
		// Open or create a our file name path and create a channel for us to access the file on
		this.mFile = new RandomAccessFile(this.mFilePath, "rw");
		this.mFileChannel = this.mFile.getChannel();
		try {
			System.out.println("Opened a channel for a " + this.mFile.length() + " bytes long.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This function will save the byte buffer given by the TFTPPacket message segment and write
	 * each block into disk. It remembers where the last segment left off and will return false
	 * when the operation is done. It will return true if it thinks there is more buffer to write.
	 * In such case, the server is meant to be getting a fileBuffer lengthed zero to terminate.
	 * 
	 * @param fileBuffer - 512 bytes of file content sent over in the TFTPPacket
	 * @return boolean - if the file has been fully saved or not
	 */
	public boolean saveFileByteBufferToDisk(byte[] fileBuffer) {
		if(fileBuffer == null) {
			// We know that the last packet is an empty packet (512 byte case)
			try {
				this.mFileChannel.force(false);
				this.mFileChannel.close();
				this.mFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		int bytesWritten = 0;
		// Try to write the bytes to disk by wrapping byte[] into a ByteBuffer
		try {
			ByteBuffer wrappedBuffer = ByteBuffer.wrap(fileBuffer);
			while(wrappedBuffer.hasRemaining()) {
				bytesWritten += this.mFileChannel.write(wrappedBuffer, this.mBytesProcessed);
			}
		} catch (IOException e) {
			System.out.println(Strings.FILE_WRITE_ERROR + " " + this.mFileName);
			e.printStackTrace();
			return false;
		}
		// Increment processed, next round, continue where we left off
		this.mBytesProcessed += bytesWritten;
		
		// Check if we received a length zero
		if(bytesWritten < Configurations.MAX_BUFFER) {
			System.out.println(Strings.FILE_WRITE_COMPLETE);
			try {
				// Force the changes into disk, without force(false) we would write 
				// the last block with nulls.
				this.mFileChannel.force(false);
				this.mFileChannel.close();
				this.mFile.close();
			} catch (IOException e) {
				System.out.println(Strings.FILE_CHANNEL_CLOSE_ERROR);
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
	
	/**
	 * This function fills an array of bytes with 512 bytes of file content. The function remember the
	 * last position it left off so it may resume from that index when called again. The function 
	 * will return false, where there is no more file to be read and true when there is still more.
	 * Make sure this function ends before re-using this class
	 * 
	 * @param inByteBufferToFile - an initialized empty byte array sized 512 bytes
	 * @return boolean - if there is or is not any more file content to buffer 
	 */
	public byte[] getFileByteBufferFromDisk() {
		ByteBuffer fileBuffer = ByteBuffer.allocate(Configurations.MAX_BUFFER);
		int bytesRead = 0;
		try {
			bytesRead = this.mFileChannel.read(fileBuffer, this.mBytesProcessed);
		} catch (IOException e) {
			// An error will occur if the file is corrupt. We need to deal with it
			System.out.println(Strings.FILE_READ_ERROR + " " + this.mFileName);
			e.printStackTrace();
			return null;
		}
		
		// Increment the total number number of bytes processed
		this.mBytesProcessed += bytesRead;
		// We determine if we reached the end of the file
		if(bytesRead < Configurations.MAX_BUFFER) {
			// We found the end of the file, or something bad happened where we cannot
			// read anymore of the file
			System.out.println(Strings.FILE_WRITE_COMPLETE);
			byte[] lastBlock = null;
			if(bytesRead != -1) {
				// This function will return NULL if the last block is 0 bytes read
				lastBlock = new byte[bytesRead];
				System.arraycopy(fileBuffer.array(), 0, lastBlock, 0, bytesRead);
			}
			
			try {
				this.mFileChannel.close();
				this.mFile.close();
			} catch (IOException e) {
				System.out.println(Strings.FILE_CHANNEL_CLOSE_ERROR);
				e.printStackTrace();
			}
			return lastBlock;
		}
		return fileBuffer.array();
	}
	
	/**
	 * Static method that can be used to check if a file exists within the TFTP system 
	 * 
	 * @param fileName 
	 * @return boolean - if the file exists and is not a directory
	 */
	public static boolean checkFileNameExists(String filePathName) {
		String filePath = Paths.get(filePathName).toString();
		File fileToCheck = new File(filePath);
		return fileToCheck.exists() && !fileToCheck.isDirectory();
	}
	
	/**
	 * Gets the current filename that the channel is opened to.
	 * 
	 * @return string - filename 
	 */
	public String getFileName() {
		return this.mFileName;
	}
	
	/**
	 * This function should be called if you want to use the same instance of this class in
	 * the event that any error occurred during writing or reading of a file. 
	 * This function will close the current broken channels and the client class must call
	 * initializeNewFileChannel(String) to reset the file channels
	 */
	public void finishedTransferingFile() {
		try {
			if(this.mFileChannel.isOpen()) {
				this.mFileChannel.close();
			}
			this.mFile.close();		
			this.mFile = null;
			this.mFileChannel = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
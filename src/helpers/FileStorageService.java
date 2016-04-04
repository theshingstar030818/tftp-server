package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Paths;
import resource.*;
import testbed.TFTPErrorMessage;
import types.DirectoryAccessViolationException;
import types.DiskFullException;
import types.InstanceType;
import types.RequestType;

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
	private TFTPErrorMessage mLastMessage = null;

	// File utility classes
	RandomAccessFile mFile = null;
	FileChannel mFileChannel = null;
	FileLock mFileLock = null;

	/**
	 *  This file encapsulates all disk IO operations that is required to 
	 *	read and write files. This classes should be created and destroyed 
	 *	each time the client class needs to operate on one file
	 *
	 * @param fileName - given to initialize this class for use on one file
	 * @throws IOException 
	 */
	public FileStorageService(String fileNameOrFilePath) throws IOException, DirectoryAccessViolationException {
		this.mDefaultStorageFolder = Configurations.SERVER_ROOT_FILE_DIRECTORY;
		initializeFileServiceStorageLocation();
		initializeNewFileChannel(fileNameOrFilePath);
	}
	
	/**
	 * This file encapsulates all disk IO operations that is required to 
	 * read and write files. This classes should be created and destroyed 
	 * each time the client class needs to operate on one file.
	 * 
	 * This function differs as it is primarily used for further customization of
	 * client or server configurations
	 *
	 * @param fileNameOrFilePath - given to initialize this class for use on one file
	 * @param instanceType	     - client or server
	 * @throws IOException 
	 */
	public FileStorageService(String fileNameOrFilePath, InstanceType instanceType, RequestType requestType) 
			throws IOException, DirectoryAccessViolationException {
		
		this.mDefaultStorageFolder = instanceType == InstanceType.CLIENT ? Configurations.CLIENT_ROOT_FILE_DIRECTORY : 
			Configurations.SERVER_ROOT_FILE_DIRECTORY;
		
		initializeFileServiceStorageLocation();
		
		//if client side initialized the transfer check if file already exists if so append file name with
		if(instanceType == InstanceType.CLIENT && requestType == RequestType.RRQ){
			fileNameOrFilePath = incrementFileName(fileNameOrFilePath);
		}
		
		initializeNewFileChannel(fileNameOrFilePath);
	}
	
	private String incrementFileName(String fileNameOrFilePath) throws FileNotFoundException{
		
		int index = 1;
		String tmpFileName = "";
		String tmpFilePath = "";
		
		if(checkFileNameExists(fileNameOrFilePath)) {
			tmpFileName = Paths.get(fileNameOrFilePath).getFileName().toString();
			if(tmpFileName == "") {
				// No filename in the path!
				throw new FileNotFoundException();
			}
			tmpFilePath = Paths.get(fileNameOrFilePath).toString();
		} else {
			if(fileNameOrFilePath == null || fileNameOrFilePath.isEmpty())
				throw new FileNotFoundException();
			// So its not a file path, maybe its a file name, so we try it out
			tmpFileName = fileNameOrFilePath;
			tmpFilePath = Paths.get(this.mDefaultStorageFolder, tmpFileName).toString();	
		}
		boolean fileExists = true;
		while(fileExists){
			File f = new File(tmpFilePath);
			if(f.exists() && !f.isDirectory()) { 
			    // do something
				
				String[] fileNameAndExtension = tmpFileName.split("\\.(?=[^\\.]+$)");
				String fileNameString = fileNameAndExtension[0];
				String extString = fileNameAndExtension[1];
				
				tmpFilePath = Paths.get(this.mDefaultStorageFolder, fileNameString+"("+index+")"+"."+extString).toString();
				index++;
			}else{
				fileExists = false;
			}
		}
		tmpFileName = Paths.get(tmpFilePath).getFileName().toString();
		return tmpFileName;
	}
	
	/**
	 * This function checks if the default folder to save TFTP files exists, if 
	 * not, creates one.
	 */
	private void initializeFileServiceStorageLocation() throws DirectoryAccessViolationException {
		File storageDirectory = new File(this.mDefaultStorageFolder);
		if(!storageDirectory.exists()) {
			if(!storageDirectory.mkdir()) {
				// Flag error for File IO
				System.out.println(Strings.DIRECTORY_MAKE_ERROR);
				throw new DirectoryAccessViolationException("Access denied");
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
	 * @throws IOException 
	 */
	public void initializeNewFileChannel(String filePathOrFileName) throws IOException{
		if(checkFileNameExists(filePathOrFileName)) {
			this.mFileName = Paths.get(filePathOrFileName).getFileName().toString();
			if(this.mFileName == "") {
				// No filename in the path!
				throw new FileNotFoundException();
			}
			this.mFilePath = Paths.get(filePathOrFileName).toString();
		} else {
			if(filePathOrFileName == null || filePathOrFileName.isEmpty())
				throw new FileNotFoundException();
			// So its not a file path, maybe its a file name, so we try it out
			this.mFileName = filePathOrFileName;
			this.mFilePath = Paths.get(this.mDefaultStorageFolder, this.mFileName).toString();
		}
		this.mBytesProcessed = 0;

		try {
			this.mFile = new RandomAccessFile(this.mFilePath, "rw");
			this.mFileChannel = this.mFile.getChannel();

			System.out.println("Opened a channel for a " + this.mFile.length() + " bytes long.");
		} catch (IOException e) {
			
			if(e.getMessage().contains("No such file or directory")) {
				throw new FileNotFoundException();
			}

			if(e.getMessage().contains("Access is denied")) {
				throw new AccessDeniedException(String.format(Strings.ACCESS_VIOLATION_FILE, this.mFileName));
			}
			// Flag file not found here as well!!!
			this.finishedTransferingFile();
		}
	}
	
	/**
	 * This function will save the byte buffer given by the TFTPPacket message segment and write
	 * each block into disk. It remembers where the last segment left off and will return false
	 * when the operation is done. It will return true if it thinks there is more buffer to write.
	 * In such case, the server is meant to be getting a fileBuffer length zero to terminate.
	 * 
	 * @param fileBuffer - 512 bytes of file content sent over in the TFTPPacket
	 * @return boolean - if the file has been fully saved or not
	 * @throws DiskFullException 
	 */
	public boolean saveFileByteBufferToDisk(byte[] fileBuffer) throws DiskFullException {
		if(fileBuffer == null) {
			// We know that the last packet is an empty packet (512 byte case)
			try {
				if(this.mFileLock != null) {
					this.mFileLock.release();
				}
				this.mFileLock  = null;
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
			if (e.getMessage().contains("space")) { // weak i know but hopefully this is only temporary.
				throw new DiskFullException("Attempted allocation exceeds remaining disk space. ("+ new File(this.mFilePath).getFreeSpace() +" remaining)");
			}
			return false;
		}
		// Increment processed, next round, continue where we left off
		this.mBytesProcessed += bytesWritten;
		
		// Check if we received a length zero
		if(bytesWritten < Configurations.MAX_PAYLOAD_BUFFER) {
			System.out.println(Strings.FILE_WRITE_COMPLETE);
			try {
				// Force the changes into disk, without force(false) we would write 
				// the last block with nulls.
				if(this.mFileLock != null) {
					this.mFileLock.release();
				}
				this.mFileLock  = null;
				this.mFileChannel.force(false);
				this.mFileChannel.close();
				this.mFile.close();
			} catch (IOException e) {
				System.out.println(Strings.FILE_CHANNEL_CLOSE_ERROR);
				e.printStackTrace();
			} finally {
				
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
	public byte[] getFileByteBufferFromDisk() throws AccessDeniedException {
		ByteBuffer fileBuffer = ByteBuffer.allocate(Configurations.MAX_PAYLOAD_BUFFER);
		int bytesRead = 0;
		try {
			bytesRead = this.mFileChannel.read(fileBuffer, this.mBytesProcessed);
		} catch (OverlappingFileLockException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// An error will occur if the file is corrupt. We need to deal with it
			System.out.println(Strings.FILE_READ_ERROR + " " + this.mFileName);
			//e.printStackTrace();
			if(e.getMessage().contains("The process cannot access the file because another process has locked a portion of the file")) {
				throw new AccessDeniedException("The file you're currently trying to read is in the process of being written. Please try again later.");
			}
			this.finishedTransferingFile();
			return null;
		}
		
		// Increment the total number number of bytes processed
		this.mBytesProcessed += bytesRead;
		// We determine if we reached the end of the file
		if(bytesRead < Configurations.MAX_PAYLOAD_BUFFER) {
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
		boolean exists = fileToCheck.exists() && !fileToCheck.isDirectory();
		return exists;
	}
	
	/**
	 * Gets the current filename that the channel is opened to.
	 * 
	 * @return string - filename 
	 */
	public String getFileName() {
		return this.mFileName;
	}
	
	public boolean lockFile() {
		if(this.mFileLock != null) return false; 
		try {
			this.mFileLock = this.mFileChannel.tryLock();
			System.out.println("Got the lock");
		} catch(OverlappingFileLockException e) {
			System.err.println(e.getMessage());
			
			while(true) {
				System.err.println(e.getMessage());
			}
			
		} catch (IOException e) {
		
			e.printStackTrace();
			return false;
		} finally {
			
		}
		return true;
	}
	
	/**
	 * This function should be called if you want to use the same instance of this class in
	 * the event that any error occurred during writing or reading of a file. 
	 * This function will close the current broken channels and the client class must call
	 * initializeNewFileChannel(String) to reset the file channels
	 */
	public void finishedTransferingFile() {
		try {
			if(this.mFileLock != null) {
				this.mFileLock.release();
			}
			this.mFileLock  = null;
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
	
	/** Deletes file from disk*/
	public void deleteFileFromDisk(){
		this.finishedTransferingFile();
		File f = new File(this.mFilePath);
		System.out.println(Strings.DELETE_FILE +this.mFilePath);
		if(f.exists()) {
			f.delete();
		} else {
			System.err.println("Tried to delete a file that does not exist.");
		}
	}
}
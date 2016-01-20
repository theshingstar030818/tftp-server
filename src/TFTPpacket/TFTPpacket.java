package TFTPpacket;

import java.util.ArrayList;

public class TFTPpacket {
	public enum Opcode{RRQ,WRQ,DATA,ACK,ERROR}; 
	private ArrayList<Byte> header = new ArrayList<Byte>();
	private static String filename;
	private static String mode;
	
	private static String message;
	private static byte[] messageBuffer;
	private static short blockNumber;
	
	private byte[] getPacketHeader(Opcode op){
		//Not sure if the hashcode of the enumeration is supposed to be used here... 
		//The TFTP op code is supposed to be 2 bytes...
		switch(op){
			case RRQ:{ 
				header.add((byte)op.RRQ.hashCode());
				break;
			}
			case WRQ:{
				header.add((byte)op.WRQ.hashCode());
				break;
			}
			case DATA:{
				header.add((byte)op.DATA.hashCode());
				break;
			}
			case ACK:{
				header.add((byte)op.ACK.hashCode());
				break;
			}
			case ERROR:{
				header.add((byte)op.ERROR.hashCode());
				break;
			}
		}//end of switch statements
			for(byte b:filename.getBytes()){
				header.add(b);
			}//end of for loop
			
			header.add((byte)0);
			
			for(byte b:mode.getBytes()){
				header.add(b);
			}//end of for loop
			
			header.add((byte)0);
			
			return(toByteArray(header));
	}//end of getPacketHeader
	
	private void buildTFTPPacket(){
		
	}//end of buildTFTPPacket
	
	private byte[] toByteArray(ArrayList<Byte> msg){
		byte bArray[] = new byte[msg.size()];
		for(int i=0; i<msg.size(); i++){
			bArray[i] = msg.get(i);
		}//end of for loop
		return bArray;
	}//end of toByteArray
}// end of TFTPPacket class

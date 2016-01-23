package TFTPpacket;

import java.net.*;
import java.util.ArrayList;
import types.RequestType;

public class packetBuilder{
	private static RequestType rt;
	private static String filename;
	private static String mode;
	private static InetAddress iAdd;
	private static int destPort;
	private DatagramPacket pkt;

	
	public packetBuilder(RequestType rt,String filename,String mode, InetAddress iAdd, int destPort ){
		this.rt = rt;
		this.filename = filename;
		this.mode = mode;
		this.iAdd = iAdd;
		this.destPort = destPort;
	}
	
	public packetBuilder(DatagramPacket dPkt){
		
	}
	
	public DatagramPacket getPacket(){
		byte[] buffer = createMsg(getRequestType(rt),filename,mode);
		pkt = new DatagramPacket(buffer, buffer.length,iAdd, destPort);
		return pkt;
	}
	//Gets opcode byte array from the request type
	public byte[] getRequestType(RequestType rt){
			byte[] opcode = {0,0}; 
			if(rt == RequestType.RRQ){ 
				opcode = rt.RRQ.getHeaderByteArray();
			}
			if(rt == RequestType.WRQ){ 
				opcode = rt.WRQ.getHeaderByteArray();
			}
			if(rt == RequestType.DATA){ 
				opcode = rt.DATA.getHeaderByteArray();
			}
			if(rt == RequestType.ACK){ 
				opcode = rt.ACK.getHeaderByteArray();
			}
			if(rt == RequestType.ERROR){ 
				opcode = rt.ERROR.getHeaderByteArray();
			}
		return opcode;
	}
	
	//Creates message that needs to be put in the packet
	private byte[] createMsg(byte[] opcode, String filename, String mode){
		ArrayList<Byte> msg = new ArrayList<Byte>();
		for(byte b:opcode){
			msg.add(b);
		}
		for(byte b:filename.getBytes()){
			msg.add(b);
		}
		for(byte b:mode.getBytes()){
			msg.add(b);
		}
		return toByteArray(msg);
	}
	//Converts Byte arraylist to a byte array
	private byte[] toByteArray(ArrayList<Byte> msg){
			
			byte bArray[] = new byte[msg.size()];
			for(int i=0; i<msg.size(); i++){
				bArray[i] = msg.get(i);
			}
			return bArray;
		}
	
}// end of TFTPPacket class

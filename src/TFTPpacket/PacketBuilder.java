package TFTPpacket;

import java.net.*;
import java.util.ArrayList;
import types.RequestType;

public class PacketBuilder{
	private RequestType rt;
	private String filename;
	private String mode;
	private InetAddress iAdd;
	private int destPort;
	private DatagramPacket pkt;
	private byte[] pktInfo;
	
	
	public PacketBuilder(RequestType rt,String filename,String mode, InetAddress iAdd, int destPort ){
		this.rt = rt;
		this.filename = filename;
		this.mode = mode;
		this.iAdd = iAdd;
		this.destPort = destPort;
	}
	
	public PacketBuilder(DatagramPacket dPkt){
		this.pkt = dPkt;
		pktInfo = dPkt.getData();
		setFilename(pktInfo);
		setRequestType(getOpcode(pktInfo));
		setMode(pktInfo);
	}
	
	//Builds and returns the message in a datagram
	public DatagramPacket getPacket(){
		byte[] buffer = createMsg(getRequestTypeArray(rt),filename,mode);
		pkt = new DatagramPacket(buffer, buffer.length,iAdd, destPort);
		return pkt;
	}
	//Gets opcode byte array from the request type
	private byte[] getRequestTypeArray(RequestType rt){
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
		msg.add((byte)0);
		for(byte b:mode.getBytes()){
			msg.add(b);
		}
		msg.add((byte)0);
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
	
	// gets opcode byte array of the inputed packet
	private byte[] getOpcode(byte[] info){
		ArrayList<Byte> opcodeLst = new ArrayList<Byte>();
		opcodeLst.add(info[0]);
		opcodeLst.add(info[1]);
		return(toByteArray(opcodeLst));
	}
	
	//sets request type rt to the request type of the inputed file
	private void setRequestType(byte[] opcode){
		if(opcode[1]==1){
			this.rt = RequestType.RRQ;
		}
		if(opcode[1]==2){
			this.rt = RequestType.WRQ;
		}
		if(opcode[1]==3){
			this.rt = RequestType.DATA;
		}
		if(opcode[1]==4){
			this.rt = RequestType.ACK;
		}
		if(opcode[1]==5){
			this.rt = RequestType.ERROR;
		}
	}
	
	//return request type
	public RequestType getRequestType(){
		return this.rt;
	}
	
	// sets filename to the filename of the input packet info
	private void setFilename(byte[] info){
		ArrayList<Byte> fileN = new ArrayList<Byte>();
		for(int i=2; info[i]!=((byte)0); i++){
			fileN.add(info[i]);
		}
		this.filename = new String(toByteArray(fileN));
	}
	
	//returns filename
	public String getFilename(){
		return(this.filename);
	}
	
	//sets mode to the mode of the inputed packet info
	private void setMode(byte[] info){
		ArrayList<Byte> fileMode = new ArrayList<Byte>();
		for(int i = (info.length-2); info[i]!=0;i--){
			fileMode.add(info[i]);
		}
		this.mode = new StringBuffer(new String(toByteArray(fileMode))).reverse().toString();	
		}
	
	//returns mode
	public String getMode(){
		return this.mode;
	}
	
}// end of TFTPPacket class

package testbed;

import java.io.IOException;
import java.net.*;

import resource.Configurations;

public class ErrorCodeFive implements Runnable{
	private int packetCount = 0;
	private DatagramPacket sendPacket;
	private DatagramSocket errorSocket;
	private InetAddress clientAddress;	
	
	public ErrorCodeFive (DatagramPacket sendPacket){
		this.sendPacket = sendPacket;
		packetCount++;
		setClientAddress();
	}
	private void setClientAddress(){
		if(packetCount == 1){
			clientAddress = sendPacket.getAddress();
		}
	}
		
	private boolean checkToCreatErrorSocket(){
		if(sendPacket.getAddress() == clientAddress && packetCount>=3){
			return true;
		}
		return false;
	}
	
	private DatagramSocket createErrorSocket(){
		 errorSocket = null;
		try{
			errorSocket = new DatagramSocket();
		}
		catch (SocketException se){   
	         se.printStackTrace();
	         System.exit(1);
	      }
		 return errorSocket;
	}
	
	public void run(){
		while(checkToCreatErrorSocket()){
			DatagramSocket newSocket = createErrorSocket();
			 try {
				 newSocket.send(sendPacket);
		      } 
		     catch (IOException e) {
		         e.printStackTrace();
		         System.exit(1);
		      }
			 //receive error message from server
			 byte data[] = new byte[Configurations.MAX_BUFFER];
		      DatagramPacket receivePacket = new DatagramPacket(data, data.length);
		      try { 
		    	  newSocket.receive(receivePacket);
		    	  System.err.println("Error packet received from server");
		       } 
		      catch(IOException e) { 
		          e.printStackTrace();
		          System.exit(1);
		       }
		}
		if(!checkToCreatErrorSocket()){
			System.out.println("The recieved packet is not sent through an error socket");}
		
	}
	

}

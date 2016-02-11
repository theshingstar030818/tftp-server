package testbed;

import java.io.IOException;
import java.net.*;

public class ErrorCodeFive {
	private int packetCount = 0;
	private DatagramPacket receivePacket;
	private DatagramSocket errorSocket;
	private InetAddress clientAddress;
	
	
	public ErrorCodeFive (DatagramPacket receivePacket){
		this.receivePacket = receivePacket;
		packetCount++;
		setClientAddress();
	}
	private void setClientAddress(){
		if(packetCount == 1){
			clientAddress = receivePacket.getAddress();
		}
	}
		
	private boolean checkToCreatErrorSocket(){
		if(receivePacket.getAddress() == clientAddress && packetCount==3){
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
	
	public void sendThroughErrorSocket(){
		if(checkToCreatErrorSocket()){
			 try {
				 createErrorSocket().send(receivePacket);
		      } 
		     catch (IOException e) {
		         e.printStackTrace();
		         System.exit(1);
		      }
		}
		else{
			System.out.println("The recieved packet is not sent through an error socket");
		}
	}

}

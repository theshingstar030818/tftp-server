package testbed;

import java.net.*;

import packet.PacketBuilder;

public class ErrorCodeSimulator {
private int errCode;
private PacketBuilder receivePacketBuilder;

public ErrorCodeSimulator(DatagramPacket receivePacket, int errCode, int subErrCode){
	this.errCode = errCode;
//	this.receivePacketBuilder = new PacketBuilder(receivePacket);
	
	switch(errCode){
	case 1: errorCodeOne();
			break;
	case 2: errorCodeTwo();
			break;
	case 3: errorCodeThree();
			break;
	case 4: errorCodeFour(subErrCode);
			break;
	case 5: errorCodeFive();
			break;
	case 6: errorCodeSix();
			break;
	case 7: errorCodeSeven();
			break;

	}
	}
private void errorCodeOne(){
	
}
private void errorCodeTwo(){
	
}
private void errorCodeThree(){
	
}
private void errorCodeFour(int sub){
	if(sub==1){
		
		
	}
	if(sub==2){
		
	}
	if(sub==3){
		
	}
	if(sub==4){
		
	}
	
}
private void errorCodeFive(){
	
}
private void errorCodeSix(){
	
}
private void errorCodeSeven(){
	
}
private void errorCodeEight(){
}

}


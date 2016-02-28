package networking;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import helpers.BufferPrinter;
import helpers.FileStorageService;
import packet.AckPacket;
import packet.ReadWritePacket;
import resource.Strings;
import testbed.TFTPErrorMessage;
import types.ErrorType;
import types.InstanceType;
import types.Logger;
import types.RequestType;

public class ServerNetworking extends TFTPNetworking {

	public ServerNetworking() {
		super();
	}

	public ServerNetworking(ReadWritePacket p) {
		super(p);
	}

	public ServerNetworking(ReadWritePacket p, DatagramSocket s) {
		super(p, s);
	}

	public TFTPErrorMessage handleInitWRQ(ReadWritePacket wrq) {

		fileName = wrq.getFilename();
		try {
			storage = new FileStorageService(fileName, InstanceType.SERVER);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		TFTPErrorMessage error = errorChecker.check(wrq, RequestType.WRQ);
		errorChecker.incrementExpectedBlockNumber(); // Could be so wrong.

		if (error.getType() != ErrorType.NO_ERROR)
			if (errorHandle(error, wrq.getPacket()))
				return error;

		AckPacket vAckPacket = new AckPacket(wrq.getPacket());
		DatagramPacket vSendPacket = vAckPacket.buildPacket();

		logger.print(Logger.VERBOSE, Strings.SENDING);
		BufferPrinter.printPacket(new AckPacket(vSendPacket), Logger.VERBOSE, RequestType.ACK);
		
		try {
			socket.send(vSendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.lastPacket = vSendPacket;
		return new TFTPErrorMessage(ErrorType.NO_ERROR, Strings.NO_ERROR);
	}

	public TFTPErrorMessage handleInitRRQ(ReadWritePacket rrq) {

		fileName = rrq.getFilename();
		try {
			storage = new FileStorageService(fileName, InstanceType.SERVER);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		TFTPErrorMessage error = errorChecker.check(rrq, RequestType.RRQ);
		if (error.getType() != ErrorType.NO_ERROR)
			if (errorHandle(error, rrq.getPacket()))
				return error;
		errorChecker.incrementExpectedBlockNumber();

		return new TFTPErrorMessage(ErrorType.NO_ERROR, Strings.NO_ERROR);
	}

}

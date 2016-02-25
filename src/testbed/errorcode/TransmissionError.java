package testbed.errorcode;

import java.net.DatagramPacket;

import resource.Configurations;
import testbed.ErrorSimulatorService;

/**
 * @author Team 3
 *
 *	The idea behind this class is to have this thread freeze for a given
 *	amount of time before synchronizing with the service class and adding the 
 *	packet back into the work queue
 */	
public class TransmissionError implements Runnable {
	
	protected DatagramPacket mPacket;
	protected ErrorSimulatorService mActiveMonitor;
	
	public TransmissionError(DatagramPacket inPacket, ErrorSimulatorService monitor) {
		this.mPacket = inPacket;
		this.mActiveMonitor = monitor;
	}

	@Override
	public void run() {
		try {
			Thread.sleep((long)( (Configurations.TRANMISSION_TIMEOUT * 1.5)));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		synchronized(this.mActiveMonitor) {
			this.mActiveMonitor.addWorkToFrontOfQueue(this.mPacket);
		}
	}

}

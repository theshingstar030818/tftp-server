package testbed.errorcode;

import java.net.DatagramPacket;

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
	protected int mFrozenMillis;
	
	/**
	 * Construct this class with basic parameters needed
	 * 
	 * @param inPacket
	 * @param ms
	 * @param monitor
	 */
	public TransmissionError(DatagramPacket inPacket, int ms, ErrorSimulatorService monitor) {
		this.mPacket = inPacket;
		this.mActiveMonitor = monitor;
		this.mFrozenMillis = ms;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			System.err.println(String.format("Delaying a packet for %d ms", (long)this.mFrozenMillis));
			Thread.sleep((long)this.mFrozenMillis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		synchronized(this.mActiveMonitor) {
			this.mActiveMonitor.addWorkToFrontOfQueue(this.mPacket);
		}
	}

}

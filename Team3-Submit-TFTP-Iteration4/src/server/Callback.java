package server;

/**
 * @author Team 3
 * 
 * A callback implementable by any thread to have the ability to contact back to initiator
 */
public interface Callback {
	/**
	 * Calls back to TFTPServer with a thread id telling the server
	 * to close this thread.
	 * 
	 * @param id The thread identifier
	 */
	void callback(long id);
}

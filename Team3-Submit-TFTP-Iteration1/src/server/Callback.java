package server;

public interface Callback {
	/**
	 * Calls back to TFTPServer with a thread id telling the server
	 * to close this thread.
	 * 
	 * @param id The thread identifier
	 */
	void callback(long id);
}

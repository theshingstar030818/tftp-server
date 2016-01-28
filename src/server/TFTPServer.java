/**
 * 
 */
package server;

/**
 * @author Team 3
 *
 */
public class TFTPServer {
	
	public static void main(String[] args) {
		TFTPServerListener listener = new TFTPServerListener();
		listener.start();
	}
}

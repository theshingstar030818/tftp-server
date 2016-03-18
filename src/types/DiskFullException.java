package types;

public class DiskFullException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public DiskFullException(String message) {
		super(message);
	}
	
}

package testbed;

import types.ErrorType;

public class TFTPError {
	private ErrorType type;
	private String message;
	
	public TFTPError(ErrorType t, String m) {
		type = t;
		message = m;
	}
	
	public ErrorType getType() {
		return type;
	}
	
	public String getString() {
		return message;
	}
}

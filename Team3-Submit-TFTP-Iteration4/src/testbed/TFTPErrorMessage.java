package testbed;

import types.ErrorType;

/**
 * @author Team 3
 *
 *         This class will encapsulate a message and error code to pass around
 *         as message
 */
public class TFTPErrorMessage {
	private ErrorType type;
	private String message;

	public TFTPErrorMessage(ErrorType t, String m) {
		type = t;
		message = m;
	}

	/**
	 * Returns the error type of this message
	 * 
	 * @return ErrorType
	 */
	public ErrorType getType() {
		return type;
	}

	/**
	 * Returns the message string
	 * 
	 * @return String
	 */
	public String getString() {
		return message;
	}

	/**
	 * Sets the message in case we want to override
	 * 
	 * @param string
	 *            to set as
	 */
	public void setString(String s) {
		this.message = s;
	}
}

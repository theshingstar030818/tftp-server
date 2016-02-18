package types;

import resource.Strings;

/**
 * This enum encapsulates the type of error messages that the 
 * TFTP protocol specified. These error codes go with the 05 
 * packet header attribute
 * 
 * @author Team 3
 *
 */
public enum ErrorType {
	
	NOT_DEFINED {
		@Override
		public short getErrorCodeShort() {
			return 0;
		}
		@Override
		public String getErrorMessageString() {
			return Strings.NOT_DEFINED;
		}
	},
	FILE_NOT_FOUND {
		@Override
		public short getErrorCodeShort() {
			return 1;
		}
		@Override
		public String getErrorMessageString() {
			return Strings.FILE_NOT_FOUND;
		}
	},
	ACCESS_VIOLATION {
		@Override
		public short getErrorCodeShort() {
			return 2;
		}
		@Override
		public String getErrorMessageString() {
			return Strings.ACCESS_VIOLATION;
		}
	},
	ALLOCATION_EXCEED {
		@Override
		public short getErrorCodeShort() {
			return 3;
		}
		@Override
		public String getErrorMessageString() {
			return Strings.ALLOCATION_EXCEED;
		}
	},
	ILLEGAL_OPERATION {
		@Override
		public short getErrorCodeShort() {
			return 4;
		}
		@Override
		public String getErrorMessageString() {
			return Strings.ILLEGAL_OPERATION;
		}
	},
	UNKNOWN_TRANSFER {
		@Override
		public short getErrorCodeShort() {
			return 5;
		}
		@Override
		public String getErrorMessageString() {
			return Strings.UNKNOWN_TRANSFER;
		}
	},
	FILE_EXISTS {
		@Override
		public short getErrorCodeShort() {
			return 6;
		}
		@Override
		public String getErrorMessageString() {
			return Strings.FILE_EXISTS;
		}
	},
	NO_SUCH_USER {
		@Override
		public short getErrorCodeShort() {
			return 7;
		}
		@Override
		public String getErrorMessageString() {
			return Strings.NO_SUCH_USER;
		}
	},
	NO_ERROR {
		@Override
		public short getErrorCodeShort() {
			return -1;
		}
		@Override
		public String getErrorMessageString() {
			return Strings.NO_ERROR;
		}
	},
	EXCEPTION_ERROR {
		@Override
		public short getErrorCodeShort() {
			return 8;
		}
		@Override
		public String getErrorMessageString() {
			return Strings.EXCEPTION_ERROR;
		}
	},
	EXIT {
		@Override
		public short getErrorCodeShort() {
			return -1;
		}
		@Override
		public String getErrorMessageString() {
			return Strings.EXIT_BYE;
		}
	}
	;
	
	/**
	 * Gets the ErrorType enum match the ordinal number 
	 * 
	 * @param num - the number which corresponds to the header
	 * @return
	 */
	public static ErrorType matchErrorByNumber(int num) {
		switch(num) {
			case 0:
				return ErrorType.NOT_DEFINED;
			case 1:
				return ErrorType.FILE_NOT_FOUND;
			case 2:
				return ErrorType.ACCESS_VIOLATION;
			case 3: 
				return ErrorType.ALLOCATION_EXCEED;
			case 4:
				return ErrorType.ILLEGAL_OPERATION;
			case 5:
				return ErrorType.UNKNOWN_TRANSFER;
			case 6: 
				return ErrorType.FILE_EXISTS;
			case 7:
				return ErrorType.NO_SUCH_USER;
			default:
				return ErrorType.NO_ERROR;
		}
	}
	
	/**
	 * Get the numeric error code that the error represents in the TFTP specs.
	 * Use Conversions to convert to byte and back
	 * 
	 * @return error code value - short
	 */
	public abstract short getErrorCodeShort();
	
	/**
	 * Gets the error message associated to the value
	 * 
	 * @return message - String
	 */
	public abstract String getErrorMessageString();
}

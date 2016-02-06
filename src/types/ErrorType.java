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
	};
	
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

package types;

/**
 * @author Team 3
 *
 */
public enum RequestType {
	RRQ {
		@Override
		public int getOptCode() {
			return 1;
		}
		@Override
		public byte[] getHeaderByteArray() {
			return new byte[] {0,1};
		}
	},
	WRQ {
		@Override
		public int getOptCode() {
			return 2;
		}
		@Override
		public byte[] getHeaderByteArray() {
			return new byte[] {0,2};
		}
	},
	DATA {
		@Override
		public int getOptCode() {
			return 3;
		}
		@Override
		public byte[] getHeaderByteArray() {
			return new byte[] {0,3};
		}
	},
	ACK {
		@Override
		public int getOptCode() {
			return 4;
		}
		@Override
		public byte[] getHeaderByteArray() {
			return new byte[] {0,4};
		}
	},
	ERROR {
		@Override
		public int getOptCode() {
			return 5;
		}
		@Override
		public byte[] getHeaderByteArray() {
			return new byte[] {0,5};
		}
	},
	NONE {
		@Override
		public int getOptCode() {
			return -1;
		}
		@Override
		// This is not a real Request Type
		public byte[] getHeaderByteArray() {
			return new byte[] {0,0};
		}
	};
	
	/**
	 * Gets the RequestType enum match the ordinal number 
	 * 
	 * @param num - the number which corresponds to the header
	 * @return
	 */
	public static RequestType matchRequestByNumber(int num) {
		num -= 1;
		switch(num) {
			case 0:
				return RequestType.RRQ;
			case 1:
				return RequestType.WRQ;
			case 2:
				return RequestType.DATA;
			case 3: 
				return RequestType.ACK;
			case 4:
				return RequestType.ERROR;
			default:
				return RequestType.NONE;
		}
	}
	
	/**
	 * This method is attached to each enum to allow each
	 * enumerable type to know how to represent itself as
	 * the header portion of the array buffer
	 * 
	 * @return packet header array
	 */
	public abstract byte[] getHeaderByteArray();
	
	public abstract int getOptCode();
	
}

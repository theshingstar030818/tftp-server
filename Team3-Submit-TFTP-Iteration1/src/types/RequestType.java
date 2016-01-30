package types;

/**
 * @author Team 3
 *
 */
public enum RequestType {
	RRQ {
		@Override
		public byte[] getHeaderByteArray() {
			return new byte[] {0,1};
		}
	},
	WRQ {
		@Override
		public byte[] getHeaderByteArray() {
			return new byte[] {0,2};
		}
	},
	DATA {
		@Override
		public byte[] getHeaderByteArray() {
			return new byte[] {0,3};
		}
	},
	ACK {
		@Override
		public byte[] getHeaderByteArray() {
			return new byte[] {0,4};
		}
	},
	ERROR {
		@Override
		public byte[] getHeaderByteArray() {
			return new byte[] {0,5};
		}
	},
	NONE {
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
	
}

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
	};
	
	/**
	 * This method is attached to each enum to allow each
	 * enumerable type to know how to represent itself as
	 * the header portion of the array buffer
	 * 
	 * @return packet header array
	 */
	abstract byte[] getHeaderByteArray();
	
}

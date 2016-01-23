package types;

/**
 * @author Team 3
 * 
 */
public enum ModeType {
	NETASCII {
		@Override 
		public byte[] getModeByteArray() {
			return "netascii".getBytes();
		}
	},
	OCTET {
		@Override 
		public byte[] getModeByteArray() {
			return "octect".getBytes();
		}
	};
	
	/**
	 * This method is attached to each enum to allow each
	 * enumerable type to know how to represent itself as
	 * the mode portion of the array buffer
	 * 
	 * @return mode byte array (netascii/octet)
	 */
	public abstract byte[] getModeByteArray();
}

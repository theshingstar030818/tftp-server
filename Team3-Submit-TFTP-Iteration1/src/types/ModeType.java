package types;

import resource.Configurations;

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
	
	/**
	 * This function will find a matching string that corresponds to 
	 * a ModeType enum, then return the result. If the string has not matched,
	 * then the default mode is given. 
	 * 
	 * @param modeString - a string representation of the mode
	 * @return ModeType (NETASCII/OCTET)
	 */
	public static ModeType matchModeFromString(String modeString) {
		if(modeString.equalsIgnoreCase("octet")){
			return ModeType.OCTET;
		} 
		if(modeString.equalsIgnoreCase("netascii")) {
			return ModeType.NETASCII;
		}
		return Configurations.DEFAULT_RW_MODE;
	}
}

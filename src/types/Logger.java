package types;

public enum Logger {
	
	VERBOSE {
		@Override
		public short getLogLevelShort() {
			return 0;
		}
		@Override
		public void print(Logger c, String s){
			//print all the information of DEBUG 
			if(c == VERBOSE || c == DEBUG || c == INFO || c == WARN ){
				System.out.println(this.classTag + s);
			}else if(c == ERROR || c == FATAL){
				System.err.println(this.classTag + s);
			}
		}
	},
	DEBUG {
		@Override
		public short getLogLevelShort() {
			return 1;
		}
		@Override
		public void print(Logger c, String s){
			// print all the debug level messages except VERBOSE messages
			if(c == DEBUG || c == INFO || c == WARN){
				System.out.println(this.classTag + s);
			}else if(c == ERROR || c == FATAL){
				System.err.println(this.classTag + s);
			}
		}
	},
	INFO {
		@Override
		public short getLogLevelShort() {
			return 2;
		}
		@Override
		public void print(Logger c, String s){
			// print all the debug level messages except VERBOSE and DEBUG level messages
			if(c == INFO || c == WARN){
				System.out.println(this.classTag + s);
			}else if(c == ERROR || c == FATAL){
				System.err.println(this.classTag + s);
			}
		}
	},
	WARN {
		@Override
		public short getLogLevelShort() {
			return 3;
		}
		@Override
		public void print(Logger c, String s){
			// print all the debug level messages except VERBOSE, DEBUG and INFO level messages
			if(c == WARN){
				System.out.println(this.classTag + s);
			}else if(c == ERROR || c == FATAL){
				System.err.println(this.classTag + s);
			}
		}
	},
	ERROR {
		@Override
		public short getLogLevelShort() {
			return 4;
		}
		@Override
		public void print(Logger c, String s){
			// print all the debug level messages except VERBOSE, DEBUG,INFO and WARN level messages
			if(c == ERROR || c == FATAL){
				System.err.println(this.classTag + s);
			}
		}
	},
	FATAL {
		@Override
		public short getLogLevelShort() {
			return 5;
		}
		@Override
		public void print(Logger c, String s){
			// only print FATAL level messages 
			if(c == FATAL){
				System.err.println(this.classTag + s);
			}
		}
	};
	
	protected String classTag = " : ";
	
	public void setClassTag(String classTag){
		this.classTag = classTag + " : ";
	}

	public abstract short getLogLevelShort();

	public abstract void print(Logger c, String s);
}

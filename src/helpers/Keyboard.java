package helpers;

import java.io.*; //tell the java compiler that we'll be doing i/o 
public class Keyboard { 
    private static BufferedReader inputStream = new BufferedReader 
                                     (new InputStreamReader(System.in)); 

    /* Get an integer from the user and return it */
    public static int getInteger() { 
		try {
	    	return (Integer.valueOf(inputStream.readLine().trim()).intValue());
		} catch (Exception e) {
			//System.err.println(e);
	    	return -2;
		}
    }
    /* Get a double from the user and return it */
    public static double getDouble() { 
		try {
            return (Double.valueOf(inputStream.readLine().trim()).doubleValue());
		} catch (Exception e) {
	    	return 0.0;
		}
    }
    /* Get a float from the user and return it */
    public static float getFloat() { 
		try {
            return (Float.valueOf(inputStream.readLine().trim()).floatValue());
		} catch (Exception e) {
	    	return 0.0f;
		}
    }
    /* Get a string of text from the user and return it */
    public static String getString() { 
		try {
            return inputStream.readLine();
		} catch (Exception e) {
			System.err.println(e);
	    	return "";
		}
    }
    /* Get a char from the user and return it */
    public static char getCharacter() { 
		try {
			String in = inputStream.readLine().trim();
			if (in.length() == 0)
				return (char)0;
			else
            	return (in.charAt(0));
		} catch (Exception e) {
	    	return(char)0;
		}
    }
    /* Get a boolean from the user and return it */
    public static boolean getBoolean() { 
		try {
			return (Boolean.valueOf(inputStream.readLine().trim()).booleanValue());
		} catch (Exception e) {
	    	return false;
		}
    }
} 
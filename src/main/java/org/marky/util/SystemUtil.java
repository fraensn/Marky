package org.marky.util;

import java.io.File;

public class SystemUtil {
	
	public static File getFileArg(String arg) {
		String prop = System.getProperty(arg);
		if (prop != null) {
			return new File(prop);
		}
		
		return null;
	}
	
	public static boolean getBoolArg(String arg, boolean defaultValue) {
		String prop = System.getProperty(arg);
		
		if ( "true".equalsIgnoreCase(prop) || "1".equals(prop) ) {
			return true;
		} else if ( "false".equalsIgnoreCase(prop) || "0".equals(prop) ) {
			return false;
		}
		
		return defaultValue;
	}
}

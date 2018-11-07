package org.marky.util;

import java.io.File;

public class FileUtil {

	public static String getExtension(File file) {
		return getExtension(file.getName());
	}
	
	public static String getExtension(String fileName) {
	    int pos = fileName.lastIndexOf('.');
	    if (pos == -1) return null;
	    return fileName.substring(pos + 1);
	}
	
	
	public static String getBaseName(File file) {
		return getBaseName(file.getName());
	}
	
	public static String getBaseName(String fileName) {
		int pos = fileName.lastIndexOf(".");
		String baseName = fileName;
		if (pos > -1) {
			baseName = fileName.substring(0, pos);
		}
		
		return baseName;
	}
}

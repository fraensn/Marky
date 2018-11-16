package org.marky.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtil {
	private static final Pattern placeholder = Pattern.compile("(\\$\\{.+\\})");
	
	
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
	
	public static boolean write(File template, File out, StringWriter content) throws IOException {
		boolean success = false;
		String fn = out.getAbsolutePath();
		
		if (out.exists()) {
			fn = getBaseName(out);
			fn = out.getParent() + "/" + fn +  "_" + out.getAbsolutePath().hashCode() + ".html";
		}
		
		try (FileWriter fw = new FileWriter(fn)) {
			if (template.exists()) {
				try ( BufferedReader reader = new BufferedReader( new FileReader(template)) ) {
					String line = null;
					while ( (line = reader.readLine()) != null) {
						Matcher matcher = placeholder.matcher(line);
						if ( matcher.find() ) {
							String group = matcher.group();
							line = line.replace(group, content.toString());
						}
						fw.write(line + "\n");
					}
					success = true;
				}
			} else {
				fw.write( content.toString() );
				success = (template == null);
			}
		}
		
		return success;
	}
}

package org.marky.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Trac2Md {

	private static Pattern PAT_HEADER = Pattern.compile("^[=]+");
	private static Pattern PAT_HEADER_END = Pattern.compile("[=]+$");
	private static Pattern PAT_MACRO = Pattern.compile("(\\[\\[.+\\]\\])");
	private static Pattern PAT_CODE_START = Pattern.compile("\\{\\{\\{(#!)?");
	private static Pattern PAT_CODE_END = Pattern.compile("\\}\\}\\}");
	
	public static void main(String[] args) {
		Trac2Md conv = new Trac2Md();
		try {
			long start = System.currentTimeMillis();
			File folder = new File("C:/projects/Konzepte/howto/TippsUndTricks/");
			conv.convertFolder(folder, null);
//			String fileName = "PostgreSQL";
//			File outFile = new File(folder, fileName + "_edited.md");
//			conv.convert( new File(folder, fileName + ".md"), outFile );
			System.out.println("Successfully converted in " + (System.currentTimeMillis() - start) + "ms");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void convertFolder(File folder, String filePrefix) throws IOException {
		if (folder == null || !folder.isDirectory()) return;
		
		File[] files = folder.listFiles( new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name != null && name.toLowerCase().endsWith(".md");
			}
		});
		
		for (File f : files) {
			File out = f;
			if (filePrefix != null) {
				out = new File(folder, filePrefix + f.getName());
			}
			convert(f, out);
		}
	}
	
	private void convert(File wikiFile, File outFile) throws IOException {
		File tempFile = new File(outFile + ".tmp");
		try ( BufferedReader br = new BufferedReader( new FileReader(wikiFile) );
			  PrintWriter writer = new PrintWriter(tempFile) ) {
			String line;
			while ( (line = br.readLine()) != null) {
				boolean empty = line.trim().length() == 0;
				line = replace(line, PAT_HEADER_END, "");
				line = replace(line, PAT_MACRO, "");
				line = replace(line, PAT_HEADER, '#');
				line = replace(line, PAT_CODE_START, "```");
				line = replace(line, PAT_CODE_END, "```");
				line = replaceLink(line);
				
				if (empty || line.trim().length() > 0) {
					writer.println(line);
				}
			}
		}
		
		outFile.delete();
		tempFile.renameTo(outFile);
		tempFile.delete();
	}
	
	private String replaceLink(String line) {
		Pattern p = Pattern.compile("\\[([\\w:/\\.\\-\\?_%@=]+) ([\\w:/\\.,\\-_%@=\\(\\)\"' ]+)\\]", Pattern.UNICODE_CHARACTER_CLASS);
		Matcher m = p.matcher(line);
		
		String newLine = "";
		int lastStart = 0;
		boolean found = false;
		while (m.find()) {
			newLine += line.substring(lastStart, m.start());
			String link = m.group(1);
			if (link.toLowerCase().startsWith("wiki:")) {
				// TODO skip main folder(s)
				link = link.substring(5);
			}
			
			String name = m.group(2);
			newLine += "[" + name + "](" + link + ")";
			lastStart = m.end();
			found = true;
		}
		
		if (found) {
			newLine += line.substring(lastStart, line.length());
		} else {
			newLine = line;
		}
		
		return newLine;
	}
	
	private String replace(String line, Pattern regex, char replacement) {
		String replaced = line;
		Matcher matcher = regex.matcher(line);
		while (matcher.find()) {
			String replace = "";
			for (int i = 0; i < matcher.group().length(); i++) {
				replace += replacement;
			}
			replaced = replaceLine(line, replace, matcher.start(), matcher.end());
		}
		
		return replaced;
	}
	
	
	private String replace(String line, Pattern regex, String replacement) {
		String replaced = line;
		Matcher matcher = regex.matcher(line);
		while (matcher.find()) {
			replaced = replaceLine(replaced, replacement, matcher.start(), matcher.end());
		}
		
		return replaced;
	}


	private String replaceLine(String line, String replacement, int start, int end) {
		String replaced;
		replaced = line.substring(0, start);
		replaced += replacement;
		replaced += line.substring(end);
		return replaced;
	}
	
}
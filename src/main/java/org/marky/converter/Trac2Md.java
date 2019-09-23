package org.marky.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts Trac wiki files to Markdown formatted files.<p>
 * 
 * It converts:
 * <ul>
 *   <li>text: bold, header
 *   <li>code blocks
 *   <li>simple tables
 * </ul>
 * 
 * TODO list:
 * TODO  ignore folder / files, e.g. do NOT process files, that ends with a tilde (~)
 * TODO  maybe convert TitleIndex-Macro ??
 * 
 * @author Franz Mayer
 */
public class Trac2Md {

	private static Pattern PAT_HEADER = Pattern.compile("^[=]+");
	private static Pattern PAT_HEADER_END = Pattern.compile("[=]+$");
	private static Pattern PAT_MACRO = Pattern.compile("(\\[\\[.+\\]\\])");
	private static Pattern PAT_CODE_START = Pattern.compile("\\{\\{\\{(#!)?");
	private static Pattern PAT_CODE_END = Pattern.compile("\\}\\}\\}");
	private static Pattern PAT_BOLD = Pattern.compile("'''");
	
	private String overviewLine = "`>` [Directory](.)";
	
	private File baseDir;
	private PrintWriter toc;
	private int folderLevel = 0;
	
	public static void main(String[] args) {
		Trac2Md conv = new Trac2Md();
		
		if (args != null && args.length > 1) {
			conv.baseDir = new File(args[0]);
			conv.start();
		} else {
			System.out.println("Invalid call. Args: folder suffix");
		}
	}
	
	public Trac2Md() {
	}
	
	public void start() {
		String suffix = "wiki";
		long start = System.currentTimeMillis();
		
		try {
			toc = new PrintWriter( new File(baseDir, "README.md") );
			int cnt = convertFolder(baseDir, null, suffix);
			
			System.out.println();
			System.out.println("Successfully converted " + cnt + " files in base directory " + baseDir + " in " + (System.currentTimeMillis() - start) + " ms");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getRelativePath(File absFile) {
		String abs = absFile.getAbsolutePath();
		String relPath = abs.replace(baseDir.getAbsolutePath(), "");
		if (relPath.startsWith("\\")) {
			return relPath.substring(1);
		}
		
		return relPath;
	}
	
	private void writeOverview(PrintWriter pw) {
		pw.print(overviewLine);
		pw.print(" `>` [README.md](");
		
		for (int i = 0; i < folderLevel; i++) {
			pw.print("../");
		}
		
		pw.println("README.md)");
	}
	
	private void writeTocLine(File out, boolean isHeader) {
		String line = "";
		
		if (isHeader) {
			for (int i = 0; i < folderLevel + 1; i++) {
				line += "#";
			}
			line = "\n" + line + " " + out.getName() + "\n";
		} else {
			String relPath = getRelativePath(out);
			line += "* [" + relPath + "](" + relPath.replaceAll("\\\\", "/") + ")";
		}
		
		toc.println(line);
	}
	
	
	private int convertFolder(File folder, String filePrefix, String fileSuffix) throws IOException {
		if (folder == null || !folder.isDirectory()) return - 1;
		System.out.println("converting folder " + folder + " (folder level: " + folderLevel + ") ...");
		writeTocLine(folder, true);
		
		File[] files = folder.listFiles( new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name != null && name.toLowerCase().endsWith("." + fileSuffix);
			}
		});
		
		File[] subfolders = folder.listFiles( new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		
		for (File f : files) {
			File out = f;
			String name = f.getName();
			int idx = name.lastIndexOf(".");
			if (idx > 0) {
				name = name.substring(0, idx) + ".md";
			}
			out = new File(folder, (filePrefix != null ? filePrefix : "") + name);
			writeTocLine(out, false);
			convert(f, out);
		}
		
		int fileCount = files.length;
		if (subfolders.length > 0) {
			folderLevel++;
			for (File dir : subfolders) {
				fileCount += convertFolder(dir, filePrefix, fileSuffix);
			}
			folderLevel--;
		}
		
		return fileCount;
	}
	
	private void convert(File wikiFile, File outFile) throws IOException {
		File tempFile = new File(outFile + ".tmp");
		StringBuffer ln = null;
		String line = null;
		
		try ( BufferedReader br = new BufferedReader( new FileReader(wikiFile) );
			  PrintWriter writer = new PrintWriter(tempFile) ) {
			boolean code = false;
			int tableRow = 0;
			writeOverview(writer);
			
			while ( (line = br.readLine()) != null) {
				boolean empty = line.trim().length() == 0;
				ln = new StringBuffer(line);
				String trimmedLine = line.trim();
				
				code = replace(ln, PAT_CODE_START, "```");
				if ( replace(ln, PAT_CODE_END, "```") ) {
					code = false;
				}
				
				if (! code) {
					if (trimmedLine.startsWith("||") && trimmedLine.endsWith("||")) {
						tableRow++;
						ln = new StringBuffer( trimmedLine.replaceAll("[=]?\\|\\|[=]?", "|") );
						if (tableRow == 1) {
							ln.insert(0, "\n");
							int cols = trimmedLine.split("[=]?\\|\\|[=]?").length;
							ln.append("\n");
							for (int i = 0; i < cols; i++) {
								ln.append("|");
								if (i < cols - 1) {
									ln.append("-");
								}
							}
						}
					} else {
						tableRow = 0;
					}
					replace(ln, PAT_HEADER, '#');
					replace(ln, PAT_HEADER_END, "");
					replace(ln, PAT_MACRO, "");
					replace(ln, PAT_BOLD, "**");
					replaceLink(ln);
				}
				
				if (empty || ln.toString().trim().length() > 0) {
					writer.println(ln.toString());
				}
			}
		} catch (IOException | RuntimeException e) {
			System.out.println("Error at file " + wikiFile + ", line " + line + "\n  currently converted: " + ln);
			throw e;
		}
		
		outFile.delete();
		tempFile.renameTo(outFile);
		tempFile.delete();
	}
	
	private boolean replaceLink(StringBuffer line) {
		// [LINK name]
		Pattern p = Pattern.compile("\\[([\\w:/\\.\\-\\?_%@=#&]+)[ ]*([\\w:/\\.,\\-_%@=\\(\\)\"' \\?]*)\\]", Pattern.UNICODE_CHARACTER_CLASS);
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
			if (name == null || name.length() == 0) {
				newLine += link;
			} else {
				newLine += "[" + name + "](" + link + ")";
			}
			lastStart = m.end();
			found = true;
		}
		
		if (found) {
			newLine += line.substring(lastStart, line.length());
		} else {
			newLine = line.toString();
		}
		
		replaceBuffer(line, newLine);
		
		return found;
	}
	
	private boolean replace(StringBuffer line, Pattern regex, char replacement) {
		String replaced = line.toString();
		Matcher matcher = regex.matcher(line);
		boolean found = false;
		
		while (matcher.find()) {
			String replace = "";
			for (int i = 0; i < matcher.group().length(); i++) {
				replace += replacement;
			}
			replaced = replaceLine(replaced, replace, matcher.start(), matcher.end());
			found = true;
		}
		
		replaceBuffer(line, replaced);
		
		return found;
	}
	
	
	private boolean replace(StringBuffer line, Pattern regex, String replacement) {
		String replaced = "";
//		String original = line.toString();
		Matcher matcher = regex.matcher(line);
		boolean found = false;
		int lastEnd = 0;
		
		while (matcher.find()) {
			replaced += line.substring(lastEnd, matcher.start());
			replaced += replacement;
			lastEnd = matcher.end();
			found = true;
		}
		
		replaced += line.substring(lastEnd, line.length());
		
		replaceBuffer(line, replaced);
		
		return found;
	}


	private String replaceLine(String line, String replacement, int start, int end) {
		String replaced;
		replaced = line.substring(0, start);
		replaced += replacement;
		replaced += line.substring(end);
		return replaced;
	}

	private void replaceBuffer(StringBuffer line, String replace) {
		line.delete(0, line.length());
		line.append(replace);
	}
}

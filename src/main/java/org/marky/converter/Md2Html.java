package org.marky.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.marky.util.FileUtil;

public class Md2Html {
	private static Parser parser = Parser.builder().build();
	
	private final File srcDir;
	private final File out;
	private File outToc = null;
	// TODO: make parametrizeable
    private File template = new File("src/main/resources/templates/content.html");
    
	private boolean srcWalk = false;
	private HashSet<String> acceptExtensions = new HashSet<>();
	

	public Md2Html(File aInDir, File aOutDirOrFile) throws IOException {
		if (aInDir == null || aOutDirOrFile == null) {
			throw new IllegalArgumentException("No folders passed! Please provide source folder (aInDir) and output folder (aOutBaseDir)");
		} else if (! aInDir.exists()) {
			throw new IllegalArgumentException("Source folder does not exist: " + aInDir.getAbsolutePath());
		}
		
		srcDir = aInDir;
		out = aOutDirOrFile;
		if (out.isFile()) {
			out.delete();
		}
		setDefaultFileExtensions();
	}
	
	public void setDefaultFileExtensions() {
		acceptExtensions.clear();
		acceptExtensions.add("md");
		acceptExtensions.add("markdown");
	}
	
	public void addFileExtension(String ext) {
		if (ext == null) return;
		
		acceptExtensions.add(ext.trim().toLowerCase());
	}
	
	public void setOutToc(File aOutToc) {
		if (aOutToc == null) return;
		
		outToc = aOutToc;
		if (aOutToc.exists()) {
			aOutToc.delete();
		}
	}
	
	public void setSrcWalk(boolean doWalkSubs) {
		srcWalk = doWalkSubs;
	}
	
	public int convertMarkdown() throws FileNotFoundException, IOException {
		if (out.isDirectory()) {
			return convertMarkdown(null, srcDir);
		}

		StringWriter writer = new StringWriter();
		int cnt = convertMarkdown(writer, srcDir);
		
		FileUtil.write(template, out, writer);
		
		return cnt;
	}
	
	
	private int convertMarkdown(Writer writer, File dir) throws FileNotFoundException, IOException {
		int cnt = 0;
		File[] listFiles = dir.listFiles( new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				if (file != null && file.isFile()) {
					String extension = FileUtil.getExtension(file);
					if (extension != null) {
						return acceptExtensions.contains(extension.toLowerCase());
					}
				}
				
				return false;
			}
		});
		
		
		for (File f : listFiles) {
			convertMarkdownFile(writer, f);
			cnt++;
		}
		
		if (srcWalk) {
			File[] dirs = dir.listFiles( new FileFilter() {
				
				@Override
				public boolean accept(File file) {
					return file.exists() && file.isDirectory();
				}
			});
			
			for (File subDir : dirs) {
				cnt += convertMarkdown(writer, subDir);
			}
		}
		
		return cnt;
	}
	
	private void convertMarkdownFile(Writer writer, File src) throws FileNotFoundException, IOException {
		Node document = parser.parseReader( new FileReader (src) );
		HtmlRenderer renderer = HtmlRenderer.builder().build();
//		String render = renderer.render(document);
		
		File outFile = null;
		FileWriter fwToc = null;
		String fullPath = src.getAbsolutePath();
		
		try {
			if (writer != null) {
				writer.write("<hr id=\"" + fullPath.hashCode() + "\" />\n");
				if (outToc != null) {
					final boolean initToc = ! outToc.exists();
					fwToc = new FileWriter(outToc, true);
					if (initToc) {
						fwToc.write("<h1>Index</h1>\n");
						fwToc.write("<p>All markdown files of " + srcDir.getAbsolutePath() + ":</p>\n");
					}
					
					final String link = out.getName() + "#" + fullPath.hashCode();
					fwToc.write("<li><a href=\"" + link + "\">" + fullPath + "</a></li>\n");
				}
				
				renderer.render(document, writer);
			} else {
				outFile = new File(out, FileUtil.getBaseName(src) + ".html");
				StringWriter sw = new StringWriter();
				renderer.render(document, sw);
				FileUtil.write(template, outFile, sw);
			}
		} finally {
			if (fwToc != null) fwToc.close();
		}
	}

}

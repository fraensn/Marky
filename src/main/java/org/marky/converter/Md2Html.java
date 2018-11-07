package org.marky.converter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.marky.util.FileUtil;

public class Md2Html {
	private static Parser parser = Parser.builder().build();
	
	private final File srcDir;
	private final File out;
	private File outToc = null;
	
	private boolean srcWalk = false;
	private HashSet<String> acceptExtensions = new HashSet<>();
	
	public Md2Html(File aInDir, File aOutDirOrFile) {
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
		return convertMarkdown(srcDir);
	}
	
	private int convertMarkdown(File dir) throws FileNotFoundException, IOException {
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
			convertMarkdownFile(f);
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
				cnt += convertMarkdown(subDir);
			}
		}
		
		return cnt;
	}
	
	public void convertMarkdownFile(File src) throws FileNotFoundException, IOException {
		Node document = parser.parseReader( new FileReader (src) );
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		String render = renderer.render(document);
		
		File outFile = null;
		FileWriter fw = null;
		FileWriter fwToc = null;
		
		try {
			if (out.isFile() || ! out.exists()) {
				fw = new FileWriter(out, true);
				fw.write("<hr id=\"" + src.getName() + "\" />\n");
				if (outToc != null) {
					final boolean initToc = ! outToc.exists();
					fwToc = new FileWriter(outToc, true);
					if (initToc) {
						fwToc.write("<h1>Index</h1>\n");
						fwToc.write("<p>All markdown files of " + srcDir.getAbsolutePath() + "</p>\n");
					}
					final String link = out.getName() + "#" + src.getName();
					fwToc.write("<p><a href=\"" + link + "\">" + link + "</a> (Original file: " + src.getAbsolutePath() + ")</p>\n");
				}
			} else {
				outFile = new File(out, FileUtil.getBaseName(src) + ".html");
				fw = new FileWriter(outFile);
			}
			
			fw.write("<!-- Complete file path: " + src.getAbsolutePath() + " -->\n");
			fw.write(render);
		} finally {
			if (fw != null) fw.close();
			if (fwToc != null) fwToc.close();
		}
		
	}
}

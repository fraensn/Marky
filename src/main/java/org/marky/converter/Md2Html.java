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
	
	private FileWriter content = null;
	
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
			content = new FileWriter(out, true);
			startHtmlConent(content);
		}
		setDefaultFileExtensions();
	}
	
	private void startHtmlConent(FileWriter fw) throws IOException {
		fw.write("<html>\n");
		fw.write("  <head>\n");
		fw.write("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
		fw.write("  </head>\n");
		fw.write("  <body>\n");
	}
	
	private void endHtmlConent(FileWriter fw) throws IOException {
		fw.write("  </body>\n");
		fw.write("</html>\n");
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
		int cnt = convertMarkdown(srcDir);
		
		if (content != null) {
			endHtmlConent(content);
		}
		
		return cnt;
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
	
	private void convertMarkdownFile(File src) throws FileNotFoundException, IOException {
		Node document = parser.parseReader( new FileReader (src) );
		HtmlRenderer renderer = HtmlRenderer.builder().build();
//		String render = renderer.render(document);
		
		File outFile = null;
		FileWriter fwToc = null;
		String fullPath = src.getAbsolutePath();
		
		try {
			if (content != null) {
				content.write("<hr id=\"" + fullPath.hashCode() + "\" />\n");
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
				
				renderer.render(document, content);
			} else {
				outFile = new File(out, FileUtil.getBaseName(src) + ".html");
				renderer.render(document, new FileWriter(outFile));
			}
		} finally {
			if (fwToc != null) fwToc.close();
		}
	}
}

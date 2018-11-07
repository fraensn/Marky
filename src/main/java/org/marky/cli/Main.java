package org.marky.cli;

import java.io.File;
import java.io.IOException;

import org.marky.converter.Md2Html;
import org.marky.util.SystemUtil;

/**
 * 
 * Samples:
 * <pre>
 * -Dsrc.dir=C:/projects/SapiEng -Dout.file=C:/projects/SapiEng/site/content.html -Dsrc.walk=1 -Dout.file.toc=C:/projects/SapiEng/site/index.html
 * 
 * -Dsrc.dir=C:/projects/SapiEng/SeProcs/site/main -Dout.file=C:/projects/SapiEng/site
 * -Dsrc.dir=C:/projects/SapiEng/SeProcs/site/main -Dout.file=C:/projects/SapiEng/site/content.html
 * -Dsrc.dir=C:/projects/SapiEng/SeProcs -Dout.file=C:/projects/SapiEng/site/content.html -Dsrc.walk=1
 * </pre>
 * 
 * @author Franz.Mayer
 */
public class Main {

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		
		String argSrc = System.getProperty("src.dir");
		String argOut = System.getProperty("out.file");
		
		if (argSrc == null || argOut == null) {
			printHelp(true);
			return;
		}
		
		File srcDir = new File( argSrc );
		File outDir = new File( argOut );
		File outToc = SystemUtil.getFileArg("out.file.toc");
		
		try {
			Md2Html md2html = new Md2Html(srcDir, outDir);
			
			boolean walk = SystemUtil.getBoolArg("src.walk", false);
			md2html.setSrcWalk(walk);
			md2html.setOutToc(outToc);
		
			int cntFiles = md2html.convertMarkdown();
			System.out.println("Processed " + cntFiles + " files in " + (System.currentTimeMillis() - start) + " ms.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static void printHelp(boolean error) {
		if (error) {
			System.err.println("Invalid call.");
		}
		
		System.out.println("Usage: please provide all mandatory system parameters: src.dir, out.file");
		System.out.println("  Example: -Dsrc.dir=C:/projects/SapiEng -Dout.file=C:/projects/SapiEng/site");
	}
}

package org.marky.converter;

import java.io.StringReader;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.junit.Test;

public class MoinMoin2MdTest {

	@Test
	public void testConvertPlain() throws Exception {
		Parser parser = Parser.builder().build();
		StringReader reader = new StringReader("## Header 2");
		Node document = parser.parseReader(reader);
		TextContentRenderer renderer = TextContentRenderer.builder().build();
		String converted = renderer.render(document);
		
		System.out.println("converted: " + converted);
	}
}

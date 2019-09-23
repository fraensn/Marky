package org.marky.exporter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class TracExporter {

	private final Connection con;
	
	public static void main(String[] args) {
		Properties info = new Properties();
		info.setProperty("user", "trac");
		info.setProperty("password", "cart");
		
		try {
			TracExporter exporter = new TracExporter("jdbc:postgresql://localhost:5433/tracdb", info);
			exporter.exportWiki("C:/projects/Konzepte/howto/Trac_Export/");
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public TracExporter(String url, Properties conInfo) throws SQLException {
		System.out.println(" [TracExporter] try to get connection for url: " + url);
		con =  DriverManager.getConnection(url, conInfo);
	}
	
	public void exportWiki(String folderName) throws SQLException, IOException {
		System.out.println(" [TracExporter] export to folder: " + folderName);
		
		String sql = "select * from WIKI  \n" +
	             " where (NAME, VERSION) in ( \n" +
	             "        select w.NAME, max(version) as LATEST_VERSION  \n" +
	             "          from WIKI w \n" +
	             "         group by w.NAME \n" +
	             "  order by NAME)";
		
		try (   PreparedStatement ps = con.prepareStatement(sql); 
				ResultSet rs = ps.executeQuery(); ) {
			while (rs.next()) {
				String wikiName = rs.getString("NAME");
				String wikiText = rs.getString("TEXT");
				
				String folder = "";
				int folderEndIdx = wikiName.lastIndexOf("/");
				if (folderEndIdx > -1) {
					folder = wikiName.substring(0, folderEndIdx);
					wikiName = wikiName.substring(folderEndIdx + 1);
					wikiName = wikiName.replaceAll(" ", "_");
				}
				File outFolder = new File(folderName + folder);
				outFolder.mkdirs();
				
				File outFile = new File(folderName + folder + "/" + wikiName + ".wiki");
				try ( PrintWriter pw = new PrintWriter(outFile); ) {
					pw.print(wikiText);
					pw.flush();
				}
			}
		}
	}
}

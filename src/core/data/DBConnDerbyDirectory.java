package data;

import gmmlVision.GmmlVision;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import util.FileUtils;
import debug.StopWatch;

public class DBConnDerbyDirectory extends DBConnDerby {	
	String lastDbName;
		
	public void finalizeNewDatabase(String dbName) throws Exception {
		try {
			DriverManager.getConnection("jdbc:derby:" + FileUtils.removeExtension(dbName) + ";shutdown=true");
		} catch(Exception e) {
			GmmlVision.log.error("Database closed", e);
		}
	}
	
	public String openChooseDbDialog(Shell shell) {
		DirectoryDialog dd = createDirectoryDialog(shell);
		return dd.open();
	}

	public String openNewDbDialog(Shell shell, String defaultName) {
		DirectoryDialog dd = createDirectoryDialog(shell);
		if(defaultName != null) dd.setFilterPath(defaultName);
		return dd.open();
	}
}

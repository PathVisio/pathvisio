package org.pathvisio.pluginmanager.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class Utils {

	
	public static File unGzip(File infile, boolean deleteGzipfileOnSuccess ) throws IOException {
		  GZIPInputStream gin = new GZIPInputStream(new FileInputStream(infile));
		  File outFile = new File(infile.getParent(), infile.getName().replaceAll("\\.gz$",   ""));
		  FileOutputStream fos = new FileOutputStream(outFile);
		  byte[] buf = new byte[100000]; // Buffer size is a matter of taste and application...
		  int len;
		  while ( ( len = gin.read(buf) ) > 0 )
		    fos.write(buf, 0, len);
		  gin.close();
		  fos.close();
		  if ( deleteGzipfileOnSuccess )
		    infile.delete();
		  return outFile;
	}
}

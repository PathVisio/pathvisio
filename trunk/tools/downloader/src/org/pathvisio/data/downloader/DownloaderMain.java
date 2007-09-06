package org.pathvisio.data.downloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import javax.swing.ProgressMonitorInputStream;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

public class DownloaderMain {
	static void downloadFile(URL url, String toFile) throws Exception {		
		OutputStream out = new BufferedOutputStream(
				new FileOutputStream(toFile));
		URLConnection conn = url.openConnection();
		InputStream in = conn.getInputStream();

		ProgressMonitorInputStream pin = new ProgressMonitorInputStream(
				null,
				"Downloading " + url,
				in
		);
		pin.getProgressMonitor().setMillisToDecideToPopup(0);
		pin.getProgressMonitor().setMaximum(conn.getContentLength());

		byte[] buffer = new byte[1024];
		int numRead;
		long numWritten = 0;
		while ((numRead = pin.read(buffer)) != -1) {
			out.write(buffer, 0, numRead);
			numWritten += numRead;
		}

		in.close();
		out.close();
	}

	static InputStream getInputStream(String tarFileName) throws Exception{
		if(tarFileName.substring(tarFileName.lastIndexOf(".") + 1, tarFileName.lastIndexOf(".") + 3).equalsIgnoreCase("gz")){
			return new GZIPInputStream(new FileInputStream(new File(tarFileName)));
		}else{
			return new FileInputStream(new File(tarFileName));
		}
	}

	static void readTar(InputStream in, String untarDir) throws IOException{
		TarInputStream tin = new TarInputStream(in);
		TarEntry tarEntry = tin.getNextEntry();
		File dir = new File(untarDir);
		if(!dir.exists()) dir.mkdirs();
		while (tarEntry != null){
			File destPath = new File(dir, tarEntry.getName());
			if(!tarEntry.isDirectory()){
				FileOutputStream fout = new FileOutputStream(destPath);
				tin.copyEntryContents(fout);
				fout.close();
			}else{
				destPath.mkdir();
			}
			tarEntry = tin.getNextEntry();
		}
		tin.close();
	}



	public static void main(String[] args) {		      
		try {
			URL url = new URL(args[0]);
			String toFile = new File(url.getFile()).getName();

			//TODO: get right directory from properties file
			
			downloadFile(url, toFile);
			
			//TODO: unzip/tar the files

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}

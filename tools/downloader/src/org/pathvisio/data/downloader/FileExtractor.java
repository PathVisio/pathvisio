package org.pathvisio.data.downloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

public class FileExtractor {
	ProgressMonitor progress;
	
	private FileExtractor() {
		this(null);
	}
	
	private FileExtractor(ProgressMonitor p) {
		progress = p;
	}
	
	private void setTotalWork(int m) {
		if(progress != null) progress.setMaximum(m);
	}
	
	private void setProgress(int p) {
		if(progress != null) progress.setProgress(p);
	}
	
	boolean cancelled;
	private void cancel() {
		cancelled = true;
	}
	
	private boolean isCancelled() {
		if(cancelled) {
			return true; 
		} else {
			return progress == null ? false : progress.isCanceled();
		}
	}
	
	private void setNote(String note) {
		if(progress != null) progress.setNote(note);
	}
	
	private void finished() {
		if(progress != null) progress.setProgress(progress.getMaximum());
	}

	private boolean overwriteAll;
	
	private boolean mayWrite(File f) {
		if(overwriteAll) return true;
		if(!f.exists()) return true;
		
		String[] buttons = new String[] {"Yes", "Yes to all", "No", "Cancel"};
		int rc = JOptionPane.showOptionDialog(null,
				"File " + f.getName() + " already exists.\nOverwrite?",
				"Confirmation",
				JOptionPane.WARNING_MESSAGE,
				0,
				null,
				buttons,
				buttons[2]
		);

		boolean mayWrite = false;
		switch(rc) {
		case 1:
			overwriteAll = true;
		case 0:
			mayWrite = true;
			break;
		case 2:
			break;
		case 3:
		case -1:
			cancel();
		}
		return mayWrite;
	}

	public static void extractFile(File f, File toPath, ProgressMonitor progress) throws FileNotFoundException, IOException {
		FileExtractor extractor = new FileExtractor(progress);
		String fn = f.toString();
		String ext = "";
		int dot = fn.lastIndexOf('.');
		if(dot > -1) {
			ext = fn.substring(dot + 1);
		}
		if ("zip".equals(ext)) {
			extractor.extractZip(f, toPath);
		}	else  {
			throw new IOException("Unrecognized filetype: '" + ext + "'");
		}
	}
	
	public static void extractFile(File f, File toPath) throws FileNotFoundException, IOException {
		extractFile(f, toPath, null);
	}
		
	private void extractZip(File file, File unzipDir) throws IOException {
		ZipFile zipFile = new ZipFile(file);
		
		setTotalWork(zipFile.size());
		int progress = 0;
		
		Enumeration<? extends ZipEntry> entries = zipFile.entries();	
		while(entries.hasMoreElements()) {
			if(isCancelled()) {
				finished();
				return;
			}
			
			ZipEntry zipEntry = entries.nextElement();
			
			setNote(zipEntry.getName());
			
			File f = new File(unzipDir + File.separator + zipEntry.getName());
			if (zipEntry.isDirectory()) { // if its a directory, create it
				f.mkdirs();
				continue;
			}
			f.getParentFile().mkdirs(); // create the parent directories
			
			if(mayWrite(f)) {
				InputStream is = zipFile.getInputStream(zipEntry); // get the input stream
				FileOutputStream fos = new java.io.FileOutputStream(f);
				while (is.available() > 0) {
					fos.write(is.read());
				}
				fos.close();
				is.close();
			}
			setProgress(++progress);
		}
		finished();
	}
}

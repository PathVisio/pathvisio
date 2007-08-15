package org.pathvisio.gui.swing;

import java.io.File;
import javax.swing.filechooser.FileFilter;

import org.pathvisio.model.PathwayExporter;
import org.pathvisio.model.PathwayImporter;

public class ImporterExporterFileFilter extends FileFilter {
	String[] exts;
	String name;
	
	public ImporterExporterFileFilter(PathwayImporter imp) {
		exts = imp.getExtensions();
		name = imp.getName();
	}
	
	public ImporterExporterFileFilter(PathwayExporter exp) {
		exts = exp.getExtensions();
		name = exp.getName();
	}
	
	public String getDefaultExtension() {
		return exts[0];
	}
	
	public boolean accept(File f) {
		if(f.isDirectory()) return true;

		String fn = f.toString();
		int i = fn.lastIndexOf('.');
		if(i > 0) {
			String ext = fn.substring(i + 1);
			for(String impExt : exts) {
				if(impExt.equalsIgnoreCase(ext)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public String getDescription() {
		StringBuilder extstr = new StringBuilder();
		for(String e : exts) {
			extstr.append(".");
			extstr.append(e);
			extstr.append(", ");
		}
		String str = extstr.substring(0, extstr.length() - 2);
		return name + " (" + str + ")";
	}
}

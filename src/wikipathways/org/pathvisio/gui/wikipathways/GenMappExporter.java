// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathvisio.gui.wikipathways;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.sql.SQLException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.filechooser.FileFilter;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.Pathway;
import org.pathvisio.view.MIMShapes;

public class GenMappExporter {
	static final int WORK_MIN = 0;
	static final int WORK_MAX= 100;
	
	public static void main(String[] args) {
		try {
			MIMShapes.registerShapes();
			
			if(args.length != 2) {
				throw new IllegalArgumentException(
						"Invalid number of arguments: " + args.length
				);
			}
			URL pwUrl = new URL(args[0]);
			String pwName = args[1];
			
			ProgressMonitor progress = new ProgressMonitor(null, "Converting to GenMAPP", "", WORK_MIN, WORK_MAX);
			progress.setMillisToPopup(0);

			File mappFile = doExport(pwUrl, pwName, progress);
			JFileChooser fc = new JFileChooser();
			fc.addChoosableFileFilter(new FileFilter() {
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().endsWith(".mapp");
				}
				public String getDescription() {
					return "GenMAPP file";
				}
			});
			int status = fc.showDialog(null, "Save");
			if(status == JFileChooser.APPROVE_OPTION) {
				File destFile = fc.getSelectedFile();
				if(!destFile.getName().endsWith(".mapp")) {
					destFile = new File(destFile.getAbsolutePath() + ".mapp");
				}
				status = JOptionPane.OK_OPTION;
				if(destFile.exists()) {
					status = JOptionPane.showConfirmDialog(
							null, 
							"File already exists, overwrite?", 
							"Overwrite?",
							JOptionPane.OK_CANCEL_OPTION
					);
				}
				if(status == JOptionPane.OK_OPTION) {
					Logger.log.info("Saving mapp to " + destFile);
					FileChannel ic = new FileInputStream(mappFile).getChannel();
					FileChannel oc = new FileOutputStream(destFile).getChannel();
					ic.transferTo(0, ic.size(), oc);
					ic.close();
					oc.close(); 
				}
			}
		} catch(Exception e) {
			Logger.log.error("Error converting to GenMAPP format", e);
			String message = "Unable to save GenMAPP file\n" + e.getClass() + ": " + e.getMessage(); 				
			if(e.getCause() instanceof SQLException) {
				message = "Exporting GenMAPP files is only supported on Windows";
			}
			JOptionPane.showMessageDialog(null, 
					message + "\nSee error log for datails", 
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
		System.exit(0);
	}

	/**
	 * Export to GenMAPP
	 */
	static File doExport(URL pwUrl, String pwName, ProgressMonitor progress) throws Exception {
		if(progress != null) {
			if(progress.isCanceled()) System.exit(0);
			progress.setNote("Loading pathway");
			progress.setProgress((int)((WORK_MAX - WORK_MIN) * 0.1));
		}
		//Load the pathway
		URLConnection con = pwUrl.openConnection();
		Pathway pathway = new Pathway();
		pathway.readFromXml(con.getInputStream(), true);

		if(progress != null) {
			if(progress.isCanceled()) System.exit(0);
			progress.setNote("Loading pathway");
			progress.setProgress((int)((WORK_MAX - WORK_MIN) * 0.5));
		}
		//Convert to temp file
		String tmp = System.getProperty("java.io.tmpdir");
		File mappFile = new File(tmp, pwName + ".mapp");
		pathway.writeToMapp(mappFile);

		if(progress != null) {
			progress.setProgress(WORK_MAX);
		}
		return mappFile;
	}
}

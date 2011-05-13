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
package org.pathvisio.data.downloader;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.ProgressMonitorInputStream;

import org.pathvisio.gui.dialogs.OkCancelDialog;

public class DownloaderMain {
	static void downloadFile(URL url, File toFile) throws Exception {
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

	static File getOutputPath(File initialPath) {
		PathDialog dialog = new PathDialog("Save to location", initialPath);
		dialog.setVisible(true);
		dialog.dispose();
		String status = dialog.getExitCode();

		if(PathDialog.OK.equals(status)) {
			return new File(dialog.getValue());
		}	else {
			return null;
		}
	}

	static class PathDialog extends OkCancelDialog {
		JTextField pathText;
		JButton browse;

		public PathDialog(String title, File path) {
			super(null, title, null, true);
			setDialogComponent(createDialogPane());
			pathText.setText("" + path);
			pack();
		}

		protected Component createDialogPane() {
			final JPanel p = new JPanel();
			p.setLayout(new BorderLayout(10, 10));

			JLabel label = new JLabel("Select location to save the files:");
			label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			pathText = new JTextField();
			browse = new JButton("Browse");

			browse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser(new File(pathText.getText()));
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int status = fc.showDialog(p, "Select");
					 if (status == JFileChooser.APPROVE_OPTION) {
				            File file = fc.getSelectedFile();
				            pathText.setText(file.toString());
				        }
				}
			});
			p.add(label, BorderLayout.PAGE_START);
			p.add(pathText, BorderLayout.CENTER);
			p.add(browse, BorderLayout.LINE_END);
			return p;
		}

		public String getValue() {
			return pathText.getText();
		}
	}



	public static void main(String[] args) {
		//args[0], the url to download from
		//args[1], the file type
		//args[2], optional flag to force default directory
		try {
			URL url = new URL(args[0]);
			String toFile = new File(url.getFile()).getName();

			boolean force = false;
			if(args.length > 2) { //The force parameter is specified
				force = "true".equals(args[2]);
			}

			Properties props =new Properties();
			File appDir = new File(System.getProperty("user.home") + File.separator +  ".PathVisio");
			File propFile = new File(appDir, ".PathVisio");
			try {
				props.load(new FileInputStream(propFile));
			} catch(Exception e) {
				System.err.println("Unable to read properties file: " + propFile);
			}

			String propType = "";
			if(args.length > 1) {
				 propType = args[1];
			}
			String propValue = props.getProperty(propType);

			//Default values are not taken from SwtPreferences, since that depends on Engine,
			//which depends on almost the whole pathvisio_v1.jar, making the size of this little
			//tool too large!
			//TODO: decouple preferences from other pathvisio classes
			File dataDir = new File(System.getProperty("user.home") + File.separator +  "PathVisio-Data");
			if(propValue == null) { //Try to set to defaults
				if("SWT_DIR_PWFILE".equals(propType)) {
					propValue = new File(dataDir, "pathways").toString();
				} else if ("SWT_DIR_GDB".equals(propType)) {
					propValue = new File(dataDir, "gene databases").toString();
				} else { //No default found, set empty
					propValue = "";
				}
			}

			File toDir = new File(propValue);

			if(!force) {
				toDir = getOutputPath(toDir);
			}

			if(toDir == null) System.exit(0);
			if(!toDir.exists()) {
				toDir.mkdirs();
			}

			File tmpFile = File.createTempFile(toFile, toFile);
			tmpFile.deleteOnExit();

			downloadFile(url, tmpFile);

			FileExtractor.extractFile(tmpFile, toDir, new ProgressMonitor(null, "Extracting...", "", 0, 1));

		} catch (Exception e) {
			if(!(e instanceof InterruptedException)) { //Don't show message if user pressed cancel
				JOptionPane.showMessageDialog(null, "Unable to save files:\n" + e + ":\n" + e.getMessage());
			}
			e.printStackTrace();
		}
		System.exit(0);
	}
}

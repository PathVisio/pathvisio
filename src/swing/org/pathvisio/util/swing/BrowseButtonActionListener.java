package org.pathvisio.util.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;

/** 
 * Utility class for attaching a "Browse" button to a text field 
 */
public class BrowseButtonActionListener implements ActionListener
{
	private final JTextField txt;
	private final JFrame frame;
	private final int fileSelectionMode;
	
	/**
	 * @param txt: JTextField, will read default file from this field, and will write selected file to this field.
	 * @param fileSelectionMode: one of JFileChooser.DIRECTORIES_ONLY, JFileChooser.FILES_ONLY or JFileChooser.FILES_AND_DIRECTORIES
	 */
	public BrowseButtonActionListener (JTextField txt, JFrame frame, int fileSelectionMode)
	{
		this.txt = txt;
		this.frame = frame;
		this.fileSelectionMode = fileSelectionMode;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(fileSelectionMode);
		jfc.setCurrentDirectory(new File(txt.getText()));
		if (jfc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
			txt.setText("" + jfc.getSelectedFile());
	}
	
}
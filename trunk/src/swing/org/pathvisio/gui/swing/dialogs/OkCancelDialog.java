// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.gui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * The basis for a dialog with ok / cancel buttons at the bottom
 * 
 * The central panel can have arbitrarily complex contents 
 */
public abstract class OkCancelDialog extends JDialog implements ActionListener {
	public static final String OK = "Ok";
	public static final String CANCEL = "Cancel";
	
	private String exitCode = CANCEL;
	JButton setButton;
	
	public OkCancelDialog(Frame frame, String title, Component locationComp, boolean modal, boolean cancellable) {
		super((frame == null && locationComp != null) ? JOptionPane.getFrameForComponent(locationComp) : frame, 
				title, modal);
		JButton cancelButton = new JButton(CANCEL);
		cancelButton.addActionListener(this);

		setButton = new JButton(OK);
		setButton.setActionCommand(OK);
		setButton.addActionListener(this);
		getRootPane().setDefaultButton(setButton);

		Component dialogPane = createDialogPane();
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		if(cancellable) {
			buttonPane.add(cancelButton);
			buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		}
		buttonPane.add(setButton);

		Container contentPane = getContentPane();
		contentPane.add(dialogPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
		pack();
		setLocationRelativeTo(null); //Center on screen
		
		//Make buttons respond to pressing 'Enter'
		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
	}
	
	public OkCancelDialog(Frame frame, String title, Component locationComp, boolean modal) {
		this(frame, title, locationComp, modal, true);
	}

	protected abstract Component createDialogPane();
	
	public String getExitCode() {
		return exitCode;
	}
	
	protected void okPressed() {
		setButton.requestFocus(); //Fix for bug #228
								 //Request focus to allow possible open celleditors 
								 //in this dialog to apply the current value
		exitCode = OK;
		setVisible(false);
	}
	
	protected void cancelPressed() {
		exitCode = CANCEL;
		setVisible(false);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (OK.equals(e.getActionCommand())) {
			okPressed();
		}
		if(CANCEL.equals(e.getActionCommand())) {
			cancelPressed();
		}
	}
}

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
package org.pathvisio.gui.swing.progress;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.pathvisio.util.ProgressKeeper.ProgressEvent;
import org.pathvisio.util.ProgressKeeper.ProgressListener;

public class ProgressDialog extends JDialog implements ActionListener, ProgressListener {
	private final String CANCEL = "Cancel";
	
	SwingProgressKeeper keeper;
	JPanel dialogPane;

	public ProgressDialog(Frame frame, String title, SwingProgressKeeper progressKeeper, boolean canCancel, boolean modal) {
		super(frame, title, modal);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		keeper = progressKeeper;
		keeper.addListener(this);
		
		dialogPane = new JPanel();
		dialogPane.setLayout(new GridLayout());
		dialogPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		dialogPane.add(keeper.getJProgressBar());
				
		Container contentPane = getContentPane();
		contentPane.add(dialogPane, BorderLayout.CENTER);

		if(canCancel) {
			JButton cancelButton = new JButton(CANCEL);
			cancelButton.addActionListener(this);
			getRootPane().setDefaultButton(cancelButton);

			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
			buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			buttonPane.add(cancelButton);
			contentPane.add(buttonPane, BorderLayout.PAGE_END);
		}
		pack();
		setLocationRelativeTo(frame);
	}
	
	protected void cancelPressed() {
		keeper.cancel();
		setVisible(false);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(CANCEL.equals(e.getActionCommand())) {
			cancelPressed();
		}
	}

	public void progressFinished(ProgressEvent e) {
		switch(e.getType()) {
		case ProgressEvent.FINISHED:
			setVisible(false);
		case ProgressEvent.TASK_NAME_CHANGED:
			setTitle(keeper.getTaskName());	
		case ProgressEvent.REPORT:
			String task = keeper.getTaskName();
			setTitle(task == null ? keeper.getReport() : task + " - " + keeper.getReport());
		}
	}
}
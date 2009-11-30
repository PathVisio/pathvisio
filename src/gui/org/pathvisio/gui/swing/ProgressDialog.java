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
package org.pathvisio.gui.swing;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.ProgressKeeper.ProgressEvent;
import org.pathvisio.util.ProgressKeeper.ProgressListener;

/**
 * Similar to the swing progress dialog, but this has the option to
 * show a dialog for a task of indeterminate length
 */
public class ProgressDialog extends JDialog implements ActionListener, ProgressListener {

	private static final String CANCEL = "Cancel";

	JLabel task;
	JLabel report;
	ProgressKeeper keeper;
	JPanel dialogPane;
	JProgressBar progressBar;

	public ProgressDialog(Frame frame, String title, ProgressKeeper progressKeeper, boolean canCancel, boolean modal) {
		super(frame, title, modal);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);

		task = new JLabel();
		//task.setPreferredSize(new Dimension (350, 50));
		report = new JLabel();
		//report.setPreferredSize(new Dimension (350, 50));

		keeper = progressKeeper;
		keeper.addListener(this);

		dialogPane = new JPanel();
		dialogPane.setLayout(new FormLayout(
				"3dlu, [200dlu,pref], 3dlu",
				"3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu"));
		CellConstraints cc = new CellConstraints();

		dialogPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		task.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		report.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		dialogPane.add(task, cc.xy (2,2));
		dialogPane.add(report, cc.xy (2,4));

		int totalWork = progressKeeper.getTotalWork();
		progressBar = new JProgressBar();
		if(progressKeeper.isIndeterminate()) {
			progressBar.setIndeterminate(true);
		}
		else
		{
			progressBar.setMaximum(totalWork < 1 ? 1 : totalWork);
		}
		dialogPane.add(progressBar, cc.xy (2,6));

		Container contentPane = getContentPane();
		contentPane.add(dialogPane, BorderLayout.CENTER);

		if(canCancel) {
			JButton cancelButton = new JButton(CANCEL);
			cancelButton.addActionListener(this);
			getRootPane().setDefaultButton(cancelButton);

			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
			buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			buttonPane.add(cancelButton, cc.xy(2,8));
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

	public void progressEvent(ProgressEvent e) {
		switch(e.getType()) {
		case ProgressEvent.FINISHED:
			progressBar.setValue(keeper.getTotalWork());
			setVisible(false);
		case ProgressEvent.TASK_NAME_CHANGED:
			task.setText(keeper.getTaskName());
			pack();
			break;
		case ProgressEvent.REPORT:
			report.setText(keeper.getReport());
			pack();
			break;
		case ProgressEvent.PROGRESS_CHANGED:
			progressBar.setValue(keeper.getProgress());
			break;
		}
	}
}
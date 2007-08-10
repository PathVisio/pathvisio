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

public abstract class OkCancelDialog extends JDialog implements ActionListener {
	public static final String OK = "Ok";
	public static final String CANCEL = "Cancel";
	
	private String exitCode = CANCEL;
	
	public OkCancelDialog(Frame frame, String title, Component locationComp, boolean modal, boolean cancellable) {
		super((frame == null && locationComp != null) ? JOptionPane.getFrameForComponent(locationComp) : frame, 
				title, modal);
		JButton cancelButton = new JButton(CANCEL);
		cancelButton.addActionListener(this);

		final JButton setButton = new JButton(OK);
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
		setLocationRelativeTo(locationComp);
	}
	
	public OkCancelDialog(Frame frame, String title, Component locationComp, boolean modal) {
		this(frame, title, locationComp, modal, true);
	}

	protected abstract Component createDialogPane();
	
	public String getExitCode() {
		return exitCode;
	}
	
	protected void okPressed() {
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

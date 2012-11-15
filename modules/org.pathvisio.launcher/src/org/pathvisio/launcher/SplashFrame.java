package org.pathvisio.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class SplashFrame extends JFrame {

	private JFrame splashFrame;
	private JProgressBar progressBar;
	
	public SplashFrame() {
		splashFrame = this;
		splashFrame.setUndecorated(true);
		
		final JPanel splashPanel = new JPanel();
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setString("");
		final JLabel label = new JLabel("Loading PathVisio...");
		progressBar.setStringPainted(true);
		
		splashPanel.setBorder(BorderFactory.createLineBorder(new Color(133,133,133), 3));
		splashPanel.setLayout(new BoxLayout(splashPanel, BoxLayout.PAGE_AXIS));
		JPanel lblPanel = new JPanel();
		lblPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		lblPanel.add(label);
		splashPanel.add(lblPanel, BorderLayout.CENTER);
		JPanel pPanel = new JPanel();
		pPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		pPanel.add(progressBar);
		splashPanel.add(pPanel, BorderLayout.SOUTH);
		splashPanel.setBackground(Color.WHITE);
		for(Component c : splashPanel.getComponents()) c.setBackground(Color.WHITE);
		
		splashFrame.add(splashPanel);
		splashFrame.setVisible(true);
		splashFrame.setSize(400, 200);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int w = splashFrame.getSize().width;
		int h = splashFrame.getSize().height;
		int x = (dim.width - w) / 2;
		int y = (dim.height - h) / 2;
		splashFrame.setLocation(x, y);
	}
	
	public JProgressBar getProgressBar() {
		return progressBar;
	}
}

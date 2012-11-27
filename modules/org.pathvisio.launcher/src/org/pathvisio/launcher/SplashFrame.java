// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
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
	private JLabel textLabel;
	
	public SplashFrame() {
		splashFrame = this;
		splashFrame.setUndecorated(true);
		
		final JPanel splashPanel = new JPanel();
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setString("");
		JLabel label = new JLabel("Loading PathVisio...");
		progressBar.setStringPainted(true);
		
		splashPanel.setBorder(BorderFactory.createLineBorder(new Color(133,133,133), 3));
		splashPanel.setLayout(new BoxLayout(splashPanel, BoxLayout.PAGE_AXIS));
		JPanel lblPanel = new JPanel();
		lblPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		lblPanel.add(label);
		splashPanel.add(lblPanel, BorderLayout.CENTER);
		JPanel pPanel = new JPanel();
		pPanel.setLayout(new GridLayout(2,1));
		pPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		pPanel.add(progressBar);
		textLabel = new JLabel();
		pPanel.add(textLabel);
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

	public JLabel getTextLabel() {
		return textLabel;
	}
}

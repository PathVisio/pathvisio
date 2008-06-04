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
package org.pathvisio.visualization.plugins;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.pathvisio.data.Sample;
import org.pathvisio.visualization.colorset.ColorSet;
import org.pathvisio.visualization.colorset.ColorSetManager;
import org.pathvisio.visualization.gui.ColorSetChooser;
import org.pathvisio.visualization.gui.ColorSetCombo;
import org.pathvisio.visualization.plugins.ColorByExpression.ConfiguredSample;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Configuration panel for the ColorByExpression visualization
 * method
 * @author thomas
 */
public class ColorByExpressionPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	static final String ACTION_ADVANCED = "Advanced";
	static final String ACTION_BASIC = "Basic";
	static final String ACTION_SAMPLE = "sample";
	static final String ACTION_COMBO = "colorset";
	
	ColorByExpression method;
	Basic basic;
	Advanced advanced;
	CardLayout cardLayout;
	JPanel settings;
	
	public ColorByExpressionPanel(ColorByExpression method) {
		this.method = method;
		
		setLayout(new FormLayout(
				"4dlu, pref, 4dlu, pref, fill:pref:grow, 4dlu",
				"4dlu, pref, 4dlu, fill:pref:grow, 4dlu"
		));
		
		ButtonGroup buttons = new ButtonGroup();
		JRadioButton b_basic = new JRadioButton(ACTION_BASIC);
		b_basic.setActionCommand(ACTION_BASIC);
		b_basic.addActionListener(this);
		buttons.add(b_basic);
		JRadioButton b_advanced = new JRadioButton(ACTION_ADVANCED);
		b_advanced.setActionCommand(ACTION_ADVANCED);
		b_advanced.addActionListener(this);
		buttons.add(b_advanced);
		
		CellConstraints cc = new CellConstraints();
		add(b_basic, cc.xy(2, 2));
		add(b_advanced, cc.xy(4, 2));
		
		settings = new JPanel();
		settings.setBorder(BorderFactory.createEtchedBorder());
		cardLayout = new CardLayout();
		settings.setLayout(cardLayout);
		
		basic = new Basic();
		advanced = new Advanced();
		settings.add(basic, ACTION_BASIC);
		settings.add(advanced, ACTION_ADVANCED);
		
		add(settings, cc.xyw(2, 4, 4));
		
		if(method.isAdvanced()) {
			b_advanced.doClick();
		} else {
			b_basic.doClick();
		}
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if(ACTION_ADVANCED.equals(action) || ACTION_BASIC.equals(action)) {
			basic.refresh();
			advanced.refresh();
			cardLayout.show(settings, action);
		}
	}
	
	class Basic extends JPanel implements ActionListener, ListDataListener {
		private static final long serialVersionUID = 1L;
		
		private SortSampleCheckList sampleList;
		
		ColorSetCombo colorSetCombo; 
		
		public Basic() {
			setLayout(new FormLayout(
					"4dlu, pref, 2dlu, fill:pref:grow, 4dlu",
					"4dlu, pref:grow, 4dlu, pref, 4dlu"
			));
			
			sampleList = new SortSampleCheckList(
					method.getSelectedSamples()
			);
			sampleList.getList().addActionListener(this);
			sampleList.getList().setActionCommand(ACTION_SAMPLE);
			sampleList.getList().getModel().addListDataListener(this);
			ColorSetManager csm = method.getVisualization()
											.getManager().getColorSetManager();
			ColorSetChooser csChooser = new ColorSetChooser(csm);
			colorSetCombo = csChooser.getColorSetCombo();
			colorSetCombo.setActionCommand(ACTION_COMBO);
			colorSetCombo.addActionListener(this);
			
			CellConstraints cc = new CellConstraints();
			add(sampleList, cc.xyw(2, 2, 3));
			add(new JLabel("Color set:"), cc.xy(2, 4));
			add(csChooser, cc.xy(4, 4));
			
			refresh();
		}

		void refresh() {
			ColorSet cs = method.getSingleColorSet();
			if(cs == null) {
				colorSetCombo.setSelectedIndex(0);
			} else {
				colorSetCombo.setSelectedItem(cs);
			}
			sampleList.getList().setSelectedSamples(method.getSelectedSamples());
		}
		
		private void refreshSamples() {
			ArrayList<ConfiguredSample> csamples = new ArrayList<ConfiguredSample>();
			for(Sample s : sampleList.getList().getSelectedSamplesInOrder()) {
				ConfiguredSample cs = method.new ConfiguredSample(s);
				
				cs.setColorSet(colorSetCombo.getSelectedColorSet());
				csamples.add(cs);
			}
			method.setUseSamples(csamples);
		}
		
		public void actionPerformed(ActionEvent e) {
			String action = e.getActionCommand();
			if(ACTION_SAMPLE.equals(action)) {
				refreshSamples();
			} else if(ACTION_COMBO.equals(action)) {
				method.setSingleColorSet(colorSetCombo.getSelectedColorSet());
			}
		}

		public void contentsChanged(ListDataEvent e) {
			refreshSamples();
		}

		public void intervalAdded(ListDataEvent e) {
			refreshSamples();
		}

		public void intervalRemoved(ListDataEvent e) {
			refreshSamples();			
		}
	}
	
	class Advanced extends JPanel implements ActionListener, ListDataListener, ListSelectionListener {
		private static final long serialVersionUID = 1L;
		SortSampleCheckList sampleList;
		ColorSetCombo colorSetCombo;
		SamplePanel samplePanel;
		
		public Advanced() {
			setLayout(new FormLayout(
					"4dlu, fill:pref:grow(0.5), 4dlu, fill:pref:grow(0.5), 4dlu",
					"4dlu, fill:pref:grow, 4dlu"
			));
			
			sampleList = new SortSampleCheckList(
					method.getSelectedSamples()
			);
			sampleList.getList().addActionListener(this);
			sampleList.getList().setActionCommand(ACTION_SAMPLE);
			sampleList.getList().getModel().addListDataListener(this);
			sampleList.getList().addListSelectionListener(this);
			samplePanel = new SamplePanel(null);
			
			refresh();
			CellConstraints cc = new CellConstraints();
			add(sampleList, cc.xy(2, 2));
			add(samplePanel, cc.xy(4, 2));
		}

		void refresh() {
			sampleList.getList().setSelectedSamples(method.getSelectedSamples());
			samplePanel.setInput(method.getConfiguredSample(
					sampleList.getList().getSelectedSample()
			));
		}
		
		public void actionPerformed(ActionEvent e) {
			String action = e.getActionCommand();
			if(ACTION_SAMPLE.equals(action)) {
				refreshSamples();
			} else if(ACTION_COMBO.equals(action)) {
				Sample s = sampleList.getList().getSelectedSample();
				ColorSet colorSet = colorSetCombo.getSelectedColorSet();
				if(s != null && colorSet != null) {
					ConfiguredSample cs = method.getConfiguredSample(s);
					if(cs != null) {
						cs.setColorSet(colorSet);
					}
				}
			}
		}

		private void refreshSamples() {
			ArrayList<ConfiguredSample> csamples = new ArrayList<ConfiguredSample>();
			for(Sample s : sampleList.getList().getSelectedSamplesInOrder()) {
				ConfiguredSample cs = method.getConfiguredSample(s);
				if(cs == null) {
					cs = method.new ConfiguredSample(s);
				}
				csamples.add(cs);
				if(sampleList.getList().getSelectedSample() == s) {
					samplePanel.setInput(cs);
				}
			}
			method.setUseSamples(csamples);
		}
		
		public void contentsChanged(ListDataEvent e) {
			refreshSamples();
		}

		public void intervalAdded(ListDataEvent e) {
			refreshSamples();			
		}

		public void intervalRemoved(ListDataEvent e) {
			refreshSamples();			
		}

		public void valueChanged(ListSelectionEvent e) {
			samplePanel.setInput(
					method.getConfiguredSample(
							sampleList.getList().getSelectedSample()
					)
			);
		}
	}
	
	class SamplePanel extends JPanel implements ActionListener {
		static final String ACTION_IMG = "Use image";
		ConfiguredSample cs;
		ColorSetCombo colorSetCombo;
		JCheckBox imageCheck;
		
		public SamplePanel(ConfiguredSample cs) {
			setInput(cs);
		}
		
		void setInput(ConfiguredSample cs) {
			this.cs = cs;
			removeAll();
			if(cs == null) {
				setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(),
					"Sample settings"
				));
				setLayout(new BorderLayout());
				add(new JLabel("Select a sample to configure"), BorderLayout.CENTER);
			} else {
				setBorder(BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(),
						"Sample settings for " + cs.getSample().getName()
				));
				setContents();
			}
			revalidate();
		}
		
		void setContents() {
			DefaultFormBuilder builder = new DefaultFormBuilder(
					new FormLayout("pref, 4dlu, fill:pref:grow"),
					this
			);
			
			ColorSetManager csm = method.getVisualization()
			.getManager().getColorSetManager();
			ColorSetChooser csChooser = new ColorSetChooser(csm);
			colorSetCombo = csChooser.getColorSetCombo();
			colorSetCombo.setActionCommand(ACTION_COMBO);
			colorSetCombo.addActionListener(this);
			colorSetCombo.setSelectedItem(cs.getColorSet());
			
			imageCheck = new JCheckBox(ACTION_IMG);
			imageCheck.setActionCommand(ACTION_IMG);
			imageCheck.addActionListener(this);
			
			builder.setDefaultDialogBorder();
			builder.append("Color set:", csChooser);
			builder.nextLine();
			builder.append(imageCheck, 3);
		}

		public void actionPerformed(ActionEvent e) {
			String action = e.getActionCommand();
			if(ACTION_COMBO.equals(action)) {
				cs.setColorSet(colorSetCombo.getSelectedColorSet());
			} else if(ACTION_IMG.equals(action)) {
				JOptionPane.showMessageDialog(this, "Not implemented!");
				imageCheck.setEnabled(false);
			}
		}
	}
}

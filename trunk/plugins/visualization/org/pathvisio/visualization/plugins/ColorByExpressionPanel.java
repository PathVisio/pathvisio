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

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.pathvisio.data.Sample;
import org.pathvisio.visualization.colorset.ColorSet;
import org.pathvisio.visualization.colorset.ColorSetCombo;
import org.pathvisio.visualization.colorset.ColorSetManager;
import org.pathvisio.visualization.plugins.ColorByExpression.ConfiguredSample;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Configuration panel for the ColorByExpression visualization
 * method
 * @author thomas
 */
public class ColorByExpressionPanel extends JPanel implements ActionListener {
	static final String ACTION_ADVANCED = "Advanced";
	static final String ACTION_BASIC = "Basic";
	
	ColorByExpression method;
	Basic basic;
	Advanced advanced;
	CardLayout cardLayout;
	JPanel settings;
	
	public ColorByExpressionPanel(ColorByExpression method) {
		this.method = method;
		
		setLayout(new FormLayout(
				"pref, 8dlu, pref, fill:pref:grow",
				"pref, 4dlu, fill:pref:grow"
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
		add(b_basic, cc.xy(1, 1));
		add(b_advanced, cc.xy(3, 1));
		
		settings = new JPanel();
		settings.setBorder(BorderFactory.createEtchedBorder());
		cardLayout = new CardLayout();
		settings.setLayout(cardLayout);
		
		basic = new Basic();
		advanced = new Advanced();
		settings.add(basic, ACTION_BASIC);
		settings.add(advanced, ACTION_ADVANCED);
		
		add(settings, cc.xyw(1, 3, 4));
		
		if(method.isAdvanced()) {
			b_advanced.doClick();
		} else {
			b_basic.doClick();
		}
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if(ACTION_ADVANCED.equals(action) || ACTION_BASIC.equals(action)) {
			cardLayout.show(settings, action);
		}
	}
	
	class Basic extends JPanel implements ItemListener, ActionListener, ListDataListener {
		static final String ACTION_SAMPLE = "sample";
		private SortSampleCheckList sampleList;
		
		public Basic() {
			setLayout(new FormLayout(
					"fill:pref:grow, 2dlu, pref, 4dlu, pref, 2dlu, pref",
					"pref:grow"
			));
			
			sampleList = new SortSampleCheckList(
					method.getSelectedSamples()
			);
			sampleList.getList().addActionListener(this);
			sampleList.getList().getModel().addListDataListener(this);
			
			ColorSetManager csm = method.getVisualization()
											.getManager().getColorSetManager();
			ColorSetCombo csc = new ColorSetCombo(csm);
			csc.setSelectedItem(method.getSingleColorSet());
			CellConstraints cc = new CellConstraints();
			add(sampleList, cc.xy(1, 1));
			add(new JLabel("Color set:"), cc.xy(5, 1, "c, t"));
			add(csc, cc.xy(7, 1, "c, t"));
		}

		public void itemStateChanged(ItemEvent e) {
			if(e.getItem() instanceof ColorSet) {
				method.setSingleColorSet((ColorSet)e.getItem());
			}
		}

		private void refreshSamples() {
			ArrayList<ConfiguredSample> csamples = new ArrayList<ConfiguredSample>();
			for(Sample s : sampleList.getList().getSelectedSamplesInOrder()) {
				ConfiguredSample cs = method.new ConfiguredSample(s);
				cs.setColorSet(method.getSingleColorSet());
				csamples.add(cs);
			}
			method.setUseSamples(csamples);
		}
		
		public void actionPerformed(ActionEvent e) {
			if(ACTION_SAMPLE.equals(e.getActionCommand())) {
				refreshSamples();
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
	
	class Advanced extends JPanel {
		public Advanced() {
			add(new JLabel("Not implemented"));
		}
	}
}

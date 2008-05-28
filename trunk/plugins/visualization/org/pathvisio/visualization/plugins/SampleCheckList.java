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

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;

import org.pathvisio.data.GexManager;
import org.pathvisio.data.Sample;
import org.pathvisio.data.SimpleGex;

public class SampleCheckList extends JCheckBoxList {
	List<JCheckBox> checkboxes = new ArrayList<JCheckBox>();
	Map<JCheckBox, Sample> checkbox2sample = new HashMap<JCheckBox, Sample>();
	Map<Sample, JCheckBox> sample2checkbox = new HashMap<Sample, JCheckBox>();
	
	public SampleCheckList(Collection<? extends Sample> selected) {
		SimpleGex gex = GexManager.getCurrentGex();
		if(gex != null) {			
			setSamples(
					gex.getSamples().values(), selected
			);
		} else {
			setSamples(
					new ArrayList<Sample>(), new ArrayList<Sample>()
			);
		}
	}
	
	public SampleCheckList(Collection<? extends Sample> samples, 
			Collection<? extends Sample> selected) {
		setSamples(samples, selected);
	}
	
	private void setSamples(Collection<? extends Sample> samples, 
			Collection<? extends Sample> selected) {
		for(Sample s : samples) {
			JCheckBox ch = new JCheckBox();
			ch.setText(s.getName());
			checkboxes.add(ch);
			checkbox2sample.put(ch, s);
			sample2checkbox.put(s, ch);
			
			if(selected.contains(s)) {
				ch.setSelected(true);
			}
		}
		setListData(checkboxes.toArray());
	}
	
	/**
	 * Adds an action listener to each checkbox in the list
	 */
	public void addActionListener(ActionListener l) {
		for(JCheckBox ch : checkboxes) {
			ch.addActionListener(l);
		}
	}
	
	/**
	 * Sets the action command for each checkbox in the list
	 */
	public void setActionCommand(String c) {
		for(JCheckBox ch : checkboxes) {
			ch.setActionCommand(c);
		}
	}
	
	public Sample getSample(JCheckBox check) {
		return checkbox2sample.get(check);
	}
	
	public Sample getSelectedSample() {
		return checkbox2sample.get(getSelectedValue());
	}
	
	public boolean isSelected(Sample s) {
		JCheckBox ch = sample2checkbox.get(s);
		if(ch != null) return ch.isSelected();
		else return false;
	}
}

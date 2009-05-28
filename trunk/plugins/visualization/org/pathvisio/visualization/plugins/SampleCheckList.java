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
package org.pathvisio.visualization.plugins;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;

import org.bridgedb.IDMapperException;
import org.pathvisio.debug.Logger;
import org.pathvisio.gex.GexManager;
import org.pathvisio.gex.Sample;
import org.pathvisio.gex.SimpleGex;

/**
 * List of samples, with a checkbox in front of each so the user can select
 * one or more.
 */
public class SampleCheckList extends JCheckBoxList {
	DefaultListModel model = new DefaultListModel();
	Map<JCheckBox, Sample> checkbox2sample = new HashMap<JCheckBox, Sample>();
	Map<Sample, JCheckBox> sample2checkbox = new HashMap<Sample, JCheckBox>();
	
	public SampleCheckList(List<? extends Sample> selection) {
		super(false);
		SimpleGex gex = GexManager.getCurrent().getCurrentGex();
		if(gex != null) {
			List<Sample> samples = new ArrayList<Sample>();
			try
			{
				samples.addAll(gex.getSamples().values());
			}
			catch (IDMapperException ex)
			{
				//TODO: notify user with popup
				Logger.log.error ("Could not fetch samples from database", ex);
			}
			
			Collections.sort(samples);
			
			setSamples(
					samples, selection
			);
		} else {
			setSamples(
					new ArrayList<Sample>(), new ArrayList<Sample>()
			);
		}
	}
	
	public SampleCheckList(List<? extends Sample> samples, 
			List<? extends Sample> selected) {
		super(false);
		setSamples(samples, selected);
	}
	
	private void setSamples(List<? extends Sample> samples, 
			List<? extends Sample> selected) {
		model = new DefaultListModel();
		
		//First add the selected samples in order
		for(Sample s : selected) {
			addSample(s).setSelected(true);
		}
		//Add the remaining samples
		for(Sample s : samples) {			
			if(!selected.contains(s)) {
				addSample(s);
			}
		}
		setModel(model);
	}
	
	private JCheckBox addSample(Sample s) 
	{
		if (s == null) throw new NullPointerException();
		JCheckBox ch = new JCheckBox();
		ch.setText("" + s.getName());
		model.addElement(ch);
		checkbox2sample.put(ch, s);
		sample2checkbox.put(s, ch);
		return ch;
	}
	
	/**
	 * Adds an action listener to each checkbox in the list
	 */
	public void addActionListener(ActionListener l) {
		for(JCheckBox ch : checkbox2sample.keySet()) {
			ch.addActionListener(l);
		}
	}
	
	/**
	 * Sets the action command for each checkbox in the list
	 */
	public void setActionCommand(String c) {
		for(JCheckBox ch : checkbox2sample.keySet()) {
			ch.setActionCommand(c);
		}
	}
	
	public Sample getSample(JCheckBox check) {
		return checkbox2sample.get(check);
	}
	
	public Sample getSelectedSample() {
		return checkbox2sample.get(getSelectedValue());
	}
	
	public void setSelectedSamples(Collection<Sample> select) {
		for(JCheckBox ch : sample2checkbox.values()) {
			ch.setSelected(false);
		}
		for(Sample s : select) {
			JCheckBox ch = sample2checkbox.get(s);
			if(ch != null) {
				ch.setSelected(true);
			}
		}
	}
	
	public boolean isSelected(Sample s) {
		JCheckBox ch = sample2checkbox.get(s);
		if(ch != null) return ch.isSelected();
		else return false;
	}
	
	/**
	 * Get all samples in the list in the order they are displayed
	 */
	public List<Sample> getSamplesInOrder() {
		Object[] sa = model.toArray();
		List<Sample> order = new ArrayList<Sample>();
		for(Object o : sa) {
			order.add(checkbox2sample.get((JCheckBox)o));
		}
		return order;
	}
	
	/**
	 * Get the selected samples in the list in the order they are
	 * displayed
	 */
	public List<Sample> getSelectedSamplesInOrder() {
		Object[] sa = model.toArray();
		List<Sample> order = new ArrayList<Sample>();
		for(Object o : sa) {
			JCheckBox ch = (JCheckBox)o;
			if(ch.isSelected()) {
				order.add(checkbox2sample.get(ch));
			}
		}
		return order;
	}
	
	
	public void moveUp(Sample s) {
		JCheckBox ch = sample2checkbox.get(s);
		if(ch != null) {
			int i = model.indexOf(ch);
			if(i > 0) {
				model.removeElementAt(i);
				model.add(i - 1, ch);
				setSelectedValue(ch, true);
			}
		}
	}
	
	public void moveDown(Sample s) {
		JCheckBox ch = sample2checkbox.get(s);
		if(ch != null) {
			int i = model.indexOf(ch);
			if(i < model.size() - 1) {
				model.removeElementAt(i);
				model.add(i + 1, ch);
				setSelectedValue(ch, true);
			}
		}
	}
	
	public void moveToBottom(Sample s) {
		JCheckBox ch = sample2checkbox.get(s);
		if(ch != null) {
			model.removeElement(ch);
			model.add(model.size() - 1, ch);
			setSelectedValue(ch, true);
		}
	}
	
	public void moveToTop(Sample s) {
		JCheckBox ch = sample2checkbox.get(s);
		if(ch != null) {
			model.removeElement(ch);
			model.add(0, ch);
			setSelectedValue(ch, true);
		}
	}
}

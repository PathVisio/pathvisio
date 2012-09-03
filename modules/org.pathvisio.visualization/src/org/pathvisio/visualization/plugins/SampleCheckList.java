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
package org.pathvisio.visualization.plugins;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;

import org.bridgedb.IDMapperException;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.data.DataException;
import org.pathvisio.data.ISample;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.gex.SimpleGex;

/**
 * List of samples, with a checkbox in front of each so the user can select
 * one or more.
 */
public class SampleCheckList extends JCheckBoxList {
	DefaultListModel model = new DefaultListModel();
	Map<JCheckBox, ISample> checkbox2sample = new HashMap<JCheckBox, ISample>();
	Map<ISample, JCheckBox> sample2checkbox = new HashMap<ISample, JCheckBox>();

	public SampleCheckList(List<? extends ISample> selection, GexManager gexManager) {
		super(false);
		SimpleGex gex = gexManager.getCurrentGex();
		if(gex != null) {
			try
			{
				setSamples(gex.getOrderedSamples(), selection);
			}
			catch (DataException ex)
			{
				//TODO: notify user with popup
				Logger.log.error ("Could not fetch samples from database", ex);
			}

		} else {
			setSamples(
					new ArrayList<ISample>(), new ArrayList<ISample>()
			);
		}
	}

	public SampleCheckList(List<? extends ISample> samples,
			List<? extends ISample> selected) {
		super(false);
		setSamples(samples, selected);
	}

	private void setSamples(List<? extends ISample> samples,
			List<? extends ISample> selected) {
		model = new DefaultListModel();

		//First add the selected samples in order
		for(ISample s : selected) {
			addSample(s).setSelected(true);
		}
		//Add the remaining samples
		for(ISample s : samples) {
			if(!selected.contains(s)) {
				addSample(s);
			}
		}
		setModel(model);
	}

	private JCheckBox addSample(ISample s)
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

	public ISample getSample(JCheckBox check) {
		return checkbox2sample.get(check);
	}

	public ISample getSelectedSample() {
		return checkbox2sample.get(getSelectedValue());
	}

	public void setSelectedSamples(Collection<ISample> select) {
		for(JCheckBox ch : sample2checkbox.values()) {
			ch.setSelected(false);
		}
		for(ISample s : select) {
			JCheckBox ch = sample2checkbox.get(s);
			if(ch != null) {
				ch.setSelected(true);
			}
		}
	}

	public boolean isSelected(ISample s) {
		JCheckBox ch = sample2checkbox.get(s);
		if(ch != null) return ch.isSelected();
		else return false;
	}

	/**
	 * Get all samples in the list in the order they are displayed
	 */
	public List<ISample> getSamplesInOrder() {
		Object[] sa = model.toArray();
		List<ISample> order = new ArrayList<ISample>();
		for(Object o : sa) {
			order.add(checkbox2sample.get((JCheckBox)o));
		}
		return order;
	}

	/**
	 * Get the selected samples in the list in the order they are
	 * displayed
	 */
	public List<ISample> getSelectedSamplesInOrder() {
		Object[] sa = model.toArray();
		List<ISample> order = new ArrayList<ISample>();
		for(Object o : sa) {
			JCheckBox ch = (JCheckBox)o;
			if(ch.isSelected()) {
				order.add(checkbox2sample.get(ch));
			}
		}
		return order;
	}


	public void moveUp(ISample s) {
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

	public void moveDown(ISample s) {
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

	public void moveToBottom(ISample s) {
		JCheckBox ch = sample2checkbox.get(s);
		if(ch != null) {
			model.removeElement(ch);
			model.add(model.size() - 1, ch);
			setSelectedValue(ch, true);
		}
	}

	public void moveToTop(ISample s) {
		JCheckBox ch = sample2checkbox.get(s);
		if(ch != null) {
			model.removeElement(ch);
			model.add(0, ch);
			setSelectedValue(ch, true);
		}
	}
}

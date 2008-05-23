package org.pathvisio.visualization.plugins;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;

import org.pathvisio.data.Sample;

public class SampleCheckList extends JCheckBoxList {
	List<JCheckBox> checkboxes = new ArrayList<JCheckBox>();
	Map<JCheckBox, Sample> checkbox2sample = new HashMap<JCheckBox, Sample>();
	Map<Sample, JCheckBox> sample2checkbox = new HashMap<Sample, JCheckBox>();
	
	public SampleCheckList(Collection<Sample> samples, Collection<Sample> selected) {
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

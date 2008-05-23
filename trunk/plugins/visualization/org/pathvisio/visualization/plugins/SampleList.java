package org.pathvisio.visualization.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;

import org.pathvisio.data.Sample;

public class SampleList extends JCheckBoxList {
	List<JCheckBox> checkboxes = new ArrayList<JCheckBox>();
	Map<JCheckBox, Sample> checkbox2sample = new HashMap<JCheckBox, Sample>();
	Map<Sample, JCheckBox> sample2checkbox = new HashMap<Sample, JCheckBox>();
	
	public SampleList(Collection<Sample> samples) {
		for(Sample s : samples) {
			JCheckBox ch = new JCheckBox();
			checkboxes.add(ch);
			checkbox2sample.put(ch, s);
			sample2checkbox.put(s, ch);
		}
		setListData(checkboxes.toArray());
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

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

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.pathvisio.gex.GexManager;
import org.pathvisio.gex.Sample;
import org.pathvisio.util.Resources;

/**
 * A SampleCheckList with buttons on the right side to
 * change the order of the samples.
 * @author thomas
 *
 */
public class SortSampleCheckList extends JPanel implements ActionListener {
	static final String ACTION_TOP = "Top";
	static final String ACTION_UP = "Up";
	static final String ACTION_DOWN = "Down";
	static final String ACTION_BOTTOM = "Bottom";
	
	static final URL ICON_TOP = Resources.getResourceURL("top.gif");
	static final URL ICON_UP = Resources.getResourceURL("up.gif");
	static final URL ICON_DOWN = Resources.getResourceURL("down.gif");
	static final URL ICON_BOTTOM = Resources.getResourceURL("bottom.gif");
	
	SampleCheckList checkList;
	
	public SortSampleCheckList(List<? extends Sample> selected, GexManager gexManager) {
		checkList = new SampleCheckList(selected, gexManager);

		setLayout(new FormLayout(
				"fill:pref:grow, 2dlu, pref", 
				"fill:pref:grow"
		));
		
		CellConstraints cc = new CellConstraints();
		add(new JScrollPane(checkList), cc.xy(1, 1));
		
		JButton top = new JButton();
		top.setActionCommand(ACTION_TOP);
		top.addActionListener(this);
		top.setIcon(new ImageIcon(ICON_TOP));
	    top.setHorizontalTextPosition(SwingConstants.CENTER);

		JButton up = new JButton();
		up.setActionCommand(ACTION_UP);
		up.addActionListener(this);
		up.setIcon(new ImageIcon(ICON_UP));
		up.setHorizontalTextPosition(SwingConstants.CENTER);
		
		JButton down = new JButton();
		down.setActionCommand(ACTION_DOWN);
		down.addActionListener(this);
		down.setIcon(new ImageIcon(ICON_DOWN));
		down.setHorizontalTextPosition(SwingConstants.CENTER);
		
		JButton bottom = new JButton();
		bottom.setActionCommand(ACTION_BOTTOM);
		bottom.addActionListener(this);
		bottom.setIcon(new ImageIcon(ICON_BOTTOM));
		bottom.setHorizontalTextPosition(SwingConstants.CENTER);
		
		JPanel btnPanel = new JPanel();
		
		ButtonStackBuilder builder = new ButtonStackBuilder(
				new FormLayout("28px"), btnPanel
		);
		builder.addGridded(top);
		builder.addUnrelatedGap();
		builder.addGridded(up);
		builder.addRelatedGap();
		builder.addGridded(down);
		builder.addRelatedGap();
		builder.addGridded(bottom);
		
		add(btnPanel, cc.xy(3,1, "c, c"));
	}

	public SampleCheckList getList() {
		return checkList;
	}
	
	public void actionPerformed(ActionEvent e) {
		Sample s = checkList.getSelectedSample();
		String action = e.getActionCommand();
		if(s != null) {
			if(ACTION_TOP.equals(action)) {
				checkList.moveToTop(s);
			} else if(ACTION_UP.equals(action)) {
				checkList.moveUp(s);
			} else if(ACTION_DOWN.equals(action)) {
				checkList.moveDown(s);
			} else if(ACTION_BOTTOM.equals(action)) {
				checkList.moveToBottom(s);
			}
		}
	}
}
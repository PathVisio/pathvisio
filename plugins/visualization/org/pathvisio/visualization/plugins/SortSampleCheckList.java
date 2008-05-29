package org.pathvisio.visualization.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.pathvisio.Engine;
import org.pathvisio.data.Sample;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
	
	static final URL ICON_TOP = Engine.getCurrent().getResourceURL("top.gif");
	static final URL ICON_UP = Engine.getCurrent().getResourceURL("up.gif");
	static final URL ICON_DOWN = Engine.getCurrent().getResourceURL("down.gif");
	static final URL ICON_BOTTOM = Engine.getCurrent().getResourceURL("bottom.gif");
	
	SampleCheckList checkList;
	
	public SortSampleCheckList(List<? extends Sample> selected) {
		checkList = new SampleCheckList(selected);

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
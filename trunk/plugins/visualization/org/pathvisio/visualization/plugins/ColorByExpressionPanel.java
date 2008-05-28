package org.pathvisio.visualization.plugins;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.pathvisio.visualization.colorset.ColorSetCombo;
import org.pathvisio.visualization.colorset.ColorSetManager;

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
		JRadioButton a_advanced = new JRadioButton(ACTION_ADVANCED);
		a_advanced.setActionCommand(ACTION_ADVANCED);
		a_advanced.addActionListener(this);
		buttons.add(a_advanced);
		
		CellConstraints cc = new CellConstraints();
		add(b_basic, cc.xy(1, 1));
		add(a_advanced, cc.xy(3, 1));
		
		settings = new JPanel();
		settings.setBorder(BorderFactory.createEtchedBorder());
		cardLayout = new CardLayout();
		settings.setLayout(cardLayout);
		
		basic = new Basic();
		advanced = new Advanced();
		settings.add(basic, ACTION_BASIC);
		settings.add(advanced, ACTION_ADVANCED);
		
		add(settings, cc.xyw(1, 3, 4));
		
		b_basic.doClick();
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if(ACTION_ADVANCED.equals(action) || ACTION_BASIC.equals(action)) {
			cardLayout.show(settings, action);
		}
	}
	
	class Basic extends JPanel {
		public Basic() {
			setLayout(new FormLayout(
					"fill:pref:grow, 2dlu, pref, 4dlu, pref, 2dlu, pref",
					"pref:grow"
			));
			
			SampleCheckList sampleList = new SampleCheckList(
					method.getSelectedSamples()
			);
			
			ColorSetManager csm = method.getVisualization()
											.getManager().getColorSetManager();
			ColorSetCombo csc = new ColorSetCombo(csm, csm.getColorSets());
			
			CellConstraints cc = new CellConstraints();
			add(new JScrollPane(sampleList), cc.xy(1, 1));
			add(new JLabel("Color set:"), cc.xy(5, 1, "c, t"));
			add(csc, cc.xy(7, 1, "c, t"));
		}
	}
	
	class Advanced extends JPanel {
		public Advanced() {
			add(new JLabel("Not implemented"));
		}
	}
}

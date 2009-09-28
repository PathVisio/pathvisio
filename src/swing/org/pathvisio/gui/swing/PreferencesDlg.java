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
package org.pathvisio.gui.swing;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.pathvisio.preferences.Preference;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.util.ColorConverter;

/**
 * Global dialog for setting the user preferences.
 */
public class PreferencesDlg 
{
	private DefaultMutableTreeNode createNodes()
	{
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Preferences");
		
		List<String> panelTitles = new ArrayList<String>();
		panelTitles.addAll (panels.keySet());
		
		Collections.sort (panelTitles);

		for (String title : panelTitles)
		{	
			top.add (new DefaultMutableTreeNode (title));
		}
		
		return top;
	}
	
	private Map <String, PreferencePanel> panels = new HashMap <String, PreferencePanel>();
	
	public void addPanel (String title, PreferencePanel panel)
	{
		panels.put (title, panel);
	}
	
	public PreferencePanel.Builder builder()
	{ 
		return new PreferencePanel.Builder(PreferenceManager.getCurrent()); 
	}

	public static class PreferencePanel implements ActionListener
	{
		private JPanel panel;
		private PreferenceManager prefs;
		private List<FieldEditor> editors = new ArrayList<FieldEditor>();
		
		public void apply()
		{
			for (FieldEditor editor : editors)
			{
				editor.apply();
			}
		}

		public void restoreDefault()
		{
			for (FieldEditor editor : editors)
			{
				editor.restoreDefault();
			}
		}
		
		public void reset()
		{
			for (FieldEditor editor : editors)
			{
				editor.reset();
			}
		}

		private abstract class FieldEditor
		{
			protected Preference p;
			FieldEditor (Preference p) { this.p = p; }
			
			/**
			 * Should copy the value of the editing component to
			 * the preference
			 */
			abstract void apply();
			
			/** 
			 * restoreDefault should set the editing component to
			 * the default, but not apply it to the preference yet,
			 * so that the user still has the chance to cancel. 
			 */
			abstract void restoreDefault();
			
			/**
			 * Should copy the value of the preference 
			 * to the editing component.
			 * Is called just before PreferenceDlg is shown.
			 */
			abstract void reset();
		}
		
		private class BooleanFieldEditor extends FieldEditor
		{
			private JCheckBox cb;
			
			BooleanFieldEditor(Preference p, JCheckBox cb)
			{
				super(p);
				this.cb = cb;
			}

			@Override void apply() 
			{
				prefs.setBoolean(p, cb.isSelected());				
			}

			@Override void restoreDefault() 
			{
				cb.setSelected(p.getDefault().equals ("" + true));				
			}

			@Override void reset() 
			{
				cb.setSelected (prefs.getBoolean (p));				
			}
		}
		
		private class ColorFieldEditor extends FieldEditor implements ActionListener
		{
			private JLabel colorLabel;
			
			ColorFieldEditor (Preference p, JLabel colorLabel)
			{
				super(p);
				this.colorLabel = colorLabel;
				colorLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				colorLabel.setOpaque(true);				
			}

			public void actionPerformed (ActionEvent ae) 
			{
				Color newColor = JColorChooser.showDialog(null, "Choose a color", prefs.getColor(p));
				if (newColor != null)
				{
					colorLabel.setBackground(newColor);
				}
			}

			@Override
			void apply() {
				prefs.setColor(p, colorLabel.getBackground());				
			}

			@Override
			void restoreDefault() 
			{
				Color c = ColorConverter.parseColorString(p.getDefault());
				colorLabel.setForeground(c);
				colorLabel.setBackground(c);
			}

			@Override void reset() 
			{
				Color c = prefs.getColor (p);
				colorLabel.setForeground(c);
				colorLabel.setBackground(c);
			}
		}
		private class IntegerFieldEditor extends FieldEditor
		{
			private JTextField txt;
			
			IntegerFieldEditor (Preference p, JTextField txt)
			{
				super(p);
				this.txt = txt;
			}

			@Override void apply() 
			{
				try
				{
					prefs.set (p, "" + Integer.parseInt (txt.getText()));
				}
				catch (NumberFormatException e)
				{
					// ignore, we just leave the old value
				}				
			}

			@Override
			void restoreDefault() {
				txt.setText (p.getDefault());
			}

			@Override void reset() 
			{
				txt.setText(prefs.get(p));
			}
		}
		private class StringFieldEditor extends FieldEditor
		{
			private JTextField txt;
			
			StringFieldEditor (Preference p, JTextField txt)
			{
				super(p);
				this.txt = txt;
			}

			@Override void apply() 
			{
				prefs.set (p, txt.getText());				
			}

			@Override void restoreDefault() {
				txt.setText (p.getDefault());
			}

			@Override void reset() 
			{
				txt.setText (prefs.get(p));
			}
		}
		private class FileFieldEditor extends FieldEditor implements ActionListener
		{
			private JTextField txt;
			
			FileFieldEditor (Preference p, JTextField txt)
			{
				super(p);
				this.txt = txt;
			}

			public void actionPerformed(ActionEvent ae) 
			{
				JFileChooser jfc = new JFileChooser();
				if (jfc.showDialog(null, "Choose") == JFileChooser.APPROVE_OPTION)
				{
					File result = jfc.getSelectedFile();
					txt.setText("" + result);
					prefs.setFile (p, result);
				}
			}

			@Override
			void apply() {
				prefs.set (p, txt.getText());				
			}

			@Override
			void restoreDefault() 
			{
				txt.setText (p.getDefault());
			}
			
			@Override void reset() 
			{
				txt.setText (prefs.get(p));
			}
		}		

		public void actionPerformed(ActionEvent ae)
		{
			restoreDefault();
		}
		
		/** Helper class to build preference panel, based on a list of preferences and their types.
		 * Builds a panel and adds a textfield for a string, checkbox for a boolean, etc. 
		 * <p>
		 * You can't instantiate directly. Use dlg.createBuilder
		 * */
		public static class Builder
		{
			private PreferencePanel result = new PreferencePanel();
			private DefaultFormBuilder builder;
			private FormLayout layout;
				
			/** Can't instantiate directly. Use dlg.createBuilder */
			private Builder(PreferenceManager prefs)
			{
				layout = new FormLayout("left:pref, 6dlu, 50dlu:grow, 4dlu, default"); 
				builder = new DefaultFormBuilder(layout);
				result.prefs = prefs;
			}
			
			public PreferencePanel build()
			{
				JButton btnRestore = new JButton();
				btnRestore.setText ("Restore Defaults");
				btnRestore.addActionListener(result);
				
				builder.append (btnRestore);
				builder.nextLine();

				result.panel = builder.getPanel();
				return result;
			}
			
			public Builder booleanField (Preference p, String desc)
			{
				JCheckBox cb = new JCheckBox (desc);
				BooleanFieldEditor editor = result.new BooleanFieldEditor (p, cb);
				builder.append (cb);
				builder.nextLine();
				result.editors.add(editor);
				return this;
			}
			
			public Builder colorField (Preference p, String desc)
			{
				JButton btnColor = new JButton("Change...");
				JLabel colorLabel = new JLabel("--");
				ColorFieldEditor editor = result.new ColorFieldEditor (p, colorLabel);
				btnColor.addActionListener(editor);
				builder.append (new JLabel (desc));
				builder.append (colorLabel);
				builder.append (btnColor);
				builder.nextLine();
				result.editors.add(editor);
				return this;
			}
			
			
			public Builder integerField (Preference p, String desc, int min, int max)
			{
				//TODO: handle min / max
				JTextField txt = new JTextField(8);
				IntegerFieldEditor editor = result.new IntegerFieldEditor (p, txt); 
				builder.append (new JLabel (desc));
				builder.append (txt);
				builder.nextLine();
				result.editors.add(editor);
				return this;
			}
			
			public Builder stringField (Preference p, String desc)
			{
				JTextField txt = new JTextField(40);
				StringFieldEditor editor = result.new StringFieldEditor (p, txt); 
				builder.append (new JLabel (desc));
				builder.append (txt);
				builder.nextLine();
				result.editors.add(editor);
				return this;
			}
			
			
			public Builder fileField (Preference p, String desc, boolean isDir)
			{
				//TODO: do something with isDir
				JTextField txt = new JTextField(40);
				JButton btnBrowse = new JButton("Browse");
				FileFieldEditor editor = result.new FileFieldEditor (p, txt); 
				btnBrowse.addActionListener(editor);
				builder.append (new JLabel (desc));
				builder.append (txt);
				builder.append (btnBrowse);
				builder.nextLine();
				result.editors.add(editor);
				return this;
			}
			
		}
	}
	
	
	PreferenceManager prefs;
	
	public PreferencesDlg (PreferenceManager prefs)
	{
		this.prefs = prefs;
	}
	
	/**
	 * call this to open the dialog
	 */
	public void createAndShowGUI(SwingEngine swingEngine)
	{
		final JDialog dlg = new JDialog(swingEngine.getFrame(), "Preferences", true);
		dlg.setLayout (new BorderLayout());
		
		DefaultMutableTreeNode top = createNodes();
		
		JPanel pnlButtons = new JPanel();
		JTree trCategories = new JTree(top);
		final JPanel pnlSettings = new JPanel();
				
		JButton btnOk = new JButton();
		btnOk.setText ("OK");
		
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae)
			{
				for (PreferencePanel panel : panels.values())
				{
					panel.apply();
				}
				dlg.setVisible (false);
				dlg.dispose();
			}
		}
		);
		
		JButton btnCancel = new JButton();
		btnCancel.setText ("Cancel");
		btnCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				dlg.setVisible(false);
				dlg.dispose();
			}
		}
		);
		
		final CardLayout cards = new CardLayout();
		pnlSettings.setLayout (cards);
		for (String title : panels.keySet())
		{
			PreferencePanel pp = panels.get(title);
			pp.reset();
			pnlSettings.add (pp.panel, title);
		}

		trCategories.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e) 
			{
				TreePath tp = e.getPath();
				String title = ((TreeNode)tp.getLastPathComponent()).toString();
				cards.show(pnlSettings, title);
			}
		});
		
		pnlButtons.add (btnOk);
		pnlButtons.add (btnCancel);
		
		dlg.add (new JScrollPane (pnlSettings), BorderLayout.CENTER);
		dlg.add (trCategories, BorderLayout.WEST);
		dlg.add (pnlButtons, BorderLayout.SOUTH);
		
		dlg.pack();
		dlg.setLocationRelativeTo(swingEngine.getFrame());
		dlg.setVisible (true);
	}
}


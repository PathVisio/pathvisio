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
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.pathvisio.core.preferences.Preference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.util.ColorConverter;

/**
 * Global dialog for setting the user preferences.
 */
public class PreferencesDlg
{
	public static final String UPDATE_COMMAND = "prefDlg.updated";
	private static final String ROOT_NODE_TITLE = "Preferences";
	private Map <String, PreferencePanel> panels = new HashMap <String, PreferencePanel>();
	private Set<ActionListener> actionListeners = new HashSet<ActionListener>();

	private DefaultMutableTreeNode createNodes()
	{
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(ROOT_NODE_TITLE);

		// sort child panels by name
		List<String> panelTitles = new ArrayList<String>();
		panelTitles.addAll (panels.keySet());
		Collections.sort (panelTitles);
		// organize child nodes
		DefaultMutableTreeNode prevNode = null;
		String prevTitle = null;
		for (String title : panelTitles)
		{
			if (prevTitle != null && title.startsWith(prevTitle + "."))
			{
				prevNode.add (new DefaultMutableTreeNode (title));
			}
			else
			{
				prevNode = new DefaultMutableTreeNode (title);
				prevTitle = title;
				top.add (prevNode);
			}
		}

		// add root node's preference panel last so that it's not treated as a child above
		panels.put(ROOT_NODE_TITLE, new PreferencePanel() {
			private JPanel panel = new JPanel();
			public JPanel getPanel() {
				return panel;
			}
			public void apply() {
			}
			public void reset() {
			}
		});

		return top;
	}

	/**
	 * @param title The title of this panel, that will be visible in the JTree on the left
	 *  side of the dialog. You can use a dot (.) to group panels in the tree: for
	 * 	example title "Display.Colors" will be arranged under "Display" in the tree (but "Display"
	 *  needs to exist). Grouping goes only one level deep.
	 * @param panel use @link{PreferencePanel.builder()} to construct an @link{PreferencePanel}.
	 * @throws IllegalArgumentException if title has already been claimed by another PreferencePanel
	 */
	public void addPanel (String title, PreferencePanel panel)
	{
		if (panels.containsKey(title) || ROOT_NODE_TITLE.equals(title)) {
			throw new IllegalArgumentException("Another panel has already been registered with the title '" + title + "'");
		}
		panels.put (title, panel);
	}

	public void removePanel(PreferencePanel panel) {
		String key = null;
		for (Map.Entry<String, PreferencePanel> e : panels.entrySet()) {
			if (panel == e.getValue()) {
				key = e.getKey();
				break;
			}
		}
		if (key != null) {
			panels.remove(key);
		}
	}

	public DefaultPreferencePanel.Builder builder()
	{
		return new DefaultPreferencePanel.Builder(PreferenceManager.getCurrent());
	}

	/**
	 * PreferencePanel groups a number of preferences.
	 * <p>
	 * Use @link{PreferencePanel.builder()} to create one.
	 * You can use the chained .xxxField() methods to add the preferences that you want to edit,
	 * with a description.
	 */
	public static class DefaultPreferencePanel implements PreferencePanel, ActionListener
	{
		private JPanel panel;
		private PreferenceManager prefs;
		private List<FieldEditor> editors = new ArrayList<FieldEditor>();


		public JPanel getPanel() {
			return panel;
		}

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
			private DefaultPreferencePanel result = new DefaultPreferencePanel();
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

				// add button to the bottom-right of the panel
				CellConstraints cc = new CellConstraints();
				builder.appendRow(RowSpec.decode("fill:pref:grow"));
				builder.add (btnRestore,
						cc.xyw(builder.getColumn(), builder.getRow(), 5, "right, bottom"));


				result.panel = builder.getPanel();
				return result;
			}

			public Builder booleanField (Preference p, String desc)
			{
				JCheckBox cb = new JCheckBox (desc);
				BooleanFieldEditor editor = result.new BooleanFieldEditor (p, cb);
				builder.append (cb, 3);
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
		final JDialog dlg = new JDialog(swingEngine.getFrame(), ROOT_NODE_TITLE, true);
		dlg.setLayout (new BorderLayout());

		DefaultMutableTreeNode top = createNodes();

		JPanel pnlButtons = new JPanel();
		JTree trCategories = new JTree(top)
		{
			// custom drawing of titles: only display part after . for subnodes.
			@Override public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
			{
				String title = "" + value;
				int pos = title.indexOf(".");
				if (pos < 0) return title;
				else return title.substring (pos + 1);
			}
		};
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
				fireUpdateAction();
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
			pnlSettings.add (pp.getPanel(), title);
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

		// set initial selection to root node
		trCategories.setSelectionPath(new TreePath(trCategories.getModel().getRoot()));

		pnlButtons.add (btnOk);
		pnlButtons.add (btnCancel);


		Border padBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		trCategories.setBorder(padBorder);
		pnlSettings.setBorder(padBorder);
		JScrollPane trScroll = new JScrollPane(trCategories);
		trScroll.setMinimumSize(new Dimension(100, 200));
		JScrollPane pnlScroll = new JScrollPane(pnlSettings);
		pnlScroll.setMinimumSize(new Dimension(350, 200));
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, trScroll, pnlScroll);
		dlg.add (splitPane);
		dlg.add (pnlButtons, BorderLayout.SOUTH);

		dlg.pack();
		dlg.setLocationRelativeTo(swingEngine.getFrame());
		dlg.setVisible (true);
	}


	public void addActionListener(ActionListener listener) {
		actionListeners.add(listener);
	}

	private void fireUpdateAction() {

		ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, UPDATE_COMMAND);
		for (ActionListener listener : actionListeners) {
			listener.actionPerformed(evt);
		}
	}
}


package org.pathvisio.desktop.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.util.BrowseButtonActionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RunLocalPluginDialog extends JDialog {

	private JDialog dlg;
	private PvDesktop desktop;
	private JTextField tfDir;
	
	public RunLocalPluginDialog(PvDesktop desktop) {
		super();
		dlg = this;
		this.desktop = desktop;
	}
	
	public void createAndShowGUI() {
		dlg.setPreferredSize(new Dimension(600, 200));
		dlg.setTitle("Plug-in Manager");
		dlg.setBackground(Color.white);
		dlg.setLayout(new BorderLayout());
		dlg.setResizable(false);
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		FormLayout layout = new FormLayout("4dlu, pref, 4dlu, min, 4dlu, min, 4dlu", "4dlu, pref, 4dlu, pref");
		panel.setLayout(layout);
		
		CellConstraints cc = new CellConstraints();
		panel.add(new JLabel("Run local plugins temporarily. Please select directory."), cc.xy(2, 2));
		tfDir = new JTextField();
		JButton browseBtn = new JButton("Browse");
		browseBtn.addActionListener(new BrowseButtonActionListener(tfDir, dlg, JFileChooser.DIRECTORIES_ONLY));
		panel.add(tfDir, cc.xy(2, 4));
		panel.add(browseBtn, cc.xy(4, 4));
		
		JPanel btnPanel = new JPanel();
		btnPanel.setBackground(Color.white);
		
		JButton cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dlg.dispose();
			}
		});
		
		JButton startBtn = new JButton("Start");
		startBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!tfDir.getText().equals("")) {
					File file = new File(tfDir.getText());
					if(file.exists()) {
						Map<String, String> map = desktop.getPluginManagerExternal().runLocalPlugin(file);
						desktop.getPluginManager().startPlugins();
						dlg.dispose();
						StatusDialog status = new StatusDialog(desktop);
						status.createAndShowGUI(map);
					}
				}
			}
		});
	
		btnPanel.add(startBtn);
		btnPanel.add(cancelBtn);
		
		dlg.add(btnPanel, BorderLayout.SOUTH);
		dlg.add(panel, BorderLayout.CENTER);

		dlg.setAlwaysOnTop(true);
		dlg.setModal(true);
		dlg.pack();
		dlg.setLocationRelativeTo(desktop.getFrame());
		dlg.setVisible(true);
	}
	
	
	private class StatusDialog extends JDialog {
		
		private JDialog dlg;
		private PvDesktop desktop;
		
		public StatusDialog(PvDesktop desktop) {
			super();
			dlg = this;
			this.desktop = desktop;
		}
		
		public void createAndShowGUI(Map<String, String> status) {
			
			Map<String, String> running = new HashMap<String, String>();
			Map<String, String> problems = new HashMap<String, String>();
			
			for(String str : status.keySet()) {
				if(status.get(str).equals("running")) {
					running.put(str, status.get(str));
				} else {
					problems.put(str, status.get(str));
				}
			}
			
			dlg.setPreferredSize(new Dimension(600, 400));
			dlg.setTitle("Run local plugin");
			dlg.setBackground(Color.white);
			dlg.setLayout(new BorderLayout());
			dlg.setResizable(false);
		
			dlg.setLayout(new BorderLayout());
			
			CellConstraints cc = new CellConstraints();
			String rowLayout = "4dlu,pref,";
			for(int i = 0; i < running.size(); i++) {
				rowLayout = rowLayout + "4dlu,pref,";
			}
			rowLayout = rowLayout + "4dlu, 30dlu,pref,";
			for(int i = 0; i < problems.size(); i++) {
				rowLayout = rowLayout + "4dlu,pref,";
			}
			rowLayout = rowLayout + "15dlu";
			FormLayout layout = new FormLayout("4dlu, pref, 4dlu, 150dlu, 4dlu", rowLayout);
			PanelBuilder builder = new PanelBuilder(layout);
			builder.setBackground(Color.white);
			builder.addLabel("Bundles started:", cc.xy(2, 2));
			builder.addSeparator("", cc.xyw(2, 3, 3));
			
			int count = 4;
			for(String b : running.keySet()) {
				builder.add(new JLabel("    " + b), cc.xy(2, count));
				JTextArea ta = new JTextArea(status.get(b));
				ta.setForeground(Color.GREEN);
				ta.setBackground(Color.white);
				ta.setLineWrap(true);
				ta.setEditable(false);
				builder.add(ta, cc.xy(4, count));
				builder.addSeparator("", cc.xyw(2, count+1, 3));
				count = count + 2;
			}
			count = count+1;
			builder.addLabel("Problems occured in:", cc.xy(2, count));
			builder.addSeparator("", cc.xyw(2, count+1, 3));
			count = count+2;
			
			for(String b : problems.keySet()) {
				builder.add(new JLabel("    " + b), cc.xy(2, count));
				JTextArea ta = new JTextArea(status.get(b));
				ta.setBackground(Color.white);
				ta.setForeground(Color.red);
				ta.setLineWrap(true);
				ta.setEditable(false);
				builder.add(ta, cc.xy(4, count));
				builder.addSeparator("", cc.xyw(2, count+1, 3));
				count = count + 2;
			}
			
			JPanel btnPanel = new JPanel();
			btnPanel.setBackground(Color.white);
			
			JButton cancelBtn = new JButton("OK");
			cancelBtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					dlg.dispose();
				}
			});
			
			btnPanel.add(cancelBtn);
			JScrollPane pane = new JScrollPane(builder.getPanel());
			pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			dlg.add(pane, BorderLayout.CENTER);
			dlg.add(btnPanel, BorderLayout.SOUTH);
			
			dlg.setAlwaysOnTop(true);
			dlg.setModal(true);
			dlg.pack();
			dlg.setLocationRelativeTo(desktop.getFrame());
			dlg.setVisible(true);
		}
	}
}

package org.pathvisio.gui.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathvisio.Engine;
import org.pathvisio.gui.swing.CommonActions.CopyAction;
import org.pathvisio.gui.swing.CommonActions.ExportAction;
import org.pathvisio.gui.swing.CommonActions.ImportAction;
import org.pathvisio.gui.swing.CommonActions.NewElementAction;
import org.pathvisio.gui.swing.CommonActions.PasteAction;
import org.pathvisio.gui.swing.CommonActions.SaveAction;
import org.pathvisio.gui.swing.CommonActions.ZoomAction;
import org.pathvisio.gui.swing.propertypanel.PathwayTableModel;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.swing.VPathwaySwing;

import com.mammothsoftware.frwk.ddb.DropDownButton;

public class MainPanel extends JPanel {
	private JSplitPane splitPane;
	private JMenuBar menuBar;
	private JToolBar toolBar;
	private JScrollPane pathwayScrollPane;
	private JScrollPane sidebarScrollPane;
	private JTable propertyTable;
	
	public MainPanel() {
		setLayout(new BorderLayout());
		
		menuBar = new JMenuBar();
		addMenuActions(menuBar);
		toolBar = new JToolBar();
		addToolBarActions(toolBar);
		
		add(toolBar, BorderLayout.NORTH);
		//menuBar will be added by container (JFrame or JApplet)
		
		pathwayScrollPane = new JScrollPane();
		
		final PathwayTableModel model = new PathwayTableModel();
		propertyTable = new JTable(model) {
			public TableCellRenderer getCellRenderer(int row, int column) {
				TableCellRenderer r = model.getCellRenderer(row, column);
				return r == null ? super.getCellRenderer(row, column) : r;
			}
			public TableCellEditor getCellEditor(int row, int column) {
				TableCellEditor e = model.getCellEditor(row, column);
				return e == null ? super.getCellEditor(row, column) : e;
			}
		};
		
		sidebarScrollPane = new JScrollPane(propertyTable);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pathwayScrollPane, sidebarScrollPane);
		splitPane.setResizeWeight(1);
		splitPane.setOneTouchExpandable(true);
		add(splitPane, BorderLayout.CENTER);
	}
	
	protected void addMenuActions(JMenuBar mb) {
		JMenu pathwayMenu = new JMenu("Pathway");
		pathwayMenu.add(new SaveAction());
		pathwayMenu.add(new ImportAction(this));
		pathwayMenu.add(new ExportAction());
		
		JMenu editMenu = new JMenu("Edit");
		editMenu.add(new CopyAction());
		editMenu.add(new PasteAction());
		
		JMenu viewMenu = new JMenu("View");
		JMenu zoomMenu = new JMenu("Zoom");
		viewMenu.add(zoomMenu);
		zoomMenu.add(new ZoomAction(VPathway.ZOOM_TO_FIT));
		zoomMenu.add(new ZoomAction(10));
		zoomMenu.add(new ZoomAction(25));
		zoomMenu.add(new ZoomAction(50));
		zoomMenu.add(new ZoomAction(75));
		zoomMenu.add(new ZoomAction(100));
		zoomMenu.add(new ZoomAction(150));
		zoomMenu.add(new ZoomAction(200));
		
		mb.add(pathwayMenu);
		mb.add(editMenu);
		mb.add(viewMenu);
	}
	
	protected void addToolBarActions(JToolBar tb) {
		tb.add(new SaveAction());
		tb.add(new ImportAction(this));
		tb.add(new ExportAction());
		tb.addSeparator();
		tb.add(new CopyAction());
		tb.add(new PasteAction());
		tb.addSeparator();
		
		tb.addSeparator();
		tb.add(new JLabel("Zoom:", JLabel.LEFT));
		JComboBox combo = new JComboBox(new Object[] { 
				new ZoomAction(VPathway.ZOOM_TO_FIT),
				new ZoomAction(10),
				new ZoomAction(25),
				new ZoomAction(50),
				new ZoomAction(75),
				new ZoomAction(100),
				new ZoomAction(150),
				new ZoomAction(200)
		} );
		combo.setEditable(true);
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox)e.getSource();
				Object s = combo.getSelectedItem();
				if(s instanceof ZoomAction) {
					((ZoomAction)s).actionPerformed(e);
				} else if(s instanceof String) {
					String zs = (String)s;
					try {
						double zf = Double.parseDouble(zs);
						new ZoomAction(zf).actionPerformed(e);
					} catch(Exception ex) {
						//Ignore bad input
					}
				}
			}
		});
		tb.add(combo);
		tb.addSeparator();
		
		tb.add(new NewElementAction(VPathway.NEWGENEPRODUCT));
		tb.add(new NewElementAction(VPathway.NEWLABEL));
		//New line menu
		DropDownButton lineButton = new DropDownButton(new ImageIcon(Engine.getResourceURL("icons/newlinemenu.gif")));
		lineButton.addComponent(new JMenuItem(new NewElementAction(VPathway.NEWLINE)));
		lineButton.addComponent(new JMenuItem(new NewElementAction(VPathway.NEWLINEARROW)));
		lineButton.addComponent(new JMenuItem(new NewElementAction(VPathway.NEWLINEDASHED)));
		lineButton.addComponent(new JMenuItem(new NewElementAction(VPathway.NEWLINEDASHEDARROW)));
		lineButton.setRunFirstItem(true);
		tb.add(lineButton);
		
		tb.add(new NewElementAction(VPathway.NEWRECTANGLE));
		tb.add(new NewElementAction(VPathway.NEWOVAL));
		tb.add(new NewElementAction(VPathway.NEWARC));
		tb.add(new NewElementAction(VPathway.NEWBRACE));
		tb.add(new NewElementAction(VPathway.NEWTBAR));
		
		//New lineshape menu
		DropDownButton lineShapeButton = new DropDownButton(new ImageIcon(Engine.getResourceURL("icons/newlineshapemenu.gif")));
		lineShapeButton.addComponent(new JMenuItem(new NewElementAction(VPathway.NEWLIGANDROUND)));
		lineShapeButton.addComponent(new JMenuItem(new NewElementAction(VPathway.NEWRECEPTORROUND)));
		lineShapeButton.addComponent(new JMenuItem(new NewElementAction(VPathway.NEWLIGANDSQUARE)));
		lineShapeButton.addComponent(new JMenuItem(new NewElementAction(VPathway.NEWRECEPTORSQUARE)));
		lineShapeButton.setRunFirstItem(true);
		tb.add(lineShapeButton);
	}
	
	public JMenuBar getMenuBar() {
		return menuBar;
	}
	
	public JToolBar getToolBar() {
		return toolBar;
	}

	public JScrollPane getScrollPane() {
		return pathwayScrollPane;
	}
}


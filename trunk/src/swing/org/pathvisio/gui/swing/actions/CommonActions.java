// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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
package org.pathvisio.gui.swing.actions;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.Revision;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.biopax.BiopaxElementManager;
import org.pathvisio.biopax.BiopaxReferenceManager;
import org.pathvisio.biopax.reflect.PublicationXRef;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.gui.swing.dialogs.PathwayElementDialog;
import org.pathvisio.gui.swing.dialogs.PublicationXRefDialog;
import org.pathvisio.model.DataNodeType;
import org.pathvisio.model.LineStyle;
import org.pathvisio.model.LineType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.ShapeType;
import org.pathvisio.model.Pathway.StatusFlagEvent;
import org.pathvisio.model.Pathway.StatusFlagListener;
import org.pathvisio.view.AlignType;
import org.pathvisio.view.DefaultTemplates;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.SelectionBox;
import org.pathvisio.view.StackType;
import org.pathvisio.view.Template;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayElement;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.view.VPathwayListener;
import org.pathvisio.view.ViewActions;
import org.pathvisio.view.ViewActions.CopyAction;
import org.pathvisio.view.ViewActions.PasteAction;
import org.pathvisio.view.ViewActions.UndoAction;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 * A collection of {@link Action}s that may be used throughout the program (e.g. in
 * toolbars, menubars and right-click menu). These actions are registered to the proper
 * group in {@ViewActions} when a new {@link VPathway} is created.
 * @author thomas
 * @see {@link ViewActions}
 */
public class CommonActions implements ApplicationEventListener {
	private static URL IMG_SAVE = Engine.getCurrent().getResourceURL("icons/save.gif");
	private static URL IMG_SAVEAS = Engine.getCurrent().getResourceURL("icons/saveas.gif");
	private static URL IMG_IMPORT = Engine.getCurrent().getResourceURL("icons/import.gif");
	private static URL IMG_EXPORT = Engine.getCurrent().getResourceURL("icons/export.gif");
	private static URL IMG_NEW = Engine.getCurrent().getResourceURL("icons/new.gif");
	private static URL IMG_OPEN = Engine.getCurrent().getResourceURL("icons/open.gif");
	private static URL IMG_ABOUT_LOGO = Engine.getCurrent().getResourceURL("images/logo.jpg");
	
	public void applicationEvent(ApplicationEvent e) {
		if(e.getType() == ApplicationEvent.VPATHWAY_CREATED) {
			ViewActions va = ((VPathway)e.getSource()).getViewActions();
			va.registerToGroup(saveAction, 	ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
			va.registerToGroup(saveAsAction,	ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
			va.registerToGroup(importAction, 	ViewActions.GROUP_ENABLE_EDITMODE);
			va.registerToGroup(exportAction, 	ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
			va.registerToGroup(copyAction, 	ViewActions.GROUP_ENABLE_WHEN_SELECTION);
			va.registerToGroup(pasteAction, 	ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
			va.registerToGroup(pasteAction, 	ViewActions.GROUP_ENABLE_EDITMODE);
			va.registerToGroup(zoomActions, 	ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
			va.registerToGroup(alignActions, 	ViewActions.GROUP_ENABLE_EDITMODE);
			va.registerToGroup(alignActions, 	ViewActions.GROUP_ENABLE_WHEN_SELECTION);
			va.registerToGroup(stackActions, 	ViewActions.GROUP_ENABLE_EDITMODE);
			va.registerToGroup(stackActions, 	ViewActions.GROUP_ENABLE_WHEN_SELECTION);
			va.registerToGroup(newElementActions, ViewActions.GROUP_ENABLE_EDITMODE);
			va.registerToGroup(newElementActions, ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);
			
			va.resetGroupStates();
		}
	}
	
	public final Action saveAction = new SaveAction();
	public final Action saveAsAction = new SaveAsAction();
	public final Action importAction = new ImportAction();
	public final Action exportAction = new ExportAction();
	public final Action openAction = new OpenAction();
	public final Action aboutAction = new AboutAction();
	public final Action helpAction = new HelpAction();
	public final Action newAction = new NewAction();
	
	public final Action copyAction = new CopyAction();
	public final Action pasteAction = new PasteAction();
	
	public final Action undoAction = new UndoAction();
	public final Action exitAction = new ExitAction();

	public final Action preferencesAction = new PreferencesAction();
	public final Action selectGeneDbAction = new SelectGeneDbAction();
	public final Action selectMetaboliteDbAction = new SelectMetaboliteDbAction();
	
	public final Action[] zoomActions = new Action[] {
			new ZoomToFitAction(),
			new ZoomAction(10),
			new ZoomAction(25),
			new ZoomAction(50),
			new ZoomAction(75),
			new ZoomAction(100),
			new ZoomAction(150),
			new ZoomAction(200)
	};
	
	public final Action[] alignActions = new Action[] {
			new AlignAction(AlignType.CENTERX),
			new AlignAction(AlignType.CENTERY),
//			new AlignAction(AlignType.LEFT),
//			new AlignAction(AlignType.RIGHT),
//			new AlignAction(AlignType.TOP),
			new AlignAction(AlignType.WIDTH),
			new AlignAction(AlignType.HEIGHT),
	};
	
	public final Action[] stackActions = new Action[] {
			new StackAction(StackType.CENTERX),
			new StackAction(StackType.CENTERY),
//			new StackAction(StackType.LEFT),
//			new StackAction(StackType.RIGHT),
//			new StackAction(StackType.TOP),
//			new StackAction(StackType.BOTTOM)
	};
		
	public final Action[][] newElementActions = new Action[][] {
			new Action[] { 
					new NewElementAction(new DefaultTemplates.DataNodeTemplate(DataNodeType.GENEPRODUCT)) 	
			},
			new Action[] { 
					new NewElementAction(new DefaultTemplates.DataNodeTemplate(DataNodeType.METABOLITE)) 	
			},
			new Action[] { 
					new NewElementAction(new DefaultTemplates.LabelTemplate())	
			},
			new Action[] { 	
					new NewElementAction(new DefaultTemplates.LineTemplate(
							LineStyle.SOLID, LineType.LINE, LineType.LINE)
					),
					new NewElementAction(new DefaultTemplates.LineTemplate(
							LineStyle.SOLID, LineType.LINE, LineType.ARROW)
					),
					new NewElementAction(new DefaultTemplates.LineTemplate(
							LineStyle.DASHED, LineType.LINE, LineType.LINE)
					),
					new NewElementAction(new DefaultTemplates.LineTemplate(
							LineStyle.DASHED, LineType.LINE, LineType.ARROW)
					),
			},
			new Action[] { 
					new NewElementAction(new DefaultTemplates.ShapeTemplate(ShapeType.RECTANGLE)) 
			},
			new Action[] { 
					new NewElementAction(new DefaultTemplates.ShapeTemplate(ShapeType.OVAL)) 
			},
			new Action[] { 
					new NewElementAction(new DefaultTemplates.ShapeTemplate(ShapeType.ARC)) 
			},
			new Action[] { 
					new NewElementAction(new DefaultTemplates.ShapeTemplate(ShapeType.BRACE)) 
			},
			new Action[] { 
					new NewElementAction(new DefaultTemplates.LineTemplate(
							LineStyle.SOLID, LineType.LINE, LineType.TBAR
					)) 
			},
			new Action[] {
					new NewElementAction(new DefaultTemplates.LineTemplate(
							LineStyle.SOLID, LineType.LINE, LineType.LIGAND_ROUND)
					),
					new NewElementAction(new DefaultTemplates.LineTemplate(
							LineStyle.SOLID, LineType.LINE, LineType.RECEPTOR_ROUND)
					),
					new NewElementAction(new DefaultTemplates.LineTemplate(
							LineStyle.SOLID, LineType.LINE, LineType.LIGAND_SQUARE)
					),
					new NewElementAction(new DefaultTemplates.LineTemplate(
							LineStyle.SOLID, LineType.LINE, LineType.RECEPTOR_SQUARE)
					),
			},
			new Action[] { 
					new NewElementAction(new DefaultTemplates.InteractionTemplate()) },
			new Action[] { 
					new NewElementAction(new DefaultTemplates.ReactionTemplate()) },
	};
	
	public CommonActions(Engine e) {
		e.addApplicationEventListener(this);
	}

	public static class ZoomToFitAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		Component parent;
		
		public ZoomToFitAction() 
		{
			super();
			putValue(Action.NAME, toString());
			putValue(Action.SHORT_DESCRIPTION, "Make the pathway fit in the window");
		}
		
		public void actionPerformed(ActionEvent e) 
		{
			VPathway vPathway = Engine.getCurrent().getActiveVPathway();
			if(vPathway != null) 
			{
				double zoomFactor = vPathway.getFitZoomFactor(); 
				vPathway.setPctZoom(zoomFactor);
			}
		}
		
		public String toString()
		{
			return "Fit to window";
		}
	}
	
	public static class ZoomAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		Component parent;
		double zoomFactor;
		
		public ZoomAction(double zf) {
			super();
			zoomFactor = zf;
			String descr = "Set zoom to " + (int)zf + "%";
			putValue(Action.NAME, toString());
			putValue(Action.SHORT_DESCRIPTION, descr);
		}
		
		public void actionPerformed(ActionEvent e) {
			VPathway vPathway = Engine.getCurrent().getActiveVPathway();
			if(vPathway != null) {
				vPathway.setPctZoom(zoomFactor);
			}
		}
		
		public String toString()
		{
			return (int)zoomFactor + "%";
		}
	}
	
	public static class SaveAsAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public SaveAsAction() {
			super();
			putValue(Action.NAME, "Save as");
			putValue(Action.SMALL_ICON, new ImageIcon(IMG_SAVEAS));
			putValue(Action.SHORT_DESCRIPTION, "Save a local copy of the pathway");
			putValue(Action.LONG_DESCRIPTION, "Save a local copy of the pathway");
		}

		public void actionPerformed(ActionEvent e) {
			SwingEngine.getCurrent().savePathwayAs();
		}
	}
	
	public static class SaveAction extends AbstractAction implements StatusFlagListener, ApplicationEventListener {
		private static final long serialVersionUID = 1L;
		boolean forceDisabled;
		
		public SaveAction() {
			super();
			putValue(Action.NAME, "Save");
			putValue(Action.SMALL_ICON, new ImageIcon(IMG_SAVE));
			putValue(Action.SHORT_DESCRIPTION, "Save a local copy of the pathway");
			putValue(Action.LONG_DESCRIPTION, "Save a local copy of the pathway");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
			Engine.getCurrent().addApplicationEventListener(this);
			Pathway p = Engine.getCurrent().getActivePathway();
			if(p != null) {
				p.addStatusFlagListener(this);
				handleStatus(p.hasChanged());
			} else {
				forceDisabled = true;
				setEnabled(false);
			}
		}

		public void actionPerformed(ActionEvent e) {
			SwingEngine.getCurrent().savePathway();
		}

		private void handleStatus(boolean status) {
			forceDisabled = !status;
			setEnabled(status);
		}
		
		public void statusFlagChanged(StatusFlagEvent e) {
			handleStatus(e.getNewStatus());
		}
		
		public void setEnabled(boolean enabled) {
			if(enabled && forceDisabled) {
				return;
			}
			super.setEnabled(enabled);
		}
		
		public void applicationEvent(ApplicationEvent e) {
			if(e.getType() == ApplicationEvent.PATHWAY_NEW ||
					e.getType() == ApplicationEvent.PATHWAY_OPENED) {
				Engine.getCurrent().getActivePathway().addStatusFlagListener(this);
			}
		}
	}
	
	public static class ImportAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public ImportAction() {
			super();
			putValue(NAME, "Import");
			putValue(SMALL_ICON, new ImageIcon(IMG_IMPORT));
			putValue(Action.SHORT_DESCRIPTION, "Import pathway");
			putValue(Action.LONG_DESCRIPTION, "Import a pathway from various file formats");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
		}
		
		public void actionPerformed(ActionEvent e) {
				SwingEngine.getCurrent().importPathway();
		}
	}
	
	public static class ExportAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public ExportAction() {
			super();
			putValue(NAME, "Export");
			putValue(SMALL_ICON, new ImageIcon(IMG_EXPORT));
			putValue(SHORT_DESCRIPTION, "Export pathway");
			putValue(LONG_DESCRIPTION, "Export the pathway to various file formats");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		}
		
		public void actionPerformed(ActionEvent e) {
			SwingEngine.getCurrent().exportPathway();
		}
		
		public void setEnabled(boolean newValue) {
			super.setEnabled(newValue);
		}
	}
			
	public static class NewElementAction extends AbstractAction implements VPathwayListener {
		private static final long serialVersionUID = 1L;

		Template template;
		
		public NewElementAction(Template template) {
			this.template = template;
			putValue(Action.SHORT_DESCRIPTION, template.getDescription());
			putValue(Action.LONG_DESCRIPTION, template.getDescription());
			if(template.getIconLocation() != null) {
				putValue(Action.SMALL_ICON, new ImageIcon(template.getIconLocation()));
			}
		}
			
		public void actionPerformed(ActionEvent e) {
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if(vp != null) {
				vp.addVPathwayListener(this);
				vp.setNewTemplate(template);
			}
		}
		
		public void vPathwayEvent(VPathwayEvent e) {
			if(e.getType() == VPathwayEvent.ELEMENT_ADDED) {
				e.getVPathway().setNewTemplate(null);
			}
		}
	}
	
	public static class StackAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		StackType type;
		
		public StackAction(StackType t) {
			super();
			putValue(NAME, t.getLabel());
			putValue(SMALL_ICON, new ImageIcon(Engine.getCurrent().getResourceURL(t.getIcon())));
			putValue(SHORT_DESCRIPTION, t.getDescription());
			type = t;
		}
		
		public void actionPerformed(ActionEvent e) {
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if(vp != null) vp.stackSelected(type);
		}
	}
	
	public static class AlignAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		AlignType type;

		public AlignAction(AlignType t) {
			super();
			putValue(NAME, t.getLabel());
			putValue(SMALL_ICON, new ImageIcon(Engine.getCurrent().getResourceURL(t.getIcon())));
			putValue(SHORT_DESCRIPTION, t.getDescription());
			type = t;
		}

		public void actionPerformed(ActionEvent e) {
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if(vp != null) vp.alignSelected(type);
		}
	}
	
	private static abstract class PathwayElementDialogAction extends AbstractAction {
		VPathwayElement element;
		Component parent;
		
		public PathwayElementDialogAction(Component parent, VPathwayElement e) {
			super();
			this.parent = parent;
			element = e;
			//If the element is an empty selectionbox,
			//the an empty space on the drawing is clicked
			//Set element to mappinfo so the pathway properties
			//will show up
			if(element instanceof SelectionBox) {
				SelectionBox s = (SelectionBox)element;
				if(s.getSelection().size() == 0) {
					element = element.getDrawing().getMappInfo();
				}
			}
		}
		
		public void actionPerformed(ActionEvent e) {
			if(element instanceof Graphics) {
				PathwayElement p = ((Graphics)element).getPathwayElement();
				PathwayElementDialog pd = PathwayElementDialog.getInstance(
						p, !element.getDrawing().isEditMode(), null, parent);
				if(pd != null) {
					pd.selectPathwayElementPanel(getSelectedPanel());
					pd.setVisible(true);
				}
			}
		}
				
		protected abstract String getSelectedPanel();
	}
	
	public static class AddLiteratureAction extends PathwayElementDialogAction {
		private static final long serialVersionUID = 1L;
		public AddLiteratureAction(Component parent, VPathwayElement e) {
			super(parent, e);
			putValue(NAME, "Add literature reference");
			putValue(SHORT_DESCRIPTION, "Add a literature reference to this element");
			setEnabled(e.getDrawing().isEditMode());
		}
		
		public void actionPerformed(ActionEvent e) {
			if(element instanceof Graphics) {
				PathwayElement pwElm = ((Graphics)element).getPathwayElement();
				BiopaxElementManager em = new BiopaxElementManager(pwElm.getParent());
				BiopaxReferenceManager m = new BiopaxReferenceManager(em, pwElm);
				PublicationXRef xref = new PublicationXRef();
				
				PublicationXRefDialog d = new PublicationXRefDialog(xref, null, parent);
				d.setVisible(true);
				if(d.getExitCode().equals(PublicationXRefDialog.OK)) {
					m.addElementReference(xref);		
				}
			}
		}
		
		protected String getSelectedPanel() {
			return null;
		}
	}
	
	public static class EditLiteratureAction extends PathwayElementDialogAction {
		private static final long serialVersionUID = 1L;

		public EditLiteratureAction(Component parent, VPathwayElement e) {
			super(parent, e);
			putValue(NAME, "Edit literature references");
			putValue(SHORT_DESCRIPTION, "Edit the literature references of this element");
			setEnabled(e.getDrawing().isEditMode());
		}
		
		protected String getSelectedPanel() {
			return PathwayElementDialog.TAB_LITERATURE;
		}
	}
	
	public static class PropertiesAction extends PathwayElementDialogAction {
		private static final long serialVersionUID = 1L;

		public PropertiesAction(Component parent, VPathwayElement e) {
			super(parent, e);
			putValue(NAME, "Properties");
			putValue(SHORT_DESCRIPTION, "View this element's properties");
		}
		
		protected String getSelectedPanel() {
			return PathwayElementDialog.TAB_COMMENTS;
		}
	}

	public static class AboutAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;

		public AboutAction() 
		{
			super();
			putValue(NAME, "About");
			putValue(SHORT_DESCRIPTION, "About " + Globals.APPLICATION_NAME);
			putValue(LONG_DESCRIPTION, "About " + Globals.APPLICATION_NAME);
		}

		public void actionPerformed(ActionEvent e) 
		{
			final JFrame aboutDlg = new JFrame();

						
			JLabel versionLabel = new JLabel (Globals.APPLICATION_VERSION_NAME);
			JLabel revisionLabel = new JLabel (Revision.REVISION);
			JTextArea label = new JTextArea();
			label.setText("R.M.H. Besseling\nS.P.M.Crijns\nI. Kaashoek\nM.M. Palm\n" +
				"E.D. Pelgrim\nT.A.J. Kelder\nM.P. van Iersel\nE. Neuteboom\nE.J. Creusen\nP. Moeskops\nBiGCaT");
			label.setBackground(UIManager.getColor("Label.background"));
			JLabel iconLbl = new JLabel(new ImageIcon (IMG_ABOUT_LOGO));
			
			Box box = Box.createHorizontalBox();
			box.add (iconLbl);	
			box.add (label);
			
			JButton btnOk = new JButton();
			btnOk.setText("OK");
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					aboutDlg.setVisible (false);
					aboutDlg.dispose();
				}
			});
			
			JPanel aboutPanel = new JPanel();
			aboutPanel.setLayout (new BoxLayout (aboutPanel, BoxLayout.Y_AXIS));
			aboutPanel.add (versionLabel);
			aboutPanel.add (revisionLabel);
			aboutPanel.add (box);
			aboutPanel.add (btnOk);			
			
			aboutDlg.setResizable(false);
			aboutDlg.setTitle("About " + Globals.APPLICATION_NAME);
			aboutDlg.add (aboutPanel);
			aboutDlg.pack();
			aboutDlg.setVisible(true);
		}
	}

	public static class HelpAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;

		public HelpAction() 
		{
			super();
			putValue(NAME, "Help");
			putValue(SHORT_DESCRIPTION, "Open online help in a browser window");
			putValue(LONG_DESCRIPTION, "Open online help in a browser window");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		}

		public void actionPerformed(ActionEvent e) 
		{
			//TODO: wrap in thread, progress dialog
			String url = Globals.HELP_URL;
			try
			{
				BrowserLauncher bl = new BrowserLauncher(null);
				bl.openURLinBrowser(url);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public static class OpenAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;

		public OpenAction() 
		{
			super();
			putValue(NAME, "Open");
			putValue(SMALL_ICON, new ImageIcon (IMG_OPEN));
			putValue(SHORT_DESCRIPTION, "Open a pathway file");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) 
		{
			SwingEngine.getCurrent().openPathway();
		}
	}

	public static class NewAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;

		public NewAction() 
		{
			super();
			putValue(NAME, "New");
			putValue(SMALL_ICON, new ImageIcon(IMG_NEW));
			putValue(SHORT_DESCRIPTION, "Start a new, empty pathway");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) 
		{
			SwingEngine.getCurrent().newPathway();
		}
	}

	/**
	 * Exit menu item. Quit the program with System.exit after checking
	 * for unsaved changes
	 */
	public static class ExitAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;

		public ExitAction() 
		{
			super();
			putValue(NAME, "Exit");
			putValue(SHORT_DESCRIPTION, "Exit pathvisio");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) 
		{
			if (SwingEngine.getCurrent().canDiscardPathway())
			{
				System.exit(0);
			}
		}
	}

	public static class SelectMetaboliteDbAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;

		public SelectMetaboliteDbAction() 
		{
			super();
			putValue(NAME, "Select metabolite database");
			putValue(SHORT_DESCRIPTION, "Select metabolite database");
		}

		public void actionPerformed(ActionEvent e) 
		{
			JOptionPane.showMessageDialog(
					null, "Not implemented", "Not implemented", 
					JOptionPane.WARNING_MESSAGE);
		}
	}

	public static class SelectGeneDbAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;

		public SelectGeneDbAction() 
		{
			super();
			putValue(NAME, "Select Gene Database");
			putValue(SHORT_DESCRIPTION, "Select Gene Database");
		}

		public void actionPerformed(ActionEvent e) 
		{
			JOptionPane.showMessageDialog(
					null, "Not implemented", "Not implemented", 
					JOptionPane.WARNING_MESSAGE);		
		}
	}

	public static class PreferencesAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;

		public PreferencesAction() 
		{
			super();
			putValue(NAME, "Preferences");
			putValue(SHORT_DESCRIPTION, "Edit preferences");
		}

		public void actionPerformed(ActionEvent e) 
		{
			JOptionPane.showMessageDialog(
					null, "Not implemented", "Not implemented", 
					JOptionPane.WARNING_MESSAGE);
		}
	}

}

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
package org.pathvisio.gui.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.biopax.BiopaxElementManager;
import org.pathvisio.biopax.BiopaxReferenceManager;
import org.pathvisio.biopax.reflect.PublicationXRef;
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
import org.pathvisio.util.Resources;
import org.pathvisio.view.AlignType;
import org.pathvisio.view.DefaultTemplates;
import org.pathvisio.view.Graphics;
import org.pathvisio.view.Handle;
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


/**
 * A collection of {@link Action}s that may be used throughout the program (e.g. in
 * toolbars, menubars and right-click menu). These actions are registered to the proper
 * group in {@ViewActions} when a new {@link VPathway} is created.
 * @author thomas
 * @see {@link ViewActions}
 */
public class CommonActions implements ApplicationEventListener {
	private static URL IMG_SAVE = Resources.getResourceURL("save.gif");
	private static URL IMG_SAVEAS = Resources.getResourceURL("saveas.gif");
	private static URL IMG_IMPORT = Resources.getResourceURL("import.gif");
	private static URL IMG_EXPORT = Resources.getResourceURL("export.gif");
	
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
	
	public final Action saveAction = new SaveAction(true, false);
	public final Action saveAsAction = new SaveAction(true, true);
	public final Action standaloneSaveAction = new SaveAction(false, false);
	public final Action standaloneSaveAsAction = new SaveAction(false, true);
	
	public final Action importAction = new ImportAction();
	public final Action exportAction = new ExportAction();
	
	public final Action copyAction = new CopyAction();
	public final Action pasteAction = new PasteAction();
	
	public final Action undoAction = new UndoAction();
	public final Action exitAction = new ExitAction();

	public final Action[] zoomActions;
	
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
	
	public CommonActions(Engine e) 
	{
		e.addApplicationEventListener(this);
		zoomActions = new Action[] {
				new ZoomToFitAction(e),
				new ZoomAction(e, 10),
				new ZoomAction(e, 25),
				new ZoomAction(e, 50),
				new ZoomAction(e, 75),
				new ZoomAction(e, 100),
				new ZoomAction(e, 150),
				new ZoomAction(e, 200)
		};
	}

	public static class ZoomToFitAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		Component parent;
		Engine engine;
		
		public ZoomToFitAction(Engine engine) 
		{
			super();
			this.engine = engine;
			putValue(Action.NAME, toString());
			putValue(Action.SHORT_DESCRIPTION, "Make the pathway fit in the window");
		}
		
		public void actionPerformed(ActionEvent e) 
		{
			VPathway vPathway = engine.getActiveVPathway();
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
		
		Engine engine;
		
		public ZoomAction(Engine e, double zf) {
			super();
			this.engine = e;
			zoomFactor = zf;
			String descr = "Set zoom to " + (int)zf + "%";
			putValue(Action.NAME, toString());
			putValue(Action.SHORT_DESCRIPTION, descr);
		}
		
		public void actionPerformed(ActionEvent e) {
			VPathway vPathway = engine.getActiveVPathway();
			if(vPathway != null) {
				vPathway.setPctZoom(zoomFactor);
			}
		}
		
		public String toString()
		{
			return (int)zoomFactor + "%";
		}
	}
	
	public static class SaveAction extends AbstractAction implements StatusFlagListener, ApplicationEventListener {
		private static final long serialVersionUID = 1L;
		boolean forceDisabled;
		boolean isSaveAs; // is either save... or save as...
		
		public SaveAction(boolean wiki, boolean isSaveAs) 
		{
			super();
			this.isSaveAs = isSaveAs;
			if (isSaveAs)
			{
				putValue(Action.NAME, "Save as");
				putValue(Action.SMALL_ICON, new ImageIcon(IMG_SAVEAS));
				putValue(Action.SHORT_DESCRIPTION, wiki ? "Save the pathway under a new name" : "Save a local copy of the pathway");
				putValue(Action.LONG_DESCRIPTION, wiki ? "Save the pathway under a new name" : "Save a local copy of the pathway");
			}
			else
			{
				putValue(Action.NAME, "Save");
				putValue(Action.SMALL_ICON, new ImageIcon(IMG_SAVE));
				putValue(Action.SHORT_DESCRIPTION, wiki ? "Save a local copy of the pathway" : "Save the pathway");
				putValue(Action.LONG_DESCRIPTION, wiki ? "Save a local copy of the pathway" : "Save the pathway");
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
			}
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

		public void actionPerformed(ActionEvent e) 
		{
			if (isSaveAs)
				SwingEngine.getCurrent().savePathwayAs();
			else
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
					e.getType() == ApplicationEvent.PATHWAY_OPENED) 
			{
				Pathway p = Engine.getCurrent().getActivePathway();
				p.addStatusFlagListener(this);
				handleStatus(p.hasChanged());
			}
		}
	}
	
	public static class ImportAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public ImportAction() {
			super();
			putValue(NAME, "Import");
			putValue(SMALL_ICON, new ImageIcon(IMG_IMPORT));
			putValue(Action.SHORT_DESCRIPTION, "Import pathway from a file on your computer");
			putValue(Action.LONG_DESCRIPTION, "Import a pathway from various file formats on your computer");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
		}
		
		public void actionPerformed(ActionEvent e) 
		{
			if (SwingEngine.getCurrent().canDiscardPathway())
			{
				SwingEngine.getCurrent().importPathway();
			}
		}
	}
	
	public static class ExportAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public ExportAction() {
			super();
			putValue(NAME, "Export");
			putValue(SMALL_ICON, new ImageIcon(IMG_EXPORT));
			putValue(SHORT_DESCRIPTION, "Export pathway to a file on your computer");
			putValue(LONG_DESCRIPTION, "Export the pathway to various file formats on your computer");
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
			putValue(SMALL_ICON, new ImageIcon(Resources.getResourceURL(t.getIcon())));
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
			putValue(SMALL_ICON, new ImageIcon(Resources.getResourceURL(t.getIcon())));
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
			//If handle, select parent
			if(element instanceof Handle) {
				element = ((Handle)element).getParent();
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
				SwingEngine.getCurrent().getFrame().dispose();
			}
		}
	}

}

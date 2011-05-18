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
package org.pathvisio.gui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine;
import org.pathvisio.core.Globals;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.core.biopax.BiopaxReferenceManager;
import org.pathvisio.core.biopax.reflect.PublicationXref;
import org.pathvisio.core.model.CellularComponentType;
import org.pathvisio.core.model.ConnectorType;
import org.pathvisio.core.model.DataNodeType;
import org.pathvisio.core.model.LineStyle;
import org.pathvisio.core.model.LineType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.ShapeType;
import org.pathvisio.core.model.Pathway.StatusFlagEvent;
import org.pathvisio.core.model.Pathway.StatusFlagListener;
import org.pathvisio.core.util.Resources;
import org.pathvisio.core.view.DefaultTemplates;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.Handle;
import org.pathvisio.core.view.LayoutType;
import org.pathvisio.core.view.MIMShapes;
import org.pathvisio.core.view.SelectionBox;
import org.pathvisio.core.view.Template;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.core.view.ViewActions;
import org.pathvisio.gui.dialogs.AboutDlg;
import org.pathvisio.gui.dialogs.PathwayElementDialog;
import org.pathvisio.gui.dialogs.PublicationXRefDialog;

/**
 * A collection of {@link Action}s that may be used throughout the program (e.g. in
 * toolbars, menubars and right-click menu). These actions are registered to the proper
 * group in {@ViewActions} when a new {@link VPathway} is created.
 * @author thomas
 * @see {@link ViewActions}
 */
public class CommonActions implements ApplicationEventListener {
	private static final URL IMG_SAVE = Resources.getResourceURL("save.gif");
	private static final URL IMG_SAVEAS = Resources.getResourceURL("saveas.gif");
	private static final URL IMG_IMPORT = Resources.getResourceURL("import.gif");
	private static final URL IMG_EXPORT = Resources.getResourceURL("export.gif");

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
			va.registerToGroup(layoutActions, 	ViewActions.GROUP_ENABLE_EDITMODE);
			va.registerToGroup(layoutActions, 	ViewActions.GROUP_ENABLE_WHEN_SELECTION);
			va.registerToGroup(newElementActions, ViewActions.GROUP_ENABLE_EDITMODE);
			va.registerToGroup(newElementActions, ViewActions.GROUP_ENABLE_VPATHWAY_LOADED);

			va.resetGroupStates();
		}
	}

	public final AboutAction aboutAction;
	public final SaveAction saveAction;
	public final SaveAction saveAsAction;
	public final SaveAction standaloneSaveAction;
	public final SaveAction standaloneSaveAsAction;

	public final Action importAction;
	public final Action exportAction;

	public final Action copyAction;
	public final Action pasteAction;

	public final ViewActions.UndoAction undoAction;
	public final Action exitAction;

	public final Action[] zoomActions;

	public final Action[] layoutActions;

	public final Action[][] newElementActions;
	
	public final Action[] newDatanodeActions;	
	
	public final Action[] newAnnotationActions;	
	
	public final Action[] newTemplateActions;	
	
	public final Action[] newShapeActions;	
	
	public final Action[] newCellularComponentActions;
	
	public final Action[] newInteractionActions;
	
	public final Action[] newRLInteractionActions;
	
	public final Action[] newMIMInteractionActions;

	private final SwingEngine swingEngine;

	public CommonActions(SwingEngine se)
	{
		swingEngine = se;
		Engine e = se.getEngine();
		e.addApplicationEventListener(this);
		zoomActions = new Action[] {
				new ZoomToFitAction(e),
				new ZoomAction(e, 10),
				new ZoomAction(e, 25),
				new ZoomAction(e, 50),
				new ZoomAction(e, 75),
				new ZoomAction(e, 100),
				new ZoomAction(e, 150),
				new ZoomAction(e, 200),
				new ZoomAction(e, 400)
		};

		 layoutActions = new Action[] {
					new LayoutAction(e, LayoutType.ALIGN_CENTERX),
					new LayoutAction(e, LayoutType.ALIGN_CENTERY),
//					new LayoutAction(e, LayoutType.ALIGN_LEFT),
//					new LayoutAction(e, LayoutType.ALIGN_RIGHT),
//					new LayoutAction(e, LayoutType.ALIGN_TOP),
//					new LayoutAction(e, LayoutType.ALIGN_BOTTOM),
					new LayoutAction(e, LayoutType.COMMON_WIDTH),
					new LayoutAction(e, LayoutType.COMMON_HEIGHT),

					new LayoutAction(e, LayoutType.STACK_CENTERX),
					new LayoutAction(e, LayoutType.STACK_CENTERY),
//					new LayoutAction(e, LayoutType.STACK_LEFT),
//					new LayoutAction(e, LayoutType.STACK_RIGHT),
//					new LayoutAction(e, LayoutType.STACK_TOP),
//					new LayoutAction(e, LayoutType.STACK_BOTTOM)
			};
		 newElementActions = new Action[][] {
					new Action[] {
							new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.GENEPRODUCT))
					},
					new Action[] {
							new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.METABOLITE))
					},
					new Action[] {
							new NewElementAction(e, new DefaultTemplates.LabelTemplate())
					},
					new Action[] {
							new NewElementAction(e, new DefaultTemplates.LineTemplate(
									"line", LineStyle.SOLID, LineType.LINE, LineType.LINE, ConnectorType.STRAIGHT)
							),
							new NewElementAction(e, new DefaultTemplates.LineTemplate(
									"arrow", LineStyle.SOLID, LineType.LINE, LineType.ARROW, ConnectorType.STRAIGHT)
							),
							new NewElementAction(e, new DefaultTemplates.LineTemplate(
									"dashedline", LineStyle.DASHED, LineType.LINE, LineType.LINE, ConnectorType.STRAIGHT)
							),
							new NewElementAction(e, new DefaultTemplates.LineTemplate(
									"dashedarrow", LineStyle.DASHED, LineType.LINE, LineType.ARROW, ConnectorType.STRAIGHT)
							),
							new NewElementAction(e, new DefaultTemplates.LineTemplate(
									"elbow", LineStyle.SOLID, LineType.LINE, LineType.LINE, ConnectorType.ELBOW)
							),
							new NewElementAction(e, new DefaultTemplates.LineTemplate(
									"curve", LineStyle.SOLID, LineType.LINE, LineType.LINE, ConnectorType.CURVED)
							),
					},
					new Action[] {
							new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.RECTANGLE))
					},
					new Action[] {
							new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.OVAL))
					},
					new Action[] {
							new NewElementAction(e, new DefaultTemplates.LineTemplate(
									"arrow", LineStyle.SOLID, LineType.LINE, LineType.ARROW, ConnectorType.STRAIGHT)
							),
					},
					//new Action[] {
						//	new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.ARC))
					//},
					//new Action[] {
						//	new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.BRACE))
					//},
					new Action[] {
							new NewElementAction(e, new DefaultTemplates.LineTemplate(
									"tbar", LineStyle.SOLID, LineType.LINE, LineType.TBAR, ConnectorType.STRAIGHT
							))
					},
//					new Action[] {
//							new NewElementAction(e, new DefaultTemplates.LineTemplate(
//									"ligandround", LineStyle.SOLID, LineType.LINE, LineType.LIGAND_ROUND, ConnectorType.STRAIGHT)
//							),
//							new NewElementAction(e, new DefaultTemplates.LineTemplate(
//									"receptorround", LineStyle.SOLID, LineType.LINE, LineType.RECEPTOR_ROUND, ConnectorType.STRAIGHT)
//							),
//							new NewElementAction(e, new DefaultTemplates.LineTemplate(
//									"ligandsquare", LineStyle.SOLID, LineType.LINE, LineType.LIGAND_SQUARE, ConnectorType.STRAIGHT)
//							),
//							new NewElementAction(e, new DefaultTemplates.LineTemplate(
//									"receptorsquare", LineStyle.SOLID, LineType.LINE, LineType.RECEPTOR_SQUARE, ConnectorType.STRAIGHT)
//							),
//					},
					new Action[] {
							new NewElementAction(e, new DefaultTemplates.InteractionTemplate()) },
					new Action[] {
							new NewElementAction(e, new DefaultTemplates.ReactionTemplate()) },

			};
	
		 // actions for "Data nodes" section
		 newDatanodeActions = new Action[] {
				 new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.GENEPRODUCT)),
				 new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.METABOLITE)),
				 new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.PATHWAY)),
				 new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.PROTEIN)),
				 new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.RNA)),
				 new NewElementAction(e, new DefaultTemplates.DataNodeTemplate(DataNodeType.UNKOWN)),
		 };
		
		 // actions for "Annotations" section
		 newAnnotationActions = new Action[] {
				 new NewElementAction(e, new DefaultTemplates.LabelTemplate()),
		 };
		
		 // actions for "Template" section
		 newTemplateActions = new Action[] {
				 new NewElementAction(e, new  DefaultTemplates.InteractionTemplate()),
				 new NewElementAction(e, new  DefaultTemplates.ReactionTemplate()),
		 };
		
		 // actions for "Basic shapes" section
		 newShapeActions = new Action[] {
				 new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.RECTANGLE)),
				 new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.OVAL)),
				 new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.ROUNDED_RECTANGLE)),
				 new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.TRIANGLE)),
				 new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.PENTAGON)),
				 new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.HEXAGON)),
				 new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.ARC)),
				 new NewElementAction(e, new DefaultTemplates.ShapeTemplate(ShapeType.BRACE)),
				 new NewElementAction(e, new DefaultTemplates.ShapeTemplate(MIMShapes.MIM_DEGRADATION_SHAPE)),
		 };
		
		// actions for "Basic interactions" section
		 newInteractionActions = new Action[] {
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "line", LineStyle.SOLID, LineType.LINE, LineType.LINE, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "arrow", LineStyle.SOLID, LineType.LINE, LineType.ARROW, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "dashedline", LineStyle.DASHED, LineType.LINE, LineType.LINE, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "dashedarrow", LineStyle.DASHED, LineType.LINE, LineType.ARROW, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "elbow", LineStyle.SOLID, LineType.LINE, LineType.LINE, ConnectorType.ELBOW)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "curve", LineStyle.SOLID, LineType.LINE, LineType.LINE, ConnectorType.CURVED)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "tbar", LineStyle.SOLID, LineType.LINE, LineType.TBAR, ConnectorType.STRAIGHT
				 )),
		 };
		
		 // actions for "Receptor/ligand interactions" section
		 newRLInteractionActions = new Action[] {
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "ligandround", LineStyle.SOLID, LineType.LINE, LineType.LIGAND_ROUND, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "ligandsquare", LineStyle.SOLID, LineType.LINE, LineType.LIGAND_SQUARE, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "receptorround", LineStyle.SOLID, LineType.LINE, LineType.RECEPTOR_ROUND, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "receptorsquare", LineStyle.SOLID, LineType.LINE, LineType.RECEPTOR_SQUARE, ConnectorType.STRAIGHT)
				 ),
		 };
		 
		 // actions for "Cellular Compartment" section
		 newCellularComponentActions = new Action[] {
				 new NewElementAction(e, new DefaultTemplates.CellularComponentTemplate(ShapeType.ROUNDED_RECTANGLE, CellularComponentType.CELL)),
				 new NewElementAction(e, new DefaultTemplates.CellularComponentTemplate(ShapeType.OVAL, CellularComponentType.NUCLEUS)),
				 new NewElementAction(e, new DefaultTemplates.CellularComponentTemplate(ShapeType.ENDOPLASMICRETICULUM, CellularComponentType.ENDOPLASMICRETICULUM)),
				 new NewElementAction(e, new DefaultTemplates.CellularComponentTemplate(ShapeType.GOLGIAPPARATUS, CellularComponentType.GOLGIAPPARATUS)),
				 new NewElementAction(e, new DefaultTemplates.CellularComponentTemplate(ShapeType.MITOCHONDRIA, CellularComponentType.MITOCHONDRIA)),
				 new NewElementAction(e, new DefaultTemplates.CellularComponentTemplate(ShapeType.SARCOPLASMICRETICULUM, CellularComponentType.SARCOPLASMICRETICULUM)),
				 new NewElementAction(e, new DefaultTemplates.CellularComponentTemplate(ShapeType.ROUNDED_RECTANGLE, CellularComponentType.ORGANELLE)),
				// new NewElementAction(e, new DefaultTemplates.CellularComponentTemplate(ShapeType.OVAL, CellularComponentType.LYSOSOME)),
				// new NewElementAction(e, new DefaultTemplates.CellularComponentTemplate(ShapeType.OVAL, CellularComponentType.NUCLEOLUS)),
				// new NewElementAction(e, new DefaultTemplates.CellularComponentTemplate(ShapeType.OVAL, CellularComponentType.VACUOLE)),
				 new NewElementAction(e, new DefaultTemplates.CellularComponentTemplate(ShapeType.OVAL, CellularComponentType.VESICLE)),
				// new NewElementAction(e, new DefaultTemplates.CellularComponentTemplate(ShapeType.ROUNDED_RECTANGLE, CellularComponentType.CYTOSOL)),
				 new NewElementAction(e, new DefaultTemplates.CellularComponentTemplate(ShapeType.ROUNDED_RECTANGLE, CellularComponentType.EXTRACELLULAR)),
				// new NewElementAction(e, new DefaultTemplates.CellularComponentTemplate(ShapeType.ROUNDED_RECTANGLE, CellularComponentType.MEMBRANE))
		 };
		 
		 // actions for "Molecular Interaction Map Interactions" section
		 newMIMInteractionActions = new Action[] {
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "Necessary stimulation", LineStyle.SOLID, LineType.LINE, MIMShapes.MIM_NECESSARY_STIMULATION, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "Binding", LineStyle.SOLID, LineType.LINE, MIMShapes.MIM_BINDING, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "Conversion", LineStyle.SOLID, LineType.LINE, MIMShapes.MIM_CONVERSION, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "Stimulation", LineStyle.SOLID, LineType.LINE, MIMShapes.MIM_STIMULATION, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "Modification", LineStyle.SOLID, LineType.LINE, MIMShapes.MIM_MODIFICATION, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "Catalysis", LineStyle.SOLID, LineType.LINE, MIMShapes.MIM_CATALYSIS, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "Inhibition", LineStyle.SOLID, LineType.LINE, MIMShapes.MIM_INHIBITION, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "Cleavage", LineStyle.SOLID, LineType.LINE, MIMShapes.MIM_CLEAVAGE, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "Covalent bond", LineStyle.SOLID, LineType.LINE, MIMShapes.MIM_COVALENT_BOND, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "Branching left", LineStyle.SOLID, LineType.LINE, MIMShapes.MIM_BRANCHING_LEFT, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "Branching right", LineStyle.SOLID, LineType.LINE, MIMShapes.MIM_BRANCHING_RIGHT, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "Transcription-translation", LineStyle.SOLID, LineType.LINE, MIMShapes.MIM_TRANSLATION, ConnectorType.STRAIGHT)
				 ),
				 new NewElementAction(e, new DefaultTemplates.LineTemplate(
						 "Gap", LineStyle.SOLID, LineType.LINE, MIMShapes.MIM_GAP, ConnectorType.STRAIGHT)
				 ),
		 };
		
		saveAction = new SaveAction(se, true, false);
		saveAsAction = new SaveAction(se, true, true);
		standaloneSaveAction = new SaveAction(se, false, false);
		standaloneSaveAsAction = new SaveAction(se, false, true);

		undoAction = new ViewActions.UndoAction(se.getEngine());
		copyAction = new ViewActions.CopyAction(se.getEngine());
		pasteAction = new ViewActions.PasteAction(se.getEngine());

		exportAction = new ExportAction(se);
		importAction = new ImportAction(se);
		aboutAction = new AboutAction(se);

		exitAction = new ExitAction(se);
	}

	/** When triggered, zoom percentage is set so that the entire pathway fits in the view */
	public static class ZoomToFitAction extends AbstractAction {

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

	/** Zooms the view to a fixed percentage. The zoom percentage is decided at creation time */
	public static class ZoomAction extends AbstractAction {

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
				vPathway.centeredZoom(zoomFactor);
			}
		}

		public String toString()
		{
			return (int)zoomFactor + "%";
		}
	}

	/**
	 * This action constitutes both the save and save as menu items,
	 * and can save both to the wiki in the case of the applet,
	 * or to file in the case of the standalone application
	 */
	public static class SaveAction extends AbstractAction implements StatusFlagListener, ApplicationEventListener {
		boolean isSaveAs; // is either save... or save as...

		SwingEngine swingEngine;

		public SaveAction(SwingEngine swingEngine, boolean wiki, boolean isSaveAs)
		{
			super();
			this.isSaveAs = isSaveAs;
			this.swingEngine = swingEngine;
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
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			}
			swingEngine.getEngine().addApplicationEventListener(this);
			Pathway p = swingEngine.getEngine().getActivePathway();
			if(p != null) {
				p.addStatusFlagListener(this);
				handleStatus(p.hasChanged());
			} else {
				setEnabled(false);
			}
		}

		public void actionPerformed(ActionEvent e)
		{
			if (isSaveAs)
				swingEngine.savePathwayAs();
			else
				swingEngine.savePathway();
		}

		private void handleStatus(boolean status) {
			if (isSaveAs)
			{
				setEnabled(true);
			}
			else {
				setEnabled(status);
			}
		}

		public void statusFlagChanged(StatusFlagEvent e) {
			handleStatus(e.getNewStatus());
		}

		public void applicationEvent(ApplicationEvent e) {
			if(e.getType() == ApplicationEvent.PATHWAY_NEW ||
					e.getType() == ApplicationEvent.PATHWAY_OPENED)
			{
				Pathway p = swingEngine.getEngine().getActivePathway();
				p.addStatusFlagListener(this);
				handleStatus(p.hasChanged());
			}
		}
	}

	/** Import a Pathway from a different format than GPML, usually that means GenMAPP format */
	public static class ImportAction extends AbstractAction {

		SwingEngine swingEngine;

		public ImportAction(SwingEngine se) {
			super();
			this.swingEngine = se;
			putValue(NAME, "Import");
			putValue(SMALL_ICON, new ImageIcon(IMG_IMPORT));
			putValue(Action.SHORT_DESCRIPTION, "Import pathway from a file on your computer");
			putValue(Action.LONG_DESCRIPTION, "Import a pathway from various file formats on your computer");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e)
		{
			if (swingEngine.canDiscardPathway())
			{
				swingEngine.importPathway();
			}
		}
	}

	/** Export a pathway to a different pathway, raster or vector image format */
	public static class ExportAction extends AbstractAction {

		SwingEngine swingEngine;

		public ExportAction(SwingEngine swingEngine) {
			super();
			this.swingEngine = swingEngine;
			putValue(NAME, "Export");
			putValue(SMALL_ICON, new ImageIcon(IMG_EXPORT));
			putValue(SHORT_DESCRIPTION, "Export pathway to a file on your computer");
			putValue(LONG_DESCRIPTION, "Export the pathway to various file formats on your computer");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			swingEngine.exportPathway();
		}

		public void setEnabled(boolean newValue) {
			super.setEnabled(newValue);
		}
	}

	/** Create a new pathway element or elements based on a {@link Template} */
	public static class NewElementAction extends AbstractAction //implements VPathwayListener
	{

		Template template;

		Engine engine;
		public NewElementAction(Engine engine, Template template)
		{
			this.template = template;
			this.engine = engine;
			putValue(Action.NAME, template.getName());
			putValue(Action.SHORT_DESCRIPTION, template.getDescription());
			putValue(Action.LONG_DESCRIPTION, template.getDescription());
			if(template.getIconLocation() != null) {
				putValue(Action.SMALL_ICON, new ImageIcon(template.getIconLocation()));
			}
		}

		public void actionPerformed(ActionEvent e) {
			VPathway vp = engine.getActiveVPathway();
			if(vp != null) {
//				vp.addVPathwayListener(this);
				vp.setNewTemplate(template);
			}
		}

//		public void vPathwayEvent(VPathwayEvent e) {
//			if(e.getType() == VPathwayEvent.ELEMENT_ADDED) {
//				e.getVPathway().setNewTemplate(null);
//			}
//		}


		@Override
		public String toString() {
			return template.getName();
		}
	}

	/**
	 * Perform simple layout operations such as
	 * aligning, setting common size and distributing evenly.
	 * Note that this doesn't include graph layout algorithms.
	 * see {@link LayoutType} for a list of possible layouts
	 */
	public static class LayoutAction extends AbstractAction {
		LayoutType type;

		Engine engine;
		public LayoutAction(Engine engine, LayoutType t) {
			super();
			this.engine = engine;
			putValue(NAME, t.getLabel());
			putValue(SMALL_ICON, new ImageIcon(Resources.getResourceURL(t.getIcon())));
			putValue(SHORT_DESCRIPTION, t.getDescription());
			type = t;
		}

		public void actionPerformed(ActionEvent e) {
			VPathway vp = engine.getActiveVPathway();
			if(vp != null) vp.layoutSelected(type);
		}
	}

	/**
	 * This is an abstract base class for actions that are triggered from the right-click menu
	 * on a PathwayElement. When the action is triggered, the PathwayElementDialog is shown,
	 * but which tab is shown depends on the implementation of getSelectedPanel
	 */
	private static abstract class PathwayElementDialogAction extends AbstractAction {
		//TODO: use parameterization instead of inheritance to create different PathwayElementDialogActions
		// inheritance is overkill because behaviour of classes is not changed
		VPathwayElement element;
		Component parent;

		SwingEngine swingEngine;

		public PathwayElementDialogAction(SwingEngine swingEngine, Component parent, VPathwayElement e) {
			super();
			this.parent = parent;
			this.swingEngine = swingEngine;
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
			if (element instanceof Handle)
			{
				element = ((Handle)element).getParent();
			}
		}

		public void actionPerformed(ActionEvent e) {
			if(element instanceof Graphics) {
				PathwayElement p = ((Graphics)element).getPathwayElement();
				PathwayElementDialog pd = swingEngine.getPopupDialogHandler().getInstance(
						p, !element.getDrawing().isEditMode(), null, parent);
				if(pd != null) {
					pd.selectPathwayElementPanel(getSelectedPanel());
					pd.setVisible(true);
				}
			}
		}

		/** implement this to determine which tab is selected first when the dialog is shown */
		protected abstract String getSelectedPanel();
	}

	/**
	 * Provides direct access to the literature reference dialog ({@link PublicationXrefDialog}) from
	 * the right click menu.
	 */
	public static class AddLiteratureAction extends PathwayElementDialogAction {
		public AddLiteratureAction(SwingEngine swingEngine, Component parent, VPathwayElement e) {
			super(swingEngine, parent, e);
			putValue(NAME, "Add literature reference");
			putValue(SHORT_DESCRIPTION, "Add a literature reference to this element");
			setEnabled(e.getDrawing().isEditMode());
		}

		public void actionPerformed(ActionEvent e) {
			if(element instanceof Graphics) {
				PathwayElement pwElm = ((Graphics)element).getPathwayElement();
				BiopaxReferenceManager m = pwElm.getBiopaxReferenceManager();
				PublicationXref xref = new PublicationXref();

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

	/**
	 * Pops up the pathway element dialog directly on the literature tab.
	 */
	public static class EditLiteratureAction extends PathwayElementDialogAction {

		public EditLiteratureAction(SwingEngine swingEngine, Component parent, VPathwayElement e) {
			super(swingEngine, parent, e);
			putValue(NAME, "Edit literature references");
			putValue(SHORT_DESCRIPTION, "Edit the literature references of this element");
			setEnabled(e.getDrawing().isEditMode());
		}

		protected String getSelectedPanel() {
			return PathwayElementDialog.TAB_LITERATURE;
		}
	}

	/** Pops up the pathway element dialog directly on the comments tab */
	public static class PropertiesAction extends PathwayElementDialogAction {

		public PropertiesAction(SwingEngine swingEngine, Component parent, VPathwayElement e) {
			super(swingEngine, parent, e);
			putValue(NAME, "Properties");
			putValue(SHORT_DESCRIPTION, "View this element's properties");
		}

		protected String getSelectedPanel() {
			return PathwayElementDialog.TAB_COMMENTS;
		}
	}

	/** Pops up the @{link AboutDlg} */
	public static class AboutAction extends AbstractAction
	{

		SwingEngine swingengine;

		public AboutAction(SwingEngine swingengine)
		{
			super();
			this.swingengine = swingengine;
			putValue(NAME, "About");
			putValue(SHORT_DESCRIPTION, "About " + Globals.APPLICATION_NAME);
			putValue(LONG_DESCRIPTION, "About " + Globals.APPLICATION_NAME);
		}

		public void actionPerformed(ActionEvent e)
		{
			AboutDlg dlg = new AboutDlg (swingengine);
			dlg.createAndShowGUI();
		}
	}

	/**
	 * Exit menu item. Quit the program with System.exit after checking
	 * for unsaved changes
	 */
	public static class ExitAction extends AbstractAction
	{

		SwingEngine swingEngine;

		public ExitAction(SwingEngine swingEngine)
		{
			super();
			this.swingEngine = swingEngine;
			putValue(NAME, "Exit");
			putValue(SHORT_DESCRIPTION, "Exit pathvisio");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		public void actionPerformed(ActionEvent e)
		{
			if (swingEngine.canDiscardPathway())
			{
				swingEngine.getFrame().dispose();
			}
		}
	}
}

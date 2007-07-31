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
import java.io.File;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitor;
import javax.swing.filechooser.FileFilter;

import org.pathvisio.Engine;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.PathwayImporter;
import org.pathvisio.view.AlignType;
import org.pathvisio.view.StackType;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.view.VPathwayListener;

public abstract class CommonActions {
	private static URL IMG_SAVE = Engine.getCurrent().getResourceURL("icons/save.gif");
	private static URL IMG_IMPORT = Engine.getCurrent().getResourceURL("icons/import.gif");
	private static URL IMG_EXPORT = Engine.getCurrent().getResourceURL("icons/export.gif");
	private static URL IMG_COPY= Engine.getCurrent().getResourceURL("icons/copy.gif");
	private static URL IMG_PASTE = Engine.getCurrent().getResourceURL("icons/paste.gif");
	
	public static class ZoomAction extends AbstractAction {
		Component parent;
		double zoomFactor;
		
		public ZoomAction(double zf) {
			zoomFactor = zf;
			String descr = "Set zoom to " + (int)zf + "%";
			putValue(Action.NAME, toString());
			putValue(Action.SHORT_DESCRIPTION, descr);
			putValue(Action.LONG_DESCRIPTION, descr);
		}
		
		public void actionPerformed(ActionEvent e) {
			VPathway vPathway = Engine.getCurrent().getActiveVPathway();
			if(vPathway != null) {
				vPathway.setPctZoom(zoomFactor);
			}
		}
		
		public String toString() {
			if(zoomFactor == VPathway.ZOOM_TO_FIT) {
				return "Fit to window";
			}
			return (int)zoomFactor + "%";
		}
	}
	
	static class SaveLocalAction extends AbstractAction {
		public SaveLocalAction() {
			super("Save", new ImageIcon(IMG_SAVE));
			putValue(Action.SHORT_DESCRIPTION, "Save a local copy of the pathway");
			putValue(Action.LONG_DESCRIPTION, "Save a local copy of the pathway");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	static class SaveToServerAction extends AbstractAction {
		public SaveToServerAction() {
			super("Save", new ImageIcon(IMG_SAVE));
			putValue(Action.SHORT_DESCRIPTION, "Save the pathway");
			putValue(Action.LONG_DESCRIPTION, "Save the pathway");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	static class ImportAction extends AbstractAction {
		MainPanel mainPanel;
		
		public ImportAction(MainPanel parent) {
			super("Import", new ImageIcon(IMG_IMPORT));
			mainPanel = parent;
			putValue(Action.SHORT_DESCRIPTION, "Import pathway");
			putValue(Action.LONG_DESCRIPTION, "Import a pathway from various file formats");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
		}
		
		public void actionPerformed(ActionEvent e) {
				final Component component = (Component)e.getSource();
				//Open file dialog
				JFileChooser jfc = new JFileChooser();
				jfc.setAcceptAllFileFilterUsed(false);
				jfc.setDialogTitle("Import pathway");
				jfc.setDialogType(JFileChooser.OPEN_DIALOG);
				
				for(final PathwayImporter imp : Engine.getCurrent().getPathwayImporters().values()) {
					FileFilter ff = new FileFilter() {
						public boolean accept(File f) {
							if(f.isDirectory()) return true;
							
							String fn = f.toString();
							int i = fn.lastIndexOf('.');
							if(i > 0) {
								String ext = fn.substring(i + 1);
								for(String impExt : imp.getExtensions()) {
									if(impExt.equalsIgnoreCase(ext)) {
										return true;
									}
								}
							}
							return false;
						}

						public String getDescription() {
							StringBuilder exts = new StringBuilder();
							for(String e : imp.getExtensions()) {
								exts.append(".");
								exts.append(e);
								exts.append(", ");
							}
							String str = exts.substring(0, exts.length() - 2);
							return imp.getName() + " (" + str + ")";
						}
					};
					jfc.addChoosableFileFilter(ff);
				}
				
				int status = jfc.showDialog(component, "Import");
				if(status == JFileChooser.APPROVE_OPTION) {
					try {
						
						int totalWork = 1000;
						ProgressMonitor m = new ProgressMonitor(component, "Loading pathway", "Please wait while the pathway is being loaded", 0, 1000);
						m.setProgress(10);
						SwingEngine.getCurrent().importPathway(jfc.getSelectedFile());
						m.setProgress((int)(totalWork*2/3));
						Engine.getCurrent().getActiveVPathway().setEditMode(true);
						m.setProgress(totalWork);
					} catch(ConverterException ex) {
						SwingEngine.getCurrent().handleConverterException(SwingEngine.MSG_UNABLE_IMPORT, component, ex);
					}
				}
		}
	}
	
	static class ExportAction extends AbstractAction {
		public ExportAction() {
			super("Export", new ImageIcon(IMG_EXPORT));
			putValue(Action.SHORT_DESCRIPTION, "Export pathway");
			putValue(Action.LONG_DESCRIPTION, "Export the pathway to various file formats");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		}
		
		public void actionPerformed(ActionEvent e) {
			//TODO
		}
	}
	
	static class CopyAction extends AbstractAction {
		public CopyAction() {
			super("Copy", new ImageIcon(IMG_COPY));
			String descr = "Copy selected pathway objects to clipboard";
			putValue(Action.SHORT_DESCRIPTION, descr);
			putValue(Action.LONG_DESCRIPTION, descr);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			Engine.getCurrent().getActiveVPathway().copyToClipboard();
		}		
	}
	
	static class PasteAction extends AbstractAction {
		public PasteAction() {
			super("Paste", new ImageIcon(IMG_PASTE));
			String descr = "Paste pathway elements from clipboard";
			putValue(Action.SHORT_DESCRIPTION, descr);
			putValue(Action.LONG_DESCRIPTION, descr);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			Engine.getCurrent().getActiveVPathway().pasteFromClipboad();
		}
	}
		
	static class NewElementAction extends AbstractAction implements VPathwayListener {
		int element;
		public NewElementAction(int type) {
			super();
			element = type;
			
			String descr = "";
			URL imageURL = null;
			switch(element) {
			case VPathway.NEWLINE: 
				descr = "Draw new line";
				imageURL = Engine.getCurrent().getResourceURL("icons/newline.gif");
				break;
			case VPathway.NEWLINEARROW:
				descr = "Draw new arrow";
				imageURL = Engine.getCurrent().getResourceURL("icons/newarrow.gif");
				break;
			case VPathway.NEWLINEDASHED:
				descr = "Draw new dashed line";
				imageURL = Engine.getCurrent().getResourceURL("icons/newdashedline.gif");
				break;
			case VPathway.NEWLINEDASHEDARROW:
				descr = "Draw new dashed arrow";
				imageURL = Engine.getCurrent().getResourceURL("icons/newdashedarrow.gif");
				break;
			case VPathway.NEWLABEL:
				descr = "Draw new label";
				imageURL = Engine.getCurrent().getResourceURL("icons/newlabel.gif");
				break;
			case VPathway.NEWARC:
				descr = "Draw new arc";
				imageURL = Engine.getCurrent().getResourceURL("icons/newarc.gif");
				break;
			case VPathway.NEWBRACE:
				descr = "Draw new brace";
				imageURL = Engine.getCurrent().getResourceURL("icons/newbrace.gif");
				break;
			case VPathway.NEWGENEPRODUCT:
				descr = "Draw new data node";
				imageURL = Engine.getCurrent().getResourceURL("icons/newgeneproduct.gif");
				break;
			case VPathway.NEWRECTANGLE:
				descr = "Draw new rectangle";
				imageURL = Engine.getCurrent().getResourceURL("icons/newrectangle.gif");
				break;
			case VPathway.NEWOVAL:
				descr = "Draw new oval";
				imageURL = Engine.getCurrent().getResourceURL("icons/newoval.gif");
				break;
			case VPathway.NEWTBAR:
				descr = "Draw new TBar";
				imageURL = Engine.getCurrent().getResourceURL("icons/newtbar.gif");
				break;
			case VPathway.NEWRECEPTORROUND:
				descr = "Draw new round receptor";
				imageURL = Engine.getCurrent().getResourceURL("icons/newreceptorround.gif");
				break;
			case VPathway.NEWRECEPTORSQUARE:
				descr = "Draw new square receptor";
				imageURL = Engine.getCurrent().getResourceURL("icons/newreceptorsquare.gif");
				break;
			case VPathway.NEWLIGANDROUND:
				descr = "Draw new round ligand";
				imageURL = Engine.getCurrent().getResourceURL("icons/newligandround.gif");
				break;
			case VPathway.NEWLIGANDSQUARE:
				descr = "Draw new square ligand";
				imageURL = Engine.getCurrent().getResourceURL("icons/newligandsquare.gif");
				break;
			case VPathway.NEWLINEMENU:
				imageURL = Engine.getCurrent().getResourceURL("icons/newlinemenu.gif");
				descr = "Draw new line or arrow";
				break;
			case VPathway.NEWLINESHAPEMENU:
				imageURL = Engine.getCurrent().getResourceURL("icons/newlineshapemenu.gif");
				descr = "Draw new ligand or receptor";
				break;
			}
			putValue(Action.SHORT_DESCRIPTION, descr);
			putValue(Action.LONG_DESCRIPTION, descr);
			if(imageURL != null) {
				putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
			}
		}
		
		public void actionPerformed(ActionEvent e) {
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if(vp != null) {
				vp.addVPathwayListener(this);
				vp.setNewGraphics(element);
			}
		}

		public void vPathwayEvent(VPathwayEvent e) {
			if(e.getType() == VPathwayEvent.ELEMENT_ADDED) {
				e.getVPathway().setNewGraphics(VPathway.NEWNONE);	
			}
		}
	}
	
	static class StackAction extends AbstractAction {
		StackType type;
		
		public StackAction(StackType t) {
			super(t.getLabel(), new ImageIcon(Engine.getCurrent().getResourceURL(t.getIcon())));
			putValue(Action.SHORT_DESCRIPTION, t.getDescription());
			type = t;
		}
		
		public void actionPerformed(ActionEvent e) {
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if(vp != null) vp.stackSelected(type);
		}
	}
	
	static class AlignAction extends AbstractAction {
		AlignType type;

		public AlignAction(AlignType t) {
			super(t.getLabel(), new ImageIcon(Engine.getCurrent().getResourceURL(t.getIcon())));
			putValue(Action.SHORT_DESCRIPTION, t.getDescription());
			type = t;
		}

		public void actionPerformed(ActionEvent e) {
			VPathway vp = Engine.getCurrent().getActiveVPathway();
			if(vp != null) vp.alignSelected(type);
		}
	}	
}

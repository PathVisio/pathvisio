package org.pathvisio.cytoscape.superpathways;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.pathvisio.debug.Logger;
import org.pathvisio.util.swing.ListWithPropertiesTableModel;
import org.pathvisio.util.swing.RowWithProperties;
import org.pathvisio.wikipathways.webservice.WSSearchResult;

import org.pathvisio.cytoscape.GpmlPlugin;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.PanelBuilder;


//import CyWikiPathwaysClientGui.ResultRow;

import cytoscape.Cytoscape;
import cytoscape.data.webservice.WebServiceClientManager;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CyMenus;
import cytoscape.view.CytoscapeDesktop;




public class SuperpathwaysPlugin extends CytoscapePlugin {
	
	   private static SuperpathwaysPlugin instance;
		
		/**
		 * Can be used by other plugins to get an instance of the SuperpathwaysPlugin.
		 * @return The instance of SuperpathwaysPlugin, or null if the plugin wasn't initialized
		 * yet by the PluginManager.
		 */
		public static SuperpathwaysPlugin getInstance() {
			return instance;
		}
		
		/**
		 * Initializes the SuperpathwaysPlugin. Should only be called by Cytoscape's plugin manager!
		 * 
		 * Only one instance of this class is allowed, but this constructor can't be made 
		 * private because it's need by the Cytoscape plugin mechanism.
		 */
		public SuperpathwaysPlugin() {
			if(instance != null) {
				throw new RuntimeException("SuperpathwaysPlugin is already instantiated! Use static" +
						" method getInstance instead!");
			}
			
			instance = this;
			Logger.log.setLogLevel(true, false, true, true, true, true);
			

			SuperpathwaysAction action = new SuperpathwaysAction();
			action.setPreferredMenu("Plugins");
			
			CytoscapeDesktop desktop = Cytoscape.getDesktop();
			CyMenus menu = desktop.getCyMenus();
			menu.addAction(action);
	     }
		
		/**
	     * This class gets attached to the menu item.
	     */
	    public class SuperpathwaysAction extends CytoscapeAction {
        	
	        /**
	         * The constructor sets the text that should appear on the menu item.
	         */
	        public SuperpathwaysAction() {
	        	super("Superpathways");}

			
			 /**
	         * This method is called when the user selects the menu item.
	         */
	        @Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
	        	GpmlPlugin gpmlPlugin=GpmlPlugin.getInstance();
	        	SuperpathwaysClient spClient=new SuperpathwaysClient(gpmlPlugin);
				SuperpathwaysGui spGui=new SuperpathwaysGui(spClient);
				JFrame window=spGui.window;
				window.setLocationRelativeTo(Cytoscape.getDesktop());
				window.setVisible(true);
			}
	        }
	        
	       
       }



package preferences;

import gmmlVision.GmmlVision;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.StringFieldEditor;

public class GmmlPreferenceManager extends PreferenceManager {
	
	public GmmlPreferenceManager() {
		super();

		PreferenceNode display = new PreferenceNode("display", new DisplayPage());
	    PreferenceNode colors = new PreferenceNode("colors", new ColorsPage());
	    PreferenceNode directories = new PreferenceNode("directories", new DirectoriesPage());
	    PreferenceNode files = new PreferenceNode("files", new FilesPage());
	    PreferenceNode database = new PreferenceNode("database", new DatabasePage());
	    
	    addToRoot(display);
	    addTo("display", colors);
	    addToRoot(directories);
	    addToRoot(files);
	    addToRoot(database);
	}
	
	private class FilesPage extends FieldEditorPreferencePage {
		public FilesPage() {
			super("Files", GRID);
		}
		
		protected void createFieldEditors() {
			FileFieldEditor f1 = new FileFieldEditor(GmmlPreferences.PREF_FILES_LOG, "Log file:", getFieldEditorParent());
			addField(f1);
		}
	}
	
	private class DirectoriesPage extends FieldEditorPreferencePage {
		public DirectoriesPage() {
			super("Directories", GRID);
			noDefaultAndApplyButton();
		}
		
		protected void createFieldEditors() {
			DirectoryFieldEditor d1 = new DirectoryFieldEditor(GmmlPreferences.DIR_PWFILES,
					"Gpml pathways:", getFieldEditorParent());
			addField(d1);
			
			DirectoryFieldEditor d2 = new DirectoryFieldEditor(GmmlPreferences.PREF_DIR_GDB,
					"Gene databases:", getFieldEditorParent());
			addField(d2);
			
			DirectoryFieldEditor d3 = new DirectoryFieldEditor(GmmlPreferences.PREF_DIR_EXPR,
					"Expression datasets:", getFieldEditorParent());
			addField(d3);

			if(GmmlVision.isUseR()) {
				DirectoryFieldEditor d4 = new DirectoryFieldEditor(GmmlPreferences.PREF_DIR_RDATA,
						"Results from pathway statistics:", getFieldEditorParent());
				addField(d4);
			}
		}
	}
	
	private class DisplayPage extends FieldEditorPreferencePage {
		public DisplayPage() {
			super("Display", GRID);
		}
		
		protected void createFieldEditors() {
			IntegerFieldEditor f = new IntegerFieldEditor(GmmlPreferences.PREF_SIDEPANEL_SIZE,
					"Initial side panel size (percent of window size):", getFieldEditorParent());
			f.setValidRange(0, 100);
			addField(f);
			
		}
	}
	private class ColorsPage extends FieldEditorPreferencePage {
		public ColorsPage() {
			super("Colors", GRID);
		}
		
		protected void createFieldEditors() {
			ColorFieldEditor f1 = new ColorFieldEditor(GmmlPreferences.PREF_COL_NO_CRIT_MET, 
					"Default color for 'no criteria met':", getFieldEditorParent());
			addField(f1);
			ColorFieldEditor f2 = new ColorFieldEditor(GmmlPreferences.PREF_COL_NO_GENE_FOUND, 
					"Default color for 'gene not found':", getFieldEditorParent());
			addField(f2);
			ColorFieldEditor f3 = new ColorFieldEditor(GmmlPreferences.PREF_COL_NO_DATA_FOUND, 
					"Default color for 'no data found':", getFieldEditorParent());
			addField(f3);
			ColorFieldEditor f4 = new ColorFieldEditor(GmmlPreferences.PREF_COL_SELECTED, 
					"Line color for selected objects:", getFieldEditorParent());
			addField(f4);
			ColorFieldEditor f5 = new ColorFieldEditor(GmmlPreferences.PREF_COL_HIGHLIGHTED, 
					"Line color for highlighted objects:", getFieldEditorParent());
			addField(f5);
//			ColorFieldEditor f6 = new ColorFieldEditor(GmmlPreferences.PREF_COL_AMBIGIOUS_REP, 
//					"Color for marking gene products with ambigious reporter:", getFieldEditorParent());
//			addField(f6);
			
		}
	}
	
	private class DatabasePage extends FieldEditorPreferencePage {
		public DatabasePage() {
			super("Database", GRID);
		}
		
		protected void createFieldEditors() {
			StringFieldEditor f1 = new StringFieldEditor(GmmlPreferences.PREF_DB_ENGINE_GDB,
					"Database connector class for gene database:", getFieldEditorParent());
			addField(f1);
			StringFieldEditor f2 = new StringFieldEditor(GmmlPreferences.PREF_DB_ENGINE_EXPR,
					"Database connector class for expression dataset:", getFieldEditorParent());
			addField(f2);
		}
	}
	
}
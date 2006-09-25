package preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;

public class GmmlPreferenceManager extends PreferenceManager {
	
	public GmmlPreferenceManager() {
		super();

		PreferenceNode display = new PreferenceNode("display", new DisplayPage());
	    PreferenceNode colors = new PreferenceNode("colors", new ColorsPage());
	    PreferenceNode directories = new PreferenceNode("directories", new DirectoriesPage());
	    PreferenceNode files = new PreferenceNode("files", new FilesPage());
	    
	    addToRoot(display);
	    addTo("display", colors);
	    addToRoot(directories);
	    addToRoot(files);
	}
	
	private class FilesPage extends FieldEditorPreferencePage {
		public FilesPage() {
			super("Files", GRID);
		}
		
		protected void createFieldEditors() {
			FileFieldEditor f1 = new FileFieldEditor("files.log", "Log file:", getFieldEditorParent());
			addField(f1);
		}
	}
	
	private class DirectoriesPage extends FieldEditorPreferencePage {
		public DirectoriesPage() {
			super("Directories", GRID);
			noDefaultAndApplyButton();
		}
		
		protected void createFieldEditors() {
			DirectoryFieldEditor d1 = new DirectoryFieldEditor("directories.gmmlFiles",
					"Gmml pathways:", getFieldEditorParent());
			addField(d1);
			
			DirectoryFieldEditor d2 = new DirectoryFieldEditor("directories.gdbFiles",
					"Gene databases:", getFieldEditorParent());
			addField(d2);
			
			DirectoryFieldEditor d3 = new DirectoryFieldEditor("directories.exprFiles",
					"Expression datasets:", getFieldEditorParent());
			addField(d3);
		}
	}
	
	private class DisplayPage extends FieldEditorPreferencePage {
		public DisplayPage() {
			super("Display", GRID);
		}
		
		protected void createFieldEditors() {
			IntegerFieldEditor f = new IntegerFieldEditor("display.sidePanelSize",
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
			ColorFieldEditor f1 = new ColorFieldEditor("colors.no_criteria_met", 
					"Default color for 'no criteria met':", getFieldEditorParent());
			addField(f1);
			ColorFieldEditor f2 = new ColorFieldEditor("colors.no_gene_found", 
					"Default color for 'gene not found':", getFieldEditorParent());
			addField(f2);
			ColorFieldEditor f3 = new ColorFieldEditor("colors.no_data_found", 
					"Default color for 'no data found':", getFieldEditorParent());
			addField(f3);
			ColorFieldEditor f4 = new ColorFieldEditor("colors.selectColor", 
					"Line color for selected objects:", getFieldEditorParent());
			addField(f4);
			ColorFieldEditor f5 = new ColorFieldEditor("colors.highlightColor", 
					"Line color for highlighted objects:", getFieldEditorParent());
			addField(f5);
			ColorFieldEditor f6 = new ColorFieldEditor("colors.ambigious_reporter", 
					"Color for marking gene products with ambigious reporter:", getFieldEditorParent());
			addField(f6);
			
		}
	}
}
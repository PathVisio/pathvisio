package preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;

public class GmmlPreferenceManager extends PreferenceManager {
	
	public GmmlPreferenceManager() {
		super();

	    PreferenceNode colors = new PreferenceNode("colors", new ColorsPage());
	    PreferenceNode directories = new PreferenceNode("directories", new DirectoriesPage());
	    
	    addToRoot(colors);
	    addToRoot(directories);
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
		}
	}
}

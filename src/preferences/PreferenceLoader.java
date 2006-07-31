package preferences;

import gmmlVision.GmmlVision;
import graphics.GmmlGraphics;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;

import util.ColorConverter;

public class PreferenceLoader implements IPropertyChangeListener {
	private static final String preferenceFile = "user.properties";
	private static PreferenceStore preferences;
	
	/**
	 * Loads all stored users preferences
	 */
	public static PreferenceStore loadPreferences()
	{
		preferences = new PreferenceStore(preferenceFile);
		preferences.addPropertyChangeListener(new PreferenceLoader());
		
		preferences.setDefault("colors.no_criteria_met", ColorConverter.getRgbString(NO_CRITERIA_MET));
		preferences.setDefault("colors.no_gene_found", ColorConverter.getRgbString(NO_GENE_FOUND));
		preferences.setDefault("colors.no_data_found", ColorConverter.getRgbString(NO_DATA_FOUND));
		preferences.setDefault("colors.selectColor", ColorConverter.getRgbString(SELECTED));
		preferences.setDefault("colors.highlightColor", ColorConverter.getRgbString(HIGHLIGHTED));
		preferences.setDefault("currentGdb", CURRENT_GDB);
		preferences.setDefault("display.sidePanelSize", SIDEPANEL_SIZE);
		
		try {
			preferences.load();
		} catch(Exception e) { 
			try { preferences.save(); } catch(Exception ex) { } 
		}
		return preferences;
	}
	
	// Default values
	//> display
	//|> sidepanel size (percent)
	static int SIDEPANEL_SIZE = 30;
	//|> colors
	static RGB NO_CRITERIA_MET = new RGB(200, 200, 200);
	static RGB NO_GENE_FOUND = new RGB(255, 255, 255);
	static RGB NO_DATA_FOUND = new RGB(100, 100, 100);
	static RGB SELECTED = new RGB(255, 0, 0);
	static RGB HIGHLIGHTED = new RGB(0, 255, 0);
	
	/// current gene database
	static String CURRENT_GDB = "none";


	public void propertyChange(PropertyChangeEvent e) {
		if(e.getProperty().equals("colors.selectColor")) { 
			GmmlGraphics.selectColor = (RGB)e.getNewValue();
		}
		else if(e.getProperty().equals("colors.highlightColor")) {
			GmmlGraphics.selectColor = (RGB)e.getNewValue();
		}
	}
	
	public static RGB getColorProperty(String name) {
		return ColorConverter.parseRgbString(
				GmmlVision.getPreferences().getString(name));
	}
}
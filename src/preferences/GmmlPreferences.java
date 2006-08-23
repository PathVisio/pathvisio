package preferences;

import gmmlVision.GmmlVision;
import graphics.GmmlGraphics;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;

import util.ColorConverter;

/**
 * This class contains all user preferences used in this application
 */
public class GmmlPreferences extends PreferenceStore implements IPropertyChangeListener {
	private static final String preferenceFile = "user.properties";
	
	public GmmlPreferences() {
		this(preferenceFile);
	}
	
	public GmmlPreferences(String fileName) {
		super(fileName);
		loadPreferences();
	}
	
	/**
	 * Loads all stored users preferences and set defaults
	 */
	public void loadPreferences()
	{
		addPropertyChangeListener(this);
		
		setDefault("colors.no_criteria_met", ColorConverter.getRgbString(NO_CRITERIA_MET));
		setDefault("colors.no_gene_found", ColorConverter.getRgbString(NO_GENE_FOUND));
		setDefault("colors.no_data_found", ColorConverter.getRgbString(NO_DATA_FOUND));
		setDefault("colors.selectColor", ColorConverter.getRgbString(SELECTED));
		setDefault("colors.highlightColor", ColorConverter.getRgbString(HIGHLIGHTED));
		setDefault("currentGdb", CURRENT_GDB);
		setDefault("display.sidePanelSize", SIDEPANEL_SIZE);
		
		try {
			load();
		} catch(Exception e) { 
			try { save(); } catch(Exception ex) { } 
		}
	}

	/**
	 * Returns the current value of the {@link RGB}-valued preference with  the given name
	 * @param name the name of the preference
	 * @return the {@link RGB}-valued preference
	 */
	public static RGB getColorProperty(String name) {
		return ColorConverter.parseRgbString(
				GmmlVision.getPreferences().getString(name));
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		if(e.getProperty().equals("colors.selectColor")) { 
			if(e.getNewValue() instanceof RGB) GmmlGraphics.selectColor = (RGB)e.getNewValue();
			else GmmlGraphics.selectColor = ColorConverter.parseRgbString((String)e.getNewValue());
		}
		else if(e.getProperty().equals("colors.highlightColor")) {
			if(e.getNewValue() instanceof RGB) GmmlGraphics.highlightColor = (RGB)e.getNewValue();
			else GmmlGraphics.highlightColor = ColorConverter.parseRgbString((String)e.getNewValue());
		}
	}
	
	// Defaults
	//> display
	//|> sidepanel size (percent)
	static int SIDEPANEL_SIZE = 30;
	//|> colors
	static RGB NO_CRITERIA_MET = new RGB(200, 200, 200);
	static RGB NO_GENE_FOUND = new RGB(255, 255, 255);
	static RGB NO_DATA_FOUND = new RGB(100, 100, 100);
	static RGB SELECTED = new RGB(255, 0, 0);
	static RGB HIGHLIGHTED = new RGB(0, 255, 0);
	
	// current gene database
	static String CURRENT_GDB = "none";
}
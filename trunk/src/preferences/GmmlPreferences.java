package preferences;

import gmmlVision.GmmlVision;
import graphics.GmmlGpColor;
import graphics.GmmlGraphics;

import java.io.File;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;

import util.ColorConverter;

/**
 * This class contains all user preferences used in this application
 */
public class GmmlPreferences extends PreferenceStore implements IPropertyChangeListener {
	private static final File preferenceFile = new File(GmmlVision.getApplicationDir(), ".GmmlVisio");
	
	public GmmlPreferences() {
		this(preferenceFile.toString());
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
		
		setDefault("files.log", new File(GmmlVision.getApplicationDir(), ".GmmlVisioLog").toString());
		setDefault("colors.no_criteria_met", ColorConverter.getRgbString(NO_CRITERIA_MET));
		setDefault("colors.no_gene_found", ColorConverter.getRgbString(NO_GENE_FOUND));
		setDefault("colors.no_data_found", ColorConverter.getRgbString(NO_DATA_FOUND));
		setDefault("colors.selectColor", ColorConverter.getRgbString(SELECTED));
		setDefault("colors.highlightColor", ColorConverter.getRgbString(HIGHLIGHTED));
		setDefault("colors.ambigious_reporter", ColorConverter.getRgbString(AMBIGIOUS_REP));
		setDefault("currentGdb", CURRENT_GDB);
		setDefault("display.sidePanelSize", SIDEPANEL_SIZE);
		
		setDefault("directories.gmmlFiles", DIR_PWFILES);
		setDefault("directories.gdbFiles", DIR_GDBFILES);
		setDefault("directories.exprFiles", DIR_EXPRFILES);
		setDefault("directories.RdataFiles", DIR_RDATAFILES);
		
		try {
			load();
		} catch(Exception e) { 
			try { save(); } catch(Exception ex) { } 
		}
		
		createDataDirectories();
		
	}

	private void createDataDirectories() {
		// For the data directories: if not defined by user, create default directories
		String[] dataProps = new String[] { "gmmlFiles", "gdbFiles", "exprFiles", "RdataFiles" };
		for(String prop : dataProps) {
			File dir = new File(getString("directories." + prop));
			if(!dir.exists()) dir.mkdirs();
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
			GmmlVision.getDrawing().redraw();
		}
		else if(e.getProperty().equals("colors.highlightColor")) {
			if(e.getNewValue() instanceof RGB) GmmlGraphics.highlightColor = (RGB)e.getNewValue();
			else GmmlGraphics.highlightColor = ColorConverter.parseRgbString((String)e.getNewValue());
			GmmlVision.getDrawing().redraw();
		}
		else if(e.getProperty().equals("colors.ambigious_reporter")) {
			if(e.getNewValue() instanceof RGB) GmmlGpColor.color_ambigious = (RGB)e.getNewValue();
			else GmmlGpColor.color_ambigious = ColorConverter.parseRgbString((String)e.getNewValue());
			GmmlVision.getDrawing().redraw();
		}
		else if(e.getProperty().startsWith("directories")) {
			createDataDirectories();
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
	static RGB AMBIGIOUS_REP = new RGB(0, 0, 255);
	
	// current gene database
	static String CURRENT_GDB = "none";

	// directories
	static String DIR_PWFILES = new File(GmmlVision.getDataDir().toString(), "pathways").toString();
	static String DIR_GDBFILES = new File(GmmlVision.getDataDir().toString(), "gene databases").toString();
	static String DIR_EXPRFILES = new File(GmmlVision.getDataDir().toString(), "expression datasets").toString();
	static String DIR_RDATAFILES = new File(GmmlVision.getDataDir().toString(), "R data").toString();
}
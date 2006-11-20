package preferences;

import gmmlVision.GmmlVision;
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
		
		setDefault(PREF_FILES_LOG, new File(GmmlVision.getApplicationDir(), ".GmmlVisioLog").toString());
		setDefault(PREF_COL_NO_CRIT_MET, ColorConverter.getRgbString(NO_CRITERIA_MET));
		setDefault(PREF_COL_NO_GENE_FOUND, ColorConverter.getRgbString(NO_GENE_FOUND));
		setDefault(PREF_COL_NO_DATA_FOUND, ColorConverter.getRgbString(NO_DATA_FOUND));
		setDefault(PREF_COL_SELECTED, ColorConverter.getRgbString(SELECTED));
		setDefault(PREF_COL_HIGHLIGHTED, ColorConverter.getRgbString(HIGHLIGHTED));
//		setDefault(PREF_COL_AMBIGIOUS_REP, ColorConverter.getRgbString(AMBIGIOUS_REP));
		setDefault(PREF_CURR_GDB, CURRENT_GDB);
		setDefault(PREF_DB_ENGINE, DB_ENGINE);
		setDefault(PREF_SIDEPANEL_SIZE, SIDEPANEL_SIZE);
		
		setDefault(PREF_DIR_PWFILES, DIR_PWFILES);
		setDefault(PREF_DIR_GDB, DIR_GDBFILES);
		setDefault(PREF_DIR_EXPR, DIR_EXPRFILES);
		setDefault(PREF_DIR_RDATA, DIR_RDATAFILES);
		
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
		if(e.getProperty().equals(GmmlPreferences.PREF_COL_SELECTED)) { 
			if(e.getNewValue() instanceof RGB) GmmlGraphics.selectColor = (RGB)e.getNewValue();
			else GmmlGraphics.selectColor = ColorConverter.parseRgbString((String)e.getNewValue());
			GmmlVision.getDrawing().redraw();
		}
		else if(e.getProperty().equals(GmmlPreferences.PREF_COL_HIGHLIGHTED)) {
			if(e.getNewValue() instanceof RGB) GmmlGraphics.highlightColor = (RGB)e.getNewValue();
			else GmmlGraphics.highlightColor = ColorConverter.parseRgbString((String)e.getNewValue());
			GmmlVision.getDrawing().redraw();
		}
//		else if(e.getProperty().equals(GmmlPreferences.PREF_COL_AMBIGIOUS_REP)) {
//			if(e.getNewValue() instanceof RGB) GmmlGpColor.color_ambigious = (RGB)e.getNewValue();
//			else GmmlGpColor.color_ambigious = ColorConverter.parseRgbString((String)e.getNewValue());
//			GmmlVision.getDrawing().redraw();
//		}
		else if(e.getProperty().startsWith("directories")) {
			createDataDirectories();
		}
	}
	
	// Preference names
	public static final String PREF_FILES_LOG = "files.log";
	
	public static final String PREF_COL_NO_CRIT_MET = "colors.no_criteria_met";
	public static final String PREF_COL_NO_GENE_FOUND = "colors.no_gene_found";
	public static final String PREF_COL_NO_DATA_FOUND = "colors.no_data_found";
	public static final String PREF_COL_SELECTED = "colors.selectColor";
	public static final String PREF_COL_HIGHLIGHTED = "colors.highlightColor";
//	public static final String PREF_COL_AMBIGIOUS_REP = "colors.ambigious_reporter";
		
	public static final String PREF_DIR_PWFILES = "directories.gmmlFiles";
	public static final String PREF_DIR_GDB = "directories.gdbFiles";
	public static final String PREF_DIR_EXPR = "directories.exprFiles";
	public static final String PREF_DIR_RDATA = "directories.RdataFiles";
		
	public static final String PREF_CURR_GDB = "currentGdb";
	public static final String PREF_DB_ENGINE = "database engine";
	public static final String PREF_SIDEPANEL_SIZE = "display.sidePanelSize";	
	
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

	//database engine
	static String DB_ENGINE = "data.DBConnHsqldb";
	
	// directories
	static final String DIR_PWFILES = new File(GmmlVision.getDataDir().toString(), "pathways").toString();
	static final String DIR_GDBFILES = new File(GmmlVision.getDataDir().toString(), "gene databases").toString();
	static final String DIR_EXPRFILES = new File(GmmlVision.getDataDir().toString(), "expression datasets").toString();
	static final String DIR_RDATAFILES = new File(GmmlVision.getDataDir().toString(), "R data").toString();
}
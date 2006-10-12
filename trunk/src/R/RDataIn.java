package R;

import java.io.File;

import R.RCommands.RException;

public class RDataIn {
	String rDataFileName;
		
	public void setRDataFile(String fn) { rDataFileName = fn; }
	public String getRDataFile() { return rDataFileName; }
	
	public void checkValid() throws Exception {
		if(!new File(rDataFileName).canRead()) throw new Exception("Can't read file " + rDataFileName);
	}
	
	public void load() throws Exception {
		checkValid();
		RCommands.eval("load('" + rDataFileName + "')");
	}
	
	public static String[] getPathwaySets() throws RException {
		return RCommands.ls("PathwaySet");
	}
	
	public static String[] getDataSets() throws RException {
		return RCommands.ls("DataSet");
	}
}

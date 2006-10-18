package R;

import java.io.File;

import util.tableviewer.PathwayTable;
import util.tableviewer.TableData;
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
	
	public static class ResultSet extends TableData {
		String name;
		String varName;
		
		public ResultSet(String varName) throws RException {
			this.varName = varName;
			loadFromR();
		}
		
		public String getName() { return name; }
		
		private void loadFromR() throws RException {
			name = RCommands.eval("name(" + varName + ")", true).asString();
			String[] cols = RCommands.eval("colnames(" + varName + ")", true).asStringArray();
			for(String col : cols) {
				addColumn(col, Column.TYPE_TEXT, !col.equalsIgnoreCase(PathwayTable.COLNAME_FILE));
			}
			int nrow = RCommands.eval("nrow(" + varName + ")", true).asInt();
			for(int i = 1; i < nrow + 1; i++) {
				String[] data = RCommands.eval(varName + "[" + i + ",]", true).asStringArray();
				Row row = new Row();
				for(int j = 0; j < data.length; j++) row.setColumn(cols[j], data[j]);
			}
		}
	}
}

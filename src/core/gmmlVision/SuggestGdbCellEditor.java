package gmmlVision;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;

import util.SuggestCellEditor;
import util.SuggestCombo.SuggestionListener;
import util.SuggestCombo.SuggestionProvider;
import data.GmmlDataObject;
import data.GmmlGdb;
import data.MappFormat;
import data.PropertyType;

public class SuggestGdbCellEditor extends SuggestCellEditor implements SuggestionProvider, SuggestionListener {
	HashMap<String, AutoFillData> suggested;
	
	SuggestGdbCellEditor(Composite parent) {
		super(parent);
		suggestCombo.addSuggetsionListener(this);
		suggested = new HashMap<String, AutoFillData>();
	}

	public String[] getSuggestions(String text) {		
		List<String> suggest = new ArrayList<String>();
		if(text.equals("")) return new String[] {};
		try {
			Statement s = GmmlGdb.getCon().createStatement();
			ResultSet r = s.executeQuery(
					"SELECT id, code FROM gene WHERE " +
					"id LIKE '" + text + "%'"
			);
			while(r.next()) {
				String sysCode = r.getString("code");
				String sysName = MappFormat.sysCode2Name.get(sysCode);
				if(sysName == null) sysName = sysCode;
				
				AutoFillData adf = new AutoFillData(PropertyType.NAME, r.getString("id"));
				adf.addValue(PropertyType.GENEPRODUCT_DATA_SOURCE, sysName);
				String label = getLabel(adf);
				suggested.put(label, adf);
				suggest.add(label);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return suggest.toArray(new String[suggest.size()]);
	}

	public String getLabel(AutoFillData adf) {
		return 	adf.getProperty(PropertyType.NAME) + " (" + 
				adf.getProperty(PropertyType.GENEPRODUCT_DATA_SOURCE) + ")";
	}
	
	public SuggestionProvider getSuggestionProvider() {
		return this;
	}

	public void suggestionSelected(String suggestion) {
		suggestCombo.setText(suggestion);
	}
	
	protected Object doGetValue() {
		return suggested.get(suggestCombo.getText());
	}
	
	static class AutoFillData {
		PropertyType mProp;
		Object mValue;
		HashMap<PropertyType, String> values;
		
		public AutoFillData(PropertyType mainProperty, String mainValue) {
			values = new HashMap<PropertyType, String>();
			mProp = mainProperty;
			mValue = mainValue;
			addValue(mainProperty, mainValue);
		}
		
		public void addValue(PropertyType property, String value) {
			values.put(property, value);
		}
		
		public PropertyType getMainProperty() { return mProp; }
		public Object getMainValue() { return mValue; }
		
		public String getProperty(PropertyType property) { return values.get(property); }
		
		public Set<PropertyType> getProperties() { return values.keySet(); }
		
		public void fillData(GmmlDataObject o) {
			for(PropertyType p : getProperties()) {
				Object vOld = o.getProperty(p);
				Object vNew = getProperty(p);
				if(vOld == null || vOld.equals("")) { //Todo, instead of equals("") do equals(default)
					o.setProperty(p, vNew);
				}
			}
		}
	}
}

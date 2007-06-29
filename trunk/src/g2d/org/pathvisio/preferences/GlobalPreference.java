package org.pathvisio.preferences;

import java.awt.Color;

import org.pathvisio.util.ColorConverter;

public enum GlobalPreference implements Preference {
	
	FILE_LOG(""), //TODO
	
	COLOR_NO_CRIT_MET(new Color(200, 200, 200)),
	COLOR_NO_GENE_FOUND(Color.WHITE),
	COLOR_NO_DATA_FOUND(new Color(100, 100, 100)),
	COLOR_SELECTED(Color.RED),
	COLOR_HIGHLIGHTED(Color.GREEN);
	
	GlobalPreference(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	GlobalPreference(Color defaultValue) {
		this.defaultValue = color2String(defaultValue);
	}
	
	private String defaultValue;
	private String value;
	
	public String getDefault() {
		return defaultValue;
	}
	
	public void setDefault(String defValue) {
		defaultValue = defValue;
	}
	
	public void setValue(String newValue) {
		value = newValue;
	}
	
	public String getValue() {
		if(value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}
		
	public static boolean isDefault(Preference p) {
		return p.getValue().equals(p.getDefault());
	}
	public static void setValue(Preference p, Color newValue) {
		p.setValue(color2String(newValue));
	}
	
	public static void setValue(Preference p, int newValue) {
		p.setValue(Integer.toString(newValue));
	}
	
	public static void setValue(Preference p, double newValue) {
		p.setValue(Double.toString(newValue));
	}
	
	public static Color getValueColor(Preference p) {
		return string2Color(p.getValue());
	}
	
	public static int getValueInt(Preference p) {
		return Integer.parseInt(p.getValue());
	}
	
	public static double getValueDouble(Preference p) {
		return Double.parseDouble(p.getValue());
	}
	
	public static boolean getValueBoolean(Preference p) {
		return Boolean.parseBoolean(p.getValue());
	}
	
	private static Color string2Color(String s) {
		return ColorConverter.parseColorString(s);
	}
	
	private static String color2String(Color c) {
		return ColorConverter.getRgbString(c);
	}
}

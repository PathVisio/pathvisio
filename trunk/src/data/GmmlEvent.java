package data;

import graphics.*;

public class GmmlEvent 
{
	public static final int MODIFIED_GENERAL = 0;
	public static final int MODIFIED_SHAPE = 1;
	public static final int DELETED = 2;
	public static final int ADDED = 3;
	public static final int PROPERTY = 4; // e.g. name change
	
	private GmmlGraphicsData affectedData;
	public GmmlGraphicsData getAffectedData () { return affectedData; }
	
	private int type;
	public int getType() { return type; }
	
	public GmmlEvent (GmmlGraphicsData g, int t)
	{
		affectedData = g;
		type = t;
	}
}

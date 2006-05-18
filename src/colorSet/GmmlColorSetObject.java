package colorSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;

public abstract class GmmlColorSetObject {
	public GmmlColorSet parent;
	public String name;
	public ArrayList<Integer> useSamples;
	
	public GmmlColorSetObject(GmmlColorSet parent, String name) 
	{	
		this.parent = parent;
		this.name = name;
	}
	public GmmlColorSetObject(GmmlColorSet parent, String name, String criterion)
	{ 
		this(parent, name);
		parseCriterionString(criterion);
	}
	
	abstract RGB getColor(HashMap<Integer, Object> data);
	
	abstract GmmlColorSet getParent();
	
	abstract void parseCriterionString(String criterion);
	
	public abstract String getCriterionString();
	
}

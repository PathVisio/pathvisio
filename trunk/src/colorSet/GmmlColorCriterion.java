package colorSet;

import gmmlVision.GmmlVision;

import java.util.HashMap;

import org.eclipse.swt.graphics.RGB;

import data.GmmlGex;
import data.GmmlGex.Sample;

public class GmmlColorCriterion extends GmmlColorSetObject {
	Criterion criterion = new Criterion();
	
	public static final RGB INITIAL_COLOR = new RGB(255, 255, 255);
	private RGB color;
	public void setColor(RGB color) { this.color = color; }
	public RGB getColor() { return color == null ? INITIAL_COLOR : color; }
	
	public Criterion getCriterion() { return criterion; }
	
	public GmmlColorCriterion(GmmlColorSet parent, String name) {
		super(parent, name);
		useSample = USE_SAMPLE_ALL;
	}
	
	public GmmlColorCriterion(GmmlColorSet parent, String name, String criterion)
	{
		super(parent, name, criterion);
	}

	RGB getColor(HashMap<Integer, Object> data, int idSample) {
		// Does this expression apply to the given sample?
		if(useSample != USE_SAMPLE_ALL && useSample != idSample) return null; 

		try {
			if(criterion.evaluate(data)) return color;
		} catch (Exception e) { 
			GmmlVision.log.error("Unable to evaluate expression '" + criterion.getExpression() + "'", e);
			//TODO: tell user that expression is incorrect
		}
		return null;
	}

	void parseCriterionString(String criterion) {
		// EXPRESSION | useSample | (r,g,b) | expression
		String[] s = criterion.split("\\|", 4);
		try
		{
			useSample = Integer.parseInt(s[1]);
			color = GmmlColorSet.parseColorString(s[2]);
			this.criterion.setExpression(s[3]);
		}
		catch (Exception e)
		{
			GmmlVision.log.error("Unable to parse color criterion data stored in " +
					"expression database: " + criterion, e);
		}
	}

	public String getCriterionString() {
		// EXPRESSION | useSample | (r,g,b) | expression
		String sep = "|";
		StringBuilder criterion = new StringBuilder("EXPRESSION" + sep);
		criterion.append(useSample + sep);
		criterion.append(GmmlColorSet.getColorString(color) + sep);
		criterion.append(this.criterion.getExpression());

		return criterion.toString();
	}
}

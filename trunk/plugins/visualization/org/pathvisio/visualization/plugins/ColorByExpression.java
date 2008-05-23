package org.pathvisio.visualization.plugins;

import java.awt.Component;
import java.awt.Graphics2D;

import org.pathvisio.view.Graphics;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationMethod;

public class ColorByExpression extends VisualizationMethod {
	
	public ColorByExpression(Visualization v) {
		super(v);
		setIsConfigurable(false); //TODO: make configurable
	}

	public String getDescription() {
		return "Color DataNodes by their expression value";
	}

	public String getName() {
		return "Expression as color";
	}

	public void visualizeOnDrawing(Graphics g, Graphics2D g2d) {
		// TODO Auto-generated method stub
		
	}

	public Component visualizeOnToolTip(Graphics g) {
		// TODO Auto-generated method stub
		return null;
	}
}

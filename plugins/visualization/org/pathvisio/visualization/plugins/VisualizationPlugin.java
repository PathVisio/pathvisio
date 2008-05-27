package org.pathvisio.visualization.plugins;

import org.pathvisio.plugin.Plugin;
import org.pathvisio.visualization.Visualization;
import org.pathvisio.visualization.VisualizationMethod;
import org.pathvisio.visualization.VisualizationMethodProvider;
import org.pathvisio.visualization.VisualizationMethodRegistry;

/**
 * Plugin that registers several visualization methods
 * @author thomas
 *
 */
public class VisualizationPlugin implements Plugin {

	public void init() {
		//Register the visualization methods
		VisualizationMethodRegistry reg = VisualizationMethodRegistry.getCurrent();
		reg.registerMethod(
				ColorByExpression.class.toString(), 
				new VisualizationMethodProvider() {
					public VisualizationMethod create(Visualization v, String registeredName) {
						return new ColorByExpression(v, registeredName);
					}
			}
		);
		reg.registerMethod(
				TextByExpression.class.toString(), 
				new VisualizationMethodProvider() {
					public VisualizationMethod create(Visualization v, String registeredName) {
						return new TextByExpression(v, registeredName);
					}
			}
		);
		reg.registerMethod(
				DataNodeLabel.class.toString(), 
				new VisualizationMethodProvider() {
					public VisualizationMethod create(Visualization v, String registeredName) {
						return new DataNodeLabel(v, registeredName);
					}
			}
		);
	}

}

package org.pathvisio.model;

import java.util.HashMap;
import java.util.Map;


/**
 * Keeps track of connector shapes. This class allows you to register
 * a custom connector shape.
 * @author thomas
 *
 */
public abstract class ConnectorShapeRegistry {
	private static Map<String, ConnectorShape> shapes = new HashMap<String, ConnectorShape>();
	
	static {
		shapes.put(ConnectorType.STRAIGHT.getName(), new StraightConnectorShape());
		shapes.put(ConnectorType.ELBOW.getName(), new ElbowConnectorShape());
		shapes.put(ConnectorType.CURVED.getName(), new CurvedConnectorShape());
	}
	
	public static void registerShape(String name, ConnectorShape shape) {
		if(name == null || shape == null) {
			throw new IllegalArgumentException("null argument provided");
		}
		shapes.put(name, shape);
	}
	
	/**
	 * Get the connector shape by name
	 * @param name The name of the connector shape
	 * @return The connector shape, or the shape for {@link ConnectorType#STRAIGHT} when
	 * a shape by the given name could not be found.
	 */
	public static ConnectorShape getShape(String name) {
		ConnectorShape shape = shapes.get(name);
		if(shape == null) {
			shape = shapes.get(ConnectorType.STRAIGHT.getName());
		}
		return shape;
	}
}

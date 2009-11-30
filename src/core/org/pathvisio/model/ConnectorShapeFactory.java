// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.pathvisio.model;

import java.util.HashMap;
import java.util.Map;

import org.pathvisio.debug.Logger;


/**
 * Constructs connector shapes. This class allows you to register
 * a custom connector shape class. The class to register must have
 * a public constructor without arguments!
 * @author thomas
 *
 */
public abstract class ConnectorShapeFactory {
	private static Map<String, Class<? extends ConnectorShape>> shapes = new HashMap<String, Class<? extends ConnectorShape>>();

	static {
		shapes.put(ConnectorType.STRAIGHT.getName(), StraightConnectorShape.class);
		shapes.put(ConnectorType.ELBOW.getName(), ElbowConnectorShape.class);
		shapes.put(ConnectorType.CURVED.getName(), CurvedConnectorShape.class);
	}

	public static void registerShape(String name, Class<? extends ConnectorShape> shapeClass) {
		if(name == null || shapeClass == null) {
			throw new IllegalArgumentException("null argument provided");
		}
		shapes.put(name, shapeClass);
	}

	/**
	 * Create an instance of a the implementor of ConnectorShape identified by the
	 * given name.
	 * @param name The name of the connector shape
	 * @return The connector shape, or the shape for {@link ConnectorType#STRAIGHT} when
	 * a shape by the given name could not be found.
	 */
	public static ConnectorShape createConnectorShape(String name) {
		Class<? extends ConnectorShape> shapeClass = shapes.get(name);
		ConnectorShape shape = null;
		try {
			shape = shapeClass.getConstructor().newInstance();
		} catch(Exception e) {
			Logger.log.error("Unable to create instance of connectorshape " + shapeClass, e);
			shape = new StraightConnectorShape();
		}
		return shape;
	}

	/**
	 * Get the class that implements ConnectorShape identified by the given name
	 * @return The class, or null if no class is registered by the given name
	 */
	public static Class<? extends ConnectorShape> getImplementingClass(String name) {
		return shapes.get(name);
	}
}

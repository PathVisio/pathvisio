package org.pathvisio.example;

import java.util.EnumSet;

import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.gui.swing.propertypanel.PropertyDisplayManager;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Property;
import org.pathvisio.model.PropertyManager;
import org.pathvisio.model.PropertyType;
import org.pathvisio.model.StaticPropertyType;
import org.pathvisio.plugin.Plugin;

/**
 * Example that shows how to make a dynamic property available in the
 * property panel, so users can edit it.
 * 
 * @author thomas
 */
public class ExDynamicPropertyPanel implements Plugin {
	public void init(PvDesktop desktop) {
		//Register a general dynamic property, that applies to all object types
		PropertyManager.registerProperty(MY_GENERAL_PROPERTY);
		PropertyDisplayManager.registerProperty(MY_GENERAL_PROPERTY);
		
		//Register a specific dynamic property, that only applies to
		//DataNode and Label
		PropertyManager.registerProperty(MY_SPECIFIC_PROPERTY);
		PropertyDisplayManager.registerProperty(MY_SPECIFIC_PROPERTY);
		PropertyDisplayManager.setPropertyScope(
				MY_SPECIFIC_PROPERTY, 
				EnumSet.of(ObjectType.DATANODE, ObjectType.LABEL)
		);
	}
	
	public void done() {}
	
	static final Property MY_GENERAL_PROPERTY = new Property () {
		public String getId() {
			return "org.pathvisio.example.MyProperty";
		}
		
		public String getDescription() {
			return "This is an example property";
		}
		
		public String getName() {
			return "Example property";
		}
		
		public PropertyType getType() {
			return StaticPropertyType.STRING;
		}
		
		public boolean isCollection() {
			return false;
		}
	};
	
	static final Property MY_SPECIFIC_PROPERTY = new Property () {
		public String getId() {
			return "org.pathvisio.example.MySpecificProperty";
		}
		
		public String getDescription() {
			return "This is an example property that only applies to DataNodes and Labels";
		}
		
		public String getName() {
			return "Example DataNode/Label property";
		}
		
		public PropertyType getType() {
			return StaticPropertyType.STRING;
		}
		
		public boolean isCollection() {
			return false;
		}
	};
}

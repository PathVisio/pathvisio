package org.pathvisio.debug;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Indicates that this class or method should only be accessed by the 
 * Event Dispatch Thread
 * <p>
 * Add this annotation to methods that call (swing) GUI methods
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR})
public @interface EventDispatchThreadOnly {

}

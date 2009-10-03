package org.pathvisio.debug;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Indicates that this class or method should only be accessed by threads
 * other than the Event Dispatch Thread
 * <p>
 * Add this annotation to methods that perform potentially blocking operations,
 * such as disk, network or database access. 
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR})
public @interface WorkerThreadOnly {

}

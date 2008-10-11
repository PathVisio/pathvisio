package org.pathvisio.biopax;

import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.pathvisio.model.PathwayElement;

/**
 * Implement this interface to provide custom graphical style mappings
 * for BioPAX to GPML elements.
 * @author thomas
 */
public interface StyleMapper {
	/**
	 * Set the visual style for the given control element.
	 * @param c The BioPAX control element
	 * @param line The GPML line that maps to the BioPAX element
	 */
	public void mapControl(control c, PathwayElement line);
	
	/**
	 * Set the visual style for the given conversion element.
	 * @param c The BioPAX conversion element
	 * @param line The GPML line that maps to the BioPAX element
	 */
	public void mapConversion(conversion c, PathwayElement line);
	
	/**
	 * Set the visual style for the given conversion element. This method will
	 * be called for all conversions that have multiple input (left) participants.
	 * @param c The BioPAX conversion element
	 * @param line The GPML line that will connect the input (left) participant
	 * with the main conversion line (see {@link #mapConversion(conversion, PathwayElement)}.
	 */
	public void mapConversionLeft(conversion c, PathwayElement line);
	
	/**
	 * Set the visual style for the given conversion element. This method will
	 * be called for all conversions that have multiple output (right) participants.
	 * @param c The BioPAX conversion element
	 * @param line The GPML line that will connect the output (right) participant
	 * with the main conversion line (see {@link #mapConversion(conversion, PathwayElement)}.
	 */
	public void mapConversionRight(conversion c, PathwayElement line);
	
	/**
	 * Set the visual style for the given physicalEntity element.
	 * @param c The BioPAX conversion element
	 * @param line The GPML datanode that maps to the BioPAX element
	 */
	public void mapPhysicalEntity(physicalEntity e, PathwayElement datanode);
	
	/**
	 * Create a GPML element that represents an unknown or unspecified participant.
	 * This is used for conversions or controls where a participant is not
	 * specified (e.g. a catalysis with unknown enzyme).
	 * @param c The BioPAX conversion element
	 * @param line The GPML line that maps to the BioPAX element
	 */
	public PathwayElement createUnknownParticipant();
}

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
package org.pathvisio.reactome;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.MLine;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayElement.MAnchor;
import org.pathvisio.model.PathwayElement.MPoint;

/**
 * Converts a reactome pathway to GPML
 * @author thomas
 */
public class ReactomeFormat {
	static final int BORDER_OFFSET = 50 * Pathway.OLD_GMMLZOOM;
	static final double PADDING = 5 * Pathway.OLD_GMMLZOOM;
	static final double SCALING = 10;

	Query query;

	Pathway pathway;

	int minX = Integer.MAX_VALUE;
	int minY = Integer.MAX_VALUE;

	public ReactomeFormat(Query query) throws SQLException {
		this.query = query;
	}

	public Pathway convert(int pathwayId) throws SQLException {
		Logger.log.info("Converting " + pathwayId);

		pathway = new Pathway();

		String name = query.getEventName(pathwayId);
		if (name != null) {
			if (name.length() > 50) {
				name = name.substring(0, 49);
			}
			pathway.getMappInfo().setMapInfoName(name);
		}

		pathway.getMappInfo().addComment(pathwayId + "", "Reactome DB_ID");

		List<Event> events = query.getPathwayEvents(pathwayId);
		for(Event e : events) {
			Logger.log.info("Processing event " + e.getName() + " (" + e + ")");
			convertEvent(e);
		}
		shiftCoordinates();
		return pathway;
	}

	private void shiftCoordinates() {
		if(minX != Integer.MAX_VALUE && minY != Integer.MAX_VALUE) {
			Logger.log.info("Correcting coordinates, shifting by " + -minX + ", " + -minY);

			double moveX = coordinate(minX) - BORDER_OFFSET;
			double moveY = coordinate(minY) - BORDER_OFFSET;

			for(PathwayElement pe : pathway.getDataObjects()) {
				int ot = pe.getObjectType();
				if(		ot == ObjectType.DATANODE ||
						ot == ObjectType.SHAPE ||
						ot == ObjectType.LABEL
				) {
					pe.setMCenterX(pe.getMCenterX() - moveX);
					pe.setMCenterY(pe.getMCenterY() - moveY);
				}
				if(ot == ObjectType.LINE) {
					for(MPoint mp : pe.getMPoints()) {
						if(mp.getGraphId() == null || "".equals(mp.getGraphId())) {
							mp.setX(mp.getX() - moveX);
							mp.setY(mp.getY() - moveY);
						}
					}
				}
			}
		}
	}

	private void convertEvent(Event event) throws SQLException {
		if(event instanceof ReactionlikeEvent) {
			Logger.log.info("ReactionLikeEvent found");
			convertReactionlikeEvent((ReactionlikeEvent)event);
		}
	}

	private void convertReactionlikeEvent(ReactionlikeEvent event) {
		minX = Math.min(event.getInputX(), minX);
		minY = Math.min(event.getInputY(), minY);
		minX = Math.min(event.getOutputX(), minX);
		minY = Math.min(event.getOutputY(), minY);

		List<PathwayElement> input = new ArrayList<PathwayElement>();
		double y = -1;
		for(PhysicalEntity phe : event.getInput()) {
			PathwayElement pe = convertPhysicalEntity(phe);
			input.add(pe);
			pe.setMCenterX(coordinate(event.getInputX()));
			if(y == -1) {
				y = coordinate(event.getInputY());
			} else {
				y += pe.getMHeight() + PADDING;
			}
			pe.setMCenterY(y);
		}
		List<PathwayElement> output = new ArrayList<PathwayElement>();
		y = -1;
		for(PhysicalEntity phe : event.getOutput()) {
			PathwayElement pe = convertPhysicalEntity(phe);
			output.add(pe);
			pe.setMCenterX(coordinate(event.getOutputX()));
			if(y == -1) {
				y = coordinate(event.getOutputY());
			} else {
				y += pe.getMHeight() + PADDING;
			}
			pe.setMCenterY(y);
		}

		MLine line = (MLine)PathwayElement.createPathwayElement(ObjectType.LINE);
		pathway.add(line);

		if(input.size() > 0) {
			line.getMStart().linkTo(input.get(0), 0, 0);
		} else {
			line.setMStartX(coordinate(event.getInputX()));
			line.setMStartY(coordinate(event.getInputY()));
		}
		if(output.size() > 0) {
			line.getMEnd().linkTo(output.get(0), 0, 0);
		} else {
			line.setMEndX(coordinate(event.getOutputX()));
			line.setMEndY(coordinate(event.getOutputY()));
		}

		if(input.size() > 1) {
			MAnchor anchorIn = line.addMAnchor(0.2);
			for(int i = 1; i < input.size(); i++) {
				MLine l = (MLine)PathwayElement.createPathwayElement(ObjectType.LINE);
				pathway.add(l);
				l.getMStart().linkTo(input.get(i), 0, 0);
				l.getMEnd().linkTo(anchorIn, 0, 0);
			}
		}
		if(output.size() > 1) {
			MAnchor anchorOut = line.addMAnchor(0.8);
			for(int i = 1; i < output.size(); i++) {
				MLine l = (MLine)PathwayElement.createPathwayElement(ObjectType.LINE);
				pathway.add(l);
				l.getMStart().linkTo(output.get(i), 0, 0);
				l.getMEnd().linkTo(anchorOut, 0, 0);
			}
		}
	}

	private double coordinate(double c) {
		return c * Pathway.OLD_GMMLZOOM * SCALING;
	}

	Map<Integer, PathwayElement> entity2pwe = new HashMap<Integer, PathwayElement>();

	private PathwayElement convertPhysicalEntity(PhysicalEntity phe) {
		PathwayElement pe = entity2pwe.get(phe.getId());

		if(pe == null) {
			pe = PathwayElement.createPathwayElement(ObjectType.DATANODE);
			pathway.add(pe);
			entity2pwe.put(phe.getId(), pe);
			pe.setInitialSize();

			String textLabel = phe.getName(); //TODO: name
			int count = phe.getCount();
			if(count > 1) {
				textLabel = count + " " + textLabel;
			}
			pe.setTextLabel(textLabel);
		}
		return pe;
	}
}

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.pathvisio.debug.Logger;


public class Query {
	public static final String FIELD_hasEvent = "hasEvent";
	public static final String FIELD_DB_ID = "DB_ID";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_hasEvent_class = "hasEvent_class";


	static final String CLASS_BlackBoxEvent = "BlackBoxEvent";
	static final String CLASS_Depolymerisation = "Deposymerisation";
	static final String CLASS_Polymerisation = "Polymerisation";
	static final String CLASS_Reaction = "Reaction";

	Connection con;

	private PreparedStatement pstPathwayEvents;
	private PreparedStatement pstEventName;
	private PreparedStatement pstReactionCoordinates;
	private PreparedStatement pstReactionInput;
	private PreparedStatement pstReactionOutput;
	private PreparedStatement pstEntityName;
	private PreparedStatement pstEvent;

	public Query(Connection con) throws SQLException {
		this.con = con;
		prepareStatements();
	}

	private void prepareStatements() throws SQLException {
		pstPathwayEvents = con.prepareStatement(
				"SELECT hasEvent, hasEvent_class FROM Pathway_2_hasEvent WHERE DB_ID = ? " +
				"ORDER BY hasEvent_rank"
		);

		pstEvent = con.prepareStatement(
			"SELECT DB_ID FROM Event WHERE DB_ID = ?"
		);
		pstEventName = con.prepareStatement(
			"SELECT name FROM Event_2_name WHERE DB_ID = ?"
		);

		pstReactionCoordinates = con.prepareStatement(
			"SELECT sourceX, sourceY, targetX, targetY FROM ReactionCoordinates " +
			"WHERE locatedEvent = ?"
		);
		pstReactionInput = con.prepareStatement(
			"SELECT input FROM ReactionlikeEvent_2_input WHERE DB_ID = ? " +
			"ORDER BY input_rank"
		);
		pstReactionOutput = con.prepareStatement(
			"SELECT output FROM ReactionlikeEvent_2_output WHERE DB_ID = ? " +
			"ORDER BY output_rank"
		);
		pstEntityName = con.prepareStatement(
			"SELECT name FROM PhysicalEntity_2_name WHERE DB_ID = ?"
		);
	}

	public List<Event> getPathwayEvents(int pathwayId) throws SQLException {
		pstPathwayEvents.setInt(1, pathwayId);
		ResultSet r = pstPathwayEvents.executeQuery();
		List<Event> events = new ArrayList<Event>();
		while(r.next()) {
			int eventId = r.getInt(1);
			String eventClass = r.getString(2);
			events.add(createEvent(eventId, eventClass));
		}
		return events;
	}

	private Event createEvent(int eventId, String eventClass) throws SQLException {
		Event e = null;
		if( //Process ReactionLikeEvents
				CLASS_BlackBoxEvent.equals(eventClass) ||
				CLASS_Depolymerisation.equals(eventClass) ||
				CLASS_Polymerisation.equals(eventClass) ||
				CLASS_Reaction.equals(eventClass))
		{
			e = createReactionlikeEvent(eventId);
		} else
		{
			e = new Event(eventId);
		}
		e.setName(getEventName(eventId));
		return e;
	}

	public ReactionlikeEvent createReactionlikeEvent(int id) throws SQLException {
		//Check if event exists
		pstEvent.setInt(1, id);
		ResultSet r = pstEvent.executeQuery();
		boolean exists = false;
		while(r.next()) {
			exists = true;
			break;
		}
		if(!exists) throw new IllegalArgumentException(
				"Event with DB_ID " + id + " doesn't exist"
		);

		ReactionlikeEvent e = new ReactionlikeEvent(id);

		pstReactionInput.setInt(1, id);
		r = pstReactionInput.executeQuery();
		e.setInput(parseReactionParticipants(r));
		pstReactionOutput.setInt(1, id);
		r = pstReactionOutput.executeQuery();
		e.setOutput(parseReactionParticipants(r));

		int[] coor = getReactionCoordinates(id);
		if(coor != null) {
			e.setInputX(coor[0]);
			e.setInputY(coor[1]);
			e.setOutputX(coor[2]);
			e.setOutputY(coor[3]);
		}
		return e;
	}

	private List<PhysicalEntity> parseReactionParticipants(ResultSet r) throws SQLException {
		List<PhysicalEntity> entities = new ArrayList<PhysicalEntity>();
		PhysicalEntity last = null;
		while(r.next()) {
			int inputId = r.getInt(1);
			if(last == null || last.getId() != inputId) {
				last = createPhysicalEntity(inputId);
				entities.add(last);
			}
			last.setCount(last.getCount());
		}
		return entities;
	}

	private PhysicalEntity createPhysicalEntity(int entityId) throws SQLException {
		Logger.log.info("Creating physical entity: " + entityId);
		PhysicalEntity phe = new PhysicalEntity(entityId);
		phe.setName(getPhysicalEntityName(entityId));
		return phe;
	}

	private String getPhysicalEntityName(int entityId) throws SQLException {
		pstEntityName.setInt(1, entityId);
		ResultSet r = pstEntityName.executeQuery();
		while(r.next()) {
			return r.getString(1);
		}
		return null;
	}

	private int[] getReactionCoordinates(int eventId) throws SQLException {
		pstReactionCoordinates.setInt(1, eventId);
		ResultSet r = pstReactionCoordinates.executeQuery();
		while(r.next()) {
			int[] coor = new int[4];
			coor[0] = r.getInt(1);
			coor[1] = r.getInt(2);
			coor[2] = r.getInt(3);
			coor[3] = r.getInt(4);
			return coor;
		}
		return null;
	}

	public String getEventName(int eventId) throws SQLException {
		pstEventName.setInt(1, eventId);
		ResultSet r = pstEventName.executeQuery();
		while(r.next()) {
			return r.getString(1);
		}
		return null;
	}
}

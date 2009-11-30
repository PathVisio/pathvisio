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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.xref;
import org.jdom.JDOMException;
import org.pathvisio.Engine;
import org.pathvisio.biopax.BiopaxFormat;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayElement.MPoint;

/**
 * Converts Reactome pathways based on the BioPAX converter.
 * This converter queries the Reactome database for coordinates
 * of the pathway entities.
 * @author thomas
 */
public class ReactomeBiopaxFormat extends BiopaxFormat {
	static final int BORDER_OFFSET = 50 * Pathway.OLD_GMMLZOOM;
	static final double PADDING = 5 * Pathway.OLD_GMMLZOOM;
	static final double SCALING = 10;

	int minX = Integer.MAX_VALUE;
	int minY = Integer.MAX_VALUE;

	static final String PREFIX_DB_ID = "Reactome";
	Query query;

	public static void main(String[] args) {
		Engine.init();
		Logger.log.setLogLevel(true, true, true, true, true, true);
		if(args.length < 2) {
			printHelp();
		}
		String db = args[0];
		String name = args[1];
		String pass = args[2];

		try {
			Connection con = connect(db, name, pass);
			Query query = new Query(con);

			ReactomeBiopaxFormat rf = new ReactomeBiopaxFormat(query, new File(args[3]));

			List<Pathway> pathways = rf.convert();
			int i = 0;
			for(Pathway p : pathways) {
				File f = p.getSourceFile();
				if(f == null) {
					f = new File("untitled-" + i++ + ".gpml");
				}
				p.writeToXml(f, true);
			}
		} catch(Exception e) {
			printHelp();
			e.printStackTrace();
		}
	}

	static void printHelp() {
		System.out.println(
				"Usage:\n" +
				"mysql_database mysql_login mysql_pass owl_file\n" +
				"- mysql_database_url: the host/name database containing reactome" +
				" (e.g. 'localhost/reactome'" +
				"- mysql_login: the login name to the mysql database" +
				"- mysql_pass: the password to the mysql database" +
				"- owl_file: the biopax file to convert\n"
		);
	}

	static Connection connect(String db, String user, String pass) throws ClassNotFoundException, SQLException {
        String url = "jdbc:mysql://" + db;
        Class.forName ("com.mysql.jdbc.Driver");
        return DriverManager.getConnection (url, user, pass);
	}

	public ReactomeBiopaxFormat(Query query, File biopaxFile) throws JDOMException, IOException {
		super(biopaxFile);
		this.query = query;
	}

	protected void newPathway() {
		super.newPathway();
		minX = Integer.MAX_VALUE;
		minY = Integer.MAX_VALUE;
	}

	protected void layoutPathway(Pathway gpmlPathway, pathway bpPathway) {
		super.layoutPathway(gpmlPathway, bpPathway);
		shiftCoordinates(gpmlPathway);
	}

	protected void mapConversion(Pathway gpmlPathway, conversion c) {
		super.mapConversion(gpmlPathway, c);

		int reactome_id = -1;
		for(xref x : c.getXREF()) {
			if(PREFIX_DB_ID.equals(x.getDB())) {
				try {
					reactome_id = Integer.parseInt(x.getID());
					break;
				} catch(NumberFormatException e) {
					Logger.log.error("Unable to parse reactom DB_ID for " + c);
				}
			}
		}
		if(reactome_id != -1) {
			try {
				ReactionlikeEvent evt = query.createReactionlikeEvent(reactome_id);

				minX = Math.min(evt.getInputX(), minX);
				minY = Math.min(evt.getInputY(), minY);
				minX = Math.min(evt.getOutputX(), minX);
				minY = Math.min(evt.getOutputY(), minY);

				setCoordinates(c.getLEFT(), evt.getInputX(), evt.getInputY());
				setCoordinates(c.getRIGHT(), evt.getOutputX(), evt.getOutputY());

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Logger.log.info("No reactome DB_ID found for " + c);
		}

	}

	private void setCoordinates(Set<physicalEntityParticipant> participants, double x, double y) {
		int i = 1;
		y = coordinate(y);
		for(physicalEntityParticipant p : participants) {
			PathwayElement pwe = getConverted(p);
			if(pwe != null) {
				pwe.setMCenterX(coordinate(x));
				pwe.setMCenterY(y);
				y += i * PADDING + pwe.getMHeight();
			} else {
				Logger.log.warn("setCoordinates: converted entity not found for " + p);
			}
		}
	}

	private void shiftCoordinates(Pathway pathway) {
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

	private double coordinate(double c) {
		return c * Pathway.OLD_GMMLZOOM * SCALING;
	}
}

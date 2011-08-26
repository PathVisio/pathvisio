//PathVisio,
//a tool for data visualization and analysis using Biological Pathways
//Copyright 2006-2007 BiGCaT Bioinformatics

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.pathvisio.kegg;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.bridgedb.bio.Organism;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.GraphLink.GraphIdContainer;
import org.pathvisio.core.view.LinAlg;
import org.pathvisio.core.view.LinAlg.Point;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class Util {
	private static Map<String, String> species2code = new HashMap<String, String>();
	
	static {
		species2code.put(Organism.HomoSapiens.latinName(), "hsa");
		species2code.put(Organism.RattusNorvegicus.latinName(), "rno");
		species2code.put(Organism.MusMusculus.latinName(), "mmu");
		species2code.put(Organism.SaccharomycesCerevisiae.latinName(), "sce");
		species2code.put(Organism.ArabidopsisThaliana.latinName(), "ath");
		species2code.put(Organism.BosTaurus.latinName(), "bta");
		species2code.put(Organism.CaenorhabditisElegans.latinName(), "hsa");
		species2code.put(Organism.CanisFamiliaris.latinName(), "cfa");
		species2code.put(Organism.DanioRerio.latinName(), "dre");
		species2code.put(Organism.DrosophilaMelanogaster.latinName(), "dme");
		species2code.put(Organism.EscherichiaColi.latinName(), "eco");
		species2code.put(Organism.GallusGallus.latinName(), "gga");
		species2code.put(Organism.OryzaSativa.latinName(), "osa");
		species2code.put(Organism.TriticumAestivum.latinName(), "etae");
		species2code.put(Organism.XenopusTropicalis.latinName(), "xtr");
		species2code.put(Organism.ZeaMays.latinName(), "ezma");
		species2code.put(Organism.MycobacteriumTuberculosis.latinName(), "mtu");
		species2code.put(Organism.AnophelesGambiae.latinName(), "aga");
		species2code.put("Aspergillus niger", "ang");
	}

	public static String getKeggOrganism(String organism) throws ConverterException {
		String code = species2code.get(organism);
		if(code == null) throw new ConverterException("No KEGG code for organism " + organism);
		return code;
	}
	
	static String getGraphId(GraphIdContainer gc) {
		//TK: Quick hack, GraphId is not automatically generated,
		//so set one explicitly...
		String id = gc.getGraphId();
		if(id == null) {
			gc.setGraphId(id = gc.getPathway().getUniqueGraphId());
		}
		return id;
	}

	static void stackElements(Collection<PathwayElement> pwElms) {
		PathwayElement[] elements = pwElms.toArray(new PathwayElement[pwElms.size()]);
		PathwayElement center = elements[0];
		double currAbove = center.getMTop();
		double currBelow = center.getMTop() + center.getMHeight();
		for(int i = 1; i < pwElms.size(); i++) {
			PathwayElement e = elements[i];

			if(i % 2 == 0) { //Place below
				e.setMTop(currBelow);
				currBelow += e.getMHeight();
			} else { //Place above
				currAbove -= e.getMHeight();
				e.setMTop(currAbove);
			}
		}
	}

	static Point[] findBorders(GraphIdContainer start, GraphIdContainer end) {
		Point2D csource = start.toAbsoluteCoordinate(new Point2D.Double(0, 0));
		Point2D ctarget = end.toAbsoluteCoordinate(new Point2D.Double(0, 0));

		Point psource = new Point(csource.getX(), csource.getY());
		Point ptarget = new Point(ctarget.getX(), ctarget.getY());

		double angle = LinAlg.angle(ptarget.subtract(psource), new Point(1, 0));
		double astart = angle;
		double aend = angle;
		if(angle < 0) 	aend += Math.PI;
		else 			aend -= Math.PI;
		if(angle == 0) {
			if(psource.x > ptarget.x) {
				aend += Math.PI;
				astart += Math.PI;
			}
		}
		Point pstart = findBorder(start, astart);
		Point pend = findBorder(end, aend);
		return new Point[] { pstart, pend };
	}

	/**
	 * Find the border to connect to. Returns a point containing
	 * the relative coordinates.
	 */
	static Point findBorder(GraphIdContainer gc, double angle) {
		Point bp = new Point(-1, -1);

		Point2D topleft = gc.toAbsoluteCoordinate(new Point2D.Double(-1, -1));
		Point2D btmright = gc.toAbsoluteCoordinate(new Point2D.Double(1, 1));
		double h = btmright.getY() - topleft.getY();
		double w = btmright.getX() - topleft.getX();

		double diagAngle = Math.atan(h / w);
		double angleA = Math.abs(angle);
		/*    da < |a| < da + pi/2
		       \   /
		        \ /
|a| > da + pi/2	 \  |a| < da
		        / \
		       /   \
		         da < |a| < da + pi/2
		 */

		if(angleA >= diagAngle && angleA <= diagAngle + Math.PI/2) {
			bp.x = 0; //center
			if(angle < 0) {
				bp.y += 2;
			}
		}
		if(angleA < diagAngle || angleA > diagAngle + Math.PI/2) {
			bp.y = 0;
			if(angle < Math.PI / 2 && angle > -Math.PI / 2) {
				bp.x += 2;
			}
		}

		return bp;
	}

	//From http://iq80.com/2007/10/disable-dtd-and-xsd-downloading.html
	public static Object unmarshal(Class<?> type, InputStream in) throws ParserConfigurationException, SAXException, JAXBException {

		// create a parser with validation disabled
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		SAXParser parser = factory.newSAXParser();

		// Get the JAXB context -- this should be cached
		JAXBContext ctx = JAXBContext.newInstance(type);

		// get the unmarshaller
		Unmarshaller unmarshaller = ctx.createUnmarshaller();

		// log errors?
		unmarshaller.setEventHandler(new ValidationEventHandler(){
			public boolean handleEvent(ValidationEvent validationEvent) {
				Logger.log.warn(validationEvent.getMessage());
				return false;
			}
		});

		// add our XMLFilter which disables dtd downloading
		NamespaceFilter xmlFilter = new NamespaceFilter(parser.getXMLReader());
		xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

		// Wrap the input stream with our filter
		SAXSource source = new SAXSource(xmlFilter, new InputSource(in));

		// unmarshal the document
		return unmarshaller.unmarshal(source);
	}

	private static class NamespaceFilter extends XMLFilterImpl {
		private static final InputSource EMPTY_INPUT_SOURCE =
			new InputSource(new ByteArrayInputStream(new byte[0]));

		public NamespaceFilter(XMLReader xmlReader) {
			super(xmlReader);
		}

		public InputSource resolveEntity(String publicId, String systemId) {
			return EMPTY_INPUT_SOURCE;
		}
	}
}

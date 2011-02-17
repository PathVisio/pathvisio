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

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import org.bridgedb.DataSource;
import org.bridgedb.bio.BioDataSource;
import org.pathvisio.biopax.reflect.BiopaxElement;
import org.pathvisio.debug.StopWatch;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.view.MIMShapes;

public class TestParser extends TestCase 
{
	StopWatch sw;
	
	public void setUp()
	{
		PreferenceManager.init();
		MIMShapes.registerShapes(); //TODO: should not be necessary to call this first
	}
	
	/** centralized helper makes it convenient to toggle parser and validation */
	private Pathway readHelper(File f) throws FileNotFoundException, ConverterException
	{
		assert (f.exists());
		Pathway data = new Pathway();
		data.readFromXml(f, true);
		return data;
	}
	
	public void testParserSpeed() throws ConverterException, FileNotFoundException
	{
		File test = new File("testData/WP248_2010a.gpml");
		
		sw = new StopWatch();
		sw.start();
		Pathway data = readHelper (test);

		long result = sw.stop();
		System.out.println ("Timing: " + result + " msec");
		
		PathwayElement mi = data.getMappInfo();
		assertEquals ("Apoptosis Mechanisms", mi.getMapInfoName());
		assertEquals ("Alexander C. Zambon", mi.getMaintainer());
		
//		System.out.println (data.getDataObjects().size());
//		for (PathwayElement elt : data.getDataObjects())
//		{
//			System.out.println (elt.getGraphId());
//			System.out.println ("    " + elt.getObjectType());
//			for (PropertyType p : elt.getStaticPropertyKeys())
//			{
//				System.out.println (p + "=" + elt.getStaticProperty(p));
//			}
//		}
	 
	 
		assertEquals (206, data.getDataObjects().size());
		PathwayElement elt = data.getElementById("c3d");
		assertEquals (elt.getObjectType(), ObjectType.DATANODE);
		assertEquals (Color.BLACK, elt.getColor());
		assertEquals (9230.0, elt.getMCenterX(), 0.1);
		assertEquals (5010.0, elt.getMCenterY(), 0.1);
		assertEquals (900.0, elt.getMWidth(), 0.1);
		assertEquals (300.0, elt.getMHeight(), 0.1);
		assertEquals ("8739", elt.getGeneID());
		assertEquals (BioDataSource.ENTREZ_GENE, elt.getDataSource());
		
		//TODO: Line doesn't have fixed graphId, can't test
		//TODO: generate line graphId based on hash of coordinates if not available
		elt = data.getElementById("d49");
		assertEquals (ObjectType.LABEL, elt.getObjectType());
		assertEquals ("Interferon regulatory factors", elt.getTextLabel());
		assertEquals (28672, elt.getZOrder());

		elt = data.getElementById("c45");
		assertEquals (ObjectType.SHAPE, elt.getObjectType());
		assertEquals (ShapeType.BRACE, elt.getShapeType());
		assertEquals (2890.0, elt.getMCenterX(), 0.1);
		assertEquals (2430.0, elt.getMCenterY(), 0.1);
		assertEquals (4253.1, elt.getMWidth(), 0.1);
		assertEquals (130.0, elt.getMHeight(), 0.1);
		assertEquals (0.0, elt.getRotation(), 0.1);

	}

	public void testParser1() throws ConverterException, FileNotFoundException
	{
		File test = new File ("testData/2010a/parsetest1.gpml");
		Pathway data = readHelper (test);
		
		assertEquals (ObjectType.INFOBOX, data.getInfoBox().getObjectType());
		assertEquals (ObjectType.MAPPINFO, data.getMappInfo().getObjectType());
		assertEquals (12.0, data.getInfoBox().getMCenterX(), 0.01);
		assertEquals (7.0, data.getInfoBox().getMCenterY(), 0.01);
		assertEquals ("Test 1", data.getMappInfo().getMapInfoName());
		assertNull (data.getMappInfo().getOrganism());
		assertEquals ("20100331", data.getMappInfo().getVersion());
		assertNull (data.getMappInfo().getMapInfoDataSource());
		assertNull (data.getMappInfo().getAuthor());
		assertNull (data.getMappInfo().getMaintainer());
		assertNull (data.getMappInfo().getEmail());
		assertNull (data.getMappInfo().getCopyright());
		assertNull (data.getMappInfo().getLastModified());		
		assertEquals (0, data.getMappInfo().getComments().size());
		assertEquals (0, data.getMappInfo().getBiopaxRefs().size());
	}

	public void testParser2() throws ConverterException, FileNotFoundException
	{
		File test = new File ("testData/2010a/parsetest2.gpml");
		Pathway data = readHelper (test);

		assertEquals (19.0, data.getInfoBox().getMCenterX(), 0.01);
		assertEquals (31.0, data.getInfoBox().getMCenterY(), 0.01);
		assertEquals ("Test 2", data.getMappInfo().getMapInfoName());
		assertEquals ("Caenorhabditis elegans", data.getMappInfo().getOrganism());
		assertEquals ("20100401", data.getMappInfo().getVersion());
		assertEquals ("Manual", data.getMappInfo().getMapInfoDataSource());
		assertEquals ("Martijn van Iersel", data.getMappInfo().getAuthor());
		assertEquals ("Thomas Kelder", data.getMappInfo().getMaintainer());
		assertEquals ("a@b.com", data.getMappInfo().getEmail());
		assertEquals ("CC-BY", data.getMappInfo().getCopyright());
		assertEquals ("20100618", data.getMappInfo().getLastModified());
		assertEquals (0, data.getMappInfo().getComments().size());
		assertEquals (0, data.getMappInfo().getBiopaxRefs().size());
	}

	public void testParser3() throws ConverterException, FileNotFoundException
	{
		File test = new File ("testData/2010a/parsetest3.gpml");
		Pathway data = readHelper (test);
		
		PathwayElement elt;
		
		elt = data.getMappInfo();
		assertEquals (2, elt.getComments().size());
		assertEquals ("a", elt.getComments().get(0).getComment());
		assertEquals ("test", elt.getComments().get(0).getSource());
		assertEquals ("Type your comment here", elt.getComments().get(1).getComment());
		assertNull (elt.getComments().get(1).getSource());
		
		elt = data.getElementById("a8a81");
		assertEquals (3, elt.getComments().size());
		assertEquals ("c", elt.getComments().get(0).getComment());
		assertNull (elt.getComments().get(0).getSource());
		assertEquals ("d", elt.getComments().get(1).getComment());
		assertNull (elt.getComments().get(1).getSource());
		assertEquals ("Type your comment here", elt.getComments().get(2).getComment());
		assertEquals ("unknown", elt.getComments().get(2).getSource());
		
		elt = data.getElementById("b7b72");
		assertEquals (1, elt.getComments().size());
		assertEquals ("This is a line", elt.getComments().get(0).getComment());
		assertEquals ("manual", elt.getComments().get(0).getSource());
	}

	public void testParser4() throws ConverterException, FileNotFoundException
	{
		PreferenceManager.init();
		File test = new File ("testData/2010a/parsetest4.gpml");
		Pathway data = readHelper (test);

		PathwayElement elt;
		elt = data.getElementById("id37eeec26");
		assertEquals (ObjectType.LINE, elt.getObjectType());
		assertEquals (12288, elt.getZOrder());
		assertEquals (LineStyle.DASHED, elt.getLineStyle());
		assertEquals (1.0, elt.getLineThickness(), 0.01);
		assertEquals (ConnectorType.STRAIGHT, elt.getConnectorType());
		assertEquals (229.0, elt.getMPoints().get(0).getX(), 0.01);
		assertEquals (173.0, elt.getMPoints().get(0).getY(), 0.01);
		assertEquals (304.0, elt.getMPoints().get(1).getX(), 0.01);
		assertEquals (183.0, elt.getMPoints().get(1).getY(), 0.01);
		assertEquals (LineType.LINE, elt.getStartLineType());
		assertEquals (LineType.LINE, elt.getEndLineType());
		assertEquals (0, elt.getMAnchors().size()); 
				
		elt = data.getElementById("ida7a6255a");
		assertEquals (LineType.LINE, elt.getStartLineType());
		assertEquals (LineType.ARROW, elt.getEndLineType());
		assertEquals (2, elt.getMAnchors().size()); 
		assertEquals (0.4, elt.getMAnchors().get(0).getPosition(), 0.01);
		assertEquals (AnchorType.NONE, elt.getMAnchors().get(0).getShape());
		assertEquals (0.6, elt.getMAnchors().get(1).getPosition(), 0.01);
		assertEquals (AnchorType.CIRCLE, elt.getMAnchors().get(1).getShape());

		elt = data.getElementById("idb5761669");
		assertEquals (ConnectorType.ELBOW, elt.getConnectorType());
		
		elt = data.getElementById("a3686");
		assertEquals (LineType.TBAR, elt.getStartLineType());
		assertEquals (LineType.RECEPTOR, elt.getEndLineType());

		elt = data.getElementById("d6034");
		assertEquals (LineType.LIGAND_SQUARE, elt.getStartLineType());
		assertEquals (LineType.RECEPTOR_SQUARE, elt.getEndLineType());
		
		elt = data.getElementById("c4eb9");
		assertEquals (LineType.LIGAND_ROUND, elt.getStartLineType());
		assertEquals (LineType.RECEPTOR_ROUND, elt.getEndLineType());
		
		elt = data.getElementById("a6d48");
		assertEquals (MIMShapes.MIM_NECESSARY_STIMULATION, elt.getStartLineType());
		assertEquals (MIMShapes.MIM_BINDING, elt.getEndLineType());

		elt = data.getElementById("ec31c");
		assertEquals (MIMShapes.MIM_CONVERSION, elt.getStartLineType());
		assertEquals (MIMShapes.MIM_STIMULATION, elt.getEndLineType());

		elt = data.getElementById("dfcd3");
		assertEquals (MIMShapes.MIM_MODIFICATION, elt.getStartLineType());
		assertEquals (MIMShapes.MIM_CATALYSIS, elt.getEndLineType());

		elt = data.getElementById("c89d2");
		assertEquals (MIMShapes.MIM_CLEAVAGE, elt.getStartLineType());
		assertEquals (MIMShapes.MIM_INHIBITION, elt.getEndLineType());
	
		elt = data.getElementById("b3573");
		assertEquals (0.5, elt.getLineThickness(), 0.01);
		
		elt = data.getElementById("afd19");
		assertEquals (1.5, elt.getLineThickness(), 0.01);
		
		elt = data.getElementById("ef9d5");
		assertEquals (Color.RED, elt.getColor());

		elt = data.getElementById("d3f58");
		assertEquals (1, elt.getMAnchors().size());
		assertEquals ("f68db", elt.getMAnchors().get(0).getGraphId());
		
		elt = data.getElementById("id123456789");
		assertEquals ("f68db", elt.getEndGraphRef());
		//TODO: Why are both 0.0? that doesn't make much sense.
		assertEquals (0.0, elt.getMPoints().get(0).getRelX(), 0.01);
		assertEquals (0.0, elt.getMPoints().get(0).getRelY(), 0.01);
		
		elt = data.getElementById("e8ff5");
		assertEquals (5, elt.getMPoints().size());
		assertEquals ("b6665", elt.getMPoints().get(0).getGraphRef());
		assertEquals (0.0, elt.getMPoints().get(0).getRelX(), 0.01);
		assertEquals (1.0, elt.getMPoints().get(0).getRelY(), 0.01);
		assertEquals ("b6665", elt.getMPoints().get(4).getGraphRef());
		assertEquals (0.0, elt.getMPoints().get(4).getRelX(), 0.01);
		assertEquals (-1.0, elt.getMPoints().get(4).getRelY(), 0.01);
		
		elt = data.getElementById("c42c6");
		assertEquals (ConnectorType.CURVED, elt.getConnectorType());
	}

	public void testParser5() throws ConverterException, FileNotFoundException
	{
		File test = new File ("testData/2010a/parsetest5.gpml");
		Pathway data = readHelper (test);
		
		PathwayElement elt;
		elt = data.getElementById("a8a81");
		assertEquals (ObjectType.DATANODE, elt.getObjectType());
		assertEquals (32768, elt.getZOrder());
		assertEquals ("Metabolite", elt.getDataNodeType());
		assertEquals ("Pyruvate", elt.getTextLabel());
		assertEquals (69.0, elt.getMCenterX(), 0.01);
		assertEquals (42.75, elt.getMCenterY(), 0.01);
		assertEquals (84.0, elt.getMWidth(), 0.01);
		assertEquals (20.5, elt.getMHeight(), 0.01);
		assertEquals (10.0, elt.getMFontSize(), 0.01);
		assertEquals (ValignType.TOP, elt.getValign());
		assertEquals (AlignType.CENTER, elt.getAlign());
		assertEquals ("Arial", elt.getFontName());
		assertEquals (Color.BLACK, elt.getColor());
		assertFalse (elt.isTransparent());
		assertFalse (elt.isBold());
		assertFalse (elt.isItalic());
		assertFalse (elt.isStrikethru());
		assertFalse (elt.isUnderline());
		assertEquals ("", elt.getGeneID());
		assertEquals (LineStyle.SOLID, elt.getLineStyle());
		assertEquals (DataSource.getByFullName("EC Number"), elt.getDataSource());
		
		elt = data.getElementById("ec886");
		assertEquals (32767, elt.getZOrder());
		assertEquals ("GeneProduct", elt.getDataNodeType());
		assertEquals ("", elt.getTextLabel());
		assertEquals (ValignType.MIDDLE, elt.getValign());
		assertEquals (AlignType.LEFT, elt.getAlign());
		assertEquals ("", elt.getGeneID());
		assertNull (elt.getDataSource());

		elt = data.getElementById("f7f5c");
		assertEquals (32768, elt.getZOrder());
		assertEquals ("Unknown", elt.getDataNodeType());
		assertEquals ("Fructose", elt.getTextLabel());
		assertEquals (ValignType.MIDDLE, elt.getValign());
		assertEquals (AlignType.CENTER, elt.getAlign());
		assertEquals ("Comic Sans MS", elt.getFontName());
		assertEquals (Color.BLACK, elt.getColor());
		assertEquals (12.0, elt.getMFontSize(), 0.01);
		assertFalse (elt.isTransparent());
		assertTrue (elt.isBold());
		assertFalse (elt.isItalic());
		assertFalse (elt.isStrikethru());
		assertFalse (elt.isUnderline());
		assertEquals ("id", elt.getGeneID());
		assertNull (elt.getDataSource());

		elt = data.getElementById("fbbac");
		assertEquals ("multi-\nline", elt.getTextLabel());
		assertEquals (Color.BLUE, elt.getFillColor());
		assertEquals (ShapeType.ROUNDED_RECTANGLE, elt.getShapeType());
		assertEquals (LineStyle.DASHED, elt.getLineStyle());
		assertEquals (DataSource.getByFullName("Entrez Gene"), elt.getDataSource());
		assertEquals ("3643", elt.getGeneID());
		
		elt = data.getElementById("be269");
		assertEquals (ObjectType.STATE, elt.getObjectType());
		assertEquals ("f7f5c", elt.getGraphRef());
		assertEquals (15.0, elt.getMWidth(), 0.01);
		assertEquals (15.0, elt.getMHeight(), 0.01);
		assertEquals (-1.0, elt.getRelX(), 0.01);
		assertEquals (1.0, elt.getRelY(), 0.01);
		assertEquals (ShapeType.OVAL, elt.getShapeType());
		assertEquals ("1234", elt.getGeneID());
		assertEquals (DataSource.getByFullName("Entrez Gene"), elt.getDataSource());
		
		elt = data.getElementById("ca5fa");
		assertEquals (ShapeType.RECTANGLE, elt.getShapeType());
	}

	public void testParser6() throws ConverterException, FileNotFoundException
	{
		File test = new File ("testData/2010a/parsetest6.gpml");
		Pathway data = readHelper (test);
	}

	public void testParser7() throws ConverterException, FileNotFoundException
	{
		File test = new File ("testData/2010a/parsetest7.gpml");
		Pathway data = readHelper (test);
		
		PathwayElement elt = data.getMappInfo();
		assertEquals (1, elt.getBiopaxRefs().size());
		assertEquals ("c73", elt.getBiopaxRefs().get(0));
		
		elt = data.getElementById("fdba5");
		assertEquals (2, elt.getBiopaxRefs().size());
		assertEquals ("c73", elt.getBiopaxRefs().get(0));
		assertEquals ("b29", elt.getBiopaxRefs().get(1));

		elt = data.getElementById("ab123");
		assertEquals (1, elt.getBiopaxRefs().size());
		assertEquals ("f7c", elt.getBiopaxRefs().get(0));
		
		BiopaxElement be = data.getBiopaxElementManager().getElement("c73");
		assertEquals ("20047655", be.getChild("ID", GpmlFormat.BIOPAX).getText());
		assertEquals ("PubMed", be.getChild("DB", GpmlFormat.BIOPAX).getText());
		assertEquals ("The BridgeDb framework: standardized access to gene, protein and metabolite identifier mapping services.", 
					be.getChild("TITLE", GpmlFormat.BIOPAX).getText());
		assertEquals ("BMC Bioinformatics", be.getChild("SOURCE", GpmlFormat.BIOPAX).getText());
		assertEquals ("2010", be.getChild("YEAR", GpmlFormat.BIOPAX).getText());
	}

	public void testParser8() throws ConverterException, FileNotFoundException
	{
		Pathway data = readHelper (new File ("testData/2010a/parsetest8.gpml"));
		
		PathwayElement elt;
		elt = data.getElementById("a8d1a");
		assertEquals ("e79b2", elt.getGroupRef());

		elt = data.getElementById("af2ec");
		assertEquals ("fb6cc", elt.getGroupRef());
		
		elt = data.getElementById("d8370");
		assertEquals (ObjectType.GROUP, elt.getObjectType());
		assertEquals ("fb6cc", elt.getGroupId());
		assertEquals ("", elt.getTextLabel());
		assertEquals (0, elt.getBiopaxRefs().size());
		assertEquals (0, elt.getComments().size());
		assertEquals (GroupStyle.GROUP, elt.getGroupStyle());
		assertNull (elt.getGroupRef());

		elt = data.getElementById("d8371");
		assertEquals (ObjectType.GROUP, elt.getObjectType());
		assertEquals ("e79b2", elt.getGroupId());
		assertEquals ("Blah", elt.getTextLabel());
		assertEquals (1, elt.getBiopaxRefs().size());
		assertEquals ("a7a", elt.getBiopaxRefs().get(0));
		assertEquals (1, elt.getComments().size());
		assertEquals ("Blah comment", elt.getComments().get(0).getComment());
		assertEquals (GroupStyle.COMPLEX, elt.getGroupStyle());
		assertEquals ("fb6cc", elt.getGroupRef());
	}

	/**
	 * test that documents that shouldn't validate result in an exception
	 */
	public void testFail() throws IOException
	{
		File[] fails = new File[] {
				new File ("testData/2010a/fail1.gpml"),
				new File ("testData/2010a/fail2.gpml"),
				new File ("testData/2010a/fail3.gpml"),
				new File ("testData/2010a/fail4.gpml"),
				new File ("testData/2010a/fail5.gpml"),
				new File ("testData/2010a/fail6.gpml"),
				new File ("testData/2010a/fail7.gpml"),
				new File ("testData/2010a/fail8.gpml"),
				new File ("testData/2010a/fail9.gpml"),
				new File ("testData/2010a/fail10.gpml"),
		};
		
		for (File ffail : fails)
		{
			try
			{
				readHelper (ffail);
				fail ("Expected validation error for file: " + ffail.getName());
			}
			catch (ConverterException ex)
			{
				// ok, expected
			}
		}
		
	}
}	

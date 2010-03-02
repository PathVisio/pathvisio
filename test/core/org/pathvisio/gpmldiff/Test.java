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
package org.pathvisio.gpmldiff;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.ShapeType;
/**
   Unit tests for package GpmlDiff
*/
public class Test extends TestCase
{
	Pathway originalPwy = new Pathway();
	PwyDoc originalDoc;

	/**
	 * TestDiffOutputter is a diff outputter that
	 * tests if all diff events are received in the right
	 * order and in the right number.
	 */
	class TestDiffOutputter extends DiffOutputter
	{
		private int deletions = 0;
		private int insertions = 0;
		private int changes = 0;
		private int modifiedElements = 0;
		private int changedSinceOpen = 0;
		private boolean modifyOpened = false;
		private boolean flushed = false;

		public void checkCounts (int del, int ins, int ch, int mod)
		{
			assertTrue ("Flush expected", flushed);
			assertEquals ("Unexpected number of deletions", del, deletions);
			assertEquals ("Unexpected number of insertions", ins, insertions);
			assertEquals ("Unexpected number of changes", ch, changes);
			assertEquals ("Unexpected number of modified elements", mod, modifiedElements);
		}

		@Override
		public void delete(PathwayElement oldElt)
		{
			assertFalse ("There was a modifyStart event not followed by a modifyEnd event", modifyOpened);
			System.out.println ("Deleted: " + oldElt.getObjectType() + " " + oldElt.getGraphId());
			deletions++;
		}

		@Override
		public void flush() throws IOException
		{
			assertFalse ("There was a modifyStart event not followed by a modifyEnd event", modifyOpened);
			flushed = true;
		}

		@Override
		public void insert(PathwayElement newElt)
		{
			assertFalse ("There was a modifyStart event not followed by a modifyEnd event", modifyOpened);
			insertions++;
		}

		@Override
		public void modifyAttr(String attr, String oldVal, String newVal)
		{
			assertTrue ("There was a modifyevent not preceded by a modifyStart event", modifyOpened);
			assertNotNull ("attr can't be null", attr);
			assertNotNull ("oldVal can't be null", oldVal);
			assertNotNull ("newVal can't be null", newVal);
			System.out.println ("	attribute: " + attr + " ("+ oldVal + " -> " + newVal + ")");
			changes++;
			changedSinceOpen++;
		}

		@Override
		public void modifyEnd()
		{
			assertTrue ("There was a modifyEnd event not preceded by a modifyStart event", modifyOpened);
			modifyOpened = false;
			assertNotSame ("Expected more than 0 changes in modified element", changedSinceOpen, 0);
		}

		@Override
		public void modifyStart(PathwayElement oldElt, PathwayElement newElt)
		{
			assertNotNull ("oldElt can't be null", oldElt);
			assertNotNull ("newElt can't be null", newElt);
			assertFalse ("There was a modifyStart event not followed by a modifyEnd event", modifyOpened);
			System.out.println ("Changed: " + oldElt.getObjectType() + " " + oldElt.getGraphId());
			modifyOpened = true;
			changedSinceOpen = 0;
			modifiedElements++;
		}

	}

	public void setUp()
	{
		try
		{
			Reader reader = new StringReader (
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
				"<Pathway xmlns=\"http://genmapp.org/GPML/2007\" Name=\"New Pathway\" Data-Source=\"GenMAPP 2.0\" Version=\"20070724\">\n" +
				"  <Graphics BoardWidth=\"18000.0\" BoardHeight=\"12000.0\" WindowWidth=\"18000.0\" WindowHeight=\"12000.0\" />\n" +
				"  <Line GraphId=\"aaa\">\n" +
				"    <Graphics Color=\"000000\">\n" +
				"      <Point x=\"1740.0\" y=\"990.0\" Head=\"Arrow\" />\n" +
				"      <Point x=\"2430.0\" y=\"2310.0\" />\n" +
				"    </Graphics>\n" +
				"  </Line>\n" +
				"  <Shape Type=\"Rectangle\" GraphId=\"bd1\">\n" +
				"    <Graphics FillColor=\"Transparent\" Color=\"000000\" CenterX=\"3907.5\" CenterY=\"2197.5\" Width=\"1246.0\" Height=\"1246.0\" Rotation=\"0.0\" />\n" +
				"  </Shape>\n" +
				"  <InfoBox CenterX=\"0.0\" CenterY=\"0.0\" />\n" +
				"</Pathway>\n"
				);
			originalPwy.readFromXml (reader, true);
		}
		catch (ConverterException e)
		{
			fail ("No ConverterException expected in original Pathway");
		}
		originalDoc = new PwyDoc (originalPwy);
	}


	public void testDiffInsertion()
	{
		// read test file
		Pathway pwy = new Pathway();
		PwyDoc newDoc;
		try
		{
			Reader reader = new StringReader (
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
				"<Pathway xmlns=\"http://genmapp.org/GPML/2007\" Name=\"New Pathway\" Data-Source=\"GenMAPP 2.0\" Version=\"20070724\">\n" +
				"  <Graphics BoardWidth=\"18000.0\" BoardHeight=\"12000.0\" WindowWidth=\"18000.0\" WindowHeight=\"12000.0\" />\n" +
				"  <Line GraphId=\"aaa\">\n" +
				"    <Graphics Color=\"000000\">\n" +
				"      <Point x=\"1740.0\" y=\"990.0\" Head=\"Arrow\" />\n" +
				"      <Point x=\"2430.0\" y=\"2310.0\" />\n" +
				"    </Graphics>\n" +
				"  </Line>\n" +
				"  <Shape Type=\"Rectangle\" GraphId=\"bd1\">\n" +
				"    <Graphics FillColor=\"Transparent\" Color=\"000000\" CenterX=\"3907.5\" CenterY=\"2197.5\" Width=\"1246.0\" Height=\"1246.0\" Rotation=\"0.0\" />\n" +
				"  </Shape>\n" +
				"  <Shape Type=\"Brace\">\n" +
				"    <Graphics FillColor=\"Transparent\" Color=\"000000\" CenterX=\"1582.5\" CenterY=\"2640.0\" Width=\"1231.0\" Height=\"823.9999999999999\" Rotation=\"1.5707963267948966\" />\n" +
				"  </Shape>\n" +
				"  <InfoBox CenterX=\"0.0\" CenterY=\"0.0\" />\n" +
				"</Pathway>\n"

				);
			pwy.readFromXml (reader, true);
		}
		catch (ConverterException e)
		{
			e.printStackTrace();
			fail ("No ConverterException expected");
		}
		newDoc = new PwyDoc (pwy);
		SearchNode result = originalDoc.findCorrespondence (newDoc, new BetterSim(), new BasicCost());
		TestDiffOutputter out = new TestDiffOutputter();
		originalDoc.writeResult (result, newDoc, out);
		try
		{
			out.flush();
		}
		catch (IOException e)
		{
			fail ("No exception expected");
		}
		out.checkCounts (0, 1, 0, 0); // check that there is one insertion
	}

	public void testDiffUnchanged()
	{
		// read test file
		Pathway pwy = new Pathway();
		PwyDoc newDoc;
		try
		{
			Reader reader = new StringReader (
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
				"<Pathway xmlns=\"http://genmapp.org/GPML/2007\" Name=\"New Pathway\" Data-Source=\"GenMAPP 2.0\" Version=\"20070724\">\n" +
				"  <Graphics BoardWidth=\"18000.0\" BoardHeight=\"12000.0\" WindowWidth=\"18000.0\" WindowHeight=\"12000.0\" />\n" +
				"  <Line GraphId=\"aaa\">\n" +
				"    <Graphics Color=\"000000\">\n" +
				"      <Point x=\"1740.0\" y=\"990.0\" Head=\"Arrow\" />\n" +
				"      <Point x=\"2430.0\" y=\"2310.0\" />\n" +
				"    </Graphics>\n" +
				"  </Line>\n" +
				"  <Shape Type=\"Rectangle\" GraphId=\"bd1\">\n" +
				"    <Graphics FillColor=\"Transparent\" Color=\"000000\" CenterX=\"3907.5\" CenterY=\"2197.5\" Width=\"1246.0\" Height=\"1246.0\" Rotation=\"0.0\" />\n" +
				"  </Shape>\n" +
				"  <InfoBox CenterX=\"0.0\" CenterY=\"0.0\" />\n" +
				"</Pathway>\n"

				);
			pwy.readFromXml (reader, true);
		}
		catch (ConverterException e)
		{
			fail ("No ConverterException expected");
		}
		newDoc = new PwyDoc (pwy);
		SearchNode result = originalDoc.findCorrespondence (newDoc, new BetterSim(), new BasicCost());
		TestDiffOutputter out = new TestDiffOutputter();
		originalDoc.writeResult (result, newDoc, out);
		try
		{
			out.flush();
		}
		catch (IOException e)
		{
			fail ("No exception expected");
		}
		out.checkCounts (0, 0, 0, 0); // check that there is one deletion
	}

	public void testDiffDeletion()
	{
		Pathway pwy = new Pathway();
		PwyDoc newDoc;
		try
		{
			Reader reader = new StringReader (
						"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
						"<Pathway xmlns=\"http://genmapp.org/GPML/2007\" Name=\"New Pathway\" Data-Source=\"GenMAPP 2.0\" Version=\"20070724\">\n" +
						"  <Graphics BoardWidth=\"18000.0\" BoardHeight=\"12000.0\" WindowWidth=\"18000.0\" WindowHeight=\"12000.0\" />\n" +
						"  <Line GraphId=\"aaa\">\n" +
						"    <Graphics Color=\"000000\">\n" +
						"      <Point x=\"1740.0\" y=\"990.0\" Head=\"Arrow\" />\n" +
						"      <Point x=\"2430.0\" y=\"2310.0\" />\n" +
						"    </Graphics>\n" +
						"  </Line>\n" +
						"  <InfoBox CenterX=\"0.0\" CenterY=\"0.0\" />\n" +
						"</Pathway>\n" +
						"\n"
						);
			pwy.readFromXml (reader, true);
		}
		catch (ConverterException e)
		{
			fail ("No ConverterException expected");
		}
		newDoc = new PwyDoc (pwy);
		SearchNode result = originalDoc.findCorrespondence (newDoc, new BetterSim(), new BasicCost());
		TestDiffOutputter out = new TestDiffOutputter();
		originalDoc.writeResult (result, newDoc, out);
		try
		{
			out.flush();
		}
		catch (IOException e)
		{
			fail ("No exception expected");
		}
		out.checkCounts (1, 0, 0, 0); // check that there is one deletion
	}

	public void testDiffModification()
	{
		Pathway pwy = new Pathway();
		PwyDoc newDoc;
		try
		{
			Reader reader = new StringReader (
								"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
								"<Pathway xmlns=\"http://genmapp.org/GPML/2007\" Name=\"New Pathway\" Data-Source=\"GenMAPP 2.0\" Version=\"20070724\">\n" +
								"  <Graphics BoardWidth=\"18000.0\" BoardHeight=\"12000.0\" WindowWidth=\"18000.0\" WindowHeight=\"12000.0\" />\n" +
								"  <Line GraphId=\"aaa\">\n" +
								"    <Graphics Color=\"000000\">\n" +
								"      <Point x=\"1740.0\" y=\"990.0\" Head=\"Arrow\" />\n" +
								"      <Point x=\"2970.0\" y=\"1605.0\" />\n" +
								"    </Graphics>\n" +
								"  </Line>\n" +
								"  <Shape Type=\"Oval\" GraphId=\"bd1\">\n" +
								"    <Graphics FillColor=\"Transparent\" Color=\"000000\" CenterX=\"3907.5\" CenterY=\"2197.5\" Width=\"1246.0\" Height=\"1246.0\" Rotation=\"0.0\" />\n" +
								"  </Shape>\n" +
								"  <InfoBox CenterX=\"0.0\" CenterY=\"0.0\" />\n" +
								"</Pathway>\n"

								);
			pwy.readFromXml (reader, true);
		}
		catch (ConverterException e)
		{
			fail ("No ConverterException expected");
		}
		newDoc = new PwyDoc (pwy);
		SearchNode result = originalDoc.findCorrespondence (newDoc, new BetterSim(), new BasicCost());
		TestDiffOutputter out = new TestDiffOutputter();
		originalDoc.writeResult (result, newDoc, out);
		try
		{
			out.flush();
		}
		catch (IOException e)
		{
			fail ("No exception expected");
		}
		out.checkCounts (0, 0, 3, 2);
        // check that that there are three modifications spread over two elements
	}

	public void testPatchModification()
	{
		Reader reader = new StringReader (
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
				"<Delta>\n" +
				"  <Modify>\n" +
				"    <Shape xmlns=\"http://genmapp.org/GPML/2007\" Type=\"Rectangle\" GraphId=\"bd1\">\n" +
				"      <Graphics FillColor=\"Transparent\" Color=\"000000\" CenterX=\"3907.5\" CenterY=\"2197.5\" Width=\"1246.0\" Height=\"1246.0\" Rotation=\"0.0\" />\n" +
				"    </Shape>\n" +
				"    <Change attr=\"ShapeType\" old=\"Rectangle\" new=\"Oval\" />\n" +
				"  </Modify>\n" +
				"  <Modify>\n" +
				"    <Line xmlns=\"http://genmapp.org/GPML/2007\" GraphId=\"aaa\">\n" +
				"      <Graphics Color=\"000000\">\n" +
				"        <Point x=\"1740.0\" y=\"990.0\" Head=\"Arrow\" />\n" +
				"        <Point x=\"2430.0\" y=\"2310.0\" />\n" +
				"      </Graphics>\n" +
				"    </Line>\n" +
				"    <Change attr=\"EndX\" old=\"2430.0\" new=\"2970.0\" />\n" +
				"    <Change attr=\"EndY\" old=\"2310.0\" new=\"1605.0\" />\n" +
				"  </Modify>\n" +
				"</Delta>\n");
		Patch patch = new Patch();
		try
		{
			patch.readFromReader (reader);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("Unexpected exception");
		}
		assertEquals (ShapeType.RECTANGLE, originalDoc.getPathway().getElementById("bd1").getShapeType());
		assertEquals (2430.0 / 15, originalDoc.getPathway().getElementById("aaa").getMEndX(), 0.01);
		patch.applyTo (originalDoc, 0);
		assertEquals (2970.0, originalDoc.getPathway().getElementById("aaa").getMEndX(), 0.01);
		assertEquals (ShapeType.OVAL, originalDoc.getPathway().getElementById("bd1").getShapeType());
	}

	public void testPatchInsertion()
	{
		Reader reader = new StringReader (
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
				"<Delta>\n" +
				"  <Insert>\n" +
				"    <Shape xmlns=\"http://genmapp.org/GPML/2007\" Type=\"Brace\" GraphId=\"cd6\">\n" +
				"      <Graphics FillColor=\"Transparent\" Color=\"000000\" CenterX=\"1582.5\" CenterY=\"2640.0\" Width=\"1231.0\" Height=\"823.9999999999999\" Rotation=\"1.5707963267948966\" />\n" +
				"    </Shape>\n" +
				"  </Insert>\n" +
				"</Delta>\n"
				);
		Patch patch = new Patch();
		try
		{
			patch.readFromReader (reader);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("Unexpected exception");
		}
		assertNull (originalDoc.getPathway().getElementById("cd6"));
		patch.applyTo (originalDoc, 0);
		assertNotNull (originalDoc.getPathway().getElementById("cd6"));
	}

	public void testPatchDeletion()
	{
		Reader reader = new StringReader (
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
				"<Delta>\n" +
				"  <Delete>\n" +
				"    <Shape xmlns=\"http://genmapp.org/GPML/2007\" Type=\"Rectangle\" GraphId=\"bd1\">\n" +
				"      <Graphics FillColor=\"Transparent\" Color=\"000000\" CenterX=\"3907.5\" CenterY=\"2197.5\" Width=\"1246.0\" Height=\"1246.0\" Rotation=\"0.0\" />\n" +
				"    </Shape>\n" +
				"  </Delete>\n" +
				"</Delta>\n"
				);
		Patch patch = new Patch();
		try
		{
			patch.readFromReader (reader);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("Unexpected exception");
		}
		assertNotNull (originalDoc.getPathway().getElementById("bd1"));
		patch.applyTo (originalDoc, 0);
		assertNull (originalDoc.getPathway().getElementById("bd1"));
	}


}
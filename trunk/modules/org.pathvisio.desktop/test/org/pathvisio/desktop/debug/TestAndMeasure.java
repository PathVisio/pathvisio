// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.desktop.debug;

import buildsystem.Measure;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import junit.framework.TestCase;

import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.pathvisio.core.Engine;
import org.pathvisio.core.debug.StopWatch;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.view.GeneProduct;
import org.pathvisio.core.view.Line;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.gui.view.VPathwaySwing;

/**
 * Test memory usage and speed of 
 * object creation, pathway loading, selection, and drag operations.
 */
public class TestAndMeasure extends TestCase
{
	private static final File PATHVISIO_BASEDIR = new File ("../..");
	private static final File TEST_PATHWAY = new File (PATHVISIO_BASEDIR, "testData/WP248_2008a.gpml");

	private Measure measure;
	
	@Override public void setUp()
	{
		measure = new Measure("pv_mut.log");
	}
	
	private interface ObjectTester
	{
		String getName();
		public Object create();
	}
	
	private static class MemWatch
	{
		private Runtime runtime = Runtime.getRuntime();

		private void runGC()
		{
			for (int i = 0; i < 20; ++i)
			{
				System.gc();
				try { Thread.sleep(100); } catch (InterruptedException ex) {}
			}
		}
		
		private long memStart;
		
		public void start()
		{
			runGC();
			memStart = (runtime.totalMemory() - runtime.freeMemory());
		}
		
		public long stop()
		{
			runGC();
			long memEnd = (runtime.totalMemory() - runtime.freeMemory());
			return (memEnd - memStart);
		}		
	}
	
	static final int N = 1000;

	private void individialTest(ObjectTester tester) 
	{
		// 1000 warm-up rounds
		for (int i = 0; i < 1000; ++i)
		{
			Object o = tester.create();
		}
		
		Runtime runtime = Runtime.getRuntime();
		for (int i = 0; i < 20; ++i)
		{
			System.gc();
			try { Thread.sleep(100); } catch (InterruptedException ex) {}
		}
		
		Object[] array = new Object[N];
		StopWatch sw = new StopWatch();
		for (int i = 0; i < 20; ++i)
		{
			System.gc();
			try { Thread.sleep(100); } catch (InterruptedException ex) {}
		}
		long memStart = (runtime.totalMemory() - runtime.freeMemory());
		sw.start();
		for (int i = 0; i < N; ++i)
		{
			array [i] = tester.create();
		}
		long msec = sw.stop();
		for (int i = 0; i < 20; ++i)
		{
			System.gc();
			try { Thread.sleep(100); } catch (InterruptedException ex) {}
		}
		long memEnd = (runtime.totalMemory() - runtime.freeMemory());
		measure.add("Memory::" + tester.getName() + " " + N + "x", "" + (memEnd - memStart) / N, "bytes");
		measure.add("Speed::" + tester.getName() + " " + N + "x", "" + (float)(msec) / (float)(N), "msec");
	}

	public void testFile()
	{
		assertTrue ("Missing file required for test: " + TEST_PATHWAY, TEST_PATHWAY.exists());
	}
	
	public void testObjectCreation()
	{
		PreferenceManager.init();
		final Pathway pwy1 = new Pathway();
		final Pathway pwy2 = new Pathway();
		final Pathway pwy3 = new Pathway();
		final VPathway vpwy3 = new VPathway(null);
		final Pathway pwy4 = new Pathway();
		final VPathway vpwy4 = new VPathway(null);
		
		individialTest(new ObjectTester ()
		{
			public Object create() 
			{
				return new Xref("ENS0000001", BioDataSource.ENTREZ_GENE);
			}

			public String getName() 
			{
				return "Xref";
			}
		});

		individialTest(new ObjectTester ()
		{
			public Object create() 
			{
				PathwayElement elt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
				elt.setMCenterX(5);
				elt.setMCenterY(10);
				elt.setMWidth(8);
				elt.setMHeight(10);
				elt.setDataSource(BioDataSource.ENTREZ_GENE);
				elt.setGeneID("3463");
				elt.setTextLabel("INSR");
				pwy1.add (elt);
				return elt;
			}

			public String getName() 
			{
				return "PathwayElement - DataNode";
			}
		});
		individialTest(new ObjectTester ()
		{
			public Object create() 
			{
				PathwayElement elt = PathwayElement.createPathwayElement(ObjectType.LINE);
				elt.setMStartX(5);
				elt.setMStartY(10);
				elt.setMEndX(8);
				elt.setMEndY(10);
				elt.setStartGraphRef("abc");
				elt.setEndGraphRef("def");
				pwy2.add (elt);
				return elt;
			}

			public String getName() 
			{
				return "PathwayElement - Line";
			}
		});


		individialTest(new ObjectTester ()
		{
			public Object create() 
			{
				PathwayElement elt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
				elt.setMCenterX(5);
				elt.setMCenterY(10);
				elt.setMWidth(8);
				elt.setMHeight(10);
				elt.setDataSource(BioDataSource.ENTREZ_GENE);
				elt.setGeneID("3463");
				elt.setTextLabel("INSR");
				pwy3.add (elt);
				VPathwayElement velt = new GeneProduct(vpwy3, elt);
				return velt;
			}

			public String getName() 
			{
				return "M/V GeneProduct pair";
			}
		});
		individialTest(new ObjectTester ()
		{
			public Object create() 
			{
				PathwayElement elt = PathwayElement.createPathwayElement(ObjectType.LINE);
				elt.setMStartX(5);
				elt.setMStartY(10);
				elt.setMEndX(8);
				elt.setMEndY(10);
				elt.setStartGraphRef("abc");
				elt.setEndGraphRef("def");
				pwy4.add (elt);
				VPathwayElement velt = new Line(vpwy4, elt);
				return velt;
			}

			public String getName() 
			{
				return "M/V Line pair";
			}
		});
	}
	
	public void testPathwayLoading() throws ConverterException
	{
		PreferenceManager.init();
		
		StopWatch sw = new StopWatch();
		
		MemWatch mw = new MemWatch();
		
		mw.start(); sw.start(); 
		Pathway pwy = new Pathway();
		pwy.readFromXml(TEST_PATHWAY, true);
		measure.add ("Speed::Hs_Apoptosis readFromXml (+validate)", "" + sw.stop(), "msec");
		measure.add ("Memory::Hs_Apoptosis readFromXml (+validate)", "" + mw.stop() / 1024, "kb");

		mw.start(); sw.start(); 
		JScrollPane sp = new JScrollPane();
		VPathwaySwing wrapper = new VPathwaySwing(sp);

		Engine engine = new Engine();
		VPathway vpwy = wrapper.createVPathway();
		vpwy.activateUndoManager(engine);
		vpwy.fromModel(pwy);
		
		measure.add ("Speed::Hs_Apoptosis create VPathway", "" + sw.stop(), "msec");
		measure.add ("Memory::Hs_Apoptosis create VPathway", "" + mw.stop() / 1024, "kb");

		mw.start(); sw.start(); 
		wrapper.setSize(vpwy.getVWidth(), vpwy.getVHeight());
		BufferedImage image = new BufferedImage(vpwy.getVWidth(), vpwy.getVHeight(), 
				BufferedImage.TYPE_INT_RGB);		
		Graphics2D g2 = image.createGraphics();
		wrapper.paint(g2);
		measure.add ("Speed::Hs_Apoptosis paint", "" + sw.stop(), "msec");
		measure.add ("Memory::Hs_Apoptosis paint", "" + mw.stop() / 1024, "kb");
		g2.dispose();
		
		mw.start(); sw.start(); 
		for (VPathwayElement elt : vpwy.getDrawingObjects())
		{
			elt.select();
		}
		measure.add ("Speed::Hs_Apoptosis select all", "" + sw.stop(), "msec");
		measure.add ("Memory::Hs_Apoptosis select all", "" + mw.stop() / 1024, "kb");
				
		image = new BufferedImage(vpwy.getVWidth(), vpwy.getVHeight(), 
				BufferedImage.TYPE_INT_RGB);
		
		g2 = image.createGraphics();
		wrapper.paint(g2);
		g2.dispose();

		mw.start(); sw.start(); 
		// move all selected items, triggers undo action
		for (int i = 0; i < 10; ++i) vpwy.moveByKey(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), 10);
		measure.add ("Speed::Hs_Apoptosis move up 10x" , "" + sw.stop(), "msec");
		measure.add ("Memory::Hs_Apoptosis move up 10x", "" + mw.stop() / 1024, "kb");		
	}

}

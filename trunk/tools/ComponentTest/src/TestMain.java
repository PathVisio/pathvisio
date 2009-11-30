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
import java.awt.Color;
import java.awt.Dimension;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Random;

import javax.swing.SwingUtilities;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;


public class TestMain {
	TestPanel panel;
	int width;
	int height;
	int number;

	public TestMain(int w, int h, int nr) {
		number = nr;
		width = w;
		height = h;
		panel = new TestPanel();
		panel.setSize(w, h);
	}

	void fill(boolean hook) {
		if(hook) {
			new Thread(addObjects).start();
		} else {
			addObjects.run();
		}
	}

	void toSVG(String f) throws Exception {
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		Document document = domImpl.createDocument ("http://www.w3.org/2000/svg", "svg", null);
		SVGGraphics2D svg = new SVGGraphics2D(document);
		svg.setSVGCanvasSize(new Dimension(width, height));
		panel.print(svg);
		Writer out = null;
		if(f == null) {
			out = new OutputStreamWriter (System.out, "UTF-8");
		} else {
			out = new FileWriter(f);
		}

		svg.stream(out, true);
	}

	Runnable addObjects = new Runnable() {
		public void run() {
			final Dimension size = new Dimension(20, 20);
			final Random r = new Random();
			for(int i = 0; i < number; i++) {
				final int x = (int)(r.nextDouble() * width);
				final int y = (int)(r.nextDouble() * height);

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						TestComponent c = new TestComponent(size, randomColor(r, 128));
						c.setLocation(x, y);
						panel.add(c);
						c.repaint();
					};
				});
			}
		}
	};

	public static void main(String[] args) {
		int w = 1000; int h = 1000; int nr = 1000;

		if(args[0] == null || args[0].equals("gui")) {
			TestGui test = new TestGui(w, h, nr);
			test.fill(true);
			test.frame.setVisible(true);
		} else if(args[0].equals("svg")) {
			String file = args.length > 1 ? args[1] : null;
			TestMain test = new TestMain(w, h, nr);
			test.fill(false);
			try {
				test.toSVG(file);
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			System.err.println(
					"Invalid arguments, use 'gui' or 'svg'"
			);
		}
	}

	static Color randomColor(Random rnd, int alpha) {
		int rgb = java.awt.Color.HSBtoRGB(rnd.nextFloat(), 1, 1);
		Color c = new Color(rgb);
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}
}

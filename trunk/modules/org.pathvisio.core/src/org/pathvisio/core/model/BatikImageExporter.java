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
package org.pathvisio.core.model;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.view.VPathway;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Export Pathway image through Batik, which can handle a number of file formats
 * including SVG, PNG, PDF and TIFF
 */
public class BatikImageExporter extends ImageExporter {

	public BatikImageExporter(String type) {
		super(type);
	}

	public void doExport(File file, VPathway vPathway) throws ConverterException {
		doExport(file, vPathway, null);
	}

	public void doExport(File file, VPathway vPathway, TranscodingHints hints) throws ConverterException {
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		Document svg = domImpl.createDocument ("http://www.w3.org/2000/svg", "svg", null);

		SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(svg);
		
		boolean textAsPath = PreferenceManager.getCurrent().getBoolean(GlobalPreference.SVG_TEXT_AS_PATH);
		SVGGraphics2D svgG2d = new SVGGraphics2D(ctx, textAsPath);
		
		vPathway.draw(svgG2d);

		//Force recalculation of size after drawing once, this allows size of text
		//to be calculated correctly
		Dimension size = vPathway.calculateVSize();
		svgG2d.setSVGCanvasSize(size);

		Transcoder t = null;
		if			(getType().equals(TYPE_SVG)) {
			try {
				Writer out = new FileWriter(file);
				svgG2d.stream(out, true);
				out.flush();
				out.close();
			} catch(Exception e) {
				throw new ConverterException(e);
			}
			return;
		} else if	(getType().equals(TYPE_PNG)) {
			t = new PNGTranscoder();
		} else if	(getType().equals(TYPE_TIFF)) {
			t = new TIFFTranscoder();
		} else if	(getType().equals(TYPE_PDF)) {
			try {
                 Class<?> pdfClass = Class.forName("org.apache.fop.svg.PDFTranscoder");
                 t = (Transcoder)pdfClass.newInstance();
             } catch(Exception e) {
            	 noExporterException();
             }
		}
		if(t == null) noExporterException();

		svgG2d.getRoot(svg.getDocumentElement());
		t.addTranscodingHint(ImageTranscoder.KEY_BACKGROUND_COLOR, java.awt.Color.WHITE);
		if(hints != null) {
			for(Object o : hints.keySet()) {
				t.addTranscodingHint((TranscodingHints.Key)o, hints.get(o));
			}
		}
		try {
			TranscoderInput input = new TranscoderInput(svg);

			// Create the transcoder output.
			OutputStream ostream = new FileOutputStream(file);
			TranscoderOutput output = new TranscoderOutput(ostream);

			// Save the image.
			t.transcode(input, output);

		    // Flush and close the stream.
	        ostream.flush();
	        ostream.close();
		} catch(Exception e) {
			throw new ConverterException(e);
		}
	}

	public void doExport(File file, Pathway pathway) throws ConverterException
	{
		VPathway vPathway = new VPathway(null);
		vPathway.fromModel(pathway);

		doExport(file, vPathway);
		vPathway.dispose();
	}
}

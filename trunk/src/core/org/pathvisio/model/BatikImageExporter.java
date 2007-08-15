package org.pathvisio.model;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.pathvisio.view.VPathway;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public class BatikImageExporter extends ImageExporter {

	public BatikImageExporter(String type) {
		super(type);
	}

	public void doExport(File file, Pathway pathway) throws ConverterException {		
		VPathway vPathway = new VPathway(null);
		vPathway.fromGmmlData(pathway);
		
		double width = vPathway.getVWidth();
		double height = vPathway.getVHeight();
		
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		Document svg = domImpl.createDocument ("http://www.w3.org/2000/svg", "svg", null);
		
		SVGGraphics2D svgG2d = new SVGGraphics2D(svg);
		svgG2d.setSVGCanvasSize(new Dimension((int)width, (int)height));
		vPathway.draw(svgG2d);
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
                 Class pdfClass = Class.forName("org.apache.fop.svg.PDFTranscoder");
                 t = (Transcoder)pdfClass.newInstance();
             } catch(Exception e) {
            	 noExporterException();
             }
		}
		if(t == null) noExporterException();

		svgG2d.getRoot(svg.getDocumentElement());
		t.addTranscodingHint(ImageTranscoder.KEY_BACKGROUND_COLOR, java.awt.Color.WHITE);

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
}

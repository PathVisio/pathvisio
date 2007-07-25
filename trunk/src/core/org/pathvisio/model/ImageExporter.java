// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;

public class ImageExporter implements PathwayExporter {
	public static final int TYPE_PNG = 0;
	public static final int TYPE_TIFF = 1;
	public static final int TYPE_PDF = 2;
	
	private int type;
	private String[] extensions;
	
	public ImageExporter(int type) {
		this.type = type;
	}
	
	public String[] getExtensions() {
		if(extensions == null) {
			extensions = new String[] { getDefaultExtension() };
		}
		return extensions;
	}
	
	public String getDefaultExtension() {
		switch(type) {
		case TYPE_PNG:
			return "png";
		case TYPE_TIFF:
			return "tiff";
		case TYPE_PDF:
			return "pdf";
		default:
			return null;
		}
	}

	public String getName() {
		switch(type) {
		case TYPE_PNG:
			return "PNG";
		case TYPE_TIFF:
			return "TIFF";
		case TYPE_PDF:
			return "PDF";
		default:
			return null;
		}
		
	}
	
	public void doExport(File file, Pathway pathway) throws ConverterException {
		Document svg = SvgFormat.createJdom(pathway);
		
		Transcoder t = null;
		switch(type) {
		case TYPE_PNG:
			t = new PNGTranscoder();
			break;
		case TYPE_TIFF:
			t = new TIFFTranscoder();
			break;
		case TYPE_PDF: try {
                 Class pdfClass = Class.forName("org.apache.fop.svg.PDFTranscoder");
                 t = (Transcoder)pdfClass.newInstance();
             } catch(Exception e) {
            	 noExporterException();
             }
		}
		if(t == null) noExporterException();

		t.addTranscodingHint(ImageTranscoder.KEY_BACKGROUND_COLOR, java.awt.Color.WHITE);

		try {
			TranscoderInput input = new TranscoderInput(convertToDOM(svg));

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

	public void noExporterException() throws ConverterException {
		throw new ConverterException("No exporter for this image format");
	}

	public org.w3c.dom.Document convertToDOM(org.jdom.Document jdomDoc) throws JDOMException {
		DOMOutputter outputter = new DOMOutputter();
		return outputter.output(jdomDoc);
	}

}

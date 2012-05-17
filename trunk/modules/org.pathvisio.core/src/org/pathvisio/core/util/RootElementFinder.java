package org.pathvisio.core.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

// TODO: this is similar to com.sun.xml.internal.ws.message.RootElementSniffer, perhaps can be re-used.
public class RootElementFinder
{
	static class RootElementHandler extends DefaultHandler
	{
		private String rootQName = null;
		private String rootUri = null;

		@Override
		public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if (rootQName == null && rootUri == null) 
			{
				rootQName = qName;
				rootUri = uri;
			}
			
			// TODO: stop parsing here...
		}

		public String getRootQName()  { return rootQName; }
		public String getRootUri()  { return rootUri; }
	};

	/**
	 * Special reader that works around problem with UTF-BOM
	 */
	private static Reader inputStreamToReader(InputStream xin) throws IOException 
	{
		BufferedInputStream in = new BufferedInputStream(xin);
		in.mark(3);
		int byte1 = in.read();
		int byte2 = in.read();
		if (byte1 == 0xFF && byte2 == 0xFE) {
			return new InputStreamReader(in, "UTF-16LE");
		} else if (byte1 == 0xFF && byte2 == 0xFF) {
			return new InputStreamReader(in, "UTF-16BE");
		} else {
			int byte3 = in.read();
			if (byte1 == 0xEF && byte2 == 0xBB && byte3 == 0xBF) {
				return new InputStreamReader(in, "UTF-8");
			} else {
				in.reset();
				return new InputStreamReader(in);
			}
		}
	}

	private static RootElementHandler parse(File file) throws FileNotFoundException, IOException, SAXException
	{
		XMLReader xr;	
		xr = XMLReaderFactory.createXMLReader();

		RootElementHandler rootElementHandler = new RootElementHandler();

		xr.setEntityResolver(null);		
		xr.setContentHandler(rootElementHandler);
		xr.setErrorHandler(rootElementHandler);
		xr.parse(new InputSource(inputStreamToReader(new FileInputStream (file))));

		return rootElementHandler;
	}

	public static String getRootUri (File file) throws SAXException, FileNotFoundException, IOException
	{
		RootElementHandler handler = parse(file);
		return handler.getRootUri();
	}

}

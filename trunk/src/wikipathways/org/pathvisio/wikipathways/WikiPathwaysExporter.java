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
package org.pathvisio.wikipathways;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.GpmlFormat;
import org.pathvisio.core.model.ImageExporter;
import org.pathvisio.core.model.Pathway;

/**
 * Special exporter that uses the WikiPathways XMLRPC interface to
 * convert to SVG and other image formats. By letting the server
 * do all the heavy work, we don't need libraries such as Batik
 * in the Applet and thus we can keep the Applet small.
 */
public class WikiPathwaysExporter extends ImageExporter {

	URL rpcUrl;

	public WikiPathwaysExporter(URL rpcUrl, String type) {
		super(type);
		this.rpcUrl = rpcUrl;
	}


	public void doExport(File file, Pathway pathway) throws ConverterException {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(rpcUrl);

		XmlRpcClient client = new XmlRpcClient();
		XmlRpcTransportFactory ctf = new XmlRpcSunHttpTransportFactory(client);
		client.setTransportFactory(ctf);
		client.setConfig(config);

		try {
			Document doc = GpmlFormat.createJdom(pathway);
			XMLOutputter outputter = new XMLOutputter();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			outputter.output(doc, out);

			byte[] data = out.toByteArray();
			byte[] data64 = Base64.encodeBase64(data);

			Object[] params = new Object[]{ data64, getDefaultExtension() };

			byte[] converted64 = ((String)client.execute("WikiPathways.convertPathway", params)).getBytes();
			Base64.decodeBase64(converted64);
			byte[] converted = Base64.decodeBase64(converted64);

			FileOutputStream fout = new FileOutputStream(file);
			fout.write(converted);
			fout.close();
		} catch(Exception e) {
			throw new ConverterException(e);
		}
	}
}

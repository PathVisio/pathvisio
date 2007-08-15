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
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.ImageExporter;
import org.pathvisio.model.Pathway;

public class WikiPathwaysExporter extends ImageExporter {
	public final static String TYPE_MAPP = "mapp";
	
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

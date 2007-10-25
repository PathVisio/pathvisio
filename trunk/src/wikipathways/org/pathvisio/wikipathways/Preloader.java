package org.pathvisio.wikipathways;

import java.applet.Applet;

public class Preloader extends Applet {
	public void init() {
		System.out.println("== Preloader started ==");
		//Do nothing, just called to download the jars and start the JVM!

		String[] classes = new String[] {
				"org.pathvisio.wikipathways.WikiPathways",
				"org.pathvisio.gui.wikipathways.AppletMain",
				"org.pathvisio.model.Pathway", //Triggers wikipathways.jar
				"org.apache.xmlrpc.client.XmlRpcClient", //Triggers xmlrpc-client.jar
				"org.apache.xmlrpc.XmlRpcConfig", //Triggers xmlrpc-common.jar
				"org.apache.commons.codec.Decoder", //Triggers commons-codec.jar
				"org.apache.commons.httpclient.HttpClient", //Triggers commons-httpclient.jar
				"org.apache.derby.jdbc.ClientDriver", //Triggers derbyclient.jar
				"org.jdom.Document", //Triggers jdom.jar
		};

		String[] resources = new String[] {
			"icons/export.gif", //Triggers resources.jar
		};
		
		new PreloaderThread(classes, resources).start();
	}
	
	class PreloaderThread extends Thread {
		String[] classes;
		String[] resources;
		
		public PreloaderThread(String[] classes, String[] resources) {
			this.classes = classes;
			this.resources = resources;
		}
		
		public void run() {
			for(String cln : classes) {
				try {
					System.out.println("> Loading class " + cln);
					Class.forName(cln);
				} catch(ClassNotFoundException e) {
					System.out.println("Error loading class " + cln);
				}
			}
			
			for(String rn : resources) {
				try {
					Preloader.class.getClassLoader().getResource(rn);
				} catch(Exception e) {
					System.out.println("Error loading resource " + rn);
				}
			}
		}
	};
}

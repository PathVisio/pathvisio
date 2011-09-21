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
package org.wikipathways.applet;

import java.applet.Applet;

public class Preloader extends Applet {
	public void init() {
		/* Don't preload jars for now (we don't want the certificate
		 * dialog to popup!
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
			"export.gif", //Triggers resources.jar
		};

		new PreloaderThread(classes, resources).start();
		*/
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

// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2013 BiGCaT Bioinformatics
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
package org.pathvisio.pluginmanager.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Pattern;

import org.apache.felix.bundlerepository.Resource;
import org.pathvisio.pluginmanager.impl.data.BundleAuthor;
import org.pathvisio.pluginmanager.impl.data.BundleVersion;

/**
 * Various utility functions
 * @author martina
 *
 */
public class Utils {
	
	/**
	 * copies a file to another location
	 * @param sourceFile = file to be copied
	 * @param destFile = destination file
	 */
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
	
	/**
	 * downloads a file and saves it in the bundle directory
	 * @param uri = URI of the repository file
	 * @param resource = bundle that should be downloaded
	 * @param bundleDir = destination directory
	 */
	public static File downloadFile(String uri, Resource resource, File bundleDir) throws Exception {
		File localFile = new File(bundleDir,resource.getSymbolicName()+"-"+resource.getVersion()+".jar");

		FileOutputStream fos = null;
		ReadableByteChannel rbc = null;
		URL website = new URL(uri);
		rbc = Channels.newChannel(website.openStream());
		fos = new FileOutputStream(localFile);
		fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		fos.close();
		rbc.close();
		return localFile;
	}
	
	/**
	 * gets pathvisio.xml file from repository URL
	 * this file contains the bundle information
	 * @param url = repository URL (repository.pathvisio.org/repository.xml)
	 */
	public static URL getXMLURL(URL url) throws MalformedURLException {
        // represent the path portion of the URL as a file
        File file = new File( url.getPath( ) );
        File xml = new File(file.getParent(),"pathvisio.xml");
 
        // construct a new url with the parent path
        URL parentUrl = new URL( url.getProtocol( ), url.getHost( ), url.getPort( ), xml.getPath() );
        return parentUrl;
	}
	
	/**
	 * formats semantic version and removes additional unnecessary elements
	 */
	public static String formatVersion(String version) {
		String [] buffer = version.split("\\.");
		if(buffer.length > 3) {
			version = buffer[0] + "." + buffer[1] + "." + buffer[2];
		}
		return version;
	}
   
    public static String formatText(String text, int length) {
    	if(text.length() > length) {
    		String str = "<html>";
    		
    		for(int i = 0; i < text.length(); i=i+length) {
    			if(i+length < text.length()) {
    				String s = text.substring(i, i+length);
    				int index = s.lastIndexOf(" ");
    				if(index == -1) {
    					str = str + text.substring(i,i+length) + "<br>";
    				} else {
    					str = str + text.substring(i, i+index) + "<br>";
    					i = i - (s.length()-index-1);
    				}
    			} else {
    				str = str + text.substring(i, text.length()-1);
    			}
    		}
    		
    		str = str + "</html>";
    		return str;
    	} else {
    		return text;
    	}
    }
    
    public static String printDescription(String text, int length) {
    	if(text.length() > length) {
    		String str = "<html>Description:<br>";
    		
    		for(int i = 0; i < text.length(); i=i+length) {
    			if(i+length < text.length()) {
    				String s = text.substring(i, i+length);
    				int index = s.lastIndexOf(" ");
    				if(index == -1) {
    					str = str + text.substring(i,i+length) + "<br>";
    				} else {
    					str = str + text.substring(i, i+index) + "<br>";
    					i = i - (s.length()-index-1);
    				}
    			} else {
    				str = str + text.substring(i, text.length()-1);
    			}
    		}
    		
    		str = str + "</html>";
    		return str;
    	} else {
    		return text;
    	}
    }
    
    public static String printAuthors(BundleVersion version) {
    	if(version.getAuthors().size() > 0) {
	    	String str = "<html>Developers:<br>";
	    	
	    	int i = 1;
	    	for(BundleAuthor author : version.getAuthors()) {
	    		String firstName = author.getDeveloper().getFirstName();
	    		String lastName = author.getDeveloper().getLastName();
	    		String aff = author.getAffiliation().getName();
	    		
	    		str = str + "   " + i + ") " + firstName + " " + lastName + "<br>" + aff + "<br>";
	    		i++;
	    	}
	    	
	    	str = str + "</html>";
	    	return str;
    	}
    	return "";
    }
    
    public static int compareVersions(String v1, String v2) {
        String s1 = normalisedVersion(v1);
        String s2 = normalisedVersion(v2);
        int cmp = s1.compareTo(s2);
        // -1 v1 < v2
        // 0 v1 = v2
        // 1 v1 > v2
        return cmp < 0 ? -1 : cmp > 0 ? 1 : 0;
    }

    private static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    private static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }
}

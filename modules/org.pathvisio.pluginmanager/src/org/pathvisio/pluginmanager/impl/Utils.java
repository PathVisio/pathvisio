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

public class Utils {
	
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
	
	public static File downloadFile(String uri, Resource resource, File bundleDir) {
		File localFile = new File(bundleDir,resource.getSymbolicName()+"-"+resource.getVersion()+".jar");
		System.out.println(localFile.getAbsolutePath());

		FileOutputStream fos = null;
		ReadableByteChannel rbc = null;
		try {
			URL website = new URL(uri);
		    rbc = Channels.newChannel(website.openStream());
		    fos = new FileOutputStream(localFile);
		    fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		 
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
				rbc.close();
				return localFile;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static URL getXMLURL(URL url) throws MalformedURLException {
        // represent the path portion of the URL as a file
        File file = new File( url.getPath( ) );
        File xml = new File(file.getParent(),"pathvisio.xml");
 
        // construct a new url with the parent path
        URL parentUrl = new URL( url.getProtocol( ), url.getHost( ), url.getPort( ), xml.getPath() );
        return parentUrl;
	}
	
	public static int compare(String v1, String v2) {
        String s1 = normalisedVersion(v1);
        String s2 = normalisedVersion(v2);
        return s1.compareTo(s2);
    }
	
	public static String formatVersion(String version) {
		String [] buffer = version.split("\\.");
		if(buffer.length > 3) {
			version = buffer[0] + "." + buffer[1] + "." + buffer[2];
		}
		return version;
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
    
    public static String formatText(String text, int length) {
//    	text = text.replace("\n", "");
//		if(text.length() > length) {
//			String result = "<html>";
//			for(int i = 0; i < text.length(); i=i+length) {
//				int end = i+length;
//				if (text.length() < i+length) end = text.length();
//				String s = text.substring(i, end);
//				System.out.println(s);
//				int index = s.lastIndexOf(" ");
//				if(index == -1) {
//					result = result + s + "<br>";
//				} else {
//					result = result + text.substring(i, i+index) + "<br>";
//					i = i - (s.length()-index);
//				}
//			}
//			result = result + "</html>";
//			System.out.println(result);
//			return result;
//		} else return text;
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
	    		String text = Utils.formatText(author.getDeveloper().getFirstName() + " " + 
	    				author.getDeveloper().getLastName() + ",  " + author.getAffiliation().getName(), 40);
	    		str = str + "     " + i + ") " + text + "<br>";
	    		i++;
	    	}
	    	
	    	str = str + "</html>";
	    	
	    	return str;
    	}
    	return "";
    }
}

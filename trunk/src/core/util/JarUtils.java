package util;

import gmmlVision.GmmlVision;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtils {
	static final String PREFIX_TMP = GmmlVision.APPLICATION_NAME;
	
	public static File resourceToTempFile(String name) throws FileNotFoundException, IOException {
		File tmp = File.createTempFile(PREFIX_TMP, null, null);
		resourceToFile(name, tmp);
		return tmp;
	}
	
	public static File resourceToNamedTempFile(String name, String fileName) 
											throws FileNotFoundException, IOException {
		return resourceToNamedTempFile(name, fileName, true);
	}
	
	public static File resourceToNamedTempFile(String name, String fileName, boolean overwrite) 
											throws FileNotFoundException, IOException {
		File tmp = new File(System.getProperty("java.io.tmpdir"), fileName);
		
		if(!overwrite && tmp.exists()) return tmp;
		
		tmp.deleteOnExit();
		resourceToFile(name, tmp);
		return tmp;
	}
	
	public static void resourceToFile(String name, File f) throws IOException {
		InputStream in = getResourceInputStream(name);
		if(in == null) throw new IOException("Unable to load resource '" + name + "'");
		
		OutputStream out = new FileOutputStream(f);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}
	
	public static List<String> listResources(String path) throws IOException {
		List<String> resNames = new ArrayList<String>();

		URL url = GmmlVision.class.getClassLoader().getResource(path);
		if(url != null) {
			if(url.getProtocol().equals("jar")) {
				JarURLConnection conn = (JarURLConnection)url.openConnection();
				JarFile jf = conn.getJarFile();
				Enumeration e = jf.entries();
				while(e.hasMoreElements()) {
					JarEntry je = (JarEntry)e.nextElement();
					if(!je.isDirectory() && je.getName().startsWith(path))
						resNames.add(je.getName());
				}
			}
		}
		return resNames;
	}
	
	/**
	 * Get the {@link URL} for the resource stored in a jar file in the classpath
	 * @param name	the filename of the resource
	 * @return the URL pointing to the resource
	 */
	public static URL getResourceURL(String name) {
		URL url = GmmlVision.class.getClassLoader().getResource(name);
		if(url == null) GmmlVision.log.error("Couldn't load resource '" + name + "'");
		return url;
	}
	
	/**
	 * Get the {@link InputStream} for the resource stored in a jar file in the classpath
	 * @param name	the filename of the resource
	 * @return the URL pointing to the resource
	 */
	public static InputStream getResourceInputStream(String name) {
		InputStream in = GmmlVision.class.getClassLoader().getResourceAsStream(name);
		if(in == null) GmmlVision.log.error("Couldn't load resource '" + name + "'");
		return in;
	}
}

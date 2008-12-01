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
package org.pathvisio.wikipathways.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.rpc.ServiceException;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pathvisio.Engine;
import org.pathvisio.model.BatikImageExporter;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.preferences.PreferenceManager;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayWrapperBase;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.client.SearchException;
import org.pathvisio.wikipathways.webservice.WSIndexField;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;
import org.pathvisio.wikipathways.webservice.WSSearchResult;

/**
 * Manages the cache of preview images
 * @author thomas
 */
public class ImageManager {
	public static final String PAR_UPDATE_CACHE = "updateCache";
	public static final int IMG_SIZE = 150;
	public static final String IMG_TYPE_CACHE = "svg";
	public static final String IMG_TYPE = "png";
	
	private static ImageManager imageManager;
	
	public static boolean isInit() {
		return imageManager != null;
	}
	
	public static void init(WikiPathwaysClient client) {
		imageManager = new ImageManager(client);
	}
	
	public static ImageManager getInstance() throws ServiceException {
		return imageManager;
	}
	
	static final int SLEEP_INTERVAL = 250;
	static final int TIMEOUT = 60000;
	static final String IMG_PATH = "images/";
	
	WikiPathwaysClient client;
	
	//Download management
	BlockingQueue<Runnable> workqueue = new LinkedBlockingQueue<Runnable>();
	ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 3, 60, TimeUnit.SECONDS, workqueue);
	private volatile Set<String> activeDownloads = new HashSet<String>();
	
	private String srvBasePath = "";
	
	private ImageManager(WikiPathwaysClient client) {
		this.client = client;
	}
	
	public void setServerBasePath(String path) {
		srvBasePath = path;
		new File(srvBasePath + "/" + IMG_PATH).mkdirs();
	}
	
	public void waitForImage(String id) throws TimeoutException, TranscoderException, IOException {
		File cacheImg = getImageFile(id);
		long start = System.currentTimeMillis();
		while(!cacheImg.exists()) {
			try {
				Thread.sleep(SLEEP_INTERVAL);
				if(System.currentTimeMillis() - start > TIMEOUT) {
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(!cacheImg.exists()) {
			//timeout, throw exception
			throw new TimeoutException();
		}
	}
	
	public byte[] getImageData(String id) throws IOException {
		File resized = getImageFile(id);
		return getBytesFromFile(resized);
	}
	
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        // Get the size of the file
        long length = file.length();
        byte[] bytes = new byte[(int)length];
        // Read in the data
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
        is.close();
        return bytes;
    }
    
    public Pathway getPathway(WSSearchResult wsr) throws ConverterException {
    	downloadGpml(wsr);
    	Pathway pathway = new Pathway();
    	pathway.readFromXml(
    			getGpmlFile(getGpmlId(wsr.getId(), wsr.getRevision())), true
    	);
    	return pathway;
    }
    
	public void startDownload(final WSSearchResult wsr) {
		final String id = getGpmlId(wsr.getId(), wsr.getRevision());
		//Only start if we're not already downloading
		if(!activeDownloads.contains(id)) {
			activeDownloads.add(id);
			executor.execute(new Runnable() {
				public void run() {
					try {
						downloadGpml(wsr);
						writeImageCache(wsr);
						activeDownloads.remove(id);
					} catch(Exception e) {
						throw new SearchException(e);
					}
				}
			});
		}
	}
	
	private void writeImageCache(WSSearchResult wsr) throws ConverterException {
		writeImageCache(wsr.getId(), wsr.getRevision());
	}
	
	private void writeImageCache(String id, String revision) throws ConverterException {
		String gid = getGpmlId(id, revision);
		File cacheImg = getImageFile(gid);
		if(cacheImg.exists()) return; //Cache already exists

		File cacheGpml = getGpmlFile(getGpmlId(id, revision));
		
		Pathway pathway = new Pathway();
		pathway.readFromXml(cacheGpml, true);
		
		BatikImageExporter exp = new BatikImageExporter(BatikImageExporter.TYPE_PNG);
		TranscodingHints hints = new TranscodingHints();
		hints.put(PNGTranscoder.KEY_WIDTH, (float)IMG_SIZE);
		hints.put(PNGTranscoder.KEY_HEIGHT, (float)IMG_SIZE);
		
		//Disable highlighting for now
//		Set<String> highlightIds = new HashSet<String>();
//		
//		WSIndexField[] fields = wsr.getFields();
//		if(fields != null) {
//			for(WSIndexField f : wsr.getFields()) {
//				if("graphId".equals(f.getName())) {
//					for(String graphId : f.getValues()) {
//						highlightIds.add(graphId);
//					}
//				}
//			}
//		}
		
		PreferenceManager.init();
		VPathway vpathway = new VPathway(new VPathwayWrapperBase());
		vpathway.fromModel(pathway);
		
		//Disable highlighting for now
//		for(String gid : highlightIds) {
//			PathwayElement pwe = pathway.getElementById(gid);
//			if(pwe != null) {
//				VPathwayElement vpwe = vpathway.getPathwayElementView(pwe);
//				switch(pwe.getObjectType()) {
//				case ObjectType.DATANODE:
//				case ObjectType.SHAPE:
//					pwe.setFillColor(Color.RED);
//				}
//				if(vpwe != null) {
//					vpwe.highlight(Color.RED);
//				}
//			}
//		}
		exp.doExport(cacheImg, vpathway, hints);
	}
	
	private void downloadGpml(WSSearchResult wsr) {
		writeGpmlCache(wsr.getId(), wsr.getRevision());
	}
	
	/**
	 * Downloads the gpml cache to create images from.
	 * @param wsr The search result to get the gpml for
	 */
	private void writeGpmlCache(String id, String revision) {
		File file = getGpmlFile(getGpmlId(id, revision));
		if(!file.exists()) {
			try {
				WSPathway wsp = client.getPathway(id, Integer.parseInt(revision));
				Pathway p = WikiPathwaysClient.toPathway(wsp);
				p.writeToXml(file, false);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private File getGpmlFile(String gpmlId) {
		return new File(srvBasePath + "/" + gpmlId);
	}
	
	private File getImageFile(String id) {
		return new File(srvBasePath + "/" + getImagePath(id));
	}
	
	private String getImagePath(String id) {
		return IMG_PATH + id;
	}
	
	public static String getGpmlId(String id, String revision) {
		return id + revision;
	}
	
	public static String getResultId(WSSearchResult wsr) {
		HashCodeBuilder hb = new HashCodeBuilder(17, 37);
		hb.append(wsr.getUrl()); //Holds the pathway id and source
		hb.append(wsr.getRevision()); //Holds the pathway version
		WSIndexField[] fields = wsr.getFields(); //Holds the search type
		if(fields != null) {
			for(WSIndexField f : fields) {
				for(String v : f.getValues()) {
					hb.append(f.getName() + v);
				}
			}
		}
		return "" + hb.toHashCode();
	}
	
	public void updateAllCache() {
		try {
			WSPathwayInfo[] pathways = client.listPathways();
			for(WSPathwayInfo p : pathways) {
				writeGpmlCache(p.getId(), p.getRevision());
				writeImageCache(p.getId(), p.getRevision());
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (ConverterException e) {
			e.printStackTrace();
		}
	}
}

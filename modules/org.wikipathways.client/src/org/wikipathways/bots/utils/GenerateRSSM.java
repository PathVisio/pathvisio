package org.wikipathways.bots.utils;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.axis.encoding.Base64;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.bridgedb.bio.Organism;
import org.bridgedb.rdb.GdbProvider;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.pathvisio.core.biopax.BiopaxNode;
import org.pathvisio.core.biopax.PublicationXref;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.PathwayElement.Comment;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.wikipathways.webservice.WSCurationTag;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;
import org.wikipathways.client.WikiPathwaysCache;
import org.wikipathways.client.WikiPathwaysClient;

public class GenerateRSSM {
	
	private String sourceUrl;
	private GdbProvider idmp;
	
	private DataSource GENE_DS = DataSource.getBySystemCode("L");
	private DataSource MET_DS = DataSource.getBySystemCode("Cpc");
	
	public static final String COMMENT_DESCRIPTION = "WikiPathways-description";
	public static final String COMMENT_CATEGORY = "WikiPathways-category";
	
	private int imgSize = 400;

	private String[] includeTags = new String[] {
		"Curation:FeaturedPathway",
		"Curation:AnalysisCollection",
	};
	
	WikiPathwaysCache cache;
	WikiPathwaysClient client;
	
	Map<String, String> org2taxid = new HashMap<String, String>();
		
	public GenerateRSSM(WikiPathwaysCache cache, WikiPathwaysClient client, GdbProvider idmp) throws IOException {
		this.cache = cache;
		this.idmp = idmp;
		this.client = client;
		initTaxids();
	}
	
	private void initTaxids() throws IOException {
		Pattern p = Pattern.compile("<Id>([0-9]+)<\\/Id>");
		String base = "http://www.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=taxonomy&term=";
		for(String org : client.listOrganisms()) {
			URL url = new URL(base + URLEncoder.encode(org));
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String tax = null;
			String line;
			while ((line = in.readLine()) != null) {
				Matcher m = p.matcher(line);
				if(m.find()) {
					tax = m.group(1);
					break;
				}
			}
			in.close();
			
			if(tax != null) org2taxid.put(org, tax);
		}
	}
	
	public void setIncludeTags(String[] includeTags) {
		this.includeTags = includeTags;
	}
	
//	public void setExcludeTags(String[] excludeTags) {
//		this.excludeTags = excludeTags;
//	}
	
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	
	public Document createRSSM(Collection<File> pathwayFiles) throws FileNotFoundException, IOException, ConverterException, IDMapperException {
		Document doc = new Document();
		
		DocType doctype = new DocType("biosystems", "rssm.dtd");
		doc.setDocType(doctype);
		
		Element root = new Element("biosystems");
		doc.setRootElement(root);
		
		addGeneralSection(root);
		
		System.out.println("Getting list of pathways to filter out based on curation tag");
//		Set<String> excludeIds = new HashSet<String>();
//		for(String tag : excludeTags) {
//			for(WSCurationTag t : client.getCurationTagsByName(tag)) {
//				excludeIds.add(t.getPathway().getId());
//			}
//		}
//		log.info("Filtering out " + excludeIds.size() + " pathways based on tag.");
		Set<String> includeIds = new HashSet<String>();
		for(String tag : includeTags) {
			for(WSCurationTag t : client.getCurationTagsByName(tag)) {
				includeIds.add(t.getPathway().getId());
			}
		}
//		log.info("Including " + excludeIds.size() + " pathways based on tag.");
		
		int i = 0;
		for(File f : pathwayFiles) {
			if(i % 10 == 0) System.out.println("Processing pathway " + ++i + " out of " + pathwayFiles.size());
			
			WSPathwayInfo info = cache.getPathwayInfo(f);
//			if(excludeTags.length > 0 && excludeIds.contains(info.getId())) {
//				log.info("Skipping " + info.getId() + ", filtered out by curation tag");
//				continue;
//			}
			if(includeTags.length > 0 && !includeIds.contains(info.getId())) {
				System.out.println("Skipping " + info.getId() + ", filtered out because doesn't have one of the curation tags to include.");
				continue;
			}
			addPathway(root, f, info);
		}
		
		return doc;
	}
	
	private void addPathway(Element root, File f, WSPathwayInfo info) throws ConverterException, IOException, IDMapperException {
		Pathway p = new Pathway();
		p.readFromXml(f, false);
		
		Element biosystem = new Element("biosystem");
		root.addContent(biosystem);
		
		Element extid = new Element("externalid");
		extid.setText(info.getId());
		biosystem.addContent(extid);
		Element name = new Element("name");
		name.setText(info.getName());
		biosystem.addContent(name);
		Element systype = new Element("biosystemtype");
		biosystem.addContent(systype);
		Element orgtype = new Element("organism_specific_biosystem");
		systype.addContent(orgtype);
		
		String descr = null;
		for(Comment c : p.getMappInfo().getComments()) {
			if(COMMENT_DESCRIPTION.equals(c.getSource())) {
				if(!"".equals(c.getComment())) descr = c.getComment();
			}
		}
		if(descr != null) {
			Element description = new Element("description");
			description.setText(descr);
			biosystem.addContent(description);
		}
		
		addThumb(biosystem, p);
		
		Element url = new Element("url");
		url.setText(info.getUrl());
		biosystem.addContent(url);
		
		String taxid = org2taxid.get(info.getSpecies());
		if(taxid != null) {
			Element taxonomy = new Element("taxonomy");
			biosystem.addContent(taxonomy);
			Element taxnode = new Element("taxnode");
			taxonomy.addContent(taxnode);
			Element tid = new Element("taxid");
			tid.setText(taxid);
			taxnode.addContent(tid);
			Element taxonomyname = new Element("taxonomyname");
			taxonomyname.setText(info.getSpecies());
			taxnode.addContent(taxonomyname);
		}
		
		addGenes(biosystem, p);
		addMetabolites(biosystem, p);
		addCitations(biosystem, p);
		addLinkedSystems(biosystem, p);
	}
	
	private void addCitations(Element biosystem, Pathway p) {
		Set<PublicationXref> refs = new HashSet<PublicationXref>();
		for(BiopaxNode bpe : p.getBiopax().getElements()) {
			if(bpe instanceof PublicationXref) {
				refs.add((PublicationXref)bpe);
			}
		}
		if(refs.size() == 0) return;
		
		Element citations = new Element("citations");
		biosystem.addContent(citations);
		
		for(PublicationXref x : refs) {
			Element citation = new Element("citation");
			citations.addContent(citation);
			if(!"".equals(x.getPubmedId()) && x.getPubmedId() != null && x.getPubmedId().matches("^[1-9]{1}[0-9]*$")) {
				Element pmid = new Element("pmid");
				pmid.setText(x.getPubmedId());
				citation.addContent(pmid);
			} else {
				Element cit = new Element("textcitation");
				String txt = "";
				if(!"".equals(x.getAuthorString())) txt += x.getAuthorString() + ", ";
				if(!"".equals(x.getTitle())) txt += x.getTitle() + ". ";
				if(!"".equals(x.getSource())) txt += x.getSource() + " ";
				if(!"".equals(x.getYear())) txt += "(" + x.getYear() + ")";
				
				cit.setText(txt);
				citation.addContent(cit);
			}
		}
	}
	
	private void addLinkedSystems(Element biosystem, Pathway p) {
		Set<Xref> linkIds = new HashSet<Xref>();
		for(Xref x : p.getDataNodeXrefs()) {
			if(DataSource.getBySystemCode("Wp").equals(x.getDataSource())) {
				linkIds.add(x);
			}
		}
		
		if(linkIds.size() == 0) return;
		
		Element linkedsystems = new Element("linkedsystems");
		biosystem.addContent(linkedsystems);
		for(Xref x : linkIds) {
			Element ls = new Element("linkedsystem");
			linkedsystems.addContent(ls);
			Element extid = new Element("externalid");
			extid.setText(x.getId());
			ls.addContent(extid);
			Element type = new Element("linkedsystemtype");
			type.addContent(new Element("linked"));
			ls.addContent(type);
		}
	}
	
	private void addMetabolites(Element biosystem, Pathway p) throws IDMapperException {
		Map<Xref, PathwayElement> xrefs = gatherXrefs(p, MET_DS);
		if(xrefs.size() == 0) return;
		
		Element sms = new Element("smallmolecules");
		biosystem.addContent(sms);
		
		for(Xref x : xrefs.keySet()) {
			PathwayElement pwe = xrefs.get(x);
			Element sm = new Element("smallmolecule");
			sms.addContent(sm);
			Element extid = new Element("externalid");
			extid.setText(pwe.getXref() + "");
			sm.addContent(extid);
			Element name = new Element("name");
			name.setText(pwe.getTextLabel());
			sm.addContent(name);
			Element cid = new Element("cid");
			cid.setText(x.getId());
			sm.addContent(cid);
		}
	}
	
	private void addGenes(Element biosystem, Pathway p) throws IDMapperException {
		Map<Xref, PathwayElement> xrefs = gatherXrefs(p, GENE_DS);
		if(xrefs.size() == 0) return;
		
		Element genes = new Element("genes");
		biosystem.addContent(genes);
		
		for(Xref x : xrefs.keySet()) {
			PathwayElement pwe = xrefs.get(x);
			Element gene = new Element("gene");
			genes.addContent(gene);
			Element extid = new Element("externalid");
			extid.setText(pwe.getXref() + "");
			gene.addContent(extid);
			Element name = new Element("name");
			name.setText(pwe.getTextLabel());
			gene.addContent(name);
			Element entity = new Element("entity");
			gene.addContent(entity);
			Element geneid = new Element("geneid");
			geneid.setText(x.getId());
			entity.addContent(geneid);
		}
	}
	
	private Map<Xref, PathwayElement> gatherXrefs(Pathway p, DataSource ds) throws IDMapperException {
		Map<Xref, PathwayElement> xrefs = new HashMap<Xref, PathwayElement>();
		
		IDMapperStack stack = idmp.getStack(Organism.fromLatinName(p.getMappInfo().getOrganism()));
		
		for(PathwayElement pwe : p.getDataObjects()) {
			if(pwe.getObjectType() == ObjectType.DATANODE) {
				Xref x = pwe.getXref();
				if(x == null || x.getId() == null || "".equals(x.getId()) || x.getId().matches("^\\s+$")|| x.getDataSource() == null) continue;
				if(
						(MET_DS.equals(x.getDataSource()) || GENE_DS.equals(x.getDataSource())) 
						&& !x.getId().matches("^[1-9]{1}[0-9]*$")) continue; //Also check for sanity of Entrez identifiers
				for(Xref xx : stack.mapID(x, ds)) xrefs.put(xx, pwe);
				if(ds.equals(x.getDataSource())) xrefs.put(x, pwe);
			}
		}
		return xrefs;
	}
	
	private void addThumb(Element biosystem, Pathway p) throws IOException {
		VPathway vPathway = new VPathway(null);
		vPathway.fromModel(p);
		
		double vh = vPathway.getVHeight();
		double vw = vPathway.getVWidth();
		double zoom = 100;
		if(vh >= vw) zoom = (double)imgSize / vPathway.getVHeight();
		if(vw > vh) zoom = (double)imgSize / vPathway.getVWidth();
		vPathway.setPctZoom(zoom * 100);
		BufferedImage imgThumb = new BufferedImage(vPathway.getVWidth(), vPathway.getVHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = imgThumb.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		vPathway.draw(g);
		g.dispose();
		
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		ImageIO.write(imgThumb, "png", o);
		o.flush();
		byte[] thumbByte = o.toByteArray();
		o.close();
		
		String base64 = Base64.encode(thumbByte);
		Element thumb = new Element("thumbnail");
		biosystem.addContent(thumb);
		Element image = new Element("image");
		thumb.addContent(image);
		Element type = new Element("type");
		type.addContent(new Element("png"));
		image.addContent(type);
		Element enc = new Element("encodedimage");
		enc.setText(base64);
		image.addContent(enc);
	}
	
	private void addGeneralSection(Element root) {
		Element source = new Element("source");
		source.setText("WikiPathways");
		root.addContent(source);

		Element feedbackurl = new Element("feedbackurl");
		feedbackurl.setText(sourceUrl + "/index.php/Contact_Us");
		root.addContent(feedbackurl);
		
		Element sourceurl = new Element("sourceurl");
		sourceurl.setText(sourceUrl);
		root.addContent(sourceurl);
		
		Element citations = new Element("citations");
		root.addContent(citations);
		Element citation = new Element("citation");
		citations.addContent(citation);
		Element pmid = new Element("pmid");
		pmid.setText("18651794");
		citation.addContent(pmid);
		citation = new Element("citation");
		citations.addContent(citation);
		Element textcitation = new Element("textcitation");
		textcitation.setText("Pico AR, Kelder T, van Iersel MP, Hanspers K, Conklin BR, and C.T.A. Evelo (2008) WikiPathways: Pathway Editing for the People. PLoS Biol 6(7): e184. doi:10.1371/journal.pbio.0060184");
		citation.addContent(textcitation);
	}
}

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

package org.pathvisio.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
contains information about a certain DataSource, such as

It's full name ("Ensembl")
It's system code ("En")
It's main url ("http://www.ensembl.org")
Id-specific url's ("http://www.ensembl.org/Homo_sapiens/searchview?species=all&idx=Gene&q=" + id)

The DataSource class uses the Extenisble enum pattern.
You can't instantiate DataSources directly, instead you have to use one of
the constants such as DataSource.ENSEMBL, or 
the "getBySystemcode" or "getByFullname" methods.
These methods return a predefined DataSource object if it exists.
If a predefined DataSource for a requested SystemCode doesn't exists,
a new one springs to life automatically. This can be used 
when the user requests new, unknown data sources. If you call
getBySystemCode twice with the same argument, it is guaranteed
that you get the same return object. However, there is no way
to combine a new DataSource with a new FullName unless you use 
the "register" method.

This way any number of pre-defined DataSources can be used, 
but plugins can define new ones and you can
handle unknown systemcodes that occur in Gpml in the same 
way as predefined ones.

PathVisio should never have to refer to system codes as Strings, except
- in low level SQL code dealing with Gdb's, Gex and MAPP's (MAPPFormat.java)
- in low level GPML code (GPMLFormat.java)

The preferred way to refer to a specific database is using a 
constant defined here, e.g. "DataSource.ENSEMBL"
*/
public class DataSource
{
	private static Map<String, DataSource> bySysCode = new HashMap<String, DataSource>();
	private static Map<String, DataSource> byFullName = new HashMap<String, DataSource>();
	private static Set<DataSource> registry = new HashSet<DataSource>();
	
	public static final DataSource TAIR = new DataSource (
		 "A", "TAIR",
		 null, "http://www.arabidopsis.org/");
	public static final DataSource AGILENT = new DataSource (
		"Ag", "Agilent", 
		null, null);
	public static final DataSource CINT = new DataSource (
		"C", "Cint", 
		null, null);
	public static final DataSource CAS = new DataSource (
		"Ca", "CAS", 
		new PrefixUrlMaker ("http://chem.sis.nlm.nih.gov/chemidplus/direct.jsp?regno="),
		null);
	public static final DataSource CHEBI = new DataSource (
		"Ce", "ChEBI", 
		new PrefixUrlMaker ("http://www.ebi.ac.uk/chebi/searchId=CHEBI:"),
		null);
	public static final DataSource HMDB = new DataSource (
		"Ch", "HMDB", 
		new PrefixUrlMaker ("http://www.hmdb.ca/scripts/show_card.cgi?METABOCARD="),
		null);
	public static final DataSource PUBCHEM = new DataSource (
		"Cp", "PubChem", 
		new PrefixUrlMaker ("http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid="),
		null);
	public static final DataSource KEGG_COMPOUND = new DataSource (
		"Ck", "Kegg Compound", 
		new PrefixUrlMaker ("http://www.genome.jp/dbget-bin/www_bget?cpd:"),
		null);
	public static final DataSource SGD = new DataSource (
		"D", "SGD", 
		new PrefixUrlMaker ("http://db.yeastgenome.org/cgi-bin/locus.pl?locus="),
		null);
	public static final DataSource ECOLI = new DataSource (
		"Ec", "Ecoli", 
		null, null);
	public static final DataSource EMBL = new DataSource (
		"Em", "EMBL", 
		new PrefixUrlMaker ("http://www.ebi.ac.uk/cgi-bin/emblfetch?style=html&id="), 
		"http://www.ebi.ac.uk/embl");
	public static final DataSource ENSEMBL = new DataSource (
		"En", "Ensembl", 
		new PrefixUrlMaker("http://www.ensembl.org/Homo_sapiens/searchview?species=all&idx=Gene&q="), 
		"http://www.ensembl.org");
	public static final DataSource FLYBASE = new DataSource (
		"F", "FlyBase", 
		null, null);
	public static final DataSource GENBANK = new DataSource (
		"G", "GenBank", 
		null, null);
	public static final DataSource CODELINK = new DataSource (
		"Ge", "CodeLink", 
		null, null);	
	public static final DataSource GRAMENE_GENES_DB = new DataSource (
		"Gg", "Gramene Genes DB", 
		null, "http://www.gramene.org/");	
	public static final DataSource GRAMENE_PATHWAY = new DataSource (
		"Gp", "Gramene Pathway", 
		null, "http://www.gramene.org/pathway");	
	public static final DataSource HUGO = new DataSource (
		"H", "HUGO", 
		new PrefixUrlMaker ("http://www.gene.ucl.ac.uk/cgi-bin/nomenclature/get_data.pl?hgnc_id="),
		"http://www.gene.ucl.ac.uk/nomenclature");
	public static final DataSource HSGENE = new DataSource (
		"Hs", "HsGene", 
		null, null);
	public static final DataSource INTERPRO = new DataSource (
		"I", "InterPro", 
		new PrefixUrlMaker ("http://www.ebi.ac.uk/interpro/IEntry?ac="),
		"http://www.ebi.ac.uk/interpro");
	public static final DataSource ILLUMINA = new DataSource (
		"Il", "Illumina", 
		null, null);
	public static final DataSource IPI = new DataSource (
		"Ip", "IPI", 
		null, null);
	public static final DataSource IRGSP_GENE = new DataSource (
		"Ir", "IRGSP Gene", 
		null, "http://rgp.dna.affrc.go.jp/IRGSP/");
	public static final DataSource ENTREZ_GENE = new DataSource (
		"L", "Entrez Gene", 
		new PrefixUrlMaker ("http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=Retrieve&dopt=full_report&list_uids="),
		null);
	public static final DataSource MGI = new DataSource (
		"M", "MGI", 
		new PrefixUrlMaker ("http://www.informatics.jax.org/searches/accession_report.cgi?id="),
		"http://www.informatics.jax.org/");
	public static final DataSource MIRBASE = new DataSource (
		"Mb", "miRBase", 
		null,
		null);
	public static final DataSource NASC_GENE = new DataSource (
		"N", "NASC Gene", 
		null,
		null);
	public static final DataSource NUGOWIKI = new DataSource (
		"Nw", "NuGO wiki", 
		new PrefixUrlMaker ("http://nugowiki.org/index.php/"),
		null);
	public static final DataSource OTHER = new DataSource (
		"O", "Other", 
		null, null);
	public static final DataSource OMIM = new DataSource (
		"Om", "OMIM", 
		new PrefixUrlMaker ("http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=OMIM&cmd=Search&doptcmdl=Detailed&term=?"),
		null);
	public static final DataSource PDB = new DataSource (
		"Pd", "PDB", 
		new PrefixUrlMaker ("http://bip.weizmann.ac.il/oca-bin/ocashort?id="),
		"http://www.rcsb.org/pdb/home/home.do");
	public static final DataSource PFAM = new DataSource (
		"Pf", "Pfam", 
		new PrefixUrlMaker ("http://www.sanger.ac.uk//cgi-bin/Pfam/getacc?"),
		"http://www.sanger.ac.uk/Software/Pfam");
	public static final DataSource PLANTGDB = new DataSource (
		"Pl", "PlantGDB", null, "http://www.plantgdb.org/");
	public static final DataSource REFSEQ = new DataSource (
		"Q", "RefSeq", 
		new UrlMaker() 
		{ 
			String pre = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?";
			public String getUrl (String id)
			{
				if(id.startsWith("NM")) 
				{
					return pre + "db=Nucleotide&cmd=Search&term=" + id;
				} 
				else 
				{
					return pre + "db=Protein&cmd=search&term=" + id;
				}
			}
		}
		, "http://www.ncbi.nlm.nih.gov/RefSeq");
	public static final DataSource RGD = new DataSource (
		"R", "RGD", 
		new PrefixUrlMaker ("http://rgd.mcw.edu/generalSearch/RgdSearch.jsp?quickSearch=1&searchKeyword="), 
		"http://rgd.mcw.edu/");
	public static final DataSource RFAM = new DataSource (
		"Rf", "Rfam", 
		null, null);
	public static final DataSource UNIPROT = new DataSource (
		"S", "Uniprot/TrEMBL", 
		new PrefixUrlMaker ("http://www.expasy.org/uniprot/"),
		"http://www.expasy.uniprot.org/");
	public static final DataSource SNP = new DataSource (
		"Sn", "dbSNP", 
		null, null);
	public static final DataSource GENE_ONTOLOGY = new DataSource (
		"T", "GeneOntology", 
		new PrefixUrlMaker ("http://godatabase.org/cgi-bin/go.cgi?view=details&search_constraint=terms&depth=0&query="), 
		"http://www.geneontology.org/");
	public static final DataSource TIGR = new DataSource (
		"Ti", "J. Craig Venter Institute (formerly TIGR)", 
		null, "http://www.jcvi.org/");
	public static final DataSource UNIGENE = new DataSource (
		"U", "UniGene", 
		new UrlMaker () 
		{
			public String getUrl(String id)
			{
				String [] org_nr = id.split("\\.");
				if(org_nr.length == 2) {
					return "http://www.ncbi.nlm.nih.gov/UniGene/clust.cgi?ORG=" + 
					org_nr[0] + "&CID=" + org_nr[1];
				}
				else {
					return null;
				}
			}
		}, null);
	public static final DataSource UCSC = new DataSource (
		"Uc", "UCSC Genome Browser", 
		null, "http://genome.ucsc.edu/");
	public static final DataSource WORMBASE = new DataSource (
		"W", "WormBase", 
		new PrefixUrlMaker ("http://www.wormbase.org/db/gene/gene?name="), 
		"http://www.wormbase.org");
	public static final DataSource AFFY = new DataSource (
		"X", "Affy", 
		new PrefixUrlMaker ("http://www.ensembl.org/Homo_sapiens/featureview?type=OligoProbe;id="), 
		null);
	public static final DataSource ZFIN = new DataSource (
		"Z", "ZFIN", 
		null, null);

	private String sysCode = null;
	private String fullName = null;
	private String mainUrl = null;
	private UrlMaker urlMaker = null;
	
	/**
	 * Constructor is private, so that we don't
	 * get any standalone DataSources.
	 * That way we can make sure that two DataSources
	 * pointing to the same datbase are really the same.
	 */
	private DataSource (String sysCode, String fullName, UrlMaker urlMaker, String mainUrl)
	{
		this.sysCode = sysCode;
		this.fullName = fullName;
		this.mainUrl = mainUrl;
		this.urlMaker = urlMaker;
		
		registry.add (this);
		if (sysCode != null || "".equals(sysCode))
			bySysCode.put(sysCode, this);
		if (fullName != null || "".equals(fullName));
			byFullName.put(fullName, this);
	}
	
	/** turn id into url pointing to info page on the web, e.g. "http://www.ensembl.org/get?id=ENSG..." */
	public String getUrl(String id)
	{
		if (urlMaker != null)
			return urlMaker.getUrl(id); 
		else 
			return null;
	}
				
	/** 
	 * returns full name of datasource e.g. "Ensembl". 
	 * May return null if only the system code is known. 
	 * Also used as identifier in GPML 
	 */
	public String getFullName()
	{
		return fullName;
	}
	
	/** 
	 * returns GenMAPP SystemCode, e.g. "En". May return null,
	 * if only the full name is known.
	 * Also used as identifier in 
	 * 1. Gdb databases, 
	 * 2. Gex databases.
	 * 3. Imported data
	 * 4. the Mapp format. 
	 * We should try not to use the system code anywhere outside
	 * these 4 uses.
	 */
	public String getSystemCode()
	{
		return sysCode;
	}
	
	/**
	 * Return the main Url for this datasource,
	 * that can be used to refer to the datasource in general.
	 * (e.g. http://www.ensembl.org/)
	 * 
	 * May return null in case the main url is unknown.
	 */
	public String getMainUrl()
	{	
		return mainUrl;
	}
	
	/** 
	 * so new system codes can be added easily by 
	 * plugins. url and urlMaker may be null 
	 */
	public static void register(String sysCode, String fullName, UrlMaker urlMaker, String mainUrl)
	{
		new DataSource (sysCode, fullName, urlMaker, mainUrl);
	}
	
	/** 
	 * returns pre-existing 
	 * DataSource object by system code, if it exists, or creates a new one 
	 */
	public static DataSource getBySystemCode(String systemCode)
	{
		if (!bySysCode.containsKey(systemCode))
		{
			register (systemCode, null, null, null);
		}
		return bySysCode.get(systemCode);
	}
	
	/** 
	 * returns pre-existing DataSource object by 
	 * full name, if it exists, 
	 * or creates a new one 
	 */
	public static DataSource getByFullName(String fullName)
	{
		if (!byFullName.containsKey(fullName))
		{
			register (null, fullName, null, null);
		}
		return byFullName.get(fullName);
	}
	
	/**
		get all registered datasoures as a set
	*/ 
	static public Set<DataSource> getDataSources()
	{
		return registry;
	}
	
	/**
	 * Get a list of all non-null full names.
	 * 
	 * Warning: the ordering of this list is undefined.
	 * Two subsequent calls may give different results.
	 */
	static public List<String> getFullNames()
	{
		List<String> result = new ArrayList<String>();
		result.addAll (byFullName.keySet());
		return result;
	}
	/**
	 * The string representation of a DataSource is equal to
	 * it's full name. (e.g. "Ensembl")
	 */
	public String toString()
	{
		return fullName;
	}
	
	/** an UrlMaker knows how to turn an id into an Url string */
	public static abstract class UrlMaker
	{
		public abstract String getUrl(String id);
	}
	
	/** Implements most common way an Url is made: add Id to a prefix */ 
	public static class PrefixUrlMaker extends UrlMaker
	{
		String prefix;
		public PrefixUrlMaker(String prefix)
		{
			this.prefix = prefix;
		}
		
		@Override
		public String getUrl(String id) 
		{
			return prefix + id;
		}
	}	
}

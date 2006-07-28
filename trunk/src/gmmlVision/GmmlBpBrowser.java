package gmmlVision;
import graphics.GmmlGeneProduct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Backpage browser - side panel that shows the backpage information when a GeneProduct is double-clicked
 */
public class GmmlBpBrowser extends Composite {
	/**
	 * Directory containing HTML files needed to display the backpage information
	 */
	final static String BPDIR = "backpage";
	/**
	 * Header file, containing style information
	 */
	final static String HEADERFILE = "header.html";
	
	/**
	 * Header for the gene information in HTML format
	 */
	final static String bpHeader = "<H1>Gene information</H1><P>";
	/**
	 * Header for the expression information in HTML format
	 */
	final static String gexHeader = "<H1>Expression data</H1><P>";
	
	private String bpText;
	private String gexText;
	private String header;
	
	private GmmlVision gmmlVision;
	private Browser bpBrowser;
	
	private GmmlGeneProduct geneProduct;
	
	/**
	 * Constructor for this class
	 * @param parent	Parent {@link Composite} for the {@Browser} widget
	 * @param style		Style for the {@Browser} widget
	 */
	public GmmlBpBrowser(Composite parent, int style, GmmlVision gmmlVision) {
		super(parent, style);
		this.gmmlVision = gmmlVision;
		
		initializeHeader(); //Load the header including style information
		setLayout(new FillLayout());
		bpBrowser = new Browser(this, style); //Set the Browser widget
		setGeneText(null);
		setGexText(null);
	}
	
	public void setGene(GmmlGeneProduct gp) 
	{ 
		geneProduct = gp;
		if(gmmlVision.gmmlGdb == null || gmmlVision.gmmlGex == null) return;
		if(gp == null) {
			setGeneText(null);
			setGexText(null);
			return;
		}
		// Get the backpage text
		String geneId = geneProduct.getId();
		String systemCode = geneProduct.getSystemCode();
		String bpText = gmmlVision.gmmlGdb.getBpInfo(geneId, systemCode);
		String gexText = gmmlVision.gmmlGex.getDataString(geneId, systemCode);
		if (bpText != null) 	setGeneText(bpText);
		else 					setGeneText("<I>No gene information found</I>");
		if (gexText != null)	setGexText(gexText);
		else 					setGexText("<I>No expression data found</I>");
	}
	
	/**
	 * Sets the text for the Gene information part of the browser. Will be prepended by a paragraph
	 * header as defined in {@link bpHeader}
	 * @param bpText	Text to display in HTML format
	 */
	public void setGeneText(String bpText) {
		if(bpText == null) { //In case no information has to be displayed
			this.bpText = bpHeader + "<I>No gene selected</I>";
		} else {
			this.bpText = bpHeader + bpText;
		}
		refresh();
	}
	
	/**
	 * Sets the text for the expression part of the browser. Will be prepended by a paragraph
	 * header as defined in {@link gexHeader}
	 * @param gexText	Text to display in HTML format
	 */
	public void setGexText(String gexText) {
		if(gexText != null) { //In case no information has to be displayed
			this.gexText = gexHeader + gexText;
		} else {
			this.gexText = "";
		}
		refresh();
	}
	
	/**
	 * Refreshes the text displayed in the browser
	 */
	public void refresh() {
		bpBrowser.setText(header + bpText + gexText);
	}
	
	/**
	 * Reads the header of the HTML content displayed in the browser. This header is displayed in the
	 * file specified in the {@link HEADERFILE} field
	 */
	private void initializeHeader() {
		try {
			File headerFile = new File(BPDIR,HEADERFILE);
			BufferedReader input = new BufferedReader(new FileReader(headerFile));
			String line;
			header = "";
			while((line = input.readLine()) != null) {
				header += line.trim();
			}
		} catch (Exception e) {
			GmmlVision.log.error("Unable to read header file for backpage browser: " + e.getMessage(), e);
		}
	}
}

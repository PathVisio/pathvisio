package gmmlVision;
import java.io.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Backpage browser - side panel that shows the backpage information for a selected gene.
 */
public class GmmlBpBrowser extends Composite {
	final static String BPDIR = "backpage";
	final static String HEADERFILE = "header.html";
	
	final static String bpHeader = "<H1>Gene information</H1><P>";
	final static String gexHeader = "<H1>Expression data</H1><P>";
	
	public String noGeneUrl;
	public String header;
	
	private String bpText;
	private String gexText;
	
	public Browser bpBrowser;
	
	public GmmlBpBrowser(Composite parent, int style) {
		super(parent, style);
		initializeHeader();
		this.setLayout(new FillLayout());
		bpBrowser = new Browser(this, style);
		setBpText(null);
		setGexText(null);
	}
	
	public void setBpText(String bpText) {
		if(bpText == null) {
			this.bpText = bpHeader + "<I>No gene selected</I>";
		} else {
			this.bpText = bpHeader + bpText;
		}
		refresh();
	}
	
	public void setGexText(String gexText) {
		if(gexText != null) {
			this.gexText = gexHeader + gexText;
		} else {
			this.gexText = "";
		}
		refresh();
	}
	
	public void refresh() {
		bpBrowser.setText(header + bpText + gexText);
	}
	
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
			e.printStackTrace();
		}
	}
}

package org.wikipathways.maintenance;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.wikipathways.WikiPathwaysClient;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;


public class InferatorLabelFix {
	public static void main(String[] args) {
		String url = args[0];
		String user = args[1];
		String pass = args[2];

		try {
			Logger.log.setLogLevel(false, false, false, true, true, true);
			WikiPathwaysClient client = new WikiPathwaysClient(new URL(url));

			client.login(user, pass);

			Pattern pat = Pattern.compile("&amp;(.+?;)");

			for(WSPathwayInfo pwi : client.listPathways()) {
				WSPathway wsp = client.getPathway(pwi.getId());
				Pathway p = WikiPathwaysClient.toPathway(wsp);

				boolean modified = false;
				for(PathwayElement pwe : p.getDataObjects()) {
					if(pwe.getObjectType() == ObjectType.LABEL) {
						String txt = pwe.getTextLabel();
						Matcher m = pat.matcher(txt);
						while(m.find()) {
							String q = m.group();
							String n = m.group(1);
							txt = txt.replaceAll(q, "&" + n);
						}
						if(!txt.equals(pwe.getTextLabel())) {
							//Unescape to prevent the xml encoder from double-escaping
							txt = StringEscapeUtils.unescapeXml(txt);
							System.out.println(pwe.getTextLabel());
							pwe.setTextLabel(txt);
							System.out.println(pwe.getTextLabel());
							System.out.println("------");
							modified = true;
						}
					}
				}
				if(modified) {
					System.out.println("Updating pathway " + pwi.getId());
					client.updatePathway(pwi.getId(), p, "Fixed text labels", Integer.parseInt(pwi.getRevision()));
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


}

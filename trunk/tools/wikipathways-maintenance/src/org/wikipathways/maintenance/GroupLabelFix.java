package org.wikipathways.maintenance;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;
import org.wikipathways.client.WikiPathwaysClient;

/**
 * Removes the "New group" labels that occur on many
 * pathways on wikipathways, but add no useful information on the group.
 * @author thomas
 */
public class GroupLabelFix {
	public static void main(String[] args) {
		String url = args[0];
		String user = args[1];
		String pass = args[2];

		try {
			Logger.log.setLogLevel(true, true, true, true, true, true);
			WikiPathwaysClient client = new WikiPathwaysClient(new URL(url));

			client.login(user, pass);

			Pattern pat = Pattern.compile("(Group object)|(new group)");

			WSPathwayInfo[] pathways = client.listPathways();
			int i = 1;
			for(WSPathwayInfo pwi : pathways) {
				Logger.log.trace("Processing pathway " + i++ + " out of " + pathways.length);

				WSPathway wsp = client.getPathway(pwi.getId());

				Pathway p = WikiPathwaysClient.toPathway(wsp);

				boolean modified = false;
				for(PathwayElement pwe : p.getDataObjects()) {
					if(pwe.getObjectType() == ObjectType.GROUP) {
						String txt = pwe.getTextLabel();
						Matcher m = pat.matcher(txt);
						if(m.matches()) {
							Logger.log.info("Found match: " + txt);
							pwe.setTextLabel("");
							modified = true;
						}
					}
				}
				if(modified) {
					System.out.println("Updating pathway " + pwi.getId());
					client.updatePathway(pwi.getId(), p, "Removed group label", Integer.parseInt(pwi.getRevision()));
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

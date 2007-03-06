package data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class GraphLink {
	public interface GraphIdContainer {
		String getGraphId();
		void setGraphId(String id);
		Set<GraphRefContainer> getStickyPoints();
	}
	
	public interface GraphRefContainer {
		String getGraphRef();
		void setGraphRef(String ref);
		void moveBy(double dx, double dy);
	}
	
	public static void moveRefsBy(GraphIdContainer idc, double dx, double dy) {
		for(GraphRefContainer refc : idc.getStickyPoints()) {
			refc.moveBy(dx, dy);
		}
	}
	
	protected static void setGraphId(String v, GraphIdContainer c, GmmlDataObject gd) {
		GmmlData data = gd.getParent();
		String graphId = c.getGraphId();
		if (graphId == null || !graphId.equals(v))
		{
			if (data != null)
			{
				if (graphId != null)
				{
					data.removeGraphId(v);
				}
				if (v != null)
				{
					data.addGraphId(v);
				}
			}
		}
	}
	
	public static Set<GraphRefContainer> getStickyPoints(GraphIdContainer gid, GmmlData gd) {
		Set<GraphRefContainer> result = 
			new HashSet<GraphRefContainer>();

		if (gd == null) return result;
		
		List<GraphRefContainer> reflist = gd.getReferringObjects(gid.getGraphId());
		
		if (reflist != null && !gid.getGraphId().equals("")) 
		{
			// get all referring points as a hashset, so
			// that a line that refers to the same object twice
			// is only treated once.
			for (GraphRefContainer o : reflist)
			{
				result.add(o);
			}
		}
		return result;
	}
}


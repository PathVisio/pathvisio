package org.pathvisio.data;

import java.util.Collection;
import java.util.Map;

import org.bridgedb.Xref;

public interface IRow extends Comparable<IRow>
{
	Xref getXref();
	Object getSampleData(ISample iSample);
	Map<String, Object> getByName();
	Collection<? extends ISample> getSamples();
	int getGroup();
}

package data;

import junit.framework.TestCase;

public class Test extends TestCase {

	public void testObjectType()
	{
		GmmlDataObject o = new GmmlDataObject(ObjectType.GENEPRODUCT);
		
		assertEquals ("getObjectType() test", o.getObjectType(), ObjectType.GENEPRODUCT);
	}
	
	public void testParent()
	{
		GmmlData data = new GmmlData();
		GmmlDataObject o = new GmmlDataObject(ObjectType.GENEPRODUCT);
		
		o.setParent(data);
		assertTrue ("Setting parent adds to container", data.getDataObjects().contains(o));
		
		o.setParent(null);
		assertFalse ("Setting parent null removes from container", data.getDataObjects().contains(o));
	}
}

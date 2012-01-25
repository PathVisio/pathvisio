package org.pathvisio.core.model;

import junit.framework.TestCase;

public class TestMGroup extends TestCase
{
	/**
	 * Check that when a line points to the group, it stays at the same position when the group disappears.
	 * Test for regression of bug #1058
	 */
	public void testUngroup()
	{
		Pathway pwy = new Pathway();
		MLine line = (MLine)PathwayElement.createPathwayElement(ObjectType.LINE);
		MGroup group = (MGroup)PathwayElement.createPathwayElement(ObjectType.GROUP);
		PathwayElement node = PathwayElement.createPathwayElement(ObjectType.DATANODE);
		pwy.add(line);
		pwy.add(node);
		pwy.add(group);
		
		node.setGeneratedGraphId();
		line.setGeneratedGraphId();
		
		group.createGroupId();
		assertNotNull (group.getGroupId());
		
		node.setMCenterX(120);
		node.setMCenterY(20);
		node.setMWidth(20);
		node.setMHeight(20);
		
		assertEquals (0, group.getGroupElements().size());
		node.setGroupRef(group.getGroupId());
		
		assertEquals (1, group.getGroupElements().size());
		assertTrue (group.getGroupElements().contains(node));
		
		line.setMEndX(group.getMCenterX());
		line.setMEndY(group.getMTop());
		line.setEndGraphRef(group.getGraphId());
		
		assertEquals (8.0, group.getGroupStyle().getMMargin());
		
		assertEquals (120.0, line.getMEndX(), 0.01);
		assertEquals (2.0, line.getMEndY(), 0.01);
		
		// ungroup
		pwy.remove(group);
		
		// check that line points at same position
		assertEquals (120.0, line.getMEndX());
		assertEquals (2.0, line.getMEndX());
	}
}

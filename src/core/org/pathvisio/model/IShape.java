package org.pathvisio.model;

public interface IShape
{
	public String getMappName();
	public String getName();
	public boolean isResizeable();
	public boolean isRotatable();
	public java.awt.Shape getShape(double x, double y, double w, double h);
}

/*
 * Copied from http://www.koders.com/java/fid7E9F096AF4B92647A91B0C00B2DC70377B296FFB.aspx
 * No licence information?
 */
package org.pathvisio.gui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class WrapLayout implements LayoutManager {

    protected int hgap;
    protected int vgap;

    public WrapLayout() {
        this(8, 8);
    }

    public WrapLayout(int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
    }

    public void addLayoutComponent(String name, Component c) { }

    public void layoutContainer(Container parent) {
        int width = parent.getWidth();
        int x = 0;
        int y = 0;
        int current_height = 0;
        for ( Component c : parent.getComponents() ) {
            Dimension d = c.getPreferredSize();
            if ( ( hgap + x + d.width + hgap > width ) && ( x > 0 ) ) {
                y += current_height + vgap;
                x = 0;
                current_height = 0;
            }
            if ( d.height > current_height ) {
                current_height = d.height;
            }
            c.setBounds(hgap + x, vgap + y, d.width, d.height);
            x += d.width + hgap;
        }
    }

    public Dimension minimumLayoutSize(Container parent) {
        Dimension result = new Dimension();
        result.width = 0;
        result.height = 0;
        for ( Component c : parent.getComponents() ) {
            Dimension d = c.getPreferredSize();
            if ( d.width > result.width ) {
                result.width = d.width;
            }
            if ( d.height > result.height ) {
                result.height = d.height;
            }
        }
        result.width = result.width + hgap * 2;
        result.height = result.height + vgap * 2;
        return result;
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension result = new Dimension();
        result.width = parent.getWidth();
        result.height = vgap;
        int x = 0;
        int current_height = 0;
        for ( Component c : parent.getComponents() ) {
            Dimension d = c.getPreferredSize();
            if ( ( hgap + x + d.width + hgap > result.width ) && ( x > 0 ) ) {
                result.height += current_height + vgap;
                x = 0;
                current_height = 0;
            }
            if ( d.height > current_height ) {
                current_height = d.height;
            }
            x += d.width + hgap;
        }
        if ( current_height > 0 ) {
            result.height += current_height + vgap;
        }
        result.width += hgap;
        return result;
    }

    public void removeLayoutComponent(Component comp) { }

    public int getHgap() {
        return hgap;
    }

    public int getVgap() {
        return vgap;
    }

    public void setHgap(int hgap) {
        this.hgap = hgap;
    }

    public void setVgap(int vgap) {
        this.vgap = vgap;
    }

}

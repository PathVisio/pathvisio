// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
/*
 * Copied from http://www.koders.com/java/fid7E9F096AF4B92647A91B0C00B2DC70377B296FFB.aspx
 * No licence information?
 */
package org.pathvisio.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

/**
 * Special Layout specifically for {@Link JToolbars},
 * If there isn't enough space, the buttons will be wrapped across several rows.
 */
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
        int currentHeight = 0;
        for ( Component c : parent.getComponents() ) {
            Dimension d = c.getPreferredSize();
            if ( ( hgap + x + d.width + hgap > width ) && ( x > 0 ) ) {
                y += currentHeight + vgap;
                x = 0;
                currentHeight = 0;
            }
            if ( d.height > currentHeight ) {
                currentHeight = d.height;
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
        int currentHeight = 0;
        for ( Component c : parent.getComponents() ) {
            Dimension d = c.getPreferredSize();
            if ( ( hgap + x + d.width + hgap > result.width ) && ( x > 0 ) ) {
                result.height += currentHeight + vgap;
                x = 0;
                currentHeight = 0;
            }
            if ( d.height > currentHeight ) {
                currentHeight = d.height;
            }
            x += d.width + hgap;
        }
        if ( currentHeight > 0 ) {
            result.height += currentHeight + vgap;
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

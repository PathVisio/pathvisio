// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 PathVisio contributors (for a complete list, see CONTRIBUTORS.txt)
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
/*****************************************
/*
/*   This class is written by Hakim
/*   It is a wat to keep track of a treedata
/*
/*****************************************/

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

public class TreeList {
    //Constructor
    //Create the empty list with 1 in length
    Object[] tree = new Object[1];
    int[] levels = new int[1];
    
    public static void main(String[] args) {
        //Main can be empty        
    }
    
    //Change the title, it is always the first value
    public void SetTitle(String title) {
        tree[0] = title;
        levels[0] = 1;
    }
    
    public void AddValue(String value, int level) {
        int newpos = java.lang.reflect.Array.getLength(tree);
        tree = (Object[])resizeArray(tree, newpos+1);
        levels = (int[])resizeArray(levels, newpos+1);

        tree[newpos] = value;
        levels[newpos] = level;
    }
    
    public Object[] GetTree() {
        int newpos;
        Object[] formattree = new Object[1];
        formattree[0] = tree[0]; //Set the title, this is always standard level
        int k=0; //The index of the formatted array
        for(int i = 1; i < tree.length; i++) {
            if (levels[i]==1 && tree[i] != "return") {
                newpos = formattree.length;
                formattree = (Object[])resizeArray(formattree, newpos+1);
                formattree[newpos] = tree[i];
            } else if (levels[i-1]==1 && levels[i]== 2) {
                newpos = formattree.length;
                formattree = (Object[])resizeArray(formattree, newpos+1);
                formattree[newpos] = SublevelGen(i, 2);
            }
       }
       	 
       return formattree;
    }
    
    private Object[] SublevelGen(int start, int level) {
        Object[] sub = new Object[0];
        int newpos;
        for(int i = start; i < tree.length; i++) {
             if (levels[i] < level) {
                 break;
             } else if (levels[i]==level && tree[i] != "return") {
                newpos = sub.length;
                sub = (Object[])resizeArray(sub, newpos+1);
                sub[newpos] = tree[i];
            } else if (levels[i-1]==level && levels[i] > level) {
                newpos = sub.length;
                sub = (Object[])resizeArray(sub, newpos+1);
                sub[newpos] = SublevelGen(i, level+1);
            }      
        } 
        
        return sub;
    }
    /**
    * Reallocates an array with a new size, and copies the contents
    * of the old array to the new array.
    * @param oldArray  the old array, to be reallocated.
    * @param newSize   the new array size.
    * @return          A new array with the same contents.
    */
    private static Object resizeArray (Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(
              elementType,newSize);
        int preserveLength = Math.min(oldSize,newSize);
        if (preserveLength > 0)
            System.arraycopy (oldArray,0,newArray,0,preserveLength);
            
        return newArray; 
    }
    
}

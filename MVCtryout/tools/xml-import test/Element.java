import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

public class Element {

	    //Constructor
    //Create the empty list with 1 in length
    private Object[] element = new Object[1];
    
    public static void main(String[] args) {
        //Main can be empty        
    }
    
    public void AddValue(String value) {
        int newpos = java.lang.reflect.Array.getLength(tree);
        element = (Object[])resizeArray(element , newpos+1);
        tree[newpos] = value;
    }
    
    public Object[] GetElement() {
        return element;
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
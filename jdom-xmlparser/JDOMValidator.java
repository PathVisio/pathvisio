import org.jdom.JDOMException;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.*;
import org.jdom.Element;
import org.jdom.Attribute;
import java.io.IOException;
import java.util.*;
import org.apache.xerces.parsers.SAXParser;


public class JDOMValidator {

  public static void main(String[] args) {
  
    if (args.length == 0) {
      args = (String[])resizeArray(args,1);
      args[0] = "Hs_G13_Signaling_Pathway.xml";

      System.out.println("Usage: java JDOMValidator URL, filename set to "+args[0]); 
//      return;
    } 
      
    SAXBuilder builder = new SAXBuilder(false);
                                    //  ^^^^
                                    // Turn on validation
      
    // command line should offer URIs or file names
    try {
      Document doc = builder.build(args[0]);
      // If there are no well-formedness or validity errors, 
      // then no exception is thrown.
      System.out.println(args[0] + " is not validated.");
      listNodes(doc, 0);
    }
    // indicates a well-formedness or validity error
    catch (JDOMException e) { 
      System.out.println(args[0] + " is not valid.");
      System.out.println(e.getMessage());
    }  
    catch (IOException e) { 
      System.out.println("Could not check " + args[0]);
      System.out.println(" because " + e.getMessage());
    }  
  
  }

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
    
  public static void listNodes(Object o, int depth) {
   
    printSpaces(depth);
    
    if (o instanceof Element) {
      Element element = (Element) o;
      System.out.println("Element: " + element.getName());
      List attributes = element.getAttributes();
      Iterator aiterator = attributes.iterator();
      while (aiterator.hasNext()) {
        Object att = aiterator.next();
        if (att instanceof Attribute) {
	       Attribute attribute = (Attribute) att;
	       printSpaces(depth+1);
          System.out.println("Attribute name: "+attribute.getName()+ "value : "+attribute.getValue());
        }
      }
      List children = element.getContent();
      Iterator iterator = children.iterator();
      while (iterator.hasNext()) {
        Object child = iterator.next();
        listNodes(child, depth+1);
      }
    }
    else if (o instanceof Document) {
      System.out.println("Document");
      Document doc = (Document) o;
      List children = doc.getContent();
      Iterator iterator = children.iterator();
      while (iterator.hasNext()) {
        Object child = iterator.next();
        listNodes(child, depth+1);
      }
    }
    else if (o instanceof Comment) {
      System.out.println("Comment");
    }
    else if (o instanceof CDATA) {
      System.out.println("CDATA section");
      // CDATA is a subclass of Text so this test must come
      // before the test for Text.
    }
    else if (o instanceof Text) {
      System.out.println("Text");
    }
    else if (o instanceof EntityRef) {
      System.out.println("Entity reference");
    }
    else if (o instanceof ProcessingInstruction) {
      System.out.println("Processing Instruction");
    }
    else {  // This really shouldn't happen
      System.out.println("Unexpected type: " + o.getClass());
    }
    
  }
  
  private static void printSpaces(int n) {
    
    for (int i = 0; i < n; i++) {
      System.out.print(' '); 
    }
    
  }
}
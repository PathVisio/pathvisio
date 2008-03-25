// import the things needed to run this java file.
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;


public class LinkChecker {


	/**
	 * @param args
	 * @throws ConverterException 
	 */
	public static void main(String[] args) throws ConverterException {
		// load the filename; the filename is declaired in the run dialog window
		String filename = args[0];
		
		// load the file
		File file = new File(filename);

		// load the pathway
		Pathway pway = new Pathway();
		boolean validate = true;
		pway.readFromXml(file, validate);
		
		List<Xref> xRefList = makeXrefList(pway);
		System.out.println("size of the xRefList: "+xRefList.size());
		}
	
	
	
	
	public static List<Xref> makeXrefList(Pathway pway){
		// for every pathway element, check if it is a datanode.
		// if this is the case, put the xRef data in a list.
		List<PathwayElement> pelts = pway.getDataObjects();
		List<Xref> xRefList = new ArrayList();
		for (PathwayElement element:pelts){
			int objectType = element.getObjectType();
			// check if the objectType is a datanode
			if (objectType == ObjectType.DATANODE)
			{
				// retrieve the reference info
				Xref reference;
				reference = element.getXref();
				
				// add the reference info to a list
				xRefList.add(reference);
				
				// uncomment to get the name of the pathway element
				// String name;
				// name = element.getTextLabel();
				// System.out.println("GenID info: name: "+name);
				
				// uncomment to get the reference info (referenceId and databasename) 
				// of the pathway element				
				//String refId = reference.getName();
				//String databasename = reference.getDatabaseName();
				//System.out.println("Xref info: referenceID: "+refId+"  databasename: "+databasename);
				//System.out.println(" ");
				
				}
			}
		return xRefList;
		
	}
	
	}

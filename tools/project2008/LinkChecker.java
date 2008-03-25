import java.io.File;
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
		// TODO Auto-generated method stub
		String filename = args[0];
		File file = new File(filename);
		Pathway pway = new Pathway();
		boolean validate = true;
		pway.readFromXml(file, validate);
		List<PathwayElement> pelts = pway.getDataObjects();
		for (PathwayElement element:pelts){
			int objectType;
			objectType = element.getObjectType();
			if (objectType == ObjectType.DATANODE)
			{
				String genId;
				genId = element.getGeneID();
				System.out.println(genId);
			
				Xref reference;
				reference = element.getXref();
				String name = reference.getName();
				System.out.println(name);
				}
			}
		}
	}

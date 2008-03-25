import java.io.File;
import java.util.List;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Xref;


public class HelloWorld {


	/**
	 * @param args
	 * @throws ConverterException 
	 */
	public static void main(String[] args) throws ConverterException {
		// TODO Auto-generated method stub
		String filename = ("C:\\Documents and Settings\\s040772\\PathVisio-Data\\pathways\\rat\\Rn_Apoptosis.gpml");
		File f = new File(filename);
		Pathway p = new Pathway();
		p.readFromXml(f, true);
		List<PathwayElement> pelts = p.getDataObjects();
		for (PathwayElement v:pelts){
			Xref reference;
			reference = v.getXref();
			String name = reference.getName();
			System.out.println(name);
			}
		}
	}

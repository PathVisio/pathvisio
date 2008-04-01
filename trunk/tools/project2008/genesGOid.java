import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class genesGOid {

	public static void main(String[] args){
		String s; 
		List<String[]> arrayGOgenes = new ArrayList<String[]>();
		
		try {
			 FileReader fr = new FileReader(args[2]);
		      BufferedReader br = new BufferedReader(fr);

		     while((s = br.readLine()) != null){
		    	 arrayGOgenes.add(s.split("\t"));
		      }
		        //System.out.println(s);
		
		
		      fr.close();
		    }
		    catch(Exception e) {
		      System.out.println("Exception: " + e);
				}
	
    String[] tweedeUitDeLijst = arrayGOgenes.get(1);
    String eersteKolom = tweedeUitDeLijst[0];
    System.out.println(eersteKolom);
	System.out.println(arrayGOgenes.size());
	
	
	List<String> ensemblGeneIds = new ArrayList<String>();
	List<String> gOIds = new ArrayList<String>();
	
	//i begint bij 1 en niet bij 0 omdat het eerste getal in de vector niet moet worden meegeteld.
	for (int i = 1;i<(arrayGOgenes.size());i++){
		if (!arrayGOgenes.get(i)[0].equals(arrayGOgenes.get(i-1)[0])){
			ensemblGeneIds.add(arrayGOgenes.get(i)[0]);
			}
		}
	
	//int k=1;
	//for (int i = 1;i<(arrayGOgenes.size()-1);i++){
	//	if (arrayGOgenes.get(i)[0].equals(ensemblGeneIds.get(k))){
			
		//}
			
	
	//ensemblGeneIds.set(0,"test");
	System.out.println("arrayGOgenes2"+arrayGOgenes.get(2)[0]);
	System.out.println("arrayGOgenes3"+arrayGOgenes.get(3)[0]);
	System.out.println("arrayGOgenes4"+arrayGOgenes.get(4)[0]);
	
	
	
	System.out.println("ensemblGeneIds2"+ensemblGeneIds.get(2));
	System.out.println("ensemblGeneIds3"+ensemblGeneIds.get(3));
	System.out.println("ensemblGeneIds4"+ensemblGeneIds.get(4));
	}
	
}
	

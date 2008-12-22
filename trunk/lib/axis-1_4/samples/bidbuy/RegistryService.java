package samples.bidbuy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class RegistryService {

    private static Hashtable registry = new Hashtable();

    /**
      * Find a named service in a list
      * @param list of services
      * @param name to search for
      * @return service found (or null)
      */

    public RegistryService() {
      load();
    }

    public void load() {
      try {
        FileInputStream fis = new FileInputStream("bid.reg");
        ObjectInputStream ois = new ObjectInputStream( fis );
        registry = (Hashtable) ois.readObject();
        ois.close();
        fis.close();
      } catch(java.io.FileNotFoundException fnfe){
        // nop
      } catch(Exception e){
        e.printStackTrace();
      }
    }

    public void save() {
      try {
        FileOutputStream fos = new FileOutputStream("bid.reg");
        ObjectOutputStream oos = new ObjectOutputStream( fos );
        oos.writeObject( registry );
        oos.close();
        fos.close();
      } catch(Exception e){
        e.printStackTrace();
      }
    }

    private Service find(Vector list, String name) {
        Enumeration e = list.elements();
        while (e.hasMoreElements()) {
            Service s = (Service) e.nextElement();
            if (s.getServiceName().equals(name)) return s;
        }
        return null;
    }

    /**
     * Unregister a serivce
     * @param server name
     */
    public void Unregister(String name) {
        Enumeration e1 = registry.keys();
        while (e1.hasMoreElements()) {
            Vector list = (Vector) registry.get(e1.nextElement());
            Enumeration e2 = list.elements();
            while (e2.hasMoreElements()) {
                Service s = (Service) e2.nextElement();
                if (s.getServiceName().equals(name)) {
                    list.remove(s);
                    save();
                }
            }
        }
    }

    /**
     * Register a new serivce
     * @param server name
     * @param url of endpoint
     * @param stype
     * @param wsdl
     */
    public void Register(String name, String url, String stype, String wsdl) {
        Vector list = (Vector)registry.get(stype);
        if (list == null) registry.put(stype, list=new Vector());
        Service service = find(list, name);
        if (service==null)
            list.add(service=new Service());
        service.setServiceName(name);
        service.setServiceUrl(url);
        service.setServiceType(stype);
        service.setServiceWsdl(wsdl);
        save();
    }

    /**
     * Return the current list of services as an array
     * @param Service Name
     * @return List of servers that implement that service
     */
    public Service[] Lookup(String stype) {
        if (!registry.containsKey(stype)) return new Service[] {};
        Vector list = (Vector)registry.get(stype);
        Service[] result = new Service[list.size()];
        list.copyInto(result);
        return result;
    }

    /*
     * Return the current list of services as a string
     */
    public String LookupAsString(String stype) {
        Service[] services = Lookup(stype);
        String result = "";
        for (int i=0; i<services.length; i++) {
            Service service = services[i];
            result += service.getServiceName() + "\t" +
                      service.getServiceUrl() + "\t" +
                      service.getServiceType() + "\t" +
                      service.getServiceWsdl() + "\n";
        }
        return result;
    }

}

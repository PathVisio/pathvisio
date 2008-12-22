/**
 * StarWarsBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 16, 2005 (11:41:21 EDT) WSDL2Java emitter.
 */

package samples.xbeans;

public class StarWarsBindingImpl implements samples.xbeans.StarWarsPortType{
    com.superflaco.xbeans.Character stashed;
    
    public com.superflaco.xbeans.Character getChewbecca() throws java.rmi.RemoteException {
        com.superflaco.xbeans.Character chewie =
            com.superflaco.xbeans.Character.Factory.newInstance();
        chewie.setName("Chewbacca");

        com.superflaco.xbeans.System sys = com.superflaco.xbeans.System.Factory.newInstance();
        sys.setName("WookieSector");
        
        chewie.setHome(sys);
        chewie.setFaction("smuggler");
        chewie.setEvil(false);
        chewie.setJedi(false);
        return chewie;
    }

    public com.superflaco.xbeans.Character stashChar(com.superflaco.xbeans.Character newChew) throws java.rmi.RemoteException {
        if (stashed == null) {
            stashed = getChewbecca();
        }

        if (newChew != null) {
            System.out.println("old: " + stashed.toString());
            System.out.println("new: " + newChew.toString());
            stashed = newChew;
        }
        return stashed;
    }

}

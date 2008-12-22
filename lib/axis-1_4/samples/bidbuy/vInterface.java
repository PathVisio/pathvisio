package samples.bidbuy ;

import java.util.Vector;

public interface vInterface {
  public void register(String registryURL, Service s) throws Exception ;
  public void unregister(String registryURL, String name) throws Exception ;
  public Boolean ping(String serverURL) throws Exception ;
  public Vector lookupAsString(String registryURL) throws Exception ;
  public double requestForQuote(String serverURL) throws Exception ;
  public String simpleBuy(String serverURL, int quantity ) throws Exception ;
  public String buy(String serverURL, int quantity, int numItems, double price)
    throws Exception;
}

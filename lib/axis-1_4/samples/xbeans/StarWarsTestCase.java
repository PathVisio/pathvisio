/**
 * StarWarsTestCase.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 16, 2005 (11:41:21 EDT) WSDL2Java emitter.
 */

package samples.xbeans;

public class StarWarsTestCase extends junit.framework.TestCase {
    public StarWarsTestCase(java.lang.String name) {
        super(name);
    }

    /** TODO: Fix me
    public void testStarWarsPortWSDL() throws Exception {
        javax.xml.rpc.ServiceFactory serviceFactory = javax.xml.rpc.ServiceFactory.newInstance();
        java.net.URL url = new java.net.URL(new samples.xbeans.StarWarsLocator().getStarWarsPortAddress() + "?WSDL");
        javax.xml.rpc.Service service = serviceFactory.createService(url, new samples.xbeans.StarWarsLocator().getServiceName());
        assertTrue(service != null);
    }

    **/
    
    public void test1StarWarsPortGetChewbecca() throws Exception {
        samples.xbeans.StarWarsBindingStub binding;
        try {
            binding = (samples.xbeans.StarWarsBindingStub)
                          new samples.xbeans.StarWarsLocator().getStarWarsPort();
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }
        assertNotNull("binding is null", binding);

        // Time out after a minute
        binding.setTimeout(60000);

        // Test operation
        com.superflaco.xbeans.Character value = null;
        value = binding.getChewbecca();
        // TBD - validate results
        
        assertNotNull(value);
        
        System.out.println(value.toString());
    }
}

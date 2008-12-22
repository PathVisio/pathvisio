/**
 * ConfiguratorServiceTestCase.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2alpha Jan 15, 2004 (11:28:11 EST) WSDL2Java emitter.
 */

package org.apache.axis.wsi.scm.configurator;

public class ConfiguratorServiceTestCase extends junit.framework.TestCase {
    public ConfiguratorServiceTestCase(java.lang.String name) {
        super(name);
    }

    /* FIXME: RUNTIME WSDL broken.
    public void testConfiguratorPortWSDL() throws Exception {
        javax.xml.rpc.ServiceFactory serviceFactory = javax.xml.rpc.ServiceFactory.newInstance();
        java.net.URL url = new java.net.URL(new org.apache.axis.wsi.scm.configurator.ConfiguratorServiceLocator().getConfiguratorPortAddress() + "?WSDL");
        javax.xml.rpc.Service service = serviceFactory.createService(url, new org.apache.axis.wsi.scm.configurator.ConfiguratorServiceLocator().getServiceName());
        assertTrue(service != null);
    }
    */

    public void test1ConfiguratorPortGetConfigurationOptions() throws Exception {
        org.apache.axis.wsi.scm.configurator.ConfiguratorBindingStub binding;
        try {
            binding = (org.apache.axis.wsi.scm.configurator.ConfiguratorBindingStub)
                          new org.apache.axis.wsi.scm.configurator.ConfiguratorServiceLocator().getConfiguratorPort();
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
        try {
            org.apache.axis.wsi.scm.configurator.ConfigOptionsType value = null;
            value = binding.getConfigurationOptions(true);
        }
        catch (org.apache.axis.wsi.scm.configurator.ConfiguratorFailedFault e1) {
            throw new junit.framework.AssertionFailedError("configuratorFailedFault Exception caught: " + e1);
        }
            // TBD - validate results
    }

}

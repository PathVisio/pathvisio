/**
 * WarehouseServiceTestCase.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2alpha Jan 15, 2004 (11:28:11 EST) WSDL2Java emitter.
 */

package org.apache.axis.wsi.scm.warehouse;

public class WarehouseServiceTestCase extends junit.framework.TestCase {
    public WarehouseServiceTestCase(java.lang.String name) {
        super(name);
    }

    /* FIXME: RUNTIME WSDL broken.
    public void testWarehouseBPortWSDL() throws Exception {
        javax.xml.rpc.ServiceFactory serviceFactory = javax.xml.rpc.ServiceFactory.newInstance();
        java.net.URL url = new java.net.URL(new org.apache.axis.wsi.scm.warehouse.WarehouseServiceLocator().getWarehouseBPortAddress() + "?WSDL");
        javax.xml.rpc.Service service = serviceFactory.createService(url, new org.apache.axis.wsi.scm.warehouse.WarehouseServiceLocator().getServiceName());
        assertTrue(service != null);
    }
    */

    public void test1WarehouseBPortShipGoods() throws Exception {
        org.apache.axis.wsi.scm.warehouse.WarehouseSoapBindingStub binding;
        try {
            binding = (org.apache.axis.wsi.scm.warehouse.WarehouseSoapBindingStub)
                          new org.apache.axis.wsi.scm.warehouse.WarehouseServiceLocator().getWarehouseBPort();
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
            org.apache.axis.wsi.scm.warehouse.ItemShippingStatusList value = null;
            value = binding.shipGoods(new org.apache.axis.wsi.scm.warehouse.ItemList(), new org.apache.axis.types.NormalizedString(), new org.apache.axis.wsi.scm.configuration.ConfigurationType());
        }
        catch (org.apache.axis.wsi.scm.configuration.ConfigurationFaultType e1) {
            throw new junit.framework.AssertionFailedError("ConfigurationFault Exception caught: " + e1);
        }
            // TBD - validate results
    }

    /*
    public void testWarehouseCPortWSDL() throws Exception {
        javax.xml.rpc.ServiceFactory serviceFactory = javax.xml.rpc.ServiceFactory.newInstance();
        java.net.URL url = new java.net.URL(new org.apache.axis.wsi.scm.warehouse.WarehouseServiceLocator().getWarehouseCPortAddress() + "?WSDL");
        javax.xml.rpc.Service service = serviceFactory.createService(url, new org.apache.axis.wsi.scm.warehouse.WarehouseServiceLocator().getServiceName());
        assertTrue(service != null);
    }
    */

    public void test2WarehouseCPortShipGoods() throws Exception {
        org.apache.axis.wsi.scm.warehouse.WarehouseSoapBindingStub binding;
        try {
            binding = (org.apache.axis.wsi.scm.warehouse.WarehouseSoapBindingStub)
                          new org.apache.axis.wsi.scm.warehouse.WarehouseServiceLocator().getWarehouseCPort();
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
            org.apache.axis.wsi.scm.warehouse.ItemShippingStatusList value = null;
            value = binding.shipGoods(new org.apache.axis.wsi.scm.warehouse.ItemList(), new org.apache.axis.types.NormalizedString(), new org.apache.axis.wsi.scm.configuration.ConfigurationType());
        }
        catch (org.apache.axis.wsi.scm.configuration.ConfigurationFaultType e1) {
            throw new junit.framework.AssertionFailedError("ConfigurationFault Exception caught: " + e1);
        }
            // TBD - validate results
    }

    /*
    public void testWarehouseAPortWSDL() throws Exception {
        javax.xml.rpc.ServiceFactory serviceFactory = javax.xml.rpc.ServiceFactory.newInstance();
        java.net.URL url = new java.net.URL(new org.apache.axis.wsi.scm.warehouse.WarehouseServiceLocator().getWarehouseAPortAddress() + "?WSDL");
        javax.xml.rpc.Service service = serviceFactory.createService(url, new org.apache.axis.wsi.scm.warehouse.WarehouseServiceLocator().getServiceName());
        assertTrue(service != null);
    }
    */    


    public void test3WarehouseAPortShipGoods() throws Exception {
        org.apache.axis.wsi.scm.warehouse.WarehouseSoapBindingStub binding;
        try {
            binding = (org.apache.axis.wsi.scm.warehouse.WarehouseSoapBindingStub)
                          new org.apache.axis.wsi.scm.warehouse.WarehouseServiceLocator().getWarehouseAPort();
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
            org.apache.axis.wsi.scm.warehouse.ItemShippingStatusList value = null;
            value = binding.shipGoods(new org.apache.axis.wsi.scm.warehouse.ItemList(), new org.apache.axis.types.NormalizedString(), new org.apache.axis.wsi.scm.configuration.ConfigurationType());
        }
        catch (org.apache.axis.wsi.scm.configuration.ConfigurationFaultType e1) {
            throw new junit.framework.AssertionFailedError("ConfigurationFault Exception caught: " + e1);
        }
            // TBD - validate results
    }

}

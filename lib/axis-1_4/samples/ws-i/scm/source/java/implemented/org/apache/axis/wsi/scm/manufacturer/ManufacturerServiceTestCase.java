/**
 * ManufacturerServiceTestCase.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2alpha Jan 15, 2004 (11:28:11 EST) WSDL2Java emitter.
 */

package org.apache.axis.wsi.scm.manufacturer;

public class ManufacturerServiceTestCase extends junit.framework.TestCase {
    public ManufacturerServiceTestCase(java.lang.String name) {
        super(name);
    }

    /* FIXME: RUNTIME WSDL broken.
    public void testManufacturerCPortWSDL() throws Exception {
        javax.xml.rpc.ServiceFactory serviceFactory = javax.xml.rpc.ServiceFactory.newInstance();
        java.net.URL url = new java.net.URL(new org.apache.axis.wsi.scm.manufacturer.ManufacturerServiceLocator().getManufacturerCPortAddress() + "?WSDL");
        javax.xml.rpc.Service service = serviceFactory.createService(url, new org.apache.axis.wsi.scm.manufacturer.ManufacturerServiceLocator().getServiceName());
        assertTrue(service != null);
    }
    */

    public void test1ManufacturerCPortSubmitPO() throws Exception {
        org.apache.axis.wsi.scm.manufacturer.ManufacturerSoapBindingStub binding;
        try {
            binding = (org.apache.axis.wsi.scm.manufacturer.ManufacturerSoapBindingStub)
                          new org.apache.axis.wsi.scm.manufacturer.ManufacturerServiceLocator().getManufacturerCPort();
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
            boolean value = false;
            value = binding.submitPO(new org.apache.axis.wsi.scm.manufacturer.po.PurchOrdType(), new org.apache.axis.wsi.scm.configuration.ConfigurationType(), new org.apache.axis.wsi.scm.manufacturer.callback.StartHeaderType());
        }
        catch (org.apache.axis.wsi.scm.configuration.ConfigurationFaultType e1) {
            throw new junit.framework.AssertionFailedError("ConfigurationFault Exception caught: " + e1);
        }
        catch (org.apache.axis.wsi.scm.manufacturer.po.SubmitPOFaultType e2) {
            throw new junit.framework.AssertionFailedError("POFault Exception caught: " + e2);
        }
            // TBD - validate results
    }

    /* FIXME: RUNTIME WSDL broken.
    public void testManufacturerBPortWSDL() throws Exception {
        javax.xml.rpc.ServiceFactory serviceFactory = javax.xml.rpc.ServiceFactory.newInstance();
        java.net.URL url = new java.net.URL(new org.apache.axis.wsi.scm.manufacturer.ManufacturerServiceLocator().getManufacturerBPortAddress() + "?WSDL");
        javax.xml.rpc.Service service = serviceFactory.createService(url, new org.apache.axis.wsi.scm.manufacturer.ManufacturerServiceLocator().getServiceName());
        assertTrue(service != null);
    }
    */

    public void test2ManufacturerBPortSubmitPO() throws Exception {
        org.apache.axis.wsi.scm.manufacturer.ManufacturerSoapBindingStub binding;
        try {
            binding = (org.apache.axis.wsi.scm.manufacturer.ManufacturerSoapBindingStub)
                          new org.apache.axis.wsi.scm.manufacturer.ManufacturerServiceLocator().getManufacturerBPort();
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
            boolean value = false;
            value = binding.submitPO(new org.apache.axis.wsi.scm.manufacturer.po.PurchOrdType(), new org.apache.axis.wsi.scm.configuration.ConfigurationType(), new org.apache.axis.wsi.scm.manufacturer.callback.StartHeaderType());
        }
        catch (org.apache.axis.wsi.scm.configuration.ConfigurationFaultType e1) {
            throw new junit.framework.AssertionFailedError("ConfigurationFault Exception caught: " + e1);
        }
        catch (org.apache.axis.wsi.scm.manufacturer.po.SubmitPOFaultType e2) {
            throw new junit.framework.AssertionFailedError("POFault Exception caught: " + e2);
        }
            // TBD - validate results
    }

    /* FIXME: RUNTIME WSDL broken.
    public void testManufacturerAPortWSDL() throws Exception {
        javax.xml.rpc.ServiceFactory serviceFactory = javax.xml.rpc.ServiceFactory.newInstance();
        java.net.URL url = new java.net.URL(new org.apache.axis.wsi.scm.manufacturer.ManufacturerServiceLocator().getManufacturerAPortAddress() + "?WSDL");
        javax.xml.rpc.Service service = serviceFactory.createService(url, new org.apache.axis.wsi.scm.manufacturer.ManufacturerServiceLocator().getServiceName());
        assertTrue(service != null);
    }
    */

    public void test3ManufacturerAPortSubmitPO() throws Exception {
        org.apache.axis.wsi.scm.manufacturer.ManufacturerSoapBindingStub binding;
        try {
            binding = (org.apache.axis.wsi.scm.manufacturer.ManufacturerSoapBindingStub)
                          new org.apache.axis.wsi.scm.manufacturer.ManufacturerServiceLocator().getManufacturerAPort();
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
            boolean value = false;
            value = binding.submitPO(new org.apache.axis.wsi.scm.manufacturer.po.PurchOrdType(), new org.apache.axis.wsi.scm.configuration.ConfigurationType(), new org.apache.axis.wsi.scm.manufacturer.callback.StartHeaderType());
        }
        catch (org.apache.axis.wsi.scm.configuration.ConfigurationFaultType e1) {
            throw new junit.framework.AssertionFailedError("ConfigurationFault Exception caught: " + e1);
        }
        catch (org.apache.axis.wsi.scm.manufacturer.po.SubmitPOFaultType e2) {
            throw new junit.framework.AssertionFailedError("POFault Exception caught: " + e2);
        }
            // TBD - validate results
    }


    /* FIXME: RUNTIME WSDL broken.
    public void testWarehouseCallbackPortWSDL() throws Exception {
        javax.xml.rpc.ServiceFactory serviceFactory = javax.xml.rpc.ServiceFactory.newInstance();
        java.net.URL url = new java.net.URL(new org.apache.axis.wsi.scm.manufacturer.ManufacturerServiceLocator().getWarehouseCallbackPortAddress() + "?WSDL");
        javax.xml.rpc.Service service = serviceFactory.createService(url, new org.apache.axis.wsi.scm.manufacturer.ManufacturerServiceLocator().getServiceName());
        assertTrue(service != null);
    }
    */

    public void test4WarehouseCallbackPortSubmitSN() throws Exception {
        org.apache.axis.wsi.scm.manufacturer.WarehouseCallbackSoapBindingStub binding;
        try {
            binding = (org.apache.axis.wsi.scm.manufacturer.WarehouseCallbackSoapBindingStub)
                          new org.apache.axis.wsi.scm.manufacturer.ManufacturerServiceLocator().getWarehouseCallbackPort();
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
            boolean value = false;
            value = binding.submitSN(new org.apache.axis.wsi.scm.manufacturer.sn.ShipmentNoticeType(), new org.apache.axis.wsi.scm.configuration.ConfigurationType(), new org.apache.axis.wsi.scm.manufacturer.callback.CallbackHeaderType());
        }
        catch (org.apache.axis.wsi.scm.configuration.ConfigurationFaultType e1) {
            throw new junit.framework.AssertionFailedError("ConfigurationFault Exception caught: " + e1);
        }
        catch (org.apache.axis.wsi.scm.manufacturer.callback.CallbackFaultType e2) {
            throw new junit.framework.AssertionFailedError("CallbackFault Exception caught: " + e2);
        }
            // TBD - validate results
    }

    public void test5WarehouseCallbackPortErrorPO() throws Exception {
        org.apache.axis.wsi.scm.manufacturer.WarehouseCallbackSoapBindingStub binding;
        try {
            binding = (org.apache.axis.wsi.scm.manufacturer.WarehouseCallbackSoapBindingStub)
                          new org.apache.axis.wsi.scm.manufacturer.ManufacturerServiceLocator().getWarehouseCallbackPort();
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
            boolean value = false;
            value = binding.errorPO(new org.apache.axis.wsi.scm.manufacturer.po.SubmitPOFaultType(), new org.apache.axis.wsi.scm.configuration.ConfigurationType(), new org.apache.axis.wsi.scm.manufacturer.callback.CallbackHeaderType());
        }
        catch (org.apache.axis.wsi.scm.configuration.ConfigurationFaultType e1) {
            throw new junit.framework.AssertionFailedError("ConfigurationFault Exception caught: " + e1);
        }
        catch (org.apache.axis.wsi.scm.manufacturer.callback.CallbackFaultType e2) {
            throw new junit.framework.AssertionFailedError("CallbackFault Exception caught: " + e2);
        }
            // TBD - validate results
    }

}

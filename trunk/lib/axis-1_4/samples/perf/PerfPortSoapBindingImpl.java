/**
 * PerfPortSoapBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2beta Apr 25, 2004 (11:19:16 EDT) WSDL2Java emitter.
 */

package samples.perf;

public class PerfPortSoapBindingImpl implements samples.perf.PerfService_PortType{
    public java.lang.String handleStringArray(java.lang.String[] s) throws java.rmi.RemoteException {
        String returnString;
        returnString = "array length was - " + s.length;
        return returnString;
    }

}

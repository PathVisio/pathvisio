/**
 * HelloBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package samples.jaxrpc.hello;

public class HelloBindingImpl implements samples.jaxrpc.hello.Hello {
    public java.lang.String sayHello(java.lang.String name) throws java.rmi.RemoteException {
        return "A dynamic proxy hello to " + name + "!";
    }

}

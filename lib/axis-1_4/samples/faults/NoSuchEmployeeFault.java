/**
 * NoSuchEmployeeFault.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package samples.faults;

import java.rmi.RemoteException;

public class NoSuchEmployeeFault extends RemoteException  implements java.io.Serializable {
    private java.lang.String info;

    public NoSuchEmployeeFault() {
    }

    public NoSuchEmployeeFault(
           java.lang.String info) {
        this.info = info;
    }

    public java.lang.String getInfo() {
        return info;
    }

    public void setInfo(java.lang.String info) {
        this.info = info;
    }
}

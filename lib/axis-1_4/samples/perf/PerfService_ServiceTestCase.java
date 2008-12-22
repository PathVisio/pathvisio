/**
 * PerfService_ServiceTestCase.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2beta Apr 25, 2004 (11:19:16 EDT) WSDL2Java emitter.
 */

package samples.perf;

import java.util.Date;
import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;

public class PerfService_ServiceTestCase extends junit.framework.TestCase {
    /** Field log */
    static Log log = LogFactory.getLog(PerfService_ServiceTestCase.class.getName());

    public PerfService_ServiceTestCase(java.lang.String name) {
        super(name);
    }

    public void test1PerfPortHandleStringArray() throws Exception {
        samples.perf.PerfPortSoapBindingStub binding;
        try {
            binding = (samples.perf.PerfPortSoapBindingStub)
                          new samples.perf.PerfService_ServiceLocator().getPerfPort();
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }
        assertNotNull("binding is null", binding);

        // Time out after a minute
        binding.setTimeout(60000);
        binding._setProperty(org.apache.axis.client.Call.STREAMING_PROPERTY, Boolean.TRUE);
        // Time out after a minute
        binding.setTimeout(60000);

        log.info(">>>> Warming up...");
        pump(binding, 1);
        log.info(">>>> Running volume tests...");
        pump(binding, 100);
        pump(binding, 1000);
        pump(binding, 10000);
        pump(binding, 100000);
    }

    private static void pump(PerfPortSoapBindingStub binding, int count)
            throws java.rmi.RemoteException {
        String[] s = new String[count];
        for (int i = 0; i < s.length; i++) {
            s[i] = "qwertyuiopåasdfghjklöäzxcvbnm";
        }
        Date start = new Date();
        String value = binding.handleStringArray(s);
        Date end = new Date();

        log.info("Count:" + count + " \tTime consumed: " +
                (end.getTime() - start.getTime()) + "\tReturn:" + value);
    }

    public static void main(String[] args) throws Exception {
        PerfService_ServiceTestCase tests = new PerfService_ServiceTestCase("Perf");
        tests.test1PerfPortHandleStringArray();
    }
}

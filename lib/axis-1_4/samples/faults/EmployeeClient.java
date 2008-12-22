package samples.faults;

import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.AxisFault;
import org.apache.axis.utils.Options;
import org.apache.axis.transport.http.SimpleAxisWorker;
import org.apache.axis.description.OperationDesc;

import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.Service;
import javax.xml.rpc.Call;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.namespace.QName;
import java.net.URL;
import java.util.Map;
import java.util.Iterator;

import samples.faults.Employee;

public class EmployeeClient {
    public static void main(String[] args) throws Exception {
        Options opts = new Options(args);
        String uri = "http://faults.samples";
        String serviceName = "EmployeeInfoService";
        ServiceFactory serviceFactory = ServiceFactory.newInstance();
        Service service = serviceFactory.createService(new QName(uri, serviceName));
        
        TypeMappingRegistry registry = service.getTypeMappingRegistry();
        TypeMapping map = registry.getDefaultTypeMapping();
        
        QName employeeQName = new QName("http://faults.samples", "Employee");
        map.register(Employee.class, employeeQName, new BeanSerializerFactory(Employee.class, employeeQName), new BeanDeserializerFactory(Employee.class, employeeQName));

        QName faultQName = new QName("http://faults.samples", "NoSuchEmployeeFault");
        map.register(NoSuchEmployeeFault.class, faultQName, new BeanSerializerFactory(NoSuchEmployeeFault.class, faultQName), new BeanDeserializerFactory(NoSuchEmployeeFault.class, faultQName));
        
        Call call = service.createCall();
        call.setTargetEndpointAddress(new URL(opts.getURL()).toString());
        call.setProperty(Call.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
        call.setProperty(Call.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        call.setProperty(Call.SOAPACTION_URI_PROPERTY, "http://faults.samples");
        call.setOperationName( new QName(uri, "getEmployee") );

        String[] args2 = opts.getRemainingArgs();
        System.out.println("Trying :" + args2[0]);
        Employee emp = (Employee) call.invoke(new Object[]{ args2[0] });
        System.out.println("Got :" + emp.getEmployeeID());
    }
}

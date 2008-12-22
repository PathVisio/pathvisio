package samples.jaxrpc.address;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;
import java.net.URL;

public class AddressClient {
    public static void main(String[] args) throws Exception {
        URL urlWsdl = new URL("http://localhost:8080/axis/services/Address?wsdl");
        String nameSpaceUri = "http://address.jaxrpc.samples";
        String serviceName = "AddressServiceService";
        String portName = "Address";

        ServiceFactory serviceFactory = ServiceFactory.newInstance();
        Service service = serviceFactory.createService(urlWsdl, new
                QName(nameSpaceUri, serviceName));
        AddressService myProxy = (AddressService) service.getPort(new
                QName(nameSpaceUri, portName), AddressService.class);
        AddressBean addressBean = new AddressBean();
        addressBean.setStreet("55, rue des Lilas");
        System.out.println(myProxy.updateAddress(addressBean, 75005));
    }
}
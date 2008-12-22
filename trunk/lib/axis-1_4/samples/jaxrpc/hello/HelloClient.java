package samples.jaxrpc.hello;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;
import java.net.URL;

public class HelloClient {
    public static void main(String[] args) throws Exception {
        String UrlString = "http://localhost:8080/axis/services/HelloPort?wsdl";
        String nameSpaceUri = "http://hello.jaxrpc.samples/";
        String serviceName = "HelloWorld";
        String portName = "HelloPort";

        URL helloWsdlUrl = new URL(UrlString);
        ServiceFactory serviceFactory = ServiceFactory.newInstance();
        Service helloService = serviceFactory.createService(helloWsdlUrl,
                new QName(nameSpaceUri, serviceName));

        java.util.List list = helloService.getHandlerRegistry().getHandlerChain(new QName(nameSpaceUri, portName));
        list.add(new javax.xml.rpc.handler.HandlerInfo(ClientHandler.class,null,null));

        Hello myProxy = (Hello) helloService.getPort(
                new QName(nameSpaceUri, portName),
                samples.jaxrpc.hello.Hello.class);

        System.out.println(myProxy.sayHello("Buzz"));
    }
}

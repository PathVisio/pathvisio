package samples.jaxrpc.hello;

public class ServerHandler implements javax.xml.rpc.handler.Handler {
    public ServerHandler() {
    }

    public boolean handleRequest(javax.xml.rpc.handler.MessageContext context) {
        System.out.println("ServerHandler: In handleRequest");
        return true;
    }

    public boolean handleResponse(javax.xml.rpc.handler.MessageContext context) {
        System.out.println("ServerHandler: In handleResponse");
        return true;
    }

    public boolean handleFault(javax.xml.rpc.handler.MessageContext context) {
        System.out.println("ServerHandler: In handleFault");
        return true;
    }

    public void init(javax.xml.rpc.handler.HandlerInfo config) {
    }

    public void destroy() {
    }

    public javax.xml.namespace.QName[] getHeaders() {
        return null;
    }
}


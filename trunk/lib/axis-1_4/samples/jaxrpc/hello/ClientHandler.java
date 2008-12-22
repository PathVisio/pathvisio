package samples.jaxrpc.hello;

public class ClientHandler implements javax.xml.rpc.handler.Handler {
    public ClientHandler() {
    }

    public boolean handleRequest(javax.xml.rpc.handler.MessageContext context) {
        System.out.println("ClientHandler: In handleRequest");
        return true;
    }

    public boolean handleResponse(javax.xml.rpc.handler.MessageContext context) {
        System.out.println("ClientHandler: In handleResponse");
        return true;
    }

    public boolean handleFault(javax.xml.rpc.handler.MessageContext context) {
        System.out.println("ClientHandler: In handleFault");
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


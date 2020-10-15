# Test2API
Simple and minimalistic REST API Server written in Java

### Usage example
```java
import eu.piotro.test2api.tcp.*;
import eu.piotro.test2api.http.*;
import eu.piotro.test2api.api.*;
import eu.piotro.test2api.api.annotations.*;

public class REST {
    public void run(){
        APIForwarder forwarder = new APIForwarder();
        forwarder.registerClass(REST.class);
        int PORT = 1234;
        Server server = new Server(PORT, forwarder);
        while(true){
            server.accept();
        }
    }
    
    @RESTHandler(method = "GET", URI = "/")
    public static HTTPResponse getRoot(HTTPRequest request){
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain", "Hello World!");
    }
}
```

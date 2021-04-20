# Rest2API
Simple and minimalistic REST API Server written in Java

### Usage example
```java
import eu.piotro.rest2api.tcp.*;
import eu.piotro.rest2api.http.*;
import eu.piotro.rest2api.api.*;
import eu.piotro.rest2api.api.annotations.*;
import java.io.IOException;

public class REST {
    public void run() throws IOException {
        APIForwarder forwarder = new APIForwarder();
        forwarder.registerClassStatic(REST.class);
        //or forwarder.registerClass(new REST()); with non-static methods
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

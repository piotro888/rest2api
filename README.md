# Rest2API
Light-weight Java server for REST APIs

## Features
- Easy to use with `@RestHandler` annotation
- Light-weight, with no external dependencies
- Universal - your handler methods assigned to URIs receive `HttpRequest` and return `HttpResponse`
- Simple and fast, provides basic HTTP functionalities to run a REST (or other API over HTTP) application
- Endpoint URIs supports regex and all HTTP methods.
- Supports both static and non-static handler methods
- Logs to `java.util.logging.Logger`
- Queues connections and executes them via workers with customizable parameters

## Usage example
```java
import eu.piotro.rest2api.tcp.*;
import eu.piotro.rest2api.http.*;
import eu.piotro.rest2api.api.*;
import eu.piotro.rest2api.api.annotations.*;
import java.io.IOException;

public class REST {
    public void run() throws IOException {
        APIForwarder forwarder = new APIForwarder();
        forwarder.registerClassStatic(REST.class); // Process annotations for REST class and register handlers automatically
        //or forwarder.registerClass(new REST()); with non-static methods
        int PORT = 1234;
        Server server = new Server(PORT, forwarder);
        while(true){
            server.accept(); // Wait for incoming socket and execute it asynchronously
        }
    }
    
    @RESTHandler(method = "GET", URI = "/") // Annotation for APIForwarder to register method
    public static HTTPResponse getRoot(HTTPRequest request) { // Handler methods are assigned to URIs and HTTP Methods
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain", "Hello World!"); 
    }
}
```
See javadoc for more details.

## Installation
### Gradle
```groovy
dependencies {
    implementation 'eu.piotro:rest2api:CURRENT_VERSION'
}
```
### Maven
```xml
<dependecies>
    <dependency>
        <groupId>eu.piotro</groupId>
        <artifactId>rest2api</artifactId>
        <version>CURRENT_VERSION</version>   
    </dependency>
</dependecies>
```
Replace CURRENT_VERSION with the latest version ex. 1.3.0

## More customization
If built-in customization isn't enough, you can override `APIForwarder` class for custom routing
or pass accepted Socket directly (or via `ServerExecutor`) to `ConnectionHandler`

## Contributing
Feel free to open a new issue or change something via pull request!

Build via `./gradlew build`

## TODO
- Support for persistent (keep-alive) connections
- SSL/TLS (for now you can use proxy)

## License
Licensed under MIT License 

rest2api by Piotr Wegrzyn

---
*Thanks for using or contributing*

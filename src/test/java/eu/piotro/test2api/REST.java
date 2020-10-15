package eu.piotro.test2api;

import eu.piotro.test2api.api.APIHandler;
import eu.piotro.test2api.api.annotations.RESTHandler;
import eu.piotro.test2api.http.HTTPCodes;
import eu.piotro.test2api.http.HTTPRequest;
import eu.piotro.test2api.http.HTTPResponse;

public class REST {
    @RESTHandler(method = "GET", URI = "/")
    public static HTTPResponse getRoot(HTTPRequest r){
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain", "Hello World!");
    }

    @RESTHandler(method = "GET", URI = "/test")
    public static HTTPResponse getRestaurants(HTTPRequest request){
        return new HTTPResponse(200, HTTPCodes.C200, APIHandler.JSON, "{\"cnt\":0}");
    }
    @RESTHandler(method = "GET", URI = "/test/[0-9]+")
    public static HTTPResponse getRestaurantsID(HTTPRequest request){
        String uri = request.getURI();
        int id = Integer.parseInt(uri.substring(uri.lastIndexOf('/')+1));
        return new HTTPResponse(200, HTTPCodes.C200, APIHandler.JSON, "{\"id\":"+id+",\"name\":\"unknown\"}");
    }
}

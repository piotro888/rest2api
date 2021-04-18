package eu.piotro.test2api.examples;

import eu.piotro.test2api.api.annotations.RESTHandler;
import eu.piotro.test2api.http.HTTPCodes;
import eu.piotro.test2api.http.HTTPRequest;
import eu.piotro.test2api.http.HTTPResponse;

public class RESTDynamic {
    RESTDynamic(String response){
        this.response = response;
    }

    @RESTHandler(method = "GET", URI = "/dynamic")
    public HTTPResponse getDynamic(HTTPRequest request){
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain", response);
    }

    private final String response;
}

package eu.piotro.test2api;

import eu.piotro.test2api.api.APIForwarder;
import eu.piotro.test2api.api.annotations.RESTHandler;
import eu.piotro.test2api.http.HTTPCodes;
import eu.piotro.test2api.http.HTTPRequest;
import eu.piotro.test2api.http.HTTPResponse;
import eu.piotro.test2api.tcp.Server;

import java.io.IOException;

/**
 * Simply run the Server
 */
public class Main {
    public static void main(String[] args){
        try {
            APIForwarder forwarder = new APIForwarder();
            forwarder.registerClass(Main.class);
            Server server = new Server(PORT, forwarder);
            while(!Thread.currentThread().isInterrupted()){server.accept();}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final int PORT = 1351;

    @RESTHandler(method = "GET", URI = "/")
    public static HTTPResponse getRoot(HTTPRequest r){
        return new HTTPResponse(200, HTTPCodes.C200, "text/plain", "Hello World!");
    }
}

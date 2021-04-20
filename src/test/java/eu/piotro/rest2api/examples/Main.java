package eu.piotro.rest2api.examples;

import eu.piotro.rest2api.api.APIForwarder;
import eu.piotro.rest2api.tcp.Server;

import java.io.IOException;

/**
 * Simply run the Server
 */
public class Main {
    public static void main(String[] args){
        try {
            APIForwarder forwarder = new APIForwarder();
            forwarder.registerClassStatic(REST.class);
            RESTDynamic restClass = new RESTDynamic("Hello from non-static method");
            forwarder.registerClass(restClass);

            Server server = new Server(PORT, forwarder);
            while(!Thread.currentThread().isInterrupted()){server.accept();}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final int PORT = 1351;
}

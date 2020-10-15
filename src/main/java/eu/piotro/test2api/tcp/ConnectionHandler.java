package eu.piotro.test2api.tcp;

import java.io.*;
import java.net.Socket;

import eu.piotro.test2api.api.APIForwarder;
import eu.piotro.test2api.http.HTTPExceptionHandler;
import eu.piotro.test2api.http.HTTPResponse;
import eu.piotro.test2api.http.HTTPException;
import eu.piotro.test2api.http.HTTPRequest;

/**
 * Handles HTTP Socket connections
 * @author piotro
 * @since 0.1
 */
public class ConnectionHandler implements Runnable {
    /**
     * Initializes handler. {@link #run()} must be called in order to process request.
     * @param socket {@link Socket} to handle
     * @param forwarder {@link APIForwarder} used to route HTTP requests
     */
    ConnectionHandler(Socket socket, APIForwarder forwarder) throws IOException {
        this.socket = socket;
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        request = new HTTPRequest(reader);
        this.apiForwarder = forwarder;
        if(forwarder.getHTTPExceptionHandler() != null)
            this.exceptionHandler = forwarder.getHTTPExceptionHandler();
    }

    /**
     * Reads, passes to {@link APIForwarder} and responds to HTTP request.
     */
    @Override
    public void run() {

        System.out.println("Received connection from " + socket);

        try{
            request.read();
            System.out.println(request.getBody());
            System.out.println(request.getHeaders());

            HTTPResponse response = apiForwarder.forward(request);
            respond(response);

        } catch (HTTPException e){
            //handleHTTPError(e);
            exceptionHandler.handleHTTPException(e);
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try{ socket.close(); } catch (IOException e) {e.printStackTrace();}
        }
        System.out.println(request);
        System.out.println("ConnEND");
    }

    private final Socket socket;
    private final PrintWriter writer;
    private final HTTPRequest request;
    private final APIForwarder apiForwarder;
    private HTTPExceptionHandler exceptionHandler = new DefaultHTTPExceptionHandler();

    private void respond(HTTPResponse response){
        System.out.println(socket + " " + response.getCode() + " " + response.getCodeDescription());
        writer.println("HTTP/1.1 " + response.getCode() + " " + response.getCodeDescription());
        writer.println("Content-Type: " + response.getType());
        writer.println("Content-Length: " + response.getBody().length());
        if(!response.getHeaders().isEmpty())
            writer.println(response.getHeaders());
        writer.print("\r\n");
        writer.println(response.getBody());
        writer.flush();
    }

    private class DefaultHTTPExceptionHandler implements HTTPExceptionHandler {
        @Override
        public HTTPResponse handleHTTPException(HTTPException e){
            System.out.println("HTTPException " + e + " " + socket);
            String errorHTTP = "<html>\n" +
                    "    <h2>API Error</h2>\n" +
                    "    <h3>" + e.getCode() + " " + e.getMessage() + "</h3>\n" +
                    "    <hr> Test2API Server v. 1.1.0\n" +
                    "</html>\r\n";
            //respond(response);
            return new HTTPResponse(e.getCode(), e.getMessage(), "text/html", e.getHeaders(), errorHTTP);
        }
    }
}
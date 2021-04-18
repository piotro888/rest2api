package eu.piotro.test2api.tcp;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

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
     * @param timeoutExecutor {@link ScheduledExecutorService} to schedule connection timeouts
     * @param readTimeout time in milliseconds to timeout request reading
     * @throws IOException if I/O error when creating socket reader or writer
     */
    ConnectionHandler(Socket socket, APIForwarder forwarder, ScheduledExecutorService timeoutExecutor, int readTimeout) throws IOException {
        this.socket = socket;
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        request = new HTTPRequest(reader, socket, timeoutExecutor, readTimeout);
        this.apiForwarder = forwarder;
        if(forwarder.getHTTPExceptionHandler() != null)
            this.exceptionHandler = forwarder.getHTTPExceptionHandler();
    }

    /**
     * Reads, passes to {@link APIForwarder} and responds to HTTP request.
     */
    @Override
    public void run() {

        logger.info("Processing connection " + socket);

        try {
            request.read();
            logger.info(socket + " request: " +  request);
            logger.fine("headers: " + request.getHeaders() + " body: " + request.getBody());

            HTTPResponse response = apiForwarder.forward(request);
            respond(response);

        } catch (HTTPException e){
            respond(exceptionHandler.handleHTTPException(e));
        } catch (IOException e){
            logger.info(socket + " IOException " + e);
        } finally {
            try{ socket.close(); } catch (IOException e) { logger.info(socket + " IOException when closing " + e); }
        }
    }

    private final Socket socket;
    private final PrintWriter writer;
    private final HTTPRequest request;
    private final APIForwarder apiForwarder;
    private HTTPExceptionHandler exceptionHandler = new DefaultHTTPExceptionHandler();
    private final Logger logger = Logger.getLogger(ConnectionHandler.class.getName());

    private void respond(HTTPResponse response){
        logger.info(socket + " " + response.getCode() + " " + response.getCodeDescription());
        if(response.getCode() == 500)
            logger.warning(socket + " 500 Status code returned");

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
            String errorHTTP = "<html>\n" +
                    "    <h2>API Error</h2>\n" +
                    "    <h3>" + e.getCode() + " " + e.getMessage() + "</h3>\n" +
                    "    <hr> Test2API Server\n" +
                    "</html>\r\n";
            return new HTTPResponse(e.getCode(), e.getMessage(), "text/html", e.getHeaders(), errorHTTP);
        }
    }
}
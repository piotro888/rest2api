package eu.piotro.rest2api.tcp;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import eu.piotro.rest2api.api.APIForwarder;
import eu.piotro.rest2api.http.*;

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
        BufferedReader breader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new HTTPReader(breader, socket, timeoutExecutor, readTimeout);
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
        reader.setTimeout();
        try {
            while (true) {
                try {
                    HTTPRequest request = reader.read();
                    logger.info(socket + " request: " + request);
                    logger.fine("headers: " + request.getHeaders() + " body: " + request.getBody());

                    HTTPResponse response = apiForwarder.forward(request);

                    if(request.getHeaders().get("Connection") != null && request.getHeaders().get("Connection").equalsIgnoreCase("close")
                      || request.getHeaders().get("connection") != null && request.getHeaders().get("connection").equalsIgnoreCase("close")) {
                        respond(response, true);
                        break;
                    }
                    else
                        respond(response, false);

                } catch (HTTPException e){
                    if(e.getCode() == 408) {
                        respond(exceptionHandler.handleHTTPException(e), true);
                        break;
                    }
                    respond(exceptionHandler.handleHTTPException(e), false);
                }
            }
        } catch (IOException e){
            logger.info(socket + " IOException " + e);
        } finally {
            try{ socket.close(); } catch (IOException e) { logger.info(socket + " IOException when closing " + e); }
            reader.cancelTimeout();
        }
    }

    private final Socket socket;
    private final PrintWriter writer;
    private final HTTPReader reader;
    private final APIForwarder apiForwarder;
    private HTTPExceptionHandler exceptionHandler = new DefaultHTTPExceptionHandler();
    private final Logger logger = Logger.getLogger(ConnectionHandler.class.getName());

    private void respond(HTTPResponse response, boolean close){
        logger.info(socket + " " + response.getCode() + " " + response.getCodeDescription());
        if(response.getCode() == 500)
            logger.warning(socket + " 500 Status code returned");

        writer.print("HTTP/1.1 " + response.getCode() + " " + response.getCodeDescription() + "\r\n");
        if(close)
            writer.print("Connection: close" + "\r\n");
        else
            writer.print("Connection: keep-alive" + "\r\n");
        writer.print("Content-Type: " + response.getType() + "\r\n");
        writer.print("Content-Length: " + response.getBody().length() + "\r\n");
        writer.print("Server: rest2api" + "\r\n");
        if(!response.getHeaders().isEmpty())
            writer.print(response.getHeaders() + "\r\n");
        writer.print("\r\n");
        if(response.getBody() != null)
            writer.print(response.getBody() + "\r\n");
        writer.flush();
    }

    private static class DefaultHTTPExceptionHandler implements HTTPExceptionHandler {
        @Override
        public HTTPResponse handleHTTPException(HTTPException e){
            String errorHTTP = "<html>\n" +
                    "    <h2>API Error</h2>\n" +
                    "    <h3>" + e.getCode() + " " + e.getMessage() + "</h3>\n" +
                    "    <hr> Rest2API Server\n" +
                    "</html>\r\n";
            return new HTTPResponse(e.getCode(), e.getMessage(), "text/html", e.getHeaders(), errorHTTP);
        }
    }
}
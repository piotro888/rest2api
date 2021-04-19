package eu.piotro.rest2api.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * Represents HTTP request
 */
public class HTTPRequest {
    /**
     * Creates new HTTP request (read via {@link #read()})
     * @param reader reader of inet socket
     * @param socket {@link Socket} to read from
     * @param timeoutExecutor {@link ScheduledExecutorService} to schedule timeout events
     * @param readTimeout time in milliseconds to timeout request reading
     */
    public HTTPRequest(BufferedReader reader, Socket socket, ScheduledExecutorService timeoutExecutor, int readTimeout) {
        this.reader = reader;
        headersMap = new HashMap<>();
        this.socket = socket;
        this.timeoutExecutor = timeoutExecutor;
        this.readTimeout = readTimeout;
    }

    Socket socket;

    /**
     * Reads and parses HTTP requests
     * @throws IOException if I/O exception while reading from socket
     * @throws HTTPException if request is invalid (4xx HTTP errors)
     */
    public void read() throws IOException, HTTPException {
        setTimeout();

        String requestLine = reader.readLine();

        if(timeout)
            throw new HTTPException(408, HTTPCodes.C408);

        if (requestLine == null)
            throw new IOException("No data sent to socket");

        String[] splitRequest = requestLine.split(" +");

        if (splitRequest.length != 3)  //GET / HTTP/1.1
            throw new HTTPException(400, HTTPCodes.C400);

        method = splitRequest[0];
        uri = splitRequest[1];
        String proto = splitRequest[2];

        if (!proto.equals("HTTP/1.1") && splitRequest[2].contains("HTTP/"))
            throw new HTTPException(505, HTTPCodes.C505);
        else if (!proto.equals("HTTP/1.1"))
            throw new HTTPException(400, HTTPCodes.C400);

        if (!(method.equals("GET") || method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("DELETE")))
            throw new HTTPException(400, HTTPCodes.C400);

        parseHeaders();
        if(!timeout)
            parseBody();

        if(timeout)
            throw new HTTPException(408, HTTPCodes.C408);

        timeoutFuture.cancel(true);
    }

    private void parseHeaders() throws IOException, HTTPException {
        String line;

        while (!timeout && ((line = reader.readLine()) == null || !line.isBlank())) {
            if (line == null)
                throw new IOException("Client closed while sending headers");

            String[] splitHeader = line.split(":", 2); // 2-split only on first :
            if (splitHeader.length != 2)
                throw new HTTPException(400, HTTPCodes.C400);

            headersMap.put(splitHeader[0].strip(), splitHeader[1].strip());
        }
    }

    private void parseBody() throws HTTPException, IOException {
        if (!headersMap.containsKey("Content-Length") && (method.equals("POST") || method.equals("PATCH") || method.equals("PUT")))
            throw new HTTPException(411, HTTPCodes.C411);

        StringBuilder stringBuilder = new StringBuilder();
        int contentLen = Integer.parseInt(headersMap.getOrDefault("Content-Length", "0"));
        final int BUFFER_SIZE = 1024;
        char[] buff = new char[BUFFER_SIZE];
        while (!timeout && contentLen > 0) {
            int readLen = reader.read(buff, 0, Math.min(contentLen, BUFFER_SIZE));
            if (readLen == -1)
                throw new IOException("Client closed before end of body");
            contentLen -= readLen;
            stringBuilder.append(buff, 0, readLen);
        }

        body = stringBuilder.toString();
    }

    private void setTimeout(){
        timeoutFuture = timeoutExecutor.schedule(() -> {
            if(socket.isClosed())
                return;
            try {
                socket.shutdownInput();
            } catch (IOException ignored) {}
            timeout = true;
        }, readTimeout, TimeUnit.MILLISECONDS);
    }

    public String getMethod() {
        return method;
    }

    public String getURI() {
        return uri;
    }

    public HashMap<String, String> getHeaders() {
        return headersMap;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return method + " " + uri;
    }

    private final BufferedReader reader;
    private String method;
    private String uri;
    private String body;
    private final HashMap<String, String> headersMap;
    private ScheduledFuture<?> timeoutFuture;
    private final ScheduledExecutorService timeoutExecutor;
    private boolean timeout = false;
    private final int readTimeout;
}

package eu.piotro.rest2api.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 *  Reads and parses HTTP requests from {@link Socket}
 */
public class HTTPReader {
    /**
     * Creates new HTTP request reader (read via {@link #read()})
     * @param reader reader of inet socket
     * @param socket {@link Socket} to read from
     * @param timeoutExecutor {@link ScheduledExecutorService} to schedule timeout events
     * @param readTimeout time in milliseconds to timeout request reading
     */
    public HTTPReader(BufferedReader reader, Socket socket, ScheduledExecutorService timeoutExecutor, int readTimeout) {
        this.reader = reader;
        this.socket = socket;
        this.timeoutExecutor = timeoutExecutor;
        this.readTimeout = readTimeout;
    }

    Socket socket;

    /**
     * Reads and parses HTTP requests
     * @throws IOException if I/O exception while reading from socket
     * @throws HTTPException if request is invalid (4xx HTTP errors)
     * @return Parsed HTTP request
     */
    public HTTPRequest read() throws IOException, HTTPException {
        if(timeout)
            throw new HTTPException(408, HTTPCodes.C408);

        String requestLine = reader.readLine();

        if(timeout)
            throw new HTTPException(408, HTTPCodes.C408);

        if (requestLine == null)
            throw new IOException("No data sent to socket");

        String[] splitRequest = requestLine.split(" +");

        if (splitRequest.length != 3)  //GET / HTTP/1.1
            throw new HTTPException(400, HTTPCodes.C400);

        String method = splitRequest[0];
        String uri = splitRequest[1];
        String proto = splitRequest[2];

        if (!proto.equals("HTTP/1.1") && splitRequest[2].contains("HTTP/"))
            throw new HTTPException(505, HTTPCodes.C505);
        else if (!proto.equals("HTTP/1.1"))
            throw new HTTPException(400, HTTPCodes.C400);

        if (!(method.equals("GET") || method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("DELETE")))
            throw new HTTPException(400, HTTPCodes.C400);

        HashMap<String, String> headersMap = parseHeaders();

        if (!headersMap.containsKey("Content-Length") && (method.equals("POST") || method.equals("PATCH") || method.equals("PUT")))
            throw new HTTPException(411, HTTPCodes.C411);

        String body = null;
        if(!timeout)
            body = parseBody(headersMap);

        if(timeout)
            throw new HTTPException(408, HTTPCodes.C408);

        return new HTTPRequest(method, uri, headersMap, body, socket);
    }

    private HashMap<String, String> parseHeaders() throws IOException, HTTPException {
        HashMap<String, String> headersMap = new HashMap<>();
        String line;

        while (!timeout && ((line = reader.readLine()) == null || !line.isBlank())) {
            if(timeout)
                break;
            if (line == null)
                throw new IOException("Client closed while sending headers");

            String[] splitHeader = line.split(":", 2); // 2-split only on first :
            if (splitHeader.length != 2)
                throw new HTTPException(400, HTTPCodes.C400);

            headersMap.put(splitHeader[0].strip(), splitHeader[1].strip());
        }
        return headersMap;
    }

    private String parseBody(HashMap<String, String> headersMap) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int contentLen = Integer.parseInt(headersMap.getOrDefault("Content-Length", "0"));
        final int BUFFER_SIZE = 1024;
        char[] buff = new char[BUFFER_SIZE];
        while (!timeout && contentLen > 0) {
            int readLen = reader.read(buff, 0, Math.min(contentLen, BUFFER_SIZE));
            if(timeout)
                break;
            if (readLen == -1)
                throw new IOException("Client closed before end of body");
            contentLen -= readLen;
            stringBuilder.append(buff, 0, readLen);
        }

        return stringBuilder.toString();
    }

    /**
     * Schedule timeout event for connection checked by {@link #read()}
     */
    public void setTimeout() {
        timeoutFuture = timeoutExecutor.schedule(() -> {
            if(socket.isClosed())
                return;
            try {
                socket.shutdownInput();
            } catch (IOException ignored) {}
            timeout = true;
        }, readTimeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Cancel scheduled timeout event
     */
    public void cancelTimeout() {
        if(timeoutFuture != null)
            timeoutFuture.cancel(true);
    }


    private final BufferedReader reader;
    private ScheduledFuture<?> timeoutFuture;
    private final ScheduledExecutorService timeoutExecutor;
    private boolean timeout = false;
    private final int readTimeout;
}

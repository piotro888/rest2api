package eu.piotro.rest2api.http;

import java.net.Socket;
import java.util.HashMap;

/**
 * Represents HTTP request
 */
public class HTTPRequest {
    /**
     * Creates new HTTP request
     * @param method Request method
     * @param uri Request URI
     * @param headersMap Map containing request headers
     * @param body Request body (may be null)
     * @param socket Socket source of request
     */
    public HTTPRequest(String method, String uri, HashMap<String, String> headersMap, String body, Socket socket) {
        this.method = method;
        this.uri = uri;
        this.headersMap = headersMap;
        this.body = body;
        this.socket = socket;
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

    public Socket getSocket() {
        return  socket;
    }

    @Override
    public String toString() {
        return method + " " + uri;
    }

    private final Socket socket;
    private final String method;
    private final String uri;
    private final String body;
    private final HashMap<String, String> headersMap;

}

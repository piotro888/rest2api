package eu.piotro.rest2api.http;

/**
 * Represents response to HTTP request
 */
public class HTTPResponse {
    /**
     * Creates response with additional headers
     * @param code HTTP status code
     * @param codeDescription HTTP code description (use {@link HTTPCodes}) interface
     * @param type MIME type of returned body
     * @param headers additional headers to send with response
     * @param body HTTP response body
     */
    public HTTPResponse(int code, String codeDescription, String type, String headers, String body){
        this.code = code;
        this.codeDescription = codeDescription;
        this.type = type;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Creates response to HTTP request
     * @param code HTTP status code
     * @param codeDescription HTTP code description (use {@link HTTPCodes}) interface
     * @param type MIME type of returned body
     * @param body HTTP response body
     */
    public HTTPResponse(int code, String codeDescription, String type, String body){
        this(code, codeDescription, type, "", body);
    }

    public String getType() {
        return type;
    }

    public String getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public int getCode(){
        return code;
    }

    public String getCodeDescription(){
        return codeDescription;
    }

    private final int code;
    private final String codeDescription;
    private final String type;
    private final String body;
    private final String headers;

}

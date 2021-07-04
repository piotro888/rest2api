package eu.piotro.rest2api.http;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Represents response to HTTP request
 */
public class HTTPResponse {
    /**
     * Creates response to HTTP request from byte array
     * @param code HTTP status code
     * @param codeDescription HTTP code description (use {@link HTTPCodes}) interface
     * @param type MIME type of returned body
     * @param headers additional headers to send with response (separated with "\r\n", no separator at last header / end)
     * @param body HTTP response body
     */
    public HTTPResponse(int code, String codeDescription, String type, String headers, byte[] body){
        this.code = code;
        this.codeDescription = codeDescription;
        this.type = type;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Creates response with additional headers
     * @param code HTTP status code
     * @param codeDescription HTTP code description (use {@link HTTPCodes}) interface
     * @param type MIME type of returned body (may include ;charset=)
     * @param headers additional headers to send with response (separated with "\r\n", no separator at last header / end)
     * @param body HTTP response body
     * @param charset Encoding for body string. If null charset is ISO-8859-1 (http default)
     */
    public HTTPResponse(int code, String codeDescription, String type, String headers, String body, Charset charset){
        this(code, codeDescription, type, headers, body.getBytes(Objects.requireNonNullElse(charset, StandardCharsets.ISO_8859_1)));
    }

    /**
     * Creates response to HTTP request
     * @param code HTTP status code
     * @param codeDescription HTTP code description (use {@link HTTPCodes}) interface
     * @param type MIME type of returned body
     * @param body HTTP response body
     */
    public HTTPResponse(int code, String codeDescription, String type, String body){
        this(code, codeDescription, type, "", body, null);
    }

    /**
     * Creates response to HTTP request
     * @param code HTTP status code
     * @param codeDescription HTTP code description (use {@link HTTPCodes}) interface
     * @param type MIME type of returned body (may include ;charset=)
     * @param body HTTP response body
     * @param charset Encoding for body string. If null charset is ISO-8859-1 (http default)
     */
    public HTTPResponse(int code, String codeDescription, String type, String body, Charset charset){
        this(code, codeDescription, type, "", body, charset);
    }


    public String getType() {
        return type;
    }

    public String getHeaders() {
        return headers;
    }

    public byte[] getBody() {
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
    private final byte[] body;
    private final String headers;

}

package eu.piotro.rest2api.http;

/**
 * Represents HTTP error response with status code, message and optionally headers, which may be thrown as Exception
 */
public class HTTPException extends Exception {

    /**
     * Create HTTP exception
     * @param code HTTP status code
     * @param message status code description
     */
    public HTTPException(int code, String message){
        this(code, message, "");
    }

    /**
     * Create HTTP exception with additional headers
     * @param code HTTP status code
     * @param message status code description
     * @param headers additional headers to include with response
     */
    public HTTPException(int code, String message, String headers){
        super(message);
        this.code = code;
        this.headers = headers;
    }

    public int getCode() {
        return code;
    }
    public String getHeaders() {
        return headers;
    }

    @Override
    public String toString(){
        return code + " " + super.getMessage();
    }

    private final int code;
    private final String headers;
}

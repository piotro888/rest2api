package eu.piotro.rest2api.http;

/**
 * Represents HTTP exception handler for use in server (allows for custom error responses)
 * @see eu.piotro.rest2api.tcp.ConnectionHandler
 * @since 1.1
 */
public interface HTTPExceptionHandler {
    /**
     * Takes {@link HTTPException} and returns {@link HTTPResponse} used to respond to client
     * @param e exception to process
     * @return error message page as {@link HTTPResponse}
     */
    HTTPResponse handleHTTPException(HTTPException e);
}

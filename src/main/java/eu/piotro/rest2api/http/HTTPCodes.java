package eu.piotro.rest2api.http;

/**
 * HTTP status codes descriptions
 */
public interface HTTPCodes {
    String C200 = "OK";
    String C404 = "Not Found";
    String C400 = "Bad Request";
    String C505 = "HTTP Version Not Supported";
    String C411 = "Length Required";
    String C405 = "Method Not Allowed";
    String C500 = "Internal Server Error";
    String C408 = "Request Timeout";
}

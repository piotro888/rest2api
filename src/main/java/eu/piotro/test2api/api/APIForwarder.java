package eu.piotro.test2api.api;

import eu.piotro.test2api.api.annotations.AnnotationsMagic;
import eu.piotro.test2api.http.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Routes HTTP request to registered handlers
 * @see eu.piotro.test2api.api.annotations.RESTHandler
 * @since 1.0
 */
public class APIForwarder {
    /**
     * Forwards HTTP Request to registered handler
     * @param request Request to forward
     * @return HTTP response from handler
     * @throws HTTPException if HTTPException was threw by handler or own HTTPException ex. URI was not found.
     */
    public HTTPResponse forward(HTTPRequest request) throws HTTPException {

        List<MethodURIPair> uriMatched = map.keySet().stream()
                .filter(keyPair -> request.getURI().matches(keyPair.getURI()))
                .collect(Collectors.toList());

        if(uriMatched.isEmpty())
            throw new HTTPException(404, HTTPCodes.C404);

        Optional<MethodURIPair> match = uriMatched.stream()
                .filter(keyPair -> keyPair.getMethod().equals(request.getMethod()))
                .findFirst();
        if(match.isEmpty()){
            String acceptedMethods = "Allow: " + uriMatched.stream().map(MethodURIPair::getMethod).collect(Collectors.joining(", "));
            throw new HTTPException(405, HTTPCodes.C405, acceptedMethods);
        }

        APIHandler handler = map.get(match.get());
        return handler.handle(request);
    }

    /**
     * Register single APIHandler (Lambda may be used to pass single function)
     * @param method method to register ex. GET, POST, PUT, DELETE
     * @param regex URI to register (supports regex)
     * @param handler APIHandler to register
     */
    public void register(String method, String regex, APIHandler handler){
        map.put(new MethodURIPair(method, regex), handler);
    }


    /**
     * Registers annotated dynamic and static methods in class.
     * @param classObject class object to register.
     * @see AnnotationsMagic#registerClass(Class, APIForwarder, Object)
     * @see eu.piotro.test2api.api.annotations
     * @since 1.3
     */
    public void registerClass(Object classObject){
        AnnotationsMagic.registerClass(classObject.getClass(), this, classObject);
    }

    /**
     * Registers annotated static methods in class.
     * @param classToRegister class to register.
     * @see APIForwarder#registerClass(Object) 
     * @see AnnotationsMagic#registerClass(Class, APIForwarder, Object)
     * @see eu.piotro.test2api.api.annotations
     */
    public void registerClassStatic(Class<?> classToRegister){
        AnnotationsMagic.registerClass(classToRegister, this, null);
    }

    private final Map<MethodURIPair, APIHandler> map = new HashMap<>();
    private HTTPExceptionHandler httpExceptionHandler;

    /**
     * Set custom {@link HTTPExceptionHandler} used to show custom HTTP Exception (HTTP error threw by server ex. 404) message page.
     * @param handler HTTP exception handler
     */
    public void setHTTPExceptionHandler(HTTPExceptionHandler handler){
        this.httpExceptionHandler = handler;
    }

    public HTTPExceptionHandler getHTTPExceptionHandler(){
        return httpExceptionHandler;
    }

    private static class MethodURIPair {
        private MethodURIPair(String method, String uri){
            this.method = method;
            this.uri = uri;
        }

        public String getMethod() { return method; }
        public String getURI() { return uri; }

        private final String method, uri;
    }

}



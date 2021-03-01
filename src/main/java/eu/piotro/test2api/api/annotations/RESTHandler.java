package eu.piotro.test2api.api.annotations;

import java.lang.annotation.*;

/**
 * Annotation for REST API handler functions
 * Consists of URI (regex supported) to which handler is assigned and HTTP method. Annotated functions should match {@link eu.piotro.test2api.api.APIHandler}
 * format (have one parameter of type {@link eu.piotro.test2api.http.HTTPRequest} and return {@link eu.piotro.test2api.http.HTTPResponse}.
 * Methods may throw {@link eu.piotro.test2api.http.HTTPException} (quick way for responding with HTTP errors) or return HTTPResponse
 * with error. Classes with handlers must be registered via {@link eu.piotro.test2api.api.APIForwarder#registerClass(Object)} or {@link eu.piotro.test2api.api.APIForwarder#registerClassStatic(Class)}.
 * @since 1.1
 * @see eu.piotro.test2api.api.annotations
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RESTHandler {
    /**
     * @return HTTP method of request
     */
    String method();
    /**
     * @return URI to which handler is assigned
     */
    String URI();
}

package flatflow.service;

/**
 * An error the user should see. The web layer turns it into an HTTP status code and
 * {"error": "..."} (D1 I/O spec, "Errors" table: 400, 401, 403, 404, 409).
 */
public class ApiException extends RuntimeException {
    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() { return status; }

    public static ApiException badRequest(String msg)   { return new ApiException(400, msg); }
    public static ApiException unauthorized(String msg) { return new ApiException(401, msg); }
    public static ApiException forbidden(String msg)    { return new ApiException(403, msg); }
    public static ApiException notFound(String msg)     { return new ApiException(404, msg); }
    public static ApiException conflict(String msg)     { return new ApiException(409, msg); }
}

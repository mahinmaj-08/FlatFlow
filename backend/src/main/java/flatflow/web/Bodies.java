package flatflow.web;

import flatflow.service.ApiException;
import io.javalin.http.Context;

/** Reads a JSON request body into a record, with a friendly error if the JSON is broken. */
final class Bodies {
    private Bodies() {}

    static <T> T read(Context ctx, Class<T> type) {
        if (ctx.body().isBlank()) throw ApiException.badRequest("Request body is empty");
        try {
            return ctx.bodyAsClass(type);
        } catch (Exception e) {
            throw ApiException.badRequest("Request body is not valid JSON for this request");
        }
    }
}

package io.tohuwabohu.kamifusen.ssr.response

import io.quarkus.logging.Log
import io.smallrye.common.annotation.CheckReturnValue
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.groups.UniOnFailure
import io.tohuwabohu.kamifusen.ssr.createErrorDiv
import jakarta.ws.rs.core.Response

/**
 * Creates an HTMX response containing an error message based on the given status.
 * The error message is rendered on a <div> element with id `"error"`.
 *
 * @param status The HTTP status to be included in the error response.
 * @return A `Uni` that emits the `Response` containing the HTML error message.
 */
fun createHtmxErrorResponse(status: Response.Status): Uni<Response> {
    return Uni.createFrom().item(Response.status(Response.Status.PARTIAL_CONTENT).entity(createErrorDiv { status }).build())
}

/**
 * Recovers from a failure by returning a structured HTMX error response. Logs the exception
 * and wraps the result of `createHtmxErrorResponse` into a `Response`.
 *
 * @param status The HTTP status to be included in the error response.
 * @return A `Uni` instance that emits a `Response` containing the HTMX error message.
 */
@CheckReturnValue
fun UniOnFailure<Response>.recoverWithHtmxResponse(status: Response.Status): Uni<Response> {
    return this.invoke { e -> Log.error("Error occured: ${status.statusCode}", e) }.onFailure()
        .recoverWithUni(createHtmxErrorResponse(status))
}

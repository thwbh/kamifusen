package io.tohuwabohu.kamifusen.crud.error

import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.groups.UniOnFailure
import jakarta.ws.rs.core.Response

/**
 * Recovers from any failure of the [Uni] by creating an error response based on the type of the thrown exception.
 *
 * Usage: /*...*/.onFailure().recoverWithResponse()
 *
 * Recovers with 200 OK by default.
 *
 * @return a [Uni] that emits a [Response] representing the recovered error response.
 */
fun UniOnFailure<Response>.recoverWithResponse(): Uni<Response> {
    return this.invoke { e -> Log.error("Recovering from Exception...", e) }
        .onFailure().recoverWithItem(Response.ok().build())
}
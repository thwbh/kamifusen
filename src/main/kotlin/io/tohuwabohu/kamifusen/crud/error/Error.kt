package io.tohuwabohu.kamifusen.crud.error

import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.groups.UniOnFailure
import jakarta.persistence.EntityNotFoundException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.core.Response

/**
 * Recovers from any failure of the [Uni] by creating an error response based on the type of the thrown exception.
 *
 * Usage: /*...*/.onFailure().recoverWithResponse()
 *
 * @return a [Uni] that emits a [Response] representing the recovered error response.
 */
fun UniOnFailure<Response>.recoverWithResponse(): Uni<Response> {
    return this.invoke { throwable ->
        Log.error("Recovering from Exception...", throwable)

        when (throwable) {
            is NotFoundException -> {
                Response.status(404).build()
            }

            is EntityNotFoundException -> {
                Response.status(404).build()
            }

            else -> {
                Response.status(500).build()
            }
        }
    }
}

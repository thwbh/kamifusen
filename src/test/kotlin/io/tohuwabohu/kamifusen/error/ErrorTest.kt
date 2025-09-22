package io.tohuwabohu.kamifusen.error

import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.Uni
import jakarta.persistence.EntityNotFoundException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.core.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@QuarkusTest
class ErrorTest {

    @Test
    fun `should recover with 404 for NotFoundException`() {
        val result = Uni.createFrom().failure<Response>(NotFoundException("Not found"))
            .onFailure().recoverWithItem { throwable ->
                when (throwable) {
                    is NotFoundException -> Response.status(404).build()
                    is EntityNotFoundException -> Response.status(404).build()
                    else -> Response.status(500).build()
                }
            }
            .await().indefinitely()

        assertEquals(404, result.status)
    }

    @Test
    fun `should recover with 404 for EntityNotFoundException`() {
        val result = Uni.createFrom().failure<Response>(EntityNotFoundException("Entity not found"))
            .onFailure().recoverWithItem { throwable ->
                when (throwable) {
                    is NotFoundException -> Response.status(404).build()
                    is EntityNotFoundException -> Response.status(404).build()
                    else -> Response.status(500).build()
                }
            }
            .await().indefinitely()

        assertEquals(404, result.status)
    }

    @Test
    fun `should recover with 500 for generic exception`() {
        val result = Uni.createFrom().failure<Response>(RuntimeException("Generic error"))
            .onFailure().recoverWithItem { throwable ->
                when (throwable) {
                    is NotFoundException -> Response.status(404).build()
                    is EntityNotFoundException -> Response.status(404).build()
                    else -> Response.status(500).build()
                }
            }
            .await().indefinitely()

        assertEquals(500, result.status)
    }

    @Test
    fun `should recover with 500 for IllegalArgumentException`() {
        val result = Uni.createFrom().failure<Response>(IllegalArgumentException("Invalid argument"))
            .onFailure().recoverWithItem { throwable ->
                when (throwable) {
                    is NotFoundException -> Response.status(404).build()
                    is EntityNotFoundException -> Response.status(404).build()
                    else -> Response.status(500).build()
                }
            }
            .await().indefinitely()

        assertEquals(500, result.status)
    }

    @Test
    fun `should recover with 500 for NullPointerException`() {
        val result = Uni.createFrom().failure<Response>(NullPointerException("Null pointer"))
            .onFailure().recoverWithItem { throwable ->
                when (throwable) {
                    is NotFoundException -> Response.status(404).build()
                    is EntityNotFoundException -> Response.status(404).build()
                    else -> Response.status(500).build()
                }
            }
            .await().indefinitely()

        assertEquals(500, result.status)
    }

    @Test
    fun `should pass through successful response`() {
        val successResponse = Response.ok("Success").build()

        val result = Uni.createFrom().item(successResponse)
            .onFailure().recoverWithResponse()
            .await().indefinitely()

        assertEquals(200, result.status)
        assertEquals("Success", result.entity)
    }
}
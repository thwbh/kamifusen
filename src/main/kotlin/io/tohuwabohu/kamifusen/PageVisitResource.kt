package io.tohuwabohu.kamifusen

import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.error.recoverWithResponse
import io.tohuwabohu.kamifusen.service.PageVisitService
import io.tohuwabohu.kamifusen.service.VisitRequestMapper
import io.tohuwabohu.kamifusen.service.dto.PageHitRequestDto
import io.vertx.core.http.HttpServerRequest
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.util.*

@Path("/public/visits")
class PageVisitResource(
    private val pageVisitService: PageVisitService,
    private val requestMapper: VisitRequestMapper
) {
    @Path("/hit")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("api-user")
    fun hit(
        @Context request: HttpServerRequest,
        body: PageHitRequestDto
    ): Uni<Response> {
        Log.debug("Received page hit request for ${body.domain}${body.path}")

        return requestMapper.validatePageHitRequest(body).chain { validationErrors ->
            if (validationErrors != null) {
                Log.warn("Invalid page hit request: ${validationErrors.joinToString(", ")}")
                return@chain Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                        .entity(mapOf("errors" to validationErrors))
                        .build()
                )
            }

            requestMapper.mapToVisitContext(request, body).chain { context ->
                pageVisitService.processPageHit(context)
                    .map { result ->
                        Log.debug("Page hit processed successfully: ${result.visitCount} visits")
                        Response.ok(result.visitCount).build()
                    }
            }
        }.onFailure().recoverWithResponse()
    }

    @Path("/count/{pageId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("api-admin")
    fun count(@PathParam("pageId") pageId: UUID): Uni<Response> {
        Log.debug("Received visit count request for page $pageId")

        return pageVisitService.getVisitCount(pageId)
            .map { count -> Response.ok(count).build() }
            .onFailure().recoverWithResponse()
    }
}
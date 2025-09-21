package io.tohuwabohu.kamifusen

import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.api.generated.PageVisitResourceApi
import io.tohuwabohu.kamifusen.api.generated.model.PageHitRequestDto
import io.tohuwabohu.kamifusen.error.recoverWithResponse
import io.tohuwabohu.kamifusen.service.PageVisitService
import io.tohuwabohu.kamifusen.service.mapper.VisitRequestMapper
import io.vertx.core.http.HttpServerRequest
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.util.*

@Path("/public/visits")
class PageVisitResource(
    private val pageVisitService: PageVisitService,
    private val requestMapper: VisitRequestMapper
): PageVisitResourceApi {
    @Inject
    lateinit var request: HttpServerRequest

    @Path("/hit")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("api-user")
    override fun hit(
        pageHitRequestDto: PageHitRequestDto
    ): Uni<Response> {
        Log.debug("Received page hit request for ${pageHitRequestDto.domain}${pageHitRequestDto.path}")

        return requestMapper.validatePageHitRequest(pageHitRequestDto).chain { validationErrors ->
            if (validationErrors != null) {
                Log.warn("Invalid page hit request: ${validationErrors.joinToString(", ")}")
                return@chain Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                        .entity(mapOf("errors" to validationErrors))
                        .build()
                )
            }

            requestMapper.mapToVisitContext(request, pageHitRequestDto).chain { context ->
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
    override fun count(@PathParam("pageId") pageId: UUID): Uni<Response> {
        Log.debug("Received visit count request for page $pageId")

        return pageVisitService.getVisitCount(pageId)
            .map { count -> Response.ok(count).build() }
            .onFailure().recoverWithResponse()
    }
}
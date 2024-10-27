package io.tohuwabohu.kamifusen

import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.helpers.spies.Spy.onItem
import io.smallrye.mutiny.tuples.Tuple2
import io.tohuwabohu.kamifusen.crud.PageRepository
import io.tohuwabohu.kamifusen.crud.PageVisitRepository
import io.tohuwabohu.kamifusen.crud.VisitorRepository
import io.tohuwabohu.kamifusen.crud.error.recoverWithResponse
import io.vertx.core.http.HttpServerRequest
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext
import java.net.URLDecoder
import java.nio.charset.Charset

@Path("/public/visits")
class PageVisitResource(
    private val pageRepository: PageRepository,
    private val pageVisitRepository: PageVisitRepository,
    private val visitorRepository: VisitorRepository
) {
    @Path("/hit")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("api-user")
    fun hit(@Context securityContext: SecurityContext, @Context request: HttpServerRequest, body: String): Uni<Response> =
        pageRepository.findPageByPath(body).flatMap { page ->
            visitorRepository.findByInfo(
                remoteAddress = request.remoteAddress().host(),
                userAgent = request.headers().get("User-Agent") ?: "unknown"
            ).map { visitor ->
                Tuple2.of(page, visitor)
            }
        }.chain { tuple ->
            val page = tuple.item1!!
            val visitor = tuple.item2

            if (visitor == null) {
                visitorRepository.addVisitor(
                    remoteAddress = request.remoteAddress().host(),
                    userAgent = request.headers().get("User-Agent") ?: "unknown"
                ).chain { newVisitor -> pageVisitRepository.addVisit(page.id, newVisitor.id) }
            } else {
                pageVisitRepository.countVisitsForVisitor(page.id, visitor.id).chain { count ->
                    if (count <= 0) {
                        pageVisitRepository.addVisit(page.id, visitor.id)
                    } else Uni.createFrom().voidItem()
                }
            }
        }.onItem().transform { Response.ok().build() }
            .onFailure().recoverWithResponse()

    @Path("/count/{pagePath}")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("api-user")
    fun count(@Context securityContext: SecurityContext, @Context request: HttpServerRequest, @PathParam("pagePath") pagePath: String): Uni<Response> =
        pageRepository.findPageByPath(URLDecoder.decode(pagePath,
            request.headers().get("Accept-Charset")?.let { Charset.forName(it) } ?: Charsets.UTF_8)
        ).chain { page ->
            if (page != null) {
                pageVisitRepository.countVisits(page.id).map { visits ->
                    Response.ok(visits).build()
                }
            } else Uni.createFrom().item(Response.status(404).build())
        }.onFailure().recoverWithResponse()
}
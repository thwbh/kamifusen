package io.tohuwabohu.kamifusen

import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.tuples.Tuple2
import io.tohuwabohu.kamifusen.crud.*
import io.tohuwabohu.kamifusen.crud.error.recoverWithResponse
import io.vertx.core.http.HttpServerRequest
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/visits")
class PageVisitResource(
    private val pageRepository: PageRepository,
    private val pageVisitRepository: PageVisitRepository,
    private val visitorRepository: VisitorRepository
) {
    @Path("/hit")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    fun hit(@Context request: HttpServerRequest, body: String): Uni<Response> =
        pageRepository.findPageByPath(body).flatMap { page ->
            visitorRepository.findByInfo(request.remoteAddress().host()).map { visitor ->
                Tuple2.of(page, visitor)
            }
        }.chain { tuple ->
            val page = tuple.item1!!
            val visitor = tuple.item2

            if (visitor == null) {
                visitorRepository.addVisitor(request.remoteAddress().host())
                    .chain { newVisitor -> pageVisitRepository.addVisit(page.id, newVisitor.id) }
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
    fun count(@Context request: HttpServerRequest, @PathParam("pagePath") pagePath: String): Uni<Response> =
        pageRepository.findPageByPath(pagePath).chain { page ->
            pageVisitRepository.countVisits(page!!.id).map { visits ->
                 Response.ok(visits).build()
            }
        }.onFailure().recoverWithResponse()
}
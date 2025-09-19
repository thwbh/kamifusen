package io.tohuwabohu.kamifusen

import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.tuples.Tuple2
import io.tohuwabohu.kamifusen.crud.PageRepository
import io.tohuwabohu.kamifusen.crud.PageVisitRepository
import io.tohuwabohu.kamifusen.crud.VisitorRepository
import io.tohuwabohu.kamifusen.crud.SessionRepository
import io.tohuwabohu.kamifusen.crud.dto.PageHitDto
import io.tohuwabohu.kamifusen.crud.error.recoverWithResponse
import io.vertx.core.http.HttpServerRequest
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext
import java.util.*

@Path("/public/visits")
class PageVisitResource(
    private val pageRepository: PageRepository,
    private val pageVisitRepository: PageVisitRepository,
    private val visitorRepository: VisitorRepository,
    private val sessionRepository: SessionRepository
) {
    @Path("/hit")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("api-user")
    fun hit(
        @Context securityContext: SecurityContext,
        @Context request: HttpServerRequest,
        body: PageHitDto
    ): Uni<Response> {
        val remoteAddress = request.remoteAddress().host()
        val userAgent = request.headers().get("User-Agent") ?: "unknown"
        val referrer = request.headers().get("Referer")
        // Note: For country detection, you'd typically use a GeoIP service
        // For now, we'll leave it null but the infrastructure is in place
        val country: String? = null

        return pageRepository.addPageIfAbsent(
            path = body.path,
            domain = body.domain
        ).flatMap { page ->
            visitorRepository.findByInfo(remoteAddress, userAgent)
                .map { visitor -> Tuple2.of(page, visitor) }
        }.chain { tuple ->
            val page = tuple.item1!!
            val visitor = tuple.item2

            if (visitor == null) {
                // Create new visitor with enhanced data
                visitorRepository.addVisitor(
                    remoteAddress = remoteAddress,
                    userAgent = userAgent,
                    referrer = referrer,
                    country = country
                ).flatMap { newVisitor ->
                    // Add the page visit and start new session (new visitor = no recent activity)
                    pageVisitRepository.addVisit(page.id, newVisitor.id)
                        .flatMap { _ ->
                            // New visitor always starts a new session
                            sessionRepository.findOrCreateSessionForVisitor(newVisitor.id, hasRecentActivity = false)
                                .flatMap { session ->
                                    sessionRepository.incrementPageViews(session.id)
                                }
                                .onFailure().recoverWithItem { _: Throwable -> null }
                                .onItem().transform { _ -> page }
                        }
                }
            } else {
                // Update last seen time and handle page visit using sliding window approach
                visitorRepository.updateLastSeen(visitor.id)
                    .onFailure().recoverWithItem(visitor) // Continue even if updateLastSeen fails
                    .flatMap { _ ->
                        pageVisitRepository.countVisitsForVisitor(page.id, visitor.id)
                            .flatMap { count ->
                                if (count <= 0) {
                                    // Add new page visit - check for recent activity to determine session handling
                                    pageVisitRepository.findRecentVisitByVisitorOnDomain(visitor.id, body.domain, 30)
                                        .flatMap { recentVisit ->
                                            val hasRecentActivity = recentVisit != null

                                            // Add the visit first
                                            pageVisitRepository.addVisit(page.id, visitor.id)
                                                .flatMap { _ ->
                                                    // Handle session based on recent activity
                                                    sessionRepository.findOrCreateSessionForVisitor(visitor.id, hasRecentActivity)
                                                        .flatMap { session ->
                                                            sessionRepository.incrementPageViews(session.id)
                                                        }
                                                        .onFailure().recoverWithItem { _: Throwable -> null }
                                                        .onItem().transform { _ -> page }
                                                }
                                        }
                                } else {
                                    Uni.createFrom().item(page)
                                }
                            }
                    }
            }
        }.flatMap { page ->
            pageVisitRepository.countVisits(page.id)
        }.onItem().transform { count -> Response.ok(count).build() }
            .onFailure().recoverWithResponse()
    }

    @Path("/count/{pageId}")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("api-admin")
    fun count(
        @Context securityContext: SecurityContext,
        @Context request: HttpServerRequest,
        @PathParam("pageId") pageId: UUID
    ): Uni<Response> =
        pageRepository.findByPageId(pageId).chain { page ->
            if (page != null) {
                pageVisitRepository.countVisits(page.id).map { visits ->
                    Response.ok(visits).build()
                }
            } else Uni.createFrom().item(Response.status(404).build())
        }.onFailure().recoverWithResponse()
}
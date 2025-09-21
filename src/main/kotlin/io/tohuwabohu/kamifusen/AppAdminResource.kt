package io.tohuwabohu.kamifusen

import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.ApiUser
import io.tohuwabohu.kamifusen.crud.ApiUserRepository
import io.tohuwabohu.kamifusen.service.PageAdminService
import io.tohuwabohu.kamifusen.service.dto.PageWithStatsDto
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import io.tohuwabohu.kamifusen.crud.PageRepository
import io.tohuwabohu.kamifusen.service.dto.AggregatedStatsDto
import io.tohuwabohu.kamifusen.service.dto.PageVisitDtoRepository
import io.tohuwabohu.kamifusen.service.dto.StatsRepository
import io.tohuwabohu.kamifusen.crud.error.recoverWithResponse
import io.tohuwabohu.kamifusen.crud.security.*
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.*
import jakarta.ws.rs.core.*
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.LocalDateTime
import java.util.*


@Path("/admin")
class AppAdminResource(
    private val apiUserRepository: ApiUserRepository,
    private val pageVisitDtoRepository: PageVisitDtoRepository,
    private val pageRepository: PageRepository,
    private val statsRepository: StatsRepository,
    private val pageAdminService: PageAdminService
) {
    @Path("/keygen")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("app-admin")
    fun generateApiKey(
        @FormParam("username") username: String,
        @FormParam("role") role: String,
        @FormParam("expiresAt") expiresAt: String
    ): Uni<Response> =
        validateUser(username, apiUserRepository).flatMap { result ->
            if (result == UserValidation.VALID) {
                apiUserRepository.addUser(
                    ApiUser(
                        username = username,
                        role = role,
                        expiresAt = when (expiresAt) {
                            "" -> null
                            else -> LocalDateTime.parse(expiresAt)
                        },
                    )
                ).map { keyRaw -> Response.ok(keyRaw).build() }
            } else {
                Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).entity("User validation failed: $result").build())
            }
        }.onFailure().invoke { e -> Log.error("Error during keygen.", e) }
            .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build())

    @Path("/stats")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(
        responseCode = "200",
        description = "Aggregated stats",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = AggregatedStatsDto::class)
        )]
    )
    @RolesAllowed("app-admin")
    fun getAggregatedStats(@QueryParam("timeRange") timeRange: String?): Uni<Response> =
        statsRepository.getAggregatedStats(timeRange ?: "7d")
            .map { stats -> Response.ok(stats).build() }
            .onFailure().invoke { e -> Log.error("Error receiving aggregated stats.", e) }
            .onFailure().recoverWithResponse()

    @Path("/visits")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("app-admin")
    fun renderVisits(): Uni<Response> =
        pageVisitDtoRepository.getAllPageVisits()
            .flatMap {
                Uni.createFrom().item(Response.ok(it).build())
            }
            .onFailure().invoke { e -> Log.error("Error receiving visits.", e) }
            .onFailure().recoverWithResponse()

    @Path("/pages")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(
        responseCode = "200",
        description = "List of registered pages with visit statistics",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = PageWithStatsDto::class, type = SchemaType.ARRAY)
        )]
    )
    @RolesAllowed("app-admin")
    fun listPages(): Uni<Response> =
        pageAdminService.getPagesWithStats().flatMap {
            Uni.createFrom().item(Response.ok(it).build())
        }.onFailure().invoke { e -> Log.error("Error receiving pages.", e) }
            .onFailure().recoverWithResponse()

    @Path("/users")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(
        responseCode = "200",
        description = "List of API users",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ApiUser::class, type = SchemaType.ARRAY)
        )]
    )
    @RolesAllowed("app-admin")
    fun listUsers(): Uni<Response> =
        apiUserRepository.listAll().flatMap { users ->
            Uni.createFrom().item(Response.ok(users).build())
        }.onFailure().invoke { e -> Log.error("Error receiving users.", e) }
            .onFailure().recoverWithResponse()


    @Path("/retire/{userId}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("app-admin")
    fun retireApiKey(userId: UUID): Uni<Response> =
        apiUserRepository.expireUser(userId).onItem()
            .transform { Response.ok().build() }
            .onFailure().invoke { e -> Log.error("Error during key retirement", e) }
            .onFailure().recoverWithResponse()

    @Path("/pagedel/{pageId}")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("app-admin")
    fun unregisterPage(pageId: UUID): Uni<Response> =
        pageRepository.deletePage(pageId).map { Response.ok().build() }
            .onFailure().invoke { e -> Log.error("Error during page deletion.", e) }
            .onFailure().recoverWithResponse()
}
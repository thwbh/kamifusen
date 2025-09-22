package io.tohuwabohu.kamifusen

import io.quarkus.logging.Log
import io.quarkus.security.identity.SecurityIdentity
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.api.generated.AppAdminResourceApi
import io.tohuwabohu.kamifusen.api.generated.model.AggregatedStatsDto
import io.tohuwabohu.kamifusen.api.generated.model.PageWithStatsDto
import io.tohuwabohu.kamifusen.error.recoverWithResponse
import io.tohuwabohu.kamifusen.service.PageAdminService
import io.tohuwabohu.kamifusen.service.PageStatsService
import io.tohuwabohu.kamifusen.service.StatsService
import io.tohuwabohu.kamifusen.service.crud.ApiUser
import io.tohuwabohu.kamifusen.service.crud.ApiUserRepository
import io.tohuwabohu.kamifusen.service.crud.BlacklistRepository
import io.tohuwabohu.kamifusen.service.crud.PageRepository
import io.tohuwabohu.kamifusen.service.validator.UserValidation
import io.tohuwabohu.kamifusen.service.validator.validatePassword
import io.tohuwabohu.kamifusen.service.validator.validateUser
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import java.net.URI
import java.time.LocalDateTime
import java.util.*


@Path("/admin")
class AppAdminResource(
    private val apiUserRepository: ApiUserRepository,
    private val pageStatsService: PageStatsService,
    private val pageRepository: PageRepository,
    private val statsService: StatsService,
    private val pageAdminService: PageAdminService,
    private val blacklistRepository: BlacklistRepository
) : AppAdminResourceApi {
    @Inject
    lateinit var securityIdentity: SecurityIdentity

    @GET
    @Path("/landing")
    @RolesAllowed("app-admin")
    override fun adminLanding(): Uni<Response> {
        val username = securityIdentity.principal.name
        Log.info("Landing page accessed by user: $username")
        Log.info("Security identity attributes in landing: ${securityIdentity.attributes}")

        // Since attributes are lost, check if user still has default credentials
        return apiUserRepository.findByUsernameAndPassword(username, "admin")
            .map { _ ->
                Log.info("Redirecting to password change page")
                Response.seeOther(URI.create("/?page=change-password")).build()
            }.onFailure().recoverWithItem(Response.seeOther(URI.create("/?page=dashboard")).build())

    }

    @Path("/keygen")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("app-admin")
    override fun generateApiKey(
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
                Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST).entity("User validation failed: $result").build()
                )
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
    override fun getStats(@QueryParam("timeRange") timeRange: String?): Uni<Response> =
        statsService.getAggregatedStats(timeRange ?: "7d")
            .map { stats -> Response.ok(stats).build() }
            .onFailure().invoke { e -> Log.error("Error receiving aggregated stats.", e) }
            .onFailure().recoverWithResponse()

    @Path("/visits")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("app-admin")
    override fun getVisits(): Uni<Response> =
        pageStatsService.getAllPageVisits()
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
    override fun listPages(): Uni<Response> =
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
    override fun listUsers(): Uni<Response> =
        apiUserRepository.listAll().flatMap { users ->
            Uni.createFrom().item(Response.ok(users).build())
        }.onFailure().invoke { e -> Log.error("Error receiving users.", e) }
            .onFailure().recoverWithResponse()


    @Path("/retire/{userId}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("app-admin")
    override fun retireApiKey(userId: UUID): Uni<Response> =
        apiUserRepository.expireUser(userId).onItem()
            .transform { Response.ok().build() }
            .onFailure().invoke { e -> Log.error("Error during key retirement", e) }
            .onFailure().recoverWithResponse()

    @Path("/pagedel/{pageId}")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("app-admin")
    override fun unregisterPage(pageId: UUID): Uni<Response> =
        pageRepository.deletePage(pageId, blacklistRepository).map { Response.ok().build() }
            .onFailure().invoke { e -> Log.error("Error during page blacklisting.", e) }
            .onFailure().recoverWithResponse()

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/plain")
    @Path("/update")
    @RolesAllowed("app-admin")
    override fun updateAdmin(
        @FormParam("oldUsername") @DefaultValue("") oldUsername: String,
        @FormParam("newUsername") @DefaultValue("") newUsername: String,
        @FormParam("oldPassword") @DefaultValue("") oldPassword: String,
        @FormParam("newPassword") @DefaultValue("") newPassword: String
    ): Uni<Response> {
        return validatePassword(newPassword, newPassword).flatMap { passwordValidation ->
            if (!passwordValidation.valid) {
                Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                        .entity(passwordValidation.message ?: "Password validation failed")
                        .build()
                )
            } else {
                // Only validate newUsername if it's different from oldUsername (for username changes)
                if (newUsername != oldUsername) {
                    validateUser(newUsername, apiUserRepository).flatMap { userValidation ->
                        if (!userValidation.valid) {
                            Uni.createFrom().item(
                                Response.status(Response.Status.BAD_REQUEST)
                                    .entity(userValidation.message ?: "Username validation failed")
                                    .build()
                            )
                        } else {
                            apiUserRepository.updateAdmin(oldUsername, newUsername, oldPassword, newPassword).flatMap { user ->
                                Uni.createFrom().item(Response.ok(user).build())
                            }
                        }
                    }
                } else {
                    apiUserRepository.updateAdmin(oldUsername, newUsername, oldPassword, newPassword).flatMap { user ->
                        Uni.createFrom().item(Response.ok(user).build())
                    }
                }
            }
        }.onFailure().invoke { e -> Log.error("Error updating admin password.", e) }
            .onFailure().recoverWithResponse()
    }
}
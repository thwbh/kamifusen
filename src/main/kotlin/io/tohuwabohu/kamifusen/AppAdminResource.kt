package io.tohuwabohu.kamifusen

import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.ApiUser
import io.tohuwabohu.kamifusen.crud.ApiUserRepository
import io.tohuwabohu.kamifusen.crud.Page
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import io.tohuwabohu.kamifusen.crud.PageRepository
import io.tohuwabohu.kamifusen.crud.dto.PageDto
import io.tohuwabohu.kamifusen.crud.dto.PageVisitDtoRepository
import io.tohuwabohu.kamifusen.crud.error.recoverWithResponse
import io.tohuwabohu.kamifusen.crud.security.*
import io.tohuwabohu.kamifusen.ssr.*
import io.tohuwabohu.kamifusen.ssr.response.recoverWithHtmxResponse
import io.vertx.ext.web.RoutingContext
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.*
import jakarta.ws.rs.core.*
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.Instant
import java.time.LocalDateTime
import java.util.*


@Path("/admin")
class AppAdminResource(
    private val apiUserRepository: ApiUserRepository,
    private val pageVisitDtoRepository: PageVisitDtoRepository,
    private val pageRepository: PageRepository
) {
    @ConfigProperty(name = "quarkus.http.auth.form.cookie-name")
    lateinit var cookieName: String

    @Path("/keygen")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("app-admin")
    fun renderNewApiKey(
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
                Uni.createFrom().item(Response
                    .ok(renderUsernameValidationError(result))
                    .header("hx-retarget", "#username")
                    .build())
            }
        }.onFailure().invoke { e -> Log.error("Error during keygen.", e) }
            .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build())

    @Path("/stats")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("app-admin")
    fun renderVisits(): Uni<Response> =
        pageVisitDtoRepository.getAllPageVisits()
            .flatMap {
                Uni.createFrom().item(Response.ok(it).build())
            }
            .onFailure().invoke { e -> Log.error("Error receiving stats.", e) }
            .onFailure().recoverWithResponse()

    @Path("/pages")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(
        responseCode = "200",
        description = "List of registered pages",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = Page::class, type = SchemaType.ARRAY)
        )]
    )
    @RolesAllowed("app-admin")
    fun renderPageList(): Uni<Response> =
        pageRepository.listAllPages().flatMap {
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
    fun renderUserList(): Uni<Response> =
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
            .onFailure().recoverWithItem(Response.serverError().build())

    @Path("/pageadd")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("app-admin")
    fun registerNewPage(@FormParam("path") path: String, @FormParam("domain") domain: String): Uni<Response> =
        validatePage(path, domain, pageRepository).flatMap { result ->
            if (result == PageValidation.VALID) {
                pageRepository.addPage(path, domain).map { Response.ok().build() }
            } else {
                Uni.createFrom().item(Response
                    .ok(renderPageValidationError(result))
                    .header("hx-retarget", "#path")
                    .build())
            }
        }.onFailure().invoke { e -> Log.error("Error during page registration.", e) }
            .onFailure().recoverWithItem(Response.serverError().build())

    @Path("/pagedel/{pageId}")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("app-admin")
    fun unregisterPage(pageId: UUID): Uni<Response> =
        pageRepository.deletePage(pageId).map { Response.ok().build() }
            .onFailure().invoke { e -> Log.error("Error during page deletion.", e) }
            .onFailure().recoverWithItem(Response.serverError().build())
}
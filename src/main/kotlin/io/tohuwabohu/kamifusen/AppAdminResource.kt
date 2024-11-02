package io.tohuwabohu.kamifusen

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.ApiUser
import io.tohuwabohu.kamifusen.crud.ApiUserRepository
import io.tohuwabohu.kamifusen.crud.PageRepository
import io.tohuwabohu.kamifusen.crud.dto.PageVisitDtoRepository
import io.tohuwabohu.kamifusen.ssr.*
import io.tohuwabohu.kamifusen.ssr.response.createHtmxErrorResponse
import io.tohuwabohu.kamifusen.ssr.response.recoverWithHtmxResponse
import io.vertx.ext.web.RoutingContext
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.*
import jakarta.ws.rs.core.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Path("/admin/render")
class AppAdminResource(
    private val apiUserRepository: ApiUserRepository,
    private val pageVisitDtoRepository: PageVisitDtoRepository,
    private val pageRepository: PageRepository
) {
    @Path("/register")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    fun registerAdmin(@FormParam("password") password: String): Uni<Response> =
        apiUserRepository.setAdminPassword(password).onItem()
            .transform { Response.ok(renderPasswordFlowSuccess()).build() }
            .onFailure().invoke { e -> Log.error("Error during admin user creation.", e) }
            .onFailure().recoverWithHtmxResponse(Response.Status.INTERNAL_SERVER_ERROR)

    @Path("/login")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    fun loginAdmin(@Context routingContext: RoutingContext, @FormParam("password") password: String): Uni<Response> =
        apiUserRepository.findByUsername("admin").chain { apiUser ->
            when (apiUser?.password) {
                null -> Uni.createFrom().item(Response.accepted(renderPasswordFlow()).build())
                else -> {
                    if (BcryptUtil.matches(password, apiUser.password)) {
                        val basicAuthToken =
                            Base64.getEncoder().encodeToString("${apiUser.username}:${password}".toByteArray())

                        Uni.createFrom().item(
                            Response.ok()/*.cookie(
                                NewCookie.Builder(Cookie("AuthToken", "Basic $basicAuthToken"))
                                    .expiry(Date.from(Instant.now().plusSeconds(3600)))
                                    .sameSite(NewCookie.SameSite.LAX)
                                    .secure(routingContext.request().isSSL)
                                    .path("/").build())*/
                                .header("Authorization", "Basic leckdumirdieeier")
                                .header("hx-redirect", "/admin.html")
                                .build()
                        )
                    } else {
                        createHtmxErrorResponse(Response.Status.UNAUTHORIZED)
                    }
                }
            }
        }

    @Path("/logout")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    fun logoutAdmin(@Context routingContext: RoutingContext): Uni<Response> =
        Uni.createFrom().item(
            Response.noContent().cookie(
                NewCookie.Builder("AuthToken")
                    .maxAge(0)
                    .expiry(Date.from(Instant.EPOCH))
                    .sameSite(NewCookie.SameSite.LAX)
                    .secure(routingContext.request().isSSL)
                    .path("/").build()
            ).header("hx-redirect", "/")
                .header("Authorization", "")
                .build()
        )

    @Path("/dashboard")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_HTML)
    fun renderAdminDashboard(): Uni<Response> =
        Uni.createFrom().item(Response.ok(renderDashboard()).build())
            .onFailure().invoke { e -> Log.error("Error during dashboard rendering.", e) }
            .onFailure().recoverWithHtmxResponse(Response.Status.INTERNAL_SERVER_ERROR)

    @Path("/stats")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_HTML)
    fun renderVisits(): Uni<Response> =
        pageVisitDtoRepository.getAllPageVisits()
            .flatMap { Uni.createFrom().item(Response.ok(renderStats(it)).build()) }
            .onFailure().invoke { e -> Log.error("Error during stats rendering.", e) }
            .onFailure().recoverWithHtmxResponse(Response.Status.INTERNAL_SERVER_ERROR)

    @Path("/pages")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_HTML)
    fun renderPageList(): Uni<Response> =
        pageRepository.listAllPages().flatMap { Uni.createFrom().item(Response.ok(renderPages(it)).build()) }
            .onFailure().invoke { e -> Log.error("Error during pages rendering.", e) }
            .onFailure().recoverWithHtmxResponse(Response.Status.INTERNAL_SERVER_ERROR)

    @Path("/users")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_HTML)
    fun renderUserList(): Uni<Response> =
        apiUserRepository.listAll().flatMap { Uni.createFrom().item(Response.ok(renderUserManagement(it)).build()) }
            .onFailure().invoke { e -> Log.error("Error during user list rendering.", e) }
            .onFailure().recoverWithHtmxResponse(Response.Status.INTERNAL_SERVER_ERROR)

    @Path("/keygen")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    fun renderNewApiKey(
        @FormParam("username") username: String,
        @FormParam("role") role: String,
        @FormParam("expiresAt") expiresAt: String
    ): Uni<Response> =
        apiUserRepository.addUser(
            ApiUser(
            username = username,
            role = role,
            expiresAt = when (expiresAt) {
                "" -> null
                else -> LocalDateTime.parse(expiresAt)
            },
        )
        ).onItem().transform { keyRaw -> Response.ok(renderCreatedApiKey(keyRaw)).build() }
            .onFailure().invoke { e -> Log.error("Error during keygen.", e) }
            .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build())
}
package io.tohuwabohu.kamifusen

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.ApiUserRepository
import io.tohuwabohu.kamifusen.crud.dto.PageVisitDtoRepository
import io.tohuwabohu.kamifusen.ssr.renderStats
import io.tohuwabohu.kamifusen.ssr.renderPasswordFlow
import io.tohuwabohu.kamifusen.ssr.renderPasswordFlowSuccess
import io.tohuwabohu.kamifusen.ssr.response.createHtmxErrorResponse
import io.tohuwabohu.kamifusen.ssr.response.recoverWithHtmxResponse
import io.vertx.ext.web.RoutingContext
import jakarta.ws.rs.*
import jakarta.ws.rs.core.*
import java.time.Instant
import java.util.*

@Path("/admin")
class AppAdminResource(
    private val apiUserRepository: ApiUserRepository,
    private val pageVisitDtoRepository: PageVisitDtoRepository
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
                        val basicAuthToken = "${apiUser.username}:${password}".toByteArray();

                        Uni.createFrom().item(
                            Response.ok().cookie(
                                NewCookie.Builder(Cookie("AuthToken", "Basic $basicAuthToken"))
                                    .expiry(Date.from(Instant.now().plusSeconds(3600)))
                                    .sameSite(NewCookie.SameSite.LAX)
                                    .secure(routingContext.request().isSSL)
                                    .path("/").build())
                                .header("hx-redirect", "/admin.html")
                                .build()
                        )
                    } else {
                        createHtmxErrorResponse(Response.Status.UNAUTHORIZED)
                    }
                }
            }
        }

    @Path("/stats")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_HTML)
    fun renderVisits(): Uni<Response> =
        pageVisitDtoRepository.getAllPageVisits()
            .flatMap { Uni.createFrom().item(Response.ok(renderStats(it)).build()) }
            .onFailure().invoke { e -> Log.error("Error during admin user creation.", e) }
            .onFailure().recoverWithHtmxResponse(Response.Status.INTERNAL_SERVER_ERROR)
}
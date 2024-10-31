package io.tohuwabohu.kamifusen

import com.ongres.scram.common.bouncycastle.base64.Base64
import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.ApiUserRepository
import io.tohuwabohu.kamifusen.crud.dto.PageVisitDtoRepository
import io.tohuwabohu.kamifusen.ssr.createPageVisitsTable
import io.tohuwabohu.kamifusen.ssr.createPasswordUpdateDiv
import io.tohuwabohu.kamifusen.ssr.createSuccessfulPasswordUpdateDiv
import io.tohuwabohu.kamifusen.ssr.response.createHtmxErrorResponse
import io.tohuwabohu.kamifusen.ssr.response.recoverWithHtmxResponse
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

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
        apiUserRepository.setAdminPassword(password).onItem().transform { Response.ok(createSuccessfulPasswordUpdateDiv()).build() }
            .onFailure().invoke { e -> Log.error("Error during admin user creation.", e) }
            .onFailure().recoverWithHtmxResponse(Response.Status.INTERNAL_SERVER_ERROR)

    @Path("/login")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    fun loginAdmin(@FormParam("password") password: String): Uni<Response> =
        apiUserRepository.findByUsername("admin").chain{ apiUser ->
            when (apiUser?.password) {
                null -> Uni.createFrom().item(Response.accepted(createPasswordUpdateDiv()).build())
                else -> {
                    if (BcryptUtil.matches(password, apiUser.password)) {
                        Uni.createFrom().item(
                            Response.ok(Base64.encode("${apiUser.username}:${password}".toByteArray())
                        ).build())
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
        pageVisitDtoRepository.getAllPageVisits().flatMap { Uni.createFrom().item(Response.ok(createPageVisitsTable(it)).build()) }
            .onFailure().invoke { e -> Log.error("Error during admin user creation.", e) }
            .onFailure().recoverWithHtmxResponse(Response.Status.INTERNAL_SERVER_ERROR)
}
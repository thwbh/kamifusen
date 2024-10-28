package io.tohuwabohu.kamifusen

import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.*
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext

@Path("/admin")
class AdminResource(
    val pageRepository: PageRepository,
    val apiUserRepository: ApiUserRepository
) {
    @Path("/add")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("api-admin")
    fun registerPage(@Context securityContext: SecurityContext, path: String): Uni<Response> =
        pageRepository.findPageByPath(path).chain { page ->
            if (page == null) {
                pageRepository.addPage(path).onItem().transform {
                    Response.status(Response.Status.CREATED).build()
                }
            } else {
                Uni.createFrom().item(Response.noContent().build())
            }
        }

    @Path("/register")
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    fun registerAdmin(password: String): Uni<Response> =
        apiUserRepository.setAdminPassword(password).onItem().transform { Response.status(Response.Status.CREATED).build() }
            .onFailure().invoke { e -> Log.error("Error during admin user creation.", e) }
            .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build())

    @Path("/keygen")
    @POST
    @Consumes("application/json")
    @Produces("text/plain")
    @RolesAllowed("app-admin")
    fun generateApiKey(body: ApiUser): Uni<Response> =
        apiUserRepository.addUser(apiUser = body).onItem().transform { keyRaw -> Response.ok(keyRaw.username).build() }
            .onFailure().invoke { e -> Log.error("Error during keygen.", e) }
            .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build())

    @Path("/pages")
    @GET
    @Consumes("text/plain")
    @Produces("application/json")
    @RolesAllowed("api-admin", "app-admin")
    fun listPages(): Uni<Response> =
        pageRepository.listAllPages().onItem().transform { pages -> Response.ok(pages).build()}
            .onFailure().invoke { e -> Log.error("Error creating page list.", e) }
            .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build())
}
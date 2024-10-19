package io.tohuwabohu.kamifusen

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.PageRepository
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext

@Path("/admin")
class AdminResource(val pageRepository: PageRepository) {
    @Path("/add")
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    fun registerPage(@Context securityContext: SecurityContext, body: String): Uni<Response> =
        Uni.createFrom().item(securityContext.isUserInRole("api-admin")).flatMap { isAdmin ->
            if (isAdmin) {
                pageRepository.findPageByPath(body).chain { page ->
                    if (page == null) {
                        pageRepository.addPage(body).onItem().transform {
                            Response.status(Response.Status.CREATED).build()
                        }
                    } else {
                        Uni.createFrom().item(Response.noContent().build())
                    }
                }
            } else Uni.createFrom().item(Response.status(Response.Status.FORBIDDEN).build())
        }
}
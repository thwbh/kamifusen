package io.tohuwabohu.kamifusen

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.PageRepository
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Response

@Path("/admin")
class AdminResource(val pageRepository: PageRepository) {
    @Path("/add")
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    fun registerPage(body: String): Uni<Response> =
        pageRepository.findPageByPath(body).chain { page ->
            if (page == null) {
                pageRepository.addPage(body).onItem().transform {
                    Response.status(Response.Status.CREATED).build()
                }
            } else {
                Uni.createFrom().item(Response.noContent().build())
            }
        }
}
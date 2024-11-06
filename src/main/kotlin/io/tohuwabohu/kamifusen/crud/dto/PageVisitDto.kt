package io.tohuwabohu.kamifusen.crud.dto

import io.quarkus.hibernate.reactive.panache.Panache
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheRepository
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.Tuple
import java.time.LocalDateTime
import java.util.*

data class PageVisitDto(
    var id: UUID,
    var path: String,
    var domain: String?,
    var pageAdded: LocalDateTime,
    var visits: Long
)

@ApplicationScoped
class PageVisitDtoRepository() : PanacheRepository<PageVisitDto> {
    val query = """
            SELECT p.id, p.path, p.pageAdded, COUNT(pv.visitorId), p.domain AS visits
            FROM Page p
            LEFT JOIN PageVisit pv ON p.id = pv.pageId
            GROUP BY p.id
            ORDER BY p.domain, p.path, p.pageAdded DESC
        """

    fun getAllPageVisits() : Uni<List<PageVisitDto>> =
        Panache.getSession().flatMap { session ->
            session.createQuery(query, Tuple::class.java).resultList
                .onItem().transform { it.map ( Tuple::toPageVisitDto ) }
        }

}

fun Tuple.toPageVisitDto() = PageVisitDto(
    id = this.get(0, UUID::class.java),
    path = this.get(1, String::class.java),
    pageAdded = this.get(2, LocalDateTime::class.java),
    visits = this.get(3, Long::class.javaObjectType),
    domain = this.get(4, String::class.java)
)
package io.tohuwabohu.kamifusen.service

import io.quarkus.hibernate.reactive.panache.Panache
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.api.generated.model.PageWithStatsDto
import io.tohuwabohu.kamifusen.extensions.toPageStatsDto
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.Tuple

/**
 * Service for managing page statistics using named queries with direct query execution.
 *
 * This service uses named queries defined in the Page entity but executes them directly
 * with Tuple results since aggregate queries don't return entity objects.
 */
@ApplicationScoped
class PageStatsService {

    fun getAllPageVisits(): Uni<List<PageWithStatsDto>> =
        Panache.getSession().flatMap { session ->
            session.createNamedQuery("Page.findAllWithStats", Tuple::class.java).resultList
                .onItem().transform { it.map(Tuple::toPageStatsDto) }
        }

    fun getNonBlacklistedPagesWithStats(): Uni<List<PageWithStatsDto>> =
        Panache.getSession().flatMap { session ->
            session.createNamedQuery("Page.findNonBlacklistedWithStats", Tuple::class.java).resultList
                .onItem().transform { it.map(Tuple::toPageStatsDto) }
        }

    fun getBlacklistedPagesWithStats(): Uni<List<PageWithStatsDto>> =
        Panache.getSession().flatMap { session ->
            session.createNamedQuery("Page.findBlacklistedWithStats", Tuple::class.java).resultList
                .onItem().transform { it.map(Tuple::toPageStatsDto) }
        }

    fun getBlacklistedPagesByDomainWithStats(domain: String): Uni<List<PageWithStatsDto>> =
        Panache.getSession().flatMap { session ->
            session.createNamedQuery("Page.findBlacklistedByDomainWithStats", Tuple::class.java)
                .setParameter("domain", domain)
                .resultList
                .onItem().transform { it.map(Tuple::toPageStatsDto) }
        }
}

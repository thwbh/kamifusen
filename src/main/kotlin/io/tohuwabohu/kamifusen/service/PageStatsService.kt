package io.tohuwabohu.kamifusen.service

import io.quarkus.hibernate.reactive.panache.Panache
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.api.generated.model.PageWithStatsDto
import io.tohuwabohu.kamifusen.extensions.toPageStatsDto
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.Tuple

/**
 * Repository for managing PageVisitDto entities. It does not possess a JPA setup.
 *
 * This repository uses a native query to join the [io.tohuwabohu.kamifusen.service.crud.Page] and
 * [io.tohuwabohu.kamifusen.PageVisit] entities for receiving a total hit count per page.
 */
@ApplicationScoped
class PageStatsService() {
    private val allPagesQuery = """
            SELECT p.id, p.path, p.pageAdded, COUNT(pv.visitorId), p.domain, p.lastHit AS visits
            FROM Page p
            LEFT JOIN PageVisit pv ON p.id = pv.pageId
            GROUP BY p.id
            ORDER BY p.domain, p.path, p.pageAdded DESC
        """

    private val nonBlacklistedPagesQuery = """
            SELECT p.id, p.path, p.pageAdded, COUNT(pv.visitorId), p.domain, p.lastHit AS visits
            FROM Page p
            LEFT JOIN PageVisit pv ON p.id = pv.pageId
            WHERE p.id NOT IN (SELECT b.pageId FROM Blacklist b)
            GROUP BY p.id
            ORDER BY p.domain, p.path, p.pageAdded DESC
        """

    private val blacklistedPagesQuery = """
            SELECT p.id, p.path, p.pageAdded, COUNT(pv.visitorId), p.domain, p.lastHit AS visits
            FROM Page p
            LEFT JOIN PageVisit pv ON p.id = pv.pageId
            INNER JOIN Blacklist b ON p.id = b.pageId
            GROUP BY p.id
            ORDER BY p.domain, p.path, p.pageAdded DESC
        """

    private val blacklistedPagesByDomainQuery = """
            SELECT p.id, p.path, p.pageAdded, COUNT(pv.visitorId), p.domain, p.lastHit AS visits
            FROM Page p
            LEFT JOIN PageVisit pv ON p.id = pv.pageId
            INNER JOIN Blacklist b ON p.id = b.pageId
            WHERE p.domain = :domain
            GROUP BY p.id
            ORDER BY p.path, p.pageAdded DESC
        """

    fun getAllPageVisits(): Uni<List<PageWithStatsDto>> =
        Panache.getSession().flatMap { session ->
            session.createQuery(allPagesQuery, Tuple::class.java).resultList
                .onItem().transform { it.map ( Tuple::toPageStatsDto ) }
        }

    fun getNonBlacklistedPagesWithStats(): Uni<List<PageWithStatsDto>> =
        Panache.getSession().flatMap { session ->
            session.createQuery(nonBlacklistedPagesQuery, Tuple::class.java).resultList
                .onItem().transform { it.map ( Tuple::toPageStatsDto ) }
        }

    fun getBlacklistedPagesWithStats(): Uni<List<PageWithStatsDto>> =
        Panache.getSession().flatMap { session ->
            session.createQuery(blacklistedPagesQuery, Tuple::class.java).resultList
                .onItem().transform { it.map ( Tuple::toPageStatsDto ) }
        }

    fun getBlacklistedPagesByDomainWithStats(domain: String): Uni<List<PageWithStatsDto>> =
        Panache.getSession().flatMap { session ->
            session.createQuery(blacklistedPagesByDomainQuery, Tuple::class.java)
                .setParameter("domain", domain)
                .resultList
                .onItem().transform { it.map ( Tuple::toPageStatsDto ) }
        }
}

package io.tohuwabohu.kamifusen.service.crud

import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy
import io.smallrye.mutiny.Uni
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*

@NamedQueries(
    NamedQuery(name = "PageVisit.countVisitsPerPage", query = "SELECT count(*) FROM PageVisit WHERE pageId = :pageId AND visitorId = :visitorId"),
    NamedQuery(name = "PageVisit.countTotalVisits", query = """
            SELECT COUNT(*)
            FROM PageVisit
            WHERE visitedAt >= :startDate
            AND pageId NOT IN (SELECT b.pageId FROM Blacklist b)
        """),
    NamedQuery(name = "PageVisit.countTotalPages", query = "SELECT COUNT(DISTINCT pageId) FROM PageVisit where visitedAt >= :startDate and pageId NOT IN (SELECT b.pageId FROM Blacklist b)"),
    NamedQuery(name = "PageVisit.countTotalDomains", query = """
        SELECT COUNT(DISTINCT p.domain)
            FROM Page p
            JOIN PageVisit pv ON p.id = pv.pageId
            WHERE pv.visitedAt >= :startDate
            AND p.id NOT IN (SELECT b.pageId FROM Blacklist b)
    """)
)
@NamedNativeQueries(
    NamedNativeQuery(name = "Stats.getVisitTrendData", query = """
            SELECT
                EXTRACT(DOW FROM pv.visited_at) as day_of_week,
                COUNT(*) as visit_count
            FROM page_visit pv
            WHERE pv.visited_at >= :startDate
            AND pv.page_id NOT IN (SELECT b.page_id FROM blacklist b)
            GROUP BY EXTRACT(DOW FROM pv.visited_at)
            ORDER BY day_of_week
        """),
    NamedNativeQuery(name = "Stats.getTopPagesData", query = """
            SELECT p.domain, p.path, COUNT(pv.page_id) as visits
            FROM page p
            JOIN page_visit pv ON p.id = pv.page_id
            WHERE pv.visited_at >= :startDate
            AND p.id NOT IN (SELECT b.page_id FROM blacklist b)
            GROUP BY p.domain, p.path
            ORDER BY visits DESC
        """
    ),
    NamedNativeQuery(name = "Stats.getDomainStatsData", query = """
            SELECT COALESCE(p.domain, 'unknown') as domain, COUNT(pv.page_id) as visits
            FROM page p
            JOIN page_visit pv ON p.id = pv.page_id
            WHERE pv.visited_at >= :startDate
            AND p.id NOT IN (SELECT b.page_id FROM blacklist b)
            GROUP BY p.domain
            ORDER BY visits DESC
        """)
)
@Entity
@IdClass(CompositeKey::class)
data class PageVisit (
    @Id
    var pageId: UUID,
    @Id
    var visitorId: UUID,
    val visitedAt: LocalDateTime = LocalDateTime.now()
): PanacheEntityBase {
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as PageVisit

        return pageId == other.pageId
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  pageId = $pageId   ,   visitorId = $visitorId   ,   visitedAt = $visitedAt )"
    }
}

@ApplicationScoped
class PageVisitRepository: PanacheRepository<PageVisit> {
    @WithTransaction
    fun countVisits(pageId: UUID) = count("pageId = ?1", pageId)
    @WithTransaction
    fun countVisitsForVisitor(pageId: UUID, visitorId: UUID) = count("#PageVisit.countVisitsPerPage", mapOf(
        "visitorId" to visitorId,
        "pageId" to pageId)
    )

    @WithTransaction
    fun addVisit(pageId: UUID, visitorId: UUID) = persist(PageVisit(pageId, visitorId))

    // Time-based analytics methods
    @WithTransaction
    fun getVisitsByTimeRange(from: LocalDateTime, to: LocalDateTime): Uni<List<PageVisit>> =
        list("visitedAt >= ?1 AND visitedAt <= ?2", from, to)

    @WithTransaction
    fun countVisitsByTimeRange(pageId: UUID, from: LocalDateTime, to: LocalDateTime): Uni<Long> =
        count("pageId = ?1 AND visitedAt >= ?2 AND visitedAt <= ?3", pageId, from, to)

    @WithTransaction
    fun getVisitsInLastHours(hours: Int): Uni<List<PageVisit>> =
        list("visitedAt >= ?1", LocalDateTime.now().minusHours(hours.toLong()))

    @WithTransaction
    fun getVisitsInLastDays(days: Int): Uni<List<PageVisit>> =
        list("visitedAt >= ?1", LocalDateTime.now().minusDays(days.toLong()))

    @WithTransaction
    fun getTotalVisitsCount(): Uni<Long> = count()

    @WithTransaction
    fun getVisitCountsByDay(days: Int): Uni<List<PageVisit>> =
        list("visitedAt >= ?1 ORDER BY visitedAt", LocalDateTime.now().minusDays(days.toLong()))

    @WithTransaction
    // Sliding window session support
    fun findRecentVisitByVisitorOnDomain(visitorId: UUID, domain: String, minutesBack: Int): Uni<PageVisit?> =
        find("""
            SELECT pv FROM PageVisit pv
            JOIN Page p ON pv.pageId = p.id
            WHERE pv.visitorId = ?1
            AND p.domain = ?2
            AND pv.visitedAt >= ?3
            ORDER BY pv.visitedAt DESC
        """, visitorId, domain, LocalDateTime.now().minusMinutes(minutesBack.toLong()))
        .firstResult()
}

@Embeddable
class CompositeKey(val pageId: UUID, val visitorId: UUID) : Serializable
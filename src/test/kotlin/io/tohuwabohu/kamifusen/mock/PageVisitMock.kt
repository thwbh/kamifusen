package io.tohuwabohu.kamifusen.mock

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.PageVisit
import io.tohuwabohu.kamifusen.crud.PageVisitRepository
import java.time.LocalDateTime
import java.util.*

/**
 * Mock repository for non-disruptive CRUD operations on PageVisit entity
 */
class PageVisitRepositoryMock : PageVisitRepository() {
    val visits = mutableListOf<PageVisit>()
    val pages = mutableListOf<io.tohuwabohu.kamifusen.crud.Page>()
    val visitCounts = mutableMapOf<UUID, Long>()

    override fun countVisits(pageId: UUID): Uni<Long> = Uni.createFrom().item(visitCounts.getOrDefault(pageId, 0L))

    override fun countVisitsForVisitor(pageId: UUID, visitorId: UUID): Uni<Long> = Uni.createFrom().item(visits.count { it.pageId == pageId && it.visitorId == visitorId }.toLong())

    override fun addVisit(pageId: UUID, visitorId: UUID): Uni<PageVisit> {
        val pageVisit = PageVisit(pageId, visitorId)

        visits.add(pageVisit)

        return Uni.createFrom().item(pageVisit)
    }

    // Enhanced time-based methods
    override fun getVisitsByTimeRange(from: LocalDateTime, to: LocalDateTime): Uni<List<PageVisit>> =
        Uni.createFrom().item(visits.filter { it.visitedAt >= from && it.visitedAt <= to })

    override fun countVisitsByTimeRange(pageId: UUID, from: LocalDateTime, to: LocalDateTime): Uni<Long> =
        Uni.createFrom().item(visits.count { it.pageId == pageId && it.visitedAt >= from && it.visitedAt <= to }.toLong())

    override fun getVisitsInLastHours(hours: Int): Uni<List<PageVisit>> {
        val cutoff = LocalDateTime.now().minusHours(hours.toLong())
        return Uni.createFrom().item(visits.filter { it.visitedAt >= cutoff })
    }

    override fun getVisitsInLastDays(days: Int): Uni<List<PageVisit>> {
        val cutoff = LocalDateTime.now().minusDays(days.toLong())
        return Uni.createFrom().item(visits.filter { it.visitedAt >= cutoff })
    }

    override fun getTotalVisitsCount(): Uni<Long> = Uni.createFrom().item(visits.size.toLong())

    override fun getVisitCountsByDay(days: Int): Uni<List<PageVisit>> {
        val cutoff = LocalDateTime.now().minusDays(days.toLong())
        return Uni.createFrom().item(visits.filter { it.visitedAt >= cutoff }.sortedBy { it.visitedAt })
    }

    // Sliding window session support
    override fun findRecentVisitByVisitorOnDomain(visitorId: UUID, domain: String, minutesBack: Int): Uni<PageVisit?> {
        val cutoff = LocalDateTime.now().minusMinutes(minutesBack.toLong())

        // Properly simulate the JOIN with Page table for domain filtering
        val recentVisit = visits
            .filter { visit ->
                val page = pages.find { it.id == visit.pageId }
                visit.visitorId == visitorId &&
                visit.visitedAt >= cutoff &&
                page?.domain == domain
            }
            .maxByOrNull { it.visitedAt }

        return Uni.createFrom().item(recentVisit)
    }
}
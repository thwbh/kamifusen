package io.tohuwabohu.kamifusen.service

import io.quarkus.hibernate.reactive.panache.Panache
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.api.generated.model.AggregatedStatsDto
import io.tohuwabohu.kamifusen.api.generated.model.DomainStatDataDto
import io.tohuwabohu.kamifusen.api.generated.model.TopPageDataDto
import io.tohuwabohu.kamifusen.api.generated.model.VisitTrendDataDto
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.Tuple
import org.hibernate.reactive.mutiny.Mutiny
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Repository for generating aggregated statistics from real database data
 */
@ApplicationScoped
class StatsService {

    @WithTransaction
    fun getAggregatedStats(timeRange: String = "7d"): Uni<AggregatedStatsDto> {
        val days = when (timeRange) {
            "24h" -> 1
            "7d" -> 7
            "30d" -> 30
            "90d" -> 90
            else -> 7
        }

        val startDate = LocalDateTime.now().minusDays(days.toLong())

        return Panache.getSession().flatMap { session ->
            // Get all the data in parallel
            val visitTrendQuery = getVisitTrendData(session, days)
            val topPagesQuery = getTopPagesData(session, startDate)
            val domainStatsQuery = getDomainStatsData(session, startDate)
            val totalStatsQuery = getTotalStats(session, startDate)

            Uni.combine().all().unis(visitTrendQuery, topPagesQuery, domainStatsQuery, totalStatsQuery)
                .asTuple()
                .onItem().transform { tuple ->
                    val visitData = tuple.item1
                    val topPages = tuple.item2
                    val domainStats = tuple.item3
                    val (totalVisits, totalPages, totalDomains) = tuple.item4

                    AggregatedStatsDto(
                        visitData = visitData,
                        topPages = topPages,
                        domainStats = domainStats,
                        totalVisits = totalVisits,
                        totalPages = totalPages,
                        totalDomains = totalDomains
                    )
                }
        }
    }

    private fun getVisitTrendData(session: Mutiny.Session, days: Int): Uni<List<VisitTrendDataDto>> {
        val startDate = LocalDateTime.now().minusDays(days.toLong())

        // Query visits grouped by day of week across all pages and domains
        val query = """
            SELECT
                EXTRACT(DOW FROM pv.visited_at) as day_of_week,
                COUNT(*) as visit_count
            FROM page_visit pv
            WHERE pv.visited_at >= :startDate
            GROUP BY EXTRACT(DOW FROM pv.visited_at)
            ORDER BY day_of_week
        """

        return session.createNativeQuery(query, Tuple::class.java)
            .setParameter("startDate", startDate)
            .resultList
            .onItem().transform { results ->
                // Convert PostgreSQL day of week (0=Sunday, 1=Monday, ..., 6=Saturday) to day names
                val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

                // Create a map from the results
                val visitsByDay = results.associate { tuple ->
                    val dayOfWeek = tuple.get(0, BigDecimal::class.java).toInt()
                    val visitCount = tuple.get(1, Long::class.javaObjectType)
                    dayOfWeek to visitCount
                }

                // Calculate thresholds for color coding based on the data
                val allCounts = visitsByDay.values
                val avgVisits = if (allCounts.isNotEmpty()) allCounts.average() else 0.0
                val lowThreshold = avgVisits * 0.7
                val highThreshold = avgVisits * 1.3

                // Build result for all 7 days of the week (Monday to Sunday order for better UX)
                val mondayFirstOrder = listOf(1, 2, 3, 4, 5, 6, 0) // Mon=1, Tue=2, ..., Sun=0

                mondayFirstOrder.map { dayOfWeek ->
                    val visits = visitsByDay[dayOfWeek] ?: 0L
                    val dayName = dayNames[dayOfWeek]

                    val category = when {
                        visits < lowThreshold -> "low"
                        visits > highThreshold -> "high"
                        else -> "normal"
                    }

                    VisitTrendDataDto(dayName, visits, category)
                }
            }
    }

    /**
     * Get top pages and their visits within the specified time range
     * Take only the first 5 pages into consideration. Use the whole data set to calculate percentages.
     * When there are fewer than 5 pages, return data as-is. When there are more than 5 pages, add an "Other"
     * row with the remaining percentage.
     *
     * @param session Mutiny session for reactive Hibernate queries
     * @param startDate Start date for the time range
     * @return List of top pages and their visits
     */
    private fun getTopPagesData(session: Mutiny.Session, startDate: LocalDateTime): Uni<List<TopPageDataDto>> {
        // Query top pages within the specified time range
        val query = """
            SELECT p.path, COUNT(pv.page_id) as visits
            FROM page p
            JOIN page_visit pv ON p.id = pv.page_id
            WHERE pv.visited_at >= :startDate
            GROUP BY p.path
            ORDER BY visits DESC
        """

        return session.createNativeQuery(query, Tuple::class.java)
            .setParameter("startDate", startDate)
            .resultList
            .onItem().transform { results ->
                val totalVisits = results.sumOf { it.get(1, Long::class.javaObjectType) }

                val topPagesData = results.take(5).map { tuple ->
                    val path = tuple.get(0, String::class.java)
                    val visits = tuple.get(1, Long::class.javaObjectType)
                    val percentage = if (totalVisits > 0) (visits.toDouble() / totalVisits.toDouble()) * 100 else 0.0

                    TopPageDataDto(path, visits, percentage)
                }

                if (topPagesData.size < 5) return@transform topPagesData

                val rest = results.drop(5).sumOf { it.get(1, Long::class.javaObjectType) }

                val remainingPercentage = 100 - topPagesData.sumOf { it.percentage.toDouble() }
                val validPercentage = maxOf(0.0, remainingPercentage)

                topPagesData + listOf(TopPageDataDto("Other", rest, validPercentage))
            }
    }

    private fun getDomainStatsData(session: Mutiny.Session, startDate: LocalDateTime): Uni<List<DomainStatDataDto>> {
        // Query domain statistics within the specified time range
        val query = """
            SELECT COALESCE(p.domain, 'unknown') as domain, COUNT(pv.page_id) as visits
            FROM page p
            JOIN page_visit pv ON p.id = pv.page_id
            WHERE pv.visited_at >= :startDate
            GROUP BY p.domain
            ORDER BY visits DESC
        """

        return session.createNativeQuery(query, Tuple::class.java)
            .setParameter("startDate", startDate)
            .resultList
            .onItem().transform { results ->
                val totalVisits = results.sumOf { it.get(1, Long::class.javaObjectType) }

                results.map { tuple ->
                    val domain = tuple.get(0, String::class.java)
                    val visits = tuple.get(1, Long::class.javaObjectType)
                    val percentage = if (totalVisits > 0) (visits.toDouble() / totalVisits.toDouble()) * 100 else 0.0

                    DomainStatDataDto(domain, visits, percentage)
                }
            }
    }

    private fun getTotalStats(session: Mutiny.Session, startDate: LocalDateTime): Uni<Triple<Long, Long, Long>> {
        // Use reactive SQL queries with time range filtering
        val totalVisitsQuery = session.createNativeQuery("""
            SELECT COUNT(*)
            FROM page_visit
            WHERE visited_at >= :startDate
        """, Long::class.javaObjectType)
            .setParameter("startDate", startDate)
            .singleResult
            .onItem().transform { result -> result.toLong()}

        val totalPagesQuery = session.createNativeQuery("""
            SELECT COUNT(DISTINCT page_id)
            FROM page_visit
            WHERE visited_at >= :startDate
        """, Long::class.javaObjectType)
            .setParameter("startDate", startDate)
            .singleResult
            .onItem().transform { result -> result.toLong() }

        val totalDomainsQuery = session.createNativeQuery("""
            SELECT COUNT(DISTINCT p.domain)
            FROM page p
            JOIN page_visit pv ON p.id = pv.page_id
            WHERE pv.visited_at >= :startDate
        """, Long::class.javaObjectType)
            .setParameter("startDate", startDate)
            .singleResult
            .onItem().transform { result -> result.toLong() }

        return Uni.combine().all().unis(totalVisitsQuery, totalPagesQuery, totalDomainsQuery)
            .asTuple()
            .onItem().transform { tuple ->
                Triple(tuple.item1, tuple.item2, tuple.item3)
            }
    }
}
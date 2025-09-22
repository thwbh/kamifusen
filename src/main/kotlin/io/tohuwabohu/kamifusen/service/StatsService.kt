package io.tohuwabohu.kamifusen.service

import io.quarkus.hibernate.reactive.panache.Panache
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.api.generated.model.AggregatedStatsDto
import io.tohuwabohu.kamifusen.api.generated.model.DomainStatDataDto
import io.tohuwabohu.kamifusen.api.generated.model.TopPageDataDto
import io.tohuwabohu.kamifusen.api.generated.model.VisitTrendDataDto
import io.tohuwabohu.kamifusen.extensions.roundTo
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
            // Execute queries sequentially to avoid session corruption with Uni.combine()
            getVisitTrendData(session, days)
                .flatMap { visitData ->
                    getTopPagesData(session, startDate)
                        .flatMap { topPages ->
                            getDomainStatsData(session, startDate)
                                .flatMap { domainStats ->
                                    getTotalStats(session, startDate)
                                        .map { (totalVisits, totalPages, totalDomains) ->
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
                }
        }
    }

    private fun getVisitTrendData(session: Mutiny.Session, days: Int): Uni<List<VisitTrendDataDto>> {
        val startDate = LocalDateTime.now().minusDays(days.toLong())

        return session.createNamedQuery("Stats.getVisitTrendData", Tuple::class.java)
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
        return session.createNamedQuery("Stats.getTopPagesData", Tuple::class.java)
            .setParameter("startDate", startDate)
            .resultList
            .onItem().transform { results ->
                val totalVisits = results.sumOf { it.get(2, Long::class.javaObjectType) }

                val topPagesData = results.take(5).map { tuple ->
                    val domain = tuple.get(0, String::class.java)
                    val path = tuple.get(1, String::class.java)
                    val visits = tuple.get(2, Long::class.javaObjectType)
                    val percentage = if (totalVisits > 0) {
                        (visits.toDouble() / totalVisits.toDouble()) * 100
                    } else 0.0

                    TopPageDataDto(domain, path, visits, percentage.roundTo(2))
                }

                if (topPagesData.size < 5) return@transform topPagesData

                val rest = results.drop(5).sumOf { it.get(2, Long::class.javaObjectType) }

                val remainingPercentage = 100 - topPagesData.sumOf { it.percentage }
                val validPercentage = maxOf(0.0, remainingPercentage.roundTo(2))

                topPagesData + listOf(TopPageDataDto("Other", "*", rest, validPercentage))
            }
    }

    private fun getDomainStatsData(session: Mutiny.Session, startDate: LocalDateTime): Uni<List<DomainStatDataDto>> {
        return session.createNamedQuery("Stats.getDomainStatsData", Tuple::class.java)
            .setParameter("startDate", startDate)
            .resultList
            .onItem().transform { results ->
                val totalVisits = results.sumOf { it.get(1, Long::class.javaObjectType) }

                results.map { tuple ->
                    val domain = tuple.get(0, String::class.java)
                    val visits = tuple.get(1, Long::class.javaObjectType)
                    val percentage = if (totalVisits > 0) {
                        (visits.toDouble() / totalVisits.toDouble()) * 100
                    } else 0.0

                    DomainStatDataDto(domain, visits, percentage.roundTo(2))
                }
            }
    }

    private fun getTotalStats(session: Mutiny.Session, startDate: LocalDateTime): Uni<Triple<Long, Long, Long>> {
        // Execute queries sequentially to avoid session corruption
        return session.createNamedQuery("PageVisit.countTotalVisits", Long::class.javaObjectType)
            .setParameter("startDate", startDate)
            .singleResult
            .onItem().transform { result -> result.toLong()}
            .flatMap { totalVisits ->
                session.createNamedQuery("PageVisit.countTotalPages", Long::class.javaObjectType)
                    .setParameter("startDate", startDate)
                    .singleResult
                    .onItem().transform { result -> result.toLong() }
                    .flatMap { totalPages ->
                        session.createNamedQuery("PageVisit.countTotalDomains", Long::class.javaObjectType)
                            .setParameter("startDate", startDate)
                            .singleResult
                            .onItem().transform { result -> result.toLong() }
                            .map { totalDomains ->
                                Triple(totalVisits, totalPages, totalDomains)
                            }
                    }
            }
    }
}
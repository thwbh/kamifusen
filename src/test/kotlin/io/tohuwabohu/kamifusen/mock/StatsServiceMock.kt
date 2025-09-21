package io.tohuwabohu.kamifusen.mock

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.api.generated.model.AggregatedStatsDto
import io.tohuwabohu.kamifusen.api.generated.model.DomainStatDataDto
import io.tohuwabohu.kamifusen.api.generated.model.TopPageDataDto
import io.tohuwabohu.kamifusen.api.generated.model.VisitTrendDataDto
import io.tohuwabohu.kamifusen.service.StatsService

/**
 * Mock repository for stats aggregation operations as maintaining that in import.sql is especially cumbersome
 */
class StatsServiceMock : StatsService() {

    override fun getAggregatedStats(timeRange: String): Uni<AggregatedStatsDto> {
        // Mock data for testing
        val visitData = listOf(
            VisitTrendDataDto(label = "Mon", category = "normal", value = 120),
            VisitTrendDataDto(label = "Tue", category = "low", value = 80),
            VisitTrendDataDto(label = "Wed", category = "high", value = 150),
            VisitTrendDataDto(label = "Thu", category = "normal", value = 95),
            VisitTrendDataDto(label = "Fri", category = "high", value = 200),
            VisitTrendDataDto(label = "Sat", category = "low", value = 50),
            VisitTrendDataDto(label = "Sun", category = "low", value = 75)
        )

        val topPages = listOf(
            TopPageDataDto(path = "/home", percentage = 45.0, visits = 500),
            TopPageDataDto(path = "/about", percentage = 27.0, visits = 300),
            TopPageDataDto(path = "/contact", percentage = 18.0, visits = 200),
            TopPageDataDto(path = "/blog", percentage = 9.0, visits = 100),
            TopPageDataDto(path = "/other", percentage = 1.0, visits = 50)
        )

        val domainStats = listOf(
            DomainStatDataDto(domain = "example.com", percentage = 65.0, visits = 800),
            DomainStatDataDto(domain = "test.org", percentage = 35.0, visits = 400)
        )

        return Uni.createFrom().item(
            AggregatedStatsDto(
                visitData = visitData,
                topPages = topPages,
                domainStats = domainStats,
                totalVisits = 1200L,
                totalPages = 15L,
                totalDomains = 2L
            )
        )
    }
}
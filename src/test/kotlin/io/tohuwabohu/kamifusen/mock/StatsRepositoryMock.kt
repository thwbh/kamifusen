package io.tohuwabohu.kamifusen.mock

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.service.dto.*
import java.time.LocalDateTime

class StatsRepositoryMock : StatsRepository() {

    override fun getAggregatedStats(timeRange: String): Uni<AggregatedStatsDto> {
        // Mock data for testing
        val visitData = listOf(
            VisitTrendDataDto("Mon", 120, "normal"),
            VisitTrendDataDto("Tue", 80, "low"),
            VisitTrendDataDto("Wed", 150, "high"),
            VisitTrendDataDto("Thu", 95, "normal"),
            VisitTrendDataDto("Fri", 200, "high"),
            VisitTrendDataDto("Sat", 50, "low"),
            VisitTrendDataDto("Sun", 75, "low")
        )

        val topPages = listOf(
            TopPageDataDto("/home", 500, 45),
            TopPageDataDto("/about", 300, 27),
            TopPageDataDto("/contact", 200, 18),
            TopPageDataDto("/blog", 100, 9),
            TopPageDataDto("/other", 50, 1)
        )

        val domainStats = listOf(
            DomainStatDataDto("example.com", 800, 65),
            DomainStatDataDto("test.org", 400, 35)
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
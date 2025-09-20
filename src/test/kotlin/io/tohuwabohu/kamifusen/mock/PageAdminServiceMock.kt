package io.tohuwabohu.kamifusen.mock

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.PageRepository
import io.tohuwabohu.kamifusen.crud.PageVisitRepository
import io.tohuwabohu.kamifusen.service.PageAdminService
import io.tohuwabohu.kamifusen.service.dto.PageWithStatsDto
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime
import java.util.*

class PageAdminServiceMock(pageRepository: PageRepository,
                           pageVisitRepository: PageVisitRepository
) : PageAdminService(pageRepository, pageVisitRepository) {

    val pagesWithStats = mutableListOf<PageWithStatsDto>()

    init {
        // Add some sample data
        pagesWithStats.addAll(listOf(
            PageWithStatsDto(
                id = UUID.randomUUID(),
                path = "/home",
                domain = "example.com",
                pageAdded = LocalDateTime.now().minusDays(10),
                lastHit = LocalDateTime.now().minusHours(2),
                visitCount = 500L
            ),
            PageWithStatsDto(
                id = UUID.randomUUID(),
                path = "/about",
                domain = "example.com",
                pageAdded = LocalDateTime.now().minusDays(5),
                lastHit = LocalDateTime.now().minusHours(1),
                visitCount = 300L
            ),
            PageWithStatsDto(
                id = UUID.randomUUID(),
                path = "/test-page",
                domain = "test.org",
                pageAdded = LocalDateTime.now().minusDays(2),
                lastHit = LocalDateTime.now().minusMinutes(30),
                visitCount = 100L
            )
        ))
    }

    override fun getPagesWithStats(): Uni<List<PageWithStatsDto>> {
        return Uni.createFrom().item(pagesWithStats)
    }
}
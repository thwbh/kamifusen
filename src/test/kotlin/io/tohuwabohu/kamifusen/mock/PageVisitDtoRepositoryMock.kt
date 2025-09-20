package io.tohuwabohu.kamifusen.mock

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.service.dto.PageVisitDto
import io.tohuwabohu.kamifusen.service.dto.PageVisitDtoRepository
import java.time.LocalDateTime
import java.util.*

class PageVisitDtoRepositoryMock : PageVisitDtoRepository() {

    val pageVisitDtos = mutableListOf<PageVisitDto>()

    init {
        // Add some sample data
        pageVisitDtos.addAll(listOf(
            PageVisitDto(
                id = UUID.randomUUID(),
                path = "/home",
                domain = "example.com",
                pageAdded = LocalDateTime.now().minusDays(10),
                visits = 500L
            ),
            PageVisitDto(
                id = UUID.randomUUID(),
                path = "/about",
                domain = "example.com",
                pageAdded = LocalDateTime.now().minusDays(5),
                visits = 300L
            ),
            PageVisitDto(
                id = UUID.randomUUID(),
                path = "/test-page",
                domain = "test.org",
                pageAdded = LocalDateTime.now().minusDays(2),
                visits = 100L
            )
        ))
    }

    override fun getAllPageVisits(): Uni<List<PageVisitDto>> {
        return Uni.createFrom().item(pageVisitDtos)
    }
}
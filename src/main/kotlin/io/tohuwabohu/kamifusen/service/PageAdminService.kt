package io.tohuwabohu.kamifusen.service

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.Page
import io.tohuwabohu.kamifusen.crud.PageRepository
import io.tohuwabohu.kamifusen.crud.PageVisitRepository
import io.tohuwabohu.kamifusen.service.dto.PageWithStatsDto
import jakarta.enterprise.context.ApplicationScoped

/**
 * Service for admin-specific page operations
 */
@ApplicationScoped
class PageAdminService(
    private val pageRepository: PageRepository,
    private val pageVisitRepository: PageVisitRepository
) {

    /**
     * Gets all pages with their visit counts for admin UI
     */
    fun getPagesWithStats(): Uni<List<PageWithStatsDto>> {
        return pageRepository.listAllPages()
            .flatMap { pages ->
                if (pages.isEmpty()) {
                    Uni.createFrom().item(emptyList<PageWithStatsDto>())
                } else {
                    // Process pages one by one to avoid complex type inference
                    processPages(pages, emptyList())
                }
            }
    }

    private fun processPages(remainingPages: List<Page>, processedPages: List<PageWithStatsDto>): Uni<List<PageWithStatsDto>> {
        if (remainingPages.isEmpty()) {
            return Uni.createFrom().item(processedPages)
        }

        val page = remainingPages.first()
        val restPages = remainingPages.drop(1)

        return pageVisitRepository.countVisits(page.id)
            .map { visitCount ->
                PageWithStatsDto(
                    id = page.id,
                    path = page.path,
                    domain = page.domain,
                    pageAdded = page.pageAdded,
                    lastHit = page.lastHit,
                    visitCount = visitCount
                )
            }
            .flatMap { pageWithStats ->
                processPages(restPages, processedPages + pageWithStats)
            }
    }
}
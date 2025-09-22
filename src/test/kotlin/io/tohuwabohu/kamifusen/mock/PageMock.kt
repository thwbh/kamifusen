package io.tohuwabohu.kamifusen.mock

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.service.crud.Page
import io.tohuwabohu.kamifusen.service.crud.PageRepository
import java.util.*

/**
 * Mock repository for non-disruptive CRUD operations on Page entity
 */
class PageRepositoryMock : PageRepository() {
    val pages = mutableListOf<Page>()

    override fun findByPageId(id: UUID): Uni<Page?> {
        return pages.find { it.id == id }.let { Uni.createFrom().item(it) }
    }

    override fun addPageIfAbsent(path: String, domain: String): Uni<Page?> {
        val existingPage = pages.find { it.path == path && it.domain == domain }

        if (existingPage == null) pages.add(Page(UUID.randomUUID(), path, domain))

        return Uni.createFrom().item(pages.find { it.path == path && it.domain == domain })
    }

    override fun findPageByPathAndDomain(path: String, domain: String): Uni<Page?> {
        return Uni.createFrom().item(pages.find { it.path == path && it.domain == domain })
    }

    override fun listAllPages(): Uni<List<Page>> {
        return Uni.createFrom().item(pages)
    }

    override fun addPage(path: String, domain: String): Uni<Page> {
        val page = Page(UUID.randomUUID(), path, domain)

        pages.add(page)

        return Uni.createFrom().item(page)
    }
}
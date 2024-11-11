package io.tohuwabohu.kamifusen.mock

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.DomainGroup
import io.tohuwabohu.kamifusen.crud.Page
import io.tohuwabohu.kamifusen.crud.PageRepository
import java.util.*

class PageRepositoryMock : PageRepository() {
    val pages = mutableListOf<Page>()

    override fun findByPageId(id: UUID): Uni<Page?> {
        return pages.find { it.id == id }.let { Uni.createFrom().item(it) }
    }

    override fun addPageIfAbsent(path: String, domainGroup: DomainGroup): Uni<Page?> {
        val existingPage = pages.find { it.path == path && it.domainGroup == domainGroup }

        if (existingPage == null) pages.add(Page(UUID.randomUUID(), path, domainGroup))

        return Uni.createFrom().item(pages.find { it.path == path && it.domainGroup == domainGroup })
    }

    override fun findPageByPathAndDomainGroup(path: String, domainGroup: DomainGroup): Uni<Page?> {
        return Uni.createFrom().item(pages.find { it.path == path && it.domainGroup == domainGroup })
    }

    override fun listAllPages(): Uni<List<Page>> {
        return Uni.createFrom().item(pages)
    }

    override fun addPage(path: String, domainGroup: DomainGroup): Uni<Page> {
        val page = Page(UUID.randomUUID(), path, domainGroup)

        pages.add(page)

        return Uni.createFrom().item(page)
    }
}
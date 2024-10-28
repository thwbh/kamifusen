package io.tohuwabohu.kamifusen.mock

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.Page
import io.tohuwabohu.kamifusen.crud.PageRepository
import java.util.*

class PageRepositoryMock : PageRepository() {
    val pages = mutableListOf<Page>()

    override fun findByPageId(id: UUID): Uni<Page?> {
        return pages.find { it.id == id }.let { Uni.createFrom().item(it) }
    }

    override fun findPageByPath(path: String): Uni<Page?> {
        return Uni.createFrom().item(pages.find { it.path == path })
    }

    override fun addPage(path: String): Uni<Page> {
        val page = Page(UUID.randomUUID(), path)

        pages.add(page)

        return Uni.createFrom().item(page)
    }
}
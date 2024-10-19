package io.tohuwabohu.kamifusen.mock

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.PageVisit
import io.tohuwabohu.kamifusen.crud.PageVisitRepository
import java.util.*

class PageVisitRepositoryMock : PageVisitRepository() {
    val visits = mutableListOf<PageVisit>()

    override fun countVisits(pageId: UUID): Uni<Long> = Uni.createFrom().item(visits.count { it.pageId == pageId }.toLong())

    override fun countVisitsForVisitor(pageId: UUID, visitorId: UUID): Uni<Long> = Uni.createFrom().item(visits.count { it.pageId == pageId && it.visitorId == visitorId }.toLong())

    override fun addVisit(pageId: UUID, visitorId: UUID): Uni<PageVisit> {
        val pageVisit = PageVisit(pageId, visitorId)

        visits.add(pageVisit)

        return Uni.createFrom().item(pageVisit)
    }
}
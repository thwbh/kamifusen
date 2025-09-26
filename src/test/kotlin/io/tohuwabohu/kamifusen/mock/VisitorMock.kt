package io.tohuwabohu.kamifusen.mock

import io.quarkus.runtime.util.HashUtil.sha256
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.service.crud.Visitor
import io.tohuwabohu.kamifusen.service.crud.VisitorRepository
import java.time.LocalDateTime
import java.util.*

/**
 * Mock repository for non-disruptive CRUD operations on Visitor entity
 */
class VisitorRepositoryMock : VisitorRepository() {
    val visitors = mutableListOf<Visitor>()

    override fun addVisitor(remoteAddress: String, userAgent: String, referrer: String?, country: String?): Uni<Visitor> {
        val visitor = Visitor(
            id = UUID.randomUUID(),
            info = sha256("$remoteAddress $userAgent"),
            userAgent = userAgent,
            referrer = referrer,
            country = country
        )

        visitors.add(visitor)

        return Uni.createFrom().item(visitor)
    }


    override fun findByInfo(remoteAddress: String, userAgent: String): Uni<Visitor?> =
        Uni.createFrom().item(visitors.find { it.info == sha256("$remoteAddress $userAgent") })

    override fun updateLastSeen(visitorId: UUID): Uni<Visitor?> {
        val visitor = visitors.find { it.id == visitorId }
        visitor?.let {
            it.lastSeen = LocalDateTime.now()
        }
        return Uni.createFrom().item(visitor)
    }
}
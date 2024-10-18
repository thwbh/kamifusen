package io.tohuwabohu.kamifusen.mock

import io.quarkus.runtime.util.HashUtil.sha256
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.Visitor
import io.tohuwabohu.kamifusen.crud.VisitorRepository
import java.util.*

class VisitorMockRepository : VisitorRepository() {
    val visitors = mutableListOf<Visitor>()

    override fun addVisitor(remoteAddress: String, userAgent: String): Uni<Visitor> {
        val visitor = Visitor(UUID.randomUUID(), sha256("$remoteAddress $userAgent"))

        visitors.add(visitor)

        return Uni.createFrom().item(visitor)
    }

    override fun findByInfo(remoteAddress: String, userAgent: String): Uni<Visitor?> = Uni.createFrom().item(visitors.find { it.info == sha256("$remoteAddress $userAgent") })
}
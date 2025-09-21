package io.tohuwabohu.kamifusen.service.crud

import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheRepositoryBase
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy
import java.time.LocalDateTime
import java.util.*

@Entity
data class Session(
    @Id
    var id: UUID,
    var visitorId: UUID,
    val startTime: LocalDateTime = LocalDateTime.now(),
    var endTime: LocalDateTime? = null,
    var pageViews: Int = 0,
    var isActive: Boolean = true
) : PanacheEntityBase {
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as Session

        return id == other.id
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id   ,   visitorId = $visitorId   ,   startTime = $startTime   ,   isActive = $isActive )"
    }
}

@ApplicationScoped
class SessionRepository : PanacheRepositoryBase<Session, UUID> {
    @WithTransaction
    fun startSession(visitorId: UUID): Uni<Session> {
        val session = Session(
            id = UUID.randomUUID(),
            visitorId = visitorId
        )
        return persist(session)
    }

    @WithTransaction
    fun endSession(sessionId: UUID): Uni<Session?> {
        return findById(sessionId).onItem().ifNotNull().call { session ->
            session.endTime = LocalDateTime.now()
            session.isActive = false
            persist(session)
        }
    }

    @WithTransaction
    fun incrementPageViews(sessionId: UUID): Uni<Session?> {
        return findById(sessionId).onItem().ifNotNull().call { session ->
            session.pageViews++
            persist(session)
        }
    }

    fun findActiveSessionByVisitor(visitorId: UUID): Uni<Session?> =
        find("visitorId = ?1 AND isActive = true", visitorId).firstResult()

    // Sliding window session support
    fun findOrCreateSessionForVisitor(visitorId: UUID, hasRecentActivity: Boolean): Uni<Session> {
        return if (hasRecentActivity) {
            // Continue existing session - find most recent session for this visitor
            find("visitorId = ?1 ORDER BY startTime DESC", visitorId)
                .firstResult()
                .onItem().ifNull().switchTo {
                    // No existing session, create new one
                    startSession(visitorId)
                }
                .onItem().transform { session -> session!! } // We know it's not null after switchTo
        } else {
            // Start new session
            startSession(visitorId)
        }
    }

    fun countActiveSessions(): Uni<Long> =
        count("isActive = true")

    fun getActiveSessionsInTimeRange(minutes: Int): Uni<List<Session>> =
        list("isActive = true AND startTime >= ?1", LocalDateTime.now().minusMinutes(minutes.toLong()))

    fun getAverageSessionDuration(): Uni<Double> {
        // This would need a native query for proper calculation
        return Uni.createFrom().item(0.0) // Placeholder
    }
}
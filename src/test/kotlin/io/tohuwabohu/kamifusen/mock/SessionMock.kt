package io.tohuwabohu.kamifusen.mock

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.service.crud.Session
import io.tohuwabohu.kamifusen.service.crud.SessionRepository
import java.time.LocalDateTime
import java.util.*

class SessionRepositoryMock : SessionRepository() {
    val sessions = mutableListOf<Session>()

    override fun startSession(visitorId: UUID): Uni<Session> {
        val session = Session(
            id = UUID.randomUUID(),
            visitorId = visitorId
        )
        sessions.add(session)
        return Uni.createFrom().item(session)
    }

    override fun updateSessionActivity(sessionId: UUID): Uni<Session?> {
        val session = sessions.find { it.id == sessionId }
        session?.let {
            it.updatedAt = LocalDateTime.now()
        }
        return Uni.createFrom().item(session)
    }

    override fun incrementPageViews(sessionId: UUID): Uni<Session?> {
        val session = sessions.find { it.id == sessionId }
        session?.let {
            it.pageViews++
            it.updatedAt = LocalDateTime.now()
        }
        return Uni.createFrom().item(session)
    }

    override fun findActiveSessionByVisitor(visitorId: UUID, minutes: Int): Uni<Session?> {
        val cutoffTime = LocalDateTime.now().minusMinutes(minutes.toLong())
        return Uni.createFrom().item(
            sessions.filter { it.visitorId == visitorId && it.updatedAt >= cutoffTime }
                .maxByOrNull { it.updatedAt }
        )
    }

    override fun countActiveSessions(minutes: Int): Uni<Long> {
        val cutoffTime = LocalDateTime.now().minusMinutes(minutes.toLong())
        return Uni.createFrom().item(sessions.count { it.updatedAt >= cutoffTime }.toLong())
    }

    override fun getActiveSessionsInTimeRange(minutes: Int): Uni<List<Session>> {
        val cutoffTime = LocalDateTime.now().minusMinutes(minutes.toLong())
        return Uni.createFrom().item(sessions.filter { it.updatedAt >= cutoffTime })
    }

    override fun getAverageSessionDuration(): Uni<Double> {
        // For sliding window sessions, calculate duration as time between start and last update
        if (sessions.isEmpty()) return Uni.createFrom().item(0.0)

        val totalMinutes = sessions.sumOf {
            java.time.Duration.between(it.startTime, it.updatedAt).toMinutes()
        }
        return Uni.createFrom().item(totalMinutes.toDouble() / sessions.size)
    }


    // Sliding window session support - completely self-contained like other mocks
    override fun findOrCreateSessionForVisitor(visitorId: UUID, hasRecentActivity: Boolean): Uni<Session> {
        return if (hasRecentActivity) {
            // Continue existing session - find most recent session for this visitor
            val existingSession = sessions.filter { it.visitorId == visitorId }
                .maxByOrNull { it.startTime }

            if (existingSession != null) {
                Uni.createFrom().item(existingSession)
            } else {
                // No existing session, create new one directly in mock
                val newSession = Session(
                    id = UUID.randomUUID(),
                    visitorId = visitorId
                )
                sessions.add(newSession)
                Uni.createFrom().item(newSession)
            }
        } else {
            // Start new session
            val newSession = Session(
                id = UUID.randomUUID(),
                visitorId = visitorId
            )
            sessions.add(newSession)
            Uni.createFrom().item(newSession)
        }
    }
}
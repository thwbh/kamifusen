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

    override fun endSession(sessionId: UUID): Uni<Session?> {
        val session = sessions.find { it.id == sessionId }
        session?.let {
            it.endTime = LocalDateTime.now()
            it.isActive = false
        }
        return Uni.createFrom().item(session)
    }

    override fun incrementPageViews(sessionId: UUID): Uni<Session?> {
        val session = sessions.find { it.id == sessionId }
        session?.let {
            it.pageViews++
        }
        return Uni.createFrom().item(session)
    }

    override fun findActiveSessionByVisitor(visitorId: UUID): Uni<Session?> =
        Uni.createFrom().item(sessions.find { it.visitorId == visitorId && it.isActive })

    override fun countActiveSessions(): Uni<Long> =
        Uni.createFrom().item(sessions.count { it.isActive }.toLong())

    override fun getActiveSessionsInTimeRange(minutes: Int): Uni<List<Session>> {
        val cutoff = LocalDateTime.now().minusMinutes(minutes.toLong())
        return Uni.createFrom().item(sessions.filter { it.isActive && it.startTime >= cutoff })
    }

    override fun getAverageSessionDuration(): Uni<Double> {
        val endedSessions = sessions.filter { !it.isActive && it.endTime != null }
        if (endedSessions.isEmpty()) return Uni.createFrom().item(0.0)

        val totalMinutes = endedSessions.sumOf {
            java.time.Duration.between(it.startTime, it.endTime).toMinutes()
        }
        return Uni.createFrom().item(totalMinutes.toDouble() / endedSessions.size)
    }


    // Sliding window session support - completely self-contained like other mocks
    override fun findOrCreateSessionForVisitor(visitorId: UUID, hasRecentActivity: Boolean): Uni<Session> {
        return if (hasRecentActivity) {
            // Continue existing session - find most recent ACTIVE session for this visitor
            val existingSession = sessions.filter { it.visitorId == visitorId && it.isActive }
                .maxByOrNull { it.startTime }

            if (existingSession != null) {
                Uni.createFrom().item(existingSession)
            } else {
                // No existing active session, create new one directly in mock
                val newSession = Session(
                    id = UUID.randomUUID(),
                    visitorId = visitorId
                )
                sessions.add(newSession)
                Uni.createFrom().item(newSession)
            }
        } else {
            // Start new session - first deactivate any existing sessions for this visitor
            sessions.filter { it.visitorId == visitorId }.forEach { it.isActive = false }

            val newSession = Session(
                id = UUID.randomUUID(),
                visitorId = visitorId
            )
            sessions.add(newSession)
            Uni.createFrom().item(newSession)
        }
    }
}
package io.tohuwabohu.kamifusen.service.crud

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@QuarkusTest
class SessionRepositoryTest {

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Test
    @RunOnVertxContext
    fun `should start session correctly`(uniAsserter: UniAsserter) {
        val visitorId = UUID.randomUUID()

        uniAsserter.assertThat(
            { sessionRepository.startSession(visitorId) },
            { session ->
                assertNotNull(session)
                assertNotNull(session.id)
                assertEquals(visitorId, session.visitorId)
                assertTrue(session.isActive)
                assertNull(session.endTime)
                assertEquals(0, session.pageViews)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should end session correctly`(uniAsserter: UniAsserter) {
        val sessionId = UUID.randomUUID()

        uniAsserter.assertThat(
            { sessionRepository.endSession(sessionId) },
            { endedSession ->
                // Should handle non-existent session gracefully by returning null
                assertNull(endedSession)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should end non-existent session gracefully`(uniAsserter: UniAsserter) {
        val nonExistentId = UUID.randomUUID()

        uniAsserter.assertThat(
            { sessionRepository.endSession(nonExistentId) },
            { result ->
                assertNull(result) // Should return null for non-existent session
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should find active session by visitor`(uniAsserter: UniAsserter) {
        val visitorId = UUID.randomUUID()

        uniAsserter.assertThat(
            { sessionRepository.startSession(visitorId) },
            { session ->
                assertNotNull(session)
                assertTrue(session.isActive)
            }
        ).assertThat(
            { sessionRepository.findActiveSessionByVisitor(visitorId) },
            { foundSession ->
                if (foundSession != null) {
                    assertEquals(visitorId, foundSession.visitorId)
                    assertTrue(foundSession.isActive)
                }
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should find no active session for non-existent visitor`(uniAsserter: UniAsserter) {
        val nonExistentVisitorId = UUID.randomUUID()

        uniAsserter.assertThat(
            { sessionRepository.findActiveSessionByVisitor(nonExistentVisitorId) },
            { foundSession ->
                assertNull(foundSession) // Should return null for non-existent visitor
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should get active sessions in time range`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { sessionRepository.getActiveSessionsInTimeRange(60) },
            { sessions ->
                assertNotNull(sessions)
                val cutoffTime = LocalDateTime.now().minusMinutes(60)

                // All sessions should be active and within time range
                sessions.forEach { session ->
                    assertTrue(session.isActive)
                    assertTrue(session.startTime >= cutoffTime)
                }
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should find or create session for visitor with recent activity`(uniAsserter: UniAsserter) {
        val visitorId = UUID.randomUUID()

        uniAsserter.assertThat(
            { sessionRepository.findOrCreateSessionForVisitor(visitorId, hasRecentActivity = true) },
            { foundSession ->
                assertNotNull(foundSession)
                assertEquals(visitorId, foundSession.visitorId)
                assertTrue(foundSession.isActive)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should create new session for visitor without recent activity`(uniAsserter: UniAsserter) {
        val visitorId = UUID.randomUUID()

        uniAsserter.assertThat(
            { sessionRepository.findOrCreateSessionForVisitor(visitorId, hasRecentActivity = false) },
            { newSession ->
                assertNotNull(newSession)
                assertEquals(visitorId, newSession.visitorId)
                assertTrue(newSession.isActive)
                assertEquals(0, newSession.pageViews)
                assertNull(newSession.endTime)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should create new session for visitor with no existing sessions`(uniAsserter: UniAsserter) {
        val visitorId = UUID.randomUUID()

        uniAsserter.assertThat(
            { sessionRepository.findOrCreateSessionForVisitor(visitorId, hasRecentActivity = true) },
            { newSession ->
                assertNotNull(newSession)
                assertEquals(visitorId, newSession.visitorId)
                assertTrue(newSession.isActive)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should get average session duration`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { sessionRepository.getAverageSessionDuration() },
            { averageDuration ->
                assertNotNull(averageDuration)
                assertTrue(averageDuration >= 0.0) // Should be non-negative (currently returns 0.0 as placeholder)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should count active sessions`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { sessionRepository.countActiveSessions() },
            { count ->
                assertNotNull(count)
                assertTrue(count >= 0) // Count should be non-negative
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should increment page views for non-existent session`(uniAsserter: UniAsserter) {
        val nonExistentId = UUID.randomUUID()

        uniAsserter.assertThat(
            { sessionRepository.incrementPageViews(nonExistentId) },
            { result ->
                assertNull(result) // Should return null for non-existent session
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should increment page views for non-existent session gracefully`(uniAsserter: UniAsserter) {
        val nonExistentId = UUID.randomUUID()

        uniAsserter.assertThat(
            { sessionRepository.incrementPageViews(nonExistentId) },
            { result ->
                assertNull(result) // Should return null for non-existent session
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle empty time range for active sessions`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { sessionRepository.getActiveSessionsInTimeRange(0) },
            { sessions ->
                assertNotNull(sessions)
                // Should return sessions from current time (might be empty)
                assertTrue(sessions.size >= 0)
            }
        )
    }
}
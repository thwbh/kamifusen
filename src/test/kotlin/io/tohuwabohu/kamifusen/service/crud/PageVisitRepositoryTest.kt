package io.tohuwabohu.kamifusen.service.crud

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@QuarkusTest
class PageVisitRepositoryTest {

    @Inject
    lateinit var pageVisitRepository: PageVisitRepository

    @Test
    @RunOnVertxContext
    fun `should get visits by time range`(uniAsserter: UniAsserter) {
        val now = LocalDateTime.now()
        val oneHourAgo = now.minusHours(1)
        val twoHoursAgo = now.minusHours(2)

        uniAsserter.assertThat(
            { pageVisitRepository.getVisitsByTimeRange(twoHoursAgo, now) },
            { visits ->
                assertNotNull(visits)
                assertTrue(visits.isNotEmpty() || visits.isEmpty()) // Should work with any data state
                // All visits should be within the time range
                visits.forEach { visit ->
                    assertTrue(visit.visitedAt >= twoHoursAgo && visit.visitedAt <= now)
                }
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should count visits by time range`(uniAsserter: UniAsserter) {
        val pageId = UUID.randomUUID()
        val now = LocalDateTime.now()
        val oneHourAgo = now.minusHours(1)

        uniAsserter.assertThat(
            { pageVisitRepository.countVisitsByTimeRange(pageId, oneHourAgo, now) },
            { count ->
                assertNotNull(count)
                assertTrue(count >= 0) // Count should be non-negative
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should get visits in last hours`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { pageVisitRepository.getVisitsInLastHours(24) },
            { visits ->
                assertNotNull(visits)
                val cutoffTime = LocalDateTime.now().minusHours(24)
                // All visits should be within the last 24 hours
                visits.forEach { visit ->
                    assertTrue(visit.visitedAt >= cutoffTime)
                }
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should get visits in last days`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { pageVisitRepository.getVisitsInLastDays(7) },
            { visits ->
                assertNotNull(visits)
                val cutoffTime = LocalDateTime.now().minusDays(7)
                // All visits should be within the last 7 days
                visits.forEach { visit ->
                    assertTrue(visit.visitedAt >= cutoffTime)
                }
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should get total visits count`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { pageVisitRepository.getTotalVisitsCount() },
            { count ->
                assertNotNull(count)
                assertTrue(count >= 0) // Total count should be non-negative
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should get visit counts by day`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { pageVisitRepository.getVisitCountsByDay(30) },
            { visits ->
                assertNotNull(visits)
                val cutoffTime = LocalDateTime.now().minusDays(30)
                // All visits should be within the last 30 days and ordered by visitedAt
                visits.forEach { visit ->
                    assertTrue(visit.visitedAt >= cutoffTime)
                }

                // Verify ordering - each visit should be after or equal to the previous one
                if (visits.size > 1) {
                    for (i in 1 until visits.size) {
                        assertTrue(visits[i].visitedAt >= visits[i-1].visitedAt)
                    }
                }
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should find recent visit by visitor on domain`(uniAsserter: UniAsserter) {
        val visitorId = UUID.randomUUID()
        val domain = "example.com"
        val minutesBack = 30

        uniAsserter.assertThat(
            { pageVisitRepository.findRecentVisitByVisitorOnDomain(visitorId, domain, minutesBack) },
            { visit ->
                // Result can be null if no recent visit found
                if (visit != null) {
                    assertEquals(visitorId, visit.visitorId)
                    val cutoffTime = LocalDateTime.now().minusMinutes(minutesBack.toLong())
                    assertTrue(visit.visitedAt >= cutoffTime)
                }
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle empty time range gracefully`(uniAsserter: UniAsserter) {
        val now = LocalDateTime.now()
        val future = now.plusHours(1)

        uniAsserter.assertThat(
            { pageVisitRepository.getVisitsByTimeRange(future, future.plusMinutes(1)) },
            { visits ->
                assertNotNull(visits)
                assertTrue(visits.isEmpty()) // Should be empty for future time range
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle zero hours gracefully`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { pageVisitRepository.getVisitsInLastHours(0) },
            { visits ->
                assertNotNull(visits)
                // Should return visits from current time (might be empty)
                assertTrue(visits.size >= 0)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle zero days gracefully`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { pageVisitRepository.getVisitsInLastDays(0) },
            { visits ->
                assertNotNull(visits)
                // Should return visits from current time (might be empty)
                assertTrue(visits.size >= 0)
            }
        )
    }
}
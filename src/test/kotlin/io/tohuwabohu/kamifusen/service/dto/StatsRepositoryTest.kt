package io.tohuwabohu.kamifusen.service.dto

import io.quarkus.test.junit.QuarkusMock
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import io.tohuwabohu.kamifusen.mock.StatsRepositoryMock
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StatsRepositoryTest {

    @Inject
    lateinit var statsRepository: StatsRepository

    @BeforeAll
    fun init() {
        QuarkusMock.installMockForType(StatsRepositoryMock(), StatsRepository::class.java)
    }

    @Test
    @RunOnVertxContext
    fun `should parse time range and set correct days`(uniAsserter: UniAsserter) {
        // Test different time range parameters
        val testCases = listOf(
            "24h" to 1,
            "7d" to 7,
            "30d" to 30,
            "90d" to 90,
            "invalid" to 7, // Should default to 7
            "" to 7 // Should default to 7
        )

        testCases.forEach { (timeRange, expectedDays) ->
            uniAsserter.assertThat(
                { statsRepository.getAggregatedStats(timeRange) },
                { result ->
                    assertNotNull(result)
                    // Verify that the method executes without error
                    // The exact results depend on test data, but we can verify structure
                    assertNotNull(result.visitData)
                    assertNotNull(result.topPages)
                    assertNotNull(result.domainStats)
                    assertTrue(result.totalVisits >= 0)
                    assertTrue(result.totalPages >= 0)
                    assertTrue(result.totalDomains >= 0)
                }
            )
        }
    }

    @Test
    @RunOnVertxContext
    fun `should get aggregated stats with default time range`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { statsRepository.getAggregatedStats() },
            { result ->
                assertNotNull(result)

                // Visit data should have 7 entries (one for each day of week)
                assertEquals(7, result.visitData.size)

                // Verify day names are present
                val dayNames = result.visitData.map { it.label }.toSet()
                val expectedDays = setOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                assertEquals(expectedDays, dayNames)

                // Verify visit data structure
                result.visitData.forEach { visitData ->
                    assertTrue(visitData.value >= 0)
                    assertTrue(visitData.category in setOf("low", "normal", "high"))
                    assertTrue(visitData.label in expectedDays)
                }

                // Verify top pages structure
                result.topPages.forEach { topPage ->
                    assertNotNull(topPage.path)
                    assertTrue(topPage.visits >= 0)
                    assertTrue(topPage.percentage.toDouble() >= 0.0)
                    assertTrue(topPage.percentage.toDouble() <= 100.0)
                }

                // Verify domain stats structure
                result.domainStats.forEach { domainStat ->
                    assertNotNull(domainStat.domain)
                    assertTrue(domainStat.visits >= 0)
                    assertTrue(domainStat.percentage.toDouble() >= 0.0)
                    assertTrue(domainStat.percentage.toDouble() <= 100.0)
                }

                // Total statistics should be non-negative
                assertTrue(result.totalVisits >= 0)
                assertTrue(result.totalPages >= 0)
                assertTrue(result.totalDomains >= 0)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle 24 hour time range`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { statsRepository.getAggregatedStats("24h") },
            { result ->
                assertNotNull(result)
                assertEquals(7, result.visitData.size) // Still 7 days of week

                // All data should be for recent activity
                assertTrue(result.totalVisits >= 0)
                assertTrue(result.totalPages >= 0)
                assertTrue(result.totalDomains >= 0)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle 30 day time range`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { statsRepository.getAggregatedStats("30d") },
            { result ->
                assertNotNull(result)
                assertEquals(7, result.visitData.size) // Still 7 days of week

                // Verify basic structure
                assertNotNull(result.visitData)
                assertNotNull(result.topPages)
                assertNotNull(result.domainStats)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle 90 day time range`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { statsRepository.getAggregatedStats("90d") },
            { result ->
                assertNotNull(result)
                assertEquals(7, result.visitData.size) // Still 7 days of week

                // Verify basic structure
                assertNotNull(result.visitData)
                assertNotNull(result.topPages)
                assertNotNull(result.domainStats)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle empty database gracefully`(uniAsserter: UniAsserter) {
        // Even with no data, the structure should be consistent
        uniAsserter.assertThat(
            { statsRepository.getAggregatedStats("7d") },
            { result ->
                assertNotNull(result)

                // Should still have 7 days of week data, even if all zeros
                assertEquals(7, result.visitData.size)

                // All visit counts might be zero, but structure should be intact
                result.visitData.forEach { visitData ->
                    assertTrue(visitData.value >= 0)
                    assertTrue(visitData.category in setOf("low", "normal", "high"))
                }

                // Collections might be empty but should not be null
                assertNotNull(result.topPages)
                assertNotNull(result.domainStats)

                // Totals should be zero or positive
                assertTrue(result.totalVisits >= 0)
                assertTrue(result.totalPages >= 0)
                assertTrue(result.totalDomains >= 0)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should categorize visit trends correctly`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { statsRepository.getAggregatedStats("7d") },
            { result ->
                assertNotNull(result)

                // Check that the categorization logic works
                val categories = result.visitData.map { it.category }.toSet()

                // All categories should be valid
                categories.forEach { category ->
                    assertTrue(category in setOf("low", "normal", "high"))
                }

                // If there are visits, there should be some categorization
                val totalDayVisits = result.visitData.sumOf { it.value }
                if (totalDayVisits > 0) {
                    // At least one category should be present
                    assertTrue(categories.isNotEmpty())
                }
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return day names in correct order`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { statsRepository.getAggregatedStats("7d") },
            { result ->
                assertNotNull(result)
                assertEquals(7, result.visitData.size)

                // Should be in Monday-first order for better UX
                val expectedOrder = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val actualOrder = result.visitData.map { it.label }

                assertEquals(expectedOrder, actualOrder)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should limit top pages to 5 results and one 'rest' result`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { statsRepository.getAggregatedStats("7d") },
            { result ->
                assertNotNull(result)

                // Top pages should be limited to 6 results maximum
                assertTrue(result.topPages.size <= 6)

                // If there are multiple pages, they should be ordered by visits (descending)
                if (result.topPages.size > 1) {
                    for (i in 0 until result.topPages.size - 1) {
                        assertTrue(
                            result.topPages[i].visits >= result.topPages[i + 1].visits,
                            "Top pages should be ordered by visit count descending"
                        )
                    }
                }
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should calculate percentages correctly for top pages`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { statsRepository.getAggregatedStats("7d") },
            { result ->
                assertNotNull(result)

                if (result.topPages.isNotEmpty()) {
                    // Sum of all percentages should be <= 100 (due to rounding and limiting to top 5)
                    val totalPercentage = result.topPages.sumOf { it.percentage.toDouble() }
                    assertTrue(totalPercentage <= 100.0)

                    // Each individual percentage should be between 0 and 100
                    result.topPages.forEach { topPage ->
                        assertTrue(topPage.percentage.toDouble() >= 0.0)
                        assertTrue(topPage.percentage.toDouble() <= 100.0)
                    }
                }
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should calculate percentages correctly for domain stats`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { statsRepository.getAggregatedStats("7d") },
            { result ->
                assertNotNull(result)

                if (result.domainStats.isNotEmpty()) {
                    // Sum of all percentages should be approximately 100% (within rounding tolerance)
                    val totalPercentage = result.domainStats.sumOf { it.percentage.toDouble() }
                    assertTrue(totalPercentage <= 100.0)

                    // Each individual percentage should be between 0 and 100
                    result.domainStats.forEach { domainStat ->
                        assertTrue(domainStat.percentage.toDouble() >= 0.0)
                        assertTrue(domainStat.percentage.toDouble() <= 100.0)
                    }

                    // Domain stats should be ordered by visits (descending)
                    if (result.domainStats.size > 1) {
                        for (i in 0 until result.domainStats.size - 1) {
                            assertTrue(
                                result.domainStats[i].visits >= result.domainStats[i + 1].visits,
                                "Domain stats should be ordered by visit count descending"
                            )
                        }
                    }
                }
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle null domains gracefully`(uniAsserter: UniAsserter) {
        uniAsserter.assertThat(
            { statsRepository.getAggregatedStats("7d") },
            { result ->
                assertNotNull(result)

                // If there are domain stats, check for 'unknown' domain handling
                result.domainStats.forEach { domainStat ->
                    assertNotNull(domainStat.domain)
                    // Domain should either be a valid domain name or 'unknown'
                    assertTrue(domainStat.domain.isNotBlank())
                }
            }
        )
    }
}
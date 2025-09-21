package io.tohuwabohu.kamifusen.service

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.QuarkusMock
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import io.tohuwabohu.kamifusen.service.crud.Page
import io.tohuwabohu.kamifusen.service.crud.PageRepository
import io.tohuwabohu.kamifusen.service.crud.PageVisitRepository
import io.tohuwabohu.kamifusen.mock.PageRepositoryMock
import io.tohuwabohu.kamifusen.mock.PageVisitRepositoryMock
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.util.*

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PageAdminServiceTest {

    @Inject
    lateinit var pageAdminService: PageAdminService

    @Inject
    lateinit var pageRepository: PageRepository

    @Inject
    lateinit var pageVisitRepository: PageVisitRepository

    private lateinit var pageRepositoryMock: PageRepositoryMock
    private lateinit var pageVisitRepositoryMock: PageVisitRepositoryMock

    @BeforeAll
    fun init() {
        pageRepositoryMock = PageRepositoryMock()
        pageVisitRepositoryMock = PageVisitRepositoryMock()

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(pageVisitRepositoryMock, pageVisitRepository)
    }

    @Test
    @RunOnVertxContext
    fun `should return pages with stats when pages exist`(uniAsserter: UniAsserter) {
        // Setup test data in mocks
        val page1 = Page(UUID.randomUUID(), "/home", "example.com")
        val page2 = Page(UUID.randomUUID(), "/about", "example.com")
        val page3 = Page(UUID.randomUUID(), "/contact", "test.org")

        pageRepositoryMock.pages.clear()
        pageRepositoryMock.pages.addAll(listOf(page1, page2, page3))

        // Setup visit counts
        pageVisitRepositoryMock.visitCounts[page1.id] = 100L
        pageVisitRepositoryMock.visitCounts[page2.id] = 50L
        pageVisitRepositoryMock.visitCounts[page3.id] = 25L

        uniAsserter.assertThat(
            { pageAdminService.getPagesWithStats() },
            { result ->
                assertNotNull(result)
                assertEquals(3, result.size)

                val page1Result = result.find { it.path == "/home" }
                assertNotNull(page1Result)
                assertEquals("example.com", page1Result?.domain)
                assertEquals(100L, page1Result?.visitCount)
                assertEquals(page1.id, page1Result?.id)

                val page2Result = result.find { it.path == "/about" }
                assertNotNull(page2Result)
                assertEquals(50L, page2Result?.visitCount)

                val page3Result = result.find { it.path == "/contact" }
                assertNotNull(page3Result)
                assertEquals("test.org", page3Result?.domain)
                assertEquals(25L, page3Result?.visitCount)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return empty list when no pages exist`(uniAsserter: UniAsserter) {
        pageRepositoryMock.pages.clear()

        uniAsserter.assertThat(
            { pageAdminService.getPagesWithStats() },
            { result ->
                assertNotNull(result)
                assertTrue(result.isEmpty())
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle single page correctly`(uniAsserter: UniAsserter) {
        val singlePage = Page(UUID.randomUUID(), "/single", "single.com")

        pageRepositoryMock.pages.clear()
        pageRepositoryMock.pages.add(singlePage)
        pageVisitRepositoryMock.visitCounts[singlePage.id] = 42L

        uniAsserter.assertThat(
            { pageAdminService.getPagesWithStats() },
            { result ->
                assertNotNull(result)
                assertEquals(1, result.size)

                val pageResult = result.first()
                assertEquals("/single", pageResult.path)
                assertEquals("single.com", pageResult.domain)
                assertEquals(42L, pageResult.visitCount)
                assertEquals(singlePage.id, pageResult.id)
                assertEquals(singlePage.pageAdded, pageResult.pageAdded)
                assertEquals(singlePage.lastHit, pageResult.lastHit)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle pages with zero visit counts`(uniAsserter: UniAsserter) {
        val page = Page(UUID.randomUUID(), "/zero-visits", "example.com")

        pageRepositoryMock.pages.clear()
        pageRepositoryMock.pages.add(page)
        pageVisitRepositoryMock.visitCounts[page.id] = 0L

        uniAsserter.assertThat(
            { pageAdminService.getPagesWithStats() },
            { result ->
                assertNotNull(result)
                assertEquals(1, result.size)

                val pageResult = result.first()
                assertEquals(0L, pageResult.visitCount)
                assertEquals(page.id, pageResult.id)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should preserve page order from repository`(uniAsserter: UniAsserter) {
        val page1 = Page(UUID.randomUUID(), "/first", "example.com")
        val page2 = Page(UUID.randomUUID(), "/second", "example.com")
        val page3 = Page(UUID.randomUUID(), "/third", "example.com")

        pageRepositoryMock.pages.clear()
        pageRepositoryMock.pages.addAll(listOf(page1, page2, page3))

        pageVisitRepositoryMock.visitCounts[page1.id] = 10L
        pageVisitRepositoryMock.visitCounts[page2.id] = 20L
        pageVisitRepositoryMock.visitCounts[page3.id] = 30L

        uniAsserter.assertThat(
            { pageAdminService.getPagesWithStats() },
            { result ->
                assertNotNull(result)
                assertEquals(3, result.size)

                // Verify order is preserved (first, second, third)
                assertEquals("/first", result[0].path)
                assertEquals("/second", result[1].path)
                assertEquals("/third", result[2].path)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle large number of pages efficiently`(uniAsserter: UniAsserter) {
        pageRepositoryMock.pages.clear()

        // Create 50 test pages
        val testPages = (1..50).map { index ->
            val page = Page(UUID.randomUUID(), "/page-$index", "example.com")
            pageVisitRepositoryMock.visitCounts[page.id] = index.toLong()
            page
        }

        pageRepositoryMock.pages.addAll(testPages)

        uniAsserter.assertThat(
            { pageAdminService.getPagesWithStats() },
            { result ->
                assertNotNull(result)
                assertEquals(50, result.size)

                // Verify all pages are processed correctly
                result.forEachIndexed { index, pageWithStats ->
                    assertEquals("/page-${index + 1}", pageWithStats.path)
                    assertEquals((index + 1).toLong(), pageWithStats.visitCount)
                }
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle pages with missing visit counts`(uniAsserter: UniAsserter) {
        val page = Page(UUID.randomUUID(), "/missing-visits", "example.com")

        pageRepositoryMock.pages.clear()
        pageRepositoryMock.pages.add(page)
        // Don't add visit count to mock - should default to 0
        pageVisitRepositoryMock.visitCounts.remove(page.id)

        uniAsserter.assertThat(
            { pageAdminService.getPagesWithStats() },
            { result ->
                assertNotNull(result)
                assertEquals(1, result.size)

                val pageResult = result.first()
                assertEquals(0L, pageResult.visitCount) // Should default to 0
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should correctly map all page fields to DTO`(uniAsserter: UniAsserter) {
        val now = LocalDateTime.now()
        val pageAdded = now.minusDays(5)
        val lastHit = now.minusHours(2)
        val pageId = UUID.randomUUID()

        val page = Page(pageId, "/detailed-page", "detailed.com", pageAdded, lastHit)

        pageRepositoryMock.pages.clear()
        pageRepositoryMock.pages.add(page)
        pageVisitRepositoryMock.visitCounts[pageId] = 999L

        uniAsserter.assertThat(
            { pageAdminService.getPagesWithStats() },
            { result ->
                assertNotNull(result)
                assertEquals(1, result.size)

                val dto = result.first()
                assertEquals(pageId, dto.id)
                assertEquals("/detailed-page", dto.path)
                assertEquals("detailed.com", dto.domain)
                assertEquals(pageAdded, dto.pageAdded)
                assertEquals(lastHit, dto.lastHit)
                assertEquals(999L, dto.visitCount)
            }
        )
    }
}
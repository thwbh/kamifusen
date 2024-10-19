package io.tohuwabohu.kamifusen

import io.quarkus.runtime.util.HashUtil.sha256
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusMock
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.tohuwabohu.kamifusen.crud.*
import io.tohuwabohu.kamifusen.mock.ApiKeyRepositoryMock
import io.tohuwabohu.kamifusen.mock.PageRepositoryMock
import io.tohuwabohu.kamifusen.mock.PageVisitRepositoryMock
import io.tohuwabohu.kamifusen.mock.VisitorRepositoryMock
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.URLEncoder
import java.util.*

@QuarkusTest
@TestHTTPEndpoint(PageVisitResource::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PageVisitResourceTest {
    @Inject
    lateinit var pageRepository: PageRepository

    @Inject
    lateinit var pageVisitRepository: PageVisitRepository

    @Inject
    lateinit var visitorRepository: VisitorRepository

    @BeforeAll
    fun init() {
        QuarkusMock.installMockForType(ApiKeyRepositoryMock(), ApiKeyRepository::class.java)
    }

    @Test
    @RunOnVertxContext
    fun `should register a new visitor`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page"))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)
        QuarkusMock.installMockForInstance(VisitorRepositoryMock(), visitorRepository)

        Given {
            header("Content-Type", "text/plain")
            header("User-Agent", "test-user-agent")
            header("Authorization", "123e4567-e89b-12d3-a456-426614174000")
            body("/page/test-page")
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        }

        uniAsserter.assertThat(
            { visitorRepository.findByInfo("127.0.0.1", "test-user-agent") },
            { result -> Assertions.assertNotNull(result) }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should increase the hit count`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page"))

        val visitorRepositoryMock = VisitorRepositoryMock()
        visitorRepositoryMock.visitors.add(Visitor(UUID.randomUUID(), sha256("127.0.0.1 test-user-agent")))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(visitorRepositoryMock, visitorRepository)

        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)

        Given {
            header("Content-Type", "text/plain")
            header("User-Agent", "test-user-agent")
            header("Authorization", "123e4567-e89b-12d3-a456-426614174000")
            body("/page/test-page")
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        }

        uniAsserter.assertThat(
            { pageVisitRepository.countVisits(pageRepositoryMock.pages[0].id) },
            { result -> Assertions.assertEquals(1, result) }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should not increase the hit count for the same visitor`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page"))

        val visitorRepositoryMock = VisitorRepositoryMock()
        visitorRepositoryMock.visitors.add(Visitor(UUID.randomUUID(), sha256("127.0.0.1 test-user-agent")))

        val pageVisitRepositoryMock = PageVisitRepositoryMock()
        pageVisitRepositoryMock.visits.add(PageVisit(pageRepositoryMock.pages[0].id, visitorRepositoryMock.visitors[0].id))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(visitorRepositoryMock, visitorRepository)
        QuarkusMock.installMockForInstance(pageVisitRepositoryMock, pageVisitRepository)

        Given {
            header("Content-Type", "text/plain")
            header("User-Agent", "test-user-agent")
            header("Authorization", "123e4567-e89b-12d3-a456-426614174000")
            body("/page/test-page")
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        }

        uniAsserter.assertThat(
            { pageVisitRepository.countVisits(pageRepositoryMock.pages[0].id) },
            { result -> Assertions.assertEquals(1, result) }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should increase the hit count for a different visitor`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page"))

        val visitorRepositoryMock = VisitorRepositoryMock()
        visitorRepositoryMock.visitors.add(Visitor(UUID.randomUUID(), sha256("localhost test-user-agent")))

        val pageVisitRepositoryMock = PageVisitRepositoryMock()
        pageVisitRepositoryMock.visits.add(PageVisit(pageRepositoryMock.pages[0].id, visitorRepositoryMock.visitors[0].id))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(visitorRepositoryMock, visitorRepository)
        QuarkusMock.installMockForInstance(pageVisitRepositoryMock, pageVisitRepository)

        Given {
            header("Content-Type", "text/plain")
            header("User-Agent", "test-user-agent")
            header("Authorization", "123e4567-e89b-12d3-a456-426614174000")
            body("/page/test-page")
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        }

        uniAsserter.assertThat(
            { pageVisitRepository.countVisits(pageRepositoryMock.pages[0].id) },
            { result -> Assertions.assertEquals(2, result) }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should not register the same visitor twice`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page"))

        val visitorRepositoryMock = VisitorRepositoryMock()
        visitorRepositoryMock.visitors.add(Visitor(UUID.randomUUID(), sha256("127.0.0.1 test-user-agent")))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(visitorRepositoryMock, visitorRepository)
        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)

        Given {
            header("Content-Type", "text/plain")
            header("User-Agent", "test-user-agent")
            header("Authorization", "123e4567-e89b-12d3-a456-426614174000")
            body("/page/test-page")
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        }

        uniAsserter.assertThat(
            { visitorRepository.findByInfo("127.0.0.1", "test-user-agent") },
            { result -> Assertions.assertEquals(visitorRepositoryMock.visitors[0], result) }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return a hit count`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page"))

        val pageVisitRepositoryMock = PageVisitRepositoryMock()
        pageVisitRepositoryMock.visits.add(PageVisit(pageRepositoryMock.pages[0].id, UUID.randomUUID()))
        pageVisitRepositoryMock.visits.add(PageVisit(pageRepositoryMock.pages[0].id, UUID.randomUUID()))
        pageVisitRepositoryMock.visits.add(PageVisit(pageRepositoryMock.pages[0].id, UUID.randomUUID()))
        pageVisitRepositoryMock.visits.add(PageVisit(pageRepositoryMock.pages[0].id, UUID.randomUUID()))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(pageVisitRepositoryMock, pageVisitRepository)

        val count = Given {
            header("Authorization", "123e4567-e89b-12d3-a456-426614174000")
        } When {
            get("/count/${URLEncoder.encode("/page/test-page", "UTF-8")}")
        } Then {
            statusCode(200)
        } Extract {
            body().asString()
        }

        Assertions.assertEquals("4", count)
    }

    @Test
    @RunOnVertxContext
    fun `should return no hit count`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page"))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)

        val count = Given {
            header("Authorization", "123e4567-e89b-12d3-a456-426614174000")
        } When {
            get("/count/${URLEncoder.encode("/page/test-page", "UTF-8")}")
        } Then {
            statusCode(200)
        } Extract {
            body().asString()
        }

        Assertions.assertEquals("0", count)
    }

    @Test
    @RunOnVertxContext
    fun `should return a 404 hit count`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page"))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)

        Given {
            header("Authorization", "123e4567-e89b-12d3-a456-426614174000")
        } When {
            get("/count/${URLEncoder.encode("/page/test-page2", "UTF-8")}")
        } Then {
            statusCode(404)
        }
    }

    @Test
    @RunOnVertxContext
    fun `should return a 403 without api key for hit`(uniAsserter: UniAsserter) {
        Given {
            body("does-not-matter")
        } When {
            post("/hit")
        } Then {
            statusCode(403)
        }
    }

    @Test
    @RunOnVertxContext
    fun `should return a 403 without api key for hit count`(uniAsserter: UniAsserter) {
        When {
            get("/count/foo")
        } Then {
            statusCode(403)
        }
    }
}
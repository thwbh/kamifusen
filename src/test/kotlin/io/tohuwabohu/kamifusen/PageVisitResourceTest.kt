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
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.*
import io.tohuwabohu.kamifusen.crud.dto.PageHitDto
import io.tohuwabohu.kamifusen.mock.ApiUserRepositoryMock
import io.tohuwabohu.kamifusen.mock.PageRepositoryMock
import io.tohuwabohu.kamifusen.mock.PageVisitRepositoryMock
import io.tohuwabohu.kamifusen.mock.VisitorRepositoryMock
import io.tohuwabohu.kamifusen.mock.SessionRepositoryMock
import jakarta.inject.Inject
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.URLEncoder
import java.time.LocalDateTime
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

    @Inject
    lateinit var sessionRepository: SessionRepository

    @BeforeAll
    fun init() {
        QuarkusMock.installMockForType(ApiUserRepositoryMock(), ApiUserRepository::class.java)
    }

    @Test
    @RunOnVertxContext
    fun `should register a new page and visitor`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)
        QuarkusMock.installMockForInstance(VisitorRepositoryMock(), visitorRepository)
        QuarkusMock.installMockForInstance(SessionRepositoryMock(), sessionRepository)

        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "test-user-agent")
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitDto("/page/test-page", "test.org"))
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        }

        uniAsserter.assertThat(
            { pageRepository.findPageByPathAndDomain("/page/test-page", "test.org") },
            { result -> Assertions.assertNotNull(result) }
        )

        uniAsserter.assertThat(
            { visitorRepository.findByInfo("127.0.0.1", "test-user-agent") },
            { result -> Assertions.assertNotNull(result) }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should increase and return the hit count`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page", "test.org"))

        val visitorRepositoryMock = VisitorRepositoryMock()
        visitorRepositoryMock.visitors.add(Visitor(UUID.randomUUID(), sha256("127.0.0.1 test-user-agent")))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(visitorRepositoryMock, visitorRepository)
        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)
        QuarkusMock.installMockForInstance(SessionRepositoryMock(), sessionRepository)

        val count = Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "test-user-agent")
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitDto("/page/test-page", "test.org"))
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        } Extract {
            body().asString()
        }

        uniAsserter.assertThat(
            { pageVisitRepository.countVisits(pageRepositoryMock.pages[0].id) },
            { result -> Assertions.assertEquals(1, result) }
        )

        uniAsserter.assertThat(
            { Uni.createFrom().item(count) },
            { result -> Assertions.assertEquals(1, result.toLong()) }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should not increase the hit count for the same visitor`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page", "test.org"))

        val visitorRepositoryMock = VisitorRepositoryMock()
        visitorRepositoryMock.visitors.add(Visitor(UUID.randomUUID(), sha256("127.0.0.1 test-user-agent")))

        val pageVisitRepositoryMock = PageVisitRepositoryMock()
        pageVisitRepositoryMock.visits.add(PageVisit(pageRepositoryMock.pages[0].id, visitorRepositoryMock.visitors[0].id))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(visitorRepositoryMock, visitorRepository)
        QuarkusMock.installMockForInstance(pageVisitRepositoryMock, pageVisitRepository)

        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "test-user-agent")
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitDto("/page/test-page", "test.org"))
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
    fun `should increase and return the hit count for a different visitor`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page", "test.org"))

        val visitorRepositoryMock = VisitorRepositoryMock()
        visitorRepositoryMock.visitors.add(Visitor(UUID.randomUUID(), sha256("localhost test-user-agent")))

        val pageVisitRepositoryMock = PageVisitRepositoryMock()
        pageVisitRepositoryMock.visits.add(PageVisit(pageRepositoryMock.pages[0].id, visitorRepositoryMock.visitors[0].id))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(visitorRepositoryMock, visitorRepository)
        QuarkusMock.installMockForInstance(pageVisitRepositoryMock, pageVisitRepository)
        QuarkusMock.installMockForInstance(SessionRepositoryMock(), sessionRepository)

        val count = Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "test-user-agent")
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitDto("/page/test-page", "test.org"))
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        } Extract {
            body().asString()
        }

        uniAsserter.assertThat(
            { pageVisitRepository.countVisits(pageRepositoryMock.pages[0].id) },
            { result -> Assertions.assertEquals(2, result) }
        )

        uniAsserter.assertThat(
            { Uni.createFrom().item(count) },
            { result -> Assertions.assertEquals(2, result.toLong()) }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should not register the same visitor twice`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page", "test.org"))

        val visitorRepositoryMock = VisitorRepositoryMock()
        visitorRepositoryMock.visitors.add(Visitor(UUID.randomUUID(), sha256("127.0.0.1 test-user-agent")))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(visitorRepositoryMock, visitorRepository)
        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)
        QuarkusMock.installMockForInstance(SessionRepositoryMock(), sessionRepository)

        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "test-user-agent")
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitDto("/page/test-page", "test.org"))
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
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page", "test.org"))

        val pageVisitRepositoryMock = PageVisitRepositoryMock()
        pageVisitRepositoryMock.visits.add(PageVisit(pageRepositoryMock.pages[0].id, UUID.randomUUID()))
        pageVisitRepositoryMock.visits.add(PageVisit(pageRepositoryMock.pages[0].id, UUID.randomUUID()))
        pageVisitRepositoryMock.visits.add(PageVisit(pageRepositoryMock.pages[0].id, UUID.randomUUID()))
        pageVisitRepositoryMock.visits.add(PageVisit(pageRepositoryMock.pages[0].id, UUID.randomUUID()))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(pageVisitRepositoryMock, pageVisitRepository)

        val count = Given {
            auth().preemptive().basic("api-key-admin", "api-key-admin")
        } When {
            get("/count/${pageRepositoryMock.pages[0].id}")
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
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page", "test.org"))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)

        val count = Given {
            auth().preemptive().basic("api-key-admin", "api-key-admin")
        } When {
            get("/count/${pageRepositoryMock.pages[0].id}")
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
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page", "test.org"))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)

        Given {
            auth().preemptive().basic("api-key-admin", "api-key-admin")
        } When {
            get("/count/${URLEncoder.encode("/page/test-page2", "UTF-8")}")
        } Then {
            statusCode(404)
        }
    }

    @Test
    @RunOnVertxContext
    fun `should return a 403 without api key for hit`(uniAsserter: UniAsserter) {
        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)

        Given {
            header("Authorization", "")
            body("does-not-matter")
        } When {
            post("/hit")
        } Then {
            statusCode(401)
        }
    }

    @Test
    @RunOnVertxContext
    fun `should return a 403 without api key for hit count`(uniAsserter: UniAsserter) {
        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)

        Given {
            header("Authorization", "")
        } When {
            get("/count/foo")
        } Then {
            statusCode(401)
        }
    }

    @Test
    @RunOnVertxContext
    fun `should not register the same page twice`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page", "test.org"))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)
        QuarkusMock.installMockForInstance(VisitorRepositoryMock(), visitorRepository)
        QuarkusMock.installMockForInstance(SessionRepositoryMock(), sessionRepository)

        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "test-user-agent")
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitDto("/page/test-page", "test.org"))
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        }

        uniAsserter.assertThat(
            { pageRepository.findPageByPathAndDomain("/page/test-page", "test.org") },
            { result -> Assertions.assertNotNull(result) }
        )

        uniAsserter.assertThat(
            { pageRepository.listAllPages() },
            { result -> Assertions.assertEquals(1, result.size) }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should continue existing session when visitor has recent activity within 30 minutes`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        val testPage = Page(UUID.randomUUID(), "/page/test-page", "test.org")
        pageRepositoryMock.pages.add(testPage)

        val visitorRepositoryMock = VisitorRepositoryMock()
        val visitor = Visitor(UUID.randomUUID(), sha256("127.0.0.1 test-user-agent"))
        visitorRepositoryMock.visitors.add(visitor)

        val pageVisitRepositoryMock = PageVisitRepositoryMock()
        // Add the page to the mock so domain filtering works
        pageVisitRepositoryMock.pages.add(testPage)
        // Add a recent visit (within 30 minutes) to simulate recent activity
        val recentVisit = PageVisit(testPage.id, visitor.id, LocalDateTime.now().minusMinutes(15))
        pageVisitRepositoryMock.visits.add(recentVisit)

        val sessionRepositoryMock = SessionRepositoryMock()
        // Add an existing session for this visitor
        val existingSession = Session(UUID.randomUUID(), visitor.id, LocalDateTime.now().minusMinutes(20), pageViews = 2)
        sessionRepositoryMock.sessions.add(existingSession)

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(visitorRepositoryMock, visitorRepository)
        QuarkusMock.installMockForInstance(pageVisitRepositoryMock, pageVisitRepository)
        QuarkusMock.installMockForInstance(sessionRepositoryMock, sessionRepository)

        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "test-user-agent")
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitDto("/page/test-page-2", "test.org")) // Different page to trigger new visit
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        }

        // Verify that the existing session was reused and page views incremented
        uniAsserter.assertThat(
            { sessionRepository.findActiveSessionByVisitor(visitor.id) },
            { result ->
                Assertions.assertNotNull(result)
                Assertions.assertEquals(existingSession.id, result?.id)
                Assertions.assertEquals(3, result?.pageViews) // Should be incremented from 2 to 3
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should start new session when visitor has no recent activity beyond 30 minutes`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        val testPage = Page(UUID.randomUUID(), "/page/test-page", "test.org")
        pageRepositoryMock.pages.add(testPage)

        val visitorRepositoryMock = VisitorRepositoryMock()
        val visitor = Visitor(UUID.randomUUID(), sha256("127.0.0.1 test-user-agent"))
        visitorRepositoryMock.visitors.add(visitor)

        val pageVisitRepositoryMock = PageVisitRepositoryMock()
        // Add the page to the mock so domain filtering works
        pageVisitRepositoryMock.pages.add(testPage)
        // Add an old visit (beyond 30 minutes) to simulate no recent activity
        val oldVisit = PageVisit(testPage.id, visitor.id, LocalDateTime.now().minusMinutes(45))
        pageVisitRepositoryMock.visits.add(oldVisit)

        val sessionRepositoryMock = SessionRepositoryMock()
        // Add an old session for this visitor
        val oldSession = Session(UUID.randomUUID(), visitor.id, LocalDateTime.now().minusMinutes(60), pageViews = 2)
        sessionRepositoryMock.sessions.add(oldSession)

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(visitorRepositoryMock, visitorRepository)
        QuarkusMock.installMockForInstance(pageVisitRepositoryMock, pageVisitRepository)
        QuarkusMock.installMockForInstance(sessionRepositoryMock, sessionRepository)

        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "test-user-agent")
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitDto("/page/test-page-2", "test.org")) // Different page to trigger new visit
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        }

        // Verify that a new session was created (not the old one)
        uniAsserter.assertThat(
            { sessionRepository.findActiveSessionByVisitor(visitor.id) },
            { result ->
                Assertions.assertNotNull(result)
                Assertions.assertNotEquals(oldSession.id, result?.id) // Should be a different session
                Assertions.assertEquals(1, result?.pageViews) // New session starts with 1 page view
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle session operation failures gracefully`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        val testPage = Page(UUID.randomUUID(), "/page/test-page", "test.org")
        pageRepositoryMock.pages.add(testPage)

        val visitorRepositoryMock = VisitorRepositoryMock()
        val visitor = Visitor(UUID.randomUUID(), sha256("127.0.0.1 test-user-agent"))
        visitorRepositoryMock.visitors.add(visitor)

        val pageVisitRepositoryMock = PageVisitRepositoryMock()
        pageVisitRepositoryMock.pages.add(testPage)

        // Create a failing session repository that throws exceptions
        val sessionRepositoryMock = object : SessionRepositoryMock() {
            override fun incrementPageViews(sessionId: UUID): Uni<Session?> {
                return Uni.createFrom().failure(RuntimeException("Session increment failed"))
            }
        }

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(visitorRepositoryMock, visitorRepository)
        QuarkusMock.installMockForInstance(pageVisitRepositoryMock, pageVisitRepository)
        QuarkusMock.installMockForInstance(sessionRepositoryMock, sessionRepository)

        // Request should still succeed despite session operation failure
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "test-user-agent")
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitDto("/page/test-page", "test.org"))
        } When {
            post("/hit")
        } Then {
            statusCode(200) // Should still return 200 despite session failure
        }

        // Verify that a page visit was still recorded
        uniAsserter.assertThat(
            { pageVisitRepository.countVisits(testPage.id) },
            { count -> Assertions.assertEquals(1L, count) }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return 404 for count method with non-existent page`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        val pageVisitRepositoryMock = PageVisitRepositoryMock()
        val visitorRepositoryMock = VisitorRepositoryMock()
        val sessionRepositoryMock = SessionRepositoryMock()

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(pageVisitRepositoryMock, pageVisitRepository)
        QuarkusMock.installMockForInstance(visitorRepositoryMock, visitorRepository)
        QuarkusMock.installMockForInstance(sessionRepositoryMock, sessionRepository)

        val nonExistentPageId = UUID.randomUUID()

        Given {
            auth().preemptive().basic("api-key-admin", "api-key-admin")
        } When {
            get("/count/$nonExistentPageId")
        } Then {
            statusCode(404)
        }
    }

    @Test
    @RunOnVertxContext
    fun `should register a the same path for a different domain`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page", "test.org"))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)
        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)
        QuarkusMock.installMockForInstance(VisitorRepositoryMock(), visitorRepository)
        QuarkusMock.installMockForInstance(SessionRepositoryMock(), sessionRepository)

        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "test-user-agent")
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitDto("/page/test-page", "test.dev"))
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        }

        uniAsserter.assertThat(
            { pageRepository.findPageByPathAndDomain("/page/test-page", "test.org") },
            { result -> Assertions.assertNotNull(result) }
        )

        uniAsserter.assertThat(
            { pageRepository.findPageByPathAndDomain("/page/test-page", "test.dev") },
            { result -> Assertions.assertNotNull(result) }
        )

        uniAsserter.assertThat(
            { pageRepository.listAllPages() },
            { result -> Assertions.assertEquals(2, result.size) }
        )
    }
}
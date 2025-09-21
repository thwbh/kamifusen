package io.tohuwabohu.kamifusen

import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.*
import io.tohuwabohu.kamifusen.service.dto.PageHitRequestDto
import jakarta.inject.Inject
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
@TestHTTPEndpoint(PageVisitResource::class)
class PageVisitResourceTest {
    @Inject
    lateinit var pageRepository: PageRepository

    @Inject
    lateinit var visitorRepository: VisitorRepository

    @Test
    @RunOnVertxContext
    fun `should register a new page and visitor`(uniAsserter: UniAsserter) {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "test-user-agent")
            auth().preemptive().basic("api-user-1", "admin")
            body(PageHitRequestDto("/page/test-page", "test.org"))
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
        val hitCount = Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "test-user-agent-unique")
            auth().preemptive().basic("api-user-2", "admin")
            body(PageHitRequestDto("/page/test-page-2", "test.org"))
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        } Extract {
            body().asString().toLong()
        }

        Assertions.assertEquals(1, hitCount)

        uniAsserter.assertThat(
            { pageRepository.findPageByPathAndDomain("/page/test-page-2", "test.org") },
            { result ->
                Assertions.assertNotNull(result)
            }
        )
    }

    @Test
    fun `should not increase the hit count for the same visitor`() {
        // First hit from a visitor
        val hitCount1 = Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "same-user-agent")
            auth().preemptive().basic("api-user-1", "admin")
            body(PageHitRequestDto("/path-2", "domain-2.test"))
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        } Extract {
            body().asString().toLong()
        }

        // subsequent hit from the same visitor
        val hitCount2 = Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "same-user-agent")
            auth().preemptive().basic("api-user-1", "admin")
            body(PageHitRequestDto("/path-2", "domain-2.test"))
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        } Extract {
            body().asString().toLong()
        }

        Assertions.assertEquals(1, hitCount1)
        Assertions.assertEquals(1, hitCount2)
    }

    @Test
    fun `should increase hit count for different visitor`() {
        // First hit from a visitor
        val hitCount1 = Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "same-user-agent")
            auth().preemptive().basic("api-user-1", "admin")
            body(PageHitRequestDto("/path-3", "domain-2.test"))
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        } Extract {
            body().asString().toLong()
        }

        // subsequent hit from another visitor
        val hitCount2 = Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "different-user-agent")
            auth().preemptive().basic("api-user-1", "admin")
            body(PageHitRequestDto("/path-3", "domain-2.test"))
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        } Extract {
            body().asString().toLong()
        }

        Assertions.assertEquals(1, hitCount1)
        Assertions.assertEquals(2, hitCount2)
    }

    @Test
    @TestSecurity(user = "admin", roles = ["api-admin"])
    fun `should return hit count for existing page`() {
        val hitCount = When {
            get("/count/9f685bd0-90e6-479a-99b6-2fad28d2a642")
        } Then {
            statusCode(200)
        } Extract {
            body().asString().toLong()
        }

        Assertions.assertEquals(2, hitCount)
    }

    @Test
    @TestSecurity(user = "admin", roles = ["api-admin"])
    fun `should return 404 for non-existent page count`() {
        When {
            get("/count/9f685bd0-90e6-479a-99b6-2fad28d2a000")
        } Then {
            statusCode(404)
        }
    }

    @Test
    @RunOnVertxContext
    fun `should return 401 without authentication for hit`() {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            body(PageHitRequestDto("/some/page", "test.org"))
        } When {
            post("/hit")
        } Then {
            statusCode(401)
        }
    }

    @Test
    @RunOnVertxContext
    fun `should return 401 without authentication for count`() {
        Given {
            header("Authorization", "")
        } When {
            get("/count/9f685bd0-90e6-479a-99b6-2fad28d2a000")
        } Then {
            statusCode(401)
        }
    }

    @Test
    @RunOnVertxContext
    fun `should register same path for different domain`(uniAsserter: UniAsserter) {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "domain-test-agent")
            auth().preemptive().basic("api-user-1", "admin")
            body(PageHitRequestDto("/path-1", "domain-4.test"))
        } When {
            post("/hit")
        } Then {
            statusCode(200)
        }

        // Verify both pages exist
        uniAsserter.assertThat(
            { pageRepository.findPageByPathAndDomain("/path-1", "domain-1.test") },
            { result -> Assertions.assertNotNull(result) }
        )

        uniAsserter.assertThat(
            { pageRepository.findPageByPathAndDomain("/path-1", "domain-4.test") },
            { result -> Assertions.assertNotNull(result) }
        )
    }

    @Test
    fun `should return 401 for expired api key`() {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            auth().preemptive().basic("api-user-3", "admin")
            body(PageHitRequestDto("/path-1", "domain-1.test"))
        } When {
            post("/hit")
        } Then {
            statusCode(401)
        }
    }
}
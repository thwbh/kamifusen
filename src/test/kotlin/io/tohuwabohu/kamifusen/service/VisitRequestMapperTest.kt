package io.tohuwabohu.kamifusen.service

import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusMock
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.tohuwabohu.kamifusen.PageVisitResource
import io.tohuwabohu.kamifusen.api.generated.model.PageHitRequestDto
import io.tohuwabohu.kamifusen.mock.*
import io.tohuwabohu.kamifusen.service.crud.ApiUserRepository
import io.tohuwabohu.kamifusen.service.crud.PageRepository
import io.tohuwabohu.kamifusen.service.crud.PageVisitRepository
import io.tohuwabohu.kamifusen.service.crud.SessionRepository
import io.tohuwabohu.kamifusen.service.crud.VisitorRepository
import io.tohuwabohu.kamifusen.service.mapper.VisitRequestMapper
import jakarta.inject.Inject
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@QuarkusTest
@TestHTTPEndpoint(PageVisitResource::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VisitRequestMapperTest {

    @Inject
    lateinit var visitRequestMapper: VisitRequestMapper

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
        QuarkusMock.installMockForInstance(PageRepositoryMock(), pageRepository)
        QuarkusMock.installMockForInstance(PageVisitRepositoryMock(), pageVisitRepository)
        QuarkusMock.installMockForInstance(VisitorRepositoryMock(), visitorRepository)
        QuarkusMock.installMockForInstance(SessionRepositoryMock(), sessionRepository)
    }

    // Test the validation methods directly since they don't require HTTP requests
    @Test
    @RunOnVertxContext
    fun `should validate valid page hit request`(uniAsserter: UniAsserter) {
        val validRequest = PageHitRequestDto(
            path = "/valid-path",
            domain = "example.com"
        )

        uniAsserter.assertThat(
            { visitRequestMapper.validatePageHitRequest(validRequest) },
            { result ->
                assertNull(result) // No errors
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should reject empty path`(uniAsserter: UniAsserter) {
        val invalidRequest = PageHitRequestDto(
            path = "",
            domain = "example.com"
        )

        uniAsserter.assertThat(
            { visitRequestMapper.validatePageHitRequest(invalidRequest) },
            { result ->
                assertNotNull(result)
                assertTrue(result!!.contains("Path cannot be empty"))
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should reject empty domain`(uniAsserter: UniAsserter) {
        val invalidRequest = PageHitRequestDto(
            path = "/test",
            domain = ""
        )

        uniAsserter.assertThat(
            { visitRequestMapper.validatePageHitRequest(invalidRequest) },
            { result ->
                assertNotNull(result)
                assertTrue(result!!.contains("Domain cannot be empty"))
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should reject long path`(uniAsserter: UniAsserter) {
        val longPath = "/test/" + "a".repeat(2050) // Over 2048 characters
        val invalidRequest = PageHitRequestDto(
            path = longPath,
            domain = "example.com"
        )

        uniAsserter.assertThat(
            { visitRequestMapper.validatePageHitRequest(invalidRequest) },
            { result ->
                assertNotNull(result)
                assertTrue(result!!.contains("Path too long (max 2048 characters)"))
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should reject long domain`(uniAsserter: UniAsserter) {
        val longDomain = "a".repeat(260) + ".com" // Over 253 characters
        val invalidRequest = PageHitRequestDto(
            path = "/test",
            domain = longDomain
        )

        uniAsserter.assertThat(
            { visitRequestMapper.validatePageHitRequest(invalidRequest) },
            { result ->
                assertNotNull(result)
                assertTrue(result!!.contains("Domain too long (max 253 characters)"))
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should reject invalid domain format`(uniAsserter: UniAsserter) {
        val invalidRequest = PageHitRequestDto(
            path = "/test",
            domain = "invalid..domain..com"
        )

        uniAsserter.assertThat(
            { visitRequestMapper.validatePageHitRequest(invalidRequest) },
            { result ->
                assertNotNull(result)
                assertTrue(result!!.contains("Invalid domain format"))
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should collect multiple validation errors`(uniAsserter: UniAsserter) {
        val invalidRequest = PageHitRequestDto(
            path = "",
            domain = ""
        )

        uniAsserter.assertThat(
            { visitRequestMapper.validatePageHitRequest(invalidRequest) },
            { result ->
                assertNotNull(result)
                assertEquals(2, result!!.size)
                assertTrue(result.contains("Path cannot be empty"))
                assertTrue(result.contains("Domain cannot be empty"))
            }
        )
    }

    // Test the header extraction behavior using HTTP requests with actual headers
    @Test
    fun `should handle requests with various User-Agent headers`() {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("User-Agent", "Mozilla/5.0 (Test Browser)")
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitRequestDto("/test", "example.com"))
        } When {
            post("hit")
        } Then {
            statusCode(200)
        }
    }

    @Test
    fun `should handle requests with Referer header`() {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("Referer", "https://google.com/search")
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitRequestDto("/from-google", "example.com"))
        } When {
            post("hit")
        } Then {
            statusCode(200)
        }
    }

    @Test
    fun `should handle requests with proxy headers`() {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("X-Forwarded-For", "203.0.113.45, 192.168.1.1")
            header("X-Real-IP", "203.0.113.45")
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitRequestDto("/proxied", "example.com"))
        } When {
            post("hit")
        } Then {
            statusCode(200)
        }
    }

    @Test
    fun `should handle requests with CloudFlare country header`() {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            header("CF-IPCountry", "US")
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitRequestDto("/from-usa", "example.com"))
        } When {
            post("hit")
        } Then {
            statusCode(200)
        }
    }

    // Test validation through HTTP requests that should be rejected
    @Test
    fun `should reject requests with empty path through HTTP`() {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitRequestDto("", "example.com"))
        } When {
            post("hit")
        } Then {
            statusCode(400) // Should return bad request for invalid input
        }
    }

    @Test
    fun `should reject requests with invalid domain through HTTP`() {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            auth().preemptive().basic("api-key-user", "api-key-user")
            body(PageHitRequestDto("/test", "invalid..domain"))
        } When {
            post("hit")
        } Then {
            statusCode(400) // Should return bad request for invalid domain
        }
    }
}
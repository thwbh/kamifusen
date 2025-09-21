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
import io.tohuwabohu.kamifusen.crud.ApiUserRepository
import io.tohuwabohu.kamifusen.crud.PageRepository
import jakarta.inject.Inject
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.util.*

@QuarkusTest
@TestHTTPEndpoint(AppAdminResource::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppAdminResourceTest {
    @Inject
    lateinit var apiUserRepository: ApiUserRepository

    @Inject
    lateinit var pageRepository: PageRepository

    @Test
    @TestSecurity(user = "admin", roles = ["app-admin"])
    @RunOnVertxContext
    fun `should generate API key for valid user`(uniAsserter: UniAsserter) {
        val response = Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
            formParam("username", "api-user-4")
            formParam("role", "api-user")
            formParam("expiresAt", "")
        } When {
            post("/keygen")
        } Then {
            statusCode(200)
            contentType(MediaType.TEXT_PLAIN)
        } Extract {
            body().asString()
        }

        // Verify API key was generated
        val decoded = Base64.getDecoder().decode(response)
        val (username, password) = decoded.decodeToString().split(":")

        Assertions.assertEquals("api-user-4", username)
        Assertions.assertDoesNotThrow { UUID.fromString(password) } // UUID should be valid

        uniAsserter.assertThat (
            { apiUserRepository.findByUsername("api-user-4") },
            { result ->
                Assertions.assertNotNull(result)
                Assertions.assertEquals("api-user", result?.role)
                Assertions.assertNull(result?.expiresAt)
            }
        )
    }

    @Test
    @TestSecurity(user = "admin", roles = ["app-admin"])
    @RunOnVertxContext
    fun `should generate API key with expiration date`(uniAsserter: UniAsserter) {
        val expirationDate = LocalDateTime.now().plusDays(30).toString()

        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
            formParam("username", "api-user-6")
            formParam("role", "api-user")
            formParam("expiresAt", expirationDate)
        } When {
            post("/keygen")
        } Then {
            statusCode(200)
        }

        uniAsserter.assertThat (
            { apiUserRepository.findByUsername("api-user-6") },
            { result ->
                Assertions.assertNotNull(result)
                Assertions.assertEquals("api-user", result?.role)
                Assertions.assertEquals(expirationDate, result?.expiresAt.toString())
            }
        )
    }

    @Test
    fun `should return 401 for unauthorized keygen request`() {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
            formParam("username", "testuser")
            formParam("role", "api-user")
            formParam("expiresAt", "")
        } When {
            post("/keygen")
        } Then {
            statusCode(401)
        }
    }

    @Test
    fun `should get aggregated stats with default time range`() {
        val response = Given {
            auth().preemptive().basic("admin", "admin")
        } When {
            get("/stats")
        } Then {
            statusCode(200)
            contentType(MediaType.APPLICATION_JSON)
        } Extract {
            body().jsonPath()
        }

        // Verify response structure based on mock data
        Assertions.assertNotNull(response.get("visitData"))
        Assertions.assertNotNull(response.get("topPages"))
        Assertions.assertNotNull(response.get("domainStats"))
        Assertions.assertEquals(1200, response.getInt("totalVisits"))
        Assertions.assertEquals(15, response.getInt("totalPages"))
        Assertions.assertEquals(2, response.getInt("totalDomains"))
    }

    @Test
    fun `should get aggregated stats with custom time range`() {
        Given {
            auth().preemptive().basic("admin", "admin")
            queryParam("timeRange", "30d")
        } When {
            get("/stats")
        } Then {
            statusCode(200)
            contentType(MediaType.APPLICATION_JSON)
        }
    }

    @Test
    fun `should return 401 for unauthorized stats request`() {
        Given {
            header("Authorization", "")
        } When {
            get("/stats")
        } Then {
            statusCode(401)
        }
    }

    @Test
    fun `should get all page visits`() {
        val response = Given {
            auth().preemptive().basic("admin", "admin")
        } When {
            get("/visits")
        } Then {
            statusCode(200)
            contentType(MediaType.APPLICATION_JSON)
        } Extract {
            body().jsonPath()
        }

        // Verify response contains expected data from mock
        val visits = response.getList<Map<String, Any>>("")
        Assertions.assertTrue(visits.size >= 3) // Based on mock data

        // Check first visit has expected structure
        val firstVisit = visits[0]
        Assertions.assertNotNull(firstVisit["id"])
        Assertions.assertNotNull(firstVisit["path"])
        Assertions.assertNotNull(firstVisit["domain"])
        Assertions.assertNotNull(firstVisit["pageAdded"])
        Assertions.assertNotNull(firstVisit["visits"])
    }

    @Test
    fun `should get pages with stats`() {
        val response = Given {
            auth().preemptive().basic("admin", "admin")
        } When {
            get("/pages")
        } Then {
            statusCode(200)
            contentType(MediaType.APPLICATION_JSON)
        } Extract {
            body().jsonPath()
        }

        // Verify response contains expected data from mock
        val pages = response.getList<Map<String, Any>>("")
        Assertions.assertTrue(pages.size >= 3) // Based on mock data

        // Check first page has expected structure
        val firstPage = pages[0]
        Assertions.assertNotNull(firstPage["id"])
        Assertions.assertNotNull(firstPage["path"])
        Assertions.assertNotNull(firstPage["domain"])
        Assertions.assertNotNull(firstPage["pageAdded"])
        Assertions.assertNotNull(firstPage["visitCount"])
        Assertions.assertTrue(firstPage["visitCount"] is Number)
    }

    @Test
    @TestSecurity(user = "admin", roles = ["app-admin"])
    fun `should get list of users`() {
        val response = Given {
            auth().preemptive().basic("admin", "admin")
        } When {
            get("/users")
        } Then {
            statusCode(200)
            contentType(MediaType.APPLICATION_JSON)
        } Extract {
            body().jsonPath()
        }

        // Verify response contains expected data from mock
        val users = response.getList<Map<String, Any>>("")
        Assertions.assertEquals(5, users.size) // Based on initial mock data

        // Check user structure
        val firstUser = users[0]
        Assertions.assertNotNull(firstUser["id"])
        Assertions.assertNotNull(firstUser["username"])
        Assertions.assertNotNull(firstUser["role"])
    }

    @Test
    @TestSecurity(user = "admin", roles = ["app-admin"])
    @RunOnVertxContext
    fun `should retire API key`(uniAsserter: UniAsserter) {
        When {
            post("/retire/9f685bd0-90e6-479a-99b6-3fad28d2a005")
        } Then {
            statusCode(200)
        }

        // username will return null after retirement
        uniAsserter.assertThat (
            { apiUserRepository.findByUsername("api-user-5") },
            { result ->
                Assertions.assertNull(result)
            }
        )

        // Verify API key was retired
        uniAsserter.assertThat (
            { apiUserRepository.findByUuid(UUID.fromString("9f685bd0-90e6-479a-99b6-3fad28d2a005")) },
            { result ->
                Assertions.assertNotNull(result)
                Assertions.assertEquals("api-user", result?.role)
                Assertions.assertNotNull(result?.expiresAt.toString())
            }
        )
    }

    @Test
    @TestSecurity(user = "admin", roles = ["app-admin"])
    @RunOnVertxContext
    fun `should delete page`(uniAsserter: UniAsserter) {
        When {
            post("/pagedel/9f685bd0-90e6-479a-99b6-2fad28d2a650")
        } Then {
            statusCode(200)
        }

        uniAsserter.assertThat (
            { pageRepository.findPageByPathAndDomain("/deletion", "delete.test") },
            { result ->
                Assertions.assertNull(result)
            }
        )
    }

    @Test
    @TestSecurity(user = "admin", roles = ["app-admin"])
    fun `should handle invalid retire page ID`() {
        When {
            post("/pagedel/9f685bd0-90e6-479a-99b6-2fad28d2a000")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `should return 401 for unauthorized user operations`() {
        Given {
            header("Authorization", "")
        } When {
            post("/retire/9f685bd0-90e6-479a-99b6-2fad28d2a000")
        } Then {
            statusCode(401)
        }
    }

    @Test
    @TestSecurity(user = "admin", roles = ["app-admin"])
    fun `should handle invalid retire user ID`() {
        When {
            post("/retire/9f685bd0-90e6-479a-99b6-2fad28d2a000")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `should return 401 for unauthorized page operations`() {
        When {
            post("/pagedel/9f685bd0-90e6-479a-99b6-2fad28d2a000")
        } Then {
            statusCode(401)
        }
    }
}

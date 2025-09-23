package io.tohuwabohu.kamifusen

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.quarkus.test.junit.QuarkusMock
import io.tohuwabohu.kamifusen.service.crud.ApiUserRepository
import io.tohuwabohu.kamifusen.service.crud.BlacklistRepository
import io.tohuwabohu.kamifusen.service.crud.PageRepository
import io.tohuwabohu.kamifusen.mock.StatsServiceMock
import io.tohuwabohu.kamifusen.service.StatsService
import jakarta.inject.Inject
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
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

    @Inject
    lateinit var blacklistRepository: BlacklistRepository

    @BeforeAll
    fun init() {
        QuarkusMock.installMockForType(StatsServiceMock(), StatsService::class.java)
    }

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
            post("/admin/keygen")
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
            post("/admin/keygen")
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
            post("/admin/keygen")
        } Then {
            statusCode(401)
        }
    }

    @Test
    @TestSecurity(user = "admin", roles = ["app-admin"])
    fun `should get aggregated stats with default time range`() {
        val response = When {
            get("/admin/stats")
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
    @TestSecurity(user = "admin", roles = ["app-admin"])
    fun `should get aggregated stats with custom time range`() {
        Given {
            queryParam("timeRange", "30d")
        } When {
            get("/admin/stats")
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
            get("/admin/stats")
        } Then {
            statusCode(401)
        }
    }

    @Test
    @TestSecurity(user = "admin", roles = ["app-admin"])
    fun `should get all page visits`() {
        val response = When {
            get("/admin/visits")
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
        Assertions.assertNotNull(firstVisit["visitCount"])
    }

    @Test
    @TestSecurity(user = "admin", roles = ["app-admin"])
    fun `should get pages with stats`() {
        val response = When {
            get("/admin/pages")
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
        val response = When {
            get("/admin/users")
        } Then {
            statusCode(200)
            contentType(MediaType.APPLICATION_JSON)
        } Extract {
            body().jsonPath()
        }

        // Verify response contains expected data from mock
        val users = response.getList<Map<String, Any>>("")
        Assertions.assertEquals(7, users.size) // Based on initial mock data

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
            post("/admin/retire/9f685bd0-90e6-479a-99b6-3fad28d2a005")
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
            post("/admin/pagedel/9f685bd0-90e6-479a-99b6-2fad28d2a650")
        } Then {
            statusCode(200)
        }

        uniAsserter.assertThat (
            { blacklistRepository.isPageBlacklisted(UUID.fromString("9f685bd0-90e6-479a-99b6-2fad28d2a650")) },
            { result ->
                Assertions.assertTrue(result)
            }
        )
    }

    @Test
    @TestSecurity(user = "admin", roles = ["app-admin"])
    fun `should handle invalid retire page ID`() {
        When {
            post("/admin/pagedel/9f685bd0-90e6-479a-99b6-2fad28d2a000")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `should return 401 for unauthorized user operations`() {
        Given {
            header("Authorization", "")
        } When {
            post("/admin/retire/9f685bd0-90e6-479a-99b6-2fad28d2a000")
        } Then {
            statusCode(401)
        }
    }

    @Test
    @TestSecurity(user = "admin", roles = ["app-admin"])
    fun `should handle invalid retire user ID`() {
        When {
            post("/admin/retire/9f685bd0-90e6-479a-99b6-2fad28d2a000")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `should return 401 for unauthorized page operations`() {
        When {
            post("/admin/pagedel/9f685bd0-90e6-479a-99b6-2fad28d2a000")
        } Then {
            statusCode(401)
        }
    }

    @Test
    @TestSecurity(user = "admin-password-change", roles = ["app-admin"])
    @RunOnVertxContext
    fun `should update admin password successfully`(uniAsserter: UniAsserter) {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
            formParam("newUsername", "admin-password-change-changed")
            formParam("newPassword", "newSecurePassword123")
            formParam("newPasswordConfirmation", "newSecurePassword123")
        } When {
            post("/admin/update")
        } Then {
            statusCode(200)
            contentType(MediaType.TEXT_PLAIN)
        }

        // Verify password was updated in the repository
        uniAsserter.assertThat(
            { apiUserRepository.findByUsername("admin-password-change-changed") },
            { result ->
                Assertions.assertNotNull(result)
                Assertions.assertEquals("admin-password-change-changed", result?.username)

                Assertions.assertNotNull(result?.password)
                Assertions.assertNotNull(result?.updated)

                Assertions.assertTrue(BcryptUtil.matches("newSecurePassword123", result?.password))
            }
        )
    }

    @Test
    @TestSecurity(user = "admin-password-dont-change", roles = ["app-admin"])
    fun `should return 400 for empty parameters in admin update`() {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
            formParam("newPassword", "")
            formParam("newPasswordConfirmation", "")
            formParam("newUsername", "")
        } When {
            post("/admin/update")
        } Then {
            statusCode(400) // Bad request for missing required parameters
        }
    }

    @Test
    fun `should return 401 for unauthorized admin update`() {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
            formParam("newUsername", "admin-password-dont-change")
            formParam("newPassword", "newPassword")
            formParam("newPasswordConfirmation", "newPassword")
        } When {
            post("/admin/update")
        } Then {
            statusCode(401)
        }
    }

    @Test
    @TestSecurity(user = "admin-password-dont-change", roles = ["app-admin"])
    fun `should return 400 for empty new password in admin update`() {
        Given {
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
            formParam("newUsername", "admin-password-dont-change")
            formParam("newPassword", "")
            formParam("newPasswordConfirmation", "")
        } When {
            post("/admin/update")
        } Then {
            statusCode(400) // Empty passwords are not valid
        }
    }
}

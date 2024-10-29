package io.tohuwabohu.kamifusen

import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusMock
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.tohuwabohu.kamifusen.crud.ApiUserRepository
import io.tohuwabohu.kamifusen.mock.ApiUserRepositoryMock
import io.tohuwabohu.kamifusen.ssr.createPasswordUpdateDiv
import io.tohuwabohu.kamifusen.ssr.createSuccessfulPasswordUpdateDiv
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@QuarkusTest
@TestHTTPEndpoint(AppAdminResource::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppAdminResourceTest {
    @Inject
    private lateinit var apiUserRepository: ApiUserRepository

    @BeforeAll
    fun init() {
        QuarkusMock.installMockForType(ApiUserRepositoryMock(), ApiUserRepository::class.java)
    }

    @Test
    @RunOnVertxContext
    fun `should update admin password`(uniAsserter: UniAsserter) {
        val expectedPassword = "awesome-administrator"
        val expectedHtmlBody = createSuccessfulPasswordUpdateDiv()

        val actualHtmlBody = Given {
            header("Content-Type", ContentType.URLENC)
            formParam("password", expectedPassword)
        } When {
            post("/register")
        } Then {
            statusCode(200)
        } Extract {
            body().asString()
        }

        Assertions.assertEquals(expectedHtmlBody, actualHtmlBody)

        uniAsserter.assertThat(
            { apiUserRepository.findByUsername("admin") },
            { result -> Assertions.assertEquals(expectedPassword, result!!.password)}
        )
    }

    @Test
    fun `should show admin password update page`() {
        val specificApiUserRepositoryMock = ApiUserRepositoryMock()
        specificApiUserRepositoryMock.apiUsers.find { it.username == "admin" }?.let { it.password = null }

        QuarkusMock.installMockForInstance(specificApiUserRepositoryMock, apiUserRepository)

        val expectedHtmlBody = createPasswordUpdateDiv()

        val actualHtmlBody = Given {
            header("Content-Type", ContentType.URLENC)
            formParam("password", "admin-password-update")
        } When {
            post("/login")
        } Then {
            statusCode(202)
        } Extract {
            body().asString()
        }

        Assertions.assertEquals(expectedHtmlBody, actualHtmlBody)

        QuarkusMock.installMockForType(ApiUserRepositoryMock(), ApiUserRepository::class.java)
    }
}
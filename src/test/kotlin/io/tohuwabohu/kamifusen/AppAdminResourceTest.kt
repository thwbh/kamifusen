package io.tohuwabohu.kamifusen

import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusMock
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.tohuwabohu.kamifusen.crud.ApiUserRepository
import io.tohuwabohu.kamifusen.crud.security.PasswordValidation
import io.tohuwabohu.kamifusen.mock.ApiUserRepositoryMock
import io.tohuwabohu.kamifusen.ssr.renderPasswordFlow
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@QuarkusTest
@TestHTTPEndpoint(AppAdminResource::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppAdminResourceTest {

    @Inject
    lateinit var apiUserRepository: ApiUserRepository
/*
    @Test
    @RunOnVertxContext
    @TestSecurity(user = "admin", roles = ["app-admin"])
    fun `should update admin password`(uniAsserter: UniAsserter) {
        QuarkusMock.installMockForInstance(ApiUserRepositoryMock(), apiUserRepository)

        val expectedPassword = "awesome-administrator"
        val expectedHtmlBody = renderPasswordFlow(PasswordValidation.VALID)

        val actualHtmlBody = Given {
            header("Content-Type", ContentType.URLENC)
            formParam("password", expectedPassword)
            formParam("password-confirm", expectedPassword)
        } When {
            post("/fragment/register")
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
    @TestSecurity(user = "admin", roles = ["app-admin"])
    fun `should show admin password update NO_MATCH error`() {
        val passwordFlowApiUserRepositoryMock = ApiUserRepositoryMock()
        passwordFlowApiUserRepositoryMock.apiUsers.find { it.username == "admin" }?.let { it.password = null }

        QuarkusMock.installMockForInstance(passwordFlowApiUserRepositoryMock, apiUserRepository)

        val expectedHtmlBody = renderPasswordFlow(PasswordValidation.NO_MATCH)

        val actualHtmlBody = Given {
            header("Content-Type", ContentType.URLENC)
            formParam("password", "admin-password-update")
            formParam("password-confirm", "admin-password-misspell")
        } When {
            post("/fragment/register")
        } Then {
            statusCode(200)
        } Extract {
            body().asString()
        }

        Assertions.assertEquals(expectedHtmlBody, actualHtmlBody)

        QuarkusMock.installMockForType(ApiUserRepositoryMock(), ApiUserRepository::class.java)
    }

    @Test
    @TestSecurity(user = "admin", roles = ["app-admin"])
    fun `should show admin password update EMPTY error`() {
        installMockForPasswordFlow()

        val expectedHtmlBody = renderPasswordFlow(PasswordValidation.EMPTY)

        val actualHtmlBody = Given {
            header("Content-Type", ContentType.URLENC)
            formParam("password", "")
            formParam("password-confirm", "")
        } When {
            post("/fragment/register")
        } Then {
            statusCode(200)
        } Extract {
            body().asString()
        }

        Assertions.assertEquals(expectedHtmlBody, actualHtmlBody)
    }

    @Test
    @TestSecurity(user = "admin", roles = ["app-admin"])
    fun `should show admin password update TOO_SHORT error`() {
        installMockForPasswordFlow()

        val expectedHtmlBody = renderPasswordFlow(PasswordValidation.TOO_SHORT)

        val actualHtmlBody = Given {
            header("Content-Type", ContentType.URLENC)
            formParam("password", "short")
            formParam("password-confirm", "short")
        } When {
            post("/fragment/register")
        } Then {
            statusCode(200)
        } Extract {
            body().asString()
        }

        Assertions.assertEquals(expectedHtmlBody, actualHtmlBody)

        QuarkusMock.installMockForType(ApiUserRepositoryMock(), ApiUserRepository::class.java)
    }

    private fun installMockForPasswordFlow() {
        val passwordFlowApiUserRepositoryMock = ApiUserRepositoryMock()
        passwordFlowApiUserRepositoryMock.apiUsers.find { it.username == "admin" }?.let { it.password = null }

        QuarkusMock.installMockForInstance(passwordFlowApiUserRepositoryMock, apiUserRepository)
    }

 */
}

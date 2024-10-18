package io.tohuwabohu.kamifusen

import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusMock
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.tohuwabohu.kamifusen.crud.Page
import io.tohuwabohu.kamifusen.crud.PageRepository
import io.tohuwabohu.kamifusen.mock.PageRepositoryMock
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
@TestHTTPEndpoint(AdminResource::class)
class AdminResourceTest {
    @Inject
    lateinit var pageRepository: PageRepository

    @Test
    @RunOnVertxContext
    fun `should add a page`(uniAsserter: UniAsserter) {
        QuarkusMock.installMockForInstance(PageRepositoryMock(), pageRepository)

        Given {
            body("/page/test-page")
        } When {
            post("/add")
        } Then {
            statusCode(201)
        }

        uniAsserter.assertThat(
            { pageRepository.findPageByPath("/page/test-page") },
            { result -> Assertions.assertNotNull(result)}
        )
    }

    @Test
    @RunOnVertxContext
    fun `should not add the same page twice`(uniAsserter: UniAsserter) {
        val pageRepositoryMock = PageRepositoryMock()
        pageRepositoryMock.pages.add(Page(UUID.randomUUID(), "/page/test-page"))

        QuarkusMock.installMockForInstance(pageRepositoryMock, pageRepository)

        Given {
            body("/page/test-page")
        } When {
            post("/add")
        } Then {
            statusCode(204)
        }

        uniAsserter.assertThat(
            { pageRepository.findPageByPath("/page/test-page") },
            { result -> Assertions.assertEquals(pageRepositoryMock.pages[0].id, result!!.id)}
        )
    }
}
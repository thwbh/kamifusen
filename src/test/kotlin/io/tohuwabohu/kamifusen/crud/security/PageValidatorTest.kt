package io.tohuwabohu.kamifusen.crud.security

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import io.tohuwabohu.kamifusen.crud.PageRepository
import jakarta.inject.Inject
import org.junit.jupiter.api.Test

@QuarkusTest
class PageValidatorTest {

    @Inject
    lateinit var pageRepository: PageRepository

    @Test
    @RunOnVertxContext
    fun `should return EMPTY when path is blank`(uniAsserter: UniAsserter) {
        val path = ""
        val domain = "example.com"

        uniAsserter.assertThat(
            { validatePage(path, domain, pageRepository) },
            { result ->
                assert(result == PageValidation.EMPTY)
                assert(!result.valid)
                assert(result.message == "Path must not be empty.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return EMPTY when path is whitespace only`(uniAsserter: UniAsserter) {
        val path = "   "
        val domain = "example.com"

        uniAsserter.assertThat(
            { validatePage(path, domain, pageRepository) },
            { result ->
                assert(result == PageValidation.EMPTY)
                assert(!result.valid)
                assert(result.message == "Path must not be empty.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return EXISTS for new page that gets created`(uniAsserter: UniAsserter) {
        val path = "/test-page-${System.currentTimeMillis()}"
        val domain = "test-domain.com"

        uniAsserter.assertThat(
            { validatePage(path, domain, pageRepository) },
            { result ->
                // The page gets created by addPageIfAbsent, so it EXISTS
                assert(result == PageValidation.EXISTS)
                assert(!result.valid)
                assert(result.message == "Page already exists.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return EXISTS for page that already exists in same domain`(uniAsserter: UniAsserter) {
        val path = "/existing-test-page"
        val domain = "test-domain.com"

        // First, add the page
        uniAsserter.execute { pageRepository.addPage(path, domain) }

        // Then try to validate the same page again
        uniAsserter.assertThat(
            { validatePage(path, domain, pageRepository) },
            { result ->
                assert(result == PageValidation.EXISTS)
                assert(!result.valid)
                assert(result.message == "Page already exists.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return EXISTS for same path in different domain`(uniAsserter: UniAsserter) {
        val path = "/shared-path"
        val domain1 = "domain1.com"
        val domain2 = "domain2.com"

        // Add page to first domain
        uniAsserter.execute { pageRepository.addPage(path, domain1) }

        // Validate same path in different domain - since addPageIfAbsent creates the page, it EXISTS
        uniAsserter.assertThat(
            { validatePage(path, domain2, pageRepository) },
            { result ->
                assert(result == PageValidation.EXISTS)
                assert(!result.valid)
                assert(result.message == "Page already exists.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should use empty string as default domain and return EXISTS`(uniAsserter: UniAsserter) {
        val path = "/test-default-domain-${System.currentTimeMillis()}"

        uniAsserter.assertThat(
            { validatePage(path, pageRepository = pageRepository) },
            { result ->
                assert(result == PageValidation.EXISTS)
                assert(!result.valid)
                assert(result.message == "Page already exists.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle special characters in path and return EXISTS`(uniAsserter: UniAsserter) {
        val path = "/test-page?param=value&other=test"
        val domain = "special-chars.com"

        uniAsserter.assertThat(
            { validatePage(path, domain, pageRepository) },
            { result ->
                assert(result == PageValidation.EXISTS)
                assert(!result.valid)
                assert(result.message == "Page already exists.")
            }
        )
    }
}
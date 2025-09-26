package io.tohuwabohu.kamifusen.service.validator

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import org.junit.jupiter.api.Test

@QuarkusTest
class PasswordValidatorTest {

    @Test
    @RunOnVertxContext
    fun `should return VALID for matching passwords with sufficient length`(uniAsserter: UniAsserter) {
        val password = "validPassword123"
        val confirmation = "validPassword123"

        uniAsserter.assertThat(
            { validatePassword(password, confirmation) },
            { result ->
                assert(result == PasswordValidation.VALID)
                assert(result.valid)
                assert(result.message == null)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return NO_MATCH when passwords do not match`(uniAsserter: UniAsserter) {
        val password = "password123"
        val confirmation = "differentPassword"

        uniAsserter.assertThat(
            { validatePassword(password, confirmation) },
            { result ->
                assert(result == PasswordValidation.NO_MATCH)
                assert(!result.valid)
                assert(result.message == "Your passwords do not match.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return EMPTY when password is blank`(uniAsserter: UniAsserter) {
        val password = ""
        val confirmation = ""

        uniAsserter.assertThat(
            { validatePassword(password, confirmation) },
            { result ->
                assert(result == PasswordValidation.EMPTY)
                assert(!result.valid)
                assert(result.message == "Your password is empty.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return EMPTY when password is whitespace only`(uniAsserter: UniAsserter) {
        val password = "   "
        val confirmation = "   "

        uniAsserter.assertThat(
            { validatePassword(password, confirmation) },
            { result ->
                assert(result == PasswordValidation.EMPTY)
                assert(!result.valid)
                assert(result.message == "Your password is empty.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return NO_MATCH when password confirmation is blank`(uniAsserter: UniAsserter) {
        val password = "validPassword"
        val confirmation = ""

        uniAsserter.assertThat(
            { validatePassword(password, confirmation) },
            { result ->
                assert(result == PasswordValidation.NO_MATCH)
                assert(!result.valid)
                assert(result.message == "Your passwords do not match.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return TOO_SHORT when password is less than 8 characters`(uniAsserter: UniAsserter) {
        val password = "short"
        val confirmation = "short"

        uniAsserter.assertThat(
            { validatePassword(password, confirmation) },
            { result ->
                assert(result == PasswordValidation.TOO_SHORT)
                assert(!result.valid)
                assert(result.message == "Your password must be at least 8 characters long.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return VALID when password is exactly 8 characters`(uniAsserter: UniAsserter) {
        val password = "12345678"
        val confirmation = "12345678"

        uniAsserter.assertThat(
            { validatePassword(password, confirmation) },
            { result ->
                assert(result == PasswordValidation.VALID)
                assert(result.valid)
                assert(result.message == null)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should prioritize NO_MATCH over TOO_SHORT`(uniAsserter: UniAsserter) {
        val password = "short"
        val confirmation = "different"

        uniAsserter.assertThat(
            { validatePassword(password, confirmation) },
            { result ->
                assert(result == PasswordValidation.NO_MATCH)
                assert(!result.valid)
                assert(result.message == "Your passwords do not match.")
            }
        )
    }
}
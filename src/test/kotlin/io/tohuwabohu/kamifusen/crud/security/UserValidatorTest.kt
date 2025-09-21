package io.tohuwabohu.kamifusen.crud.security

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.vertx.UniAsserter
import io.tohuwabohu.kamifusen.crud.ApiUser
import io.tohuwabohu.kamifusen.crud.ApiUserRepository
import jakarta.inject.Inject
import org.junit.jupiter.api.Test

@QuarkusTest
class UserValidatorTest {

    @Inject
    lateinit var apiUserRepository: ApiUserRepository

    @Test
    @RunOnVertxContext
    fun `should return EMPTY when username is blank`(uniAsserter: UniAsserter) {
        val username = ""

        uniAsserter.assertThat(
            { validateUser(username, apiUserRepository) },
            { result ->
                assert(result == UserValidation.EMPTY)
                assert(!result.valid)
                assert(result.message == "Name must not be empty.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return EMPTY when username is whitespace only`(uniAsserter: UniAsserter) {
        val username = "   "

        uniAsserter.assertThat(
            { validateUser(username, apiUserRepository) },
            { result ->
                assert(result == UserValidation.EMPTY)
                assert(!result.valid)
                assert(result.message == "Name must not be empty.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return EMPTY when username is tabs only`(uniAsserter: UniAsserter) {
        val username = "\t\t\t"

        uniAsserter.assertThat(
            { validateUser(username, apiUserRepository) },
            { result ->
                assert(result == UserValidation.EMPTY)
                assert(!result.valid)
                assert(result.message == "Name must not be empty.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return EMPTY when username is newlines only`(uniAsserter: UniAsserter) {
        val username = "\n\n\n"

        uniAsserter.assertThat(
            { validateUser(username, apiUserRepository) },
            { result ->
                assert(result == UserValidation.EMPTY)
                assert(!result.valid)
                assert(result.message == "Name must not be empty.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return EMPTY when username is mixed whitespace`(uniAsserter: UniAsserter) {
        val username = " \t\n "

        uniAsserter.assertThat(
            { validateUser(username, apiUserRepository) },
            { result ->
                assert(result == UserValidation.EMPTY)
                assert(!result.valid)
                assert(result.message == "Name must not be empty.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return EXISTS for username that already exists`(uniAsserter: UniAsserter) {
        val username = "test-existing-user-${System.currentTimeMillis()}"

        // First create the user
        val testUser = ApiUser(
            username = username,
            password = "", // Will be set by addUser
            role = "api-user"
        )

        // Create the user and then validate in a chain to ensure proper transaction handling
        uniAsserter.assertThat(
            {
                apiUserRepository.addUser(testUser)
                    .onItem().transformToUni { _ ->
                        // Validate immediately after successful creation within same transaction context
                        validateUser(username, apiUserRepository)
                    }
            },
            { result ->
                assert(result == UserValidation.EXISTS)
                assert(!result.valid)
                assert(result.message == "Name is already taken.")
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should return VALID for new username that does not exist`(uniAsserter: UniAsserter) {
        val username = "new-unique-user-${System.currentTimeMillis()}"

        uniAsserter.assertThat(
            { validateUser(username, apiUserRepository) },
            { result ->
                assert(result == UserValidation.VALID)
                assert(result.valid)
                assert(result.message == null)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle username with special characters`(uniAsserter: UniAsserter) {
        val username = "user-name_123.email@domain.com-${System.currentTimeMillis()}"

        uniAsserter.assertThat(
            { validateUser(username, apiUserRepository) },
            { result ->
                assert(result == UserValidation.VALID)
                assert(result.valid)
                assert(result.message == null)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle username with unicode characters`(uniAsserter: UniAsserter) {
        val username = "üser_ñamé-${System.currentTimeMillis()}"

        uniAsserter.assertThat(
            { validateUser(username, apiUserRepository) },
            { result ->
                assert(result == UserValidation.VALID)
                assert(result.valid)
                assert(result.message == null)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle very long username`(uniAsserter: UniAsserter) {
        val username = "a".repeat(100) + "-${System.currentTimeMillis()}"

        uniAsserter.assertThat(
            { validateUser(username, apiUserRepository) },
            { result ->
                assert(result == UserValidation.VALID)
                assert(result.valid)
                assert(result.message == null)
            }
        )
    }

    @Test
    @RunOnVertxContext
    fun `should handle single character username`(uniAsserter: UniAsserter) {
        val username = "a"

        uniAsserter.assertThat(
            { validateUser(username, apiUserRepository) },
            { result ->
                assert(result == UserValidation.VALID)
                assert(result.valid)
                assert(result.message == null)
            }
        )
    }
}
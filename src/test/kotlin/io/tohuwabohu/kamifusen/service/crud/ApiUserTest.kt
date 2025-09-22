package io.tohuwabohu.kamifusen.service.crud

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@QuarkusTest
class ApiUserTest {

    @Test
    fun `should convert ApiUser to DTO correctly`() {
        val user = ApiUser(
            username = "dtotest",
            password = "password",
            role = "api-user",
            expiresAt = LocalDateTime.now().plusDays(30)
        )
        user.id = UUID.randomUUID()
        user.added = LocalDateTime.now()
        user.updated = LocalDateTime.now()

        val dto = user.toDto()

        assertEquals(user.username, dto.username)
        assertEquals(user.role, dto.role)
        assertEquals(user.id, dto.id)
        assertEquals(user.expiresAt, dto.expiresAt)
        assertEquals(user.added, dto.added)
        assertEquals(user.updated, dto.updated)
    }

    @Test
    fun `should handle PrePersist lifecycle correctly`() {
        val user = ApiUser(
            username = "lifecycletest",
            password = "plainpassword",
            role = "api-user"
        )

        // Simulate PrePersist
        user.beforePersist()

        assertNotNull(user.id)
        assertNotNull(user.added)
        assertTrue(BcryptUtil.matches("plainpassword", user.password))
    }

    @Test
    fun `should handle PreUpdate lifecycle correctly`() {
        val user = ApiUser(
            username = "updatetest",
            password = "password",
            role = "api-user"
        )

        assertNull(user.updated)

        // Simulate PreUpdate
        user.beforeUpdate()

        assertNotNull(user.updated)
    }

    @Test
    fun `should handle equals and hashCode correctly`() {
        val user1 = ApiUser(username = "test", password = "pass", role = "role")
        val user2 = ApiUser(username = "test", password = "pass", role = "role")

        user1.id = UUID.randomUUID()
        user2.id = user1.id

        assertEquals(user1, user2)
        assertEquals(user1.hashCode(), user2.hashCode())

        user2.id = UUID.randomUUID()
        assertNotEquals(user1, user2)
    }

    @Test
    fun `should handle equals with different types`() {
        val user = ApiUser(username = "test", password = "pass", role = "role")
        user.id = UUID.randomUUID()

        assertNotEquals(user, "not a user")
        assertNotEquals(user, null)
    }

    @Test
    fun `should handle equals with same instance`() {
        val user = ApiUser(username = "test", password = "pass", role = "role")
        user.id = UUID.randomUUID()

        assertEquals(user, user)
    }

    @Test
    fun `should handle equals with null IDs`() {
        val user1 = ApiUser(username = "test1", password = "pass", role = "role")
        val user2 = ApiUser(username = "test2", password = "pass", role = "role")

        // Both have null IDs
        assertNotEquals(user1, user2)
    }

    @Test
    fun `should create user with all parameters`() {
        val expiresAt = LocalDateTime.now().plusDays(30)
        val user = ApiUser(
            username = "testuser",
            password = "testpass",
            role = "api-user",
            expiresAt = expiresAt
        )

        assertEquals("testuser", user.username)
        assertEquals("testpass", user.password)
        assertEquals("api-user", user.role)
        assertEquals(expiresAt, user.expiresAt)
        assertNull(user.id)
        assertNull(user.added)
        assertNull(user.updated)
    }

    @Test
    fun `should have proper toString representation`() {
        val user = ApiUser(
            username = "testuser",
            password = "testpass",
            role = "api-user"
        )
        user.id = UUID.randomUUID()

        val toString = user.toString()

        assertTrue(toString.contains("testuser"))
        assertTrue(toString.contains("testpass"))
        assertTrue(toString.contains("api-user"))
        assertTrue(toString.contains(user.id.toString()))
    }
}
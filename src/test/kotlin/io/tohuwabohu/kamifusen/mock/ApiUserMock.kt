package io.tohuwabohu.kamifusen.mock

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.ApiUser
import io.tohuwabohu.kamifusen.crud.ApiUserRepository
import java.time.LocalDateTime
import java.util.*

/**
 * Mock repository for non-disruptive CRUD operations on ApiUser entity
 */
class ApiUserRepositoryMock : ApiUserRepository() {
    final val apiUsers = mutableListOf(
        ApiUser(id = UUID.randomUUID(), username = "api-key-user", password = "api-key-user", role = "api-user"),
        ApiUser(id = UUID.randomUUID(), username = "api-key-admin", password = "api-key-admin", role = "api-admin"),
        ApiUser(id = UUID.randomUUID(), username = "admin", password = "admin", role = "app-admin")
    )

    override fun findByUsername(username: String): Uni<ApiUser?> {
        return apiUsers.find { it.username == username }.let { Uni.createFrom().item(it)}
    }

    override fun findByUsernameAndPassword(username: String, password: String): Uni<ApiUser?> {
        return apiUsers.find { it.username == username && it.password == password }.let { Uni.createFrom().item(it)}
    }

    override fun addUser(apiUser: ApiUser): Uni<String> {
        apiUser.id = UUID.randomUUID()
        apiUser.added = LocalDateTime.now()
        apiUsers.add(apiUser)
        return Uni.createFrom().item("mock-api-key-${apiUser.username}")
    }

    override fun setAdminPassword(password: String): Uni<ApiUser?> {
        apiUsers.find { it.username == "admin" }?.password = password

        return Uni.createFrom().item(apiUsers.find { it.username == "admin" })
    }

    override fun listAll(): Uni<List<ApiUser>> {
        return Uni.createFrom().item(apiUsers.toList())
    }

    override fun expireUser(userId: UUID): Uni<ApiUser> {
        val user = apiUsers.find { it.id == userId }
        if (user != null) {
            user.expiresAt = LocalDateTime.now()
            user.updated = LocalDateTime.now()
            return Uni.createFrom().item(user)
        }
        return Uni.createFrom().failure(NoSuchElementException("User not found"))
    }
}
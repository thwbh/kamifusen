package io.tohuwabohu.kamifusen.mock

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.ApiUser
import io.tohuwabohu.kamifusen.crud.ApiUserRepository

class ApiUserRepositoryMock : ApiUserRepository() {
    private final val keys = mutableListOf(
        ApiUser(username = "api-key-user", password = "api-key-user", role = "api-user"),
        ApiUser(username = "api-key-admin", password = "api-key-admin", role = "api-admin"),
        ApiUser(username ="admin", password = "admin", role = "app-admin")
    )

    override fun findByUsername(username: String): Uni<ApiUser?> {
        return keys.find { it.username == username }.let { Uni.createFrom().item(it)}
    }

    override fun findByUsernameAndPassword(username: String, password: String): Uni<ApiUser?> {
        return keys.find { it.username == username && it.password == password }.let { Uni.createFrom().item(it)}
    }

    override fun addUser(apiUser: ApiUser): Uni<ApiUser> {
        keys.add(apiUser)
        return Uni.createFrom().item(apiUser)
    }

    override fun setAdminPassword(password: String): Uni<ApiUser?> {
        val admin = ApiUser(
            username = "admin",
            password = "admin",
            role = "app-admin"
        )

        return Uni.createFrom().item(admin)
    }
}
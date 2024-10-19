package io.tohuwabohu.kamifusen.mock

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.ApiKey
import io.tohuwabohu.kamifusen.crud.ApiKeyRepository

class ApiKeyRepositoryMock : ApiKeyRepository() {
    val keys = mutableListOf<ApiKey>(
        ApiKey("api-key-user", "test-user", "api-user"),
        ApiKey("api-key-admin", "test-admin", "api-admin"))

    override fun findKey(key: String): Uni<ApiKey?> {
        return keys.find { it.apiKey == key }.let { Uni.createFrom().item(it)}
    }
}
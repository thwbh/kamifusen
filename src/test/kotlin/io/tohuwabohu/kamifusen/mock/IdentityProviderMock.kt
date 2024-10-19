package io.tohuwabohu.kamifusen.mock

import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.ApiKey
import io.tohuwabohu.kamifusen.crud.ApiKeyRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

class ApiKeyRepositoryMock : ApiKeyRepository() {
    val keys = mutableListOf<ApiKey>(
        ApiKey(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), "test-user", "api-user"),
        ApiKey(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), "test-admin", "api-admin"))

    override fun findKey(key: UUID): Uni<ApiKey> {
        return keys.find { it.apiKey == key }.let { Uni.createFrom().item(it)}
    }
}
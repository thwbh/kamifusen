package io.tohuwabohu.kamifusen.crud

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.proxy.HibernateProxy
import java.time.LocalDateTime
import java.util.*

@Entity
data class ApiKey (
    @Id
    var apiKey: UUID,
    var name: String,
    var role: String,
    var expiresAt: LocalDateTime? = null
) {
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as ApiKey

        return apiKey != null && apiKey == other.apiKey
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  apiKey = $apiKey   ,   name = $name   ,   role = $role   ,   expiresAt = $expiresAt )"
    }
}

@ApplicationScoped
class ApiKeyRepository : PanacheRepositoryBase<ApiKey, UUID> {
    fun findKey(key: UUID) = findById(key)
}
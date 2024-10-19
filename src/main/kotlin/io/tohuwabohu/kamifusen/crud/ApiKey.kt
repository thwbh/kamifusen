package io.tohuwabohu.kamifusen.crud

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.NamedQueries
import jakarta.persistence.NamedQuery
import jakarta.persistence.PrePersist
import org.hibernate.proxy.HibernateProxy
import java.time.LocalDateTime

@NamedQueries(
    NamedQuery(
        name = "ApiKey.findValidKey",
        query = "FROM ApiKey a WHERE a.apiKey = :apiKey AND a.expiresAt > :now")
)
@Entity
data class ApiKey (
    @Id
    var apiKey: String,
    var name: String,
    var role: String,
    var expiresAt: LocalDateTime? = null
) {
    @PrePersist
    fun obfuscateKey() {
        apiKey = BcryptUtil.bcryptHash(apiKey)
    }

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
class ApiKeyRepository : PanacheRepositoryBase<ApiKey, String> {
    fun findKey(key: String) = find("#ApiKey.findValidKey", BcryptUtil.bcryptHash(key)).firstResult()
}
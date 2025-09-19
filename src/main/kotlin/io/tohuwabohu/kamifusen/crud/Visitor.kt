package io.tohuwabohu.kamifusen.crud

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheRepositoryBase
import io.quarkus.runtime.util.HashUtil.sha256
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.proxy.HibernateProxy
import java.time.LocalDateTime
import java.util.*

@Entity
data class Visitor (
    @Id
    var id: UUID,
    var info: String,
    val firstSeen: LocalDateTime = LocalDateTime.now(),
    var lastSeen: LocalDateTime? = null,
    var userAgent: String? = null,
    var country: String? = null,
    var referrer: String? = null
) : PanacheEntityBase {
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as Visitor

        return id == other.id
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id   ,   info = $info   ,   firstSeen = $firstSeen   ,   lastSeen = $lastSeen )"
    }
}

@ApplicationScoped
class VisitorRepository : PanacheRepositoryBase<Visitor, UUID> {
    @WithTransaction
    fun addVisitor(remoteAddress: String, userAgent: String, referrer: String? = null, country: String? = null): Uni<Visitor> {
        val visitor = Visitor(
            id = UUID.randomUUID(),
            info = sha256("$remoteAddress $userAgent"),
            userAgent = userAgent,
            referrer = referrer,
            country = country
        )

        return persist(visitor)
    }

    fun findByInfo(remoteAddress: String, userAgent: String) = find("info", sha256("$remoteAddress $userAgent")).firstResult()

    @WithTransaction
    fun updateLastSeen(visitorId: UUID): Uni<Visitor?> {
        return findById(visitorId).onItem().ifNotNull().call { visitor ->
            visitor.lastSeen = LocalDateTime.now()
            persist(visitor)
        }
    }
}
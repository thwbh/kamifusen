package io.tohuwabohu.kamifusen.crud

import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheRepositoryBase
import io.quarkus.runtime.util.HashUtil.sha256
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.proxy.HibernateProxy
import java.awt.SystemColor.info
import java.util.*

@Entity
data class Visitor (
    @Id
    var id: UUID,
    var info: String
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

        return id != null && id == other.id
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id   ,   info = $info )"
    }
}

@ApplicationScoped
class VisitorRepository : PanacheRepositoryBase<Visitor, UUID> {
    @WithTransaction
    fun addVisitor(remoteAddress: String, userAgent: String): Uni<Visitor> {
        val visitor = Visitor(UUID.randomUUID(), sha256("$remoteAddress $userAgent"))

        return persist(visitor)
    }

    fun findByInfo(remoteAddress: String, userAgent: String) = find("info", sha256("$remoteAddress $userAgent")).firstResult()
}
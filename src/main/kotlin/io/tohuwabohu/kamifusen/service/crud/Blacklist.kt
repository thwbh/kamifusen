package io.tohuwabohu.kamifusen.service.crud

import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheRepositoryBase
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy
import java.time.LocalDateTime
import java.util.*

@Entity
data class Blacklist(
    @Id
    var id: UUID = UUID.randomUUID(),
    var pageId: UUID,
    val blacklistedAt: LocalDateTime = LocalDateTime.now(),
) : PanacheEntityBase {

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as Blacklist

        return id != null && id == other.id
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id   ,   pageId = $pageId   ,   blacklistedAt = $blacklistedAt )"
    }
}

@ApplicationScoped
class BlacklistRepository : PanacheRepositoryBase<Blacklist, UUID> {

    @WithTransaction
    fun findByPageId(pageId: UUID): Uni<Blacklist?> = find("pageId", pageId).firstResult()

    @WithTransaction
    fun isPageBlacklisted(pageId: UUID): Uni<Boolean> =
        findByPageId(pageId).map { it != null }

    @WithTransaction
    fun addPageToBlacklist(pageId: UUID): Uni<Blacklist> {
        val blacklistEntry = Blacklist(pageId = pageId)
        return persist(blacklistEntry)
    }

    @WithTransaction
    fun removePageFromBlacklist(pageId: UUID): Uni<Boolean> =
        find("pageId", pageId).firstResult()
            .onItem().ifNotNull().transformToUni { entry -> deleteById(entry!!.id) }
            .onItem().ifNull().continueWith(false)
}
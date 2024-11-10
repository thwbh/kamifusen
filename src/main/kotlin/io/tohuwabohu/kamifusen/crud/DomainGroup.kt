package io.tohuwabohu.kamifusen.crud

import io.quarkus.hibernate.reactive.panache.Panache
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheRepositoryBase
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.*
import jakarta.transaction.Transactional
import org.hibernate.proxy.HibernateProxy
import java.io.Serializable
import java.util.*

@NamedQueries(
    NamedQuery(
        name = "DomainGroup.findByDomain",
        query = "SELECT dg FROM DomainGroup dg JOIN dg.domains d WHERE d = :domain"
    )
)
@Entity
data class DomainGroup(
    @Id
    var id: UUID = UUID.randomUUID(),

    var groupName: String,

    @ElementCollection
    var domains: MutableSet<String>
) {
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as DomainGroup

        return id != null && id == other.id
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id   ,   groupName = $groupName   ,   domains = $domains )"
    }
}

@ApplicationScoped
class DomainGroupRepository : PanacheRepositoryBase<DomainGroup, UUID> {
    private fun findByGroupId(id: UUID): Uni<DomainGroup?> = find("id", id).firstResult()

    private fun findByDomain(domain: String): Uni<DomainGroup?> = find(
        "#DomainGroup.findByDomain", mapOf(
            "domain" to domain
        )
    ).firstResult()

    @Transactional
    fun addGroup(domain: String): Uni<DomainGroup> =
        persist(
            DomainGroup(
                groupName = domain,
                domains = mutableSetOf(domain)
            )
        )

    @Transactional
    fun addGroupIfAbsent(domain: String): Uni<DomainGroup?> =
        findByDomain(domain).onItem().ifNull().switchTo(addGroup(domain))

    @Transactional
    fun addToGroup(domain: String, groupId: UUID): Uni<DomainGroup?> =
        findByDomain(domain).flatMap { existingDomainGroup ->
            val updateExistingGroup = existingDomainGroup?.let {
                it.domains.remove(domain)
                Panache.getSession().flatMap { session -> session.merge(it) }
            } ?: Uni.createFrom().nullItem()

            updateExistingGroup.flatMap {
                findByGroupId(groupId).flatMap { targetDomainGroup ->
                    targetDomainGroup?.let {
                        it.domains.add(domain)
                        Panache.getSession().flatMap { session -> session.merge(it) }
                    } ?: Uni.createFrom().nullItem()
                }
            }
        }
}
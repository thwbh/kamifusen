package io.tohuwabohu.kamifusen.crud

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
@NamedQueries(
    NamedQuery(
        name = "Page.findByPathAndDomainGroup",
        query = "FROM Page p WHERE p.path = :path AND p.domainGroup = :domainGroup")
)
data class Page(
    @Id
    var id: UUID,
    var path: String,
    @ManyToOne
    var domainGroup: DomainGroup,
    val pageAdded: LocalDateTime = LocalDateTime.now(),
    var lastHit: LocalDateTime? = null,
) : PanacheEntityBase {
    @PreUpdate
    fun updateLastHit() {
        lastHit = LocalDateTime.now()
    }

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as Page

        return id != null && id == other.id
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id   ,   path = $path   ,   domainGroup = $domainGroup   ,   pageAdded = $pageAdded   ,   lastHit = $lastHit )"
    }
}

@ApplicationScoped
class PageRepository : PanacheRepositoryBase<Page, UUID> {
    fun findByPageId(id: UUID) = find("id", id).firstResult()

    @WithTransaction
    fun addPageIfAbsent(path: String, domainGroup: DomainGroup): Uni<Page?> = findPageByPathAndDomainGroup(path, domainGroup)
        .onItem().ifNull().switchTo(addPage(path, domainGroup))

    fun findPageByPathAndDomainGroup(path: String, domainGroup: DomainGroup) = find("#Page.findByPathAndDomainGroup",
        mapOf("path" to path, "domainGroup" to domainGroup)
    ).firstResult()

    fun listAllPages() = listAll()

    @WithTransaction
    fun addPage(path: String, domainGroup: DomainGroup): Uni<Page> {

        val page = Page(
            id = UUID.randomUUID(),
            path = path,
            domainGroup = domainGroup
        )

        return persist(page)
    }

    @WithTransaction
    fun deletePage(pageId: UUID): Uni<Boolean> = findById(pageId).onItem().ifNull().failWith(EntityNotFoundException()).onItem()
        .ifNotNull().transformToUni { entry -> deleteById(entry.id)}
}
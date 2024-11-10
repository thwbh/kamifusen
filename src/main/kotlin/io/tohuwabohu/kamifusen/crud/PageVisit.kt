package io.tohuwabohu.kamifusen.crud

import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy
import java.io.Serializable
import java.util.*

@NamedQueries(
    NamedQuery(name = "PageVisit.countVisitsPerPage", query = "SELECT count(*) FROM PageVisit WHERE pageId = :pageId AND visitorId = :visitorId")
)
@Entity
@IdClass(PageVisitCompositeKey::class)
data class PageVisit (
    @Id
    var pageId: UUID,
    @Id
    var visitorId: UUID,
): PanacheEntityBase {
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as PageVisit

        return pageId == other.pageId
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  pageId = $pageId   ,   visitorId = $visitorId )"
    }
}

@ApplicationScoped
class PageVisitRepository: PanacheRepository<PageVisit> {
    fun countVisits(pageId: UUID) = count("pageId = ?1", pageId)
    fun countVisitsForVisitor(pageId: UUID, visitorId: UUID) = count("#PageVisit.countVisitsPerPage", mapOf(
        "visitorId" to visitorId,
        "pageId" to pageId)
    )

    @WithTransaction
    fun addVisit(pageId: UUID, visitorId: UUID) = persist(PageVisit(pageId, visitorId))
}

@Embeddable
private class PageVisitCompositeKey(val pageId: UUID, val visitorId: UUID) : Serializable
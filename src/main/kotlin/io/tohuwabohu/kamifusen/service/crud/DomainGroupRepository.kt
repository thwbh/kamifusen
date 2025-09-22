package io.tohuwabohu.kamifusen.service.crud

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheRepositoryBase
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class DomainGroupRepository : PanacheRepositoryBase<DomainGroup, UUID> {

    fun findByParentDomain(parentDomain: String): Uni<DomainGroup?> {
        return find("parentDomain", parentDomain).firstResult()
    }

    fun findByChildDomain(childDomain: String): Uni<DomainGroup?> {
        return find(
            "SELECT dg FROM DomainGroup dg JOIN dg.members m WHERE m.childDomain = ?1",
            childDomain
        ).firstResult()
    }

    fun getEffectiveDomain(domain: String): Uni<String> {
        return findByChildDomain(domain)
            .map { domainGroup ->
                domainGroup?.parentDomain ?: domain
            }
    }

    fun createDomainGroup(name: String, parentDomain: String, description: String?, childDomains: List<String>): Uni<DomainGroup> {
        val domainGroup = DomainGroup(
            name = name,
            parentDomain = parentDomain,
            description = description
        )

        childDomains.forEach { childDomain ->
            domainGroup.members.add(
                DomainGroupMember(
                    domainGroup = domainGroup,
                    childDomain = childDomain
                )
            )
        }

        return persist(domainGroup)
    }

    fun addChildDomain(parentDomain: String, childDomain: String): Uni<Boolean> {
        return findByParentDomain(parentDomain)
            .flatMap { domainGroup ->
                if (domainGroup != null) {
                    val member = DomainGroupMember(
                        domainGroup = domainGroup,
                        childDomain = childDomain
                    )
                    domainGroup.members.add(member)
                    persist(domainGroup).map { true }
                } else {
                    Uni.createFrom().item(false)
                }
            }
    }

}
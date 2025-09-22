package io.tohuwabohu.kamifusen.service.crud

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "DomainGroup")
data class DomainGroup(
    @Id
    @GeneratedValue
    var id: UUID? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var parentDomain: String,

    @Column(length = 512)
    var description: String? = null,

    @Column(nullable = false)
    var created: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "domainGroup", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var members: MutableList<DomainGroupMember> = mutableListOf()
) : PanacheEntityBase()
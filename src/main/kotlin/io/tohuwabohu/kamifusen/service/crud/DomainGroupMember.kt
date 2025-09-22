package io.tohuwabohu.kamifusen.service.crud

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "DomainGroupMember")
data class DomainGroupMember(
    @Id
    @GeneratedValue
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domainGroupId", nullable = false)
    var domainGroup: DomainGroup,

    @Column(nullable = false, unique = true)
    var childDomain: String,

    @Column(nullable = false)
    var added: LocalDateTime = LocalDateTime.now()
) : PanacheEntityBase()
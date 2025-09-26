package io.tohuwabohu.kamifusen.service.crud

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.hibernate.reactive.panache.Panache
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheRepositoryBase
import io.quarkus.security.UnauthorizedException
import io.quarkus.security.jpa.Password
import io.quarkus.security.jpa.Roles
import io.quarkus.security.jpa.UserDefinition
import io.quarkus.security.jpa.Username
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.unchecked.Unchecked
import io.tohuwabohu.kamifusen.api.generated.model.ApiUserDto
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.*
import jakarta.ws.rs.NotFoundException
import org.hibernate.proxy.HibernateProxy
import java.time.LocalDateTime
import java.util.*

@NamedQueries(
    NamedQuery(
        name = "ApiUser.findValidUser",
        query = "FROM ApiUser a WHERE a.username = :username AND (a.expiresAt IS NULL OR a.expiresAt > :now)"
    )
)
@Entity
@UserDefinition
data class ApiUser(
    @Id
    var id: UUID? = null,
    @Username
    var username: String,
    @Password
    var password: String? = null,
    @Roles
    var role: String,
    var expiresAt: LocalDateTime? = null,
    var added: LocalDateTime? = null,
    var updated: LocalDateTime? = null
) {
    @PrePersist
    fun beforePersist() {
        id = UUID.randomUUID()
        password = BcryptUtil.bcryptHash(password)
        added = LocalDateTime.now()
    }

    @PreUpdate
    fun beforeUpdate() {
        updated = LocalDateTime.now()
    }

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as ApiUser

        return id != null && id == other.id
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id   ,   username = $username   ,   password = $password   ,   role = $role   ,   expiresAt = $expiresAt   ,   added = $added )"
    }

    final fun toDto() = ApiUserDto(username, role, id, expiresAt, added, updated)
}

@ApplicationScoped
class ApiUserRepository : PanacheRepositoryBase<ApiUser, UUID> {

    @WithTransaction
    fun addUser(apiUser: ApiUser): Uni<String> {
        val randomPwd = UUID.randomUUID().toString()
        apiUser.password = randomPwd

        return persist(apiUser).map { Base64.getEncoder().encodeToString("${it.username}:${randomPwd}".toByteArray()) }
    }

    @WithTransaction
    fun findByUuid(id: UUID) = findById(id)

    @WithTransaction
    fun findByUsername(username: String) = find(
        "#ApiUser.findValidUser", mapOf(
            "username" to username,
            "now" to LocalDateTime.now()
        )
    ).firstResult()

    @WithTransaction
    fun findByUsernameAndPassword(username: String, password: String): Uni<ApiUser?> =
        findByUsername(username).onItem().ifNotNull().invoke(Unchecked.consumer { user ->
            if (!BcryptUtil.matches(password, user!!.password)) {
                throw UnauthorizedException()
            }
        }).onItem().ifNull().failWith(UnauthorizedException())

    @WithTransaction
    fun updateAdmin(oldUsername: String, newUsername: String, newPassword: String): Uni<ApiUser?> {
        return findByUsername(oldUsername).onItem().ifNotNull().call { user ->
            user?.username = newUsername
            user?.password = BcryptUtil.bcryptHash(newPassword)
            user?.updated = LocalDateTime.now()

            Panache.getSession().call { s -> s.merge(user) }
        }.onItem().ifNull().failWith(NotFoundException())
    }

    @WithTransaction
    fun expireUser(userId: UUID): Uni<ApiUser> =
        findById(userId).onItem().ifNotNull().call { user ->
            user.expiresAt = LocalDateTime.now()

            Panache.getSession().call { s -> s.merge(user) }
        }.onItem().ifNull().failWith(NotFoundException())

    @WithTransaction
    fun updateUser(userId: UUID, username: String, expiresAt: LocalDateTime?): Uni<ApiUser> =
        findById(userId).onItem().ifNotNull().call { user ->
            user.username = username
            user.expiresAt = expiresAt
            user.updated = LocalDateTime.now()

            Panache.getSession().call { s -> s.merge(user) }
        }.onItem().ifNull().failWith(NotFoundException())

    @WithTransaction
    fun deleteUser(userId: UUID): Uni<Boolean> =
        findById(userId).onItem().ifNotNull().call { user ->
            Panache.getSession().call { s -> s.remove(user) }
        }.onItem().ifNull().failWith(NotFoundException())
            .map { true }

    @WithTransaction
    fun renewUser(userId: UUID, expiresAt: LocalDateTime?): Uni<String> =
        findById(userId).flatMap { user ->
            val randomPwd = UUID.randomUUID().toString()
            user.password = BcryptUtil.bcryptHash(randomPwd)
            user.expiresAt = expiresAt
            user.updated = LocalDateTime.now()

            Panache.getSession().call { s -> s.merge(user) }
                .map { Base64.getEncoder().encodeToString("${user.username}:${randomPwd}".toByteArray()) }
        }.onItem().ifNull().failWith(NotFoundException())

    @WithTransaction
    fun updateUserWithPassword(userId: UUID, username: String, password: String?): Uni<ApiUser> =
        findById(userId).onItem().ifNotNull().call { user ->
            user.username = username
            if (!password.isNullOrBlank()) {
                user.password = BcryptUtil.bcryptHash(password)
            }
            user.updated = LocalDateTime.now()

            Panache.getSession().call { s -> s.merge(user) }
        }.onItem().ifNull().failWith(NotFoundException())
}
package io.tohuwabohu.kamifusen

import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.logging.Log
import io.quarkus.security.identity.SecurityIdentity
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.api.generated.AppAdminResourceApi
import io.tohuwabohu.kamifusen.error.recoverWithResponse
import io.tohuwabohu.kamifusen.service.PageStatsService
import io.tohuwabohu.kamifusen.service.StatsService
import io.tohuwabohu.kamifusen.service.crud.ApiUser
import io.tohuwabohu.kamifusen.service.crud.ApiUserRepository
import io.tohuwabohu.kamifusen.service.crud.BlacklistRepository
import io.tohuwabohu.kamifusen.service.crud.PageRepository
import io.tohuwabohu.kamifusen.service.validator.UserValidation
import io.tohuwabohu.kamifusen.service.validator.validatePassword
import io.tohuwabohu.kamifusen.service.validator.validateUser
import jakarta.annotation.PostConstruct
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import java.net.URI
import java.time.LocalDateTime
import java.util.*

class AppAdminResource(
    private var apiUserRepository: ApiUserRepository,
    private var pageStatsService: PageStatsService,
    private var pageRepository: PageRepository,
    private var statsService: StatsService,
    private var blacklistRepository: BlacklistRepository
) : AppAdminResourceApi {

    @Inject
    private lateinit var securityIdentity: SecurityIdentity

    @PostConstruct
    fun init() {
        Log.info("AppAdminResource initialized via CDI")
    }

    @RolesAllowed("app-admin")
    override fun adminLanding(): Uni<Response> {
        val username = securityIdentity.principal.name
        Log.info("Landing page accessed by user: $username")
        Log.info("Security identity attributes in landing: ${securityIdentity.attributes}")

        // Since attributes are lost, check if user still has default credentials
        return apiUserRepository.findByUsernameAndPassword(username, "admin")
            .map { _ ->
                Log.info("Redirecting to password change page")
                Response.seeOther(URI.create("/?page=change-password")).build()
            }.onFailure().recoverWithItem(Response.seeOther(URI.create("/?page=dashboard")).build())

    }

    @WithSession
    @RolesAllowed("app-admin")
    override fun generateApiKey(
        username: String,
        role: String,
        expiresAt: String
    ): Uni<Response> =
        validateUser(username, apiUserRepository).flatMap { result ->
            if (result == UserValidation.VALID) {
                apiUserRepository.addUser(
                    ApiUser(
                        username = username,
                        role = role,
                        expiresAt = when (expiresAt) {
                            "" -> null
                            else -> LocalDateTime.parse(expiresAt)
                        },
                    )
                ).map { keyRaw -> Response.ok(keyRaw).build() }
            } else {
                Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST).entity("User validation failed: $result").build()
                )
            }
        }.onFailure().invoke { e -> Log.error("Error during keygen.", e) }
            .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build())

    @WithSession
    @RolesAllowed("app-admin")
    override fun getStats(timeRange: String?): Uni<Response> =
        statsService.getAggregatedStats(timeRange ?: "7d")
            .map { stats -> Response.ok(stats).build() }
            .onFailure().invoke { e -> Log.error("Error receiving aggregated stats.", e) }
            .onFailure().recoverWithResponse()

    @WithSession
    @RolesAllowed("app-admin")
    override fun getVisits(): Uni<Response> =
        pageStatsService.getAllPageVisits()
            .flatMap {
                Uni.createFrom().item(Response.ok(it).build())
            }
            .onFailure().invoke { e -> Log.error("Error receiving visits.", e) }
            .onFailure().recoverWithResponse()

    @WithSession
    @RolesAllowed("app-admin")
    override fun listPages(): Uni<Response> =
        pageStatsService.getNonBlacklistedPagesWithStats().flatMap {
            Uni.createFrom().item(Response.ok(it).build())
        }.onFailure().invoke { e -> Log.error("Error receiving pages.", e) }
            .onFailure().recoverWithResponse()

    @WithSession
    @RolesAllowed("app-admin")
    override fun listBlacklistedPages(domain: String?): Uni<Response> =
        if (domain != null) {
            pageStatsService.getBlacklistedPagesByDomainWithStats(domain)
        } else {
            pageStatsService.getBlacklistedPagesWithStats()
        }.flatMap {
            Uni.createFrom().item(Response.ok(it).build())
        }.onFailure().invoke { e -> Log.error("Error receiving blacklisted pages.", e) }
            .onFailure().recoverWithResponse()

    @WithSession
    @RolesAllowed("app-admin")
    override fun restorePage(pageId: UUID): Uni<Response> =
        blacklistRepository.removePageFromBlacklist(pageId).map { Response.ok().build() }
            .onFailure().invoke { e -> Log.error("Error during page restoration.", e) }
            .onFailure().recoverWithResponse()

    @WithSession
    @RolesAllowed("app-admin")
    override fun listUsers(): Uni<Response> =
        apiUserRepository.listAll().flatMap { users ->
            Uni.createFrom().item(Response.ok(users).build())
        }.onFailure().invoke { e -> Log.error("Error receiving users.", e) }
            .onFailure().recoverWithResponse()

    @WithSession
    @RolesAllowed("app-admin")
    override fun retireApiKey(userId: UUID): Uni<Response> =
        apiUserRepository.expireUser(userId).onItem()
            .transform { Response.ok().build() }
            .onFailure().invoke { e -> Log.error("Error during key retirement", e) }
            .onFailure().recoverWithResponse()

    @WithSession
    @RolesAllowed("app-admin")
    override fun renewApiKey(
        userId: UUID,
        expiresAt: String?
    ): Uni<Response> =
        apiUserRepository.renewUser(userId,
            if (expiresAt.isNullOrBlank()) null else LocalDateTime.parse(expiresAt)
        ).map { keyRaw -> Response.ok(keyRaw).build() }
        .onFailure().invoke { e -> Log.error("Error during key renewal", e) }
        .onFailure().recoverWithResponse()

    @WithSession
    @RolesAllowed("app-admin")
    override fun updateUser(
        userId: UUID,
        username: String,
        expiresAt: String?
    ): Uni<Response> {
        return validateUser(username, apiUserRepository).flatMap { userValidation ->
            if (!userValidation.valid) {
                Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                        .entity(userValidation.message ?: "Username validation failed")
                        .build()
                )
            } else {
                val parsedExpiresAt = if (expiresAt.isNullOrBlank()) null else LocalDateTime.parse(expiresAt)
                apiUserRepository.updateUser(userId, username, parsedExpiresAt)
                    .onItem().transform { Response.ok().build() }
                    .onFailure().invoke { e -> Log.error("Error updating user $userId", e) }
                    .onFailure().recoverWithResponse()
            }
        }
    }

    @WithSession
    @RolesAllowed("app-admin")
    override fun updateUserPassword(
        userId: UUID,
        username: String,
        password: String?
    ): Uni<Response> {
        return validateUser(username, apiUserRepository).flatMap { userValidation ->
            if (!userValidation.valid) {
                Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                        .entity(userValidation.message ?: "Username validation failed")
                        .build()
                )
            } else {
                apiUserRepository.updateUserWithPassword(userId, username, password)
                    .onItem().transform { Response.ok().build() }
                    .onFailure().invoke { e -> Log.error("Error updating user password $userId", e) }
                    .onFailure().recoverWithResponse()
            }
        }
    }

    @WithSession
    @RolesAllowed("app-admin")
    override fun deleteUser(userId: UUID): Uni<Response> =
        apiUserRepository.deleteUser(userId)
            .onItem().transform { deleted ->
                if (deleted) {
                    Response.ok().build()
                } else {
                    Response.status(Response.Status.NOT_FOUND).build()
                }
            }
            .onFailure().invoke { e -> Log.error("Error deleting user $userId", e) }
            .onFailure().recoverWithResponse()

    @WithSession
    @RolesAllowed("app-admin")
    override fun unregisterPage(pageId: UUID): Uni<Response> =
        pageRepository.deletePage(pageId, blacklistRepository).map { Response.ok().build() }
            .onFailure().invoke { e -> Log.error("Error during page blacklisting.", e) }
            .onFailure().recoverWithResponse()

    @WithSession
    @RolesAllowed("app-admin")
    override fun updateAdmin(
        oldUsername: String,
        newUsername: String,
        oldPassword: String,
        newPassword: String
    ): Uni<Response> {
        // Validate required parameters
        return validatePassword(newPassword, newPassword).flatMap { passwordValidation ->
            if (!passwordValidation.valid) {
                Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                        .entity(passwordValidation.message ?: "Password validation failed")
                        .build()
                )
            } else {
                // Only validate newUsername if it's different from oldUsername (for username changes)
                if (newUsername != oldUsername) {
                    validateUser(newUsername, apiUserRepository).flatMap { userValidation ->
                        if (!userValidation.valid) {
                            Uni.createFrom().item(
                                Response.status(Response.Status.BAD_REQUEST)
                                    .entity(userValidation.message ?: "Username validation failed")
                                    .build()
                            )
                        } else {
                            apiUserRepository.updateAdmin(oldUsername, newUsername, oldPassword, newPassword).flatMap { user ->
                                Uni.createFrom().item(Response.ok(user).build())
                            }
                        }
                    }
                } else {
                    apiUserRepository.updateAdmin(oldUsername, newUsername, oldPassword, newPassword).flatMap { user ->
                        Uni.createFrom().item(Response.ok(user).build())
                    }
                }
            }
        }.onFailure().invoke { e -> Log.error("Error updating admin password.", e) }
            .onFailure().recoverWithResponse()
    }
}
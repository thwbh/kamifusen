package io.tohuwabohu.kamifusen.security

import io.quarkus.security.UnauthorizedException
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.IdentityProvider
import io.quarkus.security.identity.IdentityProviderManager
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.request.AuthenticationRequest
import io.quarkus.security.runtime.QuarkusSecurityIdentity
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.ApiKeyRepository
import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.SecurityContext
import jakarta.ws.rs.ext.Provider
import java.security.Principal
import java.util.*

class ApiKeyAuthenticationRequest(val apiKey: String) : AuthenticationRequest {

    private val attributes: MutableMap<String, Any> = mutableMapOf()

    override fun <T : Any?> getAttribute(name: String?): T? {
        @Suppress("UNCHECKED_CAST")
        return attributes[name] as T?
    }

    override fun setAttribute(name: String?, value: Any?) {
        if (name != null && value != null) {
            attributes[name] = value
        }
    }

    override fun getAttributes(): MutableMap<String, Any> {
        return attributes
    }
}

@ApplicationScoped
class ApiKeyIdentityProvider(
    private val apiKeyRepository: ApiKeyRepository
) : IdentityProvider<ApiKeyAuthenticationRequest> {

    override fun authenticate(
        credentials: ApiKeyAuthenticationRequest,
        context: AuthenticationRequestContext
    ): Uni<SecurityIdentity> {
        val challenge = UUID.fromString(credentials.apiKey)

        return apiKeyRepository.findKey(challenge).flatMap { apiKey ->
            if (apiKey != null) {
                Uni.createFrom().item(QuarkusSecurityIdentity.builder()
                    .addRole(apiKey.role)
                    .setPrincipal { credentials.apiKey }
                    .build()
                )
            } else {
                Uni.createFrom().failure(UnauthorizedException())
            }
        }
    }

    override fun getRequestType(): Class<ApiKeyAuthenticationRequest> {
        return ApiKeyAuthenticationRequest::class.java
    }
}

@Provider
@Priority(Priorities.AUTHENTICATION)
class ApiKeyFilter @Inject constructor(
    private val identityProviderManager: IdentityProviderManager
) : ContainerRequestFilter {

    override fun filter(requestContext: ContainerRequestContext) {
        val apiKey = requestContext.getHeaderString("Authorization")
            ?: throw UnauthorizedException()

        val authRequest = ApiKeyAuthenticationRequest(apiKey)
        val uni: Uni<SecurityIdentity> = identityProviderManager.authenticate(authRequest)

        // Block until result is available
        uni.subscribe().with(
            { identity -> requestContext.securityContext = ApiKeySecurityContext(identity) },
            { throw UnauthorizedException() }
        )
    }
}

class ApiKeySecurityContext(
    private val securityIdentity: SecurityIdentity
) : SecurityContext {
    override fun getUserPrincipal(): Principal = securityIdentity.principal

    override fun isUserInRole(role: String?) = securityIdentity.roles.contains(role)

    override fun isSecure() = true

    override fun getAuthenticationScheme() = "API_KEY"
}
package io.tohuwabohu.kamifusen.crud.security

import io.quarkus.security.UnauthorizedException
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.IdentityProvider
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest
import io.quarkus.security.runtime.QuarkusSecurityIdentity
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.crud.ApiUserRepository
import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Priorities

@ApplicationScoped
@Priority(Priorities.AUTHENTICATION)
class ApiUserIdentityProvider(
    val apiUserRepository: ApiUserRepository
) : IdentityProvider<UsernamePasswordAuthenticationRequest> {

    override fun authenticate(request: UsernamePasswordAuthenticationRequest, context: AuthenticationRequestContext): Uni<SecurityIdentity> {
        val username = request.username
        val password = String(request.password.password)

        return apiUserRepository.findByUsernameAndPassword(username, password)
            .flatMap { apiUser ->
                if (apiUser != null) {
                    Uni.createFrom().item(
                        QuarkusSecurityIdentity.builder()
                            .addRole(apiUser.role)
                            .setPrincipal { username }
                            .build()
                    )
                } else {
                    Uni.createFrom().failure(UnauthorizedException())
                }
            }
    }

    override fun getRequestType(): Class<UsernamePasswordAuthenticationRequest> {
        return UsernamePasswordAuthenticationRequest::class.java
    }
}
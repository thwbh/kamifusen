package io.tohuwabohu.kamifusen

import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.api.generated.HealthResourceApi
import io.tohuwabohu.kamifusen.service.HealthService
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.core.Response

class HealthResource(
    private val healthService: HealthService,
): HealthResourceApi {

    @RolesAllowed("app-admin")
    @WithSession
    override fun getSystemHealth(): Uni<Response> {
        return healthService.getHealthMetrics().map { metrics -> Response.ok(metrics).build() }
    }
}
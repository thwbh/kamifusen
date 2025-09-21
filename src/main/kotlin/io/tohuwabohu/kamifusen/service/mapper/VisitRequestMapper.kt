package io.tohuwabohu.kamifusen.service.mapper

import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.api.generated.model.PageHitRequestDto
import io.tohuwabohu.kamifusen.service.context.VisitContext
import io.tohuwabohu.kamifusen.service.context.VisitorInfo
import io.vertx.core.http.HttpServerRequest
import jakarta.enterprise.context.ApplicationScoped

/**
 * Mapper service for converting HTTP requests to domain DTOs
 */
@ApplicationScoped
class VisitRequestMapper {
    companion object {
        private const val UNKNOWN_USER_AGENT = "unknown"
    }

    /**
     * Maps HTTP request and body to a VisitContextDto for service processing
     */
    fun mapToVisitContext(request: HttpServerRequest, body: PageHitRequestDto): Uni<VisitContext> {
        val visitorInfo = extractVisitorInfo(request)

        Log.debug("Mapping request for ${body.domain}${body.path} from ${visitorInfo.remoteAddress}")

        return Uni.createFrom().item(
            VisitContext(
                remoteAddress = visitorInfo.remoteAddress,
                userAgent = visitorInfo.userAgent,
                referrer = visitorInfo.referrer,
                country = visitorInfo.country,
                pageHit = body
            )
        )
    }

    /**
     * Extracts visitor information from HTTP request headers and connection details
     */
    fun extractVisitorInfo(request: HttpServerRequest): VisitorInfo {
        val remoteAddress = extractRemoteAddress(request)
        val userAgent = extractUserAgent(request)
        val referrer = extractReferrer(request)
        val country = extractCountry(request) // Placeholder for future GeoIP implementation

        return VisitorInfo(
            remoteAddress = remoteAddress,
            userAgent = userAgent,
            referrer = referrer,
            country = country
        )
    }

    /**
     * Extracts the real client IP address, considering proxy headers
     */
    private fun extractRemoteAddress(request: HttpServerRequest): String {
        // Check for common proxy headers first
        val xForwardedFor = request.headers().get("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            val firstIp = xForwardedFor.split(",").firstOrNull()?.trim()
            if (!firstIp.isNullOrBlank()) {
                return firstIp
            }
        }

        val xRealIp = request.headers().get("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }

        // Fallback to direct connection address
        return request.remoteAddress()?.host() ?: "unknown"
    }

    /**
     * Extracts and normalizes the User-Agent header
     */
    private fun extractUserAgent(request: HttpServerRequest): String {
        val userAgent = request.headers().get("User-Agent")
        return when {
            userAgent.isNullOrBlank() -> UNKNOWN_USER_AGENT
            userAgent.length > 512 -> {
                Log.warn("User-Agent header too long, truncating: ${userAgent.take(50)}...")
                userAgent.take(512)
            }
            else -> userAgent
        }
    }

    /**
     * Extracts the Referer header (note: Referer is the correct HTTP header name, despite the typo)
     */
    private fun extractReferrer(request: HttpServerRequest): String? {
        val referer = request.headers().get("Referer")
        return when {
            referer.isNullOrBlank() -> null
            referer.length > 1024 -> {
                Log.warn("Referer header too long, truncating: ${referer.take(50)}...")
                referer.take(1024)
            }
            else -> referer
        }
    }

    /**
     * Placeholder for country extraction via GeoIP lookup
     * TODO: Implement actual GeoIP service integration
     */
    private fun extractCountry(request: HttpServerRequest): String? {
        // Future implementation could:
        // 1. Use MaxMind GeoIP2 database
        // 2. Call external GeoIP API
        // 3. Use CloudFlare's CF-IPCountry header if behind CloudFlare

        val cfCountry = request.headers().get("CF-IPCountry")
        if (!cfCountry.isNullOrBlank() && cfCountry != "XX") {
            return cfCountry.uppercase()
        }

        return null
    }

    /**
     * Validates that the page hit request contains required fields
     */
    fun validatePageHitRequest(body: PageHitRequestDto): Uni<List<String>?> {
        val errors = mutableListOf<String>()

        if (body.path.isBlank()) {
            errors.add("Path cannot be empty")
        }

        if (body.domain.isBlank()) {
            errors.add("Domain cannot be empty")
        }

        if (body.path.length > 2048) {
            errors.add("Path too long (max 2048 characters)")
        }

        if (body.domain.length > 253) {
            errors.add("Domain too long (max 253 characters)")
        }

        // Basic domain validation
        if (body.domain.isNotBlank() && !isValidDomain(body.domain)) {
            errors.add("Invalid domain format")
        }

        return if (errors.isEmpty()) {
            Uni.createFrom().nullItem()
        } else {
            Uni.createFrom().item(errors)
        }
    }

    /**
     * Basic domain validation
     */
    private fun isValidDomain(domain: String): Boolean {
        val domainRegex = Regex("^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$")
        return domainRegex.matches(domain)
    }
}
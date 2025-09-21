package io.tohuwabohu.kamifusen.service.context

import io.tohuwabohu.kamifusen.api.generated.model.PageHitRequestDto
import java.util.*

/**
 * Internal context DTOs for service layer
 */
data class VisitContext(
    val remoteAddress: String,
    val userAgent: String,
    val referrer: String?,
    val country: String?,
    val pageHit: PageHitRequestDto
)

data class VisitorInfo(
    val remoteAddress: String,
    val userAgent: String,
    val referrer: String?,
    val country: String?
)

data class VisitResult(
    val visitCount: Long,
    val isNewVisitor: Boolean,
    val sessionId: UUID?
)
package io.tohuwabohu.kamifusen.extensions

import io.tohuwabohu.kamifusen.api.generated.model.PageWithStatsDto
import jakarta.persistence.Tuple
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.UUID

/**
 * Extension function to convert a JPA Tuple object to a PageVisitDto.
 *
 * This function assumes that the Tuple contains the following data at the specified positions:
 * - Position 0: The unique identifier of the page visit (UUID).
 * - Position 1: The URL path of the visited page (String).
 * - Position 2: The timestamp indicating when the page was added (LocalDateTime).
 * - Position 3: The number of visits the page has received (Long).
 * - Position 4: The domain of the visited page, which can be nullable (String).
 *
 * @receiver Tuple The JPA Tuple object containing the necessary data.
 * @return PageVisitDto The PageVisitDto object created from the Tuple data.
 */
fun Tuple.toPageStatsDto(): PageWithStatsDto = PageWithStatsDto(
    id = this.get(0, UUID::class.java),
    path = this.get(1, String::class.java),
    pageAdded = this.get(2, LocalDateTime::class.java),
    visitCount = this.get(3, Long::class.javaObjectType),
    domain = this.get(4, String::class.java),
    lastHit = this.get(5, LocalDateTime::class.java)
)

/**
 * Helper function to round percentages to 2 decimal places
 */
fun Double.roundTo(scale: Int): Double {
    return BigDecimal(this).setScale(2, RoundingMode.HALF_UP).toDouble()
}
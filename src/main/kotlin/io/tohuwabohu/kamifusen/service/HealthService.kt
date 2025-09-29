package io.tohuwabohu.kamifusen.service

import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.tohuwabohu.kamifusen.api.generated.model.HealthMetricDto
import io.tohuwabohu.kamifusen.api.generated.model.SystemHealthDto
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.hibernate.reactive.mutiny.Mutiny
import java.time.LocalDateTime

@ApplicationScoped
class HealthService {
    @Inject
    private lateinit var sessionFactory: Mutiny.SessionFactory

    fun getHealthMetrics(): Uni<SystemHealthDto> {
        return getDatabaseDiskSpace().map { diskSpaceMetric ->
            val metrics = mutableListOf<HealthMetricDto>()
            val timestamp = LocalDateTime.now(java.time.ZoneOffset.UTC)

            // Add disk space metric first
            metrics.add(diskSpaceMetric)

            // Memory Usage - Native-compatible approach
            val runtime = Runtime.getRuntime()
            val maxMemory = runtime.maxMemory()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            val memoryPercentage = if (maxMemory > 0) {
                ((usedMemory.toDouble() / maxMemory.toDouble()) * 100).toInt()
            } else {
                0
            }
            val memoryStatus = when {
                memoryPercentage > 85 -> "critical"
                memoryPercentage > 75 -> "warning"
                else -> "healthy"
            }
            metrics.add(HealthMetricDto(
                name = "Memory Usage",
                value = "${memoryPercentage}%",
                status = memoryStatus,
                description = "Runtime memory utilization",
                timestamp = timestamp
            ))

            // Available Processors - GraalVM-compatible
            val availableProcessors = runtime.availableProcessors()
            metrics.add(HealthMetricDto(
                name = "CPU Cores",
                value = availableProcessors.toString(),
                status = "healthy",
                description = "Available processor cores",
                timestamp = timestamp
            ))

            // System Properties - Native-compatible
            val javaVersion = System.getProperty("java.version", "unknown")
            metrics.add(HealthMetricDto(
                name = "Java Version",
                value = javaVersion,
                status = "healthy",
                description = "Runtime Java version",
                timestamp = timestamp
            ))

            // Runtime Type Detection - GraalVM vs JVM
            val runtimeType = detectRuntimeType()
            metrics.add(HealthMetricDto(
                name = "Runtime Type",
                value = runtimeType,
                status = "healthy",
                description = "Runtime environment type",
                timestamp = timestamp
            ))

            // Simple uptime based on class loading time (approximation)
            val uptimeMinutes = (System.currentTimeMillis() - timestamp.atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()) / (1000 * 60)
            metrics.add(HealthMetricDto(
                name = "Uptime",
                value = "$uptimeMinutes",
                status = "healthy",
                description = "Application uptime",
                timestamp = timestamp
            ))

            // Determine overall status
            val criticalCount = metrics.count { it.status == "critical" }
            val warningCount = metrics.count { it.status == "warning" }

            val overallStatus = when {
                criticalCount > 0 -> "critical"
                warningCount > 0 -> "warning"
                else -> "healthy"
            }

            val overallText = when (overallStatus) {
                "critical" -> "SYSTEM DEGRADED"
                "warning" -> "MONITORING ALERTS"
                else -> "ALL SYSTEMS NOMINAL"
            }

            SystemHealthDto(
                overallStatus = overallStatus,
                overallText = overallText,
                metrics = metrics,
                timestamp = timestamp
            )
        }
    }

    private fun getDatabaseDiskSpace(): Uni<HealthMetricDto> {
        val timestamp = LocalDateTime.now(java.time.ZoneOffset.UTC)

        return sessionFactory.withSession { session ->
            // PostgreSQL query to get database size and available disk space
            session.createNativeQuery("""
                SELECT
                    pg_size_pretty(pg_database_size(current_database())) as db_size,
                    pg_database_size(current_database()) as db_size_bytes
            """, Object::class.java)
                .singleResult
                .onItem().transform { result ->
                    val resultArray = result as Array<*>
                    val dbSizeFormatted = resultArray[0] as String
                    val dbSizeBytes = resultArray[1] as Long

                    // For disk space, we'd need a system-level check or database-specific query
                    // This is a simplified approach showing database size
                    val status = when {
                        dbSizeBytes in 10000000001..50_000_000_000L -> "warning" // > 10GB
                        dbSizeBytes > 50_000_000_000L -> "critical" // > 50GB
                        else -> "healthy"
                    }

                    HealthMetricDto(
                        name = "Database Size",
                        value = dbSizeFormatted,
                        status = status,
                        description = "Current database size",
                        timestamp = timestamp
                    )
                }
                .onFailure().recoverWithItem { e ->
                    Log.debug("Unable to determine database size: ${e.message}")

                    // Fallback if query fails
                    HealthMetricDto(
                        name = "Database Size",
                        value = "UNKNOWN",
                        status = "warning",
                        description = "Unable to determine database size",
                        timestamp = timestamp
                    )
                }
        }
    }

    private fun detectRuntimeType(): String {
        return try {
            // Check for GraalVM-specific properties
            val vmName = System.getProperty("java.vm.name", "").lowercase()
            val vmVendor = System.getProperty("java.vm.vendor", "").lowercase()

            // Check if we're running on GraalVM Native Image
            val graalvmVersion = System.getProperty("org.graalvm.version")
            val isNativeImage = System.getProperty("org.graalvm.nativeimage.imagecode") != null

            when {
                isNativeImage -> "NATIVE"
                graalvmVersion != null || vmName.contains("graalvm") || vmVendor.contains("graalvm") -> "GRAALVM"
                vmName.contains("hotspot") -> "HOTSPOT"
                vmName.contains("openj9") -> "OPENJ9"
                else -> "JVM"
            }
        } catch (e: Exception) {
            "UNKNOWN"
        }
    }
}
package io.tohuwabohu.kamifusen.service.mapper

import io.quarkus.test.junit.QuarkusTest
import io.tohuwabohu.kamifusen.api.generated.model.PageHitRequestDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@QuarkusTest
class VisitRequestMapperUnitTest {

    private val visitRequestMapper = VisitRequestMapper()

    // Test validation logic directly - these are pure functions

    @Test
    fun `should validate domain with hyphens correctly`() {
        val request = PageHitRequestDto("/test", "test-domain.co.uk")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNull(result) // Should be valid
    }

    @Test
    fun `should reject domain starting with hyphen`() {
        val request = PageHitRequestDto("/test", "-invalid.com")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNotNull(result)
        assertTrue(result!!.contains("Invalid domain format"))
    }

    @Test
    fun `should reject domain ending with hyphen`() {
        val request = PageHitRequestDto("/test", "invalid-.com")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNotNull(result)
        assertTrue(result!!.contains("Invalid domain format"))
    }

    @Test
    fun `should validate single letter domain`() {
        val request = PageHitRequestDto("/test", "a.com")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNull(result) // Should be valid
    }

    @Test
    fun `should handle blank vs empty domain differently`() {
        val request = PageHitRequestDto("/test", "   ")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        // This should still be treated as empty since we check isBlank()
        assertNotNull(result)
        assertTrue(result!!.contains("Domain cannot be empty"))
    }

    @Test
    fun `should handle domain validation when domain is not blank but validation fails`() {
        val request = PageHitRequestDto("/test", "invalid@domain.com")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNotNull(result)
        assertTrue(result!!.contains("Invalid domain format"))
    }

    @Test
    fun `should validate numeric domain parts`() {
        val request = PageHitRequestDto("/test", "123.456.789.com")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNull(result) // Should be valid
    }

    @Test
    fun `should reject domain with consecutive dots`() {
        val request = PageHitRequestDto("/test", "invalid..domain.com")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNotNull(result)
        assertTrue(result!!.contains("Invalid domain format"))
    }

    @Test
    fun `should reject domain with special characters`() {
        val request = PageHitRequestDto("/test", "invalid#domain.com")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNotNull(result)
        assertTrue(result!!.contains("Invalid domain format"))
    }

    @Test
    fun `should reject domain starting with dot`() {
        val request = PageHitRequestDto("/test", ".invalid.com")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNotNull(result)
        assertTrue(result!!.contains("Invalid domain format"))
    }

    @Test
    fun `should reject domain ending with dot`() {
        val request = PageHitRequestDto("/test", "invalid.com.")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNotNull(result)
        assertTrue(result!!.contains("Invalid domain format"))
    }

    @Test
    fun `should validate subdomain with multiple levels`() {
        val request = PageHitRequestDto("/test", "api.v1.test.example.com")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNull(result) // Should be valid
    }

    @Test
    fun `should validate domain with numbers and hyphens`() {
        val request = PageHitRequestDto("/test", "test-123.example-456.com")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNull(result) // Should be valid
    }

    @Test
    fun `should reject path that is just whitespace`() {
        val request = PageHitRequestDto("   ", "example.com")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNotNull(result)
        assertTrue(result!!.contains("Path cannot be empty"))
    }

    @Test
    fun `should validate long but acceptable path`() {
        val longPath = "/" + "a".repeat(2000) // Under 2048 characters
        val request = PageHitRequestDto(longPath, "example.com")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNull(result) // Should be valid
    }

    @Test
    fun `should validate long but acceptable domain`() {
        // Create a domain with multiple segments that's under 253 chars but respects 61-char segment limit
        val longDomain = "a".repeat(61) + "." + "b".repeat(61) + "." + "c".repeat(61) + ".co"
        val request = PageHitRequestDto("/test", longDomain)

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNull(result) // Should be valid
    }

    @Test
    fun `should collect all validation errors for completely invalid request`() {
        val longPath = "/" + "a".repeat(2050) // Over 2048 characters
        val longDomain = "a".repeat(260) + ".com" // Over 253 characters
        val request = PageHitRequestDto(longPath, longDomain)

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNotNull(result)
        assertEquals(3, result!!.size) // Path too long + Domain too long + Invalid domain format
        assertTrue(result.contains("Path too long (max 2048 characters)"))
        assertTrue(result.contains("Domain too long (max 253 characters)"))
        assertTrue(result.contains("Invalid domain format"))
    }

    @Test
    fun `should handle empty path and invalid domain combination`() {
        val request = PageHitRequestDto("", "invalid..domain")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNotNull(result)
        assertEquals(2, result!!.size)
        assertTrue(result.contains("Path cannot be empty"))
        assertTrue(result.contains("Invalid domain format"))
    }

    @Test
    fun `should validate TLD-only domain`() {
        val request = PageHitRequestDto("/test", "localhost")

        val result = visitRequestMapper.validatePageHitRequest(request).await().indefinitely()

        assertNull(result) // Single-part domains should be valid (localhost, etc.)
    }
}
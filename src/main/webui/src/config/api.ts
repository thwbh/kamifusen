/**
 * API Configuration
 * Determines the correct base URL for API calls based on environment
 */

export class ApiConfig {
  private static baseUrl: string | null = null;

  /**
   * Get the API base URL
   * In production: uses the same origin as the web app (since Quinoa serves both)
   * In development: uses localhost:8080 (Quarkus dev server)
   */
  static getBaseUrl(): string {
    if (this.baseUrl === null) {
      // Check if we're in development mode
      const isDev = import.meta.env.DEV || import.meta.env.MODE === 'development';

      if (isDev) {
        // Development: Quarkus dev server
        this.baseUrl = 'http://localhost:8080';
      } else {
        // Production: same origin (Quinoa serves both frontend and backend)
        this.baseUrl = window.location.origin;
      }
    }
    return this.baseUrl;
  }

  /**
   * Override the base URL (useful for testing or custom deployments)
   */
  static setBaseUrl(url: string): void {
    this.baseUrl = url;
  }

  /**
   * Reset to auto-detection
   */
  static reset(): void {
    this.baseUrl = null;
  }
}
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
      // If VITE_APP_VERSION exists, it means we're in a production build
      // (the .env file is only generated during build time)
      const isProduction = !!import.meta.env.VITE_APP_VERSION;

      if (isProduction) {
        // Production: same origin (Quinoa serves both frontend and backend)
        this.baseUrl = window.location.origin;
      } else {
        // Development: Quarkus dev server
        this.baseUrl = 'http://localhost:8080';
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
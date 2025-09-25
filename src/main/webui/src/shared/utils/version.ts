/**
 * Application Version Utilities
 */

/**
 * Get the application version from environment variables
 * In development: shows "dev"
 * In production: shows the actual version from build process
 */
export const getAppVersion = (): string => {
  // Vite exposes environment variables prefixed with VITE_
  const version = import.meta.env.VITE_APP_VERSION;

  if (version) {
    return version;
  }

  // Fallback for development
  if (import.meta.env.DEV) {
    return 'dev';
  }

  return 'unknown';
}

/**
 * Get build information object
 */
export const getBuildInfo = () => {
  return {
    version: getAppVersion(),
    mode: import.meta.env.MODE,
    isDevelopment: import.meta.env.DEV,
    isProduction: import.meta.env.PROD,
    buildTime: new Date().toISOString() // Could be set at build time if needed
  };
}

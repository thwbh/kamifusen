import { Configuration } from '../api/configuration';
import { AppAdminResourceApi, PageVisitResourceApi } from '../api';
import { ApiConfig } from './api';

/**
 * API Client Factory
 * Creates properly configured API clients with correct base URL
 */

// Create the configuration with dynamic base URL
function createConfiguration(): Configuration {
  return new Configuration({
    basePath: ApiConfig.getBaseUrl(),
    // Add other configuration options here if needed
  });
}

// Create pre-configured API clients
export const appAdminApi = new AppAdminResourceApi(createConfiguration());
export const pageVisitApi = new PageVisitResourceApi(createConfiguration());

// Export configuration factory for custom clients
export { createConfiguration };

// Re-export the ApiConfig for external use
export { ApiConfig };
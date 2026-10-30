export interface RuntimeEnv {
  apiUrl?: string;
}

declare global {
  interface Window {
    __env?: RuntimeEnv;
  }
}

const DEFAULT_API_URL = 'http://localhost:8080';

/**
 * Resolves the backend URL from the runtime environment.
 *
 * Deployment environments can override it by replacing `env.js` (generated from the `API_URL`
 * environment variable) before the app is served. Falls back to the local development backend.
 */
function resolveApiUrl(): string {
  const configured = typeof window === 'undefined' ? undefined : window.__env?.apiUrl;
  const apiUrl = configured?.trim() || DEFAULT_API_URL;
  return apiUrl.replace(/\/+$/, '');
}

export const API_BASE_URL = resolveApiUrl();
export const API_URL = `${API_BASE_URL}/api`;

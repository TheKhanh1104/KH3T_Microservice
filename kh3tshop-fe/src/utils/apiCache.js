/**
 * apiCache.js – Cache API responses in sessionStorage.
 * Data is kept for TTL_MS milliseconds (default 5 minutes).
 * Using sessionStorage means cache is cleared when the browser tab is closed.
 */

const TTL_MS = 5 * 60 * 1000; // 5 minutes

/**
 * Fetch with cache.
 * @param {string} url - API endpoint
 * @param {string} cacheKey - Key used to store in sessionStorage
 * @returns {Promise<any>} - parsed JSON result
 */
export async function fetchWithCache(url, cacheKey) {
  try {
    const cached = sessionStorage.getItem(cacheKey);
    if (cached) {
      const { data, timestamp } = JSON.parse(cached);
      const isValid = Date.now() - timestamp < TTL_MS;
      if (isValid) {
        return data;
      }
    }
  } catch {
    // If sessionStorage parsing fails, ignore and fetch fresh
  }

  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  const json = await response.json();
  const result = Array.isArray(json) ? json : json.result || json;

  try {
    sessionStorage.setItem(
      cacheKey,
      JSON.stringify({ data: result, timestamp: Date.now() })
    );
  } catch {
    // sessionStorage quota exceeded – just skip caching
  }

  return result;
}

/**
 * Manually clear a specific cache key.
 * Use this after adding/editing products in admin panel.
 */
export function clearCache(cacheKey) {
  sessionStorage.removeItem(cacheKey);
}

/**
 * Clear all cached API data.
 */
export function clearAllCache() {
  const keysToRemove = [];
  for (let i = 0; i < sessionStorage.length; i++) {
    const key = sessionStorage.key(i);
    if (key && key.startsWith("api_cache_")) {
      keysToRemove.push(key);
    }
  }
  keysToRemove.forEach((k) => sessionStorage.removeItem(k));
}

// Cache key constants – dùng chung để tránh nhầm
export const CACHE_KEYS = {
  PRODUCTS: "api_cache_products",
  CATEGORIES: "api_cache_categories",
};

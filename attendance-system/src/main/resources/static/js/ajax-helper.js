// AJAX Helper Functions
// Handles all HTTP requests with error handling and CSRF token support

// Get CSRF token from meta tag or cookie
function getCsrfToken() {
    // Try to get from meta tag first
    const metaTag = document.querySelector('meta[name="_csrf"]');
    if (metaTag) {
        return metaTag.getAttribute('content');
    }
    
    // Try to get from cookie
    const cookieValue = document.cookie
        .split('; ')
        .find(row => row.startsWith('XSRF-TOKEN='));
    
    return cookieValue ? cookieValue.split('=')[1] : null;
}

// Get CSRF header name
function getCsrfHeader() {
    const metaTag = document.querySelector('meta[name="_csrf_header"]');
    return metaTag ? metaTag.getAttribute('content') : 'X-CSRF-TOKEN';
}

// ============================================
// Base AJAX Function
// ============================================
async function ajax(url, options = {}) {
    const requestId = `${options.method || 'GET'}-${Date.now()}`;
    
    // Log request start
    if (window.logger) {
        logger.apiRequest(options.method || 'GET', url, options.body || options.params);
        logger.time(requestId);
    }
    
    const defaultOptions = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'same-origin'
    };
    
    // Merge options
    const config = { ...defaultOptions, ...options };
    
    // Add CSRF token for non-GET requests
    if (config.method !== 'GET') {
        const csrfToken = getCsrfToken();
        if (csrfToken) {
            config.headers[getCsrfHeader()] = csrfToken;
            if (window.logger) logger.debug('CSRF', 'Token added to request', { token: csrfToken.substring(0, 10) + '...' });
        }
    }
    
    // Convert body to JSON if it's an object
    if (config.body && typeof config.body === 'object' && !(config.body instanceof FormData)) {
        config.body = JSON.stringify(config.body);
        if (window.logger) logger.debug('AJAX', 'Request body stringified', config.body);
    }
    
    try {
        if (window.logger) logger.info('AJAX', `Fetching: ${url}`, config);
        
        const response = await fetch(url, config);
        
        // Handle different response types
        const contentType = response.headers.get('content-type');
        let data;
        
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
            if (window.logger) logger.debug('AJAX', 'Response parsed as JSON', data);
        } else {
            data = await response.text();
            if (window.logger) logger.debug('AJAX', 'Response parsed as text', data);
        }
        
        // Check if response is OK
        if (!response.ok) {
            if (window.logger) {
                logger.error('AJAX', `HTTP ${response.status} ${response.statusText}`, {
                    url,
                    status: response.status,
                    statusText: response.statusText,
                    data
                });
            }
            
            throw {
                status: response.status,
                statusText: response.statusText,
                data: data
            };
        }
        
        const result = {
            success: true,
            data: data,
            status: response.status
        };
        
        if (window.logger) {
            logger.apiResponse(options.method || 'GET', url, result);
            logger.timeEnd(requestId);
        }
        
        return result;
        
    } catch (error) {
        console.error('AJAX Error:', error);
        
        if (window.logger) {
            logger.error('AJAX', 'Request failed', {
                url,
                error: error.message || error,
                stack: error.stack
            });
            logger.timeEnd(requestId);
        }
        
        return {
            success: false,
            error: error,
            message: error.data?.message || error.statusText || 'An error occurred'
        };
    }
}

// ============================================
// HTTP Method Wrappers
// ============================================

// GET request
async function get(url, params = {}) {
    if (window.logger) logger.functionStart('ajax.get', { url, params });
    
    // Add query parameters to URL
    if (Object.keys(params).length > 0) {
        const queryString = new URLSearchParams(params).toString();
        url = `${url}?${queryString}`;
        if (window.logger) logger.debug('AJAX', 'Query string built', { original: url.split('?')[0], full: url });
    }
    
    const result = await ajax(url, { method: 'GET' });
    
    if (window.logger) logger.functionEnd('ajax.get', result);
    return result;
}

// POST request
async function post(url, data = {}) {
    if (window.logger) logger.functionStart('ajax.post', { url, data });
    
    const result = await ajax(url, {
        method: 'POST',
        body: data
    });
    
    if (window.logger) logger.functionEnd('ajax.post', result);
    return result;
}

// PUT request
async function put(url, data = {}) {
    return await ajax(url, {
        method: 'PUT',
        body: data
    });
}

// DELETE request
async function del(url) {
    return await ajax(url, { method: 'DELETE' });
}

// ============================================
// Form Submission Helper
// ============================================
async function submitForm(formElement, url, method = 'POST') {
    const formData = new FormData(formElement);
    const data = Object.fromEntries(formData.entries());
    
    if (method === 'POST') {
        return await post(url, data);
    } else if (method === 'PUT') {
        return await put(url, data);
    }
    
    return await ajax(url, { method, body: data });
}

// ============================================
// File Upload Helper
// ============================================
async function uploadFile(url, fileInput, additionalData = {}) {
    const formData = new FormData();
    
    // Add file
    if (fileInput.files && fileInput.files[0]) {
        formData.append('file', fileInput.files[0]);
    }
    
    // Add additional data
    for (const [key, value] of Object.entries(additionalData)) {
        formData.append(key, value);
    }
    
    const csrfToken = getCsrfToken();
    const headers = {};
    if (csrfToken) {
        headers[getCsrfHeader()] = csrfToken;
    }
    
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: headers,
            body: formData,
            credentials: 'same-origin'
        });
        
        const data = await response.json();
        
        if (!response.ok) {
            throw {
                status: response.status,
                statusText: response.statusText,
                data: data
            };
        }
        
        return {
            success: true,
            data: data,
            status: response.status
        };
        
    } catch (error) {
        return {
            success: false,
            error: error,
            message: error.data?.message || error.statusText || 'File upload failed'
        };
    }
}

// ============================================
// Mock Data Helper (for development/testing)
// ============================================
function mockResponse(data, delay = 500) {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                success: true,
                data: data
            });
        }, delay);
    });
}

// Export functions to global scope
window.ajax = {
    get,
    post,
    put,
    delete: del,
    ajax,
    submitForm,
    uploadFile,
    getCsrfToken,
    mockResponse
};

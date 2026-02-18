// AJAX Helper Functions
// Handles all HTTP requests with error handling and CSRF token support
// Automatically unwraps ApiResponse format from backend

// Get CSRF token from meta tag or cookie
function getCsrfToken() {
    const metaTag = document.querySelector('meta[name="_csrf"]');
    if (metaTag) {
        return metaTag.getAttribute('content');
    }
    
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

/**
 * Unwrap ApiResponse format from backend.
 * Backend returns: { success: bool, message: string, data: T, timestamp: string, error: string, errorCode: int }
 * This function extracts the actual data and normalizes the response.
 */
function unwrapApiResponse(response) {
    // If response has ApiResponse structure, unwrap it
    if (response && typeof response === 'object' && 'success' in response) {
        return {
            success: response.success,
            data: response.data,
            message: response.message || (response.success ? 'Success' : 'Error'),
            error: response.error,
            errorCode: response.errorCode,
            timestamp: response.timestamp
        };
    }
    // If not ApiResponse format, return as-is wrapped in success
    return {
        success: true,
        data: response,
        message: 'Success'
    };
}

// ============================================
// Base AJAX Function
// ============================================
async function doRequest(url, options = {}) {
    const requestId = `${options.method || 'GET'}-${Date.now()}`;
    
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
    
    const config = { ...defaultOptions, ...options };
    
    // Add CSRF token for non-GET requests
    if (config.method !== 'GET') {
        const csrfToken = getCsrfToken();
        if (csrfToken) {
            config.headers[getCsrfHeader()] = csrfToken;
        }
    }
    
    // Convert body to JSON if it's an object
    if (config.body && typeof config.body === 'object' && !(config.body instanceof FormData)) {
        config.body = JSON.stringify(config.body);
    }
    
    try {
        const response = await fetch(url, config);
        const contentType = response.headers.get('content-type');
        let rawData;
        
        if (contentType && contentType.includes('application/json')) {
            rawData = await response.json();
        } else {
            rawData = await response.text();
        }
        
        // Unwrap ApiResponse format
        const result = unwrapApiResponse(rawData);
        result.httpStatus = response.status;
        
        // Handle HTTP errors
        if (!response.ok) {
            if (window.logger) {
                logger.error('AJAX', `HTTP ${response.status}`, { url, data: result });
            }
            result.success = false;
            if (!result.message || result.message === 'Success') {
                result.message = result.error || response.statusText || 'Request failed';
            }
        }
        
        if (window.logger) {
            logger.apiResponse(options.method || 'GET', url, result);
            logger.timeEnd(requestId);
        }
        
        return result;
        
    } catch (error) {
        console.error('AJAX Error:', error);
        
        if (window.logger) {
            logger.error('AJAX', 'Request failed', { url, error: error.message });
            logger.timeEnd(requestId);
        }
        
        return {
            success: false,
            data: null,
            message: error.message || 'Network error occurred',
            error: error
        };
    }
}

// ============================================
// HTTP Method Wrappers
// ============================================

async function ajaxGet(url, params = {}) {
    if (Object.keys(params).length > 0) {
        const queryString = new URLSearchParams(params).toString();
        url = `${url}?${queryString}`;
    }
    return await doRequest(url, { method: 'GET' });
}

async function ajaxPost(url, data = {}) {
    return await doRequest(url, { method: 'POST', body: data });
}

async function ajaxPut(url, data = {}) {
    return await doRequest(url, { method: 'PUT', body: data });
}

async function ajaxDelete(url) {
    return await doRequest(url, { method: 'DELETE' });
}

// ============================================
// Form Submission Helper
// ============================================
async function submitForm(formElement, url, method = 'POST') {
    const formData = new FormData(formElement);
    const data = Object.fromEntries(formData.entries());
    
    if (method === 'POST') {
        return await ajaxPost(url, data);
    } else if (method === 'PUT') {
        return await ajaxPut(url, data);
    }
    return await doRequest(url, { method, body: data });
}

// ============================================
// File Upload Helper
// ============================================
async function uploadFile(url, fileInput, additionalData = {}) {
    const formData = new FormData();
    
    if (fileInput.files && fileInput.files[0]) {
        formData.append('file', fileInput.files[0]);
    }
    
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
        
        const rawData = await response.json();
        const result = unwrapApiResponse(rawData);
        result.httpStatus = response.status;
        
        if (!response.ok) {
            result.success = false;
        }
        
        return result;
        
    } catch (error) {
        return {
            success: false,
            data: null,
            message: error.message || 'File upload failed',
            error: error
        };
    }
}

// Export functions to global scope
window.ajax = {
    get: ajaxGet,
    post: ajaxPost,
    put: ajaxPut,
    delete: ajaxDelete,
    request: doRequest,
    submitForm,
    uploadFile,
    getCsrfToken,
    unwrapApiResponse
};

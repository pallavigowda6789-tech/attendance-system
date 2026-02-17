// Enhanced Logger for Debugging
// Provides detailed logging with timestamps and categories

const Logger = {
    enabled: true, // Set to false to disable all logging
    
    // Log levels
    levels: {
        INFO: { color: '#2196F3', emoji: 'ℹ️' },
        SUCCESS: { color: '#4CAF50', emoji: '✅' },
        WARNING: { color: '#FF9800', emoji: '⚠️' },
        ERROR: { color: '#F44336', emoji: '❌' },
        DEBUG: { color: '#9C27B0', emoji: '🔍' },
        API: { color: '#00BCD4', emoji: '🌐' }
    },
    
    // Get timestamp
    getTimestamp() {
        const now = new Date();
        return now.toLocaleTimeString('en-US', { 
            hour12: false, 
            hour: '2-digit', 
            minute: '2-digit', 
            second: '2-digit',
            fractionalSecondDigits: 3
        });
    },
    
    // Format message
    format(level, category, message, data) {
        const timestamp = this.getTimestamp();
        const levelInfo = this.levels[level] || this.levels.INFO;
        
        return {
            timestamp,
            level,
            category,
            message,
            data,
            levelInfo
        };
    },
    
    // Main log method
    log(level, category, message, data = null) {
        if (!this.enabled) return;
        
        const logData = this.format(level, category, message, data);
        const { timestamp, levelInfo } = logData;
        
        const style = `color: ${levelInfo.color}; font-weight: bold;`;
        const resetStyle = 'color: inherit; font-weight: normal;';
        
        console.groupCollapsed(
            `%c${levelInfo.emoji} [${timestamp}] [${level}] [${category}]%c ${message}`,
            style,
            resetStyle
        );
        
        if (data !== null && data !== undefined) {
            console.log('Data:', data);
        }
        
        console.trace('Stack trace');
        console.groupEnd();
    },
    
    // Convenience methods
    info(category, message, data) {
        this.log('INFO', category, message, data);
    },
    
    success(category, message, data) {
        this.log('SUCCESS', category, message, data);
    },
    
    warning(category, message, data) {
        this.log('WARNING', category, message, data);
    },
    
    error(category, message, data) {
        this.log('ERROR', category, message, data);
    },
    
    debug(category, message, data) {
        this.log('DEBUG', category, message, data);
    },
    
    api(category, message, data) {
        this.log('API', category, message, data);
    },
    
    // API call logger
    apiRequest(method, url, params) {
        this.api('REQUEST', `${method} ${url}`, { params });
    },
    
    apiResponse(method, url, response) {
        if (response.success) {
            this.success('RESPONSE', `${method} ${url}`, response);
        } else {
            this.error('RESPONSE', `${method} ${url} - Failed`, response);
        }
    },
    
    // Function execution logger
    functionStart(functionName, args) {
        this.debug('FUNCTION', `${functionName} - Started`, { arguments: args });
    },
    
    functionEnd(functionName, result) {
        this.debug('FUNCTION', `${functionName} - Completed`, { result });
    },
    
    // Data transformation logger
    dataTransform(operation, input, output) {
        this.debug('DATA', `${operation}`, { input, output });
    },
    
    // UI event logger
    uiEvent(event, element, data) {
        this.info('UI', `${event} on ${element}`, data);
    },
    
    // Clear console
    clear() {
        console.clear();
        this.info('SYSTEM', 'Console cleared');
    },
    
    // Enable/disable logging
    enable() {
        this.enabled = true;
        this.success('SYSTEM', 'Logging enabled');
    },
    
    disable() {
        this.enabled = false;
        console.log('🔇 Logging disabled');
    },
    
    // Performance measurement
    time(label) {
        console.time(label);
        this.info('PERF', `Timer started: ${label}`);
    },
    
    timeEnd(label) {
        console.timeEnd(label);
        this.info('PERF', `Timer ended: ${label}`);
    }
};

// Export to global scope
window.logger = Logger;

// Initial log
logger.success('SYSTEM', 'Logger initialized successfully');

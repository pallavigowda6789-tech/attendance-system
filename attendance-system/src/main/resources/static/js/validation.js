// Form Validation Library

// Email validation
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// Username validation
function isValidUsername(username) {
    // 3-20 characters, letters, numbers, underscore only
    const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/;
    return usernameRegex.test(username);
}

// Password strength checker
function checkPasswordStrength(password) {
    let strength = {
        score: 0,
        label: 'Very Weak',
        class: 'weak',
        suggestions: []
    };
    
    if (password.length === 0) {
        return { score: 0, label: '-', class: '', suggestions: [] };
    }
    
    // Length
    if (password.length >= 8) {
        strength.score += 25;
    } else {
        strength.suggestions.push('Use at least 8 characters');
    }
    
    if (password.length >= 12) {
        strength.score += 15;
    }
    
    // Lowercase
    if (/[a-z]/.test(password)) {
        strength.score += 15;
    } else {
        strength.suggestions.push('Add lowercase letters');
    }
    
    // Uppercase
    if (/[A-Z]/.test(password)) {
        strength.score += 15;
    } else {
        strength.suggestions.push('Add uppercase letters');
    }
    
    // Numbers
    if (/[0-9]/.test(password)) {
        strength.score += 15;
    } else {
        strength.suggestions.push('Add numbers');
    }
    
    // Special characters
    if (/[^a-zA-Z0-9]/.test(password)) {
        strength.score += 15;
    } else {
        strength.suggestions.push('Add special characters');
    }
    
    // Determine label and class
    if (strength.score < 40) {
        strength.label = 'Weak';
        strength.class = 'weak';
    } else if (strength.score < 70) {
        strength.label = 'Medium';
        strength.class = 'medium';
    } else {
        strength.label = 'Strong';
        strength.class = 'strong';
    }
    
    return strength;
}

// Required field validation
function validateRequired(value, fieldName = 'This field') {
    if (!value || value.trim() === '') {
        return { valid: false, message: `${fieldName} is required` };
    }
    return { valid: true, message: '' };
}

// Min length validation
function validateMinLength(value, minLength, fieldName = 'This field') {
    if (value.length < minLength) {
        return { 
            valid: false, 
            message: `${fieldName} must be at least ${minLength} characters` 
        };
    }
    return { valid: true, message: '' };
}

// Max length validation
function validateMaxLength(value, maxLength, fieldName = 'This field') {
    if (value.length > maxLength) {
        return { 
            valid: false, 
            message: `${fieldName} must be no more than ${maxLength} characters` 
        };
    }
    return { valid: true, message: '' };
}

// Match validation (for password confirmation)
function validateMatch(value1, value2, fieldName = 'Values') {
    if (value1 !== value2) {
        return { valid: false, message: `${fieldName} do not match` };
    }
    return { valid: true, message: '' };
}

// Phone number validation
function validatePhone(phone) {
    const phoneRegex = /^[\d\s\-\+\(\)]+$/;
    if (!phoneRegex.test(phone)) {
        return { valid: false, message: 'Please enter a valid phone number' };
    }
    return { valid: true, message: '' };
}

// URL validation
function validateURL(url) {
    try {
        new URL(url);
        return { valid: true, message: '' };
    } catch (e) {
        return { valid: false, message: 'Please enter a valid URL' };
    }
}

// Show validation error on form field
function showFieldError(fieldId, message) {
    const field = document.getElementById(fieldId);
    const errorElement = document.getElementById(`${fieldId}-error`);
    
    if (field) {
        field.classList.add('error');
        field.classList.remove('success');
    }
    
    if (errorElement) {
        errorElement.textContent = message;
    }
}

// Show validation success on form field
function showFieldSuccess(fieldId) {
    const field = document.getElementById(fieldId);
    const errorElement = document.getElementById(`${fieldId}-error`);
    
    if (field) {
        field.classList.remove('error');
        field.classList.add('success');
    }
    
    if (errorElement) {
        errorElement.textContent = '';
    }
}

// Clear validation state
function clearFieldValidation(fieldId) {
    const field = document.getElementById(fieldId);
    const errorElement = document.getElementById(`${fieldId}-error`);
    
    if (field) {
        field.classList.remove('error', 'success');
    }
    
    if (errorElement) {
        errorElement.textContent = '';
    }
}

// Validate form - returns true if valid, false otherwise
function validateForm(formId, validationRules) {
    let isValid = true;
    
    for (const [fieldId, rules] of Object.entries(validationRules)) {
        const field = document.getElementById(fieldId);
        if (!field) continue;
        
        const value = field.value.trim();
        
        // Run each validation rule
        for (const rule of rules) {
            const result = rule(value);
            if (!result.valid) {
                showFieldError(fieldId, result.message);
                isValid = false;
                break; // Stop at first error for this field
            } else {
                showFieldSuccess(fieldId);
            }
        }
    }
    
    return isValid;
}

// Real-time validation setup
function setupRealtimeValidation(fieldId, validationFn, debounceMs = 500) {
    const field = document.getElementById(fieldId);
    if (!field) return;
    
    let timeout;
    field.addEventListener('input', function() {
        clearTimeout(timeout);
        timeout = setTimeout(() => {
            const result = validationFn(field.value.trim());
            if (result.valid) {
                showFieldSuccess(fieldId);
            } else if (field.value.trim() !== '') {
                showFieldError(fieldId, result.message);
            } else {
                clearFieldValidation(fieldId);
            }
        }, debounceMs);
    });
}

// Export to global scope
window.validation = {
    isValidEmail,
    isValidUsername,
    checkPasswordStrength,
    validateRequired,
    validateMinLength,
    validateMaxLength,
    validateMatch,
    validatePhone,
    validateURL,
    showFieldError,
    showFieldSuccess,
    clearFieldValidation,
    validateForm,
    setupRealtimeValidation
};

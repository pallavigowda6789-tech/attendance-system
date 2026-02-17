// Main JavaScript - Core functionality

// Wait for DOM to be ready
document.addEventListener('DOMContentLoaded', function() {
    initializeNavbar();
    initializeDropdowns();
    initializeModals();
});

// ============================================
// Navbar Functionality
// ============================================
function initializeNavbar() {
    const mobileToggle = document.getElementById('mobile-toggle');
    const navbarNav = document.getElementById('navbar-nav');
    
    if (mobileToggle && navbarNav) {
        mobileToggle.addEventListener('click', function() {
            this.classList.toggle('active');
            navbarNav.classList.toggle('show');
        });
        
        // Close mobile menu when clicking outside
        document.addEventListener('click', function(event) {
            if (!event.target.closest('.navbar-container')) {
                mobileToggle.classList.remove('active');
                navbarNav.classList.remove('show');
            }
        });
        
        // Close mobile menu when window is resized to desktop
        window.addEventListener('resize', function() {
            if (window.innerWidth > 768) {
                mobileToggle.classList.remove('active');
                navbarNav.classList.remove('show');
            }
        });
    }
}

// ============================================
// Dropdown Functionality
// ============================================
function initializeDropdowns() {
    const userDropdownBtn = document.getElementById('user-dropdown-btn');
    const userDropdownMenu = document.getElementById('user-dropdown-menu');
    
    if (userDropdownBtn && userDropdownMenu) {
        userDropdownBtn.addEventListener('click', function(event) {
            event.stopPropagation();
            this.classList.toggle('active');
            userDropdownMenu.classList.toggle('show');
        });
        
        // Close dropdown when clicking outside
        document.addEventListener('click', function(event) {
            if (!event.target.closest('.navbar-user')) {
                userDropdownBtn.classList.remove('active');
                userDropdownMenu.classList.remove('show');
            }
        });
    }
}

// ============================================
// Modal Functionality
// ============================================
function initializeModals() {
    // Close modals when clicking on backdrop
    document.addEventListener('click', function(event) {
        if (event.target.classList.contains('modal-backdrop')) {
            closeAllModals();
        }
    });
    
    // Close modals with Escape key
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeAllModals();
        }
    });
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }
}

function closeAllModals() {
    const modals = document.querySelectorAll('.modal-backdrop');
    modals.forEach(modal => {
        modal.style.display = 'none';
    });
    document.body.style.overflow = '';
}

// ============================================
// Toast Notification System
// ============================================
function showToast(message, type = 'info', duration = 3000) {
    const toastContainer = document.getElementById('toast-container');
    if (!toastContainer) return;
    
    const toast = document.createElement('div');
    toast.className = `toast alert alert-${type}`;
    toast.textContent = message;
    
    toastContainer.appendChild(toast);
    
    // Auto remove after duration
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, duration);
}

// Convenience methods
function showSuccess(message) {
    showToast(message, 'success');
}

function showError(message) {
    showToast(message, 'error');
}

function showWarning(message) {
    showToast(message, 'warning');
}

function showInfo(message) {
    showToast(message, 'info');
}

// ============================================
// Loading Overlay
// ============================================
function showLoading() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.style.display = 'flex';
        overlay.classList.remove('hidden');
    }
}

function hideLoading() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.style.display = 'none';
        overlay.classList.add('hidden');
    }
}

// ============================================
// Confirmation Dialog
// ============================================
function confirm(message, onConfirm, onCancel) {
    const confirmed = window.confirm(message);
    if (confirmed && typeof onConfirm === 'function') {
        onConfirm();
    } else if (!confirmed && typeof onCancel === 'function') {
        onCancel();
    }
    return confirmed;
}

// ============================================
// Date Formatting Utilities
// ============================================
function formatDate(dateString, format = 'default') {
    const date = new Date(dateString);
    
    const options = {
        'default': { year: 'numeric', month: 'short', day: 'numeric' },
        'long': { year: 'numeric', month: 'long', day: 'numeric' },
        'short': { year: '2-digit', month: '2-digit', day: '2-digit' },
        'time': { hour: '2-digit', minute: '2-digit' }
    };
    
    return date.toLocaleDateString('en-US', options[format] || options.default);
}

function formatDateTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// ============================================
// Utility Functions
// ============================================

// Debounce function for search inputs
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Format numbers with commas
function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

// Calculate percentage
function calculatePercentage(part, total) {
    if (total === 0) return 0;
    return Math.round((part / total) * 100);
}

// Truncate text
function truncateText(text, maxLength) {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

// Export functions to global scope
window.app = {
    showToast,
    showSuccess,
    showError,
    showWarning,
    showInfo,
    showLoading,
    hideLoading,
    confirm,
    openModal,
    closeModal,
    closeAllModals,
    formatDate,
    formatDateTime,
    debounce,
    formatNumber,
    calculatePercentage,
    truncateText
};

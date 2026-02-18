// Profile Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeProfilePage();
});

function initializeProfilePage() {
    loadProfileData();
    setupEditMode();
    setupPasswordChange();
}

// ============================================
// Load Profile Data
// ============================================
async function loadProfileData() {
    try {
        app.showLoading();
        
        const response = await ajax.get('/api/users/profile');
        
        if (response.success && response.data) {
            // response.data is now the unwrapped UserDTO
            displayProfileData(response.data);
            loadAttendanceStats();
        } else {
            app.showError(response.message || 'Failed to load profile');
        }
    } catch (error) {
        console.error('Error loading profile:', error);
        app.showError('Failed to load profile');
    } finally {
        app.hideLoading();
    }
}

async function loadAttendanceStats() {
    try {
        const response = await ajax.get('/api/attendance/my-stats');
        if (response.success && response.data) {
            // response.data is now the unwrapped AttendanceStatsDTO
            const stats = response.data;
            const totalEl = document.getElementById('total-attendance');
            const presentEl = document.getElementById('present-days');
            const rateEl = document.getElementById('attendance-rate');
            
            if (totalEl) totalEl.textContent = stats.totalDays || 0;
            if (presentEl) presentEl.textContent = stats.presentDays || 0;
            if (rateEl) rateEl.textContent = (stats.attendancePercentage || 0).toFixed(1) + '%';
        }
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

function displayProfileData(profile) {
    // Header
    const avatar = document.getElementById('profile-avatar');
    if (avatar && profile.firstName) {
        avatar.textContent = profile.firstName.charAt(0).toUpperCase();
    }
    
    const fullName = profile.fullName || `${profile.firstName || ''} ${profile.lastName || ''}`.trim();
    const nameEl = document.getElementById('profile-name');
    const emailEl = document.getElementById('profile-email');
    const authEl = document.getElementById('auth-method');
    
    if (nameEl) nameEl.textContent = fullName || profile.username;
    if (emailEl) emailEl.textContent = profile.email || '';
    if (authEl) {
        authEl.textContent = profile.provider === 'LOCAL' ? 'Local Account' : 
            profile.provider === 'GOOGLE' ? 'Google Account' :
            profile.provider === 'GITHUB' ? 'GitHub Account' : 'SSO Account';
    }
    
    // View mode fields
    const viewFields = {
        'view-firstName': profile.firstName || '-',
        'view-lastName': profile.lastName || '-',
        'view-username': profile.username || '-',
        'view-email': profile.email || '-',
        'view-role': profile.role || '-',
        'view-createdAt': profile.createdAt ? app.formatDate(profile.createdAt) : '-'
    };
    
    for (const [id, value] of Object.entries(viewFields)) {
        const el = document.getElementById(id);
        if (el) el.textContent = value;
    }
    
    // Store current data for edit mode
    window.currentProfile = profile;
    
    // Hide password change for OAuth users
    if (profile.provider !== 'LOCAL') {
        const passwordSection = document.getElementById('password-change-section');
        if (passwordSection) passwordSection.style.display = 'none';
    }
}

// ============================================
// Edit Mode
// ============================================
function setupEditMode() {
    document.getElementById('edit-profile-btn')?.addEventListener('click', enterEditMode);
    document.getElementById('cancel-edit-btn')?.addEventListener('click', exitEditMode);
    document.getElementById('edit-profile-form')?.addEventListener('submit', async function(e) {
        e.preventDefault();
        await saveProfile();
    });
}

function enterEditMode() {
    document.getElementById('profile-view-mode')?.classList.add('hidden');
    document.getElementById('profile-edit-mode')?.classList.add('active');
    
    if (window.currentProfile) {
        const firstNameEl = document.getElementById('firstName');
        const lastNameEl = document.getElementById('lastName');
        const emailEl = document.getElementById('email');
        
        if (firstNameEl) firstNameEl.value = window.currentProfile.firstName || '';
        if (lastNameEl) lastNameEl.value = window.currentProfile.lastName || '';
        if (emailEl) emailEl.value = window.currentProfile.email || '';
    }
}

function exitEditMode() {
    document.getElementById('profile-view-mode')?.classList.remove('hidden');
    document.getElementById('profile-edit-mode')?.classList.remove('active');
}

async function saveProfile() {
    const formData = {
        id: window.currentProfile?.id,
        firstName: document.getElementById('firstName')?.value.trim(),
        lastName: document.getElementById('lastName')?.value.trim(),
        email: document.getElementById('email')?.value.trim()
    };
    
    try {
        app.showLoading();
        
        const response = await ajax.put('/api/users/profile', formData);
        
        if (response.success) {
            app.showSuccess(response.message || 'Profile updated successfully!');
            exitEditMode();
            loadProfileData();
        } else {
            app.showError(response.message || 'Failed to update profile');
        }
    } catch (error) {
        app.showError('Failed to update profile');
    } finally {
        app.hideLoading();
    }
}

// ============================================
// Password Change
// ============================================
function setupPasswordChange() {
    const form = document.getElementById('change-password-form');
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmNewPassword');
    
    newPasswordInput?.addEventListener('input', function() {
        validatePasswordRequirements(this.value);
    });
    
    confirmPasswordInput?.addEventListener('input', function() {
        const newPassword = newPasswordInput?.value || '';
        const confirmPassword = this.value;
        const errorEl = document.getElementById('confirm-password-error');
        
        if (confirmPassword && newPassword !== confirmPassword) {
            if (errorEl) errorEl.textContent = 'Passwords do not match';
            this.classList.add('error');
        } else {
            if (errorEl) errorEl.textContent = '';
            this.classList.remove('error');
        }
    });
    
    form?.addEventListener('submit', async function(e) {
        e.preventDefault();
        await changePassword();
    });
}

function validatePasswordRequirements(password) {
    const requirements = {
        'req-length': password.length >= 8,
        'req-uppercase': /[A-Z]/.test(password),
        'req-lowercase': /[a-z]/.test(password),
        'req-number': /[0-9]/.test(password),
        'req-special': /[^a-zA-Z0-9]/.test(password)
    };
    
    for (const [id, isValid] of Object.entries(requirements)) {
        const el = document.getElementById(id);
        if (el) {
            el.classList.toggle('valid', isValid);
        }
    }
}

async function changePassword() {
    const currentPassword = document.getElementById('currentPassword')?.value;
    const newPassword = document.getElementById('newPassword')?.value;
    const confirmPassword = document.getElementById('confirmNewPassword')?.value;
    
    if (newPassword !== confirmPassword) {
        app.showError('Passwords do not match');
        return;
    }
    
    if (!newPassword || newPassword.length < 8) {
        app.showError('Password must be at least 8 characters');
        return;
    }
    
    try {
        app.showLoading();
        
        const response = await ajax.post('/api/users/change-password', {
            currentPassword: currentPassword,
            newPassword: newPassword,
            confirmPassword: confirmPassword
        });
        
        if (response.success) {
            app.showSuccess(response.message || 'Password changed successfully!');
            document.getElementById('change-password-form')?.reset();
            document.querySelectorAll('#password-requirements-list li').forEach(el => {
                el.classList.remove('valid');
            });
        } else {
            app.showError(response.message || 'Failed to change password');
        }
    } catch (error) {
        app.showError('Failed to change password');
    } finally {
        app.hideLoading();
    }
}

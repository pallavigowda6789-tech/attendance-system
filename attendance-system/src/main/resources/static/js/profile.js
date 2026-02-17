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
        // TODO: Replace with actual API call
        // const response = await ajax.get('/api/users/profile');
        
        const mockProfile = {
            firstName: 'John',
            lastName: 'Doe',
            username: 'johndoe',
            email: 'john.doe@example.com',
            role: 'USER',
            authProvider: 'LOCAL',
            createdAt: '2024-01-15T10:00:00',
            stats: {
                totalDays: 45,
                presentDays: 38,
                attendanceRate: 84
            }
        };
        
        const response = await ajax.mockResponse(mockProfile);
        
        if (response.success) {
            displayProfileData(response.data);
        }
    } catch (error) {
        console.error('Error loading profile:', error);
    }
}

function displayProfileData(profile) {
    // Header
    document.getElementById('profile-avatar').textContent = profile.firstName.charAt(0).toUpperCase();
    document.getElementById('profile-name').textContent = `${profile.firstName} ${profile.lastName}`;
    document.getElementById('profile-email').textContent = profile.email;
    document.getElementById('auth-method').textContent = profile.authProvider === 'LOCAL' ? 'Local Account' : 'SSO Account';
    
    // View mode
    document.getElementById('view-firstName').textContent = profile.firstName;
    document.getElementById('view-lastName').textContent = profile.lastName;
    document.getElementById('view-username').textContent = profile.username;
    document.getElementById('view-email').textContent = profile.email;
    document.getElementById('view-role').textContent = profile.role;
    document.getElementById('view-createdAt').textContent = app.formatDate(profile.createdAt);
    
    // Stats
    document.getElementById('total-attendance').textContent = profile.stats.totalDays;
    document.getElementById('present-days').textContent = profile.stats.presentDays;
    document.getElementById('attendance-rate').textContent = profile.stats.attendanceRate + '%';
    
    // Store current data for edit mode
    window.currentProfile = profile;
}

// ============================================
// Edit Mode
// ============================================
function setupEditMode() {
    const editBtn = document.getElementById('edit-profile-btn');
    const cancelBtn = document.getElementById('cancel-edit-btn');
    const editForm = document.getElementById('edit-profile-form');
    
    editBtn?.addEventListener('click', function() {
        enterEditMode();
    });
    
    cancelBtn?.addEventListener('click', function() {
        exitEditMode();
    });
    
    editForm?.addEventListener('submit', async function(e) {
        e.preventDefault();
        await saveProfile();
    });
}

function enterEditMode() {
    // Hide view mode, show edit mode
    document.getElementById('profile-view-mode').classList.add('hidden');
    document.getElementById('profile-edit-mode').classList.add('active');
    
    // Populate form with current data
    if (window.currentProfile) {
        document.getElementById('firstName').value = window.currentProfile.firstName;
        document.getElementById('lastName').value = window.currentProfile.lastName;
        document.getElementById('email').value = window.currentProfile.email;
    }
}

function exitEditMode() {
    // Show view mode, hide edit mode
    document.getElementById('profile-view-mode').classList.remove('hidden');
    document.getElementById('profile-edit-mode').classList.remove('active');
}

async function saveProfile() {
    const formData = {
        firstName: document.getElementById('firstName').value.trim(),
        lastName: document.getElementById('lastName').value.trim(),
        email: document.getElementById('email').value.trim()
    };
    
    try {
        app.showLoading();
        
        // TODO: Replace with actual API call
        // const response = await ajax.put('/api/users/profile', formData);
        
        const response = await ajax.mockResponse({
            success: true,
            message: 'Profile updated successfully'
        });
        
        if (response.success) {
            app.showSuccess('Profile updated successfully!');
            exitEditMode();
            loadProfileData(); // Reload to show updated data
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
    
    // Real-time password validation
    newPasswordInput?.addEventListener('input', function() {
        validatePasswordRequirements(this.value);
    });
    
    // Confirm password match
    confirmPasswordInput?.addEventListener('input', function() {
        const newPassword = newPasswordInput.value;
        const confirmPassword = this.value;
        const errorEl = document.getElementById('confirm-password-error');
        
        if (confirmPassword && newPassword !== confirmPassword) {
            errorEl.textContent = 'Passwords do not match';
            this.classList.add('error');
        } else {
            errorEl.textContent = '';
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
        const element = document.getElementById(id);
        if (element) {
            if (isValid) {
                element.classList.add('valid');
            } else {
                element.classList.remove('valid');
            }
        }
    }
}

async function changePassword() {
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmNewPassword').value;
    
    // Validation
    if (newPassword !== confirmPassword) {
        app.showError('Passwords do not match');
        return;
    }
    
    if (newPassword.length < 8) {
        app.showError('Password must be at least 8 characters');
        return;
    }
    
    try {
        app.showLoading();
        
        // TODO: Replace with actual API call
        // const response = await ajax.post('/api/users/change-password', {
        //     currentPassword,
        //     newPassword
        // });
        
        const response = await ajax.mockResponse({
            success: true,
            message: 'Password changed successfully'
        });
        
        if (response.success) {
            app.showSuccess('Password changed successfully!');
            document.getElementById('change-password-form').reset();
            // Clear validation states
            document.querySelectorAll('#password-requirements-list li').forEach(el => {
                el.classList.remove('valid');
            });
        }
    } catch (error) {
        app.showError('Failed to change password');
    } finally {
        app.hideLoading();
    }
}

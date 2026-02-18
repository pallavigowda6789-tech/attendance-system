// Admin JavaScript - User Management

let currentPage = 1;
const usersPerPage = 10;
let searchQuery = '';

document.addEventListener('DOMContentLoaded', function() {
    initializeAdminPage();
});

function initializeAdminPage() {
    loadUsers();
    setupSearch();
    setupAddUserButton();
    setupUserForm();
    setupPagination();
}

// ============================================
// Load Users
// ============================================
async function loadUsers(page = 1, search = '') {
    try {
        app.showLoading();
        
        const response = await ajax.get('/api/admin/users');
        
        if (response.success && response.data) {
            // response.data is now the unwrapped List<UserDTO>
            const users = Array.isArray(response.data) ? response.data : [];
            
            // Client-side filtering
            let filteredUsers = users;
            if (search) {
                const searchLower = search.toLowerCase();
                filteredUsers = users.filter(u => 
                    u.username?.toLowerCase().includes(searchLower) ||
                    u.email?.toLowerCase().includes(searchLower) ||
                    u.firstName?.toLowerCase().includes(searchLower) ||
                    u.lastName?.toLowerCase().includes(searchLower)
                );
            }
            
            // Client-side pagination
            const startIndex = (page - 1) * usersPerPage;
            const endIndex = startIndex + usersPerPage;
            const pageUsers = filteredUsers.slice(startIndex, endIndex);
            const totalPages = Math.ceil(filteredUsers.length / usersPerPage) || 1;
            
            renderUsersTable(pageUsers);
            updatePagination(totalPages, page);
            currentPage = page;
        } else {
            app.showError(response.message || 'Failed to load users');
        }
    } catch (error) {
        console.error('Error loading users:', error);
        app.showError('Failed to load users');
    } finally {
        app.hideLoading();
    }
}

function renderUsersTable(users) {
    const tbody = document.getElementById('users-tbody');
    if (!tbody) return;
    
    if (!users || users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="no-data">No users found</td></tr>';
        return;
    }
    
    tbody.innerHTML = users.map(user => `
        <tr>
            <td>${user.id}</td>
            <td>${user.firstName || ''} ${user.lastName || ''}</td>
            <td>${user.email || ''}</td>
            <td><span class="badge role-badge badge-info">${user.role || 'USER'}</span></td>
            <td>
                <span class="user-status">
                    <span class="status-indicator ${user.enabled ? 'active' : 'inactive'}"></span>
                    ${user.enabled ? 'Active' : 'Inactive'}
                </span>
            </td>
            <td>${user.createdAt ? app.formatDate(user.createdAt) : '-'}</td>
            <td>
                <div class="table-actions">
                    <button class="action-btn" onclick="editUser(${user.id})" title="Edit">✏️</button>
                    <button class="action-btn" onclick="toggleUserStatus(${user.id})" title="${user.enabled ? 'Disable' : 'Enable'}">
                        ${user.enabled ? '🔒' : '🔓'}
                    </button>
                    <button class="action-btn delete" onclick="deleteUser(${user.id})" title="Delete">🗑️</button>
                </div>
            </td>
        </tr>
    `).join('');
}

// ============================================
// Search
// ============================================
function setupSearch() {
    const searchInput = document.getElementById('search-users');
    if (!searchInput) return;
    
    searchInput.addEventListener('input', app.debounce(function(e) {
        searchQuery = e.target.value.trim();
        loadUsers(1, searchQuery);
    }, 500));
}

// ============================================
// Add/Edit User Modal
// ============================================
function setupAddUserButton() {
    document.getElementById('add-user-btn')?.addEventListener('click', function() {
        openUserModal();
    });
}

function openUserModal(userId = null) {
    const modal = document.getElementById('user-modal');
    const title = document.getElementById('modal-title');
    const form = document.getElementById('user-form');
    
    if (form) form.reset();
    const userIdEl = document.getElementById('user-id');
    if (userIdEl) userIdEl.value = '';
    
    if (userId) {
        if (title) title.textContent = 'Edit User';
        loadUserData(userId);
    } else {
        if (title) title.textContent = 'Add User';
    }
    
    if (modal) modal.style.display = 'flex';
}

function closeUserModal() {
    const modal = document.getElementById('user-modal');
    if (modal) modal.style.display = 'none';
}

async function loadUserData(userId) {
    try {
        app.showLoading();
        
        const response = await ajax.get(`/api/admin/users/${userId}`);
        
        if (response.success && response.data) {
            const user = response.data;
            const fields = {
                'user-id': user.id,
                'user-firstName': user.firstName || '',
                'user-lastName': user.lastName || '',
                'user-email': user.email || '',
                'user-username': user.username || '',
                'user-role': user.role || 'USER'
            };
            
            for (const [id, value] of Object.entries(fields)) {
                const el = document.getElementById(id);
                if (el) el.value = value;
            }
            
            const enabledEl = document.getElementById('user-enabled');
            if (enabledEl) enabledEl.checked = user.enabled !== false;
        } else {
            app.showError(response.message || 'Failed to load user data');
        }
    } catch (error) {
        app.showError('Failed to load user data');
    } finally {
        app.hideLoading();
    }
}

// ============================================
// Save User (Role Update)
// ============================================
function setupUserForm() {
    document.getElementById('user-form')?.addEventListener('submit', async function(e) {
        e.preventDefault();
        await saveUser();
    });
}

async function saveUser() {
    const userId = document.getElementById('user-id')?.value;
    const role = document.getElementById('user-role')?.value;
    
    if (!userId) {
        app.showError('User creation via admin panel not supported. Users must register.');
        return;
    }
    
    try {
        app.showLoading();
        
        const response = await ajax.put(`/api/admin/users/${userId}/role`, { role: role });
        
        if (response.success) {
            app.showSuccess(response.message || 'User role updated successfully!');
            closeUserModal();
            loadUsers(currentPage, searchQuery);
        } else {
            app.showError(response.message || 'Failed to update user');
        }
    } catch (error) {
        app.showError('Failed to save user');
    } finally {
        app.hideLoading();
    }
}

// ============================================
// User Actions
// ============================================
window.editUser = function(userId) {
    openUserModal(userId);
};

window.toggleUserStatus = async function(userId) {
    try {
        app.showLoading();
        
        const response = await ajax.put(`/api/admin/users/${userId}/toggle-status`);
        
        if (response.success) {
            app.showSuccess(response.message || 'User status updated');
            loadUsers(currentPage, searchQuery);
        } else {
            app.showError(response.message || 'Failed to update user status');
        }
    } catch (error) {
        app.showError('Failed to update user status');
    } finally {
        app.hideLoading();
    }
};

window.deleteUser = function(userId) {
    app.confirm('Are you sure you want to delete this user?', async function() {
        try {
            app.showLoading();
            
            const response = await ajax.delete(`/api/admin/users/${userId}`);
            
            if (response.success) {
                app.showSuccess(response.message || 'User deleted successfully');
                loadUsers(currentPage, searchQuery);
            } else {
                app.showError(response.message || 'Failed to delete user');
            }
        } catch (error) {
            app.showError('Failed to delete user');
        } finally {
            app.hideLoading();
        }
    });
};

// ============================================
// Pagination
// ============================================
function setupPagination() {
    document.getElementById('prev-page')?.addEventListener('click', function() {
        if (currentPage > 1) loadUsers(currentPage - 1, searchQuery);
    });
    
    document.getElementById('next-page')?.addEventListener('click', function() {
        loadUsers(currentPage + 1, searchQuery);
    });
}

function updatePagination(totalPages, currentPage) {
    const prevBtn = document.getElementById('prev-page');
    const nextBtn = document.getElementById('next-page');
    const pageInfo = document.getElementById('page-info');
    
    if (prevBtn) prevBtn.disabled = currentPage === 1;
    if (nextBtn) nextBtn.disabled = currentPage >= totalPages;
    if (pageInfo) pageInfo.textContent = `Page ${currentPage} of ${totalPages}`;
}

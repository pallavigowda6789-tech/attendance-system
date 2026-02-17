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
        
        // TODO: Replace with actual API call
        // const response = await ajax.get('/api/admin/users', { page, size: usersPerPage, search });
        
        const mockUsers = generateMockUsers(page, search);
        const response = await ajax.mockResponse(mockUsers);
        
        if (response.success) {
            renderUsersTable(response.data.users);
            updatePagination(response.data.totalPages, page);
            currentPage = page;
        }
    } catch (error) {
        app.showError('Failed to load users');
    } finally {
        app.hideLoading();
    }
}

function renderUsersTable(users) {
    const tbody = document.getElementById('users-tbody');
    if (!tbody) return;
    
    if (users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="no-data">No users found</td></tr>';
        return;
    }
    
    tbody.innerHTML = users.map(user => `
        <tr>
            <td>${user.id}</td>
            <td>${user.firstName} ${user.lastName}</td>
            <td>${user.email}</td>
            <td><span class="badge role-badge badge-info">${user.role}</span></td>
            <td>
                <span class="user-status">
                    <span class="status-indicator ${user.enabled ? 'active' : 'inactive'}"></span>
                    ${user.enabled ? 'Active' : 'Inactive'}
                </span>
            </td>
            <td>${app.formatDate(user.createdAt)}</td>
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
    
    form.reset();
    document.getElementById('user-id').value = '';
    
    if (userId) {
        title.textContent = 'Edit User';
        loadUserData(userId);
    } else {
        title.textContent = 'Add User';
    }
    
    modal.style.display = 'flex';
}

function closeUserModal() {
    const modal = document.getElementById('user-modal');
    modal.style.display = 'none';
}

async function loadUserData(userId) {
    try {
        // TODO: Replace with actual API call
        // const response = await ajax.get(`/api/admin/users/${userId}`);
        
        const mockUser = {
            id: userId,
            firstName: 'John',
            lastName: 'Doe',
            email: 'john.doe@example.com',
            username: 'johndoe',
            role: 'USER',
            enabled: true
        };
        
        const response = await ajax.mockResponse(mockUser);
        
        if (response.success) {
            const user = response.data;
            document.getElementById('user-id').value = user.id;
            document.getElementById('user-firstName').value = user.firstName;
            document.getElementById('user-lastName').value = user.lastName;
            document.getElementById('user-email').value = user.email;
            document.getElementById('user-username').value = user.username;
            document.getElementById('user-role').value = user.role;
            document.getElementById('user-enabled').checked = user.enabled;
        }
    } catch (error) {
        app.showError('Failed to load user data');
    }
}

// ============================================
// Save User
// ============================================
function setupUserForm() {
    document.getElementById('user-form')?.addEventListener('submit', async function(e) {
        e.preventDefault();
        await saveUser();
    });
}

async function saveUser() {
    const userId = document.getElementById('user-id').value;
    const userData = {
        firstName: document.getElementById('user-firstName').value.trim(),
        lastName: document.getElementById('user-lastName').value.trim(),
        email: document.getElementById('user-email').value.trim(),
        username: document.getElementById('user-username').value.trim(),
        role: document.getElementById('user-role').value,
        enabled: document.getElementById('user-enabled').checked
    };
    
    try {
        app.showLoading();
        
        let response;
        if (userId) {
            // Update existing user
            // response = await ajax.put(`/api/admin/users/${userId}`, userData);
            response = await ajax.mockResponse({ success: true, message: 'User updated' });
        } else {
            // Create new user
            // response = await ajax.post('/api/admin/users', userData);
            response = await ajax.mockResponse({ success: true, message: 'User created' });
        }
        
        if (response.success) {
            app.showSuccess(userId ? 'User updated successfully!' : 'User created successfully!');
            closeUserModal();
            loadUsers(currentPage, searchQuery);
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
        
        // TODO: Replace with actual API call
        // const response = await ajax.put(`/api/admin/users/${userId}/toggle-status`);
        
        const response = await ajax.mockResponse({ success: true });
        
        if (response.success) {
            app.showSuccess('User status updated');
            loadUsers(currentPage, searchQuery);
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
            
            // TODO: Replace with actual API call
            // const response = await ajax.delete(`/api/admin/users/${userId}`);
            
            const response = await ajax.mockResponse({ success: true });
            
            if (response.success) {
                app.showSuccess('User deleted successfully');
                loadUsers(currentPage, searchQuery);
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
    if (nextBtn) nextBtn.disabled = currentPage === totalPages;
    if (pageInfo) pageInfo.textContent = `Page ${currentPage} of ${totalPages}`;
}

// ============================================
// Mock Data Generator
// ============================================
function generateMockUsers(page, search) {
    const users = [];
    const roles = ['USER', 'ADMIN', 'MANAGER'];
    
    for (let i = 0; i < usersPerPage; i++) {
        const id = (page - 1) * usersPerPage + i + 1;
        users.push({
            id: id,
            firstName: `User${id}`,
            lastName: `Test`,
            email: `user${id}@example.com`,
            username: `user${id}`,
            role: roles[Math.floor(Math.random() * roles.length)],
            enabled: Math.random() > 0.3,
            createdAt: new Date(Date.now() - Math.random() * 10000000000).toISOString()
        });
    }
    
    return {
        users: users,
        totalPages: 5,
        currentPage: page
    };
}

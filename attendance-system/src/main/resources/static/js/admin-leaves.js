// Admin Leave Management JavaScript

let currentPage = 0;
const recordsPerPage = 20;
let currentTab = 'pending';
let selectedLeaveId = null;

document.addEventListener('DOMContentLoaded', function() {
    initializeAdminLeaves();
});

function initializeAdminLeaves() {
    loadLeaves();
    setupEventListeners();
}

// ============================================
// Load Leave Records
// ============================================
async function loadLeaves(page = 0) {
    try {
        app.showLoading();
        
        const endpoint = currentTab === 'pending' ? '/api/leaves/admin/pending' : '/api/leaves/admin/all';
        const response = await ajax.get(endpoint, { page: page, size: recordsPerPage });
        
        if (response.success && response.data) {
            const pagedData = response.data;
            renderLeavesTable(pagedData.content || []);
            updatePagination(pagedData.totalPages || 1, page);
            currentPage = page;
        } else {
            app.showError(response.message || 'Failed to load leaves');
        }
    } catch (error) {
        console.error('Error loading leaves:', error);
        app.showError('Failed to load leaves');
    } finally {
        app.hideLoading();
    }
}

async function loadPendingCount() {
    try {
        const response = await ajax.get('/api/leaves/admin/pending-count');
        if (response.success && response.data) {
            document.getElementById('pending-badge').textContent = response.data.count || 0;
        }
    } catch (error) {
        console.error('Error loading pending count:', error);
    }
}

function renderLeavesTable(leaves) {
    const tbody = document.getElementById('leaves-tbody');
    
    if (!leaves || leaves.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="no-data">No leave requests found</td></tr>`;
        return;
    }
    
    tbody.innerHTML = leaves.map(leave => `
        <tr>
            <td>${leave.id}</td>
            <td>
                <div class="user-cell">
                    <strong>${leave.userFullName}</strong>
                    <small>${leave.username}</small>
                </div>
            </td>
            <td>
                <span class="leave-type-badge ${leave.leaveType.toLowerCase()}">
                    ${formatLeaveType(leave.leaveType)}
                </span>
            </td>
            <td>${formatDate(leave.startDate)}</td>
            <td>${formatDate(leave.endDate)}</td>
            <td>${leave.days}</td>
            <td class="reason-cell" title="${leave.reason || ''}">${truncate(leave.reason, 30) || '-'}</td>
            <td>
                <span class="status-badge ${leave.status.toLowerCase()}">
                    ${formatStatus(leave.status)}
                </span>
            </td>
            <td>
                ${leave.status === 'PENDING' ? 
                    `<button class="btn btn-sm btn-primary" onclick="openActionModal(${leave.id})">Review</button>` : 
                    `<span class="approved-by" title="By: ${leave.approvedByName || '-'}">${leave.approvedByName ? '👤 ' + leave.approvedByName : '-'}</span>`}
            </td>
        </tr>
    `).join('');
}

function updatePagination(totalPages, currentPage) {
    document.getElementById('prev-page').disabled = currentPage === 0;
    document.getElementById('next-page').disabled = currentPage >= totalPages - 1;
    document.getElementById('page-info').textContent = `Page ${currentPage + 1} of ${totalPages}`;
}

// ============================================
// Action Modal
// ============================================
async function openActionModal(leaveId) {
    selectedLeaveId = leaveId;
    
    try {
        const response = await ajax.get(`/api/leaves/${leaveId}`);
        
        if (response.success && response.data) {
            const leave = response.data;
            
            document.getElementById('leave-details').innerHTML = `
                <div class="detail-row">
                    <span class="detail-label">Employee:</span>
                    <span class="detail-value">${leave.userFullName} (${leave.username})</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Type:</span>
                    <span class="detail-value">${formatLeaveType(leave.leaveType)}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Duration:</span>
                    <span class="detail-value">${formatDate(leave.startDate)} to ${formatDate(leave.endDate)} (${leave.days} days)</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Reason:</span>
                    <span class="detail-value">${leave.reason || 'No reason provided'}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Requested:</span>
                    <span class="detail-value">${formatDateTime(leave.createdAt)}</span>
                </div>
            `;
            
            document.getElementById('action-modal').style.display = 'flex';
            document.getElementById('action-modal').classList.remove('hidden');
        }
    } catch (error) {
        console.error('Error loading leave details:', error);
        app.showError('Failed to load leave details');
    }
}

function closeActionModal() {
    document.getElementById('action-modal').style.display = 'none';
    document.getElementById('action-modal').classList.add('hidden');
    document.getElementById('action-comment').value = '';
    selectedLeaveId = null;
}

async function approveLeave() {
    if (!selectedLeaveId) return;
    
    const comment = document.getElementById('action-comment').value;
    
    try {
        app.showLoading();
        
        const response = await ajax.post(`/api/leaves/admin/${selectedLeaveId}/approve`, { comment: comment });
        
        if (response.success) {
            app.showSuccess('Leave request approved');
            closeActionModal();
            loadLeaves(currentPage);
            loadPendingCount();
        } else {
            app.showError(response.message || 'Failed to approve leave');
        }
    } catch (error) {
        console.error('Error approving leave:', error);
        app.showError('Failed to approve leave');
    } finally {
        app.hideLoading();
    }
}

async function rejectLeave() {
    if (!selectedLeaveId) return;
    
    const comment = document.getElementById('action-comment').value;
    
    try {
        app.showLoading();
        
        const response = await ajax.post(`/api/leaves/admin/${selectedLeaveId}/reject`, { comment: comment });
        
        if (response.success) {
            app.showSuccess('Leave request rejected');
            closeActionModal();
            loadLeaves(currentPage);
            loadPendingCount();
        } else {
            app.showError(response.message || 'Failed to reject leave');
        }
    } catch (error) {
        console.error('Error rejecting leave:', error);
        app.showError('Failed to reject leave');
    } finally {
        app.hideLoading();
    }
}

// ============================================
// Event Listeners
// ============================================
function setupEventListeners() {
    // Tab switching
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            currentTab = this.dataset.tab;
            loadLeaves(0);
        });
    });
    
    // Pagination
    document.getElementById('prev-page')?.addEventListener('click', () => {
        if (currentPage > 0) loadLeaves(currentPage - 1);
    });
    
    document.getElementById('next-page')?.addEventListener('click', () => {
        loadLeaves(currentPage + 1);
    });
}

// ============================================
// Utility Functions
// ============================================
function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '-';
    const date = new Date(dateTimeStr);
    return date.toLocaleString('en-US', { 
        year: 'numeric', month: 'short', day: 'numeric',
        hour: '2-digit', minute: '2-digit'
    });
}

function formatLeaveType(type) {
    const types = {
        'ANNUAL': '🏖️ Annual',
        'SICK': '🤒 Sick',
        'PERSONAL': '👤 Personal',
        'MATERNITY': '👶 Maternity',
        'PATERNITY': '👨‍👧 Paternity',
        'UNPAID': '💰 Unpaid',
        'OTHER': '📝 Other'
    };
    return types[type] || type;
}

function formatStatus(status) {
    const statuses = {
        'PENDING': '⏳ Pending',
        'APPROVED': '✓ Approved',
        'REJECTED': '✗ Rejected',
        'CANCELLED': '🚫 Cancelled'
    };
    return statuses[status] || status;
}

function truncate(str, len) {
    if (!str) return '';
    return str.length > len ? str.substring(0, len) + '...' : str;
}

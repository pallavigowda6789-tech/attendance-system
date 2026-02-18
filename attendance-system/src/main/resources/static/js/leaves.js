// User Leave Management JavaScript

let currentPage = 0;
const recordsPerPage = 10;

document.addEventListener('DOMContentLoaded', function() {
    initializeLeavesPage();
});

function initializeLeavesPage() {
    setMinDates();
    loadLeaveStats();
    loadMyLeaves();
    setupEventListeners();
}

function setMinDates() {
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('start-date').min = today;
    document.getElementById('end-date').min = today;
}

// ============================================
// Load Statistics
// ============================================
async function loadLeaveStats() {
    try {
        const response = await ajax.get('/api/leaves/my-stats');
        
        if (response.success && response.data) {
            const stats = response.data;
            document.getElementById('total-used').textContent = stats.totalDaysUsed || 0;
            document.getElementById('pending-count').textContent = stats.pendingRequests || 0;
            document.getElementById('approved-count').textContent = stats.approvedRequests || 0;
            document.getElementById('rejected-count').textContent = stats.rejectedRequests || 0;
        }
    } catch (error) {
        console.error('Error loading leave stats:', error);
    }
}

// ============================================
// Load Leave Records
// ============================================
async function loadMyLeaves(page = 0) {
    try {
        app.showLoading();
        
        const response = await ajax.get('/api/leaves/my-leaves', { page: page, size: recordsPerPage });
        
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

function renderLeavesTable(leaves) {
    const tbody = document.getElementById('leaves-tbody');
    
    if (!leaves || leaves.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="no-data">No leave requests found</td></tr>';
        return;
    }
    
    tbody.innerHTML = leaves.map(leave => `
        <tr>
            <td>${leave.id}</td>
            <td>
                <span class="leave-type-badge ${leave.leaveType.toLowerCase()}">
                    ${formatLeaveType(leave.leaveType)}
                </span>
            </td>
            <td>${formatDate(leave.startDate)}</td>
            <td>${formatDate(leave.endDate)}</td>
            <td>${leave.days}</td>
            <td>
                <span class="status-badge ${leave.status.toLowerCase()}">
                    ${formatStatus(leave.status)}
                </span>
            </td>
            <td>
                ${leave.status === 'PENDING' ? 
                    `<button class="btn btn-sm btn-outline" onclick="cancelLeave(${leave.id})">Cancel</button>` : 
                    '-'}
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
// Submit Leave Request
// ============================================
async function handleLeaveRequest(e) {
    e.preventDefault();
    
    const leaveType = document.getElementById('leave-type').value;
    const startDate = document.getElementById('start-date').value;
    const endDate = document.getElementById('end-date').value;
    const reason = document.getElementById('reason').value;
    
    // Validation
    if (!leaveType || !startDate || !endDate) {
        app.showError('Please fill all required fields');
        return;
    }
    
    if (new Date(endDate) < new Date(startDate)) {
        app.showError('End date cannot be before start date');
        return;
    }
    
    try {
        app.showLoading();
        
        const response = await ajax.post('/api/leaves/request', {
            leaveType: leaveType,
            startDate: startDate,
            endDate: endDate,
            reason: reason
        });
        
        if (response.success) {
            app.showSuccess(response.message || 'Leave request submitted successfully');
            document.getElementById('leave-request-form').reset();
            setMinDates();
            loadLeaveStats();
            loadMyLeaves(0);
        } else {
            app.showError(response.message || 'Failed to submit leave request');
        }
    } catch (error) {
        console.error('Error submitting leave:', error);
        app.showError('Failed to submit leave request');
    } finally {
        app.hideLoading();
    }
}

// ============================================
// Cancel Leave
// ============================================
async function cancelLeave(id) {
    if (!confirm('Are you sure you want to cancel this leave request?')) {
        return;
    }
    
    try {
        app.showLoading();
        
        const response = await ajax.post(`/api/leaves/${id}/cancel`);
        
        if (response.success) {
            app.showSuccess('Leave request cancelled');
            loadLeaveStats();
            loadMyLeaves(currentPage);
        } else {
            app.showError(response.message || 'Failed to cancel leave');
        }
    } catch (error) {
        console.error('Error cancelling leave:', error);
        app.showError('Failed to cancel leave');
    } finally {
        app.hideLoading();
    }
}

// ============================================
// Event Listeners
// ============================================
function setupEventListeners() {
    document.getElementById('leave-request-form')?.addEventListener('submit', handleLeaveRequest);
    
    document.getElementById('prev-page')?.addEventListener('click', () => {
        if (currentPage > 0) loadMyLeaves(currentPage - 1);
    });
    
    document.getElementById('next-page')?.addEventListener('click', () => {
        loadMyLeaves(currentPage + 1);
    });
    
    // Update end date min when start date changes
    document.getElementById('start-date')?.addEventListener('change', function() {
        document.getElementById('end-date').min = this.value;
        if (document.getElementById('end-date').value < this.value) {
            document.getElementById('end-date').value = this.value;
        }
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

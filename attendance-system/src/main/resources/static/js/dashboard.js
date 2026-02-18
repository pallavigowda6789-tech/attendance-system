// Dashboard JavaScript
// Handles dashboard functionality including stats, filters, and attendance data

document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
});

function initializeDashboard() {
    loadRecentAttendance();
    setupFilters();
    setupMarkAttendanceButton();
}

// ============================================
// Load Recent Attendance Records
// ============================================
let currentPage = 0;
const recordsPerPage = 10;

async function loadRecentAttendance(page = 0, filters = {}) {
    try {
        app.showLoading();
        
        const params = { page: page, size: recordsPerPage };
        if (filters.startDate) params.startDate = filters.startDate;
        if (filters.endDate) params.endDate = filters.endDate;
        
        console.log('Loading attendance with params:', params);
        const response = await ajax.get('/api/attendance/my-records', params);
        console.log('Full response:', response);
        
        if (response.success && response.data) {
            // response.data is now the unwrapped PagedResponse
            const pagedData = response.data;
            console.log('Paged data:', pagedData);
            const records = pagedData.content || [];
            console.log('Records:', records);
            
            renderAttendanceTable(records);
            updatePagination(pagedData.totalPages || 1, page);
            currentPage = page;
        } else {
            console.error('API returned error:', response);
            app.showError(response.message || 'Failed to load attendance records');
        }
    } catch (error) {
        console.error('Error loading attendance:', error);
        app.showError('Failed to load attendance records');
    } finally {
        app.hideLoading();
    }
}

function renderAttendanceTable(records) {
    const tbody = document.getElementById('attendance-tbody');
    if (!tbody) return;
    
    if (!records || records.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="no-data">
                    <div class="empty-state">
                        <div class="empty-state-icon">📭</div>
                        <p>No attendance records found</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = records.map(record => `
        <tr>
            <td>${record.id}</td>
            <td>${app.formatDate(record.date)}</td>
            <td>
                <span class="status-badge ${record.present ? 'present' : 'absent'}">
                    ${record.present ? '✓ Present' : '✗ Absent'}
                </span>
            </td>
            <td>${app.formatDateTime(record.checkInTime || record.timestamp)}</td>
        </tr>
    `).join('');
}

function updatePagination(totalPages, currentPage) {
    const prevBtn = document.getElementById('prev-page');
    const nextBtn = document.getElementById('next-page');
    const pageInfo = document.getElementById('page-info');
    
    if (prevBtn) prevBtn.disabled = currentPage === 0;
    if (nextBtn) nextBtn.disabled = currentPage >= totalPages - 1;
    if (pageInfo) pageInfo.textContent = `Page ${currentPage + 1} of ${totalPages}`;
}

// ============================================
// Filters
// ============================================
function setupFilters() {
    document.getElementById('apply-filter-btn')?.addEventListener('click', applyFilters);
    document.getElementById('reset-filter-btn')?.addEventListener('click', resetFilters);
}

function applyFilters() {
    const startDate = document.getElementById('start-date')?.value;
    const endDate = document.getElementById('end-date')?.value;
    
    const filters = {};
    if (startDate) filters.startDate = startDate;
    if (endDate) filters.endDate = endDate;
    
    loadRecentAttendance(0, filters);
}

function resetFilters() {
    const startDateEl = document.getElementById('start-date');
    const endDateEl = document.getElementById('end-date');
    if (startDateEl) startDateEl.value = '';
    if (endDateEl) endDateEl.value = '';
    loadRecentAttendance(0);
}

// ============================================
// Mark Attendance
// ============================================
function setupMarkAttendanceButton() {
    document.getElementById('mark-attendance-btn')?.addEventListener('click', async function() {
        try {
            app.showLoading();
            
            const response = await ajax.post('/api/attendance/mark', { present: true });
            
            if (response.success) {
                app.showSuccess(response.message || 'Attendance marked successfully!');
                setTimeout(() => window.location.reload(), 1500);
            } else {
                app.showError(response.message || 'Failed to mark attendance');
            }
        } catch (error) {
            console.error('Error marking attendance:', error);
            app.showError('Failed to mark attendance');
        } finally {
            app.hideLoading();
        }
    });
}

// ============================================
// Pagination Controls
// ============================================
document.getElementById('prev-page')?.addEventListener('click', function() {
    if (currentPage > 0) {
        loadRecentAttendance(currentPage - 1);
    }
});

document.getElementById('next-page')?.addEventListener('click', function() {
    loadRecentAttendance(currentPage + 1);
});

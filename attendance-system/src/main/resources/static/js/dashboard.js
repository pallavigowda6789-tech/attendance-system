// Dashboard JavaScript
// Handles dashboard functionality including stats, filters, and attendance data

document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
});

// Initialize dashboard
function initializeDashboard() {
    // Stats are already loaded from server-side (Thymeleaf)
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
    logger.functionStart('loadRecentAttendance', { page, filters });
    
    try {
        logger.info('DASHBOARD', `Loading attendance records for page ${page}`);
        app.showLoading();
        
        // Build query parameters
        const params = {
            page: page,
            size: recordsPerPage
        };
        
        if (filters.startDate) params.startDate = filters.startDate;
        if (filters.endDate) params.endDate = filters.endDate;
        
        logger.debug('DASHBOARD', 'Request parameters', params);
        
        const response = await ajax.get('/api/attendance/my-records', params);
        
        logger.api('DASHBOARD', 'Received dashboard response', response);
        
        if (response.success) {
            // Access the data from ajax helper response
            const apiData = response.data;
            logger.success('DASHBOARD', 'Dashboard data loaded', apiData);
            
            const records = apiData.content || apiData || [];
            logger.info('DASHBOARD', `Found ${records.length} attendance records`);
            
            renderAttendanceTable(records);
            updatePagination(apiData.totalPages || 1, page);
            currentPage = page;
        } else {
            logger.error('DASHBOARD', 'Failed to load dashboard data', response);
            app.showError(response.message || 'Failed to load attendance records');
        }
    } catch (error) {
        logger.error('DASHBOARD', 'Exception while loading attendance', error);
        app.showError('Failed to load attendance records');
    } finally {
        app.hideLoading();
    }
    
    logger.functionEnd('loadRecentAttendance', { success: true });
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
            <td>${app.formatDateTime(record.timestamp)}</td>
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
    const filterBtn = document.getElementById('apply-filter-btn');
    const resetBtn = document.getElementById('reset-filter-btn');
    
    filterBtn?.addEventListener('click', applyFilters);
    resetBtn?.addEventListener('click', resetFilters);
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
    document.getElementById('start-date').value = '';
    document.getElementById('end-date').value = '';
    loadRecentAttendance(0);
}

// ============================================
// Mark Attendance
// ============================================
function setupMarkAttendanceButton() {
    const markBtn = document.getElementById('mark-attendance-btn');
    
    markBtn?.addEventListener('click', async function() {
        try {
            app.showLoading();
            
            const response = await ajax.post('/api/attendance/mark');
            
            if (response.success) {
                app.showSuccess('Attendance marked successfully!');
                // Reload page to refresh stats and records
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

// Admin Attendance Management JavaScript

let currentPage = 0;
const recordsPerPage = 20;
let attendanceChart = null;
let userSummaryChart = null;

document.addEventListener('DOMContentLoaded', function() {
    initializeAdminAttendance();
});

function initializeAdminAttendance() {
    setDefaultDates();
    loadStats();
    loadAttendanceRecords();
    loadChartData();
    setupEventListeners();
}

function setDefaultDates() {
    const today = new Date();
    const firstOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    
    document.getElementById('start-date').value = firstOfMonth.toISOString().split('T')[0];
    document.getElementById('end-date').value = today.toISOString().split('T')[0];
    document.getElementById('mark-date').value = today.toISOString().split('T')[0];
}

// ============================================
// Load Statistics
// ============================================
async function loadStats() {
    try {
        const response = await ajax.get('/api/admin/stats');
        if (response.success && response.data) {
            const stats = response.data;
            document.getElementById('total-users').textContent = stats.totalUsers || 0;
        }
        
        // Load today's attendance summary
        const summaryResponse = await ajax.get('/api/admin/attendance/summary');
        if (summaryResponse.success && summaryResponse.data) {
            const summary = summaryResponse.data;
            let presentToday = 0;
            let totalPercent = 0;
            
            summary.forEach(u => {
                if (u.presentDays > 0) presentToday++;
                totalPercent += u.attendancePercentage || 0;
            });
            
            document.getElementById('present-today').textContent = presentToday;
            document.getElementById('absent-today').textContent = summary.length - presentToday;
            document.getElementById('avg-attendance').textContent = 
                summary.length > 0 ? Math.round(totalPercent / summary.length) + '%' : '0%';
        }
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

// ============================================
// Load Attendance Records
// ============================================
async function loadAttendanceRecords(page = 0) {
    try {
        app.showLoading();
        
        const params = { page: page, size: recordsPerPage };
        
        const userId = document.getElementById('user-filter').value;
        const startDate = document.getElementById('start-date').value;
        const endDate = document.getElementById('end-date').value;
        
        if (userId) params.userId = userId;
        if (startDate) params.startDate = startDate;
        if (endDate) params.endDate = endDate;
        
        const response = await ajax.get('/api/admin/attendance', params);
        
        if (response.success && response.data) {
            const pagedData = response.data;
            renderAttendanceTable(pagedData.content || []);
            updatePagination(pagedData.totalPages || 1, page);
            currentPage = page;
        } else {
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
    
    if (!records || records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="no-data">No attendance records found</td></tr>';
        return;
    }
    
    tbody.innerHTML = records.map(record => `
        <tr>
            <td>${record.id}</td>
            <td>
                <div class="user-cell">
                    <strong>${record.fullName || record.username}</strong>
                    <small>${record.username}</small>
                </div>
            </td>
            <td>${formatDate(record.date)}</td>
            <td>
                <span class="status-badge ${record.present ? 'present' : 'absent'}">
                    ${record.present ? '✓ Present' : '✗ Absent'}
                </span>
            </td>
            <td>${record.checkInTime ? formatTime(record.checkInTime) : '-'}</td>
            <td>${record.checkOutTime ? formatTime(record.checkOutTime) : '-'}</td>
            <td>
                <button class="btn btn-sm btn-outline" onclick="deleteAttendance(${record.id})">
                    🗑️
                </button>
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
// Charts
// ============================================
async function loadChartData() {
    try {
        const response = await ajax.get('/api/admin/attendance/summary');
        
        if (response.success && response.data) {
            const summary = response.data;
            
            // Attendance Overview Chart (Pie)
            const totalPresent = summary.reduce((sum, u) => sum + (u.presentDays || 0), 0);
            const totalAbsent = summary.reduce((sum, u) => sum + (u.absentDays || 0), 0);
            
            renderAttendanceChart(totalPresent, totalAbsent);
            renderUserSummaryChart(summary);
        }
    } catch (error) {
        console.error('Error loading chart data:', error);
    }
}

function renderAttendanceChart(present, absent) {
    const ctx = document.getElementById('attendanceChart');
    if (!ctx) return;
    
    if (attendanceChart) {
        attendanceChart.destroy();
    }
    
    attendanceChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Present', 'Absent'],
            datasets: [{
                data: [present, absent],
                backgroundColor: ['#10b981', '#ef4444'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

function renderUserSummaryChart(summary) {
    const ctx = document.getElementById('userSummaryChart');
    if (!ctx) return;
    
    if (userSummaryChart) {
        userSummaryChart.destroy();
    }
    
    // Take top 10 users for the chart
    const topUsers = summary.slice(0, 10);
    
    userSummaryChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: topUsers.map(u => u.username),
            datasets: [
                {
                    label: 'Present',
                    data: topUsers.map(u => u.presentDays || 0),
                    backgroundColor: '#10b981'
                },
                {
                    label: 'Absent',
                    data: topUsers.map(u => u.absentDays || 0),
                    backgroundColor: '#ef4444'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    stacked: true
                },
                y: {
                    stacked: true,
                    beginAtZero: true
                }
            },
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

// ============================================
// Actions
// ============================================
async function deleteAttendance(id) {
    if (!confirm('Are you sure you want to delete this attendance record?')) {
        return;
    }
    
    try {
        app.showLoading();
        const response = await ajax.delete(`/api/admin/attendance/${id}`);
        
        if (response.success) {
            app.showSuccess('Attendance record deleted');
            loadAttendanceRecords(currentPage);
            loadChartData();
        } else {
            app.showError(response.message || 'Failed to delete record');
        }
    } catch (error) {
        console.error('Error deleting attendance:', error);
        app.showError('Failed to delete record');
    } finally {
        app.hideLoading();
    }
}

// ============================================
// Mark Attendance Modal
// ============================================
function openMarkModal() {
    document.getElementById('mark-modal').style.display = 'flex';
    document.getElementById('mark-modal').classList.remove('hidden');
}

function closeMarkModal() {
    document.getElementById('mark-modal').style.display = 'none';
    document.getElementById('mark-modal').classList.add('hidden');
    document.getElementById('mark-form').reset();
    setDefaultDates();
}

async function handleMarkAttendance(e) {
    e.preventDefault();
    
    const userId = document.getElementById('mark-user').value;
    const date = document.getElementById('mark-date').value;
    const present = document.querySelector('input[name="mark-status"]:checked').value === 'true';
    
    if (!userId || !date) {
        app.showError('Please fill all required fields');
        return;
    }
    
    try {
        app.showLoading();
        
        const response = await ajax.post('/api/admin/attendance/mark', {
            userId: parseInt(userId),
            date: date,
            present: present
        });
        
        if (response.success) {
            app.showSuccess('Attendance marked successfully');
            closeMarkModal();
            loadAttendanceRecords(currentPage);
            loadChartData();
        } else {
            app.showError(response.message || 'Failed to mark attendance');
        }
    } catch (error) {
        console.error('Error marking attendance:', error);
        app.showError('Failed to mark attendance');
    } finally {
        app.hideLoading();
    }
}

// ============================================
// Event Listeners
// ============================================
function setupEventListeners() {
    document.getElementById('apply-filter-btn')?.addEventListener('click', () => loadAttendanceRecords(0));
    document.getElementById('reset-filter-btn')?.addEventListener('click', resetFilters);
    document.getElementById('prev-page')?.addEventListener('click', () => {
        if (currentPage > 0) loadAttendanceRecords(currentPage - 1);
    });
    document.getElementById('next-page')?.addEventListener('click', () => {
        loadAttendanceRecords(currentPage + 1);
    });
    document.getElementById('mark-attendance-btn')?.addEventListener('click', openMarkModal);
    document.getElementById('mark-form')?.addEventListener('submit', handleMarkAttendance);
}

function resetFilters() {
    document.getElementById('user-filter').value = '';
    setDefaultDates();
    loadAttendanceRecords(0);
}

// ============================================
// Utility Functions
// ============================================
function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

function formatTime(dateTimeStr) {
    if (!dateTimeStr) return '-';
    const date = new Date(dateTimeStr);
    return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
}

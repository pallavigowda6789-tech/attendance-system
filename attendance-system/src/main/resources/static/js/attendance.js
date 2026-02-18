// Attendance Page JavaScript

let currentMonth = new Date().getMonth();
let currentYear = new Date().getFullYear();
let attendanceData = {};

document.addEventListener('DOMContentLoaded', function() {
    initializeAttendancePage();
});

function initializeAttendancePage() {
    updateCurrentDate();
    loadAttendanceData().then(() => {
        renderCalendar();
        loadAttendanceHistory();
    });
    setupMarkAttendanceForm();
    setupCalendarNavigation();
    setupExportButton();
}

// ============================================
// Load Attendance Data
// ============================================
async function loadAttendanceData() {
    try {
        console.log('Loading attendance data...');
        const response = await ajax.get('/api/attendance/my-records', { page: 0, size: 1000 });
        console.log('Full response:', response);
        
        if (response.success && response.data) {
            // response.data is the unwrapped PagedResponse
            const records = response.data.content || [];
            console.log('Records count:', records.length);
            
            attendanceData = {};
            records.forEach(record => {
                attendanceData[record.date] = record;
            });
            console.log('Attendance data keys:', Object.keys(attendanceData));
        } else {
            console.error('API error:', response);
            app.showError(response.message || 'Failed to load attendance data');
        }
    } catch (error) {
        console.error('Error loading attendance:', error);
        app.showError('Failed to load attendance data');
    }
}

// ============================================
// Current Date Display
// ============================================
function updateCurrentDate() {
    const today = new Date();
    const dateEl = document.getElementById('current-date');
    if (dateEl) {
        dateEl.textContent = app.formatDate(today.toISOString());
    }
    checkTodayStatus();
}

async function checkTodayStatus() {
    try {
        const response = await ajax.get('/api/attendance/today-status');
        const statusEl = document.getElementById('today-status');
        if (!statusEl) return;
        
        if (response.success && response.data) {
            const status = response.data;
            if (status.marked) {
                statusEl.textContent = status.present ? 'Present ✓' : 'Absent ✗';
                if (status.checkedOut) {
                    statusEl.textContent += ' (Checked Out)';
                }
            } else {
                statusEl.textContent = 'Not Marked Yet';
            }
        } else {
            statusEl.textContent = 'Not Marked Yet';
        }
    } catch (error) {
        console.error('Error checking today status:', error);
        const statusEl = document.getElementById('today-status');
        if (statusEl) statusEl.textContent = 'Not Marked Yet';
    }
}

// ============================================
// Mark Attendance Form
// ============================================
function setupMarkAttendanceForm() {
    const form = document.getElementById('mark-attendance-form');
    
    form?.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const notes = document.getElementById('notes')?.value.trim() || '';
        
        try {
            app.showLoading();
            
            const response = await ajax.post('/api/attendance/mark', { present: true, notes: notes });
            
            if (response.success) {
                app.showSuccess(response.message || 'Attendance marked successfully!');
                document.getElementById('today-status').textContent = 'Present ✓';
                const notesEl = document.getElementById('notes');
                if (notesEl) notesEl.value = '';
                await loadAttendanceData();
                renderCalendar();
                loadAttendanceHistory();
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
// Calendar Rendering
// ============================================
function renderCalendar() {
    const grid = document.getElementById('calendar-grid');
    const monthYear = document.getElementById('calendar-month-year');
    
    if (!grid || !monthYear) return;
    
    const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
                        'July', 'August', 'September', 'October', 'November', 'December'];
    monthYear.textContent = `${monthNames[currentMonth]} ${currentYear}`;
    
    grid.innerHTML = '';
    
    // Day headers
    const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    dayNames.forEach(day => {
        const header = document.createElement('div');
        header.className = 'calendar-day-header';
        header.textContent = day;
        grid.appendChild(header);
    });
    
    const firstDay = new Date(currentYear, currentMonth, 1).getDay();
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    // Empty cells before month starts
    for (let i = 0; i < firstDay; i++) {
        const emptyDay = document.createElement('div');
        emptyDay.className = 'calendar-day disabled';
        grid.appendChild(emptyDay);
    }
    
    // Days of month
    for (let day = 1; day <= daysInMonth; day++) {
        const dayEl = document.createElement('div');
        dayEl.className = 'calendar-day';
        dayEl.textContent = day;
        
        const dateStr = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        const cellDate = new Date(currentYear, currentMonth, day);
        cellDate.setHours(0, 0, 0, 0);
        
        // Mark today
        if (day === today.getDate() && currentMonth === today.getMonth() && currentYear === today.getFullYear()) {
            dayEl.classList.add('today');
        }
        
        // Future dates
        if (cellDate > today) {
            dayEl.classList.add('future');
        } else {
            // Check attendance status
            const attendance = attendanceData[dateStr];
            if (attendance) {
                dayEl.classList.add(attendance.present ? 'present' : 'absent');
            } else {
                dayEl.classList.add('absent');
            }
        }
        
        grid.appendChild(dayEl);
    }
}

function setupCalendarNavigation() {
    document.getElementById('prev-month')?.addEventListener('click', async function() {
        currentMonth--;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }
        await loadAttendanceData();
        renderCalendar();
    });
    
    document.getElementById('next-month')?.addEventListener('click', async function() {
        currentMonth++;
        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        await loadAttendanceData();
        renderCalendar();
    });
    
    document.getElementById('today-btn')?.addEventListener('click', async function() {
        const today = new Date();
        currentMonth = today.getMonth();
        currentYear = today.getFullYear();
        await loadAttendanceData();
        renderCalendar();
    });
}

// ============================================
// Attendance History
// ============================================
let historyPage = 0;
const historyPerPage = 10;

async function loadAttendanceHistory(page = 0) {
    try {
        app.showLoading();
        
        const response = await ajax.get('/api/attendance/my-records', { page: page, size: historyPerPage });
        
        if (response.success && response.data) {
            const pagedData = response.data;
            const records = pagedData.content || [];
            
            renderHistoryTable(records);
            updateHistoryPagination(pagedData.totalPages || 1, page);
            historyPage = page;
        } else {
            app.showError(response.message || 'Failed to load attendance history');
        }
    } catch (error) {
        console.error('Error loading history:', error);
        app.showError('Failed to load attendance history');
    } finally {
        app.hideLoading();
    }
}

function renderHistoryTable(records) {
    const tbody = document.getElementById('history-tbody');
    if (!tbody) return;
    
    if (records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="no-data">No records found</td></tr>';
        return;
    }
    
    tbody.innerHTML = records.map(record => `
        <tr>
            <td>${app.formatDate(record.date)}</td>
            <td>
                <span class="status-badge ${record.present ? 'present' : 'absent'}">
                    ${record.present ? '✓ Present' : '✗ Absent'}
                </span>
            </td>
            <td>${app.formatDateTime(record.checkInTime || record.timestamp)}</td>
            <td>${record.notes || '-'}</td>
        </tr>
    `).join('');
}

function updateHistoryPagination(totalPages, currentPage) {
    const prevBtn = document.getElementById('prev-page');
    const nextBtn = document.getElementById('next-page');
    const pageInfo = document.getElementById('page-info');
    
    if (prevBtn) prevBtn.disabled = currentPage === 0;
    if (nextBtn) nextBtn.disabled = currentPage >= totalPages - 1;
    if (pageInfo) pageInfo.textContent = `Page ${currentPage + 1} of ${totalPages}`;
}

document.getElementById('prev-page')?.addEventListener('click', function() {
    if (historyPage > 0) loadAttendanceHistory(historyPage - 1);
});

document.getElementById('next-page')?.addEventListener('click', function() {
    loadAttendanceHistory(historyPage + 1);
});

// ============================================
// Export Functionality
// ============================================
function setupExportButton() {
    document.getElementById('export-btn')?.addEventListener('click', function() {
        app.showInfo('Export functionality will be available soon');
    });
}

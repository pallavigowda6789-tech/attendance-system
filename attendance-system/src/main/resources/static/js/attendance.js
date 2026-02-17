// Attendance Page JavaScript

let currentMonth = new Date().getMonth();
let currentYear = new Date().getFullYear();
let attendanceData = {}; // Store attendance records by date

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
    logger.functionStart('loadAttendanceData', {});
    
    try {
        logger.info('ATTENDANCE', 'Starting to load attendance data...');
        
        // Load all attendance records for current user
        const response = await ajax.get('/api/attendance/my-records', { page: 0, size: 1000 });
        
        logger.api('ATTENDANCE', 'Received response from API', response);
        
        if (response.success) {
            logger.success('ATTENDANCE', 'API call successful', response.data);
            
            // Convert to date-indexed object for easy lookup
            const records = response.data.content || response.data || [];
            logger.info('ATTENDANCE', `Found ${records.length} attendance records`, records.slice(0, 3));
            
            attendanceData = {};
            records.forEach(record => {
                attendanceData[record.date] = record;
            });
            
            logger.success('ATTENDANCE', `Indexed ${Object.keys(attendanceData).length} dates`, {
                dates: Object.keys(attendanceData).slice(0, 5),
                totalDates: Object.keys(attendanceData).length
            });
        } else {
            logger.error('ATTENDANCE', 'API call failed', response);
            app.showError(response.message || 'Failed to load attendance data');
        }
    } catch (error) {
        logger.error('ATTENDANCE', 'Exception occurred while loading data', error);
        app.showError('Failed to load attendance data');
    }
    
    logger.functionEnd('loadAttendanceData', { recordCount: Object.keys(attendanceData).length });
}

// ============================================
// Current Date Display
// ============================================
function updateCurrentDate() {
    const today = new Date();
    document.getElementById('current-date').textContent = app.formatDate(today.toISOString());
    checkTodayStatus();
}

async function checkTodayStatus() {
    // TODO: Check if attendance is already marked for today
    // const response = await ajax.get('/api/attendance/today');
    const statusEl = document.getElementById('today-status');
    statusEl.textContent = 'Not Marked Yet';
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
            
            const response = await ajax.post('/api/attendance/mark', { 
                present: true,
                notes: notes
            });
            
            if (response.success && response.data.success) {
                app.showSuccess(response.data.message || 'Attendance marked successfully!');
                document.getElementById('today-status').textContent = 'Present ✓';
                document.getElementById('notes').value = '';
                // Reload data and re-render
                await loadAttendanceData();
                renderCalendar();
                loadAttendanceHistory();
            } else {
                app.showError(response.data?.message || response.message || 'Failed to mark attendance');
            }
        } catch (error) {
            console.error('Error marking attendance:', error);
            app.showError('Failed to mark attendance. ' + (error.message || ''));
        } finally {
            app.hideLoading();
        }
    });
}

// ============================================
// Calendar Rendering
// ============================================
function renderCalendar() {
    logger.functionStart('renderCalendar', { currentMonth, currentYear });
    
    const grid = document.getElementById('calendar-grid');
    const monthYear = document.getElementById('calendar-month-year');
    
    if (!grid || !monthYear) {
        logger.warning('CALENDAR', 'Calendar elements not found', { grid: !!grid, monthYear: !!monthYear });
        return;
    }
    
    // Update month/year display
    const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
                        'July', 'August', 'September', 'October', 'November', 'December'];
    monthYear.textContent = `${monthNames[currentMonth]} ${currentYear}`;
    
    logger.info('CALENDAR', `Rendering calendar for ${monthNames[currentMonth]} ${currentYear}`);
    
    // Clear grid
    grid.innerHTML = '';
    
    // Day headers
    const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    dayNames.forEach(day => {
        const header = document.createElement('div');
        header.className = 'calendar-day-header';
        header.textContent = day;
        grid.appendChild(header);
    });
    
    // Get first day of month and number of days
    const firstDay = new Date(currentYear, currentMonth, 1).getDay();
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Reset time for accurate comparison
    
    logger.debug('CALENDAR', 'Calendar parameters', {
        firstDay,
        daysInMonth,
        today: today.toISOString().split('T')[0],
        attendanceDataKeys: Object.keys(attendanceData).length
    });
    
    // Empty cells for days before month starts
    for (let i = 0; i < firstDay; i++) {
        const emptyDay = document.createElement('div');
        emptyDay.className = 'calendar-day disabled';
        grid.appendChild(emptyDay);
    }
    
    let presentCount = 0;
    let absentCount = 0;
    let futureCount = 0;
    
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
        
        // Check if date is in the future
        if (cellDate > today) {
            dayEl.classList.add('future');
            futureCount++;
        } else {
            // Check attendance status from loaded data
            const attendance = attendanceData[dateStr];
            
            if (attendance) {
                if (attendance.present) {
                    dayEl.classList.add('present');
                    presentCount++;
                } else {
                    dayEl.classList.add('absent');
                    absentCount++;
                }
            } else {
                // No record = absent
                dayEl.classList.add('absent');
                absentCount++;
            }
        }
        
        grid.appendChild(dayEl);
    }
    
    logger.success('CALENDAR', 'Calendar rendered successfully', {
        totalDays: daysInMonth,
        present: presentCount,
        absent: absentCount,
        future: futureCount
    });
    
    logger.functionEnd('renderCalendar', { success: true });
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
let historyPage = 0; // Changed to 0-based to match API
const historyPerPage = 10;

async function loadAttendanceHistory(page = 0) {
    logger.functionStart('loadAttendanceHistory', { page });
    
    try {
        logger.info('HISTORY', `Loading attendance history for page ${page}`);
        app.showLoading();
        
        const response = await ajax.get('/api/attendance/my-records', { page: page, size: historyPerPage });
        
        logger.api('HISTORY', 'Received history response', response);
        
        if (response.success) {
            const apiData = response.data;
            logger.success('HISTORY', 'History data loaded', apiData);
            
            const records = apiData.content || apiData || [];
            logger.info('HISTORY', `Found ${records.length} history records`);
            
            renderHistoryTable(records);
            updateHistoryPagination(apiData.totalPages || 1, page);
            historyPage = page;
        } else {
            logger.error('HISTORY', 'Failed to load history', response);
            app.showError(response.message || 'Failed to load attendance history');
        }
    } catch (error) {
        logger.error('HISTORY', 'Exception while loading history', error);
        app.showError('Failed to load attendance history');
    } finally {
        app.hideLoading();
    }
    
    logger.functionEnd('loadAttendanceHistory', { success: true });
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
            <td>${app.formatDateTime(record.timestamp)}</td>
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
        // TODO: Implement export to CSV/PDF
    });
}

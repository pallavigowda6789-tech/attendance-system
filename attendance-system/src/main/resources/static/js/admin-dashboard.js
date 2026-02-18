// Admin Dashboard JavaScript
// Handles admin dashboard functionality including stats and charts

document.addEventListener('DOMContentLoaded', function() {
    initializeAdminDashboard();
});

async function initializeAdminDashboard() {
    await loadDashboardStats();
    await loadRecentActivity();
    setupExportButton();
}

// ============================================
// Load Dashboard Statistics
// ============================================
async function loadDashboardStats() {
    try {
        // Load system stats
        const statsResponse = await ajax.get('/api/admin/stats');
        
        if (statsResponse.success && statsResponse.data) {
            const stats = statsResponse.data;
            document.getElementById('total-users').textContent = stats.totalUsers || 0;
            document.getElementById('present-today').textContent = stats.presentToday || 0;
            document.getElementById('absent-today').textContent = stats.absentToday || 0;
        }
        
        // Load pending leaves count
        const leavesResponse = await ajax.get('/api/leaves/admin/pending');
        if (leavesResponse.success && leavesResponse.data) {
            const pendingLeaves = leavesResponse.data;
            document.getElementById('pending-leaves').textContent = 
                Array.isArray(pendingLeaves) ? pendingLeaves.length : 0;
        }
        
        // Render charts
        renderAttendanceChart(statsResponse.data);
        await renderWeeklyChart();
        
    } catch (error) {
        console.error('Error loading dashboard stats:', error);
    }
}

// ============================================
// Attendance Pie Chart
// ============================================
function renderAttendanceChart(stats) {
    const ctx = document.getElementById('attendance-chart');
    if (!ctx) return;
    
    const presentToday = stats?.presentToday || 0;
    const absentToday = stats?.absentToday || 0;
    const totalUsers = stats?.totalUsers || 0;
    const notMarked = Math.max(0, totalUsers - presentToday - absentToday);
    
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Present', 'Absent', 'Not Marked'],
            datasets: [{
                data: [presentToday, absentToday, notMarked],
                backgroundColor: [
                    '#10b981', // green
                    '#ef4444', // red
                    '#9ca3af'  // gray
                ],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

// ============================================
// Weekly Trend Chart
// ============================================
async function renderWeeklyChart() {
    const ctx = document.getElementById('weekly-chart');
    if (!ctx) return;
    
    try {
        // Get last 7 days attendance summary
        const endDate = new Date().toISOString().split('T')[0];
        const startDate = new Date(Date.now() - 6 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
        
        const response = await ajax.get('/api/admin/attendance/summary', { startDate, endDate });
        
        // Generate labels for last 7 days
        const labels = [];
        const presentData = [];
        const absentData = [];
        
        for (let i = 6; i >= 0; i--) {
            const date = new Date(Date.now() - i * 24 * 60 * 60 * 1000);
            const dateStr = date.toISOString().split('T')[0];
            labels.push(date.toLocaleDateString('en-US', { weekday: 'short' }));
            
            if (response.success && response.data && response.data[dateStr]) {
                presentData.push(response.data[dateStr].present || 0);
                absentData.push(response.data[dateStr].absent || 0);
            } else {
                presentData.push(0);
                absentData.push(0);
            }
        }
        
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Present',
                        data: presentData,
                        backgroundColor: '#10b981',
                        borderRadius: 4
                    },
                    {
                        label: 'Absent',
                        data: absentData,
                        backgroundColor: '#ef4444',
                        borderRadius: 4
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                scales: {
                    x: {
                        stacked: true
                    },
                    y: {
                        stacked: true,
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                },
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error rendering weekly chart:', error);
    }
}

// ============================================
// Recent Activity
// ============================================
async function loadRecentActivity() {
    try {
        const response = await ajax.get('/api/admin/attendance', { page: 0, size: 5 });
        
        const tbody = document.getElementById('recent-activity-tbody');
        if (!tbody) return;
        
        if (response.success && response.data && response.data.content) {
            const records = response.data.content;
            
            if (records.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="4" class="no-data">No recent activity</td>
                    </tr>
                `;
                return;
            }
            
            tbody.innerHTML = records.map(record => `
                <tr>
                    <td>
                        <div class="user-cell">
                            <strong>${record.username || 'Unknown'}</strong>
                        </div>
                    </td>
                    <td>Attendance marked</td>
                    <td>${app.formatDate(record.date)}</td>
                    <td>
                        <span class="status-badge ${record.present ? 'present' : 'absent'}">
                            ${record.present ? '✓ Present' : '✗ Absent'}
                        </span>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = `
                <tr>
                    <td colspan="4" class="no-data">Failed to load activity</td>
                </tr>
            `;
        }
    } catch (error) {
        console.error('Error loading recent activity:', error);
    }
}

// ============================================
// Export Button
// ============================================
function setupExportButton() {
    const exportBtn = document.getElementById('export-btn');
    if (exportBtn) {
        exportBtn.addEventListener('click', async function() {
            app.showInfo('Export feature coming soon!');
        });
    }
}

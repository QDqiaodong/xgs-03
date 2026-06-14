<template>
    <div class="checkin-page">
        <h1 class="page-title">🎯 养护打卡</h1>

        <div class="status-section">
            <div class="streak-card card">
                <div class="streak-main">
                    <div class="streak-number-wrapper">
                        <span class="streak-number">{{ status.currentStreak || 0 }}</span>
                        <span class="streak-unit">天</span>
                    </div>
                    <div class="streak-label">当前连续打卡</div>
                </div>
                <div class="streak-stats">
                    <div class="stat-item">
                        <span class="stat-value">{{ status.maxStreak || 0 }}</span>
                        <span class="stat-label">最长连续</span>
                    </div>
                    <div class="stat-divider"></div>
                    <div class="stat-item">
                        <span class="stat-value">{{ status.totalCheckInDays || 0 }}</span>
                        <span class="stat-label">累计打卡</span>
                    </div>
                    <div class="stat-divider"></div>
                    <div class="stat-item">
                        <span class="stat-value">{{ todayCareCount }}</span>
                        <span class="stat-label">今日养护</span>
                    </div>
                </div>
                <button
                    :class="['checkin-btn', { 'checked': status.todayCheckedIn }]"
                    @click="handleCheckIn"
                    :disabled="status.todayCheckedIn || checkingIn"
                >
                    <span v-if="checkingIn" class="btn-loading"></span>
                    <span v-else>{{ status.todayCheckedIn ? '✅ 今日已打卡' : '🎯 立即打卡' }}</span>
                </button>
            </div>

            <div v-if="recentCheckInDates.length > 0" class="recent-dates card">
                <h3 class="recent-title">近期打卡</h3>
                <div class="date-chips">
                    <span
                        v-for="date in recentCheckInDates"
                        :key="date"
                        class="date-chip"
                    >{{ formatShortDate(date) }}</span>
                </div>
            </div>
        </div>

        <CheckInCalendar
            ref="calendarRef"
            :currentStreak="status.currentStreak || 0"
        />

        <AchievementWall
            :achievements="status.achievements || []"
            :lockedAchievements="status.lockedAchievements || []"
            :currentStreak="status.currentStreak || 0"
        />

        <div v-if="showNewBadge" class="modal-overlay" @click.self="closeBadgeModal">
            <div class="badge-modal card">
                <div class="badge-modal-glow"></div>
                <div class="badge-modal-content">
                    <span class="badge-modal-icon">{{ newBadgeInfo.icon }}</span>
                    <h2 class="badge-modal-title">🎉 成就解锁！</h2>
                    <h3 class="badge-modal-name">{{ newBadgeInfo.name }}</h3>
                    <p class="badge-modal-desc">{{ newBadgeInfo.desc }}</p>
                    <button class="btn btn-primary" @click="closeBadgeModal">太棒了！</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { checkInApi, careLogApi, EventBus } from '../api'
import { useUserStore } from '../stores/user'
import CheckInCalendar from '../components/CheckInCalendar.vue'
import AchievementWall from '../components/AchievementWall.vue'

const userStore = useUserStore()
const status = ref({})
const checkingIn = ref(false)
const calendarRef = ref(null)
const todayCareCount = ref(0)
const showNewBadge = ref(false)
const newBadgeInfo = ref({ icon: '', name: '', desc: '' })

const recentCheckInDates = computed(() => status.value.recentCheckInDates || [])

function formatShortDate(dateStr) {
    if (!dateStr) return ''
    const d = new Date(dateStr)
    return `${d.getMonth() + 1}/${d.getDate()}`
}

async function loadStatus() {
    try {
        const res = await checkInApi.getStatus(userStore.currentUser.id)
        status.value = res.data
    } catch (e) {
        console.error('加载打卡状态失败', e)
        status.value = {
            todayCheckedIn: false,
            currentStreak: 0,
            maxStreak: 0,
            totalCheckInDays: 0,
            recentCheckInDates: [],
            achievements: [],
            lockedAchievements: [
                { badgeType: 'STREAK_7', badgeName: '🌱 初心萌芽', badgeIcon: '🌱', streakDays: 7, description: '7天坚持，绿植养护之路已启程', unlocked: false },
                { badgeType: 'STREAK_30', badgeName: '🌿 生机盎然', badgeIcon: '🌿', streakDays: 30, description: '30天不懈，你的绿植茁壮成长', unlocked: false },
                { badgeType: 'STREAK_100', badgeName: '🌳 绿意满园', badgeIcon: '🌳', streakDays: 100, description: '100天守护，你是真正的绿植守护者', unlocked: false }
            ]
        }
    }
}

async function loadTodayCareCount() {
    try {
        const res = await careLogApi.getByUser(userStore.currentUser.id)
        const today = new Date().toISOString().split('T')[0]
        todayCareCount.value = (res.data || []).filter(log => log.logDate === today).length
    } catch (e) {
        console.error('加载今日养护数失败', e)
        todayCareCount.value = 0
    }
}

async function handleCheckIn() {
    if (status.value.todayCheckedIn || checkingIn.value) return
    checkingIn.value = true

    try {
        const prevAchievements = (status.value.achievements || []).length
        await checkInApi.checkIn({ userId: userStore.currentUser.id })
        await loadStatus()
        const newAchievements = (status.value.achievements || []).length

        if (newAchievements > prevAchievements) {
            const newest = status.value.achievements[status.value.achievements.length - 1]
            newBadgeInfo.value = {
                icon: newest.badgeIcon || '🏅',
                name: newest.badgeName,
                desc: newest.description
            }
            showNewBadge.value = true
        }

        if (calendarRef.value) {
            calendarRef.value.loadCalendar()
        }
    } catch (e) {
        console.error('打卡失败', e)
        alert(e.response?.data?.message || '打卡失败，请稍后重试')
    } finally {
        checkingIn.value = false
    }
}

function closeBadgeModal() {
    showNewBadge.value = false
}

const handleCareDataUpdated = () => {
    loadStatus()
    loadTodayCareCount()
    if (calendarRef.value) {
        calendarRef.value.loadCalendar()
    }
}

onMounted(() => {
    loadStatus()
    loadTodayCareCount()
    EventBus.on('careLog:updated', handleCareDataUpdated)
    EventBus.on('careReminder:completed', handleCareDataUpdated)
})

onUnmounted(() => {
    EventBus.off('careLog:updated', handleCareDataUpdated)
    EventBus.off('careReminder:completed', handleCareDataUpdated)
})
</script>

<style scoped>
.checkin-page {
    max-width: 900px;
    margin: 0 auto;
}

.page-title {
    font-size: 28px;
    color: #1b5e20;
    margin-bottom: 24px;
    font-weight: 600;
}

.status-section {
    display: grid;
    grid-template-columns: 1fr auto;
    gap: 20px;
    margin-bottom: 24px;
    align-items: start;
}

.streak-card {
    padding: 28px;
    background: linear-gradient(135deg, #ffffff 0%, #f1f8e9 100%);
}

.streak-main {
    display: flex;
    align-items: baseline;
    gap: 8px;
    margin-bottom: 20px;
}

.streak-number-wrapper {
    display: flex;
    align-items: baseline;
    gap: 4px;
}

.streak-number {
    font-size: 56px;
    font-weight: 700;
    color: #1b5e20;
    line-height: 1;
}

.streak-unit {
    font-size: 20px;
    color: #388e3c;
    font-weight: 500;
}

.streak-label {
    font-size: 16px;
    color: #666;
}

.streak-stats {
    display: flex;
    align-items: center;
    gap: 24px;
    margin-bottom: 24px;
    padding: 16px;
    background: rgba(255, 255, 255, 0.6);
    border-radius: 12px;
}

.stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
}

.stat-value {
    font-size: 24px;
    font-weight: 600;
    color: #2e7d32;
}

.stat-label {
    font-size: 12px;
    color: #888;
}

.stat-divider {
    width: 1px;
    height: 36px;
    background: #c5e1a5;
}

.checkin-btn {
    width: 100%;
    padding: 16px;
    border: none;
    border-radius: 12px;
    font-size: 18px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s;
    background: linear-gradient(135deg, #4caf50 0%, #2e7d32 100%);
    color: white;
    box-shadow: 0 4px 16px rgba(76, 175, 80, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
}

.checkin-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 24px rgba(76, 175, 80, 0.5);
}

.checkin-btn:active:not(:disabled) {
    transform: translateY(0);
}

.checkin-btn.checked {
    background: linear-gradient(135deg, #81c784 0%, #66bb6a 100%);
    cursor: default;
    box-shadow: none;
}

.checkin-btn:disabled {
    cursor: default;
}

.btn-loading {
    width: 20px;
    height: 20px;
    border: 3px solid rgba(255, 255, 255, 0.3);
    border-top-color: white;
    border-radius: 50%;
    animation: spin 0.6s linear infinite;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

.recent-dates {
    padding: 20px;
    min-width: 200px;
}

.recent-title {
    font-size: 14px;
    color: #1b5e20;
    margin: 0 0 12px 0;
    font-weight: 600;
}

.date-chips {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.date-chip {
    display: inline-block;
    padding: 6px 12px;
    background: #e8f5e9;
    color: #2e7d32;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
    text-align: center;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
    padding: 20px;
}

.badge-modal {
    position: relative;
    width: 100%;
    max-width: 400px;
    padding: 40px;
    text-align: center;
    overflow: hidden;
    border: 2px solid #ffd54f;
}

.badge-modal-glow {
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 215, 0, 0.1) 0%, transparent 50%);
    animation: badgeGlow 2s ease-in-out infinite alternate;
}

@keyframes badgeGlow {
    from { opacity: 0.5; }
    to { opacity: 1; }
}

.badge-modal-content {
    position: relative;
    z-index: 1;
}

.badge-modal-icon {
    font-size: 64px;
    display: block;
    margin-bottom: 16px;
    animation: bounce 0.6s ease;
}

@keyframes bounce {
    0%, 100% { transform: scale(1); }
    50% { transform: scale(1.2); }
}

.badge-modal-title {
    font-size: 24px;
    color: #f57f17;
    margin-bottom: 8px;
}

.badge-modal-name {
    font-size: 20px;
    color: #1b5e20;
    margin-bottom: 8px;
}

.badge-modal-desc {
    font-size: 14px;
    color: #666;
    margin-bottom: 24px;
    line-height: 1.6;
}

@media (max-width: 768px) {
    .status-section {
        grid-template-columns: 1fr;
    }

    .streak-number {
        font-size: 42px;
    }

    .streak-stats {
        gap: 16px;
    }

    .stat-value {
        font-size: 20px;
    }

    .recent-dates {
        min-width: auto;
    }

    .date-chips {
        flex-direction: row;
        flex-wrap: wrap;
    }
}
</style>

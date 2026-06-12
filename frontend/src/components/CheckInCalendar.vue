<template>
    <div class="checkin-calendar card">
        <div class="calendar-header">
            <div class="calendar-title-row">
                <h2 class="calendar-title">📅 打卡日历</h2>
            </div>
            <div class="calendar-nav">
                <button class="nav-btn" @click="prevMonth">←</button>
                <span class="month-label">{{ currentYear }}年{{ currentMonth + 1 }}月</span>
                <button class="nav-btn" @click="nextMonth">→</button>
                <button class="today-btn" @click="goToToday">今天</button>
                <span class="month-count">本月打卡 {{ monthCheckInCount }} 天</span>
            </div>
        </div>

        <div class="weekdays">
            <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
        </div>

        <div class="calendar-grid">
            <div
                v-for="(cell, index) in calendarCells"
                :key="index"
                :class="['day-cell', {
                    'other-month': !cell.inCurrentMonth,
                    'today': cell.isToday,
                    'checked-in': cell.isCheckedIn,
                    'streak-day': cell.isStreakDay
                }]"
            >
                <span class="day-number">{{ cell.day }}</span>
                <div v-if="cell.isCheckedIn" class="checkin-indicator">
                    <span class="checkin-dot">✓</span>
                </div>
            </div>
        </div>

        <div class="calendar-legend">
            <span class="legend-item">
                <span class="legend-dot checked-dot"></span>已打卡
            </span>
            <span class="legend-item">
                <span class="legend-dot today-dot"></span>今天
            </span>
            <span class="legend-item">
                <span class="legend-dot streak-dot"></span>连续打卡
            </span>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { checkInApi } from '../api'
import { useUserStore } from '../stores/user'

const props = defineProps({
    currentStreak: {
        type: Number,
        default: 0
    }
})

const userStore = useUserStore()
const today = new Date()
const currentYear = ref(today.getFullYear())
const currentMonth = ref(today.getMonth())
const checkInDates = ref([])
const monthCheckInCount = ref(0)

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

function toLocalDateString(date) {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
}

function formatDateStr(year, month, day) {
    const dt = new Date(year, month, day)
    return toLocalDateString(dt)
}

const checkedInDateSet = computed(() => new Set(checkInDates.value))

const streakDates = computed(() => {
    const dates = new Set()
    if (props.currentStreak <= 0) return dates
    const todayStr = toLocalDateString(today)
    for (let i = 0; i < props.currentStreak; i++) {
        const d = new Date(today)
        d.setDate(d.getDate() - i)
        dates.add(toLocalDateString(d))
    }
    return dates
})

const calendarCells = computed(() => {
    const cells = []
    const firstDay = new Date(currentYear.value, currentMonth.value, 1)
    const lastDay = new Date(currentYear.value, currentMonth.value + 1, 0)
    const startWeekday = firstDay.getDay()
    const totalDays = lastDay.getDate()

    const prevMonthLastDay = new Date(currentYear.value, currentMonth.value, 0).getDate()
    for (let i = startWeekday - 1; i >= 0; i--) {
        const day = prevMonthLastDay - i
        cells.push({ day, inCurrentMonth: false, isToday: false, isCheckedIn: false, isStreakDay: false })
    }

    for (let day = 1; day <= totalDays; day++) {
        const dateStr = formatDateStr(currentYear.value, currentMonth.value, day)
        cells.push({
            day,
            inCurrentMonth: true,
            isToday: dateStr === toLocalDateString(today),
            isCheckedIn: checkedInDateSet.value.has(dateStr),
            isStreakDay: streakDates.value.has(dateStr)
        })
    }

    const remaining = 42 - cells.length
    for (let day = 1; day <= remaining; day++) {
        cells.push({ day, inCurrentMonth: false, isToday: false, isCheckedIn: false, isStreakDay: false })
    }

    return cells
})

function prevMonth() {
    if (currentMonth.value === 0) {
        currentMonth.value = 11
        currentYear.value--
    } else {
        currentMonth.value--
    }
}

function nextMonth() {
    if (currentMonth.value === 11) {
        currentMonth.value = 0
        currentYear.value++
    } else {
        currentMonth.value++
    }
}

function goToToday() {
    currentYear.value = today.getFullYear()
    currentMonth.value = today.getMonth()
}

async function loadCalendar() {
    try {
        const res = await checkInApi.getCalendar(userStore.currentUser.id, currentYear.value, currentMonth.value + 1)
        checkInDates.value = res.data.checkInDates || []
        monthCheckInCount.value = res.data.monthCheckInCount || 0
    } catch (e) {
        console.error('加载打卡日历失败', e)
    }
}

watch([currentYear, currentMonth], () => {
    loadCalendar()
})

onMounted(() => {
    loadCalendar()
})

defineExpose({ loadCalendar })
</script>

<style scoped>
.checkin-calendar {
    padding: 24px;
    margin-bottom: 24px;
}

.calendar-header {
    margin-bottom: 20px;
}

.calendar-title-row {
    margin-bottom: 12px;
}

.calendar-title {
    font-size: 20px;
    color: #1b5e20;
    margin: 0;
}

.calendar-nav {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
}

.nav-btn {
    width: 32px;
    height: 32px;
    border: 1px solid #c5e1a5;
    background: white;
    border-radius: 6px;
    cursor: pointer;
    font-size: 16px;
    color: #388e3c;
    transition: all 0.2s;
}

.nav-btn:hover {
    background: #f1f8e9;
    border-color: #4caf50;
}

.month-label {
    font-size: 16px;
    font-weight: 600;
    color: #1b5e20;
    min-width: 120px;
    text-align: center;
}

.today-btn {
    padding: 6px 14px;
    border: 1px solid #4caf50;
    background: white;
    border-radius: 6px;
    cursor: pointer;
    font-size: 13px;
    color: #4caf50;
    transition: all 0.2s;
}

.today-btn:hover {
    background: #f1f8e9;
}

.month-count {
    margin-left: auto;
    font-size: 14px;
    color: #666;
    background: #f1f8e9;
    padding: 4px 12px;
    border-radius: 12px;
}

.weekdays {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 4px;
    margin-bottom: 8px;
}

.weekday {
    text-align: center;
    font-size: 13px;
    font-weight: 600;
    color: #689f38;
    padding: 8px 0;
}

.calendar-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 4px;
}

.day-cell {
    min-height: 56px;
    padding: 8px;
    background: #fafafa;
    border-radius: 8px;
    border: 1px solid transparent;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
}

.day-cell.other-month {
    background: #f5f5f5;
    opacity: 0.4;
}

.day-cell.today {
    background: #e8f5e9;
    border-color: #81c784;
    border-width: 2px;
}

.day-cell.today .day-number {
    color: #2e7d32;
    font-weight: 700;
}

.day-cell.checked-in {
    background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
    border-color: #66bb6a;
}

.day-cell.streak-day {
    background: linear-gradient(135deg, #fff8e1 0%, #ffecb3 100%);
    border-color: #ffd54f;
    box-shadow: 0 2px 8px rgba(255, 213, 79, 0.3);
}

.day-number {
    font-size: 14px;
    font-weight: 500;
    color: #33691e;
}

.checkin-indicator {
    margin-top: 4px;
}

.checkin-dot {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: #4caf50;
    color: white;
    font-size: 11px;
    font-weight: bold;
}

.calendar-legend {
    display: flex;
    gap: 20px;
    margin-top: 16px;
    padding: 10px 12px;
    background: #f9fbe7;
    border-radius: 8px;
    flex-wrap: wrap;
}

.legend-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #558b2f;
}

.legend-dot {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    display: inline-block;
}

.checked-dot {
    background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
    border: 2px solid #66bb6a;
}

.today-dot {
    background: #e8f5e9;
    border: 2px solid #81c784;
}

.streak-dot {
    background: linear-gradient(135deg, #fff8e1 0%, #ffecb3 100%);
    border: 2px solid #ffd54f;
}

@media (max-width: 768px) {
    .checkin-calendar {
        padding: 16px;
    }

    .day-cell {
        min-height: 44px;
        padding: 4px;
    }

    .day-number {
        font-size: 12px;
    }

    .checkin-dot {
        width: 16px;
        height: 16px;
        font-size: 9px;
    }

    .month-count {
        margin-left: 0;
        width: 100%;
        text-align: center;
    }
}
</style>

<template>
    <div class="calendar-wrapper card">
        <div class="calendar-header">
            <div class="calendar-title-row">
                <h2 class="calendar-title">📅 养护日历</h2>
                <div class="view-toggle">
                    <button
                        :class="['toggle-btn', filterMode === 'all' ? 'active' : '']"
                        @click="filterMode = 'all'"
                    >全部植物</button>
                    <button
                        :class="['toggle-btn', filterMode === 'single' ? 'active' : '']"
                        @click="filterMode = 'single'"
                    >单株植物</button>
                </div>
            </div>
            <div v-if="filterMode === 'single' && plantList.length > 0" class="plant-select-row">
                <select v-model="selectedPlantId" class="plant-select">
                    <option v-for="plant in plantList" :key="plant.id" :value="plant.id">
                        {{ plant.customName || plant.plantCategory?.name || '未命名' }}
                    </option>
                </select>
            </div>
            <div class="calendar-nav">
                <button class="nav-btn" @click="prevMonth">←</button>
                <span class="month-label">{{ currentYear }}年{{ currentMonth + 1 }}月</span>
                <button class="nav-btn" @click="nextMonth">→</button>
                <button class="today-btn" @click="goToToday">今天</button>
            </div>
        </div>

        <div class="legend">
            <span class="legend-item">
                <span class="dot dot-water"></span>浇水
            </span>
            <span class="legend-item">
                <span class="dot dot-fertilize"></span>施肥
            </span>
            <span class="legend-item">
                <span class="dot dot-prune"></span>修剪
            </span>
            <span class="legend-item">
                <span class="dot dot-other"></span>其他
            </span>
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
                    'has-logs': cell.logs.length > 0,
                    'selected': selectedDate && cell.dateStr === selectedDate
                }]"
                @click="cell.inCurrentMonth && openDayDetail(cell)"
            >
                <span class="day-number">{{ cell.day }}</span>
                <div class="dots-row">
                    <span
                        v-for="(dot, dIdx) in cell.dots"
                        :key="dIdx"
                        :class="['dot', `dot-${dot.type}`]"
                        :title="dot.label"
                    ></span>
                    <span v-if="cell.moreCount > 0" class="more-count">+{{ cell.moreCount }}</span>
                </div>
            </div>
        </div>

        <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDayDetail">
            <div class="detail-modal card">
                <div class="modal-header">
                    <h3>{{ formatFullDate(selectedDate) }}</h3>
                    <button class="close-btn" @click="closeDayDetail">×</button>
                </div>
                <div v-if="selectedDayLogs.length === 0" class="empty-day">
                    <p>当日暂无养护记录</p>
                </div>
                <div v-else class="log-detail-list">
                    <div v-for="log in selectedDayLogs" :key="log.id" class="log-detail-item">
                        <div class="log-detail-header">
                            <span :class="['type-badge', `type-${getTypeKey(log.operationType)}`]">
                                {{ getOperationTypeLabel(log.operationType) }}
                            </span>
                            <span v-if="filterMode === 'all'" class="log-plant-name">
                                {{ getPlantName(log.plantArchiveId) }}
                            </span>
                        </div>
                        <p v-if="log.details" class="log-detail-content">{{ log.details }}</p>
                        <div class="log-detail-meta">
                            <span v-if="log.growthStatus" class="meta-item">🌱 {{ log.growthStatus }}</span>
                            <span v-if="log.diseaseStatus" class="meta-item">⚠️ {{ log.diseaseStatus }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { careLogApi } from '../api'
import { useUserStore } from '../stores/user'

const props = defineProps({
    archives: {
        type: Array,
        default: () => []
    }
})

const userStore = useUserStore()

const today = new Date()
const currentYear = ref(today.getFullYear())
const currentMonth = ref(today.getMonth())

const filterMode = ref('all')
const selectedPlantId = ref(null)
const allLogs = ref([])
const showDetailModal = ref(false)
const selectedDate = ref(null)

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const plantList = computed(() => props.archives || [])

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

function getTypeKey(type) {
    const map = {
        '浇水': 'water',
        '施肥': 'fertilize',
        '修剪': 'prune'
    }
    return map[type] || 'other'
}

function getOperationTypeLabel(type) {
    const labels = {
        '浇水': '💧 浇水',
        '施肥': '🧪 施肥',
        '修剪': '✂️ 修剪',
        '换盆': '🪴 换盆',
        '其他': '📝 其他'
    }
    return labels[type] || type
}

function getPlantName(plantId) {
    const plant = plantList.value.find(p => p.id === plantId)
    return plant ? (plant.customName || plant.plantCategory?.name || '未命名') : '未知植物'
}

function formatFullDate(dateStr) {
    if (!dateStr) return ''
    const [y, m, d] = dateStr.split('-').map(Number)
    const dt = new Date(y, m - 1, d)
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return `${dt.getFullYear()}年${dt.getMonth() + 1}月${dt.getDate()}日 ${weekdays[dt.getDay()]}`
}

function createCell(day, dateStr, inCurrentMonth) {
    const logs = filteredLogs.value.filter(log => log.logDate === dateStr)
    const isToday = dateStr === toLocalDateString(today)

    const typeSet = new Set()
    const dots = []
    for (const log of logs) {
        const typeKey = getTypeKey(log.operationType)
        if (!typeSet.has(typeKey)) {
            typeSet.add(typeKey)
            if (dots.length < 3) {
                dots.push({ type: typeKey, label: log.operationType })
            }
        }
    }
    const moreCount = logs.length > 3 ? logs.length - 3 : 0

    return { day, dateStr, inCurrentMonth, isToday, logs, dots, moreCount }
}

watch([() => plantList.value, filterMode], ([newList, newMode]) => {
    if (newMode === 'single' && newList && newList.length > 0 && !selectedPlantId.value) {
        selectedPlantId.value = newList[0].id
    }
}, { immediate: true })

watch([filterMode, selectedPlantId], () => {
    closeDayDetail()
})

watch([currentYear, currentMonth], () => {
    closeDayDetail()
})

const filteredLogs = computed(() => {
    if (filterMode.value === 'single' && selectedPlantId.value) {
        return allLogs.value.filter(l => l.plantArchiveId === selectedPlantId.value)
    }
    return allLogs.value
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
        const dateStr = formatDateStr(currentYear.value, currentMonth.value - 1, day)
        cells.push(createCell(day, dateStr, false))
    }

    for (let day = 1; day <= totalDays; day++) {
        const dateStr = formatDateStr(currentYear.value, currentMonth.value, day)
        cells.push(createCell(day, dateStr, true))
    }

    const remaining = 42 - cells.length
    for (let day = 1; day <= remaining; day++) {
        const dateStr = formatDateStr(currentYear.value, currentMonth.value + 1, day)
        cells.push(createCell(day, dateStr, false))
    }

    return cells
})

const selectedDayLogs = computed(() => {
    if (!selectedDate.value) return []
    return filteredLogs.value.filter(log => log.logDate === selectedDate.value)
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

function openDayDetail(cell) {
    selectedDate.value = cell.dateStr
    showDetailModal.value = true
}

function closeDayDetail() {
    showDetailModal.value = false
    selectedDate.value = null
}

async function loadLogs() {
    try {
        const res = await careLogApi.getByUser(userStore.currentUser.id)
        allLogs.value = res.data || []
    } catch (e) {
        console.error('加载养护记录失败', e)
        allLogs.value = generateMockLogs()
    }
}

function generateMockLogs() {
    const mock = []
    const operations = ['浇水', '施肥', '修剪', '换盆', '其他']
    const details = [
        '浇透水，土壤完全湿润',
        '施用稀释后的液体肥',
        '剪掉枯黄的老叶',
        '换了更大的花盆和新土',
        '观察生长情况'
    ]
    const now = new Date()
    for (let i = 0; i < 25; i++) {
        const d = new Date(now)
        d.setDate(d.getDate() - Math.floor(Math.random() * 60))
        const opIdx = Math.floor(Math.random() * operations.length)
        const plantId = plantList.value.length > 0
            ? plantList.value[Math.floor(Math.random() * plantList.value.length)].id
            : 1
        mock.push({
            id: 1000 + i,
            plantArchiveId: plantId,
            userId: userStore.currentUser.id,
            logDate: toLocalDateString(d),
            operationType: operations[opIdx],
            details: details[opIdx],
            growthStatus: Math.random() > 0.5 ? '长势良好' : null,
            diseaseStatus: null
        })
    }
    return mock
}

onMounted(() => {
    loadLogs()
})

defineExpose({ loadLogs })
</script>

<style scoped>
.calendar-wrapper {
    padding: 24px;
    margin-bottom: 24px;
}

.calendar-header {
    margin-bottom: 20px;
}

.calendar-title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    flex-wrap: wrap;
    gap: 12px;
}

.calendar-title {
    font-size: 20px;
    color: #1b5e20;
    margin: 0;
}

.view-toggle {
    display: flex;
    background: #e8f5e9;
    border-radius: 8px;
    padding: 4px;
}

.toggle-btn {
    padding: 6px 14px;
    border: none;
    background: transparent;
    border-radius: 6px;
    cursor: pointer;
    font-size: 13px;
    color: #388e3c;
    transition: all 0.2s;
}

.toggle-btn.active {
    background: #4caf50;
    color: white;
    box-shadow: 0 2px 6px rgba(76, 175, 80, 0.3);
}

.plant-select-row {
    margin-bottom: 12px;
}

.plant-select {
    padding: 8px 12px;
    border: 1px solid #c5e1a5;
    border-radius: 8px;
    font-size: 14px;
    background: white;
    color: #1b5e20;
    cursor: pointer;
}

.plant-select:focus {
    outline: none;
    border-color: #4caf50;
}

.calendar-nav {
    display: flex;
    align-items: center;
    gap: 12px;
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

.legend {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;
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

.dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    display: inline-block;
    flex-shrink: 0;
}

.dot-water {
    background: #2196f3;
}

.dot-fertilize {
    background: #4caf50;
}

.dot-prune {
    background: #ff9800;
}

.dot-other {
    background: #9e9e9e;
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
    min-height: 80px;
    padding: 8px;
    background: #fafafa;
    border-radius: 8px;
    border: 1px solid transparent;
    cursor: pointer;
    transition: all 0.2s;
    display: flex;
    flex-direction: column;
}

.day-cell:hover {
    background: #f1f8e9;
    border-color: #c5e1a5;
}

.day-cell.other-month {
    background: #f5f5f5;
    opacity: 0.5;
    cursor: default;
}

.day-cell.other-month:hover {
    background: #f5f5f5;
    border-color: transparent;
}

.day-cell.today {
    background: #e8f5e9;
    border-color: #81c784;
}

.day-cell.has-logs {
    background: #ffffff;
}

.day-cell.selected {
    background: #c8e6c9;
    border-color: #4caf50;
    box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
}

.day-number {
    font-size: 14px;
    font-weight: 500;
    color: #33691e;
    margin-bottom: 4px;
}

.day-cell.today .day-number {
    color: #2e7d32;
    font-weight: 700;
}

.dots-row {
    display: flex;
    align-items: center;
    gap: 4px;
    flex-wrap: wrap;
    margin-top: auto;
}

.dots-row .dot {
    width: 6px;
    height: 6px;
}

.more-count {
    font-size: 11px;
    color: #888;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
    padding: 20px;
}

.detail-modal {
    width: 100%;
    max-width: 520px;
    max-height: 80vh;
    overflow-y: auto;
    padding: 0;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-bottom: 1px solid #e0e0e0;
}

.modal-header h3 {
    color: #1b5e20;
    margin: 0;
    font-size: 18px;
}

.close-btn {
    width: 32px;
    height: 32px;
    border: none;
    background: #f5f5f5;
    border-radius: 50%;
    cursor: pointer;
    font-size: 20px;
    color: #666;
    line-height: 1;
    transition: all 0.2s;
}

.close-btn:hover {
    background: #e0e0e0;
    color: #333;
}

.empty-day {
    text-align: center;
    padding: 40px 20px;
    color: #888;
}

.log-detail-list {
    padding: 20px 24px;
}

.log-detail-item {
    padding: 14px 0;
    border-bottom: 1px solid #f0f0f0;
}

.log-detail-item:last-child {
    border-bottom: none;
}

.log-detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    gap: 12px;
    flex-wrap: wrap;
}

.type-badge {
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 13px;
    font-weight: 500;
}

.type-water {
    background: #e3f2fd;
    color: #1976d2;
}

.type-fertilize {
    background: #e8f5e9;
    color: #388e3c;
}

.type-prune {
    background: #fff3e0;
    color: #f57c00;
}

.type-other {
    background: #f5f5f5;
    color: #616161;
}

.log-plant-name {
    font-size: 13px;
    color: #666;
}

.log-detail-content {
    color: #444;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 8px;
}

.log-detail-meta {
    display: flex;
    gap: 16px;
    font-size: 12px;
    color: #888;
    flex-wrap: wrap;
}

.meta-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
}

@media (max-width: 768px) {
    .calendar-wrapper {
        padding: 16px;
    }

    .calendar-title-row {
        flex-direction: column;
        align-items: flex-start;
    }

    .day-cell {
        min-height: 60px;
        padding: 4px;
    }

    .day-number {
        font-size: 12px;
    }

    .dots-row .dot {
        width: 5px;
        height: 5px;
    }

    .more-count {
        font-size: 10px;
    }
}
</style>

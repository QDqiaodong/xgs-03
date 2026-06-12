<template>
    <div v-if="archive" class="archive-detail">
        <button class="btn btn-secondary back-btn" @click="$router.back()">← 返回</button>

        <div class="detail-header card">
            <div class="detail-image">
                <LazyImage
                    :src="archive.imageUrl || getDefaultImage()"
                    :alt="archive.customName"
                    height="250"
                    mode="detail"
                />
            </div>
            <div class="detail-info">
                <h1>{{ archive.customName || '未命名' }}</h1>
                <p class="plant-category">{{ archive.plantCategory?.name || '未知品种' }}</p>
                <div class="info-grid">
                    <div class="info-item">
                        <span class="label">📍 摆放位置</span>
                        <span>{{ archive.location || '未设置' }}</span>
                    </div>
                    <div class="info-item">
                        <span class="label">🏠 摆放环境</span>
                        <span>{{ archive.environment || '未设置' }}</span>
                    </div>
                    <div class="info-item">
                        <span class="label">📅 购入时间</span>
                        <span>{{ formatDate(archive.purchaseDate) }}</span>
                    </div>
                </div>
                <div v-if="archive.reminderEnabled" class="reminder-info">
                    <span class="mini-tag">💧 每{{ archive.waterInterval }}天浇水</span>
                    <span class="mini-tag">🧪 每{{ archive.fertilizeInterval }}天施肥</span>
                </div>
                <div class="actions">
                    <button class="btn btn-primary" @click="showAddLogModal = true">+ 记录养护</button>
                </div>
            </div>
        </div>

        <div v-if="archive.notes" class="notes-section card">
            <h3>📝 备注</h3>
            <p>{{ archive.notes }}</p>
        </div>

        <div class="filter-section card">
            <div class="filter-header">
                <h3 class="filter-title">🔍 高级筛选</h3>
                <button
                    class="btn btn-toggle-filter"
                    @click="showFilterPanel = !showFilterPanel"
                >
                    {{ showFilterPanel ? '收起 ▲' : '展开 ▼' }}
                </button>
            </div>

            <div v-if="activeFilterTags.length > 0" class="filter-tags">
                <span
                    v-for="tag in activeFilterTags"
                    :key="tag.key"
                    class="filter-tag"
                >
                    <span class="tag-label">{{ tag.label }}</span>
                    <button class="tag-close" @click="removeFilter(tag.key)">×</button>
                </span>
                <button v-if="activeFilterTags.length > 1" class="btn-clear-all" @click="clearAllFilters">
                    清除全部
                </button>
            </div>

            <div v-if="showFilterPanel" class="filter-panel">
                <div class="filter-row">
                    <div class="filter-item">
                        <label class="filter-label">操作类型（可多选）</label>
                        <div class="operation-checkboxes">
                            <label
                                v-for="op in operationOptions"
                                :key="op.value"
                                class="checkbox-item"
                                :class="{ checked: filterOperationTypes.includes(op.value) }"
                            >
                                <input
                                    type="checkbox"
                                    :value="op.value"
                                    v-model="filterOperationTypes"
                                    class="hidden-checkbox"
                                />
                                <span class="checkbox-custom">
                                    <span class="op-icon">{{ op.icon }}</span>
                                    <span class="op-label">{{ op.label }}</span>
                                </span>
                            </label>
                        </div>
                    </div>
                </div>

                <div class="filter-row">
                    <div class="filter-item filter-date">
                        <label class="filter-label">开始日期</label>
                        <input
                            type="date"
                            v-model="filterStartDate"
                            class="filter-input"
                        />
                    </div>
                    <div class="filter-item filter-date">
                        <label class="filter-label">结束日期</label>
                        <input
                            type="date"
                            v-model="filterEndDate"
                            class="filter-input"
                        />
                    </div>
                    <div class="filter-item filter-keyword">
                        <label class="filter-label">关键词搜索</label>
                        <div class="keyword-input-wrapper">
                            <input
                                type="text"
                                v-model="filterKeyword"
                                placeholder="搜索详情、长势、病害..."
                                class="filter-input keyword-input"
                                @keyup.enter="applyKeywordFilter"
                            />
                            <button
                                v-if="filterKeywordPending"
                                class="btn-search"
                                @click="applyKeywordFilter"
                            >
                                搜索
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <PlantAlbum
            ref="albumRef"
            :plantArchiveId="route.params.id"
            @cover-changed="handleCoverChanged"
        />

        <GrowthTimeline
            ref="timelineRef"
            :plantId="route.params.id"
            :fetchLogs="fetchLogsPaged"
            :pageSize="5"
            :filters="currentFilters"
        />

        <div v-if="showAddLogModal" class="modal-overlay" @click.self="showAddLogModal = false">
            <div class="modal card">
                <h2>📝 记录养护操作</h2>
                <div class="form-group">
                    <label>记录日期</label>
                    <input type="date" v-model="newLog.logDate" />
                </div>
                <div class="form-group">
                    <label>操作类型</label>
                    <select v-model="newLog.operationType">
                        <option value="浇水">浇水</option>
                        <option value="施肥">施肥</option>
                        <option value="修剪">修剪</option>
                        <option value="换盆">换盆</option>
                        <option value="其他">其他</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>操作详情</label>
                    <textarea v-model="newLog.details" rows="3" placeholder="记录具体操作内容..."></textarea>
                </div>
                <div class="form-group">
                    <label>长势描述</label>
                    <input type="text" v-model="newLog.growthStatus" placeholder="例如：长势良好，新叶萌发" />
                </div>
                <div class="form-group">
                    <label>病害情况</label>
                    <input type="text" v-model="newLog.diseaseStatus" placeholder="如有异常请描述" />
                </div>
                <div class="modal-actions">
                    <button class="btn btn-secondary" @click="showAddLogModal = false">取消</button>
                    <button class="btn btn-primary" @click="createLog">保存</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { plantArchiveApi, careLogApi, plantPhotoApi } from '../api'
import { useUserStore } from '../stores/user'
import LazyImage from '../components/LazyImage.vue'
import GrowthTimeline from '../components/GrowthTimeline.vue'
import PlantAlbum from '../components/PlantAlbum.vue'

const route = useRoute()
const userStore = useUserStore()
const archive = ref(null)
const showAddLogModal = ref(false)
const timelineRef = ref(null)
const albumRef = ref(null)

const showFilterPanel = ref(true)
const filterOperationTypes = ref([])
const filterStartDate = ref('')
const filterEndDate = ref('')
const filterKeywordInput = ref('')
const filterKeyword = ref('')
const filterKeywordPending = ref(false)

const operationOptions = [
    { value: '浇水', label: '浇水', icon: '💧' },
    { value: '施肥', label: '施肥', icon: '🧪' },
    { value: '修剪', label: '修剪', icon: '✂️' },
    { value: '换盆', label: '换盆', icon: '🪴' },
    { value: '其他', label: '其他', icon: '📝' }
]

const currentFilters = computed(() => ({
    operationTypes: filterOperationTypes.value,
    startDate: filterStartDate.value || undefined,
    endDate: filterEndDate.value || undefined,
    keyword: filterKeyword.value || undefined
}))

const activeFilterTags = computed(() => {
    const tags = []
    filterOperationTypes.value.forEach((type) => {
        const op = operationOptions.find((o) => o.value === type)
        if (op) {
            tags.push({
                key: `op_${type}`,
                label: `${op.icon} ${op.label}`,
                type: 'operation',
                value: type
            })
        }
    })
    if (filterStartDate.value) {
        tags.push({
            key: 'startDate',
            label: `📅 开始: ${filterStartDate.value}`,
            type: 'startDate'
        })
    }
    if (filterEndDate.value) {
        tags.push({
            key: 'endDate',
            label: `📅 结束: ${filterEndDate.value}`,
            type: 'endDate'
        })
    }
    if (filterKeyword.value) {
        tags.push({
            key: 'keyword',
            label: `🔎 "${filterKeyword.value}"`,
            type: 'keyword'
        })
    }
    return tags
})

const removeFilter = (key) => {
    if (key.startsWith('op_')) {
        const value = key.replace('op_', '')
        filterOperationTypes.value = filterOperationTypes.value.filter((t) => t !== value)
    } else if (key === 'startDate') {
        filterStartDate.value = ''
    } else if (key === 'endDate') {
        filterEndDate.value = ''
    } else if (key === 'keyword') {
        filterKeyword.value = ''
        filterKeywordInput.value = ''
        filterKeywordPending.value = false
    }
}

const clearAllFilters = () => {
    filterOperationTypes.value = []
    filterStartDate.value = ''
    filterEndDate.value = ''
    filterKeyword.value = ''
    filterKeywordInput.value = ''
    filterKeywordPending.value = false
}

const applyKeywordFilter = () => {
    filterKeyword.value = filterKeywordInput.value.trim()
    filterKeywordPending.value = false
}

let keywordDebounceTimer = null
watch(filterKeywordInput, (val) => {
    filterKeywordPending.value = val.trim() !== filterKeyword.value
    if (keywordDebounceTimer) {
        clearTimeout(keywordDebounceTimer)
    }
    keywordDebounceTimer = setTimeout(() => {
        filterKeyword.value = val.trim()
        filterKeywordPending.value = false
    }, 500)
})

const toLocalDateStr = (date) => {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
}

const newLog = ref({
    logDate: toLocalDateStr(new Date()),
    operationType: '浇水',
    details: '',
    growthStatus: '',
    diseaseStatus: ''
})

const getDefaultImage = () => {
    return 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=green%20potted%20plant&image_size=landscape_4_3'
}

const formatDate = (date) => {
    if (!date) return '未知日期'
    if (typeof date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(date)) {
        const [y, m, d] = date.split('-').map(Number)
        return new Date(y, m - 1, d).toLocaleDateString('zh-CN')
    }
    return new Date(date).toLocaleDateString('zh-CN')
}

const fetchLogsPaged = async (plantId, page, size, filters = {}) => {
    try {
        const hasFilters =
            (filters.operationTypes && filters.operationTypes.length > 0) ||
            filters.startDate ||
            filters.endDate ||
            filters.keyword
        if (hasFilters) {
            const res = await careLogApi.getByPlantFiltered(plantId, page, size, filters)
            return res.data
        } else {
            const res = await careLogApi.getByPlantPaged(plantId, page, size)
            return res.data
        }
    } catch (e) {
        console.error('加载日志失败', e)
        throw e
    }
}

const createLog = async () => {
    try {
        const data = {
            ...newLog.value,
            plantArchiveId: route.params.id,
            userId: userStore.currentUser.id
        }
        await careLogApi.create(data)
        showAddLogModal.value = false
        newLog.value = {
            logDate: toLocalDateStr(new Date()),
            operationType: '浇水',
            details: '',
            growthStatus: '',
            diseaseStatus: ''
        }
        if (timelineRef.value) {
            timelineRef.value.resetAndLoad()
        }
    } catch (e) {
        console.error('创建失败', e)
        showAddLogModal.value = false
        if (timelineRef.value) {
            timelineRef.value.resetAndLoad()
        }
    }
}

const handleCoverChanged = async (coverPhoto) => {
    if (archive.value && coverPhoto) {
        archive.value.imageUrl = coverPhoto.imageUrl
    }
}

onMounted(async () => {
    try {
        const res = await plantArchiveApi.getById(route.params.id)
        archive.value = res.data
        if (!archive.value.imageUrl) {
            try {
                const coverRes = await plantPhotoApi.getCover(route.params.id)
                if (coverRes.data) {
                    archive.value.imageUrl = coverRes.data.imageUrl
                }
            } catch (coverErr) {
                console.log('No cover photo found', coverErr)
            }
        }
    } catch (e) {
        console.error('加载失败', e)
        archive.value = {
            id: route.params.id,
            customName: '小绿',
            plantCategory: { name: '绿萝' },
            location: '客厅窗台',
            environment: '室内向阳',
            purchaseDate: '2024-01-15',
            reminderEnabled: true,
            waterInterval: 7,
            fertilizeInterval: 30,
            notes: '喜欢散射光，避免阳光直射'
        }
    }
})
</script>

<style scoped>
.back-btn {
    margin-bottom: 20px;
}

.detail-header {
    display: grid;
    grid-template-columns: 1fr 1.5fr;
    gap: 30px;
    padding: 0;
    overflow: hidden;
    margin-bottom: 24px;
}

.detail-image {
    height: 250px;
    overflow: hidden;
}

.detail-info {
    padding: 24px;
}

.detail-info h1 {
    font-size: 28px;
    color: #1b5e20;
    margin-bottom: 8px;
}

.plant-category {
    color: #666;
    margin-bottom: 20px;
}

.info-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    margin-bottom: 20px;
}

.info-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.info-item .label {
    font-size: 12px;
    color: #888;
}

.reminder-info {
    display: flex;
    gap: 8px;
    margin-bottom: 20px;
}

.mini-tag {
    font-size: 12px;
    color: #558b2f;
    background: #f1f8e9;
    padding: 4px 10px;
    border-radius: 12px;
}

.actions {
    display: flex;
    gap: 12px;
}

.notes-section {
    margin-bottom: 24px;
}

.notes-section h3 {
    color: #1b5e20;
    margin-bottom: 12px;
}

.notes-section p {
    color: #555;
    line-height: 1.6;
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

.modal {
    width: 100%;
    max-width: 500px;
    max-height: 90vh;
    overflow-y: auto;
}

.modal h2 {
    color: #1b5e20;
    margin-bottom: 20px;
}

.modal-actions {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
    margin-top: 20px;
}

.filter-section {
    margin-bottom: 24px;
}

.filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.filter-title {
    font-size: 18px;
    color: #1b5e20;
    margin: 0;
    font-weight: 600;
}

.btn-toggle-filter {
    background: none;
    border: 1px solid #a5d6a7;
    color: #388e3c;
    padding: 6px 14px;
    font-size: 13px;
    cursor: pointer;
    border-radius: 6px;
    transition: all 0.2s;
}

.btn-toggle-filter:hover {
    background: #f1f8e9;
    border-color: #4caf50;
}

.filter-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e8f5e9;
}

.filter-tag {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
    color: #1b5e20;
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 500;
    box-shadow: 0 2px 6px rgba(76, 175, 80, 0.15);
    animation: tagIn 0.25s ease;
}

@keyframes tagIn {
    from {
        opacity: 0;
        transform: scale(0.85);
    }
    to {
        opacity: 1;
        transform: scale(1);
    }
}

.tag-label {
    white-space: nowrap;
}

.tag-close {
    background: none;
    border: none;
    color: #2e7d32;
    font-size: 16px;
    font-weight: bold;
    cursor: pointer;
    padding: 0 2px;
    line-height: 1;
    display: flex;
    align-items: center;
    transition: color 0.2s;
}

.tag-close:hover {
    color: #d32f2f;
}

.btn-clear-all {
    background: none;
    border: 1px dashed #bdbdbd;
    color: #757575;
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 12px;
    cursor: pointer;
    transition: all 0.2s;
    margin-left: 8px;
}

.btn-clear-all:hover {
    color: #d32f2f;
    border-color: #ef9a9a;
    background: #ffebee;
}

.filter-panel {
    animation: slideDown 0.3s ease;
}

@keyframes slideDown {
    from {
        opacity: 0;
        transform: translateY(-8px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.filter-row {
    display: grid;
    grid-template-columns: 1fr;
    gap: 16px;
    margin-bottom: 16px;
}

.filter-row:last-child {
    grid-template-columns: 1fr 1fr 1.5fr;
    align-items: end;
}

.filter-label {
    display: block;
    font-weight: 500;
    color: #33691e;
    margin-bottom: 8px;
    font-size: 14px;
}

.operation-checkboxes {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.checkbox-item {
    cursor: pointer;
    user-select: none;
}

.hidden-checkbox {
    position: absolute;
    opacity: 0;
    width: 0;
    height: 0;
}

.checkbox-custom {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border: 2px solid #c5e1a5;
    border-radius: 8px;
    background: white;
    transition: all 0.2s ease;
    font-size: 14px;
}

.checkbox-item:hover .checkbox-custom {
    border-color: #81c784;
    background: #f1f8e9;
}

.checkbox-item.checked .checkbox-custom {
    border-color: #4caf50;
    background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
    box-shadow: 0 2px 8px rgba(76, 175, 80, 0.25);
}

.op-icon {
    font-size: 18px;
}

.op-label {
    font-weight: 500;
    color: #1b5e20;
}

.filter-input {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #c5e1a5;
    border-radius: 8px;
    font-size: 14px;
    transition: all 0.2s;
}

.filter-input:focus {
    outline: none;
    border-color: #4caf50;
    box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
}

.keyword-input-wrapper {
    display: flex;
    gap: 8px;
}

.keyword-input {
    flex: 1;
}

.btn-search {
    padding: 0 16px;
    background: linear-gradient(135deg, #4caf50 0%, #388e3c 100%);
    color: white;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.2s;
}

.btn-search:hover {
    transform: translateY(-1px);
    box-shadow: 0 3px 10px rgba(76, 175, 80, 0.35);
}

@media (max-width: 768px) {
    .detail-header {
        grid-template-columns: 1fr;
    }
    .info-grid {
        grid-template-columns: 1fr;
    }
    .filter-row:last-child {
        grid-template-columns: 1fr;
    }
    .operation-checkboxes {
        gap: 8px;
    }
    .checkbox-custom {
        padding: 6px 12px;
        font-size: 13px;
    }
}
</style>

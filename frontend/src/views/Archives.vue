<template>
    <div>
        <div class="page-header">
            <h1 class="page-title">🪴 我的绿植</h1>
            <button class="btn btn-primary" @click="showAddModal = true">+ 新增绿植</button>
        </div>

        <CareCalendar ref="calendarRef" :archives="archives" />

        <TagManager @tags-updated="handleTagsUpdated" />

        <div v-if="tags.length > 0" class="tag-filter-section card">
            <h3 class="section-title">🔍 按标签筛选</h3>
            <div class="tag-filter-list">
                <button
                    class="tag-filter-btn"
                    :class="{ active: selectedTagIds.length === 0 }"
                    @click="clearTagFilter"
                >
                    全部
                </button>
                <button
                    v-for="tag in tags"
                    :key="tag.id"
                    class="tag-filter-btn"
                    :class="{ active: selectedTagIds.includes(tag.id) }"
                    :style="selectedTagIds.includes(tag.id) ? { background: tag.color, borderColor: tag.color } : {}"
                    @click="toggleTagFilter(tag.id)"
                >
                    <span class="filter-dot" :style="{ background: tag.color }"></span>
                    {{ tag.name }}
                    <span class="filter-count">({{ getTagPlantCount(tag.id) }})</span>
                </button>
            </div>
        </div>

        <div v-if="filteredArchives.length === 0 && archives.length > 0" class="empty-state card">
            <div class="empty-icon">🔍</div>
            <h3>没有匹配的绿植</h3>
            <p>当前选中的标签下没有绿植，请尝试其他标签或清除筛选。</p>
            <button class="btn btn-primary" @click="clearTagFilter">清除筛选</button>
        </div>

        <div v-if="archives.length === 0" class="empty-state card">
            <div class="empty-icon">🌱</div>
            <h3>还没有绿植档案</h3>
            <p>点击上方按钮，开始记录您的第一盆绿植吧！</p>
        </div>

        <h2 class="section-title">
            🪴 我的绿植列表
            <span v-if="selectedTagIds.length > 0" class="filter-indicator">
                (已筛选 {{ filteredArchives.length }}/{{ archives.length }})
            </span>
        </h2>

        <div class="grid grid-3">
            <div v-for="archive in filteredArchives" :key="archive.id" class="archive-card card" @click="goToDetail(archive.id)">
                <div class="archive-image">
                    <LazyImage
                        :src="archive.imageUrl || getDefaultImage()"
                        :thumbnail="archive.imageUrl || getDefaultImage(true)"
                        :alt="archive.customName"
                        height="160"
                        mode="list"
                    />
                </div>
                <div class="archive-content">
                    <h3>{{ archive.customName || '未命名' }}</h3>
                    <p class="plant-name">{{ archive.plantCategory?.name || '未知品种' }}</p>
                    <div class="archive-meta">
                        <span>📍 {{ archive.location || '未设置位置' }}</span>
                        <span>📅 {{ formatDate(archive.purchaseDate) }}</span>
                    </div>
                    <div v-if="getArchiveTags(archive.id).length > 0" class="archive-tags">
                        <span
                            v-for="tag in getArchiveTags(archive.id).slice(0, 3)"
                            :key="tag.id"
                            class="archive-tag"
                            :style="{ background: tag.color, borderColor: tag.color }"
                        >
                            {{ tag.name }}
                        </span>
                        <span v-if="getArchiveTags(archive.id).length > 3" class="more-tags">
                            +{{ getArchiveTags(archive.id).length - 3 }}
                        </span>
                    </div>
                    <div v-if="archive.reminderEnabled && (archive.waterInterval || archive.fertilizeInterval)" class="reminder-tags">
                        <span v-if="archive.waterInterval" class="mini-tag">💧 每{{ archive.waterInterval }}天浇水</span>
                        <span v-if="archive.fertilizeInterval" class="mini-tag">🧪 每{{ archive.fertilizeInterval }}天施肥</span>
                    </div>
                </div>
            </div>
        </div>

        <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
            <div class="modal card">
                <h2>🌿 添加绿植档案</h2>
                <div class="form-group">
                    <label>绿植品种</label>
                    <select v-model="newArchive.plantCategoryId">
                        <option value="">请选择品种</option>
                        <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>自定义昵称</label>
                    <input type="text" v-model="newArchive.customName" placeholder="给您的绿植起个名字" />
                </div>
                <div class="form-group">
                    <label>购入时间</label>
                    <input type="date" v-model="newArchive.purchaseDate" />
                </div>
                <div class="form-group">
                    <label>摆放位置</label>
                    <input type="text" v-model="newArchive.location" placeholder="例如：客厅窗台" />
                </div>
                <div class="form-group">
                    <label>摆放环境</label>
                    <select v-model="newArchive.environment">
                        <option value="">请选择环境</option>
                        <option value="室内向阳">室内向阳</option>
                        <option value="室内背阴">室内背阴</option>
                        <option value="阳台">阳台</option>
                        <option value="室外">室外</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>
                        <input type="checkbox" v-model="newArchive.reminderEnabled" />
                        开启养护提醒
                    </label>
                </div>
                <div v-if="newArchive.reminderEnabled" class="grid grid-2">
                    <div class="form-group">
                        <label>浇水周期（天）</label>
                        <input type="number" v-model="newArchive.waterInterval" min="1" />
                    </div>
                    <div class="form-group">
                        <label>施肥周期（天）</label>
                        <input type="number" v-model="newArchive.fertilizeInterval" min="1" />
                    </div>
                </div>
                <div class="form-group">
                    <label>备注</label>
                    <textarea v-model="newArchive.notes" rows="3" placeholder="记录一些特殊的养护要点..."></textarea>
                </div>
                <div class="modal-actions">
                    <button class="btn btn-secondary" @click="showAddModal = false">取消</button>
                    <button class="btn btn-primary" @click="createArchive">保存</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { plantArchiveApi, plantCategoryApi, tagApi, plantArchiveTagApi } from '../api'
import { useUserStore } from '../stores/user'
import LazyImage from '../components/LazyImage.vue'
import CareCalendar from '../components/CareCalendar.vue'
import TagManager from '../components/TagManager.vue'

const router = useRouter()
const userStore = useUserStore()
const archives = ref([])
const categories = ref([])
const calendarRef = ref(null)
const showAddModal = ref(false)
const newArchive = ref({
    plantCategoryId: null,
    customName: '',
    purchaseDate: '',
    location: '',
    environment: '',
    reminderEnabled: false,
    waterInterval: 7,
    fertilizeInterval: 30,
    notes: ''
})

const tags = ref([])
const selectedTagIds = ref([])
const archiveTagsMap = ref({})
const tagPlantCounts = ref({})
const tagRefreshTrigger = ref(0)

const filteredArchives = computed(() => {
    if (selectedTagIds.value.length === 0) {
        return archives.value
    }
    return archives.value.filter(archive => {
        const archiveTags = archiveTagsMap.value[archive.id] || []
        const archiveTagIds = archiveTags.map(t => t.id)
        return selectedTagIds.value.some(tagId => archiveTagIds.includes(tagId))
    })
})

const getArchiveTags = (archiveId) => {
    return archiveTagsMap.value[archiveId] || []
}

const getTagPlantCount = (tagId) => {
    return tagPlantCounts.value[tagId] || 0
}

const getDefaultImage = (thumbnail = false) => {
    const size = thumbnail ? 'square' : 'landscape_4_3'
    return `https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=green%20potted%20plant&image_size=${size}`
}

const formatDate = (date) => {
    if (!date) return '未知日期'
    if (typeof date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(date)) {
        const [y, m, d] = date.split('-').map(Number)
        return new Date(y, m - 1, d).toLocaleDateString('zh-CN')
    }
    return new Date(date).toLocaleDateString('zh-CN')
}

const goToDetail = (id) => {
    router.push(`/archives/${id}`)
}

const createArchive = async () => {
    try {
        const data = { ...newArchive.value, userId: userStore.currentUser.id }
        if (!data.reminderEnabled) {
            data.waterInterval = null
            data.fertilizeInterval = null
        }
        await plantArchiveApi.create(data)
        showAddModal.value = false
        loadArchives()
        newArchive.value = {
            plantCategoryId: null,
            customName: '',
            purchaseDate: '',
            location: '',
            environment: '',
            reminderEnabled: false,
            waterInterval: 7,
            fertilizeInterval: 30,
            notes: ''
        }
    } catch (e) {
        console.error('创建失败', e)
        alert('创建绿植档案失败，请稍后重试')
    }
}

const loadArchives = async () => {
    try {
        const res = await plantArchiveApi.getByUser(userStore.currentUser.id)
        archives.value = res.data || []
        if (archives.value.length > 0) {
            await loadArchiveTags()
        }
    } catch (e) {
        console.error('加载失败', e)
        archives.value = []
    }
}

const loadTags = async () => {
    try {
        const res = await tagApi.getByUser(userStore.currentUser.id)
        tags.value = res.data || []
        await loadTagPlantCounts()
    } catch (e) {
        console.error('加载标签失败', e)
        tags.value = []
    }
}

const loadArchiveTags = async () => {
    try {
        const archiveIds = archives.value.map(a => a.id)
        const res = await plantArchiveTagApi.getByPlants(archiveIds)
        archiveTagsMap.value = res.data || {}
    } catch (e) {
        console.error('加载植物标签失败', e)
        archiveTagsMap.value = {}
    }
}

const loadTagPlantCounts = async () => {
    if (tags.value.length === 0) {
        tagPlantCounts.value = {}
        return
    }
    try {
        const tagIds = tags.value.map(t => t.id)
        const res = await plantArchiveTagApi.getCountByTags(tagIds)
        tagPlantCounts.value = res.data || {}
    } catch (e) {
        console.error('加载标签计数失败', e)
        tagPlantCounts.value = {}
    }
}

const toggleTagFilter = (tagId) => {
    const index = selectedTagIds.value.indexOf(tagId)
    if (index > -1) {
        selectedTagIds.value.splice(index, 1)
    } else {
        selectedTagIds.value.push(tagId)
    }
}

const clearTagFilter = () => {
    selectedTagIds.value = []
}

const handleTagsUpdated = () => {
    loadTags()
    loadArchiveTags()
    tagRefreshTrigger.value++
}

onMounted(async () => {
    loadArchives()
    loadTags()
    try {
        const res = await plantCategoryApi.getAll()
        categories.value = res.data
    } catch (e) {
        categories.value = [
            { id: 1, name: '绿萝' },
            { id: 2, name: '吊兰' },
            { id: 3, name: '多肉植物' }
        ]
    }
})
</script>

<style scoped>
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.empty-state {
    text-align: center;
    padding: 60px 20px;
}

.empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
}

.empty-state h3 {
    color: #1b5e20;
    margin-bottom: 8px;
}

.empty-state p {
    color: #666;
}

.archive-card {
    cursor: pointer;
    transition: transform 0.3s, box-shadow 0.3s;
    padding: 0;
    overflow: hidden;
}

.archive-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.archive-image {
    height: 160px;
    overflow: hidden;
}

.archive-content {
    padding: 16px;
}

.archive-content h3 {
    font-size: 18px;
    color: #1b5e20;
    margin-bottom: 4px;
}

.plant-name {
    color: #666;
    font-size: 14px;
    margin-bottom: 12px;
}

.archive-meta {
    display: flex;
    flex-direction: column;
    gap: 4px;
    font-size: 13px;
    color: #888;
    margin-bottom: 12px;
}

.reminder-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.mini-tag {
    font-size: 12px;
    color: #558b2f;
    background: #f1f8e9;
    padding: 4px 8px;
    border-radius: 12px;
}

.tag-filter-section {
    padding: 20px;
    margin-bottom: 24px;
}

.tag-filter-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 12px;
}

.tag-filter-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: white;
    border: 2px solid #c5e1a5;
    border-radius: 20px;
    color: #1b5e20;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

.tag-filter-btn:hover {
    border-color: #81c784;
    background: #f1f8e9;
}

.tag-filter-btn.active {
    color: white;
    border-color: #4caf50;
    box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
}

.filter-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: #4caf50;
}

.tag-filter-btn.active .filter-dot {
    background: white;
}

.filter-count {
    color: #888;
    font-size: 12px;
}

.tag-filter-btn.active .filter-count {
    color: rgba(255, 255, 255, 0.8);
}

.filter-indicator {
    font-size: 14px;
    color: #888;
    font-weight: normal;
}

.archive-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    margin-bottom: 12px;
}

.archive-tag {
    display: inline-block;
    padding: 2px 8px;
    color: white;
    border-radius: 8px;
    font-size: 11px;
    font-weight: 500;
    border: 1px solid;
}

.more-tags {
    display: inline-block;
    padding: 2px 6px;
    color: #888;
    background: #f5f5f5;
    border-radius: 8px;
    font-size: 11px;
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
</style>

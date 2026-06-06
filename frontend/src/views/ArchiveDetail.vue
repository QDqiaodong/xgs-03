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

        <div class="logs-section">
            <h2 class="section-title">📋 养护日志</h2>
            <div v-if="logs.length === 0" class="empty-logs card">
                <p>暂无养护记录，点击上方按钮开始记录</p>
            </div>
            <div v-else class="log-list">
                <div v-for="log in logs" :key="log.id" class="log-item card">
                    <div class="log-header">
                        <span class="log-date">{{ formatDate(log.logDate) }}</span>
                        <span class="log-type">{{ getOperationTypeLabel(log.operationType) }}</span>
                    </div>
                    <p v-if="log.details" class="log-details">{{ log.details }}</p>
                    <div class="log-status">
                        <span v-if="log.growthStatus">🌱 {{ log.growthStatus }}</span>
                        <span v-if="log.diseaseStatus">⚠️ {{ log.diseaseStatus }}</span>
                    </div>
                </div>
            </div>
        </div>

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
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { plantArchiveApi, careLogApi } from '../api'
import { useUserStore } from '../stores/user'
import LazyImage from '../components/LazyImage.vue'

const route = useRoute()
const userStore = useUserStore()
const archive = ref(null)
const logs = ref([])
const showAddLogModal = ref(false)

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

const getOperationTypeLabel = (type) => {
    const labels = {
        '浇水': '💧 浇水',
        '施肥': '🧪 施肥',
        '修剪': '✂️ 修剪',
        '换盆': '🪴 换盆',
        '其他': '📝 其他'
    }
    return labels[type] || type
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
        loadLogs()
        newLog.value = {
            logDate: toLocalDateStr(new Date()),
            operationType: '浇水',
            details: '',
            growthStatus: '',
            diseaseStatus: ''
        }
    } catch (e) {
        console.error('创建失败', e)
        logs.value.unshift({
            id: Date.now(),
            ...newLog.value,
            createdAt: new Date().toISOString()
        })
        showAddLogModal.value = false
    }
}

const loadLogs = async () => {
    try {
        const res = await careLogApi.getByPlant(route.params.id)
        logs.value = res.data
    } catch (e) {
        console.error('加载失败', e)
    }
}

onMounted(async () => {
    try {
        const res = await plantArchiveApi.getById(route.params.id)
        archive.value = res.data
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
    loadLogs()
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

.section-title {
    font-size: 20px;
    color: #1b5e20;
    margin-bottom: 16px;
}

.empty-logs {
    text-align: center;
    color: #888;
    padding: 40px;
}

.log-item {
    margin-bottom: 12px;
}

.log-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.log-date {
    font-weight: 500;
    color: #1b5e20;
}

.log-type {
    background: #e8f5e9;
    color: #388e3c;
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 13px;
}

.log-details {
    color: #555;
    margin-bottom: 8px;
}

.log-status {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: #666;
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

@media (max-width: 768px) {
    .detail-header {
        grid-template-columns: 1fr;
    }
    .info-grid {
        grid-template-columns: 1fr;
    }
}
</style>

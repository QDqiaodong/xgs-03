<template>
    <div class="timeline-section">
        <h2 class="section-title">🌱 成长时间轴</h2>

        <div v-if="logs.length === 0 && !loading" class="empty-logs card">
            <p>暂无养护记录，开始记录您植物的成长历程吧</p>
        </div>

        <div v-else class="timeline-container">
            <div
                v-for="(log, index) in logs"
                :key="log.id"
                class="timeline-item"
                :class="{ 'timeline-item-enter': true }"
                :style="{ animationDelay: `${index * 0.05}s` }"
            >
                <div class="timeline-left">
                    <div class="timeline-node" :class="getNodeClass(log.operationType)">
                        <span class="node-icon">{{ getOperationIcon(log.operationType) }}</span>
                    </div>
                    <div v-if="index < logs.length - 1" class="timeline-line"></div>
                </div>

                <div class="timeline-right card">
                    <div class="timeline-header">
                        <div class="timeline-date">
                            <span class="date-main">{{ formatDate(log.logDate) }}</span>
                            <span class="date-time" v-if="log.createdAt">{{ formatTime(log.createdAt) }}</span>
                        </div>
                        <span class="timeline-type-tag" :class="getTagClass(log.operationType)">
                            {{ getOperationLabel(log.operationType) }}
                        </span>
                    </div>

                    <div
                        v-if="log.details"
                        class="timeline-details"
                        :class="{ 'expanded': expandedItems.has(log.id) }"
                    >
                        <p
                            class="details-text"
                            :class="{ 'clamp-text': !expandedItems.has(log.id) && isLongText(log.details) }"
                        >{{ log.details }}</p>
                        <button
                            v-if="isLongText(log.details)"
                            class="toggle-btn"
                            @click="toggleExpand(log.id)"
                        >
                            {{ expandedItems.has(log.id) ? '收起 ↑' : '展开 ↓' }}
                        </button>
                    </div>

                    <div v-if="log.growthStatus || log.diseaseStatus" class="timeline-status">
                        <span v-if="log.growthStatus" class="status-tag status-growth">
                            🌱 {{ log.growthStatus }}
                        </span>
                        <span v-if="log.diseaseStatus" class="status-tag status-disease">
                            ⚠️ {{ log.diseaseStatus }}
                        </span>
                    </div>

                    <div v-if="parseImageUrls(log.imageUrls).length > 0" class="timeline-images">
                        <div
                            v-for="(img, imgIndex) in parseImageUrls(log.imageUrls)"
                            :key="imgIndex"
                            class="timeline-image-wrapper"
                            @click="openImagePreview(parseImageUrls(log.imageUrls), imgIndex)"
                        >
                            <LazyImage
                                :src="img"
                                :alt="`养护记录图片 ${imgIndex + 1}`"
                                width="100%"
                                height="120"
                                mode="list"
                            />
                        </div>
                    </div>
                </div>
            </div>

            <div ref="sentinelRef" class="timeline-sentinel"></div>

            <div v-if="loading" class="loading-more">
                <div class="spinner"></div>
                <span>加载中...</span>
            </div>

            <div v-if="!hasMore && logs.length > 0" class="no-more">
                <span>— 已加载全部记录 —</span>
            </div>
        </div>

        <div v-if="previewImages.length > 0" class="image-preview-overlay" @click.self="closeImagePreview">
            <button class="preview-close" @click="closeImagePreview">×</button>
            <button v-if="previewIndex > 0" class="preview-nav preview-prev" @click="prevImage">‹</button>
            <img :src="previewImages[previewIndex]" class="preview-image" alt="预览图片" />
            <button v-if="previewIndex < previewImages.length - 1" class="preview-nav preview-next" @click="nextImage">›</button>
            <div class="preview-counter">{{ previewIndex + 1 }} / {{ previewImages.length }}</div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import LazyImage from './LazyImage.vue'

const props = defineProps({
    plantId: {
        type: [String, Number],
        required: true
    },
    fetchLogs: {
        type: Function,
        required: true
    },
    pageSize: {
        type: Number,
        default: 5
    }
})

const emit = defineEmits(['logCreated'])

const logs = ref([])
const loading = ref(false)
const hasMore = ref(true)
const currentPage = ref(0)
const sentinelRef = ref(null)
const observer = ref(null)
const expandedItems = ref(new Set())

const previewImages = ref([])
const previewIndex = ref(0)

const operationConfig = {
    '浇水': { icon: '💧', label: '浇水', color: 'water' },
    '施肥': { icon: '🧪', label: '施肥', color: 'fertilize' },
    '修剪': { icon: '✂️', label: '修剪', color: 'prune' },
    '换盆': { icon: '🪴', label: '换盆', color: 'repot' },
    '其他': { icon: '📝', label: '其他', color: 'other' }
}

const getOperationIcon = (type) => {
    return operationConfig[type]?.icon || '📝'
}

const getOperationLabel = (type) => {
    return operationConfig[type]?.label || type
}

const getNodeClass = (type) => {
    return `node-${operationConfig[type]?.color || 'other'}`
}

const getTagClass = (type) => {
    return `tag-${operationConfig[type]?.color || 'other'}`
}

const formatDate = (date) => {
    if (!date) return '未知日期'
    if (typeof date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(date)) {
        const [y, m, d] = date.split('-').map(Number)
        return new Date(y, m - 1, d).toLocaleDateString('zh-CN', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        })
    }
    return new Date(date).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    })
}

const formatTime = (date) => {
    if (!date) return ''
    const d = new Date(date)
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const isLongText = (text) => {
    return text && text.length > 80
}

const toggleExpand = (id) => {
    if (expandedItems.value.has(id)) {
        expandedItems.value.delete(id)
    } else {
        expandedItems.value.add(id)
    }
    expandedItems.value = new Set(expandedItems.value)
}

const parseImageUrls = (imageUrls) => {
    if (!imageUrls) return []
    if (Array.isArray(imageUrls)) return imageUrls
    try {
        const parsed = JSON.parse(imageUrls)
        return Array.isArray(parsed) ? parsed : [imageUrls]
    } catch {
        return imageUrls.split(',').map(url => url.trim()).filter(Boolean)
    }
}

const openImagePreview = (images, index) => {
    previewImages.value = images
    previewIndex.value = index
    document.body.style.overflow = 'hidden'
}

const closeImagePreview = () => {
    previewImages.value = []
    previewIndex.value = 0
    document.body.style.overflow = ''
}

const prevImage = () => {
    if (previewIndex.value > 0) previewIndex.value--
}

const nextImage = () => {
    if (previewIndex.value < previewImages.value.length - 1) previewIndex.value++
}

const loadMoreLogs = async () => {
    if (loading.value || !hasMore.value) return
    loading.value = true
    try {
        const result = await props.fetchLogs(props.plantId, currentPage.value, props.pageSize)
        const newLogs = result.content || result.data || []
        if (newLogs.length === 0 || newLogs.length < props.pageSize) {
            hasMore.value = false
        }
        logs.value = [...logs.value, ...newLogs]
        currentPage.value++
    } catch (e) {
        console.error('加载日志失败', e)
        hasMore.value = false
    } finally {
        loading.value = false
    }
}

const resetAndLoad = async () => {
    logs.value = []
    currentPage.value = 0
    hasMore.value = true
    await loadMoreLogs()
}

watch(() => props.plantId, () => {
    resetAndLoad()
})

onMounted(async () => {
    await loadMoreLogs()

    if ('IntersectionObserver' in window && sentinelRef.value) {
        observer.value = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting && hasMore.value && !loading.value) {
                    loadMoreLogs()
                }
            })
        }, {
            rootMargin: '200px',
            threshold: 0.1
        })
        observer.value.observe(sentinelRef.value)
    }
})

onUnmounted(() => {
    if (observer.value) {
        observer.value.disconnect()
    }
    document.body.style.overflow = ''
})

defineExpose({ resetAndLoad })
</script>

<style scoped>
.timeline-section {
    margin-top: 24px;
}

.section-title {
    font-size: 20px;
    color: #1b5e20;
    margin-bottom: 20px;
    font-weight: 600;
}

.empty-logs {
    text-align: center;
    color: #888;
    padding: 40px;
}

.timeline-container {
    position: relative;
}

.timeline-item {
    display: flex;
    gap: 16px;
    opacity: 0;
    transform: translateY(20px);
    animation: fadeInUp 0.4s ease forwards;
}

@keyframes fadeInUp {
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.timeline-left {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex-shrink: 0;
    width: 48px;
}

.timeline-node {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    z-index: 1;
    background: white;
    border: 3px solid;
    transition: transform 0.2s ease;
}

.timeline-node:hover {
    transform: scale(1.1);
}

.node-icon {
    font-size: 18px;
}

.node-water {
    border-color: #42a5f5;
    background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
}

.node-fertilize {
    border-color: #ab47bc;
    background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
}

.node-prune {
    border-color: #ff7043;
    background: linear-gradient(135deg, #fbe9e7 0%, #ffccbc 100%);
}

.node-repot {
    border-color: #8d6e63;
    background: linear-gradient(135deg, #efebe9 0%, #d7ccc8 100%);
}

.node-other {
    border-color: #66bb6a;
    background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
}

.timeline-line {
    width: 3px;
    flex: 1;
    min-height: 30px;
    background: linear-gradient(to bottom, #a5d6a7, #c8e6c9);
    margin: 4px 0;
    border-radius: 2px;
}

.timeline-right {
    flex: 1;
    margin-bottom: 16px;
    padding: 16px;
    transition: box-shadow 0.2s ease;
}

.timeline-right:hover {
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12);
}

.timeline-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
    gap: 12px;
    flex-wrap: wrap;
}

.timeline-date {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.date-main {
    font-weight: 600;
    color: #1b5e20;
    font-size: 15px;
}

.date-time {
    font-size: 12px;
    color: #888;
}

.timeline-type-tag {
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 13px;
    font-weight: 500;
}

.tag-water {
    background: #e3f2fd;
    color: #1976d2;
}

.tag-fertilize {
    background: #f3e5f5;
    color: #7b1fa2;
}

.tag-prune {
    background: #fbe9e7;
    color: #d84315;
}

.tag-repot {
    background: #efebe9;
    color: #5d4037;
}

.tag-other {
    background: #e8f5e9;
    color: #388e3c;
}

.timeline-details {
    margin-bottom: 12px;
}

.details-text {
    color: #444;
    line-height: 1.7;
    font-size: 14px;
    white-space: pre-wrap;
    word-break: break-word;
}

.clamp-text {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.toggle-btn {
    background: none;
    border: none;
    color: #4caf50;
    font-size: 13px;
    cursor: pointer;
    padding: 4px 0;
    margin-top: 4px;
    font-weight: 500;
    transition: color 0.2s;
}

.toggle-btn:hover {
    color: #2e7d32;
}

.timeline-status {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;
}

.status-tag {
    padding: 4px 10px;
    border-radius: 8px;
    font-size: 12px;
    line-height: 1.5;
}

.status-growth {
    background: #e8f5e9;
    color: #2e7d32;
}

.status-disease {
    background: #fff3e0;
    color: #e65100;
}

.timeline-images {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 8px;
    margin-top: 8px;
}

.timeline-image-wrapper {
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: transform 0.2s ease;
    aspect-ratio: 1;
}

.timeline-image-wrapper:hover {
    transform: scale(1.02);
}

.timeline-sentinel {
    height: 20px;
}

.loading-more {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 20px;
    color: #666;
    font-size: 14px;
}

.spinner {
    width: 20px;
    height: 20px;
    border: 2px solid #c8e6c9;
    border-top-color: #4caf50;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

.no-more {
    text-align: center;
    padding: 16px;
    color: #999;
    font-size: 13px;
}

.image-preview-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
    padding: 40px;
}

.preview-image {
    max-width: 90%;
    max-height: 90%;
    object-fit: contain;
    border-radius: 8px;
}

.preview-close {
    position: absolute;
    top: 20px;
    right: 30px;
    background: none;
    border: none;
    color: white;
    font-size: 40px;
    cursor: pointer;
    line-height: 1;
    z-index: 10;
}

.preview-close:hover {
    opacity: 0.7;
}

.preview-nav {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    background: rgba(255, 255, 255, 0.1);
    border: none;
    color: white;
    font-size: 48px;
    cursor: pointer;
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s;
}

.preview-nav:hover {
    background: rgba(255, 255, 255, 0.2);
}

.preview-prev {
    left: 30px;
}

.preview-next {
    right: 30px;
}

.preview-counter {
    position: absolute;
    bottom: 30px;
    left: 50%;
    transform: translateX(-50%);
    color: white;
    font-size: 14px;
    background: rgba(0, 0, 0, 0.5);
    padding: 6px 16px;
    border-radius: 20px;
}

@media (max-width: 768px) {
    .timeline-left {
        width: 36px;
    }

    .timeline-node {
        width: 32px;
        height: 32px;
    }

    .node-icon {
        font-size: 14px;
    }

    .timeline-images {
        grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    }

    .preview-nav {
        width: 44px;
        height: 44px;
        font-size: 32px;
    }

    .preview-prev {
        left: 10px;
    }

    .preview-next {
        right: 10px;
    }
}
</style>

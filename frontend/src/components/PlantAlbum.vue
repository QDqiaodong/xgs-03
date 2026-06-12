<template>
    <div class="album-section card">
        <div class="album-header">
            <div class="header-left">
                <h2 class="section-title">📸 成长相册</h2>
                <span class="photo-count" v-if="photos.length > 0">共 {{ photos.length }} 张照片</span>
            </div>
            <div class="header-actions">
                <label class="btn btn-primary upload-btn">
                    <input
                        type="file"
                        ref="fileInput"
                        accept="image/*"
                        multiple
                        @change="handleFileSelect"
                        style="display: none"
                    />
                    <span>📷 上传照片</span>
                </label>
            </div>
        </div>

        <div v-if="uploading" class="upload-progress card">
            <div class="progress-header">
                <span>正在上传 {{ uploadedCount }} / {{ totalUploadCount }}</span>
                <span class="progress-percent">{{ Math.round(overallProgress) }}%</span>
            </div>
            <div class="progress-bar">
                <div class="progress-fill" :style="{ width: overallProgress + '%' }"></div>
            </div>
        </div>

        <div v-if="photos.length === 0 && !loading" class="empty-album">
            <div class="empty-icon">🌱</div>
            <p>还没有照片，记录下植物的成长瞬间吧</p>
            <label class="btn btn-primary empty-upload-btn">
                <input
                    type="file"
                    accept="image/*"
                    multiple
                    @change="handleFileSelect"
                    style="display: none"
                />
                <span>上传第一张照片</span>
            </label>
        </div>

        <div v-else class="album-content">
            <div v-for="(group, dateKey) in groupedPhotos" :key="dateKey" class="date-group">
                <div class="date-header">
                    <div class="date-marker">
                        <span class="date-icon">📅</span>
                        <span class="date-text">{{ formatDate(dateKey) }}</span>
                    </div>
                    <div class="date-count">{{ group.length }} 张</div>
                </div>
                <div class="waterfall-container">
                    <div
                        v-for="(photo, index) in group"
                        :key="photo.id"
                        class="photo-item"
                        :style="{ animationDelay: `${index * 0.05}s` }"
                        @mouseenter="hoveredPhoto = photo.id"
                        @mouseleave="hoveredPhoto = null"
                    >
                        <div class="photo-wrapper" @click="openPreview(group, group.indexOf(photo))">
                            <img
                                :src="photo.imageUrl"
                                :alt="photo.description || '植物照片'"
                                class="album-image"
                                loading="lazy"
                            />
                            <div
                                v-if="photo.isCover"
                                class="cover-badge"
                            >
                                🏆 封面
                            </div>
                            <div
                                v-if="hoveredPhoto === photo.id"
                                class="photo-overlay"
                            >
                                <div class="overlay-actions" @click.stop>
                                    <button
                                        class="overlay-btn"
                                        :class="{ active: photo.isCover }"
                                        :disabled="photo.isCover"
                                        @click="handleSetCover(photo.id)"
                                        :title="photo.isCover ? '当前封面' : '设为封面'"
                                    >
                                        {{ photo.isCover ? '✓ 封面' : '🏆 设为封面' }}
                                    </button>
                                    <button
                                        class="overlay-btn btn-edit"
                                        @click="openEditModal(photo)"
                                        title="编辑描述"
                                    >
                                        ✏️ 编辑
                                    </button>
                                    <button
                                        class="overlay-btn btn-delete"
                                        @click="handleDelete(photo.id)"
                                        title="删除照片"
                                    >
                                        🗑️ 删除
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div v-if="photo.description" class="photo-description">
                            {{ photo.description }}
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div v-if="showUploadModal" class="modal-overlay" @click.self="showUploadModal = false">
            <div class="modal card">
                <h2>📷 上传照片</h2>
                <div class="upload-preview-list">
                    <div v-for="(file, index) in pendingFiles" :key="index" class="upload-preview-item">
                        <img :src="file.previewUrl" class="preview-thumb" />
                        <div class="preview-inputs">
                            <input
                                type="date"
                                v-model="file.photoDate"
                                class="preview-date"
                            />
                            <textarea
                                v-model="file.description"
                                placeholder="添加描述（可选）..."
                                rows="2"
                                class="preview-desc"
                            ></textarea>
                        </div>
                        <button class="remove-file-btn" @click="removePendingFile(index)">×</button>
                    </div>
                </div>
                <div class="modal-actions">
                    <button class="btn btn-secondary" @click="showUploadModal = false">取消</button>
                    <button class="btn btn-primary" @click="confirmUpload" :disabled="uploading">
                        {{ uploading ? '上传中...' : '确认上传' }}
                    </button>
                </div>
            </div>
        </div>

        <div v-if="showEditModal && editingPhoto" class="modal-overlay" @click.self="showEditModal = false">
            <div class="modal card">
                <h2>✏️ 编辑照片</h2>
                <img :src="editingPhoto.imageUrl" class="edit-preview" />
                <div class="form-group">
                    <label>拍摄日期</label>
                    <input type="date" v-model="editForm.photoDate" />
                </div>
                <div class="form-group">
                    <label>照片描述</label>
                    <textarea v-model="editForm.description" rows="3" placeholder="添加描述..."></textarea>
                </div>
                <div class="modal-actions">
                    <button class="btn btn-secondary" @click="showEditModal = false">取消</button>
                    <button class="btn btn-primary" @click="confirmEdit">保存</button>
                </div>
            </div>
        </div>

        <div v-if="previewImages.length > 0" class="image-preview-overlay" @click.self="closePreview">
            <button class="preview-close" @click="closePreview">×</button>
            <button v-if="previewIndex > 0" class="preview-nav preview-prev" @click="prevImage">‹</button>
            <div class="preview-content">
                <img :src="previewImages[previewIndex].imageUrl" class="preview-image" alt="预览图片" />
                <div class="preview-info">
                    <div v-if="previewImages[previewIndex].photoDate" class="preview-date">
                        📅 {{ formatDate(previewImages[previewIndex].photoDate) }}
                    </div>
                    <div v-if="previewImages[previewIndex].description" class="preview-description">
                        {{ previewImages[previewIndex].description }}
                    </div>
                </div>
            </div>
            <button v-if="previewIndex < previewImages.length - 1" class="preview-nav preview-next" @click="nextImage">›</button>
            <div class="preview-counter">{{ previewIndex + 1 }} / {{ previewImages.length }}</div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { plantPhotoApi } from '../api'
import { useUserStore } from '../stores/user'

const props = defineProps({
    plantArchiveId: {
        type: [String, Number],
        required: true
    }
})

const emit = defineEmits(['coverChanged'])

const userStore = useUserStore()
const photos = ref([])
const loading = ref(false)
const hoveredPhoto = ref(null)

const fileInput = ref(null)
const pendingFiles = ref([])
const showUploadModal = ref(false)
const uploading = ref(false)
const uploadedCount = ref(0)
const totalUploadCount = ref(0)
const overallProgress = ref(0)

const showEditModal = ref(false)
const editingPhoto = ref(null)
const editForm = ref({ photoDate: '', description: '' })

const previewImages = ref([])
const previewIndex = ref(0)

const toLocalDateStr = (date) => {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
}

const groupedPhotos = computed(() => {
    const groups = {}
    photos.value.forEach((photo) => {
        const key = photo.photoDate || '未知日期'
        if (!groups[key]) {
            groups[key] = []
        }
        groups[key].push(photo)
    })
    const sortedKeys = Object.keys(groups).sort((a, b) => {
        if (a === '未知日期') return 1
        if (b === '未知日期') return -1
        return new Date(b) - new Date(a)
    })
    const sorted = {}
    sortedKeys.forEach((key) => {
        sorted[key] = groups[key]
    })
    return sorted
})

const formatDate = (dateStr) => {
    if (!dateStr || dateStr === '未知日期') return '未知日期'
    if (typeof dateStr === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(dateStr)) {
        const [y, m, d] = dateStr.split('-').map(Number)
        return new Date(y, m - 1, d).toLocaleDateString('zh-CN', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        })
    }
    return new Date(dateStr).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    })
}

const loadPhotos = async () => {
    loading.value = true
    try {
        const res = await plantPhotoApi.getByArchive(props.plantArchiveId)
        photos.value = res.data
    } catch (e) {
        console.error('加载相册失败', e)
    } finally {
        loading.value = false
    }
}

const handleFileSelect = (event) => {
    const files = Array.from(event.target.files || [])
    if (files.length === 0) return

    pendingFiles.value = files.map((file) => ({
        file,
        previewUrl: URL.createObjectURL(file),
        photoDate: toLocalDateStr(new Date()),
        description: ''
    }))
    showUploadModal.value = true
    event.target.value = ''
}

const removePendingFile = (index) => {
    URL.revokeObjectURL(pendingFiles.value[index].previewUrl)
    pendingFiles.value.splice(index, 1)
    if (pendingFiles.value.length === 0) {
        showUploadModal.value = false
    }
}

const confirmUpload = async () => {
    if (pendingFiles.value.length === 0 || !userStore.currentUser) return

    uploading.value = true
    totalUploadCount.value = pendingFiles.value.length
    uploadedCount.value = 0
    overallProgress.value = 0

    try {
        for (let i = 0; i < pendingFiles.value.length; i++) {
            const item = pendingFiles.value[i]
            const formData = new FormData()
            formData.append('plantArchiveId', props.plantArchiveId)
            formData.append('userId', userStore.currentUser.id)
            formData.append('file', item.file)
            if (item.description) {
                formData.append('description', item.description)
            }
            if (item.photoDate) {
                formData.append('photoDate', item.photoDate)
            }

            const onProgress = (evt) => {
                if (evt.lengthComputable) {
                    const fileProgress = (evt.loaded / evt.total) * 100
                    overallProgress.value = ((uploadedCount.value + fileProgress / 100) / totalUploadCount.value) * 100
                }
            }

            await plantPhotoApi.upload(formData, onProgress)
            uploadedCount.value = i + 1
            overallProgress.value = (uploadedCount.value / totalUploadCount.value) * 100
        }

        pendingFiles.value.forEach((item) => URL.revokeObjectURL(item.previewUrl))
        pendingFiles.value = []
        showUploadModal.value = false
        await loadPhotos()
    } catch (e) {
        console.error('上传失败', e)
        alert('上传失败，请重试')
    } finally {
        uploading.value = false
        uploadedCount.value = 0
        totalUploadCount.value = 0
        overallProgress.value = 0
    }
}

const openEditModal = (photo) => {
    editingPhoto.value = photo
    editForm.value = {
        photoDate: photo.photoDate || toLocalDateStr(new Date()),
        description: photo.description || ''
    }
    showEditModal.value = true
}

const confirmEdit = async () => {
    if (!editingPhoto.value) return
    try {
        await plantPhotoApi.update(editingPhoto.value.id, editForm.value)
        showEditModal.value = false
        editingPhoto.value = null
        await loadPhotos()
    } catch (e) {
        console.error('编辑失败', e)
        alert('编辑失败，请重试')
    }
}

const handleSetCover = async (id) => {
    try {
        const res = await plantPhotoApi.setCover(id)
        await loadPhotos()
        emit('coverChanged', res.data)
    } catch (e) {
        console.error('设置封面失败', e)
    }
}

const handleDelete = async (id) => {
    if (!confirm('确定要删除这张照片吗？')) return
    try {
        await plantPhotoApi.delete(id)
        await loadPhotos()
    } catch (e) {
        console.error('删除失败', e)
        alert('删除失败，请重试')
    }
}

const openPreview = (group, index) => {
    previewImages.value = group
    previewIndex.value = index
    document.body.style.overflow = 'hidden'
}

const closePreview = () => {
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

watch(() => props.plantArchiveId, () => {
    loadPhotos()
})

onMounted(() => {
    loadPhotos()
})

defineExpose({ loadPhotos })
</script>

<style scoped>
.album-section {
    margin-top: 24px;
    padding: 24px;
}

.album-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 12px;
}

.section-title {
    font-size: 20px;
    color: #1b5e20;
    margin: 0;
    font-weight: 600;
}

.photo-count {
    font-size: 13px;
    color: #888;
    background: #f5f5f5;
    padding: 4px 10px;
    border-radius: 12px;
}

.upload-btn {
    cursor: pointer;
}

.upload-progress {
    margin-bottom: 20px;
    padding: 16px;
    background: #f1f8e9;
    border: 1px solid #c5e1a5;
}

.progress-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 14px;
    color: #33691e;
    font-weight: 500;
}

.progress-percent {
    font-weight: 600;
}

.progress-bar {
    width: 100%;
    height: 8px;
    background: #e0e0e0;
    border-radius: 4px;
    overflow: hidden;
}

.progress-fill {
    height: 100%;
    background: linear-gradient(90deg, #66bb6a, #43a047);
    border-radius: 4px;
    transition: width 0.3s ease;
}

.empty-album {
    text-align: center;
    padding: 40px 20px;
    color: #888;
}

.empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
}

.empty-album p {
    margin-bottom: 20px;
    font-size: 14px;
}

.empty-upload-btn {
    cursor: pointer;
}

.date-group {
    margin-bottom: 28px;
}

.date-group:last-child {
    margin-bottom: 0;
}

.date-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 14px;
    padding-bottom: 8px;
    border-bottom: 2px solid #e8f5e9;
}

.date-marker {
    display: flex;
    align-items: center;
    gap: 8px;
}

.date-icon {
    font-size: 16px;
}

.date-text {
    font-size: 16px;
    font-weight: 600;
    color: #2e7d32;
}

.date-count {
    font-size: 12px;
    color: #888;
    background: #f5f5f5;
    padding: 3px 8px;
    border-radius: 10px;
}

.waterfall-container {
    column-count: 3;
    column-gap: 12px;
}

.photo-item {
    break-inside: avoid;
    margin-bottom: 12px;
    opacity: 0;
    transform: translateY(10px);
    animation: fadeInUp 0.4s ease forwards;
}

@keyframes fadeInUp {
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.photo-wrapper {
    position: relative;
    border-radius: 10px;
    overflow: hidden;
    cursor: pointer;
    background: #f5f5f5;
}

.album-image {
    display: block;
    width: 100%;
}

.cover-badge {
    position: absolute;
    top: 8px;
    left: 8px;
    background: linear-gradient(135deg, #ffb74d, #ff9800);
    color: white;
    font-size: 11px;
    font-weight: 600;
    padding: 3px 10px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(255, 152, 0, 0.4);
}

.photo-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.7) 0%, rgba(0, 0, 0, 0.3) 50%, rgba(0, 0, 0, 0) 100%);
    display: flex;
    align-items: flex-end;
    justify-content: center;
    padding: 12px;
}

.overlay-actions {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
    justify-content: center;
}

.overlay-btn {
    background: rgba(255, 255, 255, 0.95);
    border: none;
    color: #333;
    padding: 6px 10px;
    border-radius: 6px;
    font-size: 12px;
    cursor: pointer;
    transition: all 0.2s;
    font-weight: 500;
}

.overlay-btn:hover:not(:disabled) {
    background: white;
    transform: translateY(-1px);
}

.overlay-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.overlay-btn.active {
    background: linear-gradient(135deg, #66bb6a, #43a047);
    color: white;
}

.overlay-btn.btn-delete:hover {
    background: #ffebee;
    color: #d32f2f;
}

.overlay-btn.btn-edit:hover {
    background: #e3f2fd;
    color: #1976d2;
}

.photo-description {
    margin-top: 6px;
    padding: 0 4px;
    font-size: 12px;
    color: #666;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.modal-overlay {
    position: fixed;
    inset: 0;
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
    max-height: 85vh;
    overflow-y: auto;
}

.modal h2 {
    color: #1b5e20;
    margin-bottom: 16px;
}

.upload-preview-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    max-height: 50vh;
    overflow-y: auto;
    margin-bottom: 16px;
}

.upload-preview-item {
    display: flex;
    gap: 12px;
    padding: 10px;
    background: #f9fbe7;
    border-radius: 8px;
    position: relative;
}

.preview-thumb {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 6px;
    flex-shrink: 0;
}

.preview-inputs {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.preview-date,
.preview-desc {
    width: 100%;
    padding: 6px 10px;
    border: 1px solid #c5e1a5;
    border-radius: 6px;
    font-size: 13px;
    font-family: inherit;
    resize: vertical;
}

.preview-desc:focus,
.preview-date:focus {
    outline: none;
    border-color: #4caf50;
}

.remove-file-btn {
    position: absolute;
    top: 6px;
    right: 6px;
    background: #ef5350;
    border: none;
    color: white;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    font-size: 16px;
    line-height: 1;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
}

.remove-file-btn:hover {
    background: #e53935;
}

.edit-preview {
    width: 100%;
    max-height: 250px;
    object-fit: contain;
    border-radius: 8px;
    margin-bottom: 16px;
    background: #f5f5f5;
}

.form-group {
    margin-bottom: 14px;
}

.form-group label {
    display: block;
    margin-bottom: 6px;
    font-weight: 500;
    color: #33691e;
    font-size: 14px;
}

.form-group input[type="date"],
.form-group textarea {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #c5e1a5;
    border-radius: 8px;
    font-size: 14px;
    font-family: inherit;
    resize: vertical;
}

.form-group input:focus,
.form-group textarea:focus {
    outline: none;
    border-color: #4caf50;
    box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
}

.modal-actions {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
    margin-top: 20px;
}

.image-preview-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.92);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
    padding: 40px;
}

.preview-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    max-width: 90%;
}

.preview-image {
    max-width: 100%;
    max-height: 75vh;
    object-fit: contain;
    border-radius: 10px;
}

.preview-info {
    margin-top: 16px;
    text-align: center;
    color: white;
}

.preview-date {
    font-size: 13px;
    opacity: 0.8;
    margin-bottom: 4px;
}

.preview-description {
    font-size: 15px;
    line-height: 1.6;
    max-width: 600px;
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
    .waterfall-container {
        column-count: 2;
    }

    .album-header {
        flex-direction: column;
        gap: 12px;
        align-items: flex-start;
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

@media (max-width: 480px) {
    .waterfall-container {
        column-count: 1;
    }
}
</style>

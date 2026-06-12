<template>
    <div class="favorites-page">
        <h1 class="page-title">❤️ 我的收藏</h1>

        <div class="favorites-container">
            <div class="folder-sidebar">
                <div class="sidebar-header">
                    <h3>收藏夹</h3>
                    <button class="btn btn-primary btn-sm" @click="showCreateModal = true">
                        + 新建
                    </button>
                </div>

                <div class="folder-list">
                    <div
                        v-for="folder in folders"
                        :key="folder.id"
                        :class="['folder-item', { active: selectedFolderId === folder.id }]"
                        @click="selectFolder(folder.id)"
                    >
                        <span class="folder-icon">📁</span>
                        <span class="folder-name">{{ folder.name }}</span>
                        <span class="folder-count">({{ getFolderCount(folder.id) }})</span>
                        <div class="folder-actions" v-if="!folder.isDefault" @click.stop>
                            <button class="icon-btn" @click="editFolder(folder)" title="编辑">✏️</button>
                            <button class="icon-btn" @click="deleteFolder(folder)" title="删除">🗑️</button>
                        </div>
                    </div>
                </div>

                <div class="sort-hint">
                    <span>💡 拖拽排序</span>
                </div>
            </div>

            <div class="favorites-content">
                <div class="content-header">
                    <h2>{{ currentFolderName }}</h2>
                    <div class="content-actions">
                        <select v-model="filterType" class="filter-select">
                            <option value="all">全部类型</option>
                            <option value="post">帖子收藏</option>
                            <option value="solution">方案收藏</option>
                        </select>
                    </div>
                </div>

                <div v-if="favorites.length === 0" class="empty-state card">
                    <div class="empty-icon">📚</div>
                    <h3>暂无收藏</h3>
                    <p>去社区发现更多精彩内容吧！</p>
                    <button class="btn btn-primary" @click="$router.push('/posts')">去逛逛</button>
                </div>

                <div v-else class="favorite-list">
                    <div v-for="item in filteredFavorites" :key="item.id" class="favorite-item card">
                        <div class="favorite-content" @click="goToDetail(item)">
                            <h3>{{ item.title }}</h3>
                            <p class="favorite-meta">
                                <span class="target-type">{{ getTypeLabel(item.targetType) }}</span>
                                收藏于 {{ formatDate(item.createdAt) }}
                            </p>
                        </div>
                        <div class="favorite-actions">
                            <select
                                class="move-select"
                                :value="selectedFolderId"
                                @change="moveFavorite(item, $event.target.value)"
                            >
                                <option value="" disabled>移动到...</option>
                                <option v-for="f in folders" :key="f.id" :value="f.id">
                                    {{ f.name }}
                                </option>
                            </select>
                            <button class="btn btn-secondary remove-btn" @click="removeFavorite(item)">
                                取消收藏
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
            <div class="modal-content card" @click.stop>
                <h3>{{ editingFolder ? '编辑收藏夹' : '新建收藏夹' }}</h3>
                <div class="form-group">
                    <label>收藏夹名称</label>
                    <input v-model="folderForm.name" type="text" placeholder="请输入收藏夹名称" />
                </div>
                <div class="form-group">
                    <label>描述（可选）</label>
                    <textarea v-model="folderForm.description" placeholder="请输入收藏夹描述"></textarea>
                </div>
                <div class="modal-actions">
                    <button class="btn btn-secondary" @click="showCreateModal = false">取消</button>
                    <button class="btn btn-primary" @click="saveFolder">确定</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { favoriteApi, favoriteFolderApi } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const folders = ref([])
const favorites = ref([])
const selectedFolderId = ref(null)
const filterType = ref('all')
const showCreateModal = ref(false)
const editingFolder = ref(null)
const folderForm = ref({ name: '', description: '' })
const folderCounts = ref({})

const currentFolderName = computed(() => {
    const folder = folders.value.find(f => f.id === selectedFolderId.value)
    return folder ? folder.name : '全部收藏'
})

const filteredFavorites = computed(() => {
    if (filterType.value === 'all') {
        return favorites.value
    }
    return favorites.value.filter(f => f.targetType === filterType.value)
})

const getFolderCount = (folderId) => {
    return folderCounts.value[folderId] || 0
}

const getTypeLabel = (type) => {
    const labels = { post: '帖子', solution: '方案' }
    return labels[type] || type
}

const formatDate = (date) => {
    if (!date) return ''
    return new Date(date).toLocaleDateString('zh-CN')
}

const selectFolder = async (folderId) => {
    selectedFolderId.value = folderId
    await loadFavorites()
}

const loadFolders = async () => {
    try {
        const res = await favoriteFolderApi.getByUser(userStore.currentUser.id)
        folders.value = res.data
        if (folders.value.length > 0 && !selectedFolderId.value) {
            selectedFolderId.value = folders.value[0].id
        }
        await loadFolderCounts()
    } catch (e) {
        console.error('加载收藏夹失败', e)
        folders.value = [
            { id: 1, name: '默认收藏夹', isDefault: true, description: '系统默认收藏夹' },
            { id: 2, name: '养护技巧', isDefault: false, description: '实用的养护技巧' },
            { id: 3, name: '问题解答', isDefault: false, description: '常见问题解答' }
        ]
        selectedFolderId.value = 1
    }
}

const loadFolderCounts = async () => {
    const counts = {}
    for (const folder of folders.value) {
        try {
            const res = await favoriteFolderApi.getCount(userStore.currentUser.id, folder.id)
            counts[folder.id] = res.data
        } catch (e) {
            counts[folder.id] = 0
        }
    }
    folderCounts.value = counts
}

const loadFavorites = async () => {
    try {
        let res
        if (selectedFolderId.value) {
            res = await favoriteApi.getByFolder(userStore.currentUser.id, selectedFolderId.value)
        } else {
            res = await favoriteApi.getByUser(userStore.currentUser.id)
        }
        favorites.value = res.data
    } catch (e) {
        console.error('加载收藏失败', e)
        favorites.value = [
            { id: 1, targetType: 'post', targetId: 1, title: '绿萝叶子发黄怎么办？', createdAt: '2024-05-28T10:00:00', folderId: 1 },
            { id: 2, targetType: 'post', targetId: 2, title: '多肉黑腐病如何处理', createdAt: '2024-05-25T15:30:00', folderId: 2 },
            { id: 3, targetType: 'solution', targetId: 1, title: '夏季多肉养护技巧', createdAt: '2024-05-20T09:00:00', folderId: 1 }
        ]
    }
}

const goToDetail = (item) => {
    if (item.targetType === 'post') {
        router.push(`/posts/${item.targetId}`)
    }
}

const removeFavorite = async (item) => {
    try {
        if (selectedFolderId.value) {
            await favoriteApi.removeFromFolder(userStore.currentUser.id, selectedFolderId.value, {
                targetType: item.targetType,
                targetId: item.targetId
            })
        } else {
            await favoriteApi.remove({
                userId: userStore.currentUser.id,
                targetType: item.targetType,
                targetId: item.targetId
            })
        }
        favorites.value = favorites.value.filter(f => f.id !== item.id)
        await loadFolderCounts()
    } catch (e) {
        console.error('取消收藏失败', e)
        favorites.value = favorites.value.filter(f => f.id !== item.id)
    }
}

const moveFavorite = async (item, targetFolderId) => {
    if (!targetFolderId) return
    try {
        await favoriteApi.move(userStore.currentUser.id, item.id, Number(targetFolderId))
        await loadFavorites()
        await loadFolderCounts()
    } catch (e) {
        console.error('移动收藏失败', e)
        alert('移动失败，请重试')
    }
}

const editFolder = (folder) => {
    editingFolder.value = folder
    folderForm.value = { name: folder.name, description: folder.description || '' }
    showCreateModal.value = true
}

const deleteFolder = async (folder) => {
    if (!confirm(`确定要删除收藏夹"${folder.name}"吗？该收藏夹下的收藏内容也会被删除。`)) {
        return
    }
    try {
        await favoriteFolderApi.delete(userStore.currentUser.id, folder.id)
        await loadFolders()
        if (selectedFolderId.value === folder.id) {
            selectedFolderId.value = folders.value.length > 0 ? folders.value[0].id : null
        }
        await loadFavorites()
    } catch (e) {
        console.error('删除收藏夹失败', e)
        alert('删除失败，请重试')
    }
}

const saveFolder = async () => {
    if (!folderForm.value.name.trim()) {
        alert('请输入收藏夹名称')
        return
    }
    try {
        if (editingFolder.value) {
            await favoriteFolderApi.update(userStore.currentUser.id, editingFolder.value.id, folderForm.value)
        } else {
            await favoriteFolderApi.create({
                userId: userStore.currentUser.id,
                ...folderForm.value
            })
        }
        showCreateModal.value = false
        editingFolder.value = null
        folderForm.value = { name: '', description: '' }
        await loadFolders()
    } catch (e) {
        console.error('保存收藏夹失败', e)
        alert('保存失败，请重试')
    }
}

onMounted(async () => {
    await loadFolders()
    await loadFavorites()
})
</script>

<style scoped>
.favorites-page {
    max-width: 1200px;
    margin: 0 auto;
}

.favorites-container {
    display: flex;
    gap: 24px;
    align-items: flex-start;
}

.folder-sidebar {
    width: 260px;
    background: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    position: sticky;
    top: 20px;
}

.sidebar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #eee;
}

.sidebar-header h3 {
    margin: 0;
    font-size: 16px;
    color: #1b5e20;
}

.folder-list {
    max-height: 400px;
    overflow-y: auto;
}

.folder-item {
    display: flex;
    align-items: center;
    padding: 10px 12px;
    border-radius: 8px;
    cursor: pointer;
    margin-bottom: 4px;
    transition: all 0.2s;
    gap: 8px;
}

.folder-item:hover {
    background: #f5f5f5;
}

.folder-item.active {
    background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
    color: #2e7d32;
}

.folder-icon {
    font-size: 18px;
}

.folder-name {
    flex: 1;
    font-size: 14px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.folder-count {
    font-size: 12px;
    color: #999;
}

.folder-item.active .folder-count {
    color: #66bb6a;
}

.folder-actions {
    display: none;
    gap: 4px;
}

.folder-item:hover .folder-actions {
    display: flex;
}

.icon-btn {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 14px;
    padding: 2px 4px;
    border-radius: 4px;
    opacity: 0.7;
}

.icon-btn:hover {
    opacity: 1;
    background: rgba(0, 0, 0, 0.05);
}

.sort-hint {
    margin-top: 16px;
    padding-top: 12px;
    border-top: 1px solid #eee;
    text-align: center;
    font-size: 12px;
    color: #999;
}

.favorites-content {
    flex: 1;
    min-width: 0;
}

.content-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.content-header h2 {
    margin: 0;
    color: #1b5e20;
    font-size: 20px;
}

.filter-select {
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 14px;
    background: white;
    cursor: pointer;
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
    margin-bottom: 20px;
}

.favorite-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.favorite-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
}

.favorite-content {
    flex: 1;
    cursor: pointer;
    min-width: 0;
}

.favorite-content h3 {
    font-size: 16px;
    color: #1b5e20;
    margin: 0 0 6px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.favorite-meta {
    font-size: 13px;
    color: #888;
    margin: 0;
}

.target-type {
    display: inline-block;
    padding: 2px 8px;
    background: #e8f5e9;
    color: #388e3c;
    border-radius: 4px;
    font-size: 12px;
    margin-right: 8px;
}

.favorite-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-left: 16px;
    flex-shrink: 0;
}

.move-select {
    padding: 6px 10px;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 13px;
    background: white;
    cursor: pointer;
}

.remove-btn {
    padding: 6px 16px;
    font-size: 13px;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal-content {
    width: 400px;
    padding: 24px;
}

.modal-content h3 {
    margin: 0 0 20px 0;
    color: #1b5e20;
}

.form-group {
    margin-bottom: 16px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    font-size: 14px;
    color: #333;
}

.form-group input,
.form-group textarea {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 14px;
    box-sizing: border-box;
}

.form-group textarea {
    min-height: 80px;
    resize: vertical;
}

.modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
}

.btn-sm {
    padding: 6px 14px;
    font-size: 13px;
}

@media (max-width: 768px) {
    .favorites-container {
        flex-direction: column;
    }

    .folder-sidebar {
        width: 100%;
        position: static;
    }

    .favorite-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
    }

    .favorite-actions {
        width: 100%;
        justify-content: flex-start;
        margin-left: 0;
    }
}
</style>

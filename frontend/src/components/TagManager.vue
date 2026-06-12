<template>
    <div class="tag-manager card">
        <div class="tag-manager-header">
            <h3 class="section-title">🏷️ 标签管理</h3>
            <button class="btn btn-primary btn-sm" @click="showAddModal = true">+ 新建标签</button>
        </div>

        <div v-if="tags.length === 0" class="empty-tags">
            <p>还没有创建标签，点击上方按钮创建您的第一个标签吧！</p>
        </div>

        <div v-else class="tags-list">
            <div
                v-for="tag in tags"
                :key="tag.id"
                class="tag-item"
                :class="{ editing: editingTagId === tag.id }"
            >
                <div v-if="editingTagId === tag.id" class="tag-edit">
                    <input
                        v-model="editingTagName"
                        type="text"
                        class="tag-input"
                        @keyup.enter="saveRename(tag.id)"
                        @keyup.escape="cancelRename"
                        ref="editInput"
                    />
                    <div class="color-picker">
                        <button
                            v-for="color in presetColors"
                            :key="color"
                            class="color-option"
                            :style="{ background: color }"
                            :class="{ active: editingTagColor === color }"
                            @click="editingTagColor = color"
                        ></button>
                    </div>
                    <button class="btn btn-primary btn-xs" @click="saveRename(tag.id)">保存</button>
                    <button class="btn btn-secondary btn-xs" @click="cancelRename">取消</button>
                </div>
                <div v-else class="tag-display">
                    <span
                        class="tag-badge"
                        :style="{ background: tag.color, borderColor: tag.color }"
                    >
                        {{ tag.name }}
                    </span>
                    <span class="tag-count">({{ getTagCount(tag.id) }}株)</span>
                    <div class="tag-actions">
                        <button class="btn-icon" @click="startRename(tag)" title="重命名">✏️</button>
                        <button class="btn-icon" @click="confirmDelete(tag)" title="删除">🗑️</button>
                    </div>
                </div>
            </div>
        </div>

        <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
            <div class="modal card">
                <h2>🏷️ 新建标签</h2>
                <div class="form-group">
                    <label>标签名称</label>
                    <input
                        v-model="newTagName"
                        type="text"
                        placeholder="例如：卧室、办公室、喜阴"
                        @keyup.enter="createTag"
                    />
                </div>
                <div class="form-group">
                    <label>选择颜色</label>
                    <div class="color-picker">
                        <button
                            v-for="color in presetColors"
                            :key="color"
                            class="color-option"
                            :style="{ background: color }"
                            :class="{ active: newTagColor === color }"
                            @click="newTagColor = color"
                        ></button>
                    </div>
                </div>
                <div class="preview-tag">
                    <span>预览：</span>
                    <span
                        class="tag-badge"
                        :style="{ background: newTagColor, borderColor: newTagColor }"
                    >
                        {{ newTagName || '标签名称' }}
                    </span>
                </div>
                <div class="modal-actions">
                    <button class="btn btn-secondary" @click="showAddModal = false">取消</button>
                    <button class="btn btn-primary" @click="createTag" :disabled="!newTagName.trim()">创建</button>
                </div>
            </div>
        </div>

        <div v-if="showDeleteConfirm" class="modal-overlay" @click.self="showDeleteConfirm = false">
            <div class="modal card">
                <h2>⚠️ 确认删除</h2>
                <p>确定要删除标签 "<span class="highlight">{{ deletingTag?.name }}</span>" 吗？</p>
                <p class="warning-text">此操作将同时移除所有植物上的该标签，且无法恢复。</p>
                <div class="modal-actions">
                    <button class="btn btn-secondary" @click="showDeleteConfirm = false">取消</button>
                    <button class="btn btn-danger" @click="deleteTag">确认删除</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, nextTick, defineEmits, defineProps } from 'vue'
import { tagApi, plantArchiveTagApi } from '../api'
import { useUserStore } from '../stores/user'

const props = defineProps({
    refreshTrigger: {
        type: Number,
        default: 0
    }
})

const emit = defineEmits(['tags-updated'])

const userStore = useUserStore()
const tags = ref([])
const tagCounts = ref({})
const showAddModal = ref(false)
const newTagName = ref('')
const newTagColor = ref('#4caf50')

const editingTagId = ref(null)
const editingTagName = ref('')
const editingTagColor = ref('#4caf50')

const showDeleteConfirm = ref(false)
const deletingTag = ref(null)

const editInput = ref(null)

const presetColors = [
    '#4caf50', '#2196f3', '#ff9800', '#f44336',
    '#9c27b0', '#00bcd4', '#ffeb3b', '#795548',
    '#607d8b', '#e91e63', '#009688', '#3f51b5'
]

const getTagCount = (tagId) => {
    return tagCounts.value[tagId] || 0
}

const loadTags = async () => {
    try {
        const res = await tagApi.getByUser(userStore.currentUser.id)
        tags.value = res.data || []
        await loadTagCounts()
    } catch (e) {
        console.error('加载标签失败', e)
        tags.value = []
    }
}

const loadTagCounts = async () => {
    if (tags.value.length === 0) {
        tagCounts.value = {}
        return
    }
    try {
        const tagIds = tags.value.map(t => t.id)
        const res = await plantArchiveTagApi.getCountByTags(tagIds)
        tagCounts.value = res.data || {}
    } catch (e) {
        console.error('加载标签计数失败', e)
        tagCounts.value = {}
    }
}

const createTag = async () => {
    if (!newTagName.value.trim()) return
    try {
        await tagApi.create({
            userId: userStore.currentUser.id,
            name: newTagName.value.trim(),
            color: newTagColor.value
        })
        showAddModal.value = false
        newTagName.value = ''
        newTagColor.value = '#4caf50'
        await loadTags()
        emit('tags-updated')
    } catch (e) {
        console.error('创建标签失败', e)
        alert(e.response?.data?.error || '创建标签失败，请稍后重试')
    }
}

const startRename = (tag) => {
    editingTagId.value = tag.id
    editingTagName.value = tag.name
    editingTagColor.value = tag.color
    nextTick(() => {
        if (editInput.value) {
            editInput.value.focus()
        }
    })
}

const cancelRename = () => {
    editingTagId.value = null
    editingTagName.value = ''
    editingTagColor.value = '#4caf50'
}

const saveRename = async (tagId) => {
    if (!editingTagName.value.trim()) return
    try {
        await tagApi.update(tagId, {
            userId: userStore.currentUser.id,
            name: editingTagName.value.trim(),
            color: editingTagColor.value
        })
        cancelRename()
        await loadTags()
        emit('tags-updated')
    } catch (e) {
        console.error('更新标签失败', e)
        alert(e.response?.data?.error || '更新标签失败，请稍后重试')
    }
}

const confirmDelete = (tag) => {
    deletingTag.value = tag
    showDeleteConfirm.value = true
}

const deleteTag = async () => {
    if (!deletingTag.value) return
    try {
        await tagApi.delete(deletingTag.value.id)
        showDeleteConfirm.value = false
        deletingTag.value = null
        await loadTags()
        emit('tags-updated')
    } catch (e) {
        console.error('删除标签失败', e)
        alert(e.response?.data?.error || '删除标签失败，请稍后重试')
    }
}

onMounted(() => {
    loadTags()
})
</script>

<style scoped>
.tag-manager {
    padding: 20px;
    margin-bottom: 24px;
}

.tag-manager-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.section-title {
    font-size: 18px;
    color: #1b5e20;
    margin: 0;
}

.empty-tags {
    text-align: center;
    color: #888;
    padding: 30px;
}

.tags-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.tag-item {
    display: flex;
    align-items: center;
    padding: 10px 12px;
    background: #fafafa;
    border-radius: 8px;
    transition: background 0.2s;
}

.tag-item:hover {
    background: #f5f5f5;
}

.tag-item.editing {
    background: #e8f5e9;
}

.tag-display {
    display: flex;
    align-items: center;
    gap: 12px;
    width: 100%;
}

.tag-edit {
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
    flex-wrap: wrap;
}

.tag-input {
    flex: 1;
    min-width: 120px;
    padding: 6px 10px;
    border: 1px solid #c5e1a5;
    border-radius: 6px;
    font-size: 14px;
}

.tag-input:focus {
    outline: none;
    border-color: #4caf50;
    box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.1);
}

.tag-badge {
    display: inline-block;
    padding: 4px 12px;
    color: white;
    border-radius: 12px;
    font-size: 13px;
    font-weight: 500;
    border: 2px solid;
}

.tag-count {
    color: #888;
    font-size: 13px;
}

.tag-actions {
    margin-left: auto;
    display: flex;
    gap: 4px;
}

.btn-icon {
    background: none;
    border: none;
    cursor: pointer;
    padding: 4px 8px;
    font-size: 14px;
    border-radius: 4px;
    transition: background 0.2s;
}

.btn-icon:hover {
    background: #e0e0e0;
}

.color-picker {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
}

.color-option {
    width: 24px;
    height: 24px;
    border: 2px solid transparent;
    border-radius: 50%;
    cursor: pointer;
    transition: transform 0.2s, border-color 0.2s;
}

.color-option:hover {
    transform: scale(1.1);
}

.color-option.active {
    border-color: #333;
    transform: scale(1.1);
}

.preview-tag {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: #f5f5f5;
    border-radius: 8px;
    margin-bottom: 16px;
}

.highlight {
    color: #f44336;
    font-weight: 600;
}

.warning-text {
    color: #f44336;
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
    align-items: center;
    justify-content: center;
    z-index: 1000;
    padding: 20px;
}

.modal {
    width: 100%;
    max-width: 400px;
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

.btn-sm {
    padding: 6px 14px;
    font-size: 13px;
}

.btn-xs {
    padding: 4px 10px;
    font-size: 12px;
}

.btn-danger {
    background: linear-gradient(135deg, #f44336 0%, #d32f2f 100%);
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 8px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.2s;
}

.btn-danger:hover {
    transform: translateY(-1px);
    box-shadow: 0 3px 10px rgba(244, 67, 54, 0.35);
}
</style>

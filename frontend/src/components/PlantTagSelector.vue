<template>
    <div class="plant-tag-selector">
        <div class="selector-header">
            <h4 class="selector-title">🏷️ 标签</h4>
            <button v-if="!showSelector" class="btn btn-primary btn-xs" @click="openSelector">
                + 管理标签
            </button>
        </div>

        <div v-if="selectedTags.length > 0" class="selected-tags">
            <span
                v-for="tag in selectedTags"
                :key="tag.id"
                class="tag-badge"
                :style="{ background: tag.color, borderColor: tag.color }"
            >
                {{ tag.name }}
                <button class="tag-remove" @click="removeTag(tag.id)" title="移除标签">×</button>
            </span>
        </div>

        <div v-else class="no-tags">
            <span class="text-muted">暂无标签</span>
        </div>

        <div v-if="showSelector" class="selector-modal-overlay" @click.self="closeSelector">
            <div class="selector-modal card">
                <div class="selector-modal-header">
                    <h3>🏷️ 选择标签</h3>
                    <button class="btn-close" @click="closeSelector">×</button>
                </div>

                <div class="create-tag-section">
                    <div class="create-tag-input-wrapper">
                        <input
                            v-model="newTagName"
                            type="text"
                            class="create-tag-input"
                            placeholder="输入新标签名称..."
                            @keyup.enter="createAndAddTag"
                        />
                        <div class="color-picker-mini">
                            <button
                                v-for="color in presetColors.slice(0, 6)"
                                :key="color"
                                class="color-option-mini"
                                :style="{ background: color }"
                                :class="{ active: newTagColor === color }"
                                @click="newTagColor = color"
                            ></button>
                        </div>
                        <button
                            class="btn btn-primary btn-sm"
                            @click="createAndAddTag"
                            :disabled="!newTagName.trim()"
                        >
                            创建
                        </button>
                    </div>
                </div>

                <div class="available-tags-section">
                    <h4>可用标签</h4>
                    <div v-if="allTags.length === 0" class="empty-state">
                        <p>还没有可用标签，在上方输入框创建一个吧！</p>
                    </div>
                    <div v-else class="tags-grid">
                        <label
                            v-for="tag in allTags"
                            :key="tag.id"
                            class="tag-checkbox"
                            :class="{ checked: isSelected(tag.id) }"
                        >
                            <input
                                type="checkbox"
                                :checked="isSelected(tag.id)"
                                @change="toggleTag(tag)"
                                class="hidden-checkbox"
                            />
                            <span
                                class="tag-badge"
                                :style="{ background: tag.color, borderColor: tag.color }"
                            >
                                {{ tag.name }}
                            </span>
                        </label>
                    </div>
                </div>

                <div class="selector-modal-footer">
                    <button class="btn btn-secondary" @click="closeSelector">关闭</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, defineProps, defineEmits, watch } from 'vue'
import { tagApi, plantArchiveTagApi } from '../api'
import { useUserStore } from '../stores/user'

const props = defineProps({
    plantArchiveId: {
        type: [String, Number],
        required: true
    },
    refreshTrigger: {
        type: Number,
        default: 0
    }
})

const emit = defineEmits(['tags-updated'])

const userStore = useUserStore()
const allTags = ref([])
const selectedTagIds = ref([])
const showSelector = ref(false)
const newTagName = ref('')
const newTagColor = ref('#4caf50')

const presetColors = [
    '#4caf50', '#2196f3', '#ff9800', '#f44336',
    '#9c27b0', '#00bcd4', '#ffeb3b', '#795548',
    '#607d8b', '#e91e63', '#009688', '#3f51b5'
]

const selectedTags = computed(() => {
    return allTags.value.filter(tag => selectedTagIds.value.includes(tag.id))
})

const isSelected = (tagId) => {
    return selectedTagIds.value.includes(tagId)
}

const loadAllTags = async () => {
    try {
        const res = await tagApi.getByUser(userStore.currentUser.id)
        allTags.value = res.data || []
    } catch (e) {
        console.error('加载标签失败', e)
        allTags.value = []
    }
}

const loadSelectedTags = async () => {
    try {
        const res = await plantArchiveTagApi.getByPlant(props.plantArchiveId)
        const tags = res.data || []
        selectedTagIds.value = tags.map(t => t.id)
    } catch (e) {
        console.error('加载植物标签失败', e)
        selectedTagIds.value = []
    }
}

const openSelector = async () => {
    await loadAllTags()
    await loadSelectedTags()
    showSelector.value = true
}

const closeSelector = () => {
    showSelector.value = false
    newTagName.value = ''
    newTagColor.value = '#4caf50'
}

const toggleTag = async (tag) => {
    if (isSelected(tag.id)) {
        await removeTag(tag.id)
    } else {
        await addTag(tag.id)
    }
}

const addTag = async (tagId) => {
    try {
        await plantArchiveTagApi.addTag(props.plantArchiveId, tagId)
        if (!selectedTagIds.value.includes(tagId)) {
            selectedTagIds.value.push(tagId)
        }
        emit('tags-updated')
    } catch (e) {
        console.error('添加标签失败', e)
        alert('添加标签失败，请稍后重试')
    }
}

const removeTag = async (tagId) => {
    try {
        await plantArchiveTagApi.removeTag(props.plantArchiveId, tagId)
        selectedTagIds.value = selectedTagIds.value.filter(id => id !== tagId)
        emit('tags-updated')
    } catch (e) {
        console.error('移除标签失败', e)
        alert('移除标签失败，请稍后重试')
    }
}

const createAndAddTag = async () => {
    if (!newTagName.value.trim()) return
    try {
        const res = await tagApi.create({
            userId: userStore.currentUser.id,
            name: newTagName.value.trim(),
            color: newTagColor.value
        })
        const newTag = res.data
        allTags.value.push(newTag)
        await addTag(newTag.id)
        newTagName.value = ''
        newTagColor.value = '#4caf50'
        emit('tags-updated')
    } catch (e) {
        console.error('创建标签失败', e)
        alert(e.response?.data?.error || '创建标签失败，请稍后重试')
    }
}

watch(() => props.refreshTrigger, () => {
    loadSelectedTags()
})

onMounted(() => {
    loadAllTags()
    loadSelectedTags()
})
</script>

<style scoped>
.plant-tag-selector {
    background: #f9fbe7;
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 16px;
}

.selector-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.selector-title {
    font-size: 14px;
    color: #558b2f;
    margin: 0;
}

.selected-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.tag-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 12px;
    color: white;
    border-radius: 12px;
    font-size: 13px;
    font-weight: 500;
    border: 2px solid;
}

.tag-remove {
    background: none;
    border: none;
    color: white;
    font-size: 16px;
    font-weight: bold;
    cursor: pointer;
    padding: 0;
    line-height: 1;
    opacity: 0.8;
    transition: opacity 0.2s;
}

.tag-remove:hover {
    opacity: 1;
}

.no-tags {
    padding: 8px 0;
}

.text-muted {
    color: #999;
    font-size: 13px;
}

.selector-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1001;
    padding: 20px;
}

.selector-modal {
    width: 100%;
    max-width: 500px;
    max-height: 80vh;
    overflow-y: auto;
}

.selector-modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e8f5e9;
}

.selector-modal-header h3 {
    color: #1b5e20;
    margin: 0;
}

.btn-close {
    background: none;
    border: none;
    font-size: 24px;
    cursor: pointer;
    color: #999;
    padding: 0;
    line-height: 1;
}

.btn-close:hover {
    color: #666;
}

.create-tag-section {
    margin-bottom: 20px;
}

.create-tag-input-wrapper {
    display: flex;
    gap: 10px;
    align-items: center;
    flex-wrap: wrap;
}

.create-tag-input {
    flex: 1;
    min-width: 150px;
    padding: 8px 12px;
    border: 1px solid #c5e1a5;
    border-radius: 6px;
    font-size: 14px;
}

.create-tag-input:focus {
    outline: none;
    border-color: #4caf50;
    box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.1);
}

.color-picker-mini {
    display: flex;
    gap: 4px;
}

.color-option-mini {
    width: 20px;
    height: 20px;
    border: 2px solid transparent;
    border-radius: 50%;
    cursor: pointer;
    transition: transform 0.2s, border-color 0.2s;
}

.color-option-mini:hover {
    transform: scale(1.1);
}

.color-option-mini.active {
    border-color: #333;
    transform: scale(1.1);
}

.available-tags-section h4 {
    color: #1b5e20;
    margin-bottom: 12px;
    font-size: 14px;
}

.empty-state {
    text-align: center;
    color: #888;
    padding: 20px;
    background: #f5f5f5;
    border-radius: 8px;
}

.tags-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.tag-checkbox {
    cursor: pointer;
    user-select: none;
}

.hidden-checkbox {
    position: absolute;
    opacity: 0;
    width: 0;
    height: 0;
}

.tag-checkbox .tag-badge {
    opacity: 0.5;
    transition: opacity 0.2s, transform 0.2s;
}

.tag-checkbox.checked .tag-badge {
    opacity: 1;
    transform: scale(1.05);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.tag-checkbox:hover .tag-badge {
    opacity: 0.8;
}

.tag-checkbox.checked:hover .tag-badge {
    opacity: 1;
}

.selector-modal-footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
    padding-top: 12px;
    border-top: 1px solid #e8f5e9;
}

.btn-sm {
    padding: 6px 14px;
    font-size: 13px;
}

.btn-xs {
    padding: 4px 10px;
    font-size: 12px;
}
</style>

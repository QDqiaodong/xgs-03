<template>
    <div>
        <h1 class="page-title">❤️ 我的收藏</h1>

        <div class="tabs">
            <button
                :class="['tab', activeTab === 'post' && 'active']"
                @click="activeTab = 'post'"
            >
                帖子收藏
            </button>
            <button
                :class="['tab', activeTab === 'solution' && 'active']"
                @click="activeTab = 'solution'"
            >
                方案收藏
            </button>
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
                        收藏于 {{ formatDate(item.createdAt) }}
                    </p>
                </div>
                <button class="btn btn-secondary remove-btn" @click.stop="removeFavorite(item)">
                    取消收藏
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { favoriteApi } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const favorites = ref([])
const activeTab = ref('post')

const filteredFavorites = computed(() => {
    return favorites.value.filter(f => f.targetType === activeTab.value)
})

const formatDate = (date) => {
    if (!date) return ''
    return new Date(date).toLocaleDateString('zh-CN')
}

const goToDetail = (item) => {
    if (item.targetType === 'post') {
        router.push(`/posts/${item.targetId}`)
    }
}

const removeFavorite = async (item) => {
    try {
        await favoriteApi.remove({
            userId: userStore.currentUser.id,
            targetType: item.targetType,
            targetId: item.targetId
        })
        favorites.value = favorites.value.filter(f => f.id !== item.id)
    } catch (e) {
        console.error('取消收藏失败', e)
        favorites.value = favorites.value.filter(f => f.id !== item.id)
    }
}

onMounted(async () => {
    try {
        const res = await favoriteApi.getByUser(userStore.currentUser.id)
        favorites.value = res.data
    } catch (e) {
        console.error('加载失败', e)
        favorites.value = [
            { id: 1, targetType: 'post', targetId: 1, title: '绿萝叶子发黄怎么办？', createdAt: '2024-05-28T10:00:00' },
            { id: 2, targetType: 'post', targetId: 2, title: '多肉黑腐病如何处理', createdAt: '2024-05-25T15:30:00' },
            { id: 3, targetType: 'solution', targetId: 1, title: '夏季多肉养护技巧', createdAt: '2024-05-20T09:00:00' }
        ]
    }
})
</script>

<style scoped>
.tabs {
    display: flex;
    gap: 12px;
    margin-bottom: 24px;
}

.tab {
    padding: 10px 24px;
    border: none;
    background: white;
    border-radius: 8px;
    cursor: pointer;
    font-size: 14px;
    color: #666;
    transition: all 0.3s;
}

.tab.active {
    background: linear-gradient(135deg, #4caf50 0%, #388e3c 100%);
    color: white;
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

.favorite-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.favorite-content {
    flex: 1;
    cursor: pointer;
}

.favorite-content h3 {
    font-size: 16px;
    color: #1b5e20;
    margin-bottom: 6px;
}

.favorite-meta {
    font-size: 13px;
    color: #888;
}

.remove-btn {
    margin-left: 16px;
}
</style>

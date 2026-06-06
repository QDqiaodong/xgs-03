<template>
    <div>
        <div class="hero-section">
            <h1 class="hero-title">🌿 绿植养护交流园地</h1>
            <p class="hero-subtitle">与百万花友一起，分享养花种草的乐趣</p>
        </div>

        <div class="section">
            <h2 class="section-title">热门植物品种</h2>
            <div class="grid grid-4">
                <div v-for="category in categories" :key="category.id" class="category-card card" @click="goToCategory(category.id)">
                    <div class="category-image">
                        <LazyImage
                            :src="getPlantImage(category.name)"
                            :thumbnail="getPlantImage(category.name, true)"
                            :alt="category.name"
                            height="150"
                            mode="list"
                        />
                    </div>
                    <div class="category-info">
                        <h3>{{ category.name }}</h3>
                        <span class="tag">{{ category.category }}</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="section">
            <h2 class="section-title">最新求助</h2>
            <div class="grid grid-2">
                <div v-for="post in posts" :key="post.id" class="post-card card" @click="goToPost(post.id)">
                    <h3>{{ post.title }}</h3>
                    <p class="post-excerpt">{{ post.content.substring(0, 100) }}...</p>
                    <div class="post-meta">
                        <span>👁️ {{ post.viewCount }}</span>
                        <span>💬 {{ post.commentCount }}</span>
                        <span :class="['status', post.isResolved ? 'resolved' : 'pending']">
                            {{ post.isResolved ? '已解决' : '待解决' }}
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <div class="section quick-actions">
            <div class="grid grid-3">
                <div class="action-card card" @click="$router.push('/archives')">
                    <div class="action-icon">📝</div>
                    <h3>绿植建档</h3>
                    <p>记录您的每一盆绿植</p>
                </div>
                <div class="action-card card" @click="$router.push('/categories')">
                    <div class="action-icon">📚</div>
                    <h3>植物百科</h3>
                    <p>查询养护知识</p>
                </div>
                <div class="action-card card" @click="$router.push('/posts')">
                    <div class="action-icon">💡</div>
                    <h3>求助交流</h3>
                    <p>与花友互动分享</p>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { plantCategoryApi, postApi } from '../api'
import LazyImage from '../components/LazyImage.vue'

const router = useRouter()
const categories = ref([])
const posts = ref([])

const getPlantImage = (name, thumbnail = false) => {
    const encodedName = encodeURIComponent(`${name} potted plant`)
    const size = thumbnail ? 'square' : 'landscape_16_9'
    return `https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=${encodedName}&image_size=${size}`
}

const goToCategory = (id) => {
    router.push(`/categories/${id}`)
}

const goToPost = (id) => {
    router.push(`/posts/${id}`)
}

onMounted(async () => {
    try {
        const [catRes, postRes] = await Promise.all([
            plantCategoryApi.getAll(),
            postApi.getList({ postType: 'question', page: 0, size: 4 })
        ])
        categories.value = catRes.data.slice(0, 4)
        posts.value = postRes.data.content || []
    } catch (e) {
        console.error('加载数据失败', e)
        categories.value = [
            { id: 1, name: '绿萝', category: '观叶植物' },
            { id: 2, name: '多肉植物', category: '多肉植物' },
            { id: 3, name: '月季', category: '观花植物' },
            { id: 4, name: '君子兰', category: '观花植物' }
        ]
        posts.value = [
            { id: 1, title: '绿萝叶子发黄怎么办？', content: '最近发现我的绿萝叶子开始发黄，浇水频率是每周一次...', viewCount: 128, commentCount: 12, isResolved: false },
            { id: 2, title: '多肉黑腐病如何处理', content: '夏天到了，我的多肉好像得了黑腐病，根部已经开始变黑...', viewCount: 256, commentCount: 24, isResolved: true }
        ]
    }
})
</script>

<style scoped>
.hero-section {
    text-align: center;
    padding: 60px 20px;
    background: linear-gradient(135deg, rgba(76, 175, 80, 0.1) 0%, rgba(56, 142, 60, 0.15) 100%);
    border-radius: 16px;
    margin-bottom: 40px;
}

.hero-title {
    font-size: 42px;
    color: #1b5e20;
    margin-bottom: 12px;
}

.hero-subtitle {
    font-size: 18px;
    color: #388e3c;
}

.section {
    margin-bottom: 40px;
}

.section-title {
    font-size: 24px;
    color: #1b5e20;
    margin-bottom: 20px;
    font-weight: 600;
}

.category-card {
    cursor: pointer;
    transition: transform 0.3s, box-shadow 0.3s;
    padding: 0;
    overflow: hidden;
}

.category-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.category-image {
    height: 150px;
    overflow: hidden;
}

.category-info {
    padding: 16px;
}

.category-info h3 {
    font-size: 16px;
    margin-bottom: 8px;
    color: #1b5e20;
}

.post-card {
    cursor: pointer;
    transition: transform 0.3s;
}

.post-card:hover {
    transform: translateY(-2px);
}

.post-card h3 {
    font-size: 16px;
    color: #1b5e20;
    margin-bottom: 12px;
}

.post-excerpt {
    color: #666;
    font-size: 14px;
    margin-bottom: 12px;
}

.post-meta {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: #888;
}

.status {
    padding: 2px 8px;
    border-radius: 10px;
    font-size: 12px;
}

.status.resolved {
    background: #e8f5e9;
    color: #388e3c;
}

.status.pending {
    background: #fff3e0;
    color: #f57c00;
}

.quick-actions {
    margin-top: 40px;
}

.action-card {
    text-align: center;
    cursor: pointer;
    transition: transform 0.3s;
}

.action-card:hover {
    transform: translateY(-4px);
}

.action-icon {
    font-size: 48px;
    margin-bottom: 12px;
}

.action-card h3 {
    font-size: 18px;
    color: #1b5e20;
    margin-bottom: 8px;
}

.action-card p {
    color: #666;
    font-size: 14px;
}
</style>

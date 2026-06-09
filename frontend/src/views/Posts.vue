<template>
    <div>
        <div class="page-header">
            <h1 class="page-title">💬 交流社区</h1>
            <button class="btn btn-primary" @click="showAddModal = true">+ 发布求助</button>
        </div>

        <div class="topic-wall card">
            <div class="topic-wall-header">
                <span class="topic-wall-title">🔥 热门话题</span>
                <div class="topic-tabs">
                    <button
                        v-for="tab in topicTabs"
                        :key="tab.value"
                        :class="['topic-tab', activeTopicTab === tab.value && 'active']"
                        @click="activeTopicTab = tab.value"
                    >
                        {{ tab.label }}
                    </button>
                </div>
                <button v-if="selectedTopic" class="clear-filter" @click="clearTopicFilter">
                    ✕ 清除筛选
                </button>
            </div>
            <div class="topic-tags">
                <span
                    v-for="topic in filteredTopics"
                    :key="topic.type"
                    :class="['topic-tag', selectedTopic === topic.type && 'selected', getTagSizeClass(topic.count)]"
                    :style="getTagStyle(topic)"
                    @click="selectTopic(topic)"
                >
                    #{{ topic.name }}
                    <span class="topic-count">{{ topic.count }}</span>
                </span>
                <div v-if="filteredTopics.length === 0" class="no-topics">
                    暂无相关话题
                </div>
            </div>
        </div>

        <div class="tabs">
            <button
                :class="['tab', activeTab === 'question' && 'active']"
                @click="switchTab('question')"
            >
                求助问答
            </button>
            <button
                :class="['tab', activeTab === 'experience' && 'active']"
                @click="switchTab('experience')"
            >
                经验分享
            </button>
        </div>

        <div v-if="selectedTopic" class="filter-info card">
            <span>当前筛选：<strong>{{ selectedTopicName }}</strong> 相关帖子</span>
        </div>

        <div class="post-list">
            <div v-for="post in posts" :key="post.id" class="post-card card" @click="goToDetail(post.id)">
                <div class="post-header">
                    <h3>{{ post.title }}</h3>
                    <span :class="['status', post.isResolved ? 'resolved' : 'pending']">
                        {{ post.isResolved ? '已解决' : '待解决' }}
                    </span>
                </div>
                <p class="post-excerpt">{{ post.content.substring(0, 150) }}...</p>
                <div class="post-meta">
                    <span>👁️ {{ post.viewCount }} 浏览</span>
                    <span>💬 {{ post.commentCount }} 评论</span>
                    <span>👍 {{ post.likeCount }} 点赞</span>
                </div>
            </div>
        </div>

        <div v-if="posts.length === 0" class="empty-state card">
            <div class="empty-icon">💭</div>
            <h3>暂无帖子</h3>
            <p>成为第一个分享的花友吧！</p>
        </div>

        <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
            <div class="modal card">
                <h2>📝 发布{{ activeTab === 'question' ? '求助' : '分享' }}</h2>
                <div class="form-group">
                    <label>标题</label>
                    <input type="text" v-model="newPost.title" :placeholder="activeTab === 'question' ? '请简要描述您的问题' : '请给您的分享起个标题'" />
                </div>
                <div class="form-group">
                    <label>相关植物品种</label>
                    <select v-model="newPost.plantCategoryId">
                        <option value="">不指定</option>
                        <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>内容</label>
                    <textarea v-model="newPost.content" rows="6" :placeholder="activeTab === 'question' ? '请详细描述您遇到的问题，包括症状、养护环境等信息...' : '分享您的养护经验，帮助更多花友...'"></textarea>
                </div>
                <div class="modal-actions">
                    <button class="btn btn-secondary" @click="showAddModal = false">取消</button>
                    <button class="btn btn-primary" @click="createPost">发布</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { postApi, plantCategoryApi } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const posts = ref([])
const categories = ref([])
const topics = ref([])
const activeTab = ref('question')
const activeTopicTab = ref('all')
const selectedTopic = ref(null)
const selectedTopicName = ref('')
const showAddModal = ref(false)
const newPost = ref({
    title: '',
    content: '',
    plantCategoryId: null
})

const topicTabs = [
    { label: '全部', value: 'all' },
    { label: '热门品种', value: '热门品种' },
    { label: '常见问题', value: '常见问题' },
    { label: '养护技巧', value: '养护技巧' }
]

const filteredTopics = computed(() => {
    if (activeTopicTab.value === 'all') {
        return topics.value
    }
    return topics.value.filter(t => t.category === activeTopicTab.value)
})

const maxCount = computed(() => {
    if (topics.value.length === 0) return 1
    return Math.max(...topics.value.map(t => t.count))
})

const getTagSizeClass = (count) => {
    const ratio = count / maxCount.value
    if (ratio > 0.66) return 'size-large'
    if (ratio > 0.33) return 'size-medium'
    return 'size-small'
}

const getTagStyle = (topic) => {
    const colors = {
        '热门品种': { bg: '#e8f5e9', color: '#2e7d32' },
        '常见问题': { bg: '#fff3e0', color: '#e65100' },
        '养护技巧': { bg: '#e3f2fd', color: '#1565c0' }
    }
    const theme = colors[topic.category] || colors['热门品种']
    return {
        background: theme.bg,
        color: theme.color
    }
}

const selectTopic = (topic) => {
    if (selectedTopic.value === topic.type) {
        clearTopicFilter()
        return
    }
    selectedTopic.value = topic.type
    selectedTopicName.value = topic.name
    loadPosts()
}

const clearTopicFilter = () => {
    selectedTopic.value = null
    selectedTopicName.value = ''
    loadPosts()
}

const switchTab = (tab) => {
    activeTab.value = tab
    loadPosts()
}

const goToDetail = (id) => {
    router.push(`/posts/${id}`)
}

const createPost = async () => {
    try {
        const data = {
            ...newPost.value,
            userId: userStore.currentUser.id,
            postType: activeTab.value
        }
        await postApi.create(data)
        showAddModal.value = false
        loadPosts()
        loadTopics()
        newPost.value = {
            title: '',
            content: '',
            plantCategoryId: null
        }
    } catch (e) {
        console.error('发布失败', e)
        posts.value.unshift({
            id: Date.now(),
            ...newPost.value,
            postType: activeTab.value,
            viewCount: 0,
            commentCount: 0,
            likeCount: 0,
            isResolved: false,
            createdAt: new Date().toISOString()
        })
        showAddModal.value = false
    }
}

const loadPosts = async () => {
    try {
        const params = { postType: activeTab.value, page: 0, size: 20 }
        if (selectedTopic.value) {
            if (selectedTopic.value.startsWith('plant_')) {
                params.plantCategoryId = selectedTopic.value.replace('plant_', '')
            } else if (selectedTopic.value.startsWith('keyword_')) {
                params.keyword = selectedTopic.value.replace('keyword_', '')
            }
        }
        const res = await postApi.getList(params)
        posts.value = res.data.content || []
    } catch (e) {
        console.error('加载失败', e)
        posts.value = [
            { id: 1, title: '绿萝叶子发黄怎么办？', content: '最近发现我的绿萝叶子开始发黄，浇水频率是每周一次，放在客厅窗台，有散射光。不知道是什么原因，求大神指点！', viewCount: 128, commentCount: 12, likeCount: 5, isResolved: false },
            { id: 2, title: '多肉黑腐病如何处理', content: '夏天到了，我的多肉好像得了黑腐病，根部已经开始变黑，叶子也开始化水...', viewCount: 256, commentCount: 24, likeCount: 18, isResolved: true },
            { id: 3, title: '君子兰不开花是什么原因？', content: '养了三年的君子兰，一直长得很好，但就是不开花，施肥也正常，光照也充足...', viewCount: 89, commentCount: 8, likeCount: 3, isResolved: false }
        ]
    }
}

const loadTopics = async () => {
    try {
        const res = await postApi.getHotTopics()
        topics.value = res.data || []
    } catch (e) {
        console.error('加载话题失败', e)
        topics.value = [
            { name: '绿萝', category: '热门品种', count: 15, type: 'plant_1' },
            { name: '多肉植物', category: '热门品种', count: 23, type: 'plant_3' },
            { name: '君子兰', category: '热门品种', count: 8, type: 'plant_4' },
            { name: '发黄', category: '常见问题', count: 18, type: 'keyword_发黄' },
            { name: '黑腐', category: '常见问题', count: 12, type: 'keyword_黑腐' },
            { name: '不开花', category: '常见问题', count: 7, type: 'keyword_不开花' },
            { name: '浇水', category: '养护技巧', count: 20, type: 'keyword_浇水' },
            { name: '施肥', category: '养护技巧', count: 14, type: 'keyword_施肥' },
            { name: '光照', category: '养护技巧', count: 11, type: 'keyword_光照' }
        ]
    }
}

onMounted(async () => {
    loadPosts()
    loadTopics()
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

.topic-wall {
    margin-bottom: 20px;
}

.topic-wall-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;
    flex-wrap: wrap;
}

.topic-wall-title {
    font-size: 18px;
    font-weight: 600;
    color: #1b5e20;
}

.topic-tabs {
    display: flex;
    gap: 8px;
    flex: 1;
}

.topic-tab {
    padding: 6px 16px;
    border: none;
    background: #f5f5f5;
    border-radius: 20px;
    cursor: pointer;
    font-size: 13px;
    color: #666;
    transition: all 0.3s;
}

.topic-tab:hover {
    background: #e8f5e9;
    color: #2e7d32;
}

.topic-tab.active {
    background: linear-gradient(135deg, #4caf50 0%, #388e3c 100%);
    color: white;
}

.clear-filter {
    padding: 6px 14px;
    border: 1px solid #ffcdd2;
    background: #ffebee;
    color: #c62828;
    border-radius: 20px;
    cursor: pointer;
    font-size: 13px;
    transition: all 0.3s;
}

.clear-filter:hover {
    background: #ffcdd2;
}

.topic-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
}

.topic-tag {
    display: inline-flex;
    align-items: center;
    padding: 6px 14px;
    border-radius: 20px;
    cursor: pointer;
    font-weight: 500;
    transition: all 0.3s;
    border: 2px solid transparent;
    user-select: none;
}

.topic-tag:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.topic-tag.selected {
    border-color: currentColor;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.topic-tag.size-small {
    font-size: 13px;
    padding: 5px 12px;
}

.topic-tag.size-medium {
    font-size: 15px;
    padding: 7px 16px;
}

.topic-tag.size-large {
    font-size: 18px;
    padding: 8px 20px;
    font-weight: 600;
}

.topic-count {
    margin-left: 6px;
    font-size: 11px;
    opacity: 0.8;
    background: rgba(255, 255, 255, 0.5);
    padding: 1px 6px;
    border-radius: 10px;
}

.no-topics {
    color: #999;
    font-size: 14px;
    padding: 10px 0;
}

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

.filter-info {
    padding: 12px 20px;
    margin-bottom: 16px;
    font-size: 14px;
    color: #558b2f;
    background: linear-gradient(135deg, #f1f8e9 0%, #dcedc8 100%);
    border-left: 4px solid #8bc34a;
}

.post-card {
    cursor: pointer;
    transition: transform 0.3s, box-shadow 0.3s;
    margin-bottom: 16px;
}

.post-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.1);
}

.post-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
}

.post-header h3 {
    font-size: 18px;
    color: #1b5e20;
    margin: 0;
    flex: 1;
}

.status {
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 12px;
    margin-left: 12px;
}

.status.resolved {
    background: #e8f5e9;
    color: #388e3c;
}

.status.pending {
    background: #fff3e0;
    color: #f57c00;
}

.post-excerpt {
    color: #666;
    line-height: 1.6;
    margin-bottom: 16px;
}

.post-meta {
    display: flex;
    gap: 20px;
    font-size: 13px;
    color: #888;
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
    max-width: 600px;
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

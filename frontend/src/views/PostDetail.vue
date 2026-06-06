<template>
    <div v-if="post" class="post-detail">
        <button class="btn btn-secondary back-btn" @click="$router.back()">← 返回列表</button>

        <div class="post-card card">
            <div class="post-header">
                <h1>{{ post.title }}</h1>
                <span :class="['status', post.isResolved ? 'resolved' : 'pending']">
                    {{ post.isResolved ? '已解决' : '待解决' }}
                </span>
            </div>
            <div class="post-meta">
                <span>👁️ {{ post.viewCount }} 浏览</span>
                <span>💬 {{ post.commentCount }} 评论</span>
                <span>👍 {{ post.likeCount }} 点赞</span>
            </div>
            <div class="post-content">
                {{ post.content }}
            </div>
            <div class="post-actions">
                <button class="btn btn-secondary" @click="toggleFavorite">
                    {{ isFavorited ? '❤️ 已收藏' : '🤍 收藏' }}
                </button>
            </div>
        </div>

        <div class="comments-section">
            <h2 class="section-title">💬 评论 ({{ comments.length }})</h2>

            <div class="comment-form card">
                <div class="form-group">
                    <textarea v-model="newComment" rows="3" placeholder="分享您的养护经验，帮助花友解决问题..."></textarea>
                </div>
                <button class="btn btn-primary" @click="submitComment">发表评论</button>
            </div>

            <div v-for="comment in comments" :key="comment.id" class="comment-item card" :class="{ 'best-answer': comment.isBestAnswer }">
                <div v-if="comment.isBestAnswer" class="best-badge">🏆 最佳答案</div>
                <div class="comment-header">
                    <div class="comment-author">
                        <span class="avatar">{{ comment.user?.nickname?.charAt(0) || 'U' }}</span>
                        <span class="author-name">{{ comment.user?.nickname || '匿名用户' }}</span>
                    </div>
                    <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div class="comment-footer">
                    <button class="like-btn" @click="likeComment(comment.id)">👍 {{ comment.likeCount }}</button>
                    <button v-if="!comment.isBestAnswer" class="btn btn-text" @click="setBestAnswer(comment.id)">设为最佳答案</button>
                </div>
            </div>

            <div v-if="comments.length === 0" class="empty-comments card">
                <p>暂无评论，快来分享您的看法吧！</p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { postApi, commentApi, favoriteApi } from '../api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const userStore = useUserStore()
const post = ref(null)
const comments = ref([])
const newComment = ref('')
const isFavorited = ref(false)

const formatDate = (date) => {
    if (!date) return ''
    return new Date(date).toLocaleString('zh-CN')
}

const submitComment = async () => {
    if (!newComment.value.trim()) return
    try {
        await commentApi.create({
            postId: route.params.id,
            userId: userStore.currentUser.id,
            content: newComment.value
        })
        newComment.value = ''
        loadComments()
    } catch (e) {
        console.error('评论失败', e)
        comments.value.unshift({
            id: Date.now(),
            content: newComment.value,
            userId: userStore.currentUser.id,
            user: { nickname: userStore.currentUser.nickname },
            likeCount: 0,
            isBestAnswer: false,
            createdAt: new Date().toISOString()
        })
        newComment.value = ''
    }
}

const likeComment = (id) => {
    const comment = comments.value.find(c => c.id === id)
    if (comment) {
        comment.likeCount++
    }
}

const setBestAnswer = (id) => {
    comments.value.forEach(c => {
        c.isBestAnswer = c.id === id
    })
}

const toggleFavorite = async () => {
    try {
        if (isFavorited.value) {
            await favoriteApi.remove({
                userId: userStore.currentUser.id,
                targetType: 'post',
                targetId: route.params.id
            })
        } else {
            await favoriteApi.add({
                userId: userStore.currentUser.id,
                targetType: 'post',
                targetId: route.params.id,
                title: post.value.title
            })
        }
        isFavorited.value = !isFavorited.value
    } catch (e) {
        console.error('操作失败', e)
        isFavorited.value = !isFavorited.value
    }
}

const loadComments = async () => {
    try {
        const res = await commentApi.getByPost(route.params.id)
        comments.value = res.data
    } catch (e) {
        console.error('加载评论失败', e)
        comments.value = [
            { id: 1, content: '可能是浇水太多了，建议减少浇水频率，绿萝比较耐旱的。检查一下根部有没有腐烂，如果有的话需要剪掉重新发根。', user: { nickname: '养花达人' }, likeCount: 12, isBestAnswer: true, createdAt: '2024-06-01T10:30:00' },
            { id: 2, content: '我之前也遇到过这个问题，后来搬到通风更好的地方就好了。', user: { nickname: '绿植新手' }, likeCount: 3, isBestAnswer: false, createdAt: '2024-06-01T11:20:00' }
        ]
    }
}

const checkFavorite = async () => {
    try {
        const res = await favoriteApi.check({
            userId: userStore.currentUser.id,
            targetType: 'post',
            targetId: route.params.id
        })
        isFavorited.value = res.data
    } catch (e) {
        console.error('检查收藏失败', e)
    }
}

onMounted(async () => {
    try {
        const res = await postApi.getById(route.params.id)
        post.value = res.data
    } catch (e) {
        console.error('加载失败', e)
        post.value = {
            id: route.params.id,
            title: '绿萝叶子发黄怎么办？',
            content: '最近发现我的绿萝叶子开始发黄，浇水频率是每周一次，放在客厅窗台，有散射光。不知道是什么原因，求大神指点！已经持续一周了，越来越严重，好担心它会死掉啊...',
            viewCount: 128,
            commentCount: 12,
            likeCount: 5,
            isResolved: false
        }
    }
    loadComments()
    checkFavorite()
})
</script>

<style scoped>
.back-btn {
    margin-bottom: 20px;
}

.post-card {
    margin-bottom: 24px;
}

.post-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
}

.post-header h1 {
    font-size: 24px;
    color: #1b5e20;
    margin: 0;
    flex: 1;
}

.status {
    padding: 6px 16px;
    border-radius: 16px;
    font-size: 13px;
    margin-left: 16px;
}

.status.resolved {
    background: #e8f5e9;
    color: #388e3c;
}

.status.pending {
    background: #fff3e0;
    color: #f57c00;
}

.post-meta {
    display: flex;
    gap: 20px;
    font-size: 14px;
    color: #888;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #eee;
}

.post-content {
    line-height: 1.8;
    color: #333;
    white-space: pre-wrap;
}

.post-actions {
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid #eee;
}

.section-title {
    font-size: 20px;
    color: #1b5e20;
    margin-bottom: 16px;
}

.comment-form {
    margin-bottom: 20px;
}

.comment-form button {
    margin-top: 12px;
}

.comment-item {
    margin-bottom: 12px;
    position: relative;
}

.comment-item.best-answer {
    border: 2px solid #ffd700;
    background: linear-gradient(135deg, #fffde7 0%, #fff 100%);
}

.best-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    background: #ffd700;
    color: #b8860b;
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
}

.comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.comment-author {
    display: flex;
    align-items: center;
    gap: 10px;
}

.avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4caf50 0%, #388e3c 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
}

.author-name {
    font-weight: 500;
    color: #1b5e20;
}

.comment-date {
    font-size: 12px;
    color: #999;
}

.comment-content {
    color: #333;
    line-height: 1.6;
    margin-bottom: 12px;
}

.comment-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.like-btn {
    border: none;
    background: none;
    cursor: pointer;
    color: #666;
    font-size: 13px;
    padding: 4px 8px;
    border-radius: 4px;
    transition: background 0.2s;
}

.like-btn:hover {
    background: #f5f5f5;
}

.btn-text {
    border: none;
    background: none;
    cursor: pointer;
    color: #4caf50;
    font-size: 13px;
}

.empty-comments {
    text-align: center;
    color: #888;
    padding: 40px;
}
</style>

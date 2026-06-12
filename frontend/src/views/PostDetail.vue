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
                <button class="btn btn-secondary" @click="handleFavoriteClick">
                    {{ isFavorited ? '❤️ 已收藏' : '🤍 收藏' }}
                </button>
            </div>

            <div v-if="showFolderSelect" class="modal-overlay" @click="showFolderSelect = false">
                <div class="modal-content card" @click.stop>
                    <h3>选择收藏夹</h3>
                    <div class="folder-select-list">
                        <div
                            v-for="folder in folders"
                            :key="folder.id"
                            :class="['folder-option', { selected: selectedFolderId === folder.id }]"
                            @click="selectedFolderId = folder.id"
                        >
                            <span class="folder-icon">📁</span>
                            <span class="folder-name">{{ folder.name }}</span>
                            <span v-if="selectedFolderId === folder.id" class="check-icon">✓</span>
                        </div>
                    </div>
                    <div class="modal-actions">
                        <button class="btn btn-secondary" @click="showFolderSelect = false">取消</button>
                        <button class="btn btn-primary" @click="confirmFavorite">确定收藏</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="comments-section">
            <div class="comments-header">
                <h2 class="section-title">💬 评论 ({{ comments.length }})</h2>
                <div class="sort-tabs">
                    <button :class="['sort-btn', { active: sortBy === 'time' }]" @click="changeSort('time')">
                        🕐 最新
                    </button>
                    <button :class="['sort-btn', { active: sortBy === 'likes' }]" @click="changeSort('likes')">
                        🔥 最热
                    </button>
                </div>
            </div>

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
                        <span v-if="comment.isBestAnswer" class="best-reward">+{{ bestAnswerReward }}积分</span>
                    </div>
                    <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div class="comment-footer">
                    <button :class="['like-btn', { liked: comment.liked }]" @click="toggleCommentLike(comment)">
                        {{ comment.liked ? '❤️' : '👍' }} {{ comment.likeCount }}
                    </button>
                    <button
                        v-if="isPostAuthor && !comment.isBestAnswer && comment.userId !== post.userId"
                        class="btn btn-text"
                        @click="setBestAnswer(comment.id)"
                    >
                        设为最佳答案
                    </button>
                    <button
                        v-if="isPostAuthor && comment.isBestAnswer"
                        class="btn btn-text cancel-btn"
                        @click="cancelBestAnswer(comment.id)"
                    >
                        取消最佳答案
                    </button>
                </div>
            </div>

            <div v-if="comments.length === 0" class="empty-comments card">
                <p>暂无评论，快来分享您的看法吧！</p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { postApi, commentApi, favoriteApi, favoriteFolderApi } from '../api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const userStore = useUserStore()
const post = ref(null)
const comments = ref([])
const newComment = ref('')
const isFavorited = ref(false)
const sortBy = ref('time')
const showFolderSelect = ref(false)
const folders = ref([])
const selectedFolderId = ref(null)
const bestAnswerReward = 10

const isPostAuthor = computed(() => {
    return post.value && userStore.currentUser && post.value.userId === userStore.currentUser.id
})

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
            liked: false,
            isBestAnswer: false,
            createdAt: new Date().toISOString()
        })
        newComment.value = ''
    }
}

const toggleCommentLike = async (comment) => {
    try {
        const res = await commentApi.toggleLike(comment.id, userStore.currentUser.id)
        comment.liked = res.data.liked
        comment.likeCount = res.data.comment.likeCount
    } catch (e) {
        console.error('点赞操作失败', e)
        comment.liked = !comment.liked
        comment.likeCount += comment.liked ? 1 : -1
    }
}

const setBestAnswer = async (id) => {
    try {
        const res = await commentApi.setBestAnswer(id, userStore.currentUser.id)
        if (res.data && res.data.comment) {
            comments.value.forEach(c => {
                c.isBestAnswer = c.id === id
            })
            if (post.value) {
                post.value.isResolved = true
            }
            if (res.data.rewardPoints) {
                alert(`✅ 已采纳为最佳答案，回答者获得 ${res.data.rewardPoints} 积分奖励！`)
            }
        }
    } catch (e) {
        console.error('设置最佳答案失败', e)
        if (e.response && e.response.data) {
            alert(e.response.data)
        } else {
            alert('设置最佳答案失败，请重试')
        }
    }
}

const cancelBestAnswer = async (id) => {
    if (!confirm('确定要取消最佳答案吗？取消后回答者的积分奖励将被扣除。')) {
        return
    }
    try {
        const res = await commentApi.cancelBestAnswer(id, userStore.currentUser.id)
        if (res.data && res.data.comment) {
            comments.value.forEach(c => {
                if (c.id === id) {
                    c.isBestAnswer = false
                }
            })
            const hasBestAnswer = comments.value.some(c => c.isBestAnswer)
            if (post.value && !hasBestAnswer) {
                post.value.isResolved = false
            }
        }
    } catch (e) {
        console.error('取消最佳答案失败', e)
        if (e.response && e.response.data) {
            alert(e.response.data)
        } else {
            alert('取消最佳答案失败，请重试')
        }
    }
}

const changeSort = (sort) => {
    sortBy.value = sort
    loadComments()
}

const handleFavoriteClick = async () => {
    if (isFavorited.value) {
        try {
            await favoriteApi.remove({
                userId: userStore.currentUser.id,
                targetType: 'post',
                targetId: route.params.id
            })
        } catch (e) {
            console.error('取消收藏失败', e)
        }
        isFavorited.value = false
    } else {
        await loadFolders()
        if (folders.value.length > 0) {
            selectedFolderId.value = folders.value[0].id
            showFolderSelect.value = true
        }
    }
}

const loadFolders = async () => {
    try {
        const res = await favoriteFolderApi.getByUser(userStore.currentUser.id)
        folders.value = res.data
        if (folders.value.length === 0) {
            const defaultRes = await favoriteFolderApi.getOrCreateDefault(userStore.currentUser.id)
            folders.value = [defaultRes.data]
        }
    } catch (e) {
        console.error('加载收藏夹失败', e)
        folders.value = [
            { id: 1, name: '默认收藏夹', isDefault: true }
        ]
    }
}

const confirmFavorite = async () => {
    try {
        await favoriteApi.add({
            userId: userStore.currentUser.id,
            folderId: selectedFolderId.value,
            targetType: 'post',
            targetId: route.params.id,
            title: post.value.title
        })
        isFavorited.value = true
        showFolderSelect.value = false
    } catch (e) {
        console.error('收藏失败', e)
        alert('收藏失败，请重试')
    }
}

const loadComments = async () => {
    try {
        const res = await commentApi.getByPost(route.params.id, {
            userId: userStore.currentUser.id,
            sortBy: sortBy.value
        })
        comments.value = res.data
    } catch (e) {
        console.error('加载评论失败', e)
        comments.value = [
            { id: 1, userId: 2, content: '可能是浇水太多了，建议减少浇水频率，绿萝比较耐旱的。检查一下根部有没有腐烂，如果有的话需要剪掉重新发根。', user: { nickname: '养花达人' }, likeCount: 12, liked: false, isBestAnswer: true, createdAt: '2024-06-01T10:30:00' },
            { id: 2, userId: 3, content: '我之前也遇到过这个问题，后来搬到通风更好的地方就好了。', user: { nickname: '绿植新手' }, likeCount: 3, liked: false, isBestAnswer: false, createdAt: '2024-06-01T11:20:00' }
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
            userId: 1,
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

.comments-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.section-title {
    font-size: 20px;
    color: #1b5e20;
    margin: 0;
}

.sort-tabs {
    display: flex;
    gap: 8px;
}

.sort-btn {
    padding: 6px 16px;
    border: 1px solid #ddd;
    background: #fff;
    border-radius: 16px;
    cursor: pointer;
    font-size: 13px;
    color: #666;
    transition: all 0.2s;
}

.sort-btn:hover {
    border-color: #4caf50;
    color: #4caf50;
}

.sort-btn.active {
    background: linear-gradient(135deg, #4caf50 0%, #388e3c 100%);
    border-color: #4caf50;
    color: white;
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

.best-reward {
    font-size: 12px;
    color: #ff9800;
    background: #fff3e0;
    padding: 2px 8px;
    border-radius: 10px;
    font-weight: 500;
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
    padding: 4px 12px;
    border-radius: 16px;
    transition: all 0.2s;
}

.like-btn:hover {
    background: #f5f5f5;
    transform: scale(1.05);
}

.like-btn.liked {
    background: #ffebee;
    color: #e53935;
}

.btn-text {
    border: none;
    background: none;
    cursor: pointer;
    color: #4caf50;
    font-size: 13px;
}

.cancel-btn {
    color: #999 !important;
}

.cancel-btn:hover {
    color: #f44336 !important;
}

.empty-comments {
    text-align: center;
    color: #888;
    padding: 40px;
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
    width: 360px;
    padding: 24px;
}

.modal-content h3 {
    margin: 0 0 20px 0;
    color: #1b5e20;
}

.folder-select-list {
    max-height: 300px;
    overflow-y: auto;
    margin-bottom: 20px;
}

.folder-option {
    display: flex;
    align-items: center;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    margin-bottom: 6px;
    transition: all 0.2s;
    gap: 10px;
}

.folder-option:hover {
    background: #f5f5f5;
}

.folder-option.selected {
    background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
    color: #2e7d32;
}

.folder-icon {
    font-size: 20px;
}

.folder-name {
    flex: 1;
    font-size: 14px;
}

.check-icon {
    color: #4caf50;
    font-weight: bold;
}

.modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}
</style>

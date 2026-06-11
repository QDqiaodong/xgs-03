<template>
    <div
        ref="cardRef"
        :class="['post-card card', { 'post-card-lazy': !inView }]"
        @click="handleClick"
    >
        <template v-if="inView">
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
                <span v-if="post.hotnessScore > 0" class="hotness-badge">🔥 {{ formatHotness(post.hotnessScore) }}</span>
            </div>
        </template>
        <template v-else>
            <div class="post-placeholder">
                <div class="placeholder-title"></div>
                <div class="placeholder-line short"></div>
                <div class="placeholder-dots"></div>
            </div>
        </template>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
    post: {
        type: Object,
        required: true
    }
})

const emit = defineEmits(['click'])

const cardRef = ref(null)
const inView = ref(false)
let observer = null

const handleClick = () => {
    emit('click', props.post.id)
}

const formatHotness = (score) => {
    if (score >= 1000) {
        return (score / 1000).toFixed(1) + 'k'
    }
    return Math.round(score)
}

onMounted(() => {
    if ('IntersectionObserver' in window) {
        observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    inView.value = true
                    observer.unobserve(entry.target)
                }
            })
        }, {
            rootMargin: '200px 0px',
            threshold: 0.01
        })

        if (cardRef.value) {
            observer.observe(cardRef.value)
        }
    } else {
        inView.value = true
    }
})

onUnmounted(() => {
    if (observer && cardRef.value) {
        observer.unobserve(cardRef.value)
        observer.disconnect()
    }
})
</script>

<style scoped>
.post-card {
    cursor: pointer;
    transition: transform 0.3s, box-shadow 0.3s;
    margin-bottom: 16px;
    min-height: 120px;
}

.post-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.1);
}

.post-card-lazy {
    cursor: pointer;
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
    align-items: center;
}

.hotness-badge {
    background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
    color: #e65100;
    padding: 2px 10px;
    border-radius: 12px;
    font-weight: 600;
    font-size: 12px;
    margin-left: auto;
}

.post-placeholder {
    padding: 4px 0;
}

.placeholder-title {
    height: 22px;
    background: #f5f5f5;
    border-radius: 4px;
    margin-bottom: 12px;
    width: 70%;
}

.placeholder-line {
    height: 14px;
    background: #f5f5f5;
    border-radius: 4px;
    margin-bottom: 8px;
}

.placeholder-line.short {
    width: 50%;
}

.placeholder-dots {
    display: flex;
    gap: 16px;
    margin-top: 12px;
}

.placeholder-dots::before,
.placeholder-dots::after {
    content: '';
    width: 50px;
    height: 12px;
    background: #f5f5f5;
    border-radius: 4px;
}
</style>

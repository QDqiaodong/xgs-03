<template>
    <div class="achievement-wall card">
        <h2 class="wall-title">🏆 成就徽章</h2>

        <div class="badge-grid">
            <div
                v-for="badge in allBadges"
                :key="badge.badgeType"
                :class="['badge-card', { 'badge-unlocked': badge.unlocked, 'badge-locked': !badge.unlocked }]"
            >
                <div class="badge-icon-wrapper">
                    <span class="badge-icon">{{ badge.badgeIcon || getBadgeEmoji(badge.streakDays) }}</span>
                    <div v-if="badge.unlocked" class="badge-glow"></div>
                </div>
                <div class="badge-info">
                    <h3 class="badge-name">{{ badge.badgeName }}</h3>
                    <p class="badge-desc">{{ badge.description }}</p>
                    <div class="badge-meta">
                        <span class="badge-days">需连续 {{ badge.streakDays }} 天</span>
                        <span v-if="badge.unlocked" class="badge-unlocked-time">
                            ✅ {{ formatDate(badge.unlockedAt) }}
                        </span>
                        <span v-else class="badge-progress">
                            🔒 未解锁
                        </span>
                    </div>
                    <div v-if="!badge.unlocked" class="progress-bar">
                        <div class="progress-fill" :style="{ width: progressPercent(badge.streakDays) + '%' }"></div>
                    </div>
                    <div v-if="!badge.unlocked" class="progress-text">
                        {{ currentStreak }}/{{ badge.streakDays }} 天
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
const props = defineProps({
    achievements: {
        type: Array,
        default: () => []
    },
    lockedAchievements: {
        type: Array,
        default: () => []
    },
    currentStreak: {
        type: Number,
        default: 0
    }
})

import { computed } from 'vue'

const allBadges = computed(() => {
    const unlocked = props.achievements.map(b => ({ ...b, unlocked: true }))
    const locked = props.lockedAchievements.map(b => ({ ...b, unlocked: false }))
    const all = [...unlocked, ...locked]
    all.sort((a, b) => a.streakDays - b.streakDays)
    return all
})

function getBadgeEmoji(streakDays) {
    const map = { 7: '🌱', 30: '🌿', 100: '🌳' }
    return map[streakDays] || '🏅'
}

function progressPercent(target) {
    if (props.currentStreak >= target) return 100
    return Math.round((props.currentStreak / target) * 100)
}

function formatDate(dateStr) {
    if (!dateStr) return ''
    const d = new Date(dateStr)
    return `${d.getMonth() + 1}/${d.getDate()}`
}
</script>

<style scoped>
.achievement-wall {
    padding: 24px;
    margin-bottom: 24px;
}

.wall-title {
    font-size: 20px;
    color: #1b5e20;
    margin: 0 0 20px 0;
}

.badge-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
}

.badge-card {
    display: flex;
    gap: 16px;
    padding: 20px;
    border-radius: 12px;
    border: 2px solid;
    transition: all 0.3s;
}

.badge-unlocked {
    background: linear-gradient(135deg, #fff8e1 0%, #fffde7 100%);
    border-color: #ffd54f;
    box-shadow: 0 4px 16px rgba(255, 213, 79, 0.25);
}

.badge-unlocked:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 24px rgba(255, 213, 79, 0.4);
}

.badge-locked {
    background: #fafafa;
    border-color: #e0e0e0;
    opacity: 0.7;
}

.badge-locked:hover {
    opacity: 0.85;
}

.badge-icon-wrapper {
    position: relative;
    flex-shrink: 0;
}

.badge-icon {
    font-size: 40px;
    display: block;
    line-height: 1;
}

.badge-glow {
    position: absolute;
    inset: -4px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(255, 215, 0, 0.3) 0%, transparent 70%);
    animation: glow 2s ease-in-out infinite alternate;
}

@keyframes glow {
    from { opacity: 0.5; transform: scale(0.95); }
    to { opacity: 1; transform: scale(1.05); }
}

.badge-info {
    flex: 1;
    min-width: 0;
}

.badge-name {
    font-size: 16px;
    color: #1b5e20;
    margin: 0 0 6px 0;
}

.badge-desc {
    font-size: 13px;
    color: #666;
    margin: 0 0 8px 0;
    line-height: 1.4;
}

.badge-meta {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    font-size: 12px;
    margin-bottom: 6px;
}

.badge-days {
    color: #888;
}

.badge-unlocked-time {
    color: #4caf50;
    font-weight: 500;
}

.badge-progress {
    color: #999;
}

.progress-bar {
    height: 6px;
    background: #e0e0e0;
    border-radius: 3px;
    overflow: hidden;
    margin-bottom: 4px;
}

.progress-fill {
    height: 100%;
    background: linear-gradient(90deg, #4caf50, #66bb6a);
    border-radius: 3px;
    transition: width 0.5s ease;
}

.progress-text {
    font-size: 11px;
    color: #888;
    text-align: right;
}

@media (max-width: 768px) {
    .badge-grid {
        grid-template-columns: 1fr;
    }

    .badge-icon {
        font-size: 32px;
    }
}
</style>

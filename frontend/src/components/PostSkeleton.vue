<template>
    <div class="post-skeleton card">
        <div class="skeleton-header">
            <div class="skeleton-title skeleton-line"></div>
            <div class="skeleton-status skeleton-shape"></div>
        </div>
        <div class="skeleton-content">
            <div class="skeleton-line skeleton-line-long"></div>
            <div class="skeleton-line skeleton-line-medium"></div>
            <div class="skeleton-line skeleton-line-short"></div>
        </div>
        <div class="skeleton-meta">
            <div class="skeleton-line skeleton-line-tiny"></div>
            <div class="skeleton-line skeleton-line-tiny"></div>
            <div class="skeleton-line skeleton-line-tiny"></div>
        </div>
    </div>
</template>

<script setup>
</script>

<style scoped>
.post-skeleton {
    margin-bottom: 16px;
}

.skeleton-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
    gap: 12px;
}

.skeleton-title {
    flex: 1;
    height: 24px;
}

.skeleton-status {
    width: 56px;
    height: 24px;
    border-radius: 12px;
    flex-shrink: 0;
}

.skeleton-content {
    margin-bottom: 16px;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.skeleton-meta {
    display: flex;
    gap: 20px;
}

.skeleton-line {
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    border-radius: 4px;
    height: 16px;
}

.skeleton-shape {
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
}

.skeleton-line-long {
    width: 100%;
}

.skeleton-line-medium {
    width: 80%;
}

.skeleton-line-short {
    width: 60%;
}

.skeleton-line-tiny {
    width: 60px;
    height: 13px;
}

@keyframes shimmer {
    0% {
        background-position: -200% 0;
    }
    100% {
        background-position: 200% 0;
    }
}
</style>

<template>
  <div v-if="compareList.length > 0" class="compare-bar">
    <div class="compare-bar-inner">
      <div class="compare-bar-info">
        <span class="compare-bar-icon">🔍</span>
        <span class="compare-bar-text">
          已选择 <strong>{{ compareList.length }}</strong> / {{ maxCompare }} 个品种进行对比
        </span>
      </div>
      <div class="compare-bar-items">
        <div
          v-for="item in compareList"
          :key="item.id"
          class="compare-bar-item"
        >
          <span class="compare-item-name">{{ item.name }}</span>
          <button
            class="compare-item-remove"
            @click.stop="$emit('remove', item.id)"
            title="移除对比"
          >
            ×
          </button>
        </div>
        <div
          v-for="i in (maxCompare - compareList.length)"
          :key="'empty-' + i"
          class="compare-bar-item empty"
        >
          <span class="compare-item-placeholder">待选择</span>
        </div>
      </div>
      <div class="compare-bar-actions">
        <button
          class="btn btn-secondary btn-sm"
          @click="$emit('clear')"
        >
          清空
        </button>
        <button
          class="btn btn-primary btn-sm"
          :disabled="compareList.length < 2"
          @click="$emit('compare')"
        >
          ⚖️ 开始对比
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  compareList: {
    type: Array,
    required: true
  },
  maxCompare: {
    type: Number,
    default: 3
  }
})

defineEmits(['remove', 'clear', 'compare'])
</script>

<style scoped>
.compare-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, #ffffff 100%);
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  z-index: 500;
  padding: 12px 20px;
  backdrop-filter: blur(8px);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.compare-bar-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.compare-bar-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #558b2f;
  font-size: 14px;
  white-space: nowrap;
}

.compare-bar-icon {
  font-size: 20px;
}

.compare-bar-info strong {
  color: #2e7d32;
  font-size: 16px;
}

.compare-bar-items {
  flex: 1;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  min-width: 0;
}

.compare-bar-item {
  display: flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  padding: 6px 6px 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  color: #1b5e20;
  font-weight: 500;
  animation: tagIn 0.25s ease;
}

@keyframes tagIn {
  from {
    opacity: 0;
    transform: scale(0.85);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.compare-bar-item.empty {
  background: #f5f5f5;
  border: 1px dashed #bdbdbd;
  padding: 6px 16px;
}

.compare-item-name {
  white-space: nowrap;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.compare-item-placeholder {
  color: #999;
  font-style: italic;
}

.compare-item-remove {
  width: 20px;
  height: 20px;
  border: none;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 50%;
  color: #2e7d32;
  font-size: 16px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  padding: 0;
}

.compare-item-remove:hover {
  background: #ef5350;
  color: white;
  transform: rotate(90deg);
}

.compare-bar-actions {
  display: flex;
  gap: 10px;
  white-space: nowrap;
}

.btn-sm {
  padding: 8px 16px;
  font-size: 13px;
}

.btn-primary:disabled {
  background: #bdbdbd;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@media (max-width: 768px) {
  .compare-bar-inner {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .compare-bar-info {
    justify-content: center;
  }

  .compare-bar-actions {
    justify-content: center;
  }
}
</style>

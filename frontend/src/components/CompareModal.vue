<template>
  <div v-if="visible" class="compare-modal-overlay" @click.self="$emit('close')">
    <div class="compare-modal card">
      <div class="compare-modal-header">
        <h2>⚖️ 品种对比</h2>
        <button class="btn-close" @click="$emit('close')" title="关闭">×</button>
      </div>

      <div class="compare-modal-body">
        <div class="compare-table-wrapper">
          <table class="compare-table">
            <thead>
              <tr>
                <th class="param-header">
                  <span class="param-label">养护参数</span>
                </th>
                <th
                  v-for="item in compareList"
                  :key="item.id"
                  class="plant-header"
                >
                  <div class="plant-header-inner">
                    <div class="plant-avatar">
                      <LazyImage
                        :src="getPlantImage(item.name)"
                        :alt="item.name"
                        height="60"
                        mode="list"
                      />
                    </div>
                    <div class="plant-info">
                      <h3 class="plant-name">{{ item.name }}</h3>
                      <span class="plant-category">{{ item.category }}</span>
                    </div>
                    <button
                      class="plant-remove"
                      @click="$emit('remove', item.id)"
                      title="移除"
                    >
                      ×
                    </button>
                  </div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="row in compareRows"
                :key="row.key"
                class="compare-row"
              >
                <td class="param-cell">
                  <span class="param-icon">{{ row.icon }}</span>
                  <span class="param-name">{{ row.label }}</span>
                </td>
                <td
                  v-for="(item, idx) in compareList"
                  :key="item.id + '-' + row.key"
                  class="value-cell"
                  :class="{
                    'value-different': isDifferent(row.key, idx),
                    'value-same': !isDifferent(row.key, idx)
                  }"
                >
                  <span class="value-text">
                    {{ getValue(item, row.key) || '—' }}
                  </span>
                  <span
                    v-if="isDifferent(row.key, idx)"
                    class="diff-badge"
                    title="该项与其他品种存在差异"
                  >
                    差异
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="compare-legend">
          <div class="legend-item">
            <span class="legend-color same"></span>
            <span class="legend-text">参数相同</span>
          </div>
          <div class="legend-item">
            <span class="legend-color different"></span>
            <span class="legend-text">参数不同（高亮）</span>
          </div>
        </div>
      </div>

      <div class="compare-modal-footer">
        <button class="btn btn-secondary" @click="$emit('close')">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import LazyImage from './LazyImage.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  compareList: {
    type: Array,
    required: true
  }
})

defineEmits(['close', 'remove'])

const compareRows = [
  { key: 'lightRequirement', label: '光照需求', icon: '☀️' },
  { key: 'waterRequirement', label: '浇水频率', icon: '💧' },
  { key: 'temperatureRange', label: '适宜温度', icon: '🌡️' },
  { key: 'humidity', label: '湿度要求', icon: '💨' },
  { key: 'fertilization', label: '施肥建议', icon: '🧪' },
  { key: 'commonDiseases', label: '常见病害', icon: '⚠️' }
]

const getPlantImage = (name) => {
  const encodedName = encodeURIComponent(`${name} potted plant`)
  return `https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=${encodedName}&image_size=square`
}

const getValue = (item, key) => {
  const value = item[key]
  if (value === null || value === undefined || value === '') {
    return null
  }
  return value
}

const normalizeValue = (val) => {
  if (val === null || val === undefined) return ''
  return String(val).trim().toLowerCase()
}

const isDifferent = (rowKey, columnIndex) => {
  if (props.compareList.length < 2) return false

  const values = props.compareList.map((item) =>
    normalizeValue(getValue(item, rowKey))
  )
  const currentValue = values[columnIndex]

  if (!currentValue) return false

  return values.some((v, i) => i !== columnIndex && v !== currentValue && v !== '')
}
</script>

<style scoped>
.compare-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.compare-modal {
  width: 100%;
  max-width: 1000px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
  animation: zoomIn 0.25s ease;
}

@keyframes zoomIn {
  from {
    transform: scale(0.95);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.compare-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e8f5e9;
}

.compare-modal-header h2 {
  color: #1b5e20;
  font-size: 20px;
  margin: 0;
}

.btn-close {
  width: 36px;
  height: 36px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  transition: all 0.2s;
}

.btn-close:hover {
  background: #ef5350;
  color: white;
  transform: rotate(90deg);
}

.compare-modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.compare-table-wrapper {
  overflow-x: auto;
  border: 1px solid #e8f5e9;
  border-radius: 12px;
  background: white;
}

.compare-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  min-width: 600px;
}

.compare-table th,
.compare-table td {
  padding: 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  vertical-align: middle;
}

.compare-table thead th {
  background: linear-gradient(135deg, #f8faf5 0%, #e8f5e9 100%);
  font-weight: 600;
  position: sticky;
  top: 0;
  z-index: 10;
}

.param-header {
  width: 140px;
  min-width: 140px;
}

.param-label {
  color: #1b5e20;
  font-size: 14px;
}

.plant-header {
  min-width: 180px;
}

.plant-header-inner {
  display: flex;
  align-items: center;
  gap: 12px;
}

.plant-avatar {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
}

.plant-info {
  flex: 1;
  min-width: 0;
}

.plant-name {
  font-size: 15px;
  color: #1b5e20;
  margin: 0 0 2px 0;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.plant-category {
  font-size: 12px;
  color: #888;
  background: #f1f8e9;
  padding: 2px 8px;
  border-radius: 10px;
}

.plant-remove {
  width: 24px;
  height: 24px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  transition: all 0.2s;
  padding: 0;
  flex-shrink: 0;
}

.plant-remove:hover {
  background: #ef5350;
  color: white;
}

.compare-row:hover .value-cell {
  background: #fafcfa;
}

.param-cell {
  background: #f8faf5;
  font-weight: 500;
  color: #33691e;
  display: flex;
  align-items: center;
  gap: 8px;
  border-right: 1px solid #e8f5e9;
}

.param-icon {
  font-size: 18px;
}

.param-name {
  font-size: 13px;
}

.value-cell {
  position: relative;
  transition: background 0.2s;
}

.value-cell.value-same {
  background: #ffffff;
}

.value-cell.value-different {
  background: linear-gradient(135deg, #fff8e1 0%, #ffecb3 100%);
}

.value-text {
  display: block;
  font-size: 13px;
  color: #444;
  line-height: 1.6;
}

.diff-badge {
  display: inline-block;
  margin-top: 6px;
  padding: 2px 8px;
  background: #ff9800;
  color: white;
  font-size: 10px;
  border-radius: 8px;
  font-weight: 500;
}

.compare-legend {
  display: flex;
  gap: 24px;
  margin-top: 20px;
  justify-content: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}

.legend-color.same {
  background: #ffffff;
}

.legend-color.different {
  background: linear-gradient(135deg, #fff8e1 0%, #ffecb3 100%);
}

.legend-text {
  font-size: 13px;
}

.compare-modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e8f5e9;
  display: flex;
  justify-content: flex-end;
  background: #fafafa;
}

@media (max-width: 768px) {
  .compare-modal {
    max-height: 90vh;
  }

  .compare-modal-header {
    padding: 16px;
  }

  .compare-modal-body {
    padding: 16px;
  }

  .compare-table th,
  .compare-table td {
    padding: 12px 10px;
  }

  .param-header {
    width: 100px;
    min-width: 100px;
  }

  .param-name {
    font-size: 12px;
  }

  .plant-header-inner {
    gap: 8px;
  }

  .plant-avatar {
    width: 40px;
    height: 40px;
  }

  .compare-legend {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}
</style>

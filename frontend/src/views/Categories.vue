<template>
    <div>
        <div class="page-header">
            <h1 class="page-title">🌿 植物百科</h1>
            <div v-if="compareCount > 0" class="header-hint">
                💡 点击卡片上的 ☐ 勾选品种进行对比（最多{{ MAX_COMPARE }}个）
            </div>
        </div>

        <div class="search-bar card">
            <input
                type="text"
                v-model="searchKeyword"
                placeholder="搜索植物品种..."
                @input="handleSearch"
            />
            <select v-model="selectedCategory" @change="filterByCategory">
                <option value="">全部分类</option>
                <option value="观叶植物">观叶植物</option>
                <option value="观花植物">观花植物</option>
                <option value="多肉植物">多肉植物</option>
            </select>
        </div>

        <div class="grid grid-3">
            <div
                v-for="category in filteredCategories"
                :key="category.id"
                class="category-card card"
                :class="{ 'card-selected': isInCompare(category.id) }"
                @click="goToDetail(category.id)"
            >
                <div class="compare-checkbox" @click.stop="handleToggleCompare(category)">
                    <label class="checkbox-wrapper">
                        <input
                            type="checkbox"
                            :checked="isInCompare(category.id)"
                            :disabled="!isInCompare(category.id) && !canAddToCompare"
                            @change="handleToggleCompare(category)"
                        />
                        <span
                            class="checkbox-custom"
                            :class="{ disabled: !isInCompare(category.id) && !canAddToCompare }"
                        >
                            <span v-if="isInCompare(category.id)" class="check-icon">✓</span>
                        </span>
                    </label>
                    <span v-if="isInCompare(category.id)" class="compare-badge">
                        对比中
                    </span>
                </div>

                <div class="category-image">
                    <LazyImage
                        :src="getPlantImage(category.name)"
                        :thumbnail="getPlantImage(category.name, true)"
                        :alt="category.name"
                        height="180"
                        mode="list"
                    />
                </div>
                <div class="category-content">
                    <div class="category-header">
                        <h3>{{ category.name }}</h3>
                        <span class="tag">{{ category.category }}</span>
                    </div>
                    <p class="category-desc">{{ category.description }}</p>
                    <div class="category-tags">
                        <span class="mini-tag">☀️ {{ category.lightRequirement }}</span>
                        <span class="mini-tag">💧 {{ category.waterRequirement }}</span>
                    </div>
                </div>
            </div>
        </div>

        <CompareBar
            :compare-list="compareList"
            :max-compare="MAX_COMPARE"
            @remove="removeFromCompare"
            @clear="clearCompare"
            @compare="openCompareModal"
        />

        <CompareModal
            :visible="showCompareModal"
            :compare-list="compareList"
            @close="closeCompareModal"
            @remove="removeFromCompare"
        />
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { plantCategoryApi } from '../api'
import LazyImage from '../components/LazyImage.vue'
import CompareBar from '../components/CompareBar.vue'
import CompareModal from '../components/CompareModal.vue'
import { useCompare } from '../composables/useCompare'

const router = useRouter()
const categories = ref([])
const searchKeyword = ref('')
const selectedCategory = ref('')

const {
    compareList,
    showCompareModal,
    isInCompare,
    canAddToCompare,
    compareCount,
    removeFromCompare,
    toggleCompare,
    clearCompare,
    openCompareModal,
    closeCompareModal,
    MAX_COMPARE
} = useCompare()

const filteredCategories = computed(() => {
    let result = categories.value
    if (selectedCategory.value) {
        result = result.filter(c => c.category === selectedCategory.value)
    }
    if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        result = result.filter(c =>
            c.name.toLowerCase().includes(keyword) ||
            c.category.toLowerCase().includes(keyword)
        )
    }
    return result
})

const getPlantImage = (name, thumbnail = false) => {
    const encodedName = encodeURIComponent(`${name} potted plant`)
    const size = thumbnail ? 'square' : 'landscape_4_3'
    return `https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=${encodedName}&image_size=${size}`
}

const goToDetail = (id) => {
    router.push(`/categories/${id}`)
}

const handleToggleCompare = (category) => {
    const added = toggleCompare(category)
    if (!added && !isInCompare(category.id) && !canAddToCompare.value) {
        alert(`最多只能对比 ${MAX_COMPARE} 个品种，请先移除已选择的品种`)
    }
}

const handleSearch = () => {
}

const filterByCategory = () => {
}

onMounted(async () => {
    try {
        const res = await plantCategoryApi.getAll()
        categories.value = res.data
    } catch (e) {
        console.error('加载失败', e)
        categories.value = [
            { id: 1, name: '绿萝', category: '观叶植物', description: '绿萝是非常常见的室内观叶植物，具有很强的空气净化能力。', lightRequirement: '散射光', waterRequirement: '保持湿润', temperatureRange: '15-30℃', humidity: '40-70%', fertilization: '生长季每月施一次稀薄液肥', commonDiseases: '叶斑病、根腐病' },
            { id: 2, name: '吊兰', category: '观叶植物', description: '吊兰适应性强，是新手养植的首选。', lightRequirement: '明亮散射光', waterRequirement: '保持湿润', temperatureRange: '15-25℃', humidity: '50-70%', fertilization: '每月施一次复合肥', commonDiseases: '叶枯病、蚜虫' },
            { id: 3, name: '多肉植物', category: '多肉植物', description: '多肉植物品种繁多，形态各异，需水量少。', lightRequirement: '充足阳光', waterRequirement: '干透浇透', temperatureRange: '10-30℃', humidity: '30-50%', fertilization: '生长季每月施一次稀释肥', commonDiseases: '黑腐病、介壳虫' },
            { id: 4, name: '君子兰', category: '观花植物', description: '君子兰叶片肥厚有光泽，花期长。', lightRequirement: '明亮散射光', waterRequirement: '见干见湿', temperatureRange: '15-25℃', humidity: '40-60%', fertilization: '花期前增施磷钾肥', commonDiseases: '叶斑病、根腐病' },
            { id: 5, name: '发财树', category: '观叶植物', description: '发财树寓意招财进宝，是办公室和家居装饰的热门选择。', lightRequirement: '散射光', waterRequirement: '宁干勿湿', temperatureRange: '18-30℃', humidity: '40-60%', fertilization: '春秋季每月施一次复合肥', commonDiseases: '叶斑病、根腐病' },
            { id: 6, name: '虎皮兰', category: '观叶植物', description: '虎皮兰夜间释放氧气，非常适合放置在卧室。', lightRequirement: '适应性强', waterRequirement: '耐干旱', temperatureRange: '10-30℃', humidity: '30-60%', fertilization: '生长季每月施一次稀释肥', commonDiseases: '细菌性软腐病' }
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
    flex-wrap: wrap;
    gap: 12px;
}

.header-hint {
    font-size: 13px;
    color: #558b2f;
    background: #e8f5e9;
    padding: 6px 14px;
    border-radius: 20px;
}

.search-bar {
    display: flex;
    gap: 16px;
    margin-bottom: 24px;
}

.search-bar input {
    flex: 1;
    padding: 12px 16px;
    border: 1px solid #c5e1a5;
    border-radius: 8px;
    font-size: 14px;
}

.search-bar select {
    padding: 12px 16px;
    border: 1px solid #c5e1a5;
    border-radius: 8px;
    font-size: 14px;
    min-width: 140px;
}

.category-card {
    cursor: pointer;
    transition: transform 0.3s, box-shadow 0.3s;
    padding: 0;
    overflow: hidden;
    position: relative;
}

.category-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.category-card.card-selected {
    border: 2px solid #4caf50;
    box-shadow: 0 4px 20px rgba(76, 175, 80, 0.25);
}

.compare-checkbox {
    position: absolute;
    top: 12px;
    left: 12px;
    z-index: 10;
    display: flex;
    align-items: center;
    gap: 8px;
}

.checkbox-wrapper {
    position: relative;
    cursor: pointer;
    display: inline-flex;
}

.checkbox-wrapper input {
    position: absolute;
    opacity: 0;
    width: 0;
    height: 0;
}

.checkbox-custom {
    width: 24px;
    height: 24px;
    border: 2px solid #fff;
    border-radius: 6px;
    background: rgba(255, 255, 255, 0.95);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.checkbox-wrapper:hover .checkbox-custom:not(.disabled) {
    border-color: #4caf50;
    background: #f1f8e9;
}

.checkbox-custom.disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.check-icon {
    color: #fff;
    font-size: 14px;
    font-weight: bold;
    line-height: 1;
}

.checkbox-wrapper input:checked + .checkbox-custom {
    background: linear-gradient(135deg, #4caf50 0%, #388e3c 100%);
    border-color: #388e3c;
}

.compare-badge {
    background: linear-gradient(135deg, #4caf50 0%, #388e3c 100%);
    color: white;
    font-size: 11px;
    padding: 3px 10px;
    border-radius: 12px;
    font-weight: 500;
    box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
}

.category-image {
    height: 180px;
    overflow: hidden;
}

.category-content {
    padding: 16px;
}

.category-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.category-header h3 {
    font-size: 18px;
    color: #1b5e20;
    margin: 0;
}

.category-desc {
    color: #666;
    font-size: 14px;
    margin-bottom: 12px;
    line-height: 1.6;
}

.category-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.mini-tag {
    font-size: 12px;
    color: #558b2f;
    background: #f1f8e9;
    padding: 4px 8px;
    border-radius: 12px;
}
</style>

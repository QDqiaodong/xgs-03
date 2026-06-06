<template>
    <div>
        <h1 class="page-title">🌿 植物百科</h1>

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
            <div v-for="category in filteredCategories" :key="category.id" class="category-card card" @click="goToDetail(category.id)">
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
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { plantCategoryApi } from '../api'
import LazyImage from '../components/LazyImage.vue'

const router = useRouter()
const categories = ref([])
const searchKeyword = ref('')
const selectedCategory = ref('')

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
            { id: 1, name: '绿萝', category: '观叶植物', description: '绿萝是非常常见的室内观叶植物，具有很强的空气净化能力。', lightRequirement: '散射光', waterRequirement: '保持湿润' },
            { id: 2, name: '吊兰', category: '观叶植物', description: '吊兰适应性强，是新手养植的首选。', lightRequirement: '明亮散射光', waterRequirement: '保持湿润' },
            { id: 3, name: '多肉植物', category: '多肉植物', description: '多肉植物品种繁多，形态各异，需水量少。', lightRequirement: '充足阳光', waterRequirement: '干透浇透' },
            { id: 4, name: '君子兰', category: '观花植物', description: '君子兰叶片肥厚有光泽，花期长。', lightRequirement: '明亮散射光', waterRequirement: '见干见湿' },
            { id: 5, name: '发财树', category: '观叶植物', description: '发财树寓意招财进宝，是办公室和家居装饰的热门选择。', lightRequirement: '散射光', waterRequirement: '宁干勿湿' },
            { id: 6, name: '虎皮兰', category: '观叶植物', description: '虎皮兰夜间释放氧气，非常适合放置在卧室。', lightRequirement: '适应性强', waterRequirement: '耐干旱' }
        ]
    }
})
</script>

<style scoped>
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
}

.category-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
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

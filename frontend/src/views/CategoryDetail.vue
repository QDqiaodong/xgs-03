<template>
    <div v-if="category" class="category-detail">
        <button class="btn btn-secondary back-btn" @click="$router.back()">← 返回</button>

        <div class="detail-header card">
            <div class="detail-image">
                <LazyImage
                    :src="getPlantImage(category.name)"
                    :alt="category.name"
                    height="300"
                    mode="detail"
                />
            </div>
            <div class="detail-info">
                <h1>{{ category.name }}</h1>
                <span class="tag">{{ category.category }}</span>
                <p class="scientific-name">{{ category.scientificName }}</p>
                <p class="description">{{ category.description }}</p>
                <button class="btn btn-primary" @click="addToArchive">+ 添加到我的绿植</button>
            </div>
        </div>

        <div class="care-guide card">
            <h2 class="section-title">📋 养护指南</h2>
            <div class="grid grid-2">
                <div class="guide-item">
                    <div class="guide-icon">☀️</div>
                    <div>
                        <h3>光照需求</h3>
                        <p>{{ category.lightRequirement }}</p>
                    </div>
                </div>
                <div class="guide-item">
                    <div class="guide-icon">💧</div>
                    <div>
                        <h3>浇水需求</h3>
                        <p>{{ category.waterRequirement }}</p>
                    </div>
                </div>
                <div class="guide-item">
                    <div class="guide-icon">🌡️</div>
                    <div>
                        <h3>适宜温度</h3>
                        <p>{{ category.temperatureRange }}</p>
                    </div>
                </div>
                <div class="guide-item">
                    <div class="guide-icon">💨</div>
                    <div>
                        <h3>湿度要求</h3>
                        <p>{{ category.humidity }}</p>
                    </div>
                </div>
                <div class="guide-item">
                    <div class="guide-icon">🧪</div>
                    <div>
                        <h3>施肥建议</h3>
                        <p>{{ category.fertilization }}</p>
                    </div>
                </div>
                <div class="guide-item">
                    <div class="guide-icon">✂️</div>
                    <div>
                        <h3>修剪建议</h3>
                        <p>{{ category.pruning }}</p>
                    </div>
                </div>
            </div>
        </div>

        <div v-if="category.commonDiseases" class="diseases card">
            <h2 class="section-title">⚠️ 常见病害</h2>
            <p>{{ category.commonDiseases }}</p>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { plantCategoryApi } from '../api'
import LazyImage from '../components/LazyImage.vue'

const route = useRoute()
const category = ref(null)

const getPlantImage = (name) => {
    const encodedName = encodeURIComponent(`${name} potted plant`)
    return `https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=${encodedName}&image_size=landscape_16_9`
}

const addToArchive = () => {
    alert('功能开发中...')
}

onMounted(async () => {
    try {
        const res = await plantCategoryApi.getById(route.params.id)
        category.value = res.data
    } catch (e) {
        console.error('加载失败', e)
        category.value = {
            id: route.params.id,
            name: '绿萝',
            category: '观叶植物',
            scientificName: 'Epipremnum aureum',
            description: '绿萝是非常常见的室内观叶植物，具有很强的空气净化能力，能有效吸收甲醛。',
            lightRequirement: '散射光，耐阴',
            waterRequirement: '保持土壤微湿，避免积水',
            temperatureRange: '15-30℃',
            humidity: '40-70%',
            fertilization: '生长季每月施一次稀薄液肥',
            pruning: '定期修剪过长藤蔓',
            commonDiseases: '叶斑病、根腐病'
        }
    }
})
</script>

<style scoped>
.back-btn {
    margin-bottom: 20px;
}

.detail-header {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 30px;
    padding: 0;
    overflow: hidden;
    margin-bottom: 24px;
}

.detail-image {
    height: 300px;
    overflow: hidden;
}

.detail-info {
    padding: 30px;
}

.detail-info h1 {
    font-size: 32px;
    color: #1b5e20;
    margin-bottom: 12px;
}

.scientific-name {
    color: #888;
    font-style: italic;
    margin: 12px 0;
}

.description {
    color: #555;
    line-height: 1.8;
    margin: 20px 0;
}

.care-guide {
    margin-bottom: 24px;
}

.section-title {
    font-size: 20px;
    color: #1b5e20;
    margin-bottom: 20px;
}

.guide-item {
    display: flex;
    gap: 16px;
    padding: 16px;
    background: #f8faf5;
    border-radius: 8px;
}

.guide-icon {
    font-size: 32px;
}

.guide-item h3 {
    font-size: 14px;
    color: #33691e;
    margin-bottom: 4px;
}

.guide-item p {
    color: #666;
    font-size: 14px;
}

.diseases {
    background: #fff8e1;
}

@media (max-width: 768px) {
    .detail-header {
        grid-template-columns: 1fr;
    }
}
</style>

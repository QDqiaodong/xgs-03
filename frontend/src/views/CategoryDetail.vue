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

        <div class="care-tabs card">
            <h2 class="section-title">🌿 养护知识</h2>
            <div class="tabs-nav">
                <button
                    v-for="tab in tabs"
                    :key="tab.key"
                    class="tab-btn"
                    :class="{ active: activeTab === tab.key }"
                    :style="{ '--tab-gradient': tab.gradient }"
                    @click="activeTab = tab.key"
                >
                    <span class="tab-icon">{{ tab.icon }}</span>
                    <span class="tab-label">{{ tab.label }}</span>
                </button>
            </div>

            <div class="tabs-content">
                <div v-show="activeTab === 'basic'" class="tab-panel">
                    <div class="gradient-header basic-gradient">
                        <span class="gradient-icon">📖</span>
                        <span class="gradient-title">基础信息</span>
                    </div>
                    <div class="info-list">
                        <div class="info-row">
                            <span class="info-label">植物学名</span>
                            <span class="info-value">{{ category.scientificName || '暂无数据' }}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">植物分类</span>
                            <span class="info-value">{{ category.category || '暂无数据' }}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">品种简介</span>
                            <span class="info-value desc">{{ category.description || '暂无数据' }}</span>
                        </div>
                    </div>
                </div>

                <div v-show="activeTab === 'lightWater'" class="tab-panel">
                    <div class="gradient-header light-gradient">
                        <span class="gradient-icon">☀️💧</span>
                        <span class="gradient-title">光照浇水</span>
                    </div>
                    <div class="info-list">
                        <div class="info-card">
                            <div class="card-icon-wrapper light-icon">
                                <span class="card-icon">☀️</span>
                            </div>
                            <div class="card-content">
                                <h4>光照需求</h4>
                                <p>{{ category.lightRequirement || '暂无数据' }}</p>
                            </div>
                        </div>
                        <div class="info-card">
                            <div class="card-icon-wrapper water-icon">
                                <span class="card-icon">💧</span>
                            </div>
                            <div class="card-content">
                                <h4>浇水需求</h4>
                                <p>{{ category.waterRequirement || '暂无数据' }}</p>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-show="activeTab === 'tempHumidity'" class="tab-panel">
                    <div class="gradient-header temp-gradient">
                        <span class="gradient-icon">🌡️💨</span>
                        <span class="gradient-title">温度湿度</span>
                    </div>
                    <div class="info-list">
                        <div class="info-card">
                            <div class="card-icon-wrapper temp-icon">
                                <span class="card-icon">🌡️</span>
                            </div>
                            <div class="card-content">
                                <h4>适宜温度</h4>
                                <p>{{ category.temperatureRange || '暂无数据' }}</p>
                            </div>
                        </div>
                        <div class="info-card">
                            <div class="card-icon-wrapper humidity-icon">
                                <span class="card-icon">💨</span>
                            </div>
                            <div class="card-content">
                                <h4>湿度要求</h4>
                                <p>{{ category.humidity || '暂无数据' }}</p>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-show="activeTab === 'fertilize'" class="tab-panel">
                    <div class="gradient-header fertilize-gradient">
                        <span class="gradient-icon">🧪✂️</span>
                        <span class="gradient-title">施肥修剪</span>
                    </div>
                    <div class="info-list">
                        <div class="info-card">
                            <div class="card-icon-wrapper fertilize-icon">
                                <span class="card-icon">🧪</span>
                            </div>
                            <div class="card-content">
                                <h4>施肥建议</h4>
                                <p>{{ category.fertilization || '暂无数据' }}</p>
                            </div>
                        </div>
                        <div class="info-card">
                            <div class="card-icon-wrapper prune-icon">
                                <span class="card-icon">✂️</span>
                            </div>
                            <div class="card-content">
                                <h4>修剪建议</h4>
                                <p>{{ category.pruning || '暂无数据' }}</p>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-show="activeTab === 'disease'" class="tab-panel">
                    <div class="gradient-header disease-gradient">
                        <span class="gradient-icon">⚠️</span>
                        <span class="gradient-title">病害防治</span>
                    </div>
                    <div class="info-list">
                        <div class="disease-card">
                            <div class="disease-header">
                                <span class="disease-icon">🩺</span>
                                <span class="disease-title">常见病害</span>
                            </div>
                            <p class="disease-content">{{ category.commonDiseases || '暂无数据' }}</p>
                        </div>
                    </div>
                </div>
            </div>
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
const activeTab = ref('basic')

const tabs = [
    { key: 'basic', label: '基础信息', icon: '📖', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
    { key: 'lightWater', label: '光照浇水', icon: '☀️💧', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
    { key: 'tempHumidity', label: '温度湿度', icon: '🌡️💨', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
    { key: 'fertilize', label: '施肥修剪', icon: '🧪✂️', gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' },
    { key: 'disease', label: '病害防治', icon: '⚠️', gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' }
]

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

.care-tabs {
    margin-bottom: 24px;
}

.section-title {
    font-size: 20px;
    color: #1b5e20;
    margin-bottom: 20px;
}

.tabs-nav {
    display: flex;
    gap: 8px;
    margin-bottom: 24px;
    flex-wrap: wrap;
    padding-bottom: 4px;
    border-bottom: 1px solid #e8f5e9;
}

.tab-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 18px;
    background: #f5f5f5;
    border: none;
    border-radius: 10px 10px 0 0;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    color: #666;
    transition: all 0.3s ease;
    position: relative;
}

.tab-btn:hover {
    background: #f0f0f0;
    color: #333;
    transform: translateY(-1px);
}

.tab-btn.active {
    background: var(--tab-gradient);
    color: white;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
}

.tab-icon {
    font-size: 16px;
}

.tab-label {
    white-space: nowrap;
}

.tabs-content {
    min-height: 200px;
}

.tab-panel {
    animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(8px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.gradient-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    border-radius: 12px;
    margin-bottom: 20px;
    color: white;
}

.gradient-header .gradient-icon {
    font-size: 24px;
}

.gradient-header .gradient-title {
    font-size: 18px;
    font-weight: 600;
}

.basic-gradient {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.light-gradient {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.temp-gradient {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.fertilize-gradient {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.disease-gradient {
    background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.info-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.info-row {
    display: grid;
    grid-template-columns: 120px 1fr;
    gap: 16px;
    padding: 16px;
    background: #fafafa;
    border-radius: 10px;
    border-left: 4px solid #a5d6a7;
}

.info-label {
    font-weight: 600;
    color: #33691e;
    font-size: 14px;
}

.info-value {
    color: #555;
    font-size: 14px;
    line-height: 1.6;
}

.info-value.desc {
    color: #444;
    line-height: 1.8;
}

.info-card {
    display: flex;
    gap: 16px;
    padding: 18px;
    background: #fafafa;
    border-radius: 12px;
    align-items: flex-start;
    transition: all 0.3s ease;
}

.info-card:hover {
    transform: translateX(4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.card-icon-wrapper {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.light-icon {
    background: linear-gradient(135deg, #fff3cd, #ffe066);
}

.water-icon {
    background: linear-gradient(135deg, #d1ecf1, #81d4fa);
}

.temp-icon {
    background: linear-gradient(135deg, #f8d7da, #ff8a80);
}

.humidity-icon {
    background: linear-gradient(135deg, #e1f5fe, #4fc3f7);
}

.fertilize-icon {
    background: linear-gradient(135deg, #e8f5e9, #81c784);
}

.prune-icon {
    background: linear-gradient(135deg, #fff8e1, #ffd54f);
}

.card-icon {
    font-size: 24px;
}

.card-content {
    flex: 1;
}

.card-content h4 {
    font-size: 15px;
    color: #33691e;
    margin: 0 0 8px 0;
    font-weight: 600;
}

.card-content p {
    color: #555;
    font-size: 14px;
    line-height: 1.7;
    margin: 0;
}

.disease-card {
    padding: 20px;
    background: linear-gradient(135deg, #fff8e1 0%, #fff3cd 100%);
    border-radius: 12px;
    border: 1px solid #ffe082;
}

.disease-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
}

.disease-icon {
    font-size: 22px;
}

.disease-title {
    font-size: 16px;
    font-weight: 600;
    color: #e65100;
}

.disease-content {
    color: #5d4037;
    font-size: 14px;
    line-height: 1.8;
    margin: 0;
}

@media (max-width: 768px) {
    .detail-header {
        grid-template-columns: 1fr;
    }

    .tab-btn {
        padding: 8px 14px;
        font-size: 13px;
    }

    .tab-label {
        display: none;
    }

    .tab-btn.active .tab-label {
        display: inline;
    }

    .info-row {
        grid-template-columns: 1fr;
        gap: 8px;
    }

    .info-card {
        flex-direction: column;
    }
}
</style>

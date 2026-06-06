<template>
    <div class="lazy-image-container" :style="containerStyle">
        <img
            ref="imgRef"
            :src="loaded ? src : thumbnail"
            :class="['lazy-image', loaded && 'loaded']"
            :alt="alt"
            :style="imageStyle"
            @load="onImageLoad"
        />
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'

const props = defineProps({
    src: {
        type: String,
        required: true
    },
    thumbnail: {
        type: String,
        default: ''
    },
    alt: {
        type: String,
        default: ''
    },
    width: {
        type: [String, Number],
        default: '100%'
    },
    height: {
        type: [String, Number],
        default: 'auto'
    },
    mode: {
        type: String,
        default: 'list',
        validator: (val) => ['list', 'detail'].includes(val)
    }
})

const imgRef = ref(null)
const loaded = ref(false)
const observer = ref(null)

const containerStyle = computed(() => ({
    width: typeof props.width === 'number' ? `${props.width}px` : props.width,
    height: typeof props.height === 'number' ? `${props.height}px` : props.height
}))

const imageStyle = computed(() => ({
    width: '100%',
    height: '100%',
    objectFit: props.mode === 'detail' ? 'contain' : 'cover'
}))

const onImageLoad = () => {
    if (props.mode === 'list') {
        setTimeout(() => {
            loaded.value = true
        }, 100)
    } else {
        loaded.value = true
    }
}

const loadOriginalImage = () => {
    if (imgRef.value && props.thumbnail) {
        const tempImg = new Image()
        tempImg.onload = () => {
            imgRef.value.src = props.src
        }
        tempImg.src = props.src
    }
}

onMounted(() => {
    if (props.mode === 'list' && 'IntersectionObserver' in window) {
        observer.value = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    loadOriginalImage()
                    observer.value.unobserve(entry.target)
                }
            })
        }, {
            rootMargin: '100px',
            threshold: 0.1
        })

        if (imgRef.value) {
            observer.value.observe(imgRef.value)
        }
    } else if (props.mode === 'detail') {
        loaded.value = true
    }
})

onUnmounted(() => {
    if (observer.value) {
        observer.value.disconnect()
    }
})
</script>

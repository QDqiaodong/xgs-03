<template>
    <Transition name="fade">
        <button
            v-show="visible"
            class="back-to-top"
            @click="scrollToTop"
            :aria-label="'回到顶部'"
        >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="18 15 12 9 6 15"></polyline>
            </svg>
        </button>
    </Transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
    threshold: {
        type: Number,
        default: 300
    },
    smooth: {
        type: Boolean,
        default: true
    }
})

const visible = ref(false)

const handleScroll = () => {
    visible.value = window.scrollY > props.threshold
}

const scrollToTop = () => {
    if (props.smooth) {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        })
    } else {
        window.scrollTo(0, 0)
    }
}

onMounted(() => {
    window.addEventListener('scroll', handleScroll, { passive: true })
    handleScroll()
})

onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.back-to-top {
    position: fixed;
    right: 24px;
    bottom: 32px;
    width: 48px;
    height: 48px;
    border-radius: 50%;
    border: none;
    background: linear-gradient(135deg, #4caf50 0%, #388e3c 100%);
    color: white;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 16px rgba(76, 175, 80, 0.4);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    z-index: 999;
}

.back-to-top:hover {
    transform: translateY(-4px);
    box-shadow: 0 6px 20px rgba(76, 175, 80, 0.5);
}

.back-to-top:active {
    transform: translateY(-1px);
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
    transform: translateY(20px);
}
</style>

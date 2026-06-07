import { ref, computed } from 'vue'

const MAX_COMPARE = 3

export function useCompare() {
  const compareList = ref([])
  const showCompareModal = ref(false)

  const isInCompare = (id) => {
    return compareList.value.some((item) => item.id === id)
  }

  const canAddToCompare = computed(() => {
    return compareList.value.length < MAX_COMPARE
  })

  const compareCount = computed(() => {
    return compareList.value.length
  })

  const addToCompare = (item) => {
    if (!isInCompare(item.id) && compareList.value.length < MAX_COMPARE) {
      compareList.value.push(item)
      return true
    }
    return false
  }

  const removeFromCompare = (id) => {
    const index = compareList.value.findIndex((item) => item.id === id)
    if (index > -1) {
      compareList.value.splice(index, 1)
      return true
    }
    return false
  }

  const toggleCompare = (item) => {
    if (isInCompare(item.id)) {
      removeFromCompare(item.id)
      return false
    } else {
      return addToCompare(item)
    }
  }

  const clearCompare = () => {
    compareList.value = []
  }

  const openCompareModal = () => {
    if (compareList.value.length >= 2) {
      showCompareModal.value = true
    }
  }

  const closeCompareModal = () => {
    showCompareModal.value = false
  }

  return {
    compareList,
    showCompareModal,
    isInCompare,
    canAddToCompare,
    compareCount,
    addToCompare,
    removeFromCompare,
    toggleCompare,
    clearCompare,
    openCompareModal,
    closeCompareModal,
    MAX_COMPARE
  }
}

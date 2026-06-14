import axios from 'axios'

const api = axios.create({
    baseURL: '/api',
    timeout: 10000
})

export const plantCategoryApi = {
    getAll: () => api.get('/plant-categories'),
    getById: (id) => api.get(`/plant-categories/${id}`),
    getByCategory: (category) => api.get(`/plant-categories/category/${category}`),
    search: (keyword) => api.get('/plant-categories/search', { params: { keyword } })
}

export const plantArchiveApi = {
    getByUser: (userId) => api.get(`/plant-archives/user/${userId}`),
    getById: (id) => api.get(`/plant-archives/${id}`),
    create: (data) => api.post('/plant-archives', data),
    update: (id, data) => api.put(`/plant-archives/${id}`, data),
    delete: (id) => api.delete(`/plant-archives/${id}`)
}

export const careLogApi = {
    getByPlant: (plantId) => api.get(`/care-logs/plant/${plantId}`),
    getByPlantPaged: (plantId, page = 0, size = 5) => api.get(`/care-logs/plant/${plantId}/paged`, { params: { page, size } }),
    getByPlantFiltered: (plantId, page = 0, size = 5, filters = {}) => api.get(`/care-logs/plant/${plantId}/filtered`, {
        params: {
            page,
            size,
            operationTypes: filters.operationTypes && filters.operationTypes.length > 0 ? filters.operationTypes : undefined,
            startDate: filters.startDate || undefined,
            endDate: filters.endDate || undefined,
            keyword: filters.keyword || undefined
        },
        paramsSerializer: {
            indexes: null
        }
    }),
    getByUser: (userId) => api.get(`/care-logs/user/${userId}`),
    getById: (id) => api.get(`/care-logs/${id}`),
    create: (data) => api.post('/care-logs', data),
    update: (id, data) => api.put(`/care-logs/${id}`, data),
    delete: (id) => api.delete(`/care-logs/${id}`)
}

export const postApi = {
    getList: (params) => api.get('/posts', { params }),
    getHotTopics: () => api.get('/posts/topics'),
    getByUser: (userId) => api.get(`/posts/user/${userId}`),
    getById: (id) => api.get(`/posts/${id}`),
    create: (data) => api.post('/posts', data),
    update: (id, data) => api.put(`/posts/${id}`, data),
    delete: (id) => api.delete(`/posts/${id}`),
    getHotList: (params) => api.get('/posts', { params: { ...params, sort: 'hotness' } })
}

export const commentApi = {
    getByPost: (postId, params = {}) => api.get(`/comments/post/${postId}`, { params }),
    getByUser: (userId) => api.get(`/comments/user/${userId}`),
    create: (data) => api.post('/comments', data),
    delete: (id) => api.delete(`/comments/${id}`),
    setBestAnswer: (id, userId) => api.put(`/comments/${id}/best-answer`, { userId }),
    cancelBestAnswer: (id, userId) => api.delete(`/comments/${id}/best-answer`, { params: { userId } }),
    like: (id, userId) => api.post(`/comments/${id}/like`, { userId }),
    unlike: (id, userId) => api.delete(`/comments/${id}/like`, { params: { userId } }),
    toggleLike: (id, userId) => api.post(`/comments/${id}/toggle-like`, { userId }),
    getLikeStatus: (id, userId) => api.get(`/comments/${id}/like-status`, { params: { userId } })
}

export const favoriteApi = {
    getByUser: (userId) => api.get(`/favorites/user/${userId}`),
    getByFolder: (userId, folderId) => api.get(`/favorites/folder/${folderId}`, { params: { userId } }),
    check: (params) => api.get('/favorites/check', { params }),
    checkFolder: (params) => api.get('/favorites/check-folder', { params }),
    add: (data) => api.post('/favorites', data),
    remove: (params) => api.delete('/favorites', { params }),
    removeFromFolder: (userId, folderId, params) => api.delete(`/favorites/folder/${folderId}`, { params: { userId, ...params } }),
    move: (userId, favoriteId, targetFolderId) => api.put(`/favorites/${favoriteId}/move`, { targetFolderId }, { params: { userId } }),
    moveBatch: (userId, sourceFolderId, targetFolderId) => api.put('/favorites/move-batch', null, { params: { userId, sourceFolderId, targetFolderId } })
}

export const favoriteFolderApi = {
    getByUser: (userId) => api.get(`/favorite-folders/user/${userId}`),
    getById: (userId, folderId) => api.get(`/favorite-folders/${folderId}`, { params: { userId } }),
    create: (data) => api.post('/favorite-folders', data),
    update: (userId, folderId, data) => api.put(`/favorite-folders/${folderId}`, data, { params: { userId } }),
    delete: (userId, folderId) => api.delete(`/favorite-folders/${folderId}`, { params: { userId } }),
    updateOrder: (userId, folderIds) => api.put('/favorite-folders/order', folderIds, { params: { userId } }),
    getFavorites: (userId, folderId) => api.get(`/favorite-folders/${folderId}/favorites`, { params: { userId } }),
    getCount: (userId, folderId) => api.get(`/favorite-folders/${folderId}/count`, { params: { userId } }),
    getOrCreateDefault: (userId) => api.post('/favorite-folders/default', { userId })
}

export const userApi = {
    getById: (id) => api.get(`/users/${id}`),
    getByUsername: (username) => api.get(`/users/username/${username}`),
    create: (data) => api.post('/users', data),
    update: (id, data) => api.put(`/users/${id}`, data)
}

export const recommendationApi = {
    getPlantRecommendations: (userId, type, limit = 10) => api.get(`/recommendations/plants/${userId}`, {
        params: { type, limit }
    }),
    getSimilarRecommendations: (userId, limit = 10) => api.get(`/recommendations/plants/${userId}/similar`, {
        params: { limit }
    }),
    getEasyCareRecommendations: (userId, limit = 10) => api.get(`/recommendations/plants/${userId}/easy`, {
        params: { limit }
    }),
    getPopularRecommendations: (userId, limit = 10) => api.get(`/recommendations/plants/${userId}/popular`, {
        params: { limit }
    }),
    getUserProfile: (userId) => api.get(`/recommendations/profile/${userId}`)
}

export const browseHistoryApi = {
    recordBrowse: (data) => api.post('/browse-history', data),
    getByUser: (userId) => api.get(`/browse-history/user/${userId}`),
    getByUserAndType: (userId, targetType) => api.get(`/browse-history/user/${userId}/type/${targetType}`),
    delete: (params) => api.delete('/browse-history', { params })
}

export const plantPhotoApi = {
    getByArchive: (plantArchiveId) => api.get(`/plant-photos/archive/${plantArchiveId}`),
    getById: (id) => api.get(`/plant-photos/${id}`),
    getCover: (plantArchiveId) => api.get(`/plant-photos/archive/${plantArchiveId}/cover`),
    upload: (formData, onProgress) => api.post('/plant-photos/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: onProgress
    }),
    update: (id, data) => api.put(`/plant-photos/${id}`, data),
    delete: (id) => api.delete(`/plant-photos/${id}`),
    setCover: (id) => api.put(`/plant-photos/${id}/set-cover`)
}

export const checkInApi = {
    checkIn: (data) => api.post('/checkin', data),
    getStatus: (userId) => api.get(`/checkin/status/${userId}`),
    getCalendar: (userId, year, month) => api.get(`/checkin/calendar/${userId}`, { params: { year, month } }),
    getHistory: (userId) => api.get(`/checkin/history/${userId}`),
    getAchievements: (userId) => api.get(`/checkin/achievements/${userId}`)
}

export const tagApi = {
    getByUser: (userId) => api.get(`/tags/user/${userId}`),
    getById: (id) => api.get(`/tags/${id}`),
    create: (data) => api.post('/tags', data),
    update: (id, data) => api.put(`/tags/${id}`, data),
    rename: (id, name) => api.put(`/tags/${id}/rename`, { name }),
    delete: (id) => api.delete(`/tags/${id}`),
    updateOrder: (userId, tagIds) => api.put('/tags/order', tagIds, { params: { userId } })
}

export const plantArchiveTagApi = {
    getByPlant: (plantArchiveId) => api.get(`/plant-archive-tags/plant/${plantArchiveId}`),
    getByPlants: (plantArchiveIds) => api.get('/plant-archive-tags/plants/batch', { params: { plantArchiveIds }, paramsSerializer: { indexes: null } }),
    addTag: (plantArchiveId, tagId) => api.post('/plant-archive-tags', { plantArchiveId, tagId }),
    setTags: (plantArchiveId, tagIds) => api.put(`/plant-archive-tags/plant/${plantArchiveId}`, tagIds),
    removeTag: (plantArchiveId, tagId) => api.delete('/plant-archive-tags', { params: { plantArchiveId, tagId } }),
    getByTag: (userId, tagId) => api.get(`/plant-archive-tags/user/${userId}/tag/${tagId}`),
    getByTags: (userId, tagIds) => api.get(`/plant-archive-tags/user/${userId}/tags`, {
        params: { tagIds },
        paramsSerializer: { indexes: null }
    }),
    getCountByTag: (tagId) => api.get(`/plant-archive-tags/tag/${tagId}/count`),
    getCountByTags: (tagIds) => api.get('/plant-archive-tags/tags/count', { params: { tagIds }, paramsSerializer: { indexes: null } })
}

export const careReminderApi = {
    getByUser: (userId) => api.get(`/care-reminders/user/${userId}`),
    getByUserPaged: (userId, page = 0, size = 10) => api.get(`/care-reminders/user/${userId}/paged`, { params: { page, size } }),
    getByUserAndStatus: (userId, status) => api.get(`/care-reminders/user/${userId}/status/${status}`),
    getPendingByUser: (userId) => api.get(`/care-reminders/user/${userId}/pending`),
    getByPlant: (plantArchiveId) => api.get(`/care-reminders/plant/${plantArchiveId}`),
    getById: (id) => api.get(`/care-reminders/${id}`),
    complete: (id, data) => api.post(`/care-reminders/${id}/complete`, data),
    defer: (id, data) => api.post(`/care-reminders/${id}/defer`, data),
    cancel: (id) => api.post(`/care-reminders/${id}/cancel`),
    triggerScan: () => api.post('/care-reminders/trigger-scan')
}

const EventBus = {
    events: {},
    on(event, callback) {
        if (!this.events[event]) {
            this.events[event] = []
        }
        this.events[event].push(callback)
    },
    off(event, callback) {
        if (!this.events[event]) return
        this.events[event] = this.events[event].filter(cb => cb !== callback)
    },
    emit(event, payload) {
        if (!this.events[event]) return
        this.events[event].forEach(callback => callback(payload))
    }
}

export { EventBus }

export default api

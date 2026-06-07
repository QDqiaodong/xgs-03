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
    getByUser: (userId) => api.get(`/posts/user/${userId}`),
    getById: (id) => api.get(`/posts/${id}`),
    create: (data) => api.post('/posts', data),
    update: (id, data) => api.put(`/posts/${id}`, data),
    delete: (id) => api.delete(`/posts/${id}`)
}

export const commentApi = {
    getByPost: (postId) => api.get(`/comments/post/${postId}`),
    getByUser: (userId) => api.get(`/comments/user/${userId}`),
    create: (data) => api.post('/comments', data),
    delete: (id) => api.delete(`/comments/${id}`),
    setBestAnswer: (id, isBest) => api.put(`/comments/${id}/best-answer`, null, { params: { isBest } })
}

export const favoriteApi = {
    getByUser: (userId) => api.get(`/favorites/user/${userId}`),
    check: (params) => api.get('/favorites/check', { params }),
    add: (data) => api.post('/favorites', data),
    remove: (params) => api.delete('/favorites', { params })
}

export const userApi = {
    getById: (id) => api.get(`/users/${id}`),
    getByUsername: (username) => api.get(`/users/username/${username}`),
    create: (data) => api.post('/users', data),
    update: (id, data) => api.put(`/users/${id}`, data)
}

export default api

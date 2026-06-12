import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/Home.vue')
    },
    {
        path: '/categories',
        name: 'Categories',
        component: () => import('../views/Categories.vue')
    },
    {
        path: '/categories/:id',
        name: 'CategoryDetail',
        component: () => import('../views/CategoryDetail.vue')
    },
    {
        path: '/archives',
        name: 'Archives',
        component: () => import('../views/Archives.vue')
    },
    {
        path: '/archives/:id',
        name: 'ArchiveDetail',
        component: () => import('../views/ArchiveDetail.vue')
    },
    {
        path: '/posts',
        name: 'Posts',
        component: () => import('../views/Posts.vue')
    },
    {
        path: '/posts/:id',
        name: 'PostDetail',
        component: () => import('../views/PostDetail.vue')
    },
    {
        path: '/favorites',
        name: 'Favorites',
        component: () => import('../views/Favorites.vue')
    },
    {
        path: '/checkin',
        name: 'CheckIn',
        component: () => import('../views/CheckIn.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router

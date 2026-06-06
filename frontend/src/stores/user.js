import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const currentUser = ref({
        id: 1,
        username: 'plant_lover',
        nickname: '绿植爱好者',
        avatar: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=cute%20gardener%20avatar&image_size=square'
    })

    const setUser = (user) => {
        currentUser.value = user
    }

    return {
        currentUser,
        setUser
    }
})

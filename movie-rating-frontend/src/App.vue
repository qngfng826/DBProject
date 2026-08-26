<template>
  <router-view />
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { onMounted } from 'vue'

const userStore = useUserStore()

// 页面加载时，尝试从本地存储恢复用户状态
onMounted(() => {
  userStore.loadUserInfo()
})
</script>
    
<style>
</style>

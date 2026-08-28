<template>
  <el-container style="height: 100vh; background: var(--bg-primary);">
    <!-- 侧边栏 -->
    <el-aside width="220px" style="background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);">
      <div style="padding: 20px 15px; color: #fff; font-size: 18px; font-weight: 700; border-bottom: 1px solid rgba(255,255,255,0.1);">
        <el-icon style="margin-right: 8px;"><Setting /></el-icon>
        管理后台
      </div>
      
      <!-- 导航菜单 -->
      <el-menu
        router
        default-active="/admin/movie"
        style="border-right: none; background: transparent;"
        text-color="#fff"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin/movie" style="margin-top: 10px;">
          <el-icon><VideoCamera /></el-icon>
          <span>电影管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/actor">
          <el-icon><User /></el-icon>
          <span>演员管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/director">
          <el-icon><Film /></el-icon>
          <span>导演管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/user">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        
        <el-divider style="border-color: rgba(255,255,255,0.2); margin: 15px 0;"></el-divider>
        
        <!-- 返回前台按钮 -->
        <el-menu-item index="/" style="color: #fff;">
          <el-icon><HomeFilled /></el-icon>
          <span>返回前台</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container>
      <el-header style="background: var(--bg-card); border-bottom: 1px solid var(--border-color); display: flex; align-items: center; justify-content: space-between;">
        <div style="color: var(--text-primary); font-size: 16px;">
          <span v-if="currentPath === '/admin/movie'">电影管理</span>
          <span v-else-if="currentPath === '/admin/actor'">演员管理</span>
          <span v-else-if="currentPath === '/admin/director'">导演管理</span>
          <span v-else-if="currentPath === '/admin/user'">用户管理</span>
          <span v-else>管理后台</span>
        </div>
        
        <div style="display: flex; align-items: center; gap: 15px;">
          <span style="color: var(--text-secondary);">管理员</span>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      
      <el-main style="padding: 20px; overflow-y: auto; background: var(--bg-primary);">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Setting, VideoCamera, User, UserFilled, Film, HomeFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentPath = computed(() => route.path)

function handleLogout() {
  ElMessageBox.confirm('确定退出管理后台吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.el-menu-item {
  height: 45px;
  line-height: 45px;
  margin: 0;
  border-radius: 0;
}

.el-menu-item:hover {
  background: rgba(255,255,255,0.1) !important;
}

.el-menu-item.is-active {
  background: rgba(64,158,255,0.3) !important;
  border-left: 4px solid #409EFF;
}
</style>

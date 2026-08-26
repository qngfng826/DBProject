<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="navbar-inner">
        <div class="logo" @click="$router.push('/')">
          <el-icon :size="28"><Film /></el-icon>
          <span>MovieRate</span>
        </div>

        <nav class="nav-links">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/movies" class="nav-link">电影</router-link>
          <router-link to="/actors" class="nav-link">演员</router-link>
          <router-link to="/directors" class="nav-link">导演</router-link>
          <router-link to="/report" class="nav-link">数据报表</router-link>
          <router-link v-if="userStore.isAdmin" to="/admin/movie" class="nav-link">管理后台</router-link>
        </nav>

        <div class="nav-actions">
          <template v-if="userStore.isLogin">
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" class="user-avatar">
                  {{ userStore.userInfo?.username?.charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="my-rating" :icon="Star">我的评分</el-dropdown-item>
                  <el-dropdown-item command="my-comment" :icon="ChatDotRound">我的评论</el-dropdown-item>
                  <el-dropdown-item command="logout" :icon="SwitchButton" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" round @click="$router.push('/login')">登录</el-button>
            <el-button round @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <p>MovieRate © 2026 · 电影评分系统 · 仿豆瓣电影</p>
    </footer>
  </div>
</template>

<script setup>
import { Film, ArrowDown, Star, ChatDotRound, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/')
    }).catch(() => {})
  } else if (command === 'admin') {
    router.push('/admin/movie')
  } else {
    router.push(`/${command}`)
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(13, 17, 23, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-color);
}

.navbar-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 800;
  color: var(--accent);
  cursor: pointer;
  user-select: none;
}

.nav-links {
  display: flex;
  gap: 4px;
}

.nav-link {
  padding: 8px 16px;
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 15px;
  font-weight: 500;
  transition: var(--transition);
}

.nav-link:hover {
  color: var(--accent);
  background: var(--bg-hover);
}

.nav-link.router-link-exact-active {
  color: var(--accent);
  background: var(--bg-hover);
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 8px;
  transition: var(--transition);
}

.user-info:hover {
  background: var(--bg-hover);
}

.user-avatar {
  background: var(--accent);
  color: #000;
  font-weight: 700;
}

.username {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

.main-content {
  flex: 1;
  min-height: calc(100vh - 64px - 60px);
}

.footer {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid var(--border-color);
  color: var(--text-muted);
  font-size: 14px;
}

@media (max-width: 768px) {
  .nav-links { display: none; }
  .username { display: none; }
}
</style>

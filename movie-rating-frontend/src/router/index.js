import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    component: () => import('@/views/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    children: [
      { path: '', component: () => import('@/views/Home.vue') },
      { path: 'movies', component: () => import('@/views/MovieList.vue') },
      { path: 'movie/:id', component: () => import('@/views/MovieDetail.vue') },
      { path: 'actors', component: () => import('@/views/ActorList.vue') },
      { path: 'actor/:id', component: () => import('@/views/ActorDetail.vue') },
      { path: 'directors', component: () => import('@/views/DirectorList.vue') },
      { path: 'director/:id', component: () => import('@/views/DirectorDetail.vue') },
      { path: 'my-rating', component: () => import('@/views/MyRating.vue') },
      { path: 'my-comment', component: () => import('@/views/MyComment.vue') },
      { path: 'report', component: () => import('@/views/Report.vue') }
    ],
    meta: { public: true }
  },
  {
    path: '/admin',
    component: () => import('@/views/AdminLayout.vue'),
    meta: { admin: true },
    children: [
      { path: 'movie', component: () => import('@/views/admin/MovieManage.vue') },
      { path: 'actor', component: () => import('@/views/admin/ActorManage.vue') },
      { path: 'director', component: () => import('@/views/admin/DirectorManage.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  // 优化守卫逻辑：先检查公共路由
  if (to.meta.public) return next()
  // 检查登录状态
  if (!userStore.token) return next('/login')
  // 检查管理员权限
  if (to.meta.admin && !userStore.isAdmin) {
    ElMessage.error('您没有权限访问该页面')
    return next('/')
  }
  next()
})

export default router

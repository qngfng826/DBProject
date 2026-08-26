import { defineStore } from 'pinia'
import request from '@/api/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    userInfo: {} // 初始化为空对象
  }),

  getters: {
    isLogin: (state) => !!state.token,
    isAdmin: (state) => state.userInfo?.username === 'admin' || state.userInfo?.role === 'admin'
    // 检查用户名或角色是否为 admin
  },

  actions: {
    async login(credentials) {
      try {
        const res = await request.post('/login', credentials) // 调用后端登录接口
        this.token = res.data.token // 存储 token
        this.userInfo = res.data.user || {} // 存储用户信息
        if (!this.userInfo.userId && this.userInfo.UserID) {
          this.userInfo.userId = this.userInfo.UserID
        }
        localStorage.setItem('token', this.token) // 持久化 token
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo)) // 持久化用户信息
        return res // 返回响应数据
      } catch (error) {
        throw error // 抛出错误，让组件捕获处理
      }
    },
    async register(Data) {
      try {
        const res = await request.post('/register', Data) // 调用后端注册接口
        return res // 返回响应数据
      }
      catch (error) {
        throw error // 抛出错误，让组件捕获处理
      }
    },
    logout() {
      this.clearToken()
    },
    clearToken() {
      this.token = null
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
    // 页面加载时读取并解析localStorage
    loadUserInfo() {
      const stored = localStorage.getItem('userInfo')
      if (stored) {
        try {
          const parsed = JSON.parse(stored)
          this.userInfo = parsed
          if (!parsed.userId && parsed.UserID) {
            this.userInfo.userId = parsed.UserID
          }
        } catch (e) {
          console.error('解析userInfo失败:', e)
          this.userInfo = {}
        }
      }
    }
  }
})
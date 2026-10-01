import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 简单的内存缓存
const cache = new Map()
const CACHE_DURATION = 5 * 60 * 1000 // 5分钟

// 供登出/切换账号时清空缓存，避免下一个用户读到上一个用户的数据
export function clearCache() {
  cache.clear()
}

// 401 正在跳转登录页的标记（防并发重复跳转）
let redirectingToLogin = false

const request = axios.create({
  // 生产环境通过 VITE_API_BASE_URL 指定后端地址，本地开发走 /api 由 vite 代理
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 8000
})

// 请求拦截器：添加缓存支持
request.interceptors.request.use(
  (config) => {
    // 缓存 GET 请求
    if (config.method === 'get') {
      const cacheKey = `${config.url}?${JSON.stringify(config.params)}`
      const cached = cache.get(cacheKey)
      if (cached && Date.now() - cached.timestamp < CACHE_DURATION) {
        // 打上来自缓存的标记，避免响应拦截器刷新时间戳造成热点 key 永不过期
        config._fromCache = true
        config.adapter = () => Promise.resolve({
          data: cached.data,
          status: 200,
          statusText: 'OK',
          headers: {},
          config,
          request: {}
        })
      }
    }

    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 缓存成功的 GET 请求响应（来自缓存的伪响应不重写，避免滑动过期）
    if (response.config.method === 'get' && res.code === 200 && !response.config._fromCache) {
      const cacheKey = `${response.config.url}?${JSON.stringify(response.config.params)}`
      cache.set(cacheKey, {
        data: res,
        timestamp: Date.now()
      })
    }

    // 写操作成功后清空 GET 缓存，避免发评/删评/评分后的重新拉取拿到旧数据
    if (response.config.method !== 'get' && res.code === 200) {
      cache.clear()
    }

    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')

      if (res.code === 401 && !redirectingToLogin) {
        redirectingToLogin = true
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
        setTimeout(() => { redirectingToLogin = false }, 2000)
      }

      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },

  (error) => {
    if (error.response) {
      if (error.response.status === 401) {
        // 并发多个请求同时 401 时只处理一次，避免重复弹窗和重复跳转
        if (!redirectingToLogin) {
          redirectingToLogin = true
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          ElMessage.error('登录已过期，请重新登录')
          setTimeout(() => { redirectingToLogin = false }, 2000)
        }
      } else {
        ElMessage.error(error.response.data?.msg || '网络错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default request

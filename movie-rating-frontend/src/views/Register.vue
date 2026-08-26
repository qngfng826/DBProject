<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <el-icon :size="40" color="var(--accent)"><Film /></el-icon>
        <h1>创建账号</h1>
        <p>加入 MovieRate，开始你的电影之旅</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（至少3位）" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（至少6位）" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱（选填）" :prefix-icon="Message" />
        </el-form-item>
        <el-button type="primary" class="auth-btn" :loading="loading" @click="handleRegister">注 册</el-button>
      </el-form>

      <div class="auth-footer">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Film, User, Lock, Message } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '', confirmPassword: '', email: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次输入密码不一致'))
  else callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少3位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    const { confirmPassword, ...registerData } = form
    await userStore.register(registerData)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '注册失败')
    console.error('注册失败:', e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0d1117 0%, #161b22 50%, #1c2128 100%);
  padding: 20px;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: var(--shadow);
}

.auth-header { text-align: center; margin-bottom: 32px; }
.auth-header h1 { font-size: 28px; font-weight: 800; color: var(--text-primary); margin: 16px 0 8px; }
.auth-header p { color: var(--text-secondary); font-size: 15px; }
.auth-btn { width: 100%; height: 44px; font-size: 16px; font-weight: 600; margin-top: 8px; }
.auth-footer { text-align: center; margin-top: 24px; color: var(--text-secondary); font-size: 14px; }
</style>

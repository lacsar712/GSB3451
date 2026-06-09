<template>
  <div class="min-h-[80vh] flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <el-card class="max-w-md w-full p-8">
      <div class="text-center mb-10">
        <h2 class="text-3xl font-extrabold text-gray-900 mb-2">欢迎回来</h2>
        <p class="text-gray-500">请登录您的账号以继续交易</p>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password @keyup.enter="handleLogin">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <div class="flex items-center justify-between mb-6">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <el-button link type="primary" @click="$router.push('/forgot-password')">忘记密码？</el-button>
        </div>
        <el-button type="primary" class="w-full h-12 text-lg" :loading="loading" @click="handleLogin">登 录</el-button>
        <div class="mt-6 text-center text-sm text-gray-500">
          还没有账号？
          <el-button link type="primary" @click="$router.push('/register')">立即注册</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const rememberMe = ref(false)
const loginRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginRef.value) return
  await loginRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await auth.login(loginForm.username, loginForm.password)
        ElMessage.success('登录成功')
        if (auth.isAdmin) router.push('/dashboard/admin')
        else if (auth.isLandlord) router.push('/dashboard/landlord')
        else router.push('/')
      } catch (err) {
        ElMessage.error(err.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

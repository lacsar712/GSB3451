<template>
  <div class="min-h-[80vh] flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <el-card class="max-w-md w-full p-8">
      <div class="text-center mb-10">
        <h2 class="text-3xl font-extrabold text-gray-900 mb-2">创建新账号</h2>
        <p class="text-gray-500">加入我们的平台，开启您的房产之旅</p>
      </div>
      <el-form :model="registerForm" :rules="rules" ref="registerRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请设置用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请设置密码" size="large" show-password />
        </el-form-item>
        <el-form-item label="注册身份" prop="role">
          <el-radio-group v-model="registerForm.role" class="w-full flex justify-around">
            <el-radio :label="'USER'">普通用户</el-radio>
            <el-radio :label="'LANDLORD'">房东</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" size="large" />
        </el-form-item>
        <el-button type="primary" class="w-full h-12 text-lg mt-4" :loading="loading" @click="handleRegister">注 册</el-button>
        <div class="mt-6 text-center text-sm text-gray-500">
          已有账号？
          <el-button link type="primary" @click="$router.push('/login')">立即登录</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const registerRef = ref(null)

const registerForm = reactive({
  username: '',
  password: '',
  role: 'USER',
  phone: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择注册身份', trigger: 'change' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const handleRegister = async () => {
  if (!registerRef.value) return
  await registerRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await axios.post('/api/auth/register', registerForm)
        if (res.data.code === 200) {
          ElMessage.success('注册成功，请登录')
          router.push('/login')
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (err) {
        ElMessage.error('注册失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<template>
  <div class="min-h-[80vh] flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <el-card class="max-w-md w-full p-8">
      <div class="text-center mb-10">
        <h2 class="text-3xl font-extrabold text-gray-900 mb-2">重置密码</h2>
        <p class="text-gray-500">请输入您的新密码</p>
      </div>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入新密码" size="large" show-password>
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" size="large" show-password>
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        
        <el-button type="primary" class="w-full h-12 text-lg" :loading="loading" @click="handleSubmit">重置密码</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  password: '',
  confirmPassword: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
}

const token = ref('')

onMounted(() => {
  token.value = route.query.token
  if (!token.value) {
    ElMessage.error('无效的重置链接')
    router.push('/login')
  }
})

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await axios.post('/api/auth/reset-password', { 
          token: token.value, 
          password: form.password 
        })
        ElMessage.success('密码重置成功，请重新登录')
        router.push('/login')
      } catch (err) {
        ElMessage.error(err.response?.data?.message || '重置失败，链接可能已过期')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

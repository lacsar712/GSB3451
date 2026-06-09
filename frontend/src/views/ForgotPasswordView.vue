<template>
  <div class="min-h-[80vh] flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <el-card class="max-w-md w-full p-8">
      <div class="text-center mb-10">
        <h2 class="text-3xl font-extrabold text-gray-900 mb-2">找回密码</h2>
        <p class="text-gray-500">请输入您的注册邮箱，我们将发送重置链接</p>
      </div>
      
      <el-form v-if="!submitted" :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入电子邮箱" size="large">
            <template #prefix><el-icon><Message /></el-icon></template>
          </el-input>
        </el-form-item>
        
        <el-button type="primary" class="w-full h-12 text-lg" :loading="loading" @click="handleSubmit">发送重置邮件</el-button>
        
        <div class="mt-6 text-center text-sm">
          <el-button link type="primary" @click="$router.push('/login')">返回登录</el-button>
        </div>
      </el-form>

      <div v-else class="text-center">
        <el-result
          icon="success"
          title="邮件已发送"
          sub-title="请查看您的邮箱并点击链接重置密码（如果没有收到，请检查垃圾箱）"
        >
          <template #extra>
            <el-button type="primary" @click="$router.push('/login')">返回登录</el-button>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const formRef = ref(null)
const loading = ref(false)
const submitted = ref(false)

const form = reactive({
  email: ''
})

const rules = {
  email: [
    { required: true, message: '请输入电子邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await axios.post('/api/auth/forgot-password', { email: form.email })
        submitted.value = true
      } catch (err) {
        ElMessage.error(err.response?.data?.message || '发送失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

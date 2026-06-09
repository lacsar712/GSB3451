<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center bg-white p-6 rounded-2xl shadow-sm">
      <h1 class="text-2xl font-bold text-gray-800">用户管理</h1>
    </div>

    <el-card>
      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="role" label="角色">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : (scope.row.role === 'LANDLORD' ? 'warning' : 'info')">
              {{ scope.row.role === 'ADMIN' ? '管理员' : (scope.row.role === 'LANDLORD' ? '房东' : '普通用户') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间">
          <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 1" 
              link type="warning" 
              @click="handleStatus(scope.row, 0)"
            >禁用</el-button>
            <el-button 
              v-else 
              link type="success" 
              @click="handleStatus(scope.row, 1)"
            >启用</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const users = ref([])
const loading = ref(false)
const token = localStorage.getItem('token')

const fetchAll = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/users/admin/all', {
      headers: { Authorization: `Bearer ${token}` }
    })
    users.value = res.data.data
  } catch (err) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleStatus = async (user, status) => {
  try {
    await axios.put(`/api/users/admin/status/${user.id}/${status}`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    })
    ElMessage.success('操作成功')
    fetchAll()
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (user) => {
  ElMessageBox.confirm(`确定要删除用户 ${user.username} 吗？`, '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await axios.delete(`/api/users/admin/delete/${user.id}`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      ElMessage.success('用户已删除')
      fetchAll()
    } catch (err) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

onMounted(fetchAll)
</script>

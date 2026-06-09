<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-2xl shadow-sm">
      <h1 class="text-2xl font-bold text-gray-800">我的预约</h1>
      <p class="text-gray-500 mt-1">追踪您的看房记录与状态</p>
    </div>

    <div v-loading="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <el-card v-for="item in appointments" :key="item.id" class="relative hover:shadow-lg transition-shadow duration-300">
        <template #header>
          <div class="flex justify-between items-center">
            <span class="font-bold text-lg truncate flex-1 mr-2">{{ item.houseTitle }}</span>
            <el-tag :type="getStatusType(item.status)">{{ getStatusLabel(item.status) }}</el-tag>
          </div>
        </template>
        <div class="flex justify-between items-center mb-4">
          <span class="text-xs text-gray-400 font-mono">创建于: {{ formatDate(item.createTime) }}</span>
        </div>
        <div class="flex items-center mb-4">
          <el-icon class="text-primary text-xl mr-2"><Calendar /></el-icon>
          <span class="font-bold text-gray-700">{{ formatDateTime(item.appointmentTime) }}</span>
        </div>
        <p class="text-sm text-gray-600 bg-gray-50 p-3 rounded-lg border border-gray-100 italic">
          "{{ item.remark || '暂无备注' }}"
        </p>
        <div class="mt-6 flex space-x-2">
          <el-button size="small" type="primary" plain class="flex-1" @click="$router.push(`/house/${item.houseId}`)">查看房源</el-button>
          <el-button size="small" type="danger" plain class="flex-1">取消预约</el-button>
        </div>
      </el-card>

      <el-empty v-if="!loading && appointments.length === 0" description="暂无预约记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { Calendar } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const appointments = ref([])
const loading = ref(false)
const token = localStorage.getItem('token')

const fetchAppointments = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/appointments/user/my', {
      headers: { Authorization: `Bearer ${token}` }
    })
    appointments.value = res.data.data
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'info' }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = { PENDING: '待确认', APPROVED: '已通过', REJECTED: '已拒绝', CANCELLED: '已取消' }
  return map[status] || status
}

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD')
const formatDateTime = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

onMounted(fetchAppointments)
</script>

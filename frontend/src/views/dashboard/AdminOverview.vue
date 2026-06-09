<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-2xl shadow-sm">
      <h1 class="text-2xl font-bold text-gray-800">管理概览</h1>
      <p class="text-gray-500 mt-1">系统数据实时监控与管理</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <el-card v-for="stat in stats" :key="stat.title" shadow="hover">
        <div class="flex items-center space-x-4">
          <div :class="`p-4 rounded-xl ${stat.bgColor}`">
            <el-icon :size="24" :class="stat.iconColor"><component :is="stat.icon" /></el-icon>
          </div>
          <div>
            <p class="text-sm text-gray-500">{{ stat.title }}</p>
            <p class="text-2xl font-bold">{{ stat.value }}</p>
          </div>
        </div>
      </el-card>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <el-card header="最近房源" shadow="never">
        <el-table :data="recentHouses" style="width: 100%" size="small">
          <el-table-column prop="title" label="标题" show-overflow-tooltip />
          <el-table-column prop="price" label="价格">
            <template #default="scope">{{ (scope.row.price / 10000).toFixed(1) }}万</template>
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag size="small" :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card header="系统公告" shadow="never">
        <el-table :data="recentAnnouncements" style="width: 100%" size="small">
          <el-table-column prop="title" label="标题" show-overflow-tooltip />
          <el-table-column prop="createTime" label="时间">
            <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { User, House, Message, Bell } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const users = ref([])
const houses = ref([])
const announcements = ref([])
const token = localStorage.getItem('token')

const stats = computed(() => [
  { title: '总用户数', value: users.value.length, icon: User, bgColor: 'bg-blue-50', iconColor: 'text-blue-500' },
  { title: '总房源数', value: houses.value.length, icon: House, bgColor: 'bg-green-50', iconColor: 'text-green-500' },
  { title: '待审核', value: houses.value.filter(h => h.status === 'PENDING').length, icon: Bell, bgColor: 'bg-orange-50', iconColor: 'text-orange-500' },
  { title: '公告数', value: announcements.value.length, icon: Message, bgColor: 'bg-purple-50', iconColor: 'text-purple-500' },
])

const recentHouses = computed(() => houses.value.slice(0, 5))
const recentAnnouncements = computed(() => announcements.value.slice(0, 5))

const fetchData = async () => {
  try {
    const config = { headers: { Authorization: `Bearer ${token}` } }
    const [userRes, houseRes, announceRes] = await Promise.all([
      axios.get('/api/users/admin/all', config),
      axios.get('/api/houses/admin/all', config),
      axios.get('/api/announcements/public/list')
    ])
    users.value = userRes.data.data
    houses.value = houseRes.data.data
    announcements.value = announceRes.data.data
  } catch (err) {
    console.error('Failed to fetch stats', err)
  }
}

const getStatusType = (status) => {
  const map = { PENDING: 'warning', ACTIVE: 'success', SOLD: 'info', ARCHIVED: 'danger' }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = { PENDING: '待审核', ACTIVE: '展示中', REJECTED: '已驳回', SOLD: '已成交', ARCHIVED: '已下架' }
  return map[status] || status
}

const formatDate = (date) => dayjs(date).format('MM-DD HH:mm')

onMounted(fetchData)
</script>

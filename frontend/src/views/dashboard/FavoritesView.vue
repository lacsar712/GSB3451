<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-2xl shadow-sm">
      <h1 class="text-2xl font-bold text-gray-800">我的收藏</h1>
      <p class="text-gray-500 mt-1">您收藏的所有心仪房源</p>
    </div>

    <div v-loading="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <el-card v-for="item in collections" :key="item.id" :body-style="{ padding: '0px' }" class="overflow-hidden group">
        <div class="relative h-48 overflow-hidden">
          <el-image 
            :src="item.house?.imageUrl" 
            class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
          />
          <div class="absolute top-4 right-4">
            <el-button type="danger" circle @click="handleRemove(item.houseId)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
        <div class="p-4">
          <h3 class="text-lg font-bold text-gray-800 mb-2 truncate">{{ item.house?.title }}</h3>
          <div class="flex items-center text-gray-500 text-sm mb-4">
            <el-icon class="mr-1"><Location /></el-icon> {{ item.house?.address }}
          </div>
          <div class="flex justify-between items-center">
            <span class="text-xl font-bold text-red-500">{{ (item.house?.price / 10000).toFixed(1) }}万</span>
            <el-button type="primary" link @click="$router.push(`/house/${item.houseId}`)">查看详情</el-button>
          </div>
        </div>
      </el-card>

      <el-empty v-if="!loading && collections.length === 0" description="暂无收藏房源" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { Location, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const collections = ref([])
const loading = ref(false)
const token = localStorage.getItem('token')

const fetchCollections = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/collections/user/my', {
      headers: { Authorization: `Bearer ${token}` }
    })
    collections.value = res.data.data
  } catch (err) {
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

const handleRemove = async (houseId) => {
  try {
    await axios.post(`/api/collections/user/toggle/${houseId}`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    })
    ElMessage.success('已取消收藏')
    fetchCollections()
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

onMounted(fetchCollections)
</script>

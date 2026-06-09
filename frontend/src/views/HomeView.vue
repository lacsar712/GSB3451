<template>
  <div class="space-y-8">
    <!-- Hero Section -->
    <div class="relative rounded-3xl overflow-hidden bg-primary p-12 text-white">
      <div class="relative z-10 max-w-2xl">
        <h1 class="text-4xl font-extrabold mb-4">寻找您的理想家园</h1>
        <p class="text-lg opacity-90 mb-8">海量房源，诚信交易，为您提供最专业的二手房交易服务。</p>
        <div class="flex space-x-2 bg-white p-2 rounded-xl shadow-lg">
          <el-input v-model="searchQuery" placeholder="搜索区域、小区或房源标题..." class="search-input" />
          <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
        </div>
      </div>
      <div class="absolute right-0 bottom-0 opacity-20 transform translate-x-1/4 translate-y-1/4">
        <el-icon :size="400"><HomeFilled /></el-icon>
      </div>
    </div>

    <!-- House List -->
    <div>
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold text-gray-800">精选房源</h2>
        <el-radio-group v-model="filterType" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="recent">最新</el-radio-button>
          <el-radio-button label="hot">热门</el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <el-skeleton v-for="i in 4" :key="i" animated>
          <template #template>
            <el-skeleton-item variant="image" style="height: 200px; border-radius: 12px" />
            <div class="p-4">
              <el-skeleton-item variant="p" style="width: 50%" />
              <el-skeleton-item variant="h3" style="width: 80%" />
            </div>
          </template>
        </el-skeleton>
      </div>

      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <el-card v-for="house in houses" :key="house.id" class="house-card" :body-style="{ padding: '0px' }" @click="$router.push(`/house/${house.id}`)">
          <img :src="house.imageUrl" class="w-full h-48 object-cover" />
          <div class="p-4">
            <div class="flex justify-between items-start mb-2">
              <span class="text-2xl font-bold text-red-500">{{ (house.price / 10000).toFixed(1) }}<span class="text-sm">万</span></span>
              <el-tag size="small" type="info">{{ house.houseType }}</el-tag>
            </div>
            <h3 class="text-lg font-bold text-gray-800 mb-1 truncate">{{ house.title }}</h3>
            <p class="text-sm text-gray-500 flex items-center mb-3">
              <el-icon class="mr-1"><Location /></el-icon> {{ house.address }}
            </p>
            <div class="flex justify-between text-xs text-gray-400">
              <span>{{ house.area }}㎡</span>
              <span>{{ house.orientation }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'
import { HomeFilled, Location } from '@element-plus/icons-vue'

const houses = ref([])
const loading = ref(true)
const searchQuery = ref('')
const filterType = ref('all')

const fetchHouses = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/houses/public/list', {
      params: { type: filterType.value }
    })
    houses.value = res.data.data
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

watch(filterType, fetchHouses)

const handleSearch = () => {
  if (!searchQuery.value) {
    fetchHouses()
    return
  }
  houses.value = houses.value.filter(h => 
    h.title.includes(searchQuery.value) || h.address.includes(searchQuery.value)
  )
}

onMounted(fetchHouses)
</script>

<style scoped>
.house-card {
  cursor: pointer;
  border-radius: 12px;
}
.search-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent;
}
</style>

<template>
  <div class="space-y-8">
    <!-- Hero Section -->
    <div class="relative rounded-3xl overflow-hidden bg-primary p-12 text-white">
      <div class="relative z-10 max-w-2xl">
        <h1 class="text-4xl font-extrabold mb-4">寻找您的理想家园</h1>
        <p class="text-lg opacity-90 mb-8">海量房源，诚信交易，为您提供最专业的二手房交易服务。</p>
        <div class="bg-white p-2 rounded-xl shadow-lg">
          <div class="flex space-x-2">
            <el-input v-model="searchQuery" placeholder="搜索区域、小区或房源标题..." class="search-input" @keyup.enter="handleSearch" />
            <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
          </div>
          <div v-if="isAuthenticated && searchHistory.length > 0" class="mt-3 pt-3 border-t border-gray-100">
            <div class="flex justify-between items-center mb-2">
              <span class="text-gray-600 text-sm">最近搜索</span>
              <el-button type="text" size="small" @click="clearSearchHistory" class="text-gray-400 hover:text-danger !p-0">
                清空
              </el-button>
            </div>
            <div class="flex flex-wrap gap-2">
              <div
                v-for="item in searchHistory"
                :key="item.id"
                class="search-history-item flex items-center gap-1 bg-gray-50 hover:bg-gray-100 text-gray-700 px-3 py-1 rounded-full text-sm cursor-pointer transition-colors"
              >
                <span @click="searchByHistory(item.keyword)">{{ item.keyword }}</span>
                <el-icon class="text-gray-400 hover:text-danger text-xs" @click.stop="deleteSearchHistory(item.id)">
                  <Close />
                </el-icon>
              </div>
            </div>
          </div>
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
import { ref, onMounted, watch, computed } from 'vue'
import axios from 'axios'
import { HomeFilled, Location, Close } from '@element-plus/icons-vue'
import { useAuthStore } from '../store/auth'

const authStore = useAuthStore()
const isAuthenticated = computed(() => authStore.isAuthenticated)

const houses = ref([])
const loading = ref(true)
const searchQuery = ref('')
const filterType = ref('all')
const searchHistory = ref([])

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

const fetchSearchHistory = async () => {
  if (!isAuthenticated.value) return
  try {
    const res = await axios.get('/api/search-history/list', {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    if (res.data.code === 200) {
      searchHistory.value = res.data.data
    }
  } catch (err) {
    console.error(err)
  }
}

const addSearchHistory = async (keyword) => {
  if (!isAuthenticated.value || !keyword) return
  try {
    await axios.post('/api/search-history/add', null, {
      params: { keyword },
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    fetchSearchHistory()
  } catch (err) {
    console.error(err)
  }
}

const deleteSearchHistory = async (id) => {
  if (!isAuthenticated.value) return
  try {
    await axios.delete(`/api/search-history/${id}`, {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    fetchSearchHistory()
  } catch (err) {
    console.error(err)
  }
}

const clearSearchHistory = async () => {
  if (!isAuthenticated.value) return
  try {
    await axios.delete('/api/search-history/clear', {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    searchHistory.value = []
  } catch (err) {
    console.error(err)
  }
}

const searchByHistory = (keyword) => {
  searchQuery.value = keyword
  handleSearch()
}

watch(filterType, fetchHouses)

watch(isAuthenticated, (val) => {
  if (val) {
    fetchSearchHistory()
  } else {
    searchHistory.value = []
  }
})

const handleSearch = () => {
  if (!searchQuery.value) {
    fetchHouses()
    return
  }
  addSearchHistory(searchQuery.value)
  houses.value = houses.value.filter(h => 
    h.title.includes(searchQuery.value) || h.address.includes(searchQuery.value)
  )
}

onMounted(() => {
  fetchHouses()
  if (isAuthenticated.value) {
    fetchSearchHistory()
  }
})
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

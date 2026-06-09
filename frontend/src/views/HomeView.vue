<template>
  <div class="space-y-8">
    <!-- Hero Section -->
    <div class="relative rounded-3xl overflow-hidden bg-primary p-12 text-white">
      <div class="relative z-10 max-w-2xl">
        <h1 class="text-4xl font-extrabold mb-4">寻找您的理想家园</h1>
        <p class="text-lg opacity-90 mb-8">海量房源，诚信交易，为您提供最专业的二手房交易服务。</p>
        <div class="relative">
          <div class="flex space-x-2 bg-white p-2 rounded-xl shadow-lg">
            <el-input
              v-model="searchQuery"
              placeholder="搜索区域、小区或房源标题..."
              class="search-input"
              @focus="handleSearchFocus"
              @blur="handleSearchBlur"
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
          </div>
          <div
            v-if="showHistory && isAuthenticated && searchHistory.length > 0"
            class="absolute top-full left-0 right-0 mt-2 bg-white rounded-xl shadow-lg p-4 text-gray-700 z-20"
          >
            <div class="flex justify-between items-center mb-3">
              <span class="text-sm font-medium text-gray-500">最近搜索</span>
              <el-button type="primary" link size="small" @mousedown.prevent="handleClearHistory">
                清空全部
              </el-button>
            </div>
            <div class="flex flex-wrap gap-2">
              <div
                v-for="item in searchHistory"
                :key="item.id"
                class="group flex items-center bg-gray-100 hover:bg-primary hover:text-white rounded-full px-3 py-1 cursor-pointer transition-colors"
              >
                <span class="text-sm" @mousedown.prevent="handleHistoryClick(item.keyword)">{{ item.keyword }}</span>
                <el-icon
                  class="ml-1 text-gray-400 group-hover:text-white"
                  @mousedown.prevent.stop="handleDeleteHistory(item.id)"
                >
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
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import { HomeFilled, Location, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'

const authStore = useAuthStore()
const isAuthenticated = computed(() => authStore.isAuthenticated)
const token = computed(() => authStore.token)

const houses = ref([])
const allHouses = ref([])
const loading = ref(true)
const searchQuery = ref('')
const filterType = ref('all')
const searchHistory = ref([])
const showHistory = ref(false)

const getAuthHeaders = () => {
  return isAuthenticated.value ? { Authorization: `Bearer ${token.value}` } : {}
}

const fetchHouses = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/houses/public/list', {
      params: { type: filterType.value }
    })
    allHouses.value = res.data.data
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
    const res = await axios.get('/api/search-history/user/my', {
      headers: getAuthHeaders()
    })
    if (res.data.code === 200) {
      searchHistory.value = res.data.data
    }
  } catch (err) {
    console.error('获取搜索历史失败', err)
  }
}

const saveSearchHistory = async (keyword) => {
  if (!isAuthenticated.value || !keyword.trim()) return
  try {
    await axios.post('/api/search-history/user/add', { keyword: keyword.trim() }, {
      headers: getAuthHeaders()
    })
    await fetchSearchHistory()
  } catch (err) {
    console.error('保存搜索历史失败', err)
  }
}

const handleSearchFocus = () => {
  if (isAuthenticated.value && searchHistory.value.length > 0) {
    showHistory.value = true
  } else if (isAuthenticated.value) {
    fetchSearchHistory().then(() => {
      if (searchHistory.value.length > 0) {
        showHistory.value = true
      }
    })
  }
}

const handleSearchBlur = () => {
  setTimeout(() => {
    showHistory.value = false
  }, 200)
}

const handleHistoryClick = (keyword) => {
  searchQuery.value = keyword
  showHistory.value = false
  handleSearch()
}

const handleDeleteHistory = async (id) => {
  try {
    await axios.delete(`/api/search-history/user/${id}`, {
      headers: getAuthHeaders()
    })
    searchHistory.value = searchHistory.value.filter(h => h.id !== id)
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

const handleClearHistory = async () => {
  try {
    await axios.delete('/api/search-history/user/clear', {
      headers: getAuthHeaders()
    })
    searchHistory.value = []
    showHistory.value = false
  } catch (err) {
    ElMessage.error('清空失败')
  }
}

watch(filterType, fetchHouses)

const handleSearch = () => {
  const keyword = searchQuery.value.trim()
  if (!keyword) {
    houses.value = allHouses.value
    return
  }
  houses.value = allHouses.value.filter(h =>
    h.title.includes(keyword) || h.address.includes(keyword)
  )
  saveSearchHistory(keyword)
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

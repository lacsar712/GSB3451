<template>
  <div class="space-y-8">
    <!-- Hero Section -->
    <div class="relative rounded-3xl overflow-hidden bg-primary p-12 text-white">
      <div class="relative z-10 max-w-2xl">
        <h1 class="text-4xl font-extrabold mb-4">寻找您的理想家园</h1>
        <p class="text-lg opacity-90 mb-8">海量房源，诚信交易，为您提供最专业的二手房交易服务。</p>
        <div class="flex space-x-2 bg-white p-2 rounded-xl shadow-lg">
          <el-popover
            placement="bottom-start"
            :width="searchBoxWidth"
            trigger="manual"
            :visible="showHistory && authStore.isAuthenticated && historyList.length > 0"
            popper-class="search-history-popover"
          >
            <template #reference>
              <div class="flex-1" ref="searchInputWrap">
                <el-input
                  v-model="searchQuery"
                  placeholder="搜索区域、小区或房源标题..."
                  class="search-input"
                  @focus="onSearchFocus"
                  @blur="onSearchBlur"
                  @keyup.enter="handleSearch"
                />
              </div>
            </template>
            <div class="py-1">
              <div class="flex justify-between items-center px-2 pb-2 border-b border-gray-100">
                <span class="text-sm font-semibold text-gray-700">最近搜索</span>
                <el-button type="primary" link size="small" @mousedown.prevent @click="clearHistory">清空</el-button>
              </div>
              <div class="max-h-64 overflow-y-auto">
                <div
                  v-for="item in historyList"
                  :key="item.id"
                  class="flex justify-between items-center px-2 py-2 hover:bg-gray-50 cursor-pointer rounded"
                  @mousedown.prevent
                  @click="useHistory(item)"
                >
                  <div class="flex items-center text-sm text-gray-700 truncate">
                    <el-icon class="mr-2 text-gray-400"><Clock /></el-icon>
                    <span class="truncate">{{ item.keyword }}</span>
                  </div>
                  <el-icon class="text-gray-400 hover:text-red-500" @click.stop="deleteHistory(item)"><Close /></el-icon>
                </div>
              </div>
            </div>
          </el-popover>
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
import { ref, onMounted, watch, nextTick } from 'vue'
import axios from 'axios'
import { HomeFilled, Location, Clock, Close } from '@element-plus/icons-vue'
import { useAuthStore } from '../store/auth'

const authStore = useAuthStore()

const houses = ref([])
const loading = ref(true)
const searchQuery = ref('')
const filterType = ref('all')

const historyList = ref([])
const showHistory = ref(false)
const searchInputWrap = ref(null)
const searchBoxWidth = ref(320)

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

const fetchHistory = async () => {
  if (!authStore.isAuthenticated) return
  try {
    const res = await axios.get('/api/search-history/user/list')
    if (res.data.code === 200) {
      historyList.value = res.data.data || []
    }
  } catch (err) {
    console.error(err)
  }
}

const addHistory = async (keyword) => {
  if (!authStore.isAuthenticated || !keyword) return
  try {
    await axios.post('/api/search-history/user/add', { keyword })
    await fetchHistory()
  } catch (err) {
    console.error(err)
  }
}

const deleteHistory = async (item) => {
  try {
    await axios.delete(`/api/search-history/user/${item.id}`)
    historyList.value = historyList.value.filter(h => h.id !== item.id)
  } catch (err) {
    console.error(err)
  }
}

const clearHistory = async () => {
  try {
    await axios.delete('/api/search-history/user/clear')
    historyList.value = []
    showHistory.value = false
  } catch (err) {
    console.error(err)
  }
}

const useHistory = (item) => {
  searchQuery.value = item.keyword
  showHistory.value = false
  handleSearch()
}

const onSearchFocus = async () => {
  await fetchHistory()
  await nextTick()
  if (searchInputWrap.value) {
    searchBoxWidth.value = searchInputWrap.value.offsetWidth
  }
  showHistory.value = true
}

const onSearchBlur = () => {
  // 延迟隐藏，避免点击历史项时直接关闭
  setTimeout(() => {
    showHistory.value = false
  }, 200)
}

watch(filterType, fetchHouses)

const handleSearch = async () => {
  showHistory.value = false
  if (!searchQuery.value) {
    fetchHouses()
    return
  }
  await addHistory(searchQuery.value)
  houses.value = houses.value.filter(h =>
    h.title.includes(searchQuery.value) || h.address.includes(searchQuery.value)
  )
}

onMounted(() => {
  fetchHouses()
  fetchHistory()
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

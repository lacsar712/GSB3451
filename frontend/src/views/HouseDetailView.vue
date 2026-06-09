<template>
  <div v-if="house" class="space-y-8 animate-in fade-in duration-500">
    <!-- Breadcrumb -->
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>房源详情</el-breadcrumb-item>
      <el-breadcrumb-item>{{ house.title }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Main Content (Left) -->
      <div class="lg:col-span-2 space-y-6">
        <el-image :src="house.imageUrl" class="w-full h-[400px] rounded-3xl object-cover shadow-lg" :preview-src-list="[house.imageUrl]" />
        
        <el-card>
          <div class="flex justify-between items-start mb-4">
            <h1 class="text-3xl font-bold text-gray-900">{{ house.title }}</h1>
            <el-button 
              :type="isFavorited ? 'danger' : 'default'" 
              circle 
              @click="handleFavorite"
              :icon="isFavorited ? 'StarFilled' : 'Star'"
              size="large"
            />
          </div>
          <div class="flex items-center text-gray-500 mb-6">
            <el-icon class="mr-1"><Location /></el-icon> {{ house.address }}
          </div>
          
          <div class="grid grid-cols-3 gap-4 py-6 border-y border-gray-100 mb-6">
            <div class="text-center">
              <p class="text-gray-400 text-sm mb-1">价格</p>
              <p class="text-3xl font-bold text-red-500">{{ (house.price / 10000).toFixed(1) }}<span class="text-sm">万</span></p>
            </div>
            <div class="text-center">
              <p class="text-gray-400 text-sm mb-1">户型</p>
              <p class="text-xl font-bold">{{ house.houseType }}</p>
            </div>
            <div class="text-center">
              <p class="text-gray-400 text-sm mb-1">面积</p>
              <p class="text-xl font-bold">{{ house.area }}㎡</p>
            </div>
          </div>

          <div>
            <h3 class="text-xl font-bold mb-4">房源描述</h3>
            <p class="text-gray-600 leading-relaxed">{{ house.description }}</p>
          </div>
        </el-card>

        <!-- Reviews Section -->
        <el-card>
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-xl font-bold">用户评价 ({{ reviews.length }})</h3>
            <el-button v-if="token" type="primary" link @click="reviewDialogVisible = true">写评价</el-button>
          </div>
          
          <div v-if="reviews.length > 0" class="space-y-6">
            <div v-for="review in reviews" :key="review.id" class="border-b border-gray-100 last:border-0 pb-6 last:pb-0">
              <div class="flex items-start mb-2">
                <el-avatar :size="40" :src="review.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + review.username" class="mr-3" />
                <div class="flex-1">
                  <div class="flex justify-between items-center mb-1">
                    <span class="font-bold text-gray-800">{{ review.username }}</span>
                    <span class="text-xs text-gray-400">{{ formatDateTime(review.createTime) }}</span>
                  </div>
                  <el-rate v-model="review.rating" disabled show-score text-color="#ff9900" />
                </div>
              </div>
              <p class="text-gray-600 text-sm ml-[52px]">{{ review.content }}</p>
            </div>
          </div>
          <el-empty v-else description="暂无评价" />
        </el-card>
      </div>

      <!-- Sidebar (Right) -->
      <div class="space-y-6">
        <el-card class="sticky top-24">
          <div class="flex items-center mb-6 p-4 bg-gray-50 rounded-xl">
            <el-avatar :size="60" src="https://api.dicebear.com/7.x/avataaars/svg?seed=landlord" class="mr-4" />
            <div>
              <p class="font-bold text-lg text-gray-800">王经理</p>
              <p class="text-sm text-gray-500">认证房东 | 已实名</p>
            </div>
          </div>
          
          <div class="flex flex-col space-y-4">
            <el-button type="primary" size="large" class="w-full h-12 !ml-0" @click="bookingVisible = true">预约看房</el-button>
            <el-button size="large" class="w-full h-12 !ml-0" plain @click="consultVisible = true">在线咨询</el-button>
          </div>
          
          <p class="mt-6 text-center text-xs text-gray-400">
            温馨提示：线下交易存在风险，请审慎核实身份及信息并建议通过平台签署合规合同。
          </p>
        </el-card>
      </div>
    </div>

    <!-- Booking Modal -->
    <el-dialog v-model="bookingVisible" title="预约看房" width="400px" align-center class="rounded-2xl">
      <el-form label-position="top">
        <el-form-item label="选择日期时间">
          <el-date-picker v-model="bookingForm.time" type="datetime" placeholder="选择时间" class="w-full" />
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="bookingForm.remark" type="textarea" placeholder="填写您的看房要求..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookingVisible = false">取消</el-button>
        <el-button type="primary" :loading="bookingLoading" @click="handleBooking">确认预约</el-button>
      </template>
    </el-dialog>

    <!-- Consultation Modal -->
    <el-dialog v-model="consultVisible" title="在线咨询" width="400px" align-center class="rounded-2xl">
      <el-form label-position="top">
        <el-form-item label="咨询内容">
          <el-input v-model="consultContent" type="textarea" :rows="4" placeholder="请输入您想咨询的问题..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="consultVisible = false">取消</el-button>
        <el-button type="primary" :loading="consultLoading" @click="handleConsult">发送</el-button>
      </template>
    </el-dialog>

    <!-- Review Dialog -->
    <el-dialog v-model="reviewDialogVisible" title="写评价" width="400px" align-center class="rounded-2xl">
      <el-form label-position="top">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" show-score text-color="#ff9900" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="说说您的看房感受吧..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewLoading" @click="handleReviewSubmit">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { Location, Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const house = ref(null)
const bookingVisible = ref(false)
const bookingLoading = ref(false)
const bookingForm = reactive({ time: '', remark: '' })

const consultVisible = ref(false)
const consultLoading = ref(false)
const consultContent = ref('')

const isFavorited = ref(false)
const token = localStorage.getItem('token')

const reviews = ref([])
const reviewDialogVisible = ref(false)
const reviewLoading = ref(false)
const reviewForm = reactive({
  rating: 5,
  content: ''
})

const fetchHouse = async () => {
  try {
    const res = await axios.get(`/api/houses/public/${route.params.id}`)
    house.value = res.data.data
    checkFavorite()
  } catch (err) {
    ElMessage.error('获取房源详情失败')
  }
}

const fetchReviews = async () => {
  try {
    const res = await axios.get(`/api/reviews/public/house/${route.params.id}`)
    reviews.value = res.data.data
  } catch (err) {
    console.error('Failed to fetch reviews', err)
  }
}

const handleReviewSubmit = async () => {
  if (!reviewForm.content) return ElMessage.warning('请输入评价内容')
  reviewLoading.value = true
  try {
    await axios.post('/api/reviews/submit', {
      houseId: house.value.id,
      rating: reviewForm.rating,
      content: reviewForm.content
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    ElMessage.success('评价提交成功')
    reviewDialogVisible.value = false
    reviewForm.content = ''
    reviewForm.rating = 5
    fetchReviews()
  } catch (err) {
    ElMessage.error('提交失败，请重试')
  } finally {
    reviewLoading.value = false
  }
}

const checkFavorite = async () => {
  if (!token) return
  try {
    const res = await axios.get('/api/collections/user/my', {
      headers: { Authorization: `Bearer ${token}` }
    })
    isFavorited.value = res.data.data.some(c => c.houseId === house.value.id)
  } catch (err) {
    console.error(err)
  }
}

const handleFavorite = async () => {
  if (!token) return ElMessage.warning('请先登录')
  try {
    await axios.post(`/api/collections/user/toggle/${house.value.id}`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    })
    isFavorited.value = !isFavorited.value
    ElMessage.success(isFavorited.value ? '已收藏' : '已取消收藏')
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

const handleBooking = async () => {
  if (!bookingForm.time) return ElMessage.warning('请选择看房时间')
  bookingLoading.value = true
  try {
    await axios.post('/api/appointments/user/book', {
      houseId: house.value.id,
      appointmentTime: bookingForm.time,
      remark: bookingForm.remark
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    ElMessage.success('预约申请已提交')
    bookingVisible.value = false
  } catch (err) {
    ElMessage.error('预约失败，请先登录')
  } finally {
    bookingLoading.value = false
  }
}

const handleConsult = async () => {
  if (!consultContent.value) return ElMessage.warning('请输入咨询内容')
  consultLoading.value = true
  try {
    await axios.post('/api/consultations/user/submit', {
      houseId: house.value.id,
      landlordId: house.value.landlordId,
      content: consultContent.value
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    ElMessage.success('咨询已发送')
    consultVisible.value = false
    consultContent.value = ''
  } catch (err) {
    ElMessage.error('发送失败，请先登录')
  } finally {
    consultLoading.value = false
  }
}

const formatDateTime = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

onMounted(() => {
  fetchHouse()
  fetchReviews()
})
</script>

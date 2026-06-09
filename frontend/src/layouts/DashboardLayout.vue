<template>
  <div class="flex h-screen bg-gray-100 overflow-hidden">
    <!-- Sidebar -->
    <aside class="w-64 bg-white shadow-lg z-20 hidden md:flex flex-col">
      <div class="p-6 flex items-center cursor-pointer" @click="$router.push('/')">
        <el-icon class="text-2xl text-primary mr-2"><HomeFilled /></el-icon>
        <span class="text-lg font-bold">房产控制台</span>
      </div>
      <nav class="flex-1 px-4 space-y-2 mt-4">
        <template v-for="item in menuItems" :key="item.path">
          <div 
            class="flex items-center px-4 py-3 rounded-lg cursor-pointer transition-all duration-200"
            :class="[
              $route.path === item.path 
                ? 'bg-primary text-white shadow-md' 
                : 'text-gray-600 hover:bg-blue-50 hover:text-primary'
            ]"
            @click="$router.push(item.path)"
          >
            <el-icon class="mr-3"><component :is="item.icon" /></el-icon>
            <span class="font-medium">{{ item.name }}</span>
          </div>
        </template>
      </nav>
      <div class="p-4 border-t border-gray-100">
        <div class="flex items-center p-2 rounded-lg hover:bg-red-50 text-red-500 cursor-pointer" @click="handleLogout">
          <el-icon class="mr-3"><SwitchButton /></el-icon>
          <span class="font-medium">退出登录</span>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 flex flex-col overflow-hidden">
      <header class="h-16 bg-white shadow-sm flex items-center justify-between px-8 z-10">
        <div class="flex items-center">
          <h2 class="text-xl font-bold text-gray-800">{{ currentTitle }}</h2>
        </div>
        <div class="flex items-center space-x-6">
          <div class="flex items-center cursor-pointer hover:opacity-70 transition-opacity" @click="ElMessage.info(`您有 ${notificationCount} 条新提醒`)">
            <el-badge :value="notificationCount" :hidden="notificationCount === 0" class="mt-1">
              <el-icon class="text-2xl text-gray-500"><Bell /></el-icon>
            </el-badge>
          </div>
          <el-avatar :size="40" :src="auth.user?.avatar" class="border border-gray-100 shadow-sm" />
        </div>
      </header>
      <div class="flex-1 overflow-y-auto p-8">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../store/auth'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { HomeFilled, User, House, Calendar, Document, Setting, SwitchButton, Bell, Message, CreditCard } from '@element-plus/icons-vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const notificationCount = ref(0)

const fetchNotificationCount = async () => {
  try {
    const res = await axios.get('/api/notifications/count')
    notificationCount.value = res.data.data
  } catch (err) {
    console.error('Failed to fetch notifications', err)
  }
}

onMounted(() => {
  fetchNotificationCount()
  // Refresh every 30 seconds
  const timer = setInterval(fetchNotificationCount, 30000)
  return () => clearInterval(timer)
})

const menuItems = computed(() => {
  if (auth.isAdmin) {
    return [
      { name: '仪表盘', path: '/dashboard/admin', icon: Setting },
      { name: '用户管理', path: '/dashboard/admin/users', icon: User },
      { name: '房源审核', path: '/dashboard/admin/houses', icon: House },
      { name: '公告发布', path: '/dashboard/admin/announcements', icon: Message },
      { name: '支付管理', path: '/dashboard/payments', icon: CreditCard },
    ]
  } else if (auth.isLandlord) {
    return [
      { name: '房源管理', path: '/dashboard/landlord', icon: House },
      { name: '预约请求', path: '/dashboard/landlord/appointments', icon: Calendar },
      { name: '合同管理', path: '/dashboard/landlord/contracts', icon: Document },
      { name: '支付管理', path: '/dashboard/payments', icon: CreditCard },
    ]
  } else {
    return [
      { name: '我的预约', path: '/dashboard/user', icon: Calendar },
      { name: '我的收藏', path: '/dashboard/user/favorites', icon: HomeFilled },
      { name: '我的合同', path: '/dashboard/user/contracts', icon: Document },
      { name: '支付管理', path: '/dashboard/payments', icon: CreditCard },
    ]
  }
})

const currentTitle = computed(() => {
  const current = menuItems.value.find(item => item.path === route.path)
  return current ? current.name : '控制台'
})

const handleLogout = () => {
  auth.logout()
  router.push('/login')
}
</script>

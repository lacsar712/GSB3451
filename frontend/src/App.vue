<template>
  <el-config-provider :locale="zhCn">
    <div class="min-h-screen bg-gray-50">
      <nav v-if="!$route.path.startsWith('/dashboard')" class="bg-white shadow-sm sticky top-0 z-50">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="flex justify-between h-16 items-center">
            <div class="flex items-center cursor-pointer" @click="$router.push('/')">
              <el-icon class="text-3xl text-primary mr-2"><HomeFilled /></el-icon>
              <span class="text-xl font-bold bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">
                二手房交易平台
              </span>
            </div>
            <div class="flex items-center space-x-4">
              <template v-if="!auth.isAuthenticated">
                <el-button text @click="$router.push('/login')">登录</el-button>
                <el-button type="primary" @click="$router.push('/register')">注册</el-button>
              </template>
              <template v-else>
                <el-dropdown @command="handleCommand">
                  <span class="flex items-center cursor-pointer outline-none">
                    <el-avatar :size="32" :src="auth.user.avatar" class="mr-2" />
                    <span class="text-gray-700">{{ auth.user.username }}</span>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="dashboard">个人中心</el-dropdown-item>
                      <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </div>
          </div>
        </div>
      </nav>

      <main :class="{'max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8': !$route.path.startsWith('/dashboard')}">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </el-config-provider>
</template>

<script setup>
import { useAuthStore } from './store/auth'
import { useRouter } from 'vue-router'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const auth = useAuthStore()
const router = useRouter()

const goToDashboard = () => {
  if (auth.isAdmin) router.push('/dashboard/admin')
  else if (auth.isLandlord) router.push('/dashboard/landlord')
  else router.push('/dashboard/user')
}

const handleCommand = (command) => {
  if (command === 'dashboard') goToDashboard()
  else if (command === 'logout') handleLogout()
}

const handleLogout = () => {
  auth.logout()
  router.push('/')
}
</script>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

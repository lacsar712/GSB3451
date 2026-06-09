<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-2xl shadow-sm">
      <h1 class="text-2xl font-bold text-gray-800">支付管理</h1>
      <p class="text-gray-500 mt-1">管理系统内的交易流水与支付状态</p>
    </div>

    <el-card>
      <el-table :data="payments" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="支付ID" width="80" />
        <el-table-column prop="contractId" label="合同ID" width="80" />
        <el-table-column label="支付金额">
          <template #default="scope">
            <span class="font-bold text-red-500">￥{{ scope.row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间">
          <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'PENDING' && !auth.isLandlord && !auth.isAdmin" 
              type="primary" 
              size="small" 
              @click="handlePay(scope.row.id)"
            >
              去支付
            </el-button>
            <span v-else-if="scope.row.status === 'SUCCESS'" class="text-green-500 text-sm">已完成</span>
            <span v-else class="text-gray-400 text-sm">等待中</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../store/auth'
import dayjs from 'dayjs'

const auth = useAuthStore()
const payments = ref([])
const loading = ref(false)
const token = localStorage.getItem('token')

const fetchPayments = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/payments/my', {
      headers: { Authorization: `Bearer ${token}` }
    })
    payments.value = res.data.data
  } catch (err) {
    ElMessage.error('获取支付记录失败')
  } finally {
    loading.value = false
  }
}

const handlePay = (id) => {
  ElMessageBox.confirm('这只是一个模拟支付过程，确认支付该项费用吗？', '支付确认', {
    confirmButtonText: '立即支付',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await axios.post(`/api/payments/pay/${id}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      })
      ElMessage.success('支付成功')
      fetchPayments()
    } catch (err) {
      ElMessage.error('支付失败')
    }
  }).catch(() => {})
}

const getStatusType = (status) => {
  const map = { PENDING: 'warning', SUCCESS: 'success', CANCELLED: 'danger' }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = { PENDING: '待支付', SUCCESS: '支付成功', CANCELLED: '已取消' }
  return map[status] || status
}

const formatDateTime = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

onMounted(fetchPayments)
</script>

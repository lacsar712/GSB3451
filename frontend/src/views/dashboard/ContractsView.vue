<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-2xl shadow-sm flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">合同管理</h1>
        <p class="text-gray-500 mt-1">查看与签署房产交易电子合同</p>
      </div>
    </div>

    <el-card shadow="never" class="rounded-2xl border-none">
      <el-table :data="contracts" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="合同ID" width="80" />
        <el-table-column prop="houseTitle" label="房源名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="金额" width="120">
          <template #default="scope">
            <span class="font-bold text-red-500">￥{{ scope.row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="150">
          <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="right">
          <template #default="scope">
            <div class="flex justify-end space-x-2">
              <el-button link type="primary" @click="viewContract(scope.row)">详情</el-button>
              <el-button 
                v-if="scope.row.status === 'DRAFT' && !auth.isLandlord && !auth.isAdmin" 
                type="success" 
                size="small" 
                @click="handleSign(scope.row.id)"
              >
                立即签署
              </el-button>
              <el-button 
                v-if="scope.row.status === 'SIGNED' && !auth.isLandlord && !auth.isAdmin" 
                type="warning" 
                size="small" 
                @click="$router.push('/dashboard/payments')"
              >
                去支付
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Contract View Dialog -->
    <el-dialog v-model="dialogVisible" title="合同详情" width="600px" class="rounded-2xl">
      <div v-if="selectedContract" class="p-4 bg-gray-50 rounded-xl min-h-[300px] whitespace-pre-wrap text-gray-700 leading-relaxed">
        {{ selectedContract.contractContent }}
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button 
          v-if="selectedContract?.status === 'DRAFT' && !auth.isLandlord && !auth.isAdmin" 
          type="success" 
          @click="handleSign(selectedContract.id)"
        >
          确认签署
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../store/auth'
import dayjs from 'dayjs'

const auth = useAuthStore()
const contracts = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const selectedContract = ref(null)
const token = localStorage.getItem('token')

const fetchContracts = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/contracts/my', {
      headers: { Authorization: `Bearer ${token}` }
    })
    contracts.value = res.data.data
  } catch (err) {
    ElMessage.error('获取合同列表失败')
  } finally {
    loading.value = false
  }
}

const handleSign = (id) => {
  ElMessageBox.confirm('确认签署该电子合同吗？签署后将生成支付账单。', '签署确认', {
    confirmButtonText: '立即签署',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.post(`/api/contracts/user/sign/${id}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      })
      ElMessage.success(res.data.data)
      dialogVisible.value = false
      fetchContracts()
    } catch (err) {
      ElMessage.error('签署失败')
    }
  }).catch(() => {})
}

const viewContract = (contract) => {
  selectedContract.value = contract
  dialogVisible.value = true
}

const getStatusType = (status) => {
  const map = { DRAFT: 'info', SIGNED: 'success', TERMINATED: 'danger' }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = { DRAFT: '待签署', SIGNED: '已签署', TERMINATED: '已终止' }
  return map[status] || status
}

const formatDateTime = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

onMounted(fetchContracts)
</script>

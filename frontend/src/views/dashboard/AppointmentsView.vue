<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-2xl shadow-sm">
      <h1 class="text-2xl font-bold text-gray-800">通过预约请求</h1>
      <p class="text-gray-500 mt-1">管理用户对您发布房源的看房预约</p>
    </div>

    <el-card>
      <el-table :data="appointments" style="width: 100%" v-loading="loading">
        <el-table-column prop="houseTitle" label="房源名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="appointmentTime" label="预约时间">
          <template #default="scope">{{ formatDateTime(scope.row.appointmentTime) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="right">
          <template #default="scope">
            <div class="flex items-center justify-end space-x-2">
              <template v-if="scope.row.status === 'PENDING'">
                <el-button link type="success" @click="handleAction(scope.row.id, 'approve')">通过</el-button>
                <el-button link type="danger" @click="handleAction(scope.row.id, 'reject')">拒绝</el-button>
              </template>
              <el-button v-if="scope.row.status === 'APPROVED'" link type="warning" @click="openContractDialog(scope.row)">生成合同</el-button>
              <el-button link type="primary" @click="$router.push(`/house/${scope.row.houseId}`)">房源详情</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Contract Dialog -->
    <el-dialog v-model="contractVisible" title="发起电子合同" width="500px" align-center class="rounded-2xl">
      <el-form :model="contractForm" label-position="top">
        <el-form-item label="房源">
          <el-input :value="selectedAppointment?.houseTitle" disabled />
        </el-form-item>
        <el-form-item label="成交金额 (元)">
          <el-input-number v-model="contractForm.totalAmount" :min="0" class="w-full" />
        </el-form-item>
        <el-form-item label="合同内容">
          <el-input v-model="contractForm.contractContent" type="textarea" :rows="6" placeholder="请输入合规的租赁或买卖合同条款..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="contractVisible = false">取消</el-button>
        <el-button type="primary" :loading="contractLoading" @click="submitContract">确认发起</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const appointments = ref([])
const loading = ref(false)
const token = localStorage.getItem('token')

// Contract related
const contractVisible = ref(false)
const contractLoading = ref(false)
const selectedAppointment = ref(null)
const contractForm = reactive({
  totalAmount: 0,
  contractContent: ''
})

const fetchAppointments = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/appointments/landlord/my', {
      headers: { Authorization: `Bearer ${token}` }
    })
    appointments.value = res.data.data
  } catch (err) {
    ElMessage.error('获取预约列表失败')
  } finally {
    loading.value = false
  }
}

const handleAction = async (id, action) => {
  try {
    await axios.post(`/api/appointments/landlord/${action}/${id}`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    })
    ElMessage.success('操作成功')
    fetchAppointments()
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

const openContractDialog = (row) => {
  selectedAppointment.value = row
  contractForm.totalAmount = 0
  contractForm.contractContent = `甲方（出租/出售方）：\n乙方（承租/购买方）：\n\n关于房源【${row.houseTitle}】的交易协议如下：\n1. 二手房买卖/租赁约定...\n2. 付款金额与方式...\n3. 双方责任与义务...`
  contractVisible.value = true
}

const submitContract = async () => {
  if (!contractForm.contractContent) return ElMessage.warning('请输入合同内容')
  if (contractForm.totalAmount <= 0) return ElMessage.warning('请输入有效的金额')
  
  contractLoading.value = true
  try {
    await axios.post('/api/contracts/landlord/create', {
      userId: selectedAppointment.value.userId,
      houseId: selectedAppointment.value.houseId,
      totalAmount: contractForm.totalAmount,
      contractContent: contractForm.contractContent
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    ElMessage.success('合同发起成功，请等待用户签署')
    contractVisible.value = false
    fetchAppointments()
  } catch (err) {
    ElMessage.error('发起失败')
  } finally {
    contractLoading.value = false
  }
}

const getStatusType = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'info' }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = { PENDING: '待确认', APPROVED: '已同意', REJECTED: '已拒绝', CANCELLED: '已取消' }
  return map[status] || status
}

const formatDateTime = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

onMounted(fetchAppointments)
</script>

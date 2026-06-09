<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-2xl shadow-sm">
      <h1 class="text-2xl font-bold text-gray-800">房源审核</h1>
      <p class="text-gray-500 mt-1">请审核新发布的房源信息，确保质量与真实性</p>
    </div>

    <el-card>
      <el-table :data="allHouses" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="price" label="价格">
          <template #default="scope">{{ (scope.row.price / 10000).toFixed(1) }}万</template>
        </el-table-column>
        <el-table-column prop="status" label="当前状态">
          <template #default="scope">
            <el-tooltip
              v-if="scope.row.status === 'REJECTED' && scope.row.rejectionReason"
              class="box-item"
              effect="dark"
              :content="scope.row.rejectionReason"
              placement="top"
            >
              <el-tag :type="getStatusType(scope.row.status)" class="cursor-help">{{ getStatusLabel(scope.row.status) }}</el-tag>
            </el-tooltip>
            <el-tag v-else :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="right">
          <template #default="scope">
            <template v-if="scope.row.status === 'PENDING'">
              <el-button link type="success" @click="handleApprove(scope.row)">通过</el-button>
              <el-button link type="danger" @click="openRejectDialog(scope.row)">驳回</el-button>
            </template>
            <el-button link type="primary" @click="$router.push(`/dashboard/admin/house/${scope.row.id}`)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Reject Dialog -->
    <el-dialog v-model="rejectVisible" title="房源驳回" width="400px" align-center class="rounded-2xl">
      <el-form label-position="top">
        <el-form-item label="驳回原因">
          <el-input v-model="rejectionReason" type="textarea" :rows="4" placeholder="请输入驳回具体原因..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="handleReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const allHouses = ref([])
const loading = ref(false)
const token = localStorage.getItem('token')

const rejectVisible = ref(false)
const rejectLoading = ref(false)
const rejectionReason = ref('')
const currentHouse = ref(null)

const fetchAllHouses = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/houses/admin/all', {
      headers: { Authorization: `Bearer ${token}` }
    })
    allHouses.value = res.data.data
  } catch (err) {
    ElMessage.error('获取房源列表失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要通过该房源发布申请吗？', '提示', { type: 'warning' })
    await axios.post(`/api/houses/admin/approve/${row.id}`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    })
    ElMessage.success('房源已审核通过')
    fetchAllHouses()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('操作失败')
  }
}

const openRejectDialog = (row) => {
  currentHouse.value = row
  rejectionReason.value = ''
  rejectVisible.value = true
}

const handleReject = async () => {
  if (!rejectionReason.value) return ElMessage.warning('请输入驳回原因')
  rejectLoading.value = true
  try {
    await axios.post(`/api/houses/admin/reject/${currentHouse.value.id}`, rejectionReason.value, {
      headers: { 
        Authorization: `Bearer ${token}`,
        'Content-Type': 'text/plain'
      }
    })
    ElMessage.success('房源已驳回')
    rejectVisible.value = false
    fetchAllHouses()
  } catch (err) {
    ElMessage.error('操作失败')
  } finally {
    rejectLoading.value = false
  }
}

const getStatusType = (status) => {
  const map = { PENDING: 'warning', ACTIVE: 'success', REJECTED: 'danger', SOLD: 'info', ARCHIVED: 'info' }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = { PENDING: '审核中', ACTIVE: '已通过', REJECTED: '已驳回', SOLD: '已成交', ARCHIVED: '已下架' }
  return map[status] || status
}

onMounted(fetchAllHouses)
</script>

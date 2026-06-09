<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center bg-white p-6 rounded-2xl shadow-sm">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">房源管理</h1>
        <p class="text-gray-500 mt-1">您可以发布、修改或删除您的房源信息</p>
      </div>
      <el-button type="primary" size="large" @click="openAddDialog">
        <el-icon class="mr-1"><Plus /></el-icon> 发布房源
      </el-button>
    </div>

    <el-card>
      <el-table :data="myHouses" style="width: 100%" v-loading="loading">
        <el-table-column label="房源图片" width="120">
          <template #default="scope">
            <el-image :src="scope.row.imageUrl" class="w-16 h-12 rounded-lg object-cover" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="price" label="价格">
          <template #default="scope">{{ (scope.row.price / 10000).toFixed(1) }}万</template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
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
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- House Dialog (Add/Edit) -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑房源' : '发布新房源'" width="600px">
      <el-form :model="houseForm" label-position="top">
        <el-form-item label="房源标题">
          <el-input v-model="houseForm.title" placeholder="例如：精装两居室，近地铁" />
        </el-form-item>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="售价 (元)">
            <el-input-number v-model="houseForm.price" class="w-full" :min="0" />
          </el-form-item>
          <el-form-item label="面积 (㎡)">
            <el-input-number v-model="houseForm.area" class="w-full" :min="0" />
          </el-form-item>
        </div>
        <el-form-item label="详细地址">
          <el-input v-model="houseForm.address" placeholder="请输入完整地址" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="houseForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="图片链接">
          <el-input v-model="houseForm.imageUrl" placeholder="https://..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHouse">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const myHouses = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const houseForm = reactive({
  title: '', price: 0, area: 0, address: '', description: '', imageUrl: ''
})

const token = localStorage.getItem('token')

const fetchMyHouses = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/houses/landlord/my', {
      headers: { Authorization: `Bearer ${token}` }
    })
    myHouses.value = res.data.data
  } catch (err) {
    ElMessage.error('获取房源失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(houseForm, { title: '', price: 0, area: 0, address: '', description: '', imageUrl: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(houseForm, row)
  dialogVisible.value = true
}

const submitHouse = async () => {
  try {
    const url = isEdit.value ? '/api/houses/landlord/update' : '/api/houses/landlord/add'
    const method = isEdit.value ? 'put' : 'post'
    await axios[method](url, houseForm, {
      headers: { Authorization: `Bearer ${token}` }
    })
    ElMessage.success(isEdit.value ? '修改成功' : '发布成功，请等待审核')
    dialogVisible.value = false
    fetchMyHouses()
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  // Custom delete UI requirement
  ElMessageBox({
    title: '确认删除房源？',
    message: `<div class="p-4 flex flex-col items-center">
                <div class="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mb-4">
                  <i class="el-icon-warning text-red-500 text-3xl"></i>
                </div>
                <p class="text-gray-600">您确定要删除 <b>${row.title}</b> 吗？此操作不可撤销。</p>
              </div>`,
    dangerouslyUseHTMLString: true,
    showCancelButton: true,
    confirmButtonText: '确定删除',
    cancelButtonText: '我在想想',
    confirmButtonClass: 'el-button--danger',
    customClass: 'custom-delete-dialog'
  }).then(async () => {
    try {
      await axios.delete(`/api/houses/landlord/delete/${row.id}`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      ElMessage.success('房源已删除')
      fetchMyHouses()
    } catch (err) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const getStatusType = (status) => {
  const map = { PENDING: 'warning', ACTIVE: 'success', SOLD: 'info', ARCHIVED: 'danger' }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = { PENDING: '审核中', ACTIVE: '展示中', REJECTED: '已驳回', SOLD: '已成交', ARCHIVED: '已下架' }
  return map[status] || status
}

onMounted(fetchMyHouses)
</script>

<style>
.custom-delete-dialog {
  border-radius: 16px;
  overflow: hidden;
}
</style>

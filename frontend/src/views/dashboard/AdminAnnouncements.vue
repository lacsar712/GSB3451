<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center bg-white p-6 rounded-2xl shadow-sm">
      <h1 class="text-2xl font-bold text-gray-800">公告管理</h1>
      <el-button type="primary" @click="addVisible = true">发布公告</el-button>
    </div>

    <el-card>
      <el-table :data="announcements" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发布时间">
          <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="right">
          <template #default="scope">
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="addVisible" title="发布新公告" width="500px">
      <el-form :model="form" label-position="top">
        <el-form-item label="公告标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="公告内容">
          <el-input v-model="form.content" type="textarea" :rows="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const announcements = ref([])
const loading = ref(false)
const addVisible = ref(false)
const form = reactive({ title: '', content: '' })
const token = localStorage.getItem('token')

const fetchAll = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/announcements/public/list')
    announcements.value = res.data.data
  } catch (err) {
    ElMessage.error('获取公告失败')
  } finally {
    loading.value = false
  }
}

const submitAdd = async () => {
  try {
    await axios.post('/api/announcements/admin/add', form, {
      headers: { Authorization: `Bearer ${token}` }
    })
    ElMessage.success('发布成功')
    addVisible.value = false
    fetchAll()
  } catch (err) {
    ElMessage.error('发布失败')
  }
}

const handleDelete = async (row) => {
  try {
    await axios.delete(`/api/announcements/admin/delete/${row.id}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    ElMessage.success('已删除')
    fetchAll()
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

onMounted(fetchAll)
</script>

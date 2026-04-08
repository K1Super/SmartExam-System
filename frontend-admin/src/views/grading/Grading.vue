<template>
  <div class="grading-container">
    <el-card class="grading-card">
      <template #header>
        <div class="card-header">
          <span>自动阅卷</span>
        </div>
      </template>
      <el-table :data="examRecords" style="width: 100%">
        <el-table-column prop="id" label="记录ID" width="80"></el-table-column>
        <el-table-column prop="examId" label="考试ID"></el-table-column>
        <el-table-column prop="userId" label="用户ID"></el-table-column>
        <el-table-column prop="startTime" label="开始时间">
          <template #default="scope">
            {{ formatDate(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间">
          <template #default="scope">
            {{ formatDate(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="warning">答题中</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="info">已提交</el-tag>
            <el-tag v-else-if="scope.row.status === 3" type="success">已批改</el-tag>
            <el-tag v-else type="danger">未开始</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleAutoGrade(scope.row.id)"
              :disabled="scope.row.status !== 2"
            >
              自动阅卷
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'

const examRecords = ref([])

onMounted(() => {
  fetchExamRecords()
})

const fetchExamRecords = async () => {
  try {
    const response = await api.get('/exam-records')
    if (response.code === 200) {
      examRecords.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取考试记录失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const handleAutoGrade = async (recordId) => {
  try {
    const response = await api.post(`/grading/auto/${recordId}`)
    if (response.code === 200) {
      ElMessage.success('自动阅卷成功')
      fetchExamRecords()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('自动阅卷失败')
  }
}
</script>

<style scoped>
.grading-container {
  padding: 20px;
}

.grading-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

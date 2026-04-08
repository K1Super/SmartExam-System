<template>
  <div class="exam-detail-container">
    <el-card class="exam-detail-card">
      <template #header>
        <div class="card-header">
          <span>考试详情</span>
        </div>
      </template>
      <div class="exam-info">
        <el-descriptions :column="2">
          <el-descriptions-item label="考试名称">
            {{ exam?.title }}
          </el-descriptions-item>
          <el-descriptions-item label="考试时长">
            {{ exam?.duration }} 分钟
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">
            {{ formatDate(exam?.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间">
            {{ formatDate(exam?.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="exam?.status === 0" type="info">未开始</el-tag>
            <el-tag v-else-if="exam?.status === 1" type="success">进行中</el-tag>
            <el-tag v-else type="danger">已结束</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="exam-actions" style="margin-top: 30px;">
          <el-button 
            type="primary" 
            @click="handleStartExam"
            :disabled="exam?.status !== 1"
          >
            开始考试
          </el-button>
          <el-button @click="router.push('/exams')">返回列表</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const route = useRoute()
const router = useRouter()
const examId = ref(route.params.id)
const exam = ref(null)

onMounted(() => {
  fetchExamDetail()
})

const fetchExamDetail = async () => {
  try {
    const response = await api.get(`/exams/${examId.value}`)
    if (response.code === 200) {
      exam.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取考试详情失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const handleStartExam = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    if (!user) {
      ElMessage.error('请先登录')
      return
    }
    
    const response = await api.post('/exam-records/start', {
      examId: examId.value,
      userId: user.id
    })
    
    if (response.code === 200) {
      router.push(`/exam/${examId.value}/answer?recordId=${response.data.id}`)
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('开始考试失败')
  }
}
</script>

<style scoped>
.exam-detail-container {
  padding: 20px;
}

.exam-detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.exam-info {
  margin-top: 20px;
}

.exam-actions {
  display: flex;
  gap: 10px;
}
</style>

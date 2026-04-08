<template>
  <div class="exams-container">
    <el-card class="exams-card">
      <template #header>
        <div class="card-header">
          <span>可用考试</span>
        </div>
      </template>
      <el-table :data="examsList" style="width: 100%">
        <el-table-column prop="id" label="考试ID" width="80"></el-table-column>
        <el-table-column prop="title" label="考试名称"></el-table-column>
        <el-table-column prop="startTime" label="开始时间">
          <template #default="scope">
            {{ formatDate(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间">
          <template #default="scope">
            {{ formatDate(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="考试时长(分钟)"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="info">未开始</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="success">进行中</el-tag>
            <el-tag v-else type="danger">已结束</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleStartExam(scope.row.id)"
              :disabled="scope.row.status !== 1"
            >
              开始考试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="exams-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>我的考试记录</span>
        </div>
      </template>
      <el-table :data="myExamRecords" style="width: 100%">
        <el-table-column prop="examId" label="考试ID" width="80"></el-table-column>
        <el-table-column prop="examTitle" label="考试名称"></el-table-column>
        <el-table-column prop="startTime" label="开始时间">
          <template #default="scope">
            {{ formatDate(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间">
          <template #default="scope">
            {{ scope.row.submitTime ? formatDate(scope.row.submitTime) : '未提交' }}
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分">
          <template #default="scope">
            {{ scope.row.score || '待批改' }}
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
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()
const examsList = ref([])
const myExamRecords = ref([])

onMounted(() => {
  fetchExams()
  fetchMyExamRecords()
})

const fetchExams = async () => {
  try {
    const response = await api.get('/exams')
    if (response.code === 200) {
      examsList.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取考试列表失败')
  }
}

const fetchMyExamRecords = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    if (user) {
      const response = await api.get(`/exam-records/user/${user.id}`)
      if (response.code === 200) {
        myExamRecords.value = response.data
      }
    }
  } catch (error) {
    ElMessage.error('获取考试记录失败')
  }
}

const handleStartExam = async (examId) => {
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    if (!user) {
      ElMessage.error('请先登录')
      return
    }
    
    const response = await api.post('/exam-records/start', {
      examId: examId,
      userId: user.id
    })
    
    if (response.code === 200) {
      router.push(`/exam/${examId}/answer?recordId=${response.data.id}`)
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('开始考试失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}
</script>

<style scoped>
.exams-container {
  padding: 20px;
}

.exams-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

<template>
  <div class="results-container">
    <el-card class="results-card">
      <template #header>
        <div class="card-header">
          <span>考试成绩</span>
        </div>
      </template>
      <el-table :data="examResults" style="width: 100%">
        <el-table-column prop="examId" label="考试ID" width="80"></el-table-column>
        <el-table-column prop="examTitle" label="考试名称"></el-table-column>
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
        <el-table-column prop="score" label="得分">
          <template #default="scope">
            <span class="score" :class="getScoreClass(scope.row.score)">
              {{ scope.row.score }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 3" type="success">已批改</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="info">已提交</el-tag>
            <el-tag v-else type="warning">答题中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="viewDetails(scope.row.id)"
              :disabled="scope.row.status !== 3"
            >
              查看详情
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
import api from '../api'

const examResults = ref([])

onMounted(() => {
  fetchExamResults()
})

const fetchExamResults = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    if (user) {
      const response = await api.get(`/exam-records/user/${user.id}`)
      if (response.code === 200) {
        examResults.value = response.data
      }
    }
  } catch (error) {
    ElMessage.error('获取成绩失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const getScoreClass = (score) => {
  if (!score) return ''
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

const viewDetails = (recordId) => {
  // 这里可以跳转到成绩详情页面
  console.log('查看详情:', recordId)
}
</script>

<style scoped>
.results-container {
  padding: 20px;
}

.results-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.score {
  font-weight: bold;
  font-size: 16px;
}

.score-excellent {
  color: #67C23A;
}

.score-good {
  color: #409EFF;
}

.score-pass {
  color: #E6A23C;
}

.score-fail {
  color: #F56C6C;
}
</style>

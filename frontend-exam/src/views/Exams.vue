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
        <el-table-column prop="paperId" label="试卷">
          <template #default="scope">
            {{ getPaperTitle(scope.row.paperId) }}
          </template>
        </el-table-column>
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
              v-if="getUserExamStatus(scope.row.id) === 0 && scope.row.status === 1"
              type="primary" 
              size="small" 
              @click="handleStartExam(scope.row.id)"
            >
              开始考试
            </el-button>
            <el-button 
              v-else-if="getUserExamStatus(scope.row.id) === 1 && scope.row.status === 1"
              type="warning" 
              size="small" 
              @click="handleContinueExam(scope.row.id)"
            >
              继续考试
            </el-button>
            <el-button 
              v-else
              type="info" 
              size="small" 
              disabled
            >
              {{ getUserExamStatusText(scope.row.id) }}
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
            <span v-if="scope.row.status === 3 && scope.row.score !== null">{{ scope.row.score }} 分</span>
            <span v-else>待批改</span>
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
              v-if="scope.row.status === 1"
              type="warning" 
              size="small" 
              @click="handleContinueExam(scope.row.examId)"
            >
              继续考试
            </el-button>
            <el-button 
              v-if="scope.row.status === 3"
              type="primary" 
              size="small" 
              @click="viewExamPaper(scope.row)"
            >
              查看试卷
            </el-button>
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
const examPapersList = ref([])

onMounted(async () => {
  await fetchMyExamRecords() // 必须先拿我的记录，再过滤可用考试
  fetchExams()
  fetchExamPapers()
})

const fetchExamPapers = async () => {
  try {
    const response = await api.get('/exam-papers')
    if (response.code === 200) {
      examPapersList.value = response.data
    }
  } catch (error) {
    console.error('获取试卷列表失败')
  }
}

const getPaperTitle = (paperId) => {
  const paper = examPapersList.value.find(p => p.id === paperId)
  return paper ? paper.title : paperId
}

const fetchExams = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    console.log('当前登录学生:', user)
    const response = await api.get('/exams')
    console.log('所有考试列表:', response.data)
    
    if (response.code === 200) {
      // 只显示学生所在班级，并且还没开始考的考试
      if (user && user.clazzId) {
        const userClazzId = String(user.clazzId)
        examsList.value = response.data.filter(exam => {
          if (!exam.clazzIds) return false
          const examClazzIds = String(exam.clazzIds).split(',').map(id => id.trim())
          const classMatch = examClazzIds.includes(userClazzId)
          // 关键：只要有考试记录，就不在可用考试里显示了
          const notStarted = !myExamRecords.value.find(r => r.examId === exam.id)
          return classMatch && notStarted
        })
        console.log('过滤后学生可见考试:', examsList.value)
      } else {
        examsList.value = response.data
      }
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

const getUserExamStatus = (examId) => {
  const record = myExamRecords.value.find(r => r.examId === examId)
  if (!record) return 0 // 未开始
  return record.status // 1=答题中，2=已提交，3=已批改
}

const getUserExamStatusText = (examId) => {
  const status = getUserExamStatus(examId)
  if (status === 0) return '未到考试时间'
  if (status === 1) return '答题中'
  if (status === 2) return '已交卷'
  if (status === 3) return '已批改'
  return '已结束'
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
      // 开始考试成功，立即刷新列表，从可用考试移除
      await fetchMyExamRecords()
      fetchExams()
      router.push(`/exam/${examId}/answer?recordId=${response.data.id}`)
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '开始考试失败')
  }
}

const handleContinueExam = async (examId) => {
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    if (!user) {
      ElMessage.error('请先登录')
      return
    }
    
    const record = myExamRecords.value.find(r => r.examId === examId)
    if (record) {
      router.push(`/exam/${examId}/answer?recordId=${record.id}`)
    }
  } catch (error) {
    ElMessage.error('继续考试失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const viewExamPaper = (record) => {
  router.push(`/exam-result/${record.id}`)
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

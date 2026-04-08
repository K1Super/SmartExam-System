<template>
  <div class="manual-grading-container">
    <el-card class="manual-grading-card">
      <template #header>
        <div class="card-header">
          <span>手动阅卷</span>
        </div>
      </template>
      <el-table :data="answerRecords" style="width: 100%">
        <el-table-column prop="id" label="记录ID" width="80"></el-table-column>
        <el-table-column prop="examRecordId" label="考试记录ID"></el-table-column>
        <el-table-column prop="questionId" label="题目ID"></el-table-column>
        <el-table-column prop="userAnswer" label="用户答案" show-overflow-tooltip></el-table-column>
        <el-table-column prop="isCorrect" label="状态">
          <template #default="scope">
            <el-tag v-if="scope.row.isCorrect === 1" type="success">正确</el-tag>
            <el-tag v-else-if="scope.row.isCorrect === 0" type="danger">错误</el-tag>
            <el-tag v-else type="warning">待批改</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleManualGrade(scope.row)"
              :disabled="scope.row.isCorrect !== 2"
            >
              手动评分
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 手动评分对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="手动评分"
      width="500px"
    >
      <el-form :model="gradeForm" :rules="rules" ref="gradeFormRef" label-width="80px">
        <el-form-item label="用户答案">
          <el-input v-model="gradeForm.userAnswer" type="textarea" :rows="4" readonly></el-input>
        </el-form-item>
        <el-form-item label="得分" prop="score">
          <el-input v-model.number="gradeForm.score" placeholder="请输入得分"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitGrade">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'

const answerRecords = ref([])
const dialogVisible = ref(false)
const gradeFormRef = ref(null)
const gradeForm = ref({
  id: null,
  userAnswer: '',
  score: 0
})

const rules = {
  score: [
    { required: true, message: '请输入得分', trigger: 'blur' }
  ]
}

onMounted(() => {
  fetchAnswerRecords()
})

const fetchAnswerRecords = async () => {
  try {
    const response = await api.get('/answer-records')
    if (response.code === 200) {
      answerRecords.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取答题记录失败')
  }
}

const handleManualGrade = (record) => {
  gradeForm.value = {
    id: record.id,
    userAnswer: record.userAnswer,
    score: record.score || 0
  }
  dialogVisible.value = true
}

const handleSubmitGrade = async () => {
  if (!gradeFormRef.value) return
  
  await gradeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await api.post(`/grading/manual/${gradeForm.value.id}`, {
          score: gradeForm.value.score
        })
        if (response.code === 200) {
          ElMessage.success('评分成功')
          dialogVisible.value = false
          fetchAnswerRecords()
        } else {
          ElMessage.error(response.message)
        }
      } catch (error) {
        ElMessage.error('评分失败')
      }
    }
  })
}
</script>

<style scoped>
.manual-grading-container {
  padding: 20px;
}

.manual-grading-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

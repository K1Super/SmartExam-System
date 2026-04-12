<template>
  <div class="exam-answer-container">
    <el-card class="exam-card">
      <template #header>
        <div class="card-header">
          <span class="exam-title">{{ exam?.title }}</span>
          <div class="exam-info">
            <span class="time-left" :class="{ 'time-warning': remainingTime < 600, 'time-danger': remainingTime < 300 }">
              剩余时间: {{ formatTime(remainingTime) }}
            </span>
            <el-button type="danger" @click="handleSubmit" style="margin-left: 20px;">提交试卷</el-button>
          </div>
        </div>
      </template>
      
      <div class="exam-content">
        <div class="questions-nav">
          <h3>题目导航</h3>
          <div class="question-numbers">
            <div 
              v-for="(question, index) in questions" 
              :key="question.id"
              :class="['question-number', getQuestionStatusClass(index)]"
              @click="currentQuestion = index"
            >
              {{ index + 1 }}
            </div>
          </div>
          <div class="nav-legend">
            <div class="legend-item">
              <div class="legend-dot unattempted"></div>
              <span>未作答</span>
            </div>
            <div class="legend-item">
              <div class="legend-dot attempted"></div>
              <span>已作答</span>
            </div>
          </div>
        </div>
        
        <div class="question-content">
          <div v-if="questions.length > 0">
            <div class="question-header">
              <h4>第 {{ currentQuestion + 1 }} 题 ({{ getQuestionType(questions[currentQuestion].type) }})</h4>
              <span class="question-score">分值: {{ questions[currentQuestion].score }}</span>
            </div>
            <div class="question-body">
              <div class="question-text" v-html="questions[currentQuestion].content"></div>
              
              <!-- 单选题 -->
              <div v-if="questions[currentQuestion].type === 1" class="question-options">
                <div class="option-item" v-for="(option, index) in parseOptions(questions[currentQuestion].options)" :key="index">
                  <el-radio v-model="answers[currentQuestion].userAnswer" :label="String.fromCharCode(65 + index)">
                    <span class="option-label">{{ String.fromCharCode(65 + index) }}</span>
                    <span class="option-text">{{ option }}</span>
                  </el-radio>
                </div>
              </div>
              
              <!-- 多选题 - 必须用el-checkbox-group包裹！ -->
              <div v-else-if="questions[currentQuestion].type === 2" class="question-options">
                <el-checkbox-group v-model="answers[currentQuestion].userAnswer">
                  <div class="option-item" v-for="(option, index) in parseOptions(questions[currentQuestion].options)" :key="index">
                    <el-checkbox :label="String.fromCharCode(65 + index)">
                      <span class="option-label">{{ String.fromCharCode(65 + index) }}</span>
                      <span class="option-text">{{ option }}</span>
                    </el-checkbox>
                  </div>
                </el-checkbox-group>
              </div>
              
              <!-- 判断题 -->
              <div v-else-if="questions[currentQuestion].type === 3" class="question-options">
                <div class="judgment-options">
                  <el-radio v-model="answers[currentQuestion].userAnswer" label="正确">
                    <span class="judgment-label">正确</span>
                  </el-radio>
                  <el-radio v-model="answers[currentQuestion].userAnswer" label="错误">
                    <span class="judgment-label">错误</span>
                  </el-radio>
                </div>
              </div>
              
              <!-- 填空题 -->
              <div v-else-if="questions[currentQuestion].type === 4" class="question-options">
                <el-input 
                  v-model="answers[currentQuestion].userAnswer" 
                  type="textarea" 
                  placeholder="请输入答案"
                  :rows="3"
                  class="fill-blank-input"
                ></el-input>
              </div>
              
              <!-- 简答题 -->
              <div v-else-if="questions[currentQuestion].type === 5" class="question-options">
                <el-input 
                  v-model="answers[currentQuestion].userAnswer" 
                  type="textarea" 
                  placeholder="请输入答案"
                  :rows="5"
                  class="essay-input"
                ></el-input>
              </div>
            </div>
            
            <div class="question-navigation">
              <el-button @click="prevQuestion" :disabled="currentQuestion === 0" type="primary" plain>
                <el-icon><ArrowLeft /></el-icon>
                上一题
              </el-button>
              <el-button @click="nextQuestion" :disabled="currentQuestion === questions.length - 1" type="primary" plain>
                下一题
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
          <div v-else class="loading">
            <el-icon class="loading-icon"><Loading /></el-icon>
            <span>正在加载题目...</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Loading } from '@element-plus/icons-vue'
import api from '../api'

const route = useRoute()
const router = useRouter()
const examId = ref(route.params.id)
const recordId = ref(route.query.recordId)

const exam = ref(null)
const questions = ref([])
const answers = ref([])
const currentQuestion = ref(0)
const remainingTime = ref(0)
const timer = ref(null)

onMounted(() => {
  if (!recordId.value) {
    ElMessage.error('考试记录ID不存在，无法加载考试')
    router.push('/exams')
    return
  }
  fetchExamInfo()
  fetchQuestions()
})

onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
})

const fetchExamInfo = async () => {
  try {
    const response = await api.get(`/exams/${examId.value}`)
    if (response.code === 200) {
      exam.value = response.data
      remainingTime.value = exam.value.duration * 60
      startTimer()
    }
  } catch (error) {
    ElMessage.error('获取考试信息失败')
  }
}

const fetchQuestions = async () => {
  try {
    // 先获取考试信息，包含试卷ID
    const examResponse = await api.get(`/exams/${examId.value}`)
    if (examResponse.code === 200) {
      const paperId = examResponse.data.paperId
      
      // 获取试卷的题目ID列表
      const paperQuestionsResponse = await api.get(`/exam-papers/${paperId}/questions`)
      if (paperQuestionsResponse.code === 200) {
        const questionIds = paperQuestionsResponse.data
        
        // 获取题目的详细信息
        const questionsResponse = await api.get('/questions')
        if (questionsResponse.code === 200) {
          // 根据ID过滤题目
          questions.value = questionsResponse.data.filter(q => questionIds.includes(q.id))
          // 初始化答案数组：多选题用数组，其他用字符串
          answers.value = questions.value.map(q => ({ 
            userAnswer: q.type === 2 ? [] : '' 
          }))
        }
      }
    }
  } catch (error) {
    ElMessage.error('获取题目失败')
  }
}

const startTimer = () => {
  timer.value = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      clearInterval(timer.value)
      handleSubmit()
    }
  }, 1000)
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const getQuestionType = (type) => {
  const types = ['', '单选题', '多选题', '判断题', '填空题', '简答题']
  return types[type] || '未知题型'
}

const parseOptions = (optionsStr) => {
  try {
    if (typeof optionsStr === 'string') {
      const parsed = JSON.parse(optionsStr)
      // 确保返回的是数组格式
      if (Array.isArray(parsed)) {
        return parsed
      }
    }
    return []
  } catch (error) {
    return []
  }
}

const getQuestionStatusClass = (index) => {
  if (!answers.value[index]) return 'unattempted'
  const userAnswer = answers.value[index].userAnswer
  // 多选题：数组有长度就是已作答
  if (Array.isArray(userAnswer)) {
    return userAnswer.length > 0 ? 'attempted' : 'unattempted'
  }
  // 其他题型：有字符串就是已作答
  return userAnswer ? 'attempted' : 'unattempted'
}

const prevQuestion = () => {
  if (currentQuestion.value > 0) {
    currentQuestion.value--
  }
}

const nextQuestion = () => {
  if (currentQuestion.value < questions.value.length - 1) {
    currentQuestion.value++
  }
}

const handleSubmit = async () => {
  if (!recordId.value) {
    ElMessage.error('考试记录ID不存在，无法提交')
    return
  }
  
  // 提交确认弹窗
  try {
    await ElMessageBox.confirm(
      '确定要提交试卷吗？提交后将无法修改答案！',
      '提交确认',
      {
        confirmButtonText: '确定提交',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch (error) {
    // 用户取消提交
    return
  }
  
  try {
    const answerRecords = answers.value.map((answer, index) => {
      // 多选题：数组转逗号分隔的字符串
      let userAnswer = answer.userAnswer
      if (Array.isArray(userAnswer)) {
        userAnswer = userAnswer.sort().join(',')
      }
      
      // ❌ 禁止空卷自动输入答案！空就是空，不传空字符串
      if (!userAnswer || userAnswer === '') {
        userAnswer = ''
      }
      
      return {
        questionId: questions.value[index].id,
        userAnswer: userAnswer
      }
    })
    
    const response = await api.post(`/exam-records/submit/${recordId.value}`, answerRecords)
    if (response.code === 200) {
      ElMessage.success('提交成功')
      router.push('/exams')
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('提交失败')
  }
}
</script>

<style scoped>
.exam-answer-container {
  padding: 0;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.exam-card {
  margin: 0;
  height: 100vh;
  border-radius: 0;
  border: none;
  box-shadow: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  background-color: #ffffff;
  border-bottom: 1px solid #e4e7ed;
}

.exam-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.exam-info {
  display: flex;
  align-items: center;
}

.time-left {
  font-size: 18px;
  font-weight: bold;
  color: #F56C6C;
  transition: all 0.3s ease;
}

.time-warning {
  color: #E6A23C;
  animation: pulse 1s infinite;
}

.time-danger {
  color: #F56C6C;
  animation: pulse 0.5s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

.exam-content {
  display: flex;
  height: calc(100vh - 80px);
  overflow: hidden;
}

.questions-nav {
  width: 250px;
  padding: 20px;
  background-color: #fafafa;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
}

.questions-nav h3 {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #303133;
}

.question-numbers {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.question-number {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.question-number.unattempted {
  background-color: #ffffff;
  border: 1px solid #dcdfe6;
  color: #606266;
}

.question-number.unattempted:hover {
  border-color: #409EFF;
  color: #409EFF;
}

.question-number.attempted {
  background-color: #409EFF;
  border: 1px solid #409EFF;
  color: #ffffff;
}

.question-number.attempted:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.nav-legend {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 15px;
  border-top: 1px solid #e4e7ed;
}

.legend-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}

.legend-dot.unattempted {
  background-color: #ffffff;
  border: 1px solid #dcdfe6;
}

.legend-dot.attempted {
  background-color: #409EFF;
  border: 1px solid #409EFF;
}

.question-content {
  flex: 1;
  padding: 30px;
  background-color: #ffffff;
  overflow-y: auto;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.question-header h4 {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.question-score {
  font-size: 16px;
  font-weight: 500;
  color: #409EFF;
  background-color: #ecf5ff;
  padding: 4px 12px;
  border-radius: 12px;
}

.question-body {
  margin-bottom: 30px;
}

.question-text {
  margin-bottom: 25px;
  font-size: 16px;
  line-height: 1.6;
  color: #303133;
}

.question-options {
  margin-bottom: 30px;
}

.option-item {
  margin-bottom: 12px;
}

.option-label {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  margin-right: 10px;
  font-weight: bold;
  color: #409EFF;
}

.option-text {
  font-size: 15px;
  color: #303133;
  line-height: 1.5;
}

.judgment-options {
  display: flex;
  gap: 30px;
  margin-top: 10px;
}

.judgment-label {
  font-size: 15px;
  color: #303133;
}

.fill-blank-input,
.essay-input {
  width: 100%;
  font-size: 15px;
  line-height: 1.5;
}

.question-navigation {
  display: flex;
  justify-content: space-between;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.loading-icon {
  font-size: 48px;
  margin-bottom: 20px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 响应式布局 */
@media (max-width: 768px) {
  .exam-content {
    flex-direction: column;
  }
  
  .questions-nav {
    width: 100%;
    height: 150px;
    border-right: none;
    border-bottom: 1px solid #e4e7ed;
  }
  
  .question-numbers {
    justify-content: center;
  }
  
  .question-content {
    flex: 1;
    padding: 20px;
  }
}
</style>

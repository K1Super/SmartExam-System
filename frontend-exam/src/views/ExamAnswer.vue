<template>
  <div class="exam-answer-container">
    <el-card class="exam-card">
      <template #header>
        <div class="card-header">
          <span>{{ exam?.title }}</span>
          <div class="exam-info">
            <span class="time-left">剩余时间: {{ formatTime(remainingTime) }}</span>
            <el-button type="danger" @click="handleSubmit" style="margin-left: 20px;">提交试卷</el-button>
          </div>
        </div>
      </template>
      
      <div class="exam-content">
        <div class="questions-nav">
          <h3>题目导航</h3>
          <div class="question-numbers">
            <el-button 
              v-for="(question, index) in questions" 
              :key="question.id"
              :type="getQuestionStatus(index)"
              size="small"
              @click="currentQuestion = index"
              class="question-number"
            >
              {{ index + 1 }}
            </el-button>
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
                <el-radio-group v-model="answers[currentQuestion].userAnswer">
                  <el-radio v-for="(option, key) in parseOptions(questions[currentQuestion].options)" :key="key" :label="key">
                    {{ key }}. {{ option }}
                  </el-radio>
                </el-radio-group>
              </div>
              
              <!-- 多选题 -->
              <div v-else-if="questions[currentQuestion].type === 2" class="question-options">
                <el-checkbox-group v-model="answers[currentQuestion].userAnswer">
                  <el-checkbox v-for="(option, key) in parseOptions(questions[currentQuestion].options)" :key="key" :label="key">
                    {{ key }}. {{ option }}
                  </el-checkbox>
                </el-checkbox-group>
              </div>
              
              <!-- 判断题 -->
              <div v-else-if="questions[currentQuestion].type === 3" class="question-options">
                <el-radio-group v-model="answers[currentQuestion].userAnswer">
                  <el-radio label="正确">正确</el-radio>
                  <el-radio label="错误">错误</el-radio>
                </el-radio-group>
              </div>
              
              <!-- 填空题 -->
              <div v-else-if="questions[currentQuestion].type === 4" class="question-options">
                <el-input 
                  v-model="answers[currentQuestion].userAnswer" 
                  type="textarea" 
                  placeholder="请输入答案"
                  :rows="3"
                ></el-input>
              </div>
              
              <!-- 简答题 -->
              <div v-else-if="questions[currentQuestion].type === 5" class="question-options">
                <el-input 
                  v-model="answers[currentQuestion].userAnswer" 
                  type="textarea" 
                  placeholder="请输入答案"
                  :rows="5"
                ></el-input>
              </div>
            </div>
            
            <div class="question-navigation">
              <el-button @click="prevQuestion" :disabled="currentQuestion === 0">上一题</el-button>
              <el-button @click="nextQuestion" :disabled="currentQuestion === questions.length - 1">下一题</el-button>
            </div>
          </div>
          <div v-else class="loading">
            正在加载题目...
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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
    const response = await api.get('/questions')
    if (response.code === 200) {
      questions.value = response.data
      // 初始化答案数组
      answers.value = questions.value.map(() => ({ userAnswer: '' }))
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
    return JSON.parse(optionsStr)
  } catch (error) {
    return {}
  }
}

const getQuestionStatus = (index) => {
  if (!answers.value[index]) return ''
  return answers.value[index].userAnswer ? 'primary' : ''
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
  try {
    const answerRecords = answers.value.map((answer, index) => ({
      questionId: questions.value[index].id,
      userAnswer: answer.userAnswer
    }))
    
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
  padding: 20px;
}

.exam-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.exam-info {
  display: flex;
  align-items: center;
}

.time-left {
  font-size: 16px;
  font-weight: bold;
  color: #F56C6C;
}

.exam-content {
  display: flex;
  margin-top: 20px;
}

.questions-nav {
  width: 200px;
  padding-right: 20px;
  border-right: 1px solid #e4e7ed;
}

.question-numbers {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.question-number {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.question-content {
  flex: 1;
  padding-left: 30px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.question-text {
  margin-bottom: 20px;
  font-size: 16px;
  line-height: 1.5;
}

.question-options {
  margin-bottom: 30px;
}

.question-options .el-radio,
.question-options .el-checkbox {
  display: block;
  margin-bottom: 10px;
}

.question-navigation {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.loading {
  text-align: center;
  padding: 100px 0;
  color: #909399;
}
</style>

<template>
  <div class="exam-result-container">
    <el-card class="result-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="exam-title">{{ examRecord?.examTitle || '考试结果' }}</span>
            <el-tag v-if="examRecord?.status === 3" type="success">已批改</el-tag>
            <el-tag v-else-if="examRecord?.status === 2" type="info">已提交</el-tag>
          </div>
          <div class="header-right">
            <span class="score-info" v-if="examRecord?.score !== null && examRecord?.score !== undefined">
              总得分: <span class="score">{{ examRecord.score }}</span> 分
            </span>
            <el-button @click="goBack">返回</el-button>
          </div>
        </div>
      </template>

      <div class="questions-list">
        <div 
          v-for="(item, index) in questionAnswers" 
          :key="item.question.id"
          class="question-item"
          :class="{ 'correct': item.isCorrect === 1, 'incorrect': item.isCorrect === 0 }"
        >
          <div class="question-header">
            <span class="question-number">第 {{ index + 1 }} 题</span>
            <span class="question-type">({{ getQuestionType(item.question.type) }})</span>
            <span class="question-score">分值: {{ item.question.score }} 分</span>
            <el-tag v-if="item.isCorrect === 1" type="success" size="small">正确</el-tag>
            <el-tag v-else-if="item.isCorrect === 0" type="danger" size="small">错误</el-tag>
            <el-tag v-else type="info" size="small">待批改</el-tag>
          </div>

          <div class="question-content" v-html="item.question.content"></div>

          <!-- 选项展示 -->
          <div class="options-section" v-if="item.question.type === 1 || item.question.type === 2">
            <div class="option-list">
              <div 
                v-for="(option, optIndex) in parseOptions(item.question.options)" 
                :key="optIndex"
                class="option-item"
                :class="{
                  'user-selected': isOptionSelected(item, optIndex),
                  'correct-answer': isCorrectOption(item, optIndex)
                }"
              >
                <span class="option-label">{{ String.fromCharCode(65 + optIndex) }}.</span>
                <span class="option-text">{{ option }}</span>
                <el-icon v-if="isOptionSelected(item, optIndex)" class="user-icon"><User /></el-icon>
                <el-icon v-if="isCorrectOption(item, optIndex)" class="correct-icon"><CircleCheck /></el-icon>
              </div>
            </div>
          </div>

          <!-- 判断题展示 -->
          <div class="judgment-section" v-else-if="item.question.type === 3">
            <div class="judgment-options">
              <el-radio-group :model-value="item.answer?.userAnswer" disabled>
                <el-radio label="正确">正确</el-radio>
                <el-radio label="错误">错误</el-radio>
              </el-radio-group>
            </div>
          </div>

          <!-- 主观题展示 -->
          <div class="subjective-section" v-else>
            <div class="answer-label">学生答案:</div>
            <div class="user-answer">{{ item.answer?.userAnswer || '未作答' }}</div>
          </div>

          <!-- 正确答案展示 -->
          <div class="correct-answer-section" v-if="item.question.answer && item.question.type !== 5">
            <div class="answer-label">正确答案:</div>
            <div class="correct-answer-text">{{ formatCorrectAnswer(item.question.answer, item.question.type) }}</div>
          </div>

          <!-- 答案解析 -->
          <div class="analysis-section" v-if="item.question.analysis">
            <div class="answer-label">答案解析:</div>
            <div class="analysis-text" v-html="item.question.analysis"></div>
          </div>

          <!-- 得分展示/编辑 -->
          <div class="score-section">
            <div class="score-display" v-if="item.answer?.score !== null && item.answer?.score !== undefined">
              得分: <span class="score-value">{{ item.answer.score }}</span> / {{ item.question.score }} 分
            </div>
            <div class="score-edit" v-else-if="isTeacher && item.question.type === 5">
              <el-input-number 
                v-model="item.tempScore" 
                :min="0" 
                :max="item.question.score"
                :precision="1"
                size="small"
              />
              <span class="score-max">/ {{ item.question.score }} 分</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 教师批改按钮 -->
      <div class="grading-actions" v-if="isTeacher && hasUngradedQuestions">
        <el-button type="primary" @click="submitGrading">提交批改</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, CircleCheck } from '@element-plus/icons-vue'
import api from '../api'

const route = useRoute()
const router = useRouter()
const recordId = route.params.recordId

const examRecord = ref(null)
const questionAnswers = ref([])
const isTeacher = ref(false)

onMounted(() => {
  checkUserRole()
  fetchExamResult()
})

const checkUserRole = () => {
  const user = JSON.parse(localStorage.getItem('user'))
  if (user) {
    isTeacher.value = user.role === 2 || user.role === 1 // 教师或管理员
  }
}

const fetchExamResult = async () => {
  try {
    // 获取考试记录 - 接口已经直接返回 QuestionAnswerVO 包含 question + answer
    const recordResponse = await api.get(`/exam-records/answers/${recordId}`)
    if (recordResponse.code === 200) {
      // 获取答题记录（已包含题目数据）
      questionAnswers.value = recordResponse.data.sort((a, b) => a.question.id - b.question.id)
      
      // 获取考试记录详情
      const examRecordResponse = await api.get(`/exam-records`)
      if (examRecordResponse.code === 200) {
        examRecord.value = examRecordResponse.data.find(r => r.id === parseInt(recordId))
      }
    }
  } catch (error) {
    console.error('获取考试结果失败', error)
    ElMessage.error('获取考试结果失败')
  }
}

const getQuestionType = (type) => {
  const types = ['', '单选题', '多选题', '判断题', '填空题', '简答题']
  return types[type] || '未知题型'
}

const parseOptions = (optionsStr) => {
  if (!optionsStr) return ['选项A', '选项B', '选项C', '选项D']
  if (typeof optionsStr === 'string') {
    const arr = optionsStr.split('|').filter(Boolean)
    return arr.length >= 4 ? arr : ['选项A', '选项B', '选项C', '选项D']
  }
  return Array.isArray(optionsStr) ? optionsStr : ['选项A', '选项B', '选项C', '选项D']
}

const isOptionSelected = (item, index) => {
  if (!item.answer?.userAnswer) return false
  const optionLetter = String.fromCharCode(65 + index)
  if (item.question.type === 2) {
    // 多选题
    return item.answer.userAnswer.includes(optionLetter)
  }
  return item.answer.userAnswer === optionLetter
}

const isCorrectOption = (item, index) => {
  if (!item.question.answer) return false
  const optionLetter = String.fromCharCode(65 + index)
  if (item.question.type === 2) {
    // 多选题
    return item.question.answer.includes(optionLetter)
  }
  return item.question.answer === optionLetter
}

const formatCorrectAnswer = (answer, type) => {
  if (type === 3) {
    // 处理判断题多种答案格式（字符串、布尔、数字）
    return answer === 'true' || answer === '1' || answer === true || answer === 1 ? '正确' : '错误'
  }
  return answer
}

const hasUngradedQuestions = computed(() => {
  return questionAnswers.value.some(item => 
    item.question.type === 5 && (item.answer?.score === null || item.answer?.score === undefined)
  )
})

const submitGrading = async () => {
  try {
    let totalScore = 0
    
    for (const item of questionAnswers.value) {
      if (item.question.type === 5 && item.tempScore !== undefined) {
        // 更新主观题分数
        await api.post(`/answer-records/update-score/${item.answer.id}`, {
          score: item.tempScore,
          isCorrect: item.tempScore > 0 ? 1 : 0
        })
        totalScore += item.tempScore
      } else if (item.answer?.score) {
        totalScore += item.answer.score
      }
    }
    
    // 更新考试记录总分
    await api.post(`/exam-records/update-score/${recordId}`, {
      score: totalScore
    })
    
    ElMessage.success('批改提交成功')
    fetchExamResult() // 刷新数据
  } catch (error) {
    ElMessage.error('提交批改失败')
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.exam-result-container {
  padding: 20px;
}

.result-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.exam-title {
  font-size: 18px;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.score-info {
  font-size: 16px;
}

.score {
  font-size: 24px;
  font-weight: bold;
  color: #67c23a;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  background-color: #fafafa;
}

.question-item.correct {
  border-left: 4px solid #67c23a;
}

.question-item.incorrect {
  border-left: 4px solid #f56c6c;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.question-number {
  font-weight: bold;
  font-size: 16px;
}

.question-type {
  color: #909399;
}

.question-score {
  color: #e6a23c;
  font-weight: bold;
}

.question-content {
  margin-bottom: 15px;
  line-height: 1.6;
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-radius: 4px;
  background-color: #fff;
  border: 1px solid #dcdfe6;
}

.option-item.user-selected {
  background-color: #ecf5ff;
  border-color: #409eff;
}

.option-item.correct-answer {
  background-color: #f0f9ff;
  border-color: #67c23a;
}

.option-item.user-selected.correct-answer {
  background-color: #f0f9eb;
  border-color: #67c23a;
}

.option-label {
  font-weight: bold;
  margin-right: 8px;
  min-width: 25px;
}

.option-text {
  flex: 1;
}

.user-icon {
  color: #409eff;
  margin-left: 8px;
}

.correct-icon {
  color: #67c23a;
  margin-left: 8px;
}

.judgment-section {
  margin: 15px 0;
}

.subjective-section {
  margin: 15px 0;
}

.answer-label {
  font-weight: bold;
  margin-bottom: 8px;
  color: #606266;
}

.user-answer {
  padding: 12px;
  background-color: #fff;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  line-height: 1.6;
  min-height: 60px;
}

.correct-answer-section {
  margin-top: 15px;
  padding: 12px;
  background-color: #f0f9eb;
  border-radius: 4px;
}

.correct-answer-text {
  color: #67c23a;
  font-weight: bold;
}

.analysis-section {
  margin-top: 15px;
  padding: 12px;
  background-color: #fdf6ec;
  border-radius: 4px;
}

.analysis-text {
  line-height: 1.6;
}

.score-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #dcdfe6;
  display: flex;
  align-items: center;
  gap: 10px;
}

.score-value {
  font-size: 18px;
  font-weight: bold;
  color: #67c23a;
}

.score-edit {
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-max {
  color: #909399;
}

.grading-actions {
  margin-top: 30px;
  text-align: center;
  padding: 20px;
  border-top: 1px solid #e4e7ed;
}
</style>

<template>
  <div class="manual-grading-container">
    <el-card class="manual-grading-card">
      <el-table :data="examRecords" style="width: 100%">
        <el-table-column prop="recordId" label="记录ID" width="100"></el-table-column>
        <el-table-column prop="examTitle" label="考试标题"></el-table-column>
        <el-table-column prop="userName" label="学生姓名" width="120"></el-table-column>
        <el-table-column prop="clazzName" label="班级" width="150"></el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 2" type="info">已提交</el-tag>
            <el-tag v-else-if="scope.row.status === 3" type="success">已批改</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="success" @click="handleGrade(scope.row)" :disabled="scope.row.status === 3">
              批改
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      width="90%"
      top="3vh"
      :close-on-click-modal="false"
    >
      <template #header>
        <div class="dialog-header">
          <div class="header-left">
            <span class="dialog-title-text">批改试卷</span>
            <span class="header-student">「{{ currentExamRecord?.examTitle }}」</span>
          </div>
        </div>
      </template>

      <div class="grading-empty" v-if="questionAnswers.length === 0">
        <el-empty description="加载中..." />
      </div>

      <div class="grading-container" v-if="questionAnswers.length > 0">
        <div class="grading-sidebar">
          <div class="student-info-card">
            <div class="info-row">
              <span class="label">学生</span>
              <span class="value">{{ currentExamRecord?.userName }}</span>
            </div>
            <div class="info-row">
              <span class="label">班级</span>
              <span class="value">{{ currentExamRecord?.clazzName }}</span>
            </div>
            <div class="info-row total">
              <span class="label">总分</span>
              <span class="value total-score">{{ calculateTotalScore }}</span>
            </div>
          </div>
          
          <div class="nav-title">题目导航</div>
          <div class="nav-grid">
            <div 
              v-for="(item, index) in questionAnswers" 
              :key="index"
              :class="['nav-item', navItemClass(item, index)]"
              @click="currentQuestion = index"
            >
              {{ index + 1 }}
            </div>
          </div>
        </div>
        
        <div class="grading-main">
          <div class="question-card">
            <div class="q-header">
              <span class="q-type">{{ questionTypeText }}</span>
              <span class="q-score">{{ questionScore }} 分</span>
            </div>
            
            <div class="q-content" v-html="questionContent"></div>
            
            <div class="q-options" v-if="currentQ.type === 1 || currentQ.type === 2">
              <div 
                v-for="(option, index) in optionList"
                :key="index"
                class="q-option"
                :class="optionClass(index)"
              >
                <span class="opt-letter">{{ optLetter(index) }}</span>
                <span class="opt-text">{{ option }}</span>
              </div>
            </div>
            
            <div class="q-options" v-else-if="currentQ.type === 3">
              <div class="judge-row">
                <div class="judge-item" :class="judgeClass('true')">
                  <span class="judge-icon">✓</span>
                  正确
                </div>
                <div class="judge-item" :class="judgeClass('false')">
                  <span class="judge-icon">✕</span>
                  错误
                </div>
              </div>
            </div>
            
            <div class="answer-section" v-else>
              <div class="ans-block">
                <div class="ans-title">学生答案</div>
                <div class="ans-content">{{ studentAnswerText }}</div>
              </div>
            </div>
            
            <div class="correct-ans-section">
              <span class="ans-label">参考答案</span>
              <span class="ans-text">{{ correctAnswerText }}</span>
            </div>
            
            <div class="score-area" v-if="isGradingMode && currentQ.type >= 4">
              <span class="score-label">本题得分</span>
              <el-input-number 
                v-model="currentScore"
                :min="0"
                :max="maxScore"
                :precision="0"
                size="large"
                :controls="false"
                class="score-input"
              />
              <span class="score-max">/ {{ maxScore }}</span>
            </div>
            
            <div class="auto-grade-score" v-if="currentQ.type <= 3">
              <span class="auto-label">自动判分</span>
              <span class="auto-score">{{ questionAnswers[currentQuestion]?.answer?.score || 0 }}</span>
              <span class="auto-max"> / {{ maxScore }} 分</span>
            </div>
          </div>
          
          <div class="q-nav-buttons">
            <el-button @click="prevQuestion" :disabled="currentQuestion === 0" class="nav-btn">
              ← 上一题
            </el-button>
            <span class="q-progress">{{ currentQuestion + 1 }} / {{ questionAnswers.length }}</span>
            <el-button @click="nextQuestion" :disabled="currentQuestion === questionAnswers.length - 1" class="nav-btn">
              下一题 →
            </el-button>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <div></div>
          <el-button size="large" type="primary" @click="submitGrading" :loading="submitting" class="submit-btn">
            确认提交 最终得分 {{ calculateTotalScore }} 分
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const examRecords = ref([])
const dialogVisible = ref(false)
const isGradingMode = ref(true)
const submitting = ref(false)
const currentExamRecord = ref(null)
const questionAnswers = ref([])
const currentQuestion = ref(0)

const currentQ = computed(() => questionAnswers.value[currentQuestion.value]?.question || {})
const currentA = computed(() => questionAnswers.value[currentQuestion.value]?.answer || {})

const questionTypeText = computed(() => {
  const map = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题' }
  return map[currentQ.value.type] || '未知'
})

const questionScore = computed(() => Number(currentQ.value.score) || 0)
const maxScore = computed(() => Number(currentQ.value.score) || 100)
const questionContent = computed(() => currentQ.value.content || '')
const correctAnswerText = computed(() => currentQ.value.answer || '无')
const studentAnswerText = computed(() => currentA.value.userAnswer || '未作答')

const currentScore = computed({
  get: () => questionAnswers.value[currentQuestion.value]?.answer?.score || 0,
  set: (val) => {
    if (questionAnswers.value[currentQuestion.value]) {
      questionAnswers.value[currentQuestion.value].answer.score = val
    }
  }
})

const optionList = computed(() => {
  const options = currentQ.value.options
  if (!options) return ['选项A', '选项B', '选项C', '选项D']
  if (typeof options === 'string') {
    // 支持多种分隔符
    let arr = []
    if (options.includes('|')) {
      arr = options.split('|').filter(Boolean)
    } else if (options.includes(',')) {
      arr = options.split(',').filter(Boolean)
    } else if (options.includes(';')) {
      arr = options.split(';').filter(Boolean)
    } else {
      // 处理 A.xxxB.xxxC.xxx 格式
      arr = options.split(/(?=[A-D]\.)/).filter(Boolean)
    }
    // 去掉 A. B. 前缀
    arr = arr.map(opt => opt.replace(/^[A-D]\.?\s*/, '').trim())
    return arr.length >= 4 ? arr : ['选项A', '选项B', '选项C', '选项D']
  }
  return Array.isArray(options) ? options : ['选项A', '选项B', '选项C', '选项D']
})

const optLetter = (index) => String.fromCharCode(65 + index)

const optionClass = (index) => {
  const letter = optLetter(index)
  const correctAnswer = (currentQ.value.answer || '').toUpperCase().trim()
  const userAnswer = (currentA.value.userAnswer || '').toUpperCase().trim()
  
  // 智能分割：有逗号按逗号分，没逗号按单个字符分
  const splitAnswer = (str) => {
    if (str.includes(',') || str.includes('，')) {
      return str.split(/[,，]/).filter(Boolean)
    }
    // 没有逗号就是ABCD连在一起的形式，按单个字符分割
    return str.split('').filter(c => /[A-D]/.test(c))
  }
  
  const correctSet = new Set(splitAnswer(correctAnswer))
  const userSet = new Set(splitAnswer(userAnswer))
  
  const isCorrectAns = correctSet.has(letter)
  const isSelected = userSet.has(letter)
  

  
  // 只在学生选择的选项上显示颜色
  if (isSelected) {
    return isCorrectAns ? 'correct' : 'wrong'
  }
  return ''
}

const normalizeJudge = (v) => {
  if (!v) return ''
  const val = String(v).trim().toLowerCase()
  if (val === 'true' || val === '1' || val === '正确' || val === '对') return 'true'
  if (val === 'false' || val === '0' || val === '错误' || val === '错') return 'false'
  return val
}

const judgeClass = (val) => {
  // 归一化标准答案和学生答案格式
  const correctAns = normalizeJudge(currentQ.value.answer)
  const userAns = normalizeJudge(currentA.value.userAnswer)
  
  const isCorrectAns = correctAns === val
  const isSelected = userAns === val
  

  // 只在学生选择的选项上显示颜色
  if (isSelected) {
    return isCorrectAns ? 'correct' : 'wrong'
  }
  return ''
}

const navItemClass = (item, index) => {
  const classes = []
  if (index === currentQuestion.value) classes.push('active')
  if (item.answer?.score !== undefined && item.answer?.score !== null) classes.push('scored')
  return classes.join(' ')
}

const prevQuestion = () => currentQuestion.value > 0 && currentQuestion.value--
const nextQuestion = () => currentQuestion.value < questionAnswers.value.length - 1 && currentQuestion.value++

onMounted(() => fetchExamRecords())

const fetchExamRecords = async () => {
  try {
    const res = await api.get('/exam-records?type=manual')
    if (res.code === 200) examRecords.value = res.data
  } catch (e) {}
}

const formatDate = (str) => str ? new Date(str).toLocaleString('zh-CN') : '-'

const calculateTotalScore = computed(() => {
  return questionAnswers.value.reduce((sum, item) => {
    return sum + Number(item.answer?.score || 0)
  }, 0)
})

const handleGrade = async (row) => {
  currentExamRecord.value = row
  currentQuestion.value = 0
  dialogVisible.value = true
  await loadQuestionData(row)
}

const loadQuestionData = async (row) => {
  try {
    const recordId = row.id || row.recordId
    
    await api.post(`/grading/auto/${recordId}`)
    
    const res = await api.get(`/exam-records/answers/${recordId}`)
    if (res.code === 200) {
      questionAnswers.value = res.data.map(item => {
        const rawScore = item.answer?.score
        const scoreNum = Number(rawScore) || 0
        
        return {
          question: item.question,
          answer: { 
            ...item.answer, 
            score: scoreNum
          }
        }
      })
    }
  } catch (err) {
    ElMessage.error('加载失败: ' + (err.message || '未知错误'))
  }
}

const submitGrading = async () => {
  submitting.value = true
  try {
    const recordId = currentExamRecord.value.id || currentExamRecord.value.recordId
    
    for (const item of questionAnswers.value) {
      if (item.question.type >= 4) {
        await api.post(`/answer-records/update-score/${item.answer.id}`, {
          score: Number(item.answer.score || 0),
          isCorrect: Number(item.answer.score) > 0 ? 1 : 0
        })
      }
    }
    
    await api.post(`/exam-records/update-score/${recordId}`, {
      score: calculateTotalScore.value
    })
    
    ElMessage.success('批改完成，分数已保存')
    dialogVisible.value = false
    await fetchExamRecords()
  } catch (e) {
    ElMessage.error('提交失败: ' + (e.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除？', '警告', { type: 'warning' })
    await api.delete(`/exam-records/${row.id || row.recordId}`)
    ElMessage.success('删除成功')
    await fetchExamRecords()
  } catch (e) {}
}
</script>

<style scoped>
.manual-grading-container {
  padding: 24px;
}

:deep(.manual-grading-card) {
  border: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

:deep(.el-dialog__header) {
  padding: 0;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dialog-title-text {
  font-size: 18px;
  font-weight: 600;
}

.header-student {
  font-size: 14px;
  color: #909399;
}

:deep(.el-dialog__body) {
  padding: 0;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.grading-container {
  display: flex;
  min-height: 70vh;
}

.grading-sidebar {
  width: 260px;
  background: #fafafa;
  border-right: 1px solid #f0f0f0;
  padding: 24px;
}

.student-info-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-row:last-child {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #f0f0f0;
  margin-bottom: 0;
}

.info-row .label {
  color: #909399;
}

.info-row .value {
  font-weight: 500;
}

.info-row .total-score {
  color: #1a1a1a;
  font-size: 20px;
  font-weight: 600;
}

.nav-title {
  font-size: 13px;
  font-weight: 500;
  color: #8c8c8c;
  margin-bottom: 16px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  justify-items: center;
}

.nav-item {
  height: 44px;
  width: 44px;
  line-height: 44px;
  text-align: center;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 500;
  color: #595959;
}

.nav-item.active {
  border-color: #1a1a1a;
  background: #1a1a1a;
  color: #fff;
  font-weight: 600;
}

.nav-item.scored {
  border-color: #1a1a1a;
  font-weight: 600;
}

.grading-main {
  flex: 1;
  padding: 24px 32px;
  background: #fff;
  overflow-y: auto;
}

.question-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 28px;
  margin-bottom: 20px;
}

.q-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.q-type {
  padding: 3px 10px;
  background: #f5f5f5;
  color: #595959;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.q-score {
  color: #1a1a1a;
  font-weight: 600;
  font-size: 15px;
}

.q-content {
  font-size: 16px;
  line-height: 1.8;
  margin-bottom: 24px;
  color: #303133;
}

.q-options {
  margin-bottom: 24px;
}

.q-option {
  display: flex;
  align-items: center;
  padding: 14px 18px;
  margin-bottom: 12px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.2s;
}

.q-option.correct {
  border-color: #67c23a;
  background: #f0f9eb;
}

.q-option.correct .opt-letter {
  background: #67c23a;
  color: #fff;
}

.q-option.wrong {
  border-color: #f56c6c;
  background: #fef0f0;
  opacity: 1;
}

.q-option.wrong .opt-letter {
  background: #f56c6c;
  color: #fff;
  text-decoration: line-through;
}

.opt-letter {
  width: 26px;
  height: 26px;
  line-height: 26px;
  text-align: center;
  border-radius: 4px;
  background: #f0f0f0;
  font-weight: 600;
  margin-right: 14px;
  font-size: 13px;
  color: #595959;
}

.opt-text {
  flex: 1;
  font-size: 14px;
}

.judge-row {
  display: flex;
  gap: 20px;
}

.judge-item {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 12px 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  gap: 10px;
}

.judge-item.correct {
  border-color: #67c23a;
  background: #f0f9eb;
  color: #67c23a;
  font-weight: 600;
}

.judge-item.wrong {
  border-color: #f56c6c;
  background: #fef0f0;
  color: #f56c6c;
  opacity: 1;
  text-decoration: line-through;
}

.judge-icon {
  font-weight: bold;
  font-size: 18px;
}

.ans-block {
  margin-bottom: 20px;
}

.ans-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.ans-content {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  line-height: 1.6;
  min-height: 40px;
}

.correct-ans-section {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  margin-bottom: 24px;
  gap: 12px;
}

.ans-label {
  color: #595959;
  font-weight: 500;
  font-size: 13px;
}

.ans-text {
  font-weight: 600;
  color: #1a1a1a;
  font-size: 15px;
}

.score-area {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  gap: 12px;
}

.score-label {
  font-size: 14px;
  color: #606266;
}

.score-max {
  color: #909399;
  margin-left: 8px;
}

:deep(.score-input .el-input__inner) {
  width: 100px !important;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
}

.auto-grade-score {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
  gap: 8px;
}

.auto-label {
  font-size: 14px;
  color: #8c8c8c;
}

.auto-score {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.auto-max {
  font-size: 14px;
  color: #8c8c8c;
}

.q-nav-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
}

.nav-btn {
  width: 120px;
}

.q-progress {
  font-size: 14px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  width: 100%;
}

.submit-btn {
  padding-left: 40px;
  padding-right: 40px;
}

.grading-empty {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
}
</style>

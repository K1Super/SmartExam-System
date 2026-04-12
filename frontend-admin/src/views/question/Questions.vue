<template>
  <div class="questions-container">
    <el-card class="questions-card">
      <div class="search-toolbar">
        <div class="search-left">
          <el-input v-model="searchKeyword" placeholder="请输入题目内容" style="width: 300px; margin-right: 10px;"></el-input>
          <el-select v-model="typeFilter" placeholder="选择题型" style="width: 150px; margin-right: 10px;">
            <el-option label="全部" value=""></el-option>
            <el-option label="单选题" value="1"></el-option>
            <el-option label="多选题" value="2"></el-option>
            <el-option label="判断题" value="3"></el-option>
            <el-option label="填空题" value="4"></el-option>
            <el-option label="简答题" value="5"></el-option>
          </el-select>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
        <div class="search-right">
          <el-button type="primary" @click="handleAddQuestion">添加题目</el-button>
        </div>
      </div>
      <el-table :data="questionsList" style="width: 100%">
        <el-table-column type="index" label="序号" width="80"></el-table-column>
        <el-table-column prop="content" label="题目内容" show-overflow-tooltip></el-table-column>
        <el-table-column prop="type" label="题型">
          <template #default="scope">
            <span v-if="scope.row.type === 1">单选题</span>
            <span v-else-if="scope.row.type === 2">多选题</span>
            <span v-else-if="scope.row.type === 3">判断题</span>
            <span v-else-if="scope.row.type === 4">填空题</span>
            <span v-else>简答题</span>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度">
          <template #default="scope">
            <span v-if="scope.row.difficulty === 1">简单</span>
            <span v-else-if="scope.row.difficulty === 2">中等</span>
            <span v-else>困难</span>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditQuestion(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteQuestion(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑题目对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
    >
      <el-form :model="questionForm" :rules="rules" ref="questionFormRef" label-width="100px">
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="questionForm.content" type="textarea" :rows="3" placeholder="请输入题目内容"></el-input>
        </el-form-item>
        <el-form-item label="题型" prop="type">
          <el-select v-model="questionForm.type" placeholder="请选择题型" @change="handleTypeChange">
            <el-option label="单选题" :value="1"></el-option>
            <el-option label="多选题" :value="2"></el-option>
            <el-option label="判断题" :value="3"></el-option>
            <el-option label="填空题" :value="4"></el-option>
            <el-option label="简答题" :value="5"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="questionForm.difficulty" placeholder="请选择难度">
            <el-option label="简单" :value="1"></el-option>
            <el-option label="中等" :value="2"></el-option>
            <el-option label="困难" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="分值" prop="score">
          <el-input v-model.number="questionForm.score" type="number" placeholder="请输入分值"></el-input>
        </el-form-item>
        
        <!-- 单选题和多选题的选项 -->
        <el-form-item label="选项" v-if="[1, 2].includes(questionForm.type)">
          <div v-for="(option, index) in questionForm.options" :key="index" class="option-item">
            <span class="option-label">{{ String.fromCharCode(65 + index) }}:</span>
            <el-input v-model="questionForm.options[index]" placeholder="请输入选项" style="flex: 1; margin: 0 12px;"></el-input>
            <el-button 
              :type="questionForm.answer.includes(String.fromCharCode(65 + index)) ? 'primary' : 'default'"
              size="small"
              @click="toggleAnswer(index)"
              :class="{ 'is-correct': questionForm.answer.includes(String.fromCharCode(65 + index)) }"
            >
              {{ questionForm.answer.includes(String.fromCharCode(65 + index)) ? '✓ 正确答案' : '正确答案' }}
            </el-button>
          </div>
        </el-form-item>
        
        <!-- 判断题的选项 -->
        <el-form-item label="选项" v-if="questionForm.type === 3">
          <div class="option-item">
            <span class="option-label">对:</span>
            <el-input v-model="questionForm.options[0]" placeholder="请输入对的描述" style="flex: 1; margin: 0 12px;"></el-input>
            <el-button 
              :type="questionForm.answer === '对' ? 'primary' : 'default'"
              size="small"
              @click="questionForm.answer = '对'"
              :class="{ 'is-correct': questionForm.answer === '对' }"
            >
              {{ questionForm.answer === '对' ? '✓ 正确答案' : '正确答案' }}
            </el-button>
          </div>
          <div class="option-item">
            <span class="option-label">错:</span>
            <el-input v-model="questionForm.options[1]" placeholder="请输入错的描述" style="flex: 1; margin: 0 12px;"></el-input>
            <el-button 
              :type="questionForm.answer === '错' ? 'primary' : 'default'"
              size="small"
              @click="questionForm.answer = '错'"
              :class="{ 'is-correct': questionForm.answer === '错' }"
            >
              {{ questionForm.answer === '错' ? '✓ 正确答案' : '正确答案' }}
            </el-button>
          </div>
        </el-form-item>
        
        <!-- 填空题和简答题只需要正确答案和解析 -->
        <el-form-item label="正确答案" prop="answer" v-if="[4, 5].includes(questionForm.type)">
          <el-input 
            v-model="questionForm.answer" 
            placeholder="请输入正确答案"
            :type="questionForm.type === 5 ? 'textarea' : 'text'"
            :rows="questionForm.type === 5 ? 4 : 1"
            style="width: 100%;"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="解析" prop="analysis">
          <el-input v-model="questionForm.analysis" type="textarea" :rows="2" placeholder="请输入解析"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const questionsList = ref([])
const searchKeyword = ref('')
const typeFilter = ref('')

const dialogVisible = ref(false)
const dialogTitle = ref('添加题目')
const questionFormRef = ref(null)
const questionForm = ref({
  id: null,
  content: '',
  type: 1,
  difficulty: 2, // 默认中等难度
  score: 2, // 默认2分
  options: ['', '', '', ''], // 默认ABCD四个选项
  answer: '',
  analysis: ''
})

const rules = {
  content: [
    { required: true, message: '请输入题目内容', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择题型', trigger: 'change' }
  ],
  difficulty: [
    { required: true, message: '请选择难度', trigger: 'change' }
  ],
  score: [
    { required: true, message: '请输入分值', trigger: 'blur' },
    { type: 'number', min: 1, message: '分值至少为1', trigger: 'blur' }
  ],
  answer: [
    { required: true, message: '请输入正确答案', trigger: 'blur' }
  ]
}

onMounted(() => {
  fetchQuestions()
})

const fetchQuestions = async () => {
  try {
    const response = await api.get('/questions')
    if (response.code === 200) {
      questionsList.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取题目列表失败')
  }
}

const handleSearch = () => {
  fetchQuestions()
}

const handleTypeChange = () => {
  const currentOptions = [...questionForm.value.options]
  // 根据题型调整选项
  if ([1, 2].includes(questionForm.value.type)) {
    // 单选题和多选题，默认ABCD四个选项
    const newOptions = ['', '', '', '']
    // 保留用户已输入的内容
    for (let i = 0; i < currentOptions.length && i < newOptions.length; i++) {
      if (currentOptions[i]) {
        newOptions[i] = currentOptions[i]
      }
    }
    questionForm.value.options = newOptions
  } else if (questionForm.value.type === 3) {
    // 判断题，默认对和错两个选项
    const newOptions = ['', '']
    // 保留用户已输入的内容
    for (let i = 0; i < currentOptions.length && i < newOptions.length; i++) {
      if (currentOptions[i]) {
        newOptions[i] = currentOptions[i]
      }
    }
    questionForm.value.options = newOptions
  } else {
    // 填空题和简答题，不需要选项
    questionForm.value.options = []
  }
  // 清空答案
  questionForm.value.answer = ''
}

const toggleAnswer = (index) => {
  const optionLabel = String.fromCharCode(65 + index)
  if (questionForm.value.type === 1) {
    // 单选题，只能选择一个答案
    questionForm.value.answer = optionLabel
  } else if (questionForm.value.type === 2) {
    // 多选题，可以选择多个答案
    if (questionForm.value.answer.includes(optionLabel)) {
      // 取消选择
      questionForm.value.answer = questionForm.value.answer.replace(optionLabel, '')
    } else {
      // 添加选择
      questionForm.value.answer += optionLabel
    }
  }
}

const handleAddQuestion = () => {
  dialogTitle.value = '添加题目'
  questionForm.value = {
    id: null,
    content: '',
    type: 1,
    difficulty: 2, // 默认中等难度
    score: 2, // 默认2分
    options: ['', '', '', ''], // 默认ABCD四个选项
    answer: '',
    analysis: ''
  }
  dialogVisible.value = true
}

const handleEditQuestion = (question) => {
  dialogTitle.value = '编辑题目'
  questionForm.value = { ...question }
  // 处理options字段，确保它是数组
  if (typeof questionForm.value.options === 'string') {
    try {
      questionForm.value.options = JSON.parse(questionForm.value.options)
    } catch (e) {
      questionForm.value.options = []
    }
  }
  if (!questionForm.value.options) {
    questionForm.value.options = []
  }
  // 确保选项数量正确，最多4个选项（ABCD）
  if ([1, 2].includes(questionForm.value.type)) {
    // 限制选项数量为4个
    if (questionForm.value.options.length > 4) {
      questionForm.value.options = questionForm.value.options.slice(0, 4)
    } else if (questionForm.value.options.length < 4) {
      while (questionForm.value.options.length < 4) {
        questionForm.value.options.push('')
      }
    }
  } else if (questionForm.value.type === 3) {
    // 判断题限制为2个选项
    if (questionForm.value.options.length > 2) {
      questionForm.value.options = questionForm.value.options.slice(0, 2)
    } else if (questionForm.value.options.length < 2) {
      while (questionForm.value.options.length < 2) {
        questionForm.value.options.push('')
      }
    }
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!questionFormRef.value) return
  
  await questionFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 准备提交数据
        const submitData = {
          ...questionForm.value,
          options: JSON.stringify(questionForm.value.options),
          subjectId: 1, // 默认科目ID
          chapterId: 1, // 默认章节ID
          creatorId: 1 // 默认创建者ID
        }
        
        let response
        if (questionForm.value.id) {
          response = await api.put(`/questions/${questionForm.value.id}`, submitData)
        } else {
          response = await api.post('/questions', submitData)
        }
        if (response.code === 200) {
          ElMessage.success(questionForm.value.id ? '更新题目成功' : '添加题目成功')
          dialogVisible.value = false
          fetchQuestions()
        } else {
          ElMessage.error(response.message)
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handleDeleteQuestion = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个题目吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await api.delete(`/questions/${id}`)
    if (response.code === 200) {
      ElMessage.success('删除题目成功')
      fetchQuestions()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    // 用户取消删除
  }
}
</script>

<style scoped>
.questions-container {
  padding: 20px;
}

.questions-card {
  margin-bottom: 20px;
}

.search-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-left {
  display: flex;
  align-items: center;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.option-item:hover {
  background-color: #f0f0f0;
}

.option-label {
  width: 40px;
  font-weight: bold;
  font-size: 14px;
  color: #333;
  margin-right: 8px;
}

.is-correct {
  font-weight: bold;
}

/* 正确答案按钮样式 */
.el-button.is-correct {
  background-color: #67c23a !important;
  border-color: #67c23a !important;
  color: #fff !important;
}

.el-button.is-correct:hover {
  background-color: #85ce61 !important;
  border-color: #85ce61 !important;
}

/* 对话框样式 */
.el-dialog__header {
  background-color: #f5f7fa !important;
  border-bottom: 1px solid #e4e7ed !important;
  padding: 20px !important;
}

.el-dialog__title {
  font-size: 16px !important;
  font-weight: bold !important;
  color: #303133 !important;
}

.el-dialog__body {
  padding: 24px !important;
}

.el-form-item__label {
  font-weight: 500 !important;
  color: #303133 !important;
}

/* 输入框样式 */
.el-input__inner {
  border-radius: 4px !important;
  border: 1px solid #dcdfe6 !important;
  transition: all 0.3s ease !important;
}

.el-input__inner:focus {
  border-color: #409eff !important;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2) !important;
}
</style>

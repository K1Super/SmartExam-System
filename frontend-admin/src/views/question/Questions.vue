<template>
  <div class="questions-container">
    <el-card class="questions-card">
      <template #header>
        <div class="card-header">
          <span>题目管理</span>
          <el-button type="primary" @click="handleAddQuestion">添加题目</el-button>
        </div>
      </template>
      <div class="questions-search">
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
      <el-table :data="questionsList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const questionsList = ref([])
const searchKeyword = ref('')
const typeFilter = ref('')

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

const handleAddQuestion = () => {
  // 添加题目逻辑
  console.log('添加题目')
}

const handleEditQuestion = (question) => {
  // 编辑题目逻辑
  console.log('编辑题目', question)
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.questions-search {
  margin-bottom: 20px;
}
</style>

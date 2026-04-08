<template>
  <div class="exam-papers-container">
    <el-card class="exam-papers-card">
      <template #header>
        <div class="card-header">
          <span>试卷管理</span>
          <el-button type="primary" @click="handleAddExamPaper">添加试卷</el-button>
        </div>
      </template>
      <el-table :data="examPapersList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="试卷标题"></el-table-column>
        <el-table-column prop="subjectId" label="科目"></el-table-column>
        <el-table-column prop="totalScore" label="总分"></el-table-column>
        <el-table-column prop="duration" label="时长(分钟)"></el-table-column>
        <el-table-column prop="questionCount" label="题目数量"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="info">草稿</el-tag>
            <el-tag v-else type="success">已发布</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditExamPaper(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status === 0" type="success" size="small" @click="handlePublish(scope.row.id)">发布</el-button>
            <el-button type="danger" size="small" @click="handleDeleteExamPaper(scope.row.id)">删除</el-button>
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

const examPapersList = ref([])

onMounted(() => {
  fetchExamPapers()
})

const fetchExamPapers = async () => {
  try {
    const response = await api.get('/exam-papers')
    if (response.code === 200) {
      examPapersList.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取试卷列表失败')
  }
}

const handleAddExamPaper = () => {
  // 添加试卷逻辑
  console.log('添加试卷')
}

const handleEditExamPaper = (examPaper) => {
  // 编辑试卷逻辑
  console.log('编辑试卷', examPaper)
}

const handlePublish = async (id) => {
  try {
    const response = await api.put(`/exam-papers/${id}/publish`)
    if (response.code === 200) {
      ElMessage.success('发布试卷成功')
      fetchExamPapers()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('发布试卷失败')
  }
}

const handleDeleteExamPaper = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个试卷吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await api.delete(`/exam-papers/${id}`)
    if (response.code === 200) {
      ElMessage.success('删除试卷成功')
      fetchExamPapers()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    // 用户取消删除
  }
}
</script>

<style scoped>
.exam-papers-container {
  padding: 20px;
}

.exam-papers-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

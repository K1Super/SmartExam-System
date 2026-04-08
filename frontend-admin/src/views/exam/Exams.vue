<template>
  <div class="exams-container">
    <el-card class="exams-card">
      <template #header>
        <div class="card-header">
          <span>考试管理</span>
          <el-button type="primary" @click="handleAddExam">添加考试</el-button>
        </div>
      </template>
      <el-table :data="examsList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="考试标题"></el-table-column>
        <el-table-column prop="paperId" label="试卷"></el-table-column>
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
        <el-table-column prop="duration" label="时长(分钟)"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="info">未开始</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="success">进行中</el-tag>
            <el-tag v-else type="danger">已结束</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditExam(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteExam(scope.row.id)">删除</el-button>
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

const examsList = ref([])

onMounted(() => {
  fetchExams()
})

const fetchExams = async () => {
  try {
    const response = await api.get('/exams')
    if (response.code === 200) {
      examsList.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取考试列表失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const handleAddExam = () => {
  // 添加考试逻辑
  console.log('添加考试')
}

const handleEditExam = (exam) => {
  // 编辑考试逻辑
  console.log('编辑考试', exam)
}

const handleDeleteExam = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个考试吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await api.delete(`/exams/${id}`)
    if (response.code === 200) {
      ElMessage.success('删除考试成功')
      fetchExams()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    // 用户取消删除
  }
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

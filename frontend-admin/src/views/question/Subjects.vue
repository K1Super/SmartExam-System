<template>
  <div class="subjects-container">
    <el-card class="subjects-card">
      <template #header>
        <div class="card-header">
          <span>科目管理</span>
          <el-button type="primary" @click="handleAddSubject">添加科目</el-button>
        </div>
      </template>
      <el-table :data="subjectsList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="科目名称"></el-table-column>
        <el-table-column prop="code" label="科目代码"></el-table-column>
        <el-table-column prop="description" label="科目描述" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditSubject(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteSubject(scope.row.id)">删除</el-button>
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

const subjectsList = ref([])

onMounted(() => {
  fetchSubjects()
})

const fetchSubjects = async () => {
  try {
    const response = await api.get('/subjects')
    if (response.code === 200) {
      subjectsList.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取科目列表失败')
  }
}

const handleAddSubject = () => {
  // 添加科目逻辑
  console.log('添加科目')
}

const handleEditSubject = (subject) => {
  // 编辑科目逻辑
  console.log('编辑科目', subject)
}

const handleDeleteSubject = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个科目吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await api.delete(`/subjects/${id}`)
    if (response.code === 200) {
      ElMessage.success('删除科目成功')
      fetchSubjects()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    // 用户取消删除
  }
}
</script>

<style scoped>
.subjects-container {
  padding: 20px;
}

.subjects-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

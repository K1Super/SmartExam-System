<template>
  <div class="subjects-container">
    <el-card class="subjects-card">
      <div class="search-toolbar">
        <div class="search-left"></div>
        <div class="search-right">
          <el-button type="primary" @click="handleAddSubject">添加科目</el-button>
        </div>
      </div>
      <el-table :data="subjectsList" style="width: 100%">
        <el-table-column type="index" label="序号" width="80"></el-table-column>
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

    <!-- 添加/编辑科目对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="subjectForm" :rules="rules" ref="subjectFormRef" label-width="80px">
        <el-form-item label="科目名称" prop="name">
          <el-input v-model="subjectForm.name" placeholder="请输入科目名称"></el-input>
        </el-form-item>
        <el-form-item label="科目代码" prop="code">
          <el-input v-model="subjectForm.code" placeholder="请输入科目代码"></el-input>
        </el-form-item>
        <el-form-item label="科目描述" prop="description">
          <el-input v-model="subjectForm.description" type="textarea" :rows="2" placeholder="请输入科目描述"></el-input>
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

const subjectsList = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('添加科目')
const subjectFormRef = ref(null)
const subjectForm = ref({
  id: null,
  name: '',
  code: '',
  description: ''
})

const rules = {
  name: [
    { required: true, message: '请输入科目名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入科目代码', trigger: 'blur' }
  ]
}

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
  dialogTitle.value = '添加科目'
  subjectForm.value = {
    id: null,
    name: '',
    code: '',
    description: ''
  }
  dialogVisible.value = true
}

const handleEditSubject = (subject) => {
  dialogTitle.value = '编辑科目'
  subjectForm.value = { ...subject }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!subjectFormRef.value) return
  
  await subjectFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (subjectForm.value.id) {
          response = await api.put(`/subjects/${subjectForm.value.id}`, subjectForm.value)
        } else {
          response = await api.post('/subjects', subjectForm.value)
        }
        if (response.code === 200) {
          ElMessage.success(subjectForm.value.id ? '更新科目成功' : '添加科目成功')
          dialogVisible.value = false
          fetchSubjects()
        } else {
          ElMessage.error(response.message)
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
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

.search-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>

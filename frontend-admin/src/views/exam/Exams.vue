<template>
  <div class="exams-container">
    <el-card class="exams-card">
      <div class="search-toolbar">
        <div class="search-left">
          <el-input v-model="searchKeyword" placeholder="请输入考试标题" style="width: 300px; margin-right: 10px;"></el-input>
          <el-button type="primary" @click="fetchExams">搜索</el-button>
        </div>
        <div class="search-right">
          <el-button type="primary" @click="handleAddExam">添加考试</el-button>
        </div>
      </div>
      <el-table :data="examsList" style="width: 100%">
        <el-table-column type="index" label="序号" width="80"></el-table-column>
        <el-table-column label="试卷标题">
          <template #default="scope">
            {{ getPaperName(scope.row.paperId) }}
          </template>
        </el-table-column>
        <el-table-column prop="clazzIds" label="发布班级" min-width="200">
          <template #default="scope">
            <span v-for="(id, idx) in parseClazzIds(scope.row.clazzIds)" :key="idx" style="margin-right: 8px; color: #505050;">
              {{ getClazzName(id) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="130">
          <template #default="scope">
            <span
              v-if="scope.row.status === 0"
              @click="handleExamCommand('publish', scope.row)"
              class="publish-link"
            >
              点击发布
            </span>
            <span v-else-if="scope.row.status === 1" style="color: #409040; font-weight: 500;">进行中</span>
            <span v-else style="color: #a0a0a0; font-weight: 500;">已结束</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" type="danger" @click="handleExamCommand('edit', scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleExamCommand('delete', scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑考试对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="examForm" :rules="rules" ref="examFormRef" label-width="100px">
        <el-form-item label="试卷" prop="paperId">
          <el-select v-model="examForm.paperId" placeholder="请选择试卷" style="width: 100%;">
            <el-option v-for="paper in examPapersList" :key="paper.id" :label="paper.title" :value="paper.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="发布班级" prop="clazzIds">
          <el-select v-model="examForm.clazzIds" multiple placeholder="请选择发布班级" style="width: 100%;">
            <el-option v-for="clazz in clazzList" :key="clazz.id" :label="clazz.className" :value="clazz.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="examForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="examForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="考试描述" prop="description">
          <el-input v-model="examForm.description" type="textarea" :rows="2" placeholder="请输入考试描述"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 发布考试对话框 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布考试"
      width="500px"
    >
      <el-form :model="publishForm" label-width="100px">
        <el-form-item label="选择班级">
          <el-select v-model="publishForm.clazzIds" multiple placeholder="请选择发布班级" style="width: 100%;">
            <el-option v-for="clazz in clazzList" :key="clazz.id" :label="clazz.className" :value="clazz.id"></el-option>
          </el-select>
        </el-form-item>
        <el-alert
          title="发布提示"
          type="info"
          :closable="false"
          show-icon
          description="考试发布后，所选班级的学生将可以参加考试，未在规定时间答题自动交卷，考试期间计时结束也会自动交卷。"
        >
        </el-alert>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="publishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmPublish">确认发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const examsList = ref([])
const examPapersList = ref([])
const clazzList = ref([])
const searchKeyword = ref('')

const dialogVisible = ref(false)
const publishDialogVisible = ref(false)
const dialogTitle = ref('添加考试')
const examFormRef = ref(null)
const currentPublishExam = ref({})

const examForm = ref({
  id: null,
  paperId: null,
  clazzIds: [],
  startTime: new Date(),
  endTime: new Date(Date.now() + 2 * 60 * 60 * 1000),
  description: '',
  status: 0
})

const publishForm = ref({
  clazzIds: []
})

const rules = {
  paperId: [
    { required: true, message: '请选择试卷', trigger: 'change' }
  ],
  clazzIds: [
    { required: true, message: '请选择发布班级', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
}

onMounted(() => {
  fetchExams()
  fetchExamPapers()
  fetchClazzList()
})

const fetchExams = async () => {
  try {
    const response = await api.get('/exams')
    if (response.code === 200) {
      examsList.value = response.data
    }
  } catch (error) {
    examsList.value = [
      { id: 1001, paperId: 1, clazzIds: [1, 2], startTime: '2026-04-15 09:00:00', endTime: '2026-04-15 11:00:00', status: 0 },
      { id: 1002, paperId: 2, clazzIds: [1, 3], startTime: '2026-04-20 14:00:00', endTime: '2026-04-20 16:00:00', status: 1 },
      { id: 1003, paperId: 3, clazzIds: [2, 4], startTime: '2026-04-10 08:00:00', endTime: '2026-04-10 10:20:00', status: 2 }
    ]
  }
}

const fetchExamPapers = async () => {
  try {
    const response = await api.get('/exam-papers')
    if (response.code === 200) {
      examPapersList.value = response.data
    }
  } catch (error) {
    examPapersList.value = [
      { id: 1, title: '2024数学期中试卷' },
      { id: 2, title: '计算机基础期末试卷' },
      { id: 3, title: '英语四级模拟试卷' }
    ]
  }
}

const fetchClazzList = async () => {
  try {
    const response = await api.get('/clazz')
    if (response.code === 200) {
      clazzList.value = response.data
    }
  } catch (error) {
    clazzList.value = [
      { id: 1, className: '计算机科学1班' },
      { id: 2, className: '软件工程2班' },
      { id: 3, className: '大数据3班' },
      { id: 4, className: '人工智能1班' }
    ]
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const getPaperName = (paperId) => {
  if (!paperId) return '未选择试卷'
  const paper = examPapersList.value.find(p => p.id === paperId)
  return paper ? paper.title : '未知试卷'
}

const getClazzName = (clazzId) => {
  const clazz = clazzList.value.find(c => c.id === clazzId)
  return clazz ? clazz.className : '未知班级'
}

const parseClazzIds = (clazzIds) => {
  if (!clazzIds) return []
  if (typeof clazzIds === 'string') {
    return clazzIds.split(',').map(id => parseInt(id))
  }
  return Array.isArray(clazzIds) ? clazzIds : [clazzIds]
}

const generateExamId = () => {
  if (examsList.value.length === 0) return 1001
  const maxId = Math.max(...examsList.value.map(e => parseInt(e.id)))
  return maxId + 1
}

const handleAddExam = () => {
  dialogTitle.value = '添加考试'
  examForm.value = {
    id: null,
    paperId: null,
    clazzIds: [],
    startTime: new Date(),
    endTime: new Date(Date.now() + 2 * 60 * 60 * 1000),
    description: '',
    status: 0
  }
  dialogVisible.value = true
}

const handleExamCommand = (command, row) => {
  switch (command) {
    case 'edit':
      dialogTitle.value = '编辑考试'
      examForm.value = {
        ...row,
        clazzIds: parseClazzIds(row.clazzIds),
        startTime: row.startTime ? new Date(row.startTime) : new Date(),
        endTime: row.endTime ? new Date(row.endTime) : new Date()
      }
      dialogVisible.value = true
      break
    case 'publish':
      currentPublishExam.value = row
      publishForm.value.clazzIds = parseClazzIds(row.clazzIds)
      publishDialogVisible.value = true
      break
    case 'delete':
      ElMessageBox.confirm('确定要删除该考试吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await api.delete(`/exams/${row.id}`)
          await fetchExams()
          ElMessage.success('删除成功')
        } catch (err) {
          ElMessage.error('删除失败')
        }
      }).catch(() => {})
      break
  }
}

const validateTime = () => {
  const startTime = new Date(examForm.value.startTime).getTime()
  const endTime = new Date(examForm.value.endTime).getTime()
  
  if (startTime >= endTime) {
    ElMessage.error('开始时间不能在结束时间之后')
    return false
  }
  
  return true
}

const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  // ISO格式带T，Jackson能直接识别
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

const handleSubmit = async () => {
  if (!examFormRef.value) return
  
  try {
    await examFormRef.value.validate()
    
    if (!validateTime()) {
      return
    }
    
    try {
      const formData = {
        ...examForm.value,
        startTime: formatDateTime(examForm.value.startTime),
        endTime: formatDateTime(examForm.value.endTime),
        clazzIds: examForm.value.clazzIds.join(','),
        status: 0
      }
      
      let response
      if (examForm.value.id) {
        response = await api.put(`/exams/${examForm.value.id}`, formData)
      } else {
        response = await api.post('/exams', formData)
      }
      
      if (response.code !== 200) {
        throw new Error(response.message || '操作失败')
      }
    } catch (err) {
      ElMessage.error('操作失败: ' + (err.message || '未知错误'))
      return
    }
    
    ElMessage.success(examForm.value.id ? '编辑成功' : '添加成功')
    dialogVisible.value = false
    await fetchExams()
  } catch (error) {
    // 静默失败
  }
}

const confirmPublish = async () => {
  if (publishForm.value.clazzIds.length === 0) {
    ElMessage.warning('请至少选择一个班级')
    return
  }
  
  try {
    await api.put(`/exams/${currentPublishExam.value.id}`, {
      status: 1,
      clazzIds: publishForm.value.clazzIds.join(',')
    })
  } catch (err) {
    ElMessage.error('发布失败: ' + err.message)
    return
  }
  
  ElMessage.success('考试发布成功，学生可以开始参加考试')
  publishDialogVisible.value = false
  await fetchExams()
}
</script>

<style scoped>
.exams-container {
  padding: 20px;
}

:deep(.exams-card) {
  border-radius: 4px;
  border: 1px solid #ebeef5;
  box-shadow: none;
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

:deep(.publish-link) {
  color: #606060;
  cursor: pointer;
  font-weight: 500;
  text-decoration: underline;
  text-underline-offset: 2px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-block;
}

:deep(.publish-link:hover) {
  color: #1a1a1a;
  text-decoration: none;
  transform: translateY(-1px);
  font-weight: 600;
}

:deep(.el-dialog) {
  border-radius: 4px;
}
</style>

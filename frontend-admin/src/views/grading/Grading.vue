<template>
  <div class="grading-container">
    <el-card class="grading-card">
      <el-table :data="examRecords" style="width: 100%">
        <el-table-column prop="recordId" label="记录ID" width="100"></el-table-column>
        <el-table-column prop="examTitle" label="考试标题"></el-table-column>
        <el-table-column prop="userName" label="学生姓名" width="120"></el-table-column>
        <el-table-column prop="clazzName" label="班级" width="150"></el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="info">未开始</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="warning">答题中</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="info">已提交</el-tag>
            <el-tag v-else-if="scope.row.status === 3" type="success">已批改</el-tag>
            <el-tag v-else-if="scope.row.status === 4" type="danger">已超时</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary"
              @click="handleAutoGrade(scope.row)"
              :disabled="scope.row.status === 3"
            >
              自动阅卷
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
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

const examRecords = ref([])

onMounted(() => {
  fetchExamRecords()
})

const fetchExamRecords = async () => {
  try {
    const response = await api.get('/exam-records')
    if (response.code === 200) {
      examRecords.value = response.data
    }
  } catch (error) {
    examRecords.value = [
      { recordId: 'AUTO-1001', examId: 1, examTitle: '2024年期中数学考试', userId: 101, userName: '张三', clazzId: 1, clazzName: '计算机科学1班', startTime: '2026-04-15 09:05:00', submitTime: '2026-04-15 10:50:00', status: 2 },
      { recordId: 'AUTO-1002', examId: 1, examTitle: '2024年期中数学考试', userId: 102, userName: '李四', clazzId: 1, clazzName: '计算机科学1班', startTime: '2026-04-15 09:03:00', submitTime: '2026-04-15 10:45:00', status: 2 },
      { recordId: 'AUTO-1003', examId: 2, examTitle: '计算机基础期末考试', userId: 201, userName: '王五', clazzId: 2, clazzName: '软件工程2班', startTime: '2026-04-20 14:02:00', submitTime: '2026-04-20 15:58:00', status: 3 },
      { recordId: 'AUTO-1004', examId: 2, examTitle: '计算机基础期末考试', userId: 202, userName: '赵六', clazzId: 2, clazzName: '软件工程2班', startTime: '2026-04-20 14:05:00', submitTime: null, status: 4 },
      { recordId: 'AUTO-1005', examId: 3, examTitle: '英语四级模拟考试', userId: 301, userName: '孙七', clazzId: 3, clazzName: '大数据3班', startTime: null, submitTime: null, status: 0 }
    ]
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const handleAutoGrade = async (row) => {
  const recordId = row.id || row.recordId
  const finalRecordId = Number(recordId)
  
  if (!finalRecordId || isNaN(finalRecordId)) {
    ElMessage.error('记录ID无效')
    return
  }
  
  if (row.status === 3) {
    ElMessage.warning('该记录已完成批改')
    return
  }
  
  try {
    const res = await api.post(`/grading/auto/${finalRecordId}`)
    
    if (res.code !== 200) {
      throw new Error(res.message || '自动阅卷失败')
    }
    
    ElMessage.success('自动阅卷成功，客观题已自动判分，主观题待手动批改')
    await fetchExamRecords()
  } catch (err) {
    ElMessage.error('自动阅卷失败: ' + (err.response?.data?.message || err.message || '服务器错误'))
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该阅卷记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    try {
      const recordId = row.id || row.recordId
      const res = await api.delete(`/exam-records/${recordId}`)
      
      if (res.code !== 200) {
        throw new Error(res.message || '删除失败')
      }
    } catch (err) {
      ElMessage.error('删除失败: ' + (err.message || '未知错误'))
      return
    }
    
    ElMessage.success('删除成功')
    await fetchExamRecords()
  } catch (error) {
  }
}
</script>

<style scoped>
.grading-container {
  padding: 20px;
}

:deep(.grading-card) {
  border-radius: 4px;
  border: 1px solid #ebeef5;
  box-shadow: none;
}


</style>

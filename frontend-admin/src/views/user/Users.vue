<template>
  <div class="users-container">
    <el-card class="users-card">
      <div class="search-toolbar">
        <div class="search-left">
          <el-input v-model="searchKeyword" placeholder="请输入姓名" style="width: 300px; margin-right: 10px;"></el-input>
          <el-select v-model="roleFilter" placeholder="选择角色" style="width: 150px; margin-right: 10px;">
            <el-option label="全部" value=""></el-option>
            <el-option label="管理员" :value="1"></el-option>
            <el-option label="教师" :value="2"></el-option>
            <el-option label="学生" :value="3"></el-option>
          </el-select>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
        <div class="search-right">
          <el-button type="primary" @click="handleAddUser">添加用户</el-button>
        </div>
      </div>
      <el-table :data="usersList" style="width: 100%">
        <el-table-column type="index" label="序号" width="80"></el-table-column>
        <el-table-column prop="realName" label="姓名"></el-table-column>
        <el-table-column prop="phone" label="学号"></el-table-column>
        <el-table-column prop="role" label="角色">
          <template #default="scope">
            <span v-if="scope.row.role === 1">管理员</span>
            <span v-else-if="scope.row.role === 2">教师</span>
            <span v-else>学生</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(scope.row)"></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditUser(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteUser(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          layout="prev, pager, next"
          :total="total"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="450px"
    >
      <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="70px">
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="学号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入学号"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%;" @change="handleRoleChange">
            <el-option label="管理员" :value="1"></el-option>
            <el-option label="教师" :value="2"></el-option>
            <el-option label="学生" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码，学生默认123456"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="userForm.status" :active-value="1" :inactive-value="0"></el-switch>
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

const usersList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const roleFilter = ref('')

const dialogVisible = ref(false)
const dialogTitle = ref('添加用户')
const userFormRef = ref(null)
const userForm = ref({
  id: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  role: null,
  status: 1
})

const rules = {
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

onMounted(() => {
  fetchUsers()
})

const fetchUsers = async () => {
  try {
    const response = await api.get('/users')
    if (response.code === 200) {
      usersList.value = response.data
      total.value = response.data.length
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  }
}

const handleSearch = () => {
  fetchUsers()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  fetchUsers()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  fetchUsers()
}

const handleAddUser = () => {
  dialogTitle.value = '添加用户'
  userForm.value = {
    id: null,
    username: '',
    password: '',
    realName: '',
    phone: '',
    role: null,
    status: 1
  }
  dialogVisible.value = true
}

const handleRoleChange = (role) => {
  if (role === 3) {
    userForm.value.password = '123456'
  }
}

const handleEditUser = (user) => {
  dialogTitle.value = '编辑用户'
  userForm.value = { 
    ...user,
    role: Number(user.role),
    status: user.status ? 1 : 0
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const formData = { 
          ...userForm.value,
          username: userForm.value.phone,
          role: Number(userForm.value.role)
        }
        delete formData.createTime
        delete formData.updateTime
        
        let response
        if (userForm.value.id) {
          response = await api.put(`/users/${userForm.value.id}`, formData)
        } else {
          response = await api.post('/users', formData)
        }
        if (response.code === 200) {
          ElMessage.success(userForm.value.id ? '更新用户成功' : '添加用户成功')
          dialogVisible.value = false
          fetchUsers()
        } else {
          ElMessage.error(response.message)
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handleDeleteUser = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个用户吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await api.delete(`/users/${id}`)
    if (response.code === 200) {
      ElMessage.success('删除用户成功')
      fetchUsers()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
  }
}

const handleStatusChange = async (user) => {
  try {
    const updateData = {
      id: user.id,
      status: user.status,
      username: user.username,
      realName: user.realName,
      phone: user.phone,
      role: user.role
    }
    delete updateData.createTime
    delete updateData.updateTime
    
    const response = await api.put(`/users/${user.id}`, updateData)
    if (response.code !== 200) {
      user.status = user.status === 1 ? 0 : 1
      ElMessage.error('更新状态失败')
    }
  } catch (error) {
    user.status = user.status === 1 ? 0 : 1
    ElMessage.error('更新状态失败')
  }
}
</script>

<style scoped>
.users-container {
  padding: 0;
}

:deep(.users-card) {
  border: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

:deep(.el-button--primary) {
  background: #1a1a1a !important;
  border-color: #1a1a1a !important;
  color: #fff !important;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  background: #404040 !important;
  border-color: #404040 !important;
}

:deep(.el-button--danger) {
  background: #fff !important;
  border-color: rgba(0, 0, 0, 0.15) !important;
  color: #1a1a1a !important;
  transition: all 0.3s ease;
}

:deep(.el-button--danger:hover) {
  background: #f5f5f5 !important;
  border-color: rgba(0, 0, 0, 0.25) !important;
}

:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #606060 !important;
  border-color: #606060 !important;
}

.search-left {
  display: flex;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

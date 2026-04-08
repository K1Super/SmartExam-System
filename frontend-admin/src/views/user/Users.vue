<template>
  <div class="users-container">
    <el-card class="users-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAddUser">添加用户</el-button>
        </div>
      </template>
      <div class="users-search">
        <el-input v-model="searchKeyword" placeholder="请输入用户名" style="width: 300px; margin-right: 10px;"></el-input>
        <el-select v-model="roleFilter" placeholder="选择角色" style="width: 150px; margin-right: 10px;">
          <el-option label="全部" value=""></el-option>
          <el-option label="管理员" value="1"></el-option>
          <el-option label="教师" value="2"></el-option>
          <el-option label="学生" value="3"></el-option>
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <el-table :data="usersList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="realName" label="真实姓名"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="phone" label="手机号"></el-table-column>
        <el-table-column prop="role" label="角色">
          <template #default="scope">
            <span v-if="scope.row.role === 1">管理员</span>
            <span v-else-if="scope.row.role === 2">教师</span>
            <span v-else>学生</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-switch v-model="scope.row.status" @change="handleStatusChange(scope.row)"></el-switch>
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
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!userForm.id">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="1"></el-option>
            <el-option label="教师" value="2"></el-option>
            <el-option label="学生" value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="userForm.status"></el-switch>
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
  email: '',
  phone: '',
  role: 3,
  status: 1
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' }
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
  // 这里可以添加搜索逻辑
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
    email: '',
    phone: '',
    role: 3,
    status: 1
  }
  dialogVisible.value = true
}

const handleEditUser = (user) => {
  dialogTitle.value = '编辑用户'
  userForm.value = { ...user }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (userForm.value.id) {
          response = await api.put(`/users/${userForm.value.id}`, userForm.value)
        } else {
          response = await api.post('/users', userForm.value)
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
    // 用户取消删除
  }
}

const handleStatusChange = async (user) => {
  try {
    const response = await api.put(`/users/${user.id}`, user)
    if (response.code !== 200) {
      user.status = !user.status
      ElMessage.error('更新状态失败')
    }
  } catch (error) {
    user.status = !user.status
    ElMessage.error('更新状态失败')
  }
}
</script>

<style scoped>
.users-container {
  padding: 20px;
}

.users-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.users-search {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

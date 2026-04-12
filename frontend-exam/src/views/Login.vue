<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-form">
        <div class="login-header">
          <h1 class="login-title">SmartExam</h1>
          <p class="login-subtitle">在线考试平台</p>
        </div>
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-position="top">
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名" 
              :prefix-icon="User"
              class="login-input"
            ></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              :prefix-icon="Lock"
              class="login-input"
              @keyup.enter="handleLogin"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleLogin" 
              class="login-button"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="login-footer">
          <p class="login-copyright">© 2026 SmartExam. All rights reserved.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const loginFormRef = ref(null)
const loginForm = ref({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

onMounted(() => {
  document.body.classList.add('login-page')
  localStorage.removeItem('token')
  localStorage.removeItem('user')
})

onUnmounted(() => {
  document.body.classList.remove('login-page')
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await api.post('/auth/login', loginForm.value)
        if (response.code === 200) {
          localStorage.setItem('token', response.data.token)
          localStorage.setItem('user', JSON.stringify(response.data.user))
          router.push('/exams')
        } else {
          ElMessage.error(response.message)
        }
      } catch (error) {
        ElMessage.error('登录失败，请检查用户名和密码')
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(180deg, #f8f9fa 0%, #e9ecef 100%);
  position: relative;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #1a1a1a 0%, #4a4a4a 50%, #1a1a1a 100%);
}

.login-wrapper {
  width: 100%;
  max-width: 360px;
  padding: 20px;
}

.login-form {
  padding: 40px 32px;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #e0e0e0;
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-title {
  font-size: 30px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  letter-spacing: 0.02em;
}

.login-subtitle {
  font-size: 14px;
  color: #666666;
  margin: 0;
  font-weight: 400;
}

.login-input {
  height: 48px;
  font-size: 15px;
}

.login-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  border: 1.5px solid #e0e0e0;
  background: #fafafa;
  transition: all 0.25s ease;
}

.login-input :deep(.el-input__wrapper:hover) {
  border-color: #999999;
  background: #ffffff;
}

.login-input :deep(.el-input__wrapper.is-focus) {
  border-color: #1a1a1a;
  background: #ffffff;
}

.login-input :deep(.el-input__inner) {
  color: #1a1a1a;
  font-weight: 400;
}

.login-input :deep(.el-input__inner::placeholder) {
  color: #999999;
}

.login-input :deep(.el-input__prefix) {
  color: #666666;
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.05em;
  border-radius: 10px;
  background: #1a1a1a;
  border: none;
  color: #ffffff;
  transition: all 0.25s ease;
  margin-top: 8px;
}

.login-button:hover {
  background: #333333;
}

.login-button:active {
  background: #1a1a1a;
}

.login-footer {
  text-align: center;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.login-copyright {
  font-size: 13px;
  color: #999999;
  margin: 0;
}

@media (max-width: 768px) {
  .login-wrapper {
    padding: 16px;
  }
  
  .login-form {
    padding: 32px 24px;
    border-radius: 12px;
  }
  
  .login-title {
    font-size: 26px;
  }
  
  .login-input,
  .login-button {
    height: 46px;
  }
}

@media (max-width: 480px) {
  .login-form {
    padding: 28px 20px;
    border-radius: 12px;
  }
  
  .login-title {
    font-size: 24px;
  }
}
</style>

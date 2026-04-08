<template>
  <div class="app-container">
    <el-container style="height: 100vh">
      <el-header height="60px" class="header">
        <div class="logo">SmartExam 考试系统</div>
        <div class="user-info">
          <span class="user-name">{{ user?.realName || '考生' }}</span>
          <el-button type="primary" size="small" @click="handleLogout" style="margin-left: 20px;">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const user = ref(null)

const handleLogout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}

// 检查登录状态
const checkLogin = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
  }
}

checkLogin()
</script>

<style scoped>
.app-container {
  width: 100%;
  height: 100vh;
}

.header {
  background-color: #409EFF;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-name {
  margin-right: 20px;
}

.main-content {
  padding: 20px;
  background-color: #f0f2f5;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

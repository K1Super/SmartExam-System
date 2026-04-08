<template>
  <div class="app-container">
    <el-container style="height: 100vh">
      <el-header height="60px" class="header">
        <div class="logo">SmartExam</div>
        <div class="user-info">
          <el-dropdown>
            <span class="user-name">{{ user?.realName || '管理员' }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" class="sidebar">
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            router
          >
            <el-menu-item index="/dashboard">
              <el-icon><House /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>
            <el-sub-menu index="user">
              <template #title>
                <el-icon><User /></el-icon>
                <span>用户管理</span>
              </template>
              <el-menu-item index="/users">用户列表</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="question">
              <template #title>
                <el-icon><Document /></el-icon>
                <span>题库管理</span>
              </template>
              <el-menu-item index="/questions">题目列表</el-menu-item>
              <el-menu-item index="/subjects">科目管理</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="exam">
              <template #title>
                <el-icon><DataAnalysis /></el-icon>
                <span>考试管理</span>
              </template>
              <el-menu-item index="/exam-papers">试卷管理</el-menu-item>
              <el-menu-item index="/exams">考试管理</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="grading">
              <template #title>
                <el-icon><Check /></el-icon>
                <span>阅卷管理</span>
              </template>
              <el-menu-item index="/grading">自动阅卷</el-menu-item>
              <el-menu-item index="/manual-grading">手动阅卷</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="statistics">
              <template #title>
                <el-icon><TrendCharts /></el-icon>
                <span>成绩统计</span>
              </template>
              <el-menu-item index="/score-statistics">成绩分析</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { House, User, Document, DataAnalysis, Check, TrendCharts } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const user = ref(null)

const activeMenu = computed(() => {
  return route.path
})

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
  cursor: pointer;
  padding: 0 10px;
}

.sidebar {
  background-color: #f5f7fa;
  border-right: 1px solid #e4e7ed;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
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

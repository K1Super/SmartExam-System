<template>
  <div class="app-container">
    <el-container style="height: 100vh">
      <el-header height="60px" class="header">
        <div class="logo">
          <span class="logo-text">SmartExam</span>
        </div>
        <div class="user-info">
          <el-dropdown>
            <div class="user-dropdown">
              <span class="user-name">{{ user?.realName || '管理员' }}</span>
              <el-icon class="user-icon"><arrow-down /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout" class="logout-item">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside width="220px" class="sidebar">
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            router
            :collapse-transition="false"
          >
            <el-sub-menu index="user">
              <template #title>
                <el-icon class="menu-icon"><User /></el-icon>
                <span class="menu-text">用户管理</span>
              </template>
              <el-menu-item index="/users">
                <span class="menu-text">用户列表</span>
              </el-menu-item>
              <el-menu-item index="/clazz">
                <span class="menu-text">班级管理</span>
              </el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="question">
              <template #title>
                <el-icon class="menu-icon"><Document /></el-icon>
                <span class="menu-text">题库管理</span>
              </template>
              <el-menu-item index="/questions">
                <span class="menu-text">题目列表</span>
              </el-menu-item>
              <el-menu-item index="/subjects">
                <span class="menu-text">科目管理</span>
              </el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="exam">
              <template #title>
                <el-icon class="menu-icon"><DataAnalysis /></el-icon>
                <span class="menu-text">考试管理</span>
              </template>
              <el-menu-item index="/exam-papers">
                <span class="menu-text">试卷管理</span>
              </el-menu-item>
              <el-menu-item index="/exams">
                <span class="menu-text">考试管理</span>
              </el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="grading">
              <template #title>
                <el-icon class="menu-icon"><Check /></el-icon>
                <span class="menu-text">阅卷管理</span>
              </template>
              <el-menu-item index="/grading">
                <span class="menu-text">自动阅卷</span>
              </el-menu-item>
              <el-menu-item index="/manual-grading">
                <span class="menu-text">手动阅卷</span>
              </el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="statistics">
              <template #title>
                <el-icon class="menu-icon"><TrendCharts /></el-icon>
                <span class="menu-text">成绩统计</span>
              </template>
              <el-menu-item index="/score-statistics">
                <span class="menu-text">成绩分析</span>
              </el-menu-item>
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
import { User, Document, DataAnalysis, Check, TrendCharts, SwitchButton, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const user = ref(null)

const activeMenu = computed(() => {
  if (route.path === '/welcome') {
    return ''
  }
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
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 100;
}

.logo {
  display: flex;
  align-items: center;
  padding-left: 4px;
}

.logo-text {
  font-size: 18px;
  font-weight: 300;
  color: #1a1a1a;
  letter-spacing: 3px;
  text-transform: uppercase;
  position: relative;
  font-feature-settings: 'tnum';
  font-variant-numeric: tabular-nums;
}

.logo-text::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, #1a1a1a, transparent);
  opacity: 0.15;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  background: #f8f8f8;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.user-dropdown:hover {
  background: #f0f0f0;
  border-color: rgba(0, 0, 0, 0.08);
}

.user-name {
  font-size: 13px;
  font-weight: 400;
  color: #404040;
  letter-spacing: 0.3px;
}

.user-icon {
  font-size: 11px;
  color: #8c8c8c;
  transition: all 0.3s ease;
}

.user-dropdown:hover .user-icon {
  color: #1a1a1a;
}

.logout-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.sidebar {
  background: #fff;
  border-right: 1px solid rgba(0, 0, 0, 0.04);
  position: relative;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
  background: transparent;
  padding: 24px 12px;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 2px 0;
  border-radius: 8px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  color: #505050;
  font-size: 13px;
  font-weight: 400;
  letter-spacing: 0.2px;
  background: transparent !important;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: #f8f8f8 !important;
  color: #1a1a1a !important;
  transform: translateX(2px);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: #f5f5f5 !important;
  color: #1a1a1a !important;
  font-weight: 500;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.sidebar-menu :deep(.el-menu-item.is-active .menu-icon) {
  color: #1a1a1a !important;
  opacity: 1;
}

.sidebar-menu :deep(.el-sub-menu) {
  background: transparent !important;
}

.sidebar-menu :deep(.el-menu--inline) {
  padding-left: 12px !important;
}

.sidebar-menu :deep(.el-menu--inline .el-menu-item) {
  padding-left: 44px !important;
  height: 40px;
  line-height: 40px;
  font-size: 12px;
}

.menu-icon {
  font-size: 17px;
  margin-right: 12px;
  color: #bfbfbf;
  opacity: 0.9;
  transition: all 0.3s ease;
}

.menu-text {
  font-size: 13px;
  font-weight: 400;
  transition: all 0.3s ease;
}

.main-content {
  padding: 28px;
  background: #fafafa;
  min-height: calc(100vh - 60px);
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
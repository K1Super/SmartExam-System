<template>
  <div class="app-container">
    <el-container style="height: 100vh">
      <el-header height="60px" class="header">
        <div class="logo">
          <span class="logo-text">SmartExam</span>
        </div>
        <div class="user-info">
          <div class="user-profile">
            <span class="user-name">{{ user?.realName || '考生' }}</span>
          </div>
          <el-dropdown @command="handleCommand" trigger="click">
            <el-button 
              class="setting-button" 
              size="small" 
              :icon="Setting"
              :class="{ 'has-hovered': hasHovered }"
              @mouseenter="hasHovered = true"
            >
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Setting, User, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const user = ref(null)
const hasHovered = ref(false)

const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'profile') {
    ElMessage.info('个人中心功能开发中')
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  ElMessage.success('已退出登录')
  router.push('/login')
}

const checkLogin = () => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  if (!token) {
    router.push('/login')
    return false
  }
  if (userStr) {
    user.value = JSON.parse(userStr)
  }
  return true
}

onMounted(() => {
  checkLogin()
})

checkLogin()
</script>

<style scoped>
.app-container {
  width: 100%;
  height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif;
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
  gap: 12px;
}

.user-profile {
  display: flex;
  align-items: center;
  padding: 6px 0;
  background: transparent;
  border: none;
  position: relative;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  letter-spacing: 0.3px;
  position: relative;
  user-select: none;
  -webkit-user-select: none;
  cursor: default;
}

.user-name::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, #1a1a1a, transparent);
}

.setting-button {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.setting-button {
  cursor: pointer;
}

.setting-button:hover {
  color: #495057;
}

.setting-button.has-hovered :deep(.el-icon) {
  transition: transform 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.setting-button.has-hovered:hover :deep(.el-icon) {
  transform: rotate(360deg);
}

.setting-button.has-hovered:not(:hover) :deep(.el-icon) {
  transform: rotate(-360deg);
}

.setting-button :deep(.el-icon) {
  font-size: 16px;
}

:deep(.el-dropdown) {
  --el-dropdown-menuItem-hover-fill: #f5f7fa;
}

:deep(.el-dropdown-menu) {
  border-radius: 6px;
  padding: 3px;
  border: 1px solid #ebeef5;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07);
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  border-radius: 4px;
  padding: 6px 12px;
  margin: 0;
  transition: all 0.2s ease;
}

:deep(.el-dropdown-menu__item:not(.is-disabled):hover) {
  background-color: #f5f7fa;
  margin: 0;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
}

.main-content {
  background: #f8f9fa;
  padding: 24px;
  overflow-y: auto;
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

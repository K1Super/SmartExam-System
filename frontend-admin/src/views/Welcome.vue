
<template>
  <div class="welcome-container">
    <div class="welcome-content">
      <h1 class="welcome-title">SmartExam</h1>
      <p class="welcome-subtitle">智能考试管理系统</p>
      <div class="welcome-divider"></div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.welcome-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.welcome-content {
  text-align: center;
}

.welcome-title {
  font-size: 48px;
  font-weight: 200;
  color: #1a1a1a;
  letter-spacing: 8px;
  margin: 0 0 16px 0;
  text-transform: uppercase;
}

.welcome-subtitle {
  font-size: 16px;
  font-weight: 300;
  color: #8c8c8c;
  letter-spacing: 4px;
  margin: 0 0 40px 0;
}

.welcome-divider {
  width: 120px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 0, 0, 0.1), transparent);
  margin: 0 auto;
}
</style>

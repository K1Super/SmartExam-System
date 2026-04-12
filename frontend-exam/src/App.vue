<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: Arial, sans-serif;
}

body:not(.login-page) .el-button--primary {
  background-color: #ffffff !important;
  border-color: #d0d0d0 !important;
  color: #333333 !important;
}

body:not(.login-page) .el-button--primary:hover {
  background-color: #f8f8f8 !important;
  border-color: #333333 !important;
  color: #333333 !important;
}

body:not(.login-page) .el-button--primary:active {
  background-color: #f0f0f0 !important;
  border-color: #333333 !important;
  color: #333333 !important;
}

body:not(.login-page) .el-button--success {
  background-color: #ffffff !important;
  border-color: #b0b0b0 !important;
  color: #555555 !important;
}

body:not(.login-page) .el-button--success:hover {
  background-color: #fafafa !important;
  border-color: #555555 !important;
  color: #555555 !important;
}

body:not(.login-page) .el-button--warning {
  background-color: #ffffff !important;
  border-color: #c0c0c0 !important;
  color: #666666 !important;
}

body:not(.login-page) .el-button--warning:hover {
  background-color: #fafafa !important;
  border-color: #666666 !important;
  color: #666666 !important;
}

body:not(.login-page) .el-button--danger {
  background-color: #ffffff !important;
  border-color: #d0d0d0 !important;
  color: #333333 !important;
}

body:not(.login-page) .el-button--danger:hover {
  background-color: #f8f8f8 !important;
  border-color: #333333 !important;
  color: #333333 !important;
}

body:not(.login-page) .el-button--info {
  background-color: #f5f5f5 !important;
  border-color: #e0e0e0 !important;
  color: #666666 !important;
}

body:not(.login-page) .el-button--info:hover {
  background-color: #ffffff !important;
  border-color: #666666 !important;
  color: #666666 !important;
}

body:not(.login-page) .el-button--default {
  background-color: #ffffff !important;
  border-color: #e0e0e0 !important;
  color: #333333 !important;
}

body:not(.login-page) .el-button--default:hover {
  background-color: #fafafa !important;
  border-color: #333333 !important;
  color: #333333 !important;
}

body:not(.login-page) .el-button {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s ease;
  border-width: 1px;
}

body:not(.login-page) .el-button:focus {
  outline: none;
}

body:not(.login-page) .el-button.is-plain {
  background-color: #ffffff !important;
}

body:not(.login-page) .el-button--primary.is-plain {
  color: #333333 !important;
  border-color: #d0d0d0 !important;
}

body:not(.login-page) .el-button--primary.is-plain:hover {
  background-color: #f8f8f8 !important;
  border-color: #333333 !important;
  color: #333333 !important;
}
</style>

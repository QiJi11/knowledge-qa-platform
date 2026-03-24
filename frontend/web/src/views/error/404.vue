<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}

function goBack() {
  router.back()
}
</script>

<template>
  <div class="not-found-page">
    <div class="content">
      <div class="error-code">404</div>
      <h1>页面不存在</h1>
      <p>抱歉，您访问的页面不存在或已被移除</p>
      <div class="actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.content {
  text-align: center;
  color: #fff;
}

.error-code {
  font-size: 160px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 16px;
  text-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

h1 {
  font-size: 32px;
  margin-bottom: 16px;
}

p {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 32px;
}

.actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.actions .el-button {
  padding: 12px 32px;
}
</style>

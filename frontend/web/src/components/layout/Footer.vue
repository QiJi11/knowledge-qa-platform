<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-info">
          <p>&copy; 2026 学习平台 | 在线教育知识付费系统</p>
          <p class="footer-links">
            <a href="#">关于我们</a>
            <a href="#">联系方式</a>
            <a href="#">用户协议</a>
            <a href="#">隐私政策</a>
          </p>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: #2c3e50;
  color: #bdc3c7;
  padding: 40px 0 20px;
  margin-top: 80px;
}

.footer-content {
  text-align: center;
}

.footer-info p {
  margin-bottom: 10px;
  font-size: 14px;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 30px;
  flex-wrap: wrap;
}

.footer-links a {
  color: #bdc3c7;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: white;
}
</style>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Phone, Lock, Message, ChatDotRound, Connection as ConnectionIcon } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const loading = ref(false)
const loginMode = ref<'account' | 'phone'>('account')

const form = reactive({
  account: '', // 用户名或邮箱
  password: '',
  phone: '',
  code: ''
})

const rules = reactive<FormRules>({
  account: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
})

const codeSending = ref(false)
const countdown = ref(0)

async function sendCode() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  codeSending.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('验证码已发送，请注意查收')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch {
    ElMessage.error('发送失败，请重试')
  } finally {
    codeSending.value = false
  }
}

async function handleLogin() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: form.account,
          password: form.password
        })
      })
      const result = await res.json()
      
      if (result.code !== 0) {
        ElMessage.error(result.message || '登录失败')
        return
      }

      localStorage.setItem('token', result.data.token)
      localStorage.setItem('nickname', result.data.nickname || '')
      localStorage.setItem('role', result.data.role || 'student')
      localStorage.setItem('userId', String(result.data.userId || ''))
      ElMessage.success('登录成功，欢迎回来！')
      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    } catch (error: any) {
      ElMessage.error(error.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}

const switchMode = (mode: 'account' | 'phone') => {
  loginMode.value = mode
  formRef.value?.clearValidate()
}

const handleForgotPwd = () => {
  ElMessageBox.alert(
    '请使用注册时预留的手机号进行验证码找回，或者联系平台客服（admin@eduplatform.com）处理。', 
    '忘记密码', 
    { confirmButtonText: '我知道了', type: 'info' }
  )
}
</script>

<template>
  <div class="auth-layout">
    <!-- 左侧高清摄影图与 Slogan -->
    <div class="auth-banner">
      <div class="banner-overlay"></div>
      <div class="banner-content">
        <div class="logo-area">
          <span class="logo-icon">📚</span>
          <span class="logo-text">EduPlatform</span>
        </div>
        <h1 class="banner-title">开启卓越的<br/>数字学习之旅</h1>
        <p class="banner-desc">突破传统的边界，在这里与数万名行业精英一起探索前沿技术、交流深度经验。</p>
        
        <div class="testimonial">
          <div class="quote">"这是我用过的最干净、最专业的在线学习平台，课程质量极高，社区也非常活跃。"</div>
          <div class="author">
            <div class="avatar"></div>
            <div class="info">
              <span class="name">Sarah Chen</span>
              <span class="title">高级前端架构师</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧容器：不再是纯白平板，加入了质感背景和沉浸卡片 -->
    <div class="auth-panel-container">
      <div class="panel-header">
        <router-link to="/" class="back-link">
          <span class="arrow">←</span> 返回首页
        </router-link>
      </div>

      <div class="form-card-wrapper">
        <!-- 悬浮微光质感登录卡片 -->
        <div class="form-card">
          <div class="form-header">
            <h2>欢迎回来 👋</h2>
            <p>登录 EduPlatform，继续你的学习之旅</p>
          </div>

          <!-- 登录模式切换 Tab -->
          <div class="login-tabs">
            <div 
              class="tab-item" 
              :class="{ active: loginMode === 'account' }"
              @click="switchMode('account')"
            >账号密码登录</div>
            <div 
              class="tab-item" 
              :class="{ active: loginMode === 'phone' }"
              @click="switchMode('phone')"
            >手机验证码登录</div>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            size="large"
            class="modern-form"
            @keyup.enter="handleLogin"
          >
            <!-- 账号密码模式 -->
            <template v-if="loginMode === 'account'">
              <el-form-item prop="account">
                <el-input
                  v-model="form.account"
                  placeholder="请输入用户名 / 邮箱"
                  :prefix-icon="User"
                  class="modern-input"
                  clearable
                />
              </el-form-item>

              <el-form-item prop="password">
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="请输入登录密码"
                  show-password
                  :prefix-icon="Lock"
                  class="modern-input"
                />
              </el-form-item>
            </template>

            <!-- 手机号短信模式 -->
            <template v-else>
              <el-form-item prop="phone">
                <el-input
                  v-model="form.phone"
                  placeholder="手机号码"
                  :prefix-icon="Phone"
                  class="modern-input"
                  maxlength="11"
                  clearable
                />
              </el-form-item>

              <el-form-item prop="code">
                <div class="code-container">
                  <el-input
                    v-model="form.code"
                    placeholder="短信验证码"
                    :prefix-icon="Message"
                    class="modern-input"
                    maxlength="6"
                  />
                  <el-button
                    class="code-btn"
                    :disabled="countdown > 0"
                    :loading="codeSending"
                    @click="sendCode"
                  >
                    {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
                  </el-button>
                </div>
              </el-form-item>
            </template>

            <div class="form-actions">
              <el-checkbox>保持登录状态</el-checkbox>
              <router-link to="/forgot-password" class="forgot-pwd">忘记密码？</router-link>
            </div>

            <el-button
              type="primary"
              :loading="loading"
              class="submit-btn"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form>

          <div class="register-prompt">
            还没有账号？<router-link to="/register">立即免费注册</router-link>
          </div>

          <div class="divider">
            <span>其他快捷方式</span>
          </div>

          <div class="social-login">
            <button class="social-btn"><el-icon><chat-dot-round /></el-icon>微信扫码</button>
            <button class="social-btn outline"><el-icon><connection-icon /></el-icon>企业微信</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-layout {
  display: flex;
  min-height: 100vh;
  /* 更富层次的低饱和度底色，不再是干瘪的纯白 */
  background-color: #f1f5f9; 
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* -------------------------------------
   Banner 区域 (左侧) 
------------------------------------- */
.auth-banner {
  position: relative;
  flex: 1.1;
  display: none; 
  background-image: url('https://images.unsplash.com/photo-1522202176988-66273c2fd55f?ixlib=rb-4.0.3&auto=format&fit=crop&w=1600&q=80');
  background-size: cover;
  background-position: center;
  overflow: hidden;
}

@media (min-width: 1024px) {
  .auth-banner {
    display: flex;
    flex-direction: column;
  }
}

.banner-overlay {
  position: absolute;
  inset: 0;
  /* 带有温和紫蓝光的渐变覆盖，增加高级感 */
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(99, 102, 241, 0.85) 100%);
  z-index: 1;
}

.banner-content {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 60px 5vw;
  color: #ffffff;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 26px;
  font-weight: 800;
  margin-bottom: auto;
  text-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.logo-icon {
  font-size: 32px;
}

.banner-title {
  font-size: 48px;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 24px;
  max-width: 500px;
  letter-spacing: -0.5px;
}

.banner-desc {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 60px;
  max-width: 480px;
  line-height: 1.7;
}

.testimonial {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 32px;
  max-width: 500px;
  box-shadow: 0 20px 40px -10px rgba(0,0,0,0.1);
}

.quote {
  font-size: 16px;
  line-height: 1.7;
  margin-bottom: 24px;
  color: #f8fafc;
}

.author {
  display: flex;
  align-items: center;
  gap: 16px;
}

.author .avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: url('https://images.unsplash.com/photo-1494790108377-be9c29b29330?ixlib=rb-4.0.3&auto=format&fit=crop&w=150&q=80') center/cover;
}

.author .info {
  display: flex;
  flex-direction: column;
}

.author .name {
  font-weight: 700;
  font-size: 15px;
  color: #ffffff;
}

.author .title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 2px;
}

/* -------------------------------------
   交互面板容器 (右侧) 
------------------------------------- */
.auth-panel-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow-y: auto;
}

@media (min-width: 1024px) {
  .auth-panel-container {
    max-width: 650px;
  }
}

.panel-header {
  padding: 40px 50px 20px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 15px;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.back-link:hover {
  color: #3b82f6;
}

.form-card-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 40px 60px;
}

/* 带有极简投影和弧度的洁白悬浮卡片，打破满屏留白 */
.form-card {
  background: #ffffff;
  border-radius: 24px;
  padding: 48px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 10px 40px -10px rgba(15, 23, 42, 0.08); /* 柔和的高级阴影 */
  border: 1px solid #f8fafc;
}

.form-header {
  margin-bottom: 36px;
  text-align: center;
}

.form-header h2 {
  font-size: 30px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.form-header p {
  font-size: 15px;
  color: #64748b;
}

/* 登录方式切换的 Tab 样式 */
.login-tabs {
  display: flex;
  background-color: #f1f5f9;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 32px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 15px;
  font-weight: 600;
  color: #64748b;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-item.active {
  background-color: #ffffff;
  color: #3b82f6; /* 主题蓝提取突出 */
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.tab-item:hover:not(.active) {
  color: #0f172a;
}

/* 深度优化后的高定输入框 */
:deep(.modern-input .el-input__wrapper) {
  background-color: #f8fafc;
  border-radius: 12px;
  box-shadow: inset 0 0 0 1px #e2e8f0;
  padding: 8px 16px;
  transition: all 0.3s ease;
}

:deep(.modern-input .el-input__wrapper:hover) {
  background-color: #ffffff;
  box-shadow: inset 0 0 0 1px #cbd5e1;
}

:deep(.modern-input .el-input__wrapper.is-focus) {
  background-color: #ffffff;
  box-shadow: inset 0 0 0 2px #3b82f6, 0 4px 14px 0 rgba(59, 130, 246, 0.15) !important;
}

:deep(.modern-input .el-input__inner) {
  height: 46px;
  font-size: 15px;
  color: #0f172a;
}

:deep(.modern-input .el-input__inner::placeholder) {
  color: #94a3b8;
}

/* 验证码内联器 */
.code-container {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-container .modern-input {
  flex: 1;
}

.code-btn {
  width: 140px;
  height: 62px; /* 和 input 的 46+8*2 = 62 一致 */
  border-radius: 12px;
  font-weight: 600;
  font-size: 14px;
  background-color: #e0e7ff;
  border: none;
  color: #4f46e5;
  transition: all 0.2s;
}

.code-btn:hover:not(:disabled) {
  background-color: #c7d2fe;
}

.code-btn:disabled {
  background-color: #f1f5f9;
  color: #94a3b8;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #3b82f6;
  border-color: #3b82f6;
}

:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #3b82f6;
  font-weight: 500;
}

.forgot-pwd {
  font-size: 14px;
  color: #64748b;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-pwd:hover {
  color: #3b82f6;
}

/* 渐变流光高级主按钮 */
.submit-btn {
  width: 100%;
  height: 56px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 14px;
  background: linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%);
  border: none;
  color: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.3);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.45);
}

.submit-btn:active {
  transform: translateY(0);
}

.register-prompt {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: #64748b;
}

.register-prompt a {
  color: #4f46e5;
  font-weight: 700;
  text-decoration: none;
  margin-left: 6px;
}

.register-prompt a:hover {
  text-decoration: underline;
}

.divider {
  display: flex;
  align-items: center;
  text-align: center;
  margin: 36px 0;
  color: #94a3b8;
  font-size: 13px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid #e2e8f0;
}

.divider span {
  padding: 0 16px;
}

.social-login {
  display: flex;
  gap: 16px;
}

.social-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 48px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #475569;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.social-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #0f172a;
}

.social-btn .el-icon {
  font-size: 18px;
  color: #10b981; 
}

.social-btn.outline .el-icon {
  color: #3b82f6;
}

@media (max-width: 768px) {
  .form-card {
    padding: 32px 24px;
    box-shadow: none;
    background: transparent;
    border: none;
  }
  .auth-panel-container {
    background: #ffffff; /* 手机端恢复纯白，节省空间 */
  }
}
</style>

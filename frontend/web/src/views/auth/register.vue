<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Message as MailIcon, Phone, Lock, Message, Unlock } from '@element-plus/icons-vue'

const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const registerMode = ref<'account' | 'phone'>('account')

const form = reactive({
  username: '',
  email: '',
  phone: '',
  code: '',
  password: '',
  confirmPassword: '',
  agree: false
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请设置您的用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入短信验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置登录密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  agree: [
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback(new Error('请阅读并同意协议细则'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
})

const codeSending = ref(false)
const countdown = ref(0)

async function sendCode() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号之后再获取验证码')
    return
  }

  codeSending.value = true
  try {
    // 模拟发送
    await new Promise(resolve => setTimeout(resolve, 800))
    ElMessage.success('验证码已发送，请查收')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch {
    ElMessage.error('获取验证码失败，请重试')
  } finally {
    codeSending.value = false
  }
}

async function handleRegister() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      // 模拟注册完成
      await new Promise(resolve => setTimeout(resolve, 1200))
      ElMessage.success('注册成功，欢迎加入！即刻为您跳转登录')
      router.push('/login')
    } catch (error: any) {
      ElMessage.error(error.message || '系统注册繁忙，请稍后')
    } finally {
      loading.value = false
    }
  })
}

const switchMode = (mode: 'account' | 'phone') => {
  registerMode.value = mode
  formRef.value?.clearValidate()
}
</script>

<template>
  <div class="auth-layout">
    <div class="auth-panel-container">
      <div class="panel-header">
        <router-link to="/" class="back-link">
          <span class="arrow">←</span> 返回首页
        </router-link>
      </div>

      <div class="form-card-wrapper">
        <div class="form-card">
          <div class="form-header">
            <h2>创建账号 🚀</h2>
            <p>加入 EduPlatform，打破技术边界</p>
          </div>

          <!-- 注册模式切换 Tab -->
          <div class="register-tabs">
            <div 
              class="tab-item" 
              :class="{ active: registerMode === 'account' }"
              @click="switchMode('account')"
            >用户名/邮箱注册</div>
            <div 
              class="tab-item" 
              :class="{ active: registerMode === 'phone' }"
              @click="switchMode('phone')"
            >手机号极速注册</div>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            size="large"
            class="modern-form"
          >
            <!-- 邮箱/用户名注册模式 -->
            <template v-if="registerMode === 'account'">
              <el-form-item prop="username">
                <el-input
                  v-model="form.username"
                  placeholder="设置一个响亮的用户名"
                  :prefix-icon="User"
                  class="modern-input"
                  clearable
                />
              </el-form-item>
              
              <el-form-item prop="email">
                <el-input
                  v-model="form.email"
                  placeholder="常用邮箱 (用于找回密码)"
                  :prefix-icon="MailIcon"
                  class="modern-input"
                  clearable
                />
              </el-form-item>
            </template>

            <!-- 手机号注册模式 -->
            <template v-else>
              <el-form-item prop="phone">
                <el-input
                  v-model="form.phone"
                  placeholder="接收验证码的手机号码"
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
                    placeholder="6 位短信数字验证码"
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
                    {{ countdown > 0 ? `${countdown}s 后重试` : '获取验证码' }}
                  </el-button>
                </div>
              </el-form-item>
            </template>

            <!-- 公共密码字段 -->
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="设置登录密码 (建议包含字母与数字)"
                show-password
                :prefix-icon="Lock"
                class="modern-input"
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请确认上方输入的密码"
                show-password
                :prefix-icon="Unlock"
                class="modern-input"
              />
            </el-form-item>

            <el-form-item prop="agree" class="agree-item">
              <el-checkbox v-model="form.agree">
                我已仔细阅读并同意
                <a href="#" class="protocol-link" @click.prevent>《用户服务隐私协议》</a>
              </el-checkbox>
            </el-form-item>

            <el-button
              type="primary"
              :loading="loading"
              class="submit-btn"
              @click="handleRegister"
            >
              同 意 协 议 并 注 册
            </el-button>
          </el-form>

          <div class="login-prompt">
            已有通行证？<router-link to="/login">直达安全登录</router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧：带有科技极客风的高清 Banner -->
    <div class="auth-banner register-banner">
      <div class="banner-overlay"></div>
      <div class="banner-content">
        <div class="logo-area">
          <span class="logo-icon">💡</span>
          <span class="logo-text" style="text-shadow: 0 2px 10px rgba(0,0,0,0.2);">EduPlatform</span>
        </div>
        
        <div class="benefits">
          <h2 class="benefits-title">你注册的不只是账号，也是：</h2>
          <ul class="benefit-list">
            <li>
              <span class="icon">✨</span>
              <div class="text">
                <h3>千万级流量的微服务实战打磨</h3>
                <p>从 0 开始拆解主流大厂复杂架构底座。</p>
              </div>
            </li>
            <li>
              <span class="icon">🤖</span>
              <div class="text">
                <h3>专属 AI RAG 私人学术顾问</h3>
                <p>基于 Spring AI 构建的陪伴对话系统，随时解答疑惑。</p>
              </div>
            </li>
            <li>
              <span class="icon">🧭</span>
              <div class="text">
                <h3>职业素养与大满贯能力内推圈</h3>
                <p>海量学习轨迹数据直逼名企猎头，为你的每一步简历镀金。</p>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 此部分与 Login 的深层重构高度一致 */
.auth-layout {
  display: flex;
  min-height: 100vh;
  /* 柔和护眼的高级灰蓝低饱和度底色 */
  background-color: #f1f5f9; 
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 交互面板容器 (现在固定在左侧了) */
.auth-panel-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow-y: auto;
  z-index: 10;
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

/* 带有极简投影和弧度的洁白悬浮卡片 */
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
  margin-bottom: 32px;
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

/* Tab 切换栏设计 */
.register-tabs {
  display: flex;
  background-color: #f1f5f9;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 28px;
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
  color: #4f46e5;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.tab-item:hover:not(.active) {
  color: #0f172a;
}

/* 表单外观深度优化区 */
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
  box-shadow: inset 0 0 0 2px #4f46e5, 0 4px 14px 0 rgba(79, 70, 229, 0.15) !important;
}

:deep(.modern-input .el-input__inner) {
  height: 46px;
  font-size: 15px;
  color: #0f172a;
}

:deep(.modern-input .el-input__inner::placeholder) {
  color: #94a3b8;
}

/* 验证码组合框 */
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
  height: 62px;
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

.agree-item {
  margin-top: 24px;
  margin-bottom: 24px;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #4f46e5;
  border-color: #4f46e5;
}

:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #4f46e5;
  font-weight: 500;
}

:deep(.el-checkbox__label) {
  font-size: 13px;
  color: #64748b;
}

.protocol-link {
  color: #4f46e5;
  text-decoration: none;
  font-weight: 500;
}

.protocol-link:hover {
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  height: 56px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 14px;
  /* 注册主推靛蓝色调渐变，与登录的科技蓝做少许区分 */
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
  border: none;
  color: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 14px rgba(79, 70, 229, 0.3);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(79, 70, 229, 0.45);
}

.submit-btn:active {
  transform: translateY(0);
}

.login-prompt {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: #64748b;
}

.login-prompt a {
  color: #4f46e5;
  font-weight: 700;
  text-decoration: none;
  margin-left: 6px;
}

.login-prompt a:hover {
  text-decoration: underline;
}

/* Banner 区域 (注册页面置于右边) */
.register-banner {
  position: relative;
  flex: 1.2;
  display: none;
  background-image: url('https://images.unsplash.com/photo-1516321318423-f06f85e504b3?ixlib=rb-4.0.3&auto=format&fit=crop&w=1600&q=80');
  background-size: cover;
  background-position: center;
  overflow: hidden;
}

@media (min-width: 1024px) {
  .register-banner {
    display: flex;
    flex-direction: column;
  }
}

.banner-overlay {
  position: absolute;
  inset: 0;
  /* 带有温和紫蓝光的渐变覆盖 */
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.85) 0%, rgba(79, 70, 229, 0.9) 100%);
  z-index: 1;
}

.banner-content {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 60px 4vw;
  color: #ffffff;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 26px;
  font-weight: 800;
  margin-bottom: 60px;
}

.logo-icon {
  font-size: 32px;
}

.benefits {
  margin-top: auto;
  margin-bottom: auto;
  max-width: 500px;
}

.benefits-title {
  font-size: 34px;
  font-weight: 800;
  margin-bottom: 40px;
  letter-spacing: -0.5px;
  line-height: 1.3;
}

.benefit-list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 36px;
}

.benefit-list li {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.benefit-list .icon {
  font-size: 30px;
  background: rgba(255, 255, 255, 0.1);
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}

.benefit-list .text h3 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #ffffff;
}

.benefit-list .text p {
  font-size: 15.5px;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .form-card {
    padding: 32px 24px;
    box-shadow: none;
    background: transparent;
    border: none;
  }
  .auth-panel-container {
    background: #ffffff;
  }
}
</style>

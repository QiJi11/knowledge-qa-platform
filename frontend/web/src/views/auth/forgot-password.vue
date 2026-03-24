<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Message, Lock, Unlock } from '@element-plus/icons-vue'

const router = useRouter()

// 步骤控制：1 = 输入邮箱  2 = 输入验证码  3 = 设置新密码  4 = 完成
const step = ref(1)

const formRef = ref<FormInstance>()
const loading = ref(false)
const codeSending = ref(false)
const countdown = ref(0)

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = reactive<FormRules>({
  email: [
    { required: true, message: '请输入注册时填写的邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请设置新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 遮罩显示的邮箱
const maskedEmail = computed(() => {
  if (!form.email) return ''
  const parts = form.email.split('@')
  const name = parts[0] || ''
  const domain = parts[1] || ''
  if (!domain) return form.email
  const maskedName = name.length <= 3 
    ? (name[0] || '') + '***' 
    : name.slice(0, 3) + '***'
  return maskedName + '@' + domain
})

// 步骤1：发送验证码到邮箱
async function sendCode() {
  if (!form.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    ElMessage.warning('请输入正确的邮箱地址')
    return
  }

  codeSending.value = true
  try {
    // Mock 发送验证码
    await new Promise(resolve => setTimeout(resolve, 1200))
    ElMessage.success(`验证码已发送至 ${maskedEmail.value}，请查收`)
    step.value = 2
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch {
    ElMessage.error('发送失败，请稍后重试')
  } finally {
    codeSending.value = false
  }
}

// 步骤2：验证验证码
async function verifyCode() {
  if (!form.code || form.code.length !== 6) {
    ElMessage.warning('请输入 6 位数字验证码')
    return
  }

  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 800))
    ElMessage.success('验证通过，请设置新密码')
    step.value = 3
  } catch (error: any) {
    ElMessage.error(error.message || '验证码错误或已过期')
  } finally {
    loading.value = false
  }
}

// 步骤3：重置密码
async function resetPassword() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      // Mock 重置
      await new Promise(resolve => setTimeout(resolve, 1000))
      step.value = 4
    } catch {
      ElMessage.error('密码重置失败，请重试')
    } finally {
      loading.value = false
    }
  })
}
</script>

<template>
  <div class="auth-layout">
    <!-- 左侧 Banner -->
    <div class="auth-banner">
      <div class="banner-overlay"></div>
      <div class="banner-content">
        <div class="logo-area">
          <span class="logo-icon">📚</span>
          <span class="logo-text">EduPlatform</span>
        </div>
        <h1 class="banner-title">安全找回<br/>你的学习账号</h1>
        <p class="banner-desc">通过你注册时留下的邮箱，验证身份后即可重新设置你的登录密码。整个过程安全可靠。</p>

        <div class="security-tips">
          <div class="tip-title">🔒 安全提示</div>
          <ul>
            <li>验证码有效期为 5 分钟</li>
            <li>请勿将验证码分享给他人</li>
            <li>新密码建议包含字母与数字组合</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 右侧操作面板 -->
    <div class="auth-panel-container">
      <div class="panel-header">
        <router-link to="/login" class="back-link">
          <span class="arrow">←</span> 返回登录
        </router-link>
      </div>

      <div class="form-card-wrapper">
        <div class="form-card">

          <!-- 步骤进度条 -->
          <div class="step-progress">
            <div class="step-item" :class="{ active: step >= 1, done: step > 1 }">
              <div class="step-dot">{{ step > 1 ? '✓' : '1' }}</div>
              <span>验证邮箱</span>
            </div>
            <div class="step-line" :class="{ active: step > 1 }"></div>
            <div class="step-item" :class="{ active: step >= 2, done: step > 2 }">
              <div class="step-dot">{{ step > 2 ? '✓' : '2' }}</div>
              <span>输入验证码</span>
            </div>
            <div class="step-line" :class="{ active: step > 2 }"></div>
            <div class="step-item" :class="{ active: step >= 3, done: step > 3 }">
              <div class="step-dot">{{ step > 3 ? '✓' : '3' }}</div>
              <span>重置密码</span>
            </div>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" size="large" class="modern-form">

            <!-- ===== 步骤 1：输入邮箱 ===== -->
            <template v-if="step === 1">
              <div class="step-header">
                <h2>验证你的邮箱 📧</h2>
                <p>我们会向你的注册邮箱发送一封包含 6 位数字验证码的邮件</p>
              </div>

              <el-form-item prop="email">
                <el-input
                  v-model="form.email"
                  placeholder="请输入注册时使用的邮箱地址"
                  :prefix-icon="Message"
                  class="modern-input"
                  clearable
                  @keyup.enter="sendCode"
                />
              </el-form-item>

              <el-button
                type="primary"
                class="submit-btn"
                :loading="codeSending"
                @click="sendCode"
              >
                发送验证码
              </el-button>
            </template>

            <!-- ===== 步骤 2：输入验证码 ===== -->
            <template v-else-if="step === 2">
              <div class="step-header">
                <h2>输入验证码 🔐</h2>
                <p>验证码已发送至 <strong>{{ maskedEmail }}</strong></p>
              </div>

              <el-form-item prop="code">
                <el-input
                  v-model="form.code"
                  placeholder="请输入 6 位数字验证码"
                  class="modern-input code-input"
                  maxlength="6"
                  @keyup.enter="verifyCode"
                />
              </el-form-item>

              <div class="resend-row">
                <span v-if="countdown > 0" class="countdown-text">{{ countdown }}s 后可重新发送</span>
                <a v-else href="#" class="resend-link" @click.prevent="sendCode">重新发送验证码</a>
              </div>

              <el-button
                type="primary"
                class="submit-btn"
                :loading="loading"
                @click="verifyCode"
              >
                验证并继续
              </el-button>
            </template>

            <!-- ===== 步骤 3：设置新密码 ===== -->
            <template v-else-if="step === 3">
              <div class="step-header">
                <h2>设置新密码 🔑</h2>
                <p>请为你的账号设置一个全新的登录密码</p>
              </div>

              <el-form-item prop="newPassword">
                <el-input
                  v-model="form.newPassword"
                  type="password"
                  placeholder="输入新密码 (6-20位)"
                  show-password
                  :prefix-icon="Lock"
                  class="modern-input"
                />
              </el-form-item>

              <el-form-item prop="confirmPassword">
                <el-input
                  v-model="form.confirmPassword"
                  type="password"
                  placeholder="再次确认新密码"
                  show-password
                  :prefix-icon="Unlock"
                  class="modern-input"
                />
              </el-form-item>

              <el-button
                type="primary"
                class="submit-btn"
                :loading="loading"
                @click="resetPassword"
              >
                确认重置密码
              </el-button>
            </template>

            <!-- ===== 步骤 4：完成 ===== -->
            <template v-else>
              <div class="success-area">
                <div class="success-icon">🎉</div>
                <h2>密码重置成功！</h2>
                <p>你的密码已经成功修改。现在可以使用新密码登录系统了。</p>
                <el-button type="primary" class="submit-btn" @click="router.push('/login')">
                  立即登录
                </el-button>
              </div>
            </template>

          </el-form>

        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-layout {
  display: flex;
  min-height: 100vh;
  background-color: #f1f5f9;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* Banner 区域 (左侧) */
.auth-banner {
  position: relative;
  flex: 1.1;
  display: none;
  background-image: url('https://images.unsplash.com/photo-1557683316-973673baf926?ixlib=rb-4.0.3&auto=format&fit=crop&w=1600&q=80');
  background-size: cover;
  background-position: center;
  overflow: hidden;
}

@media (min-width: 1024px) {
  .auth-banner { display: flex; flex-direction: column; }
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.85) 0%, rgba(14, 116, 144, 0.9) 100%);
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
}

.logo-icon { font-size: 32px; }

.banner-title {
  font-size: 46px;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 24px;
  max-width: 500px;
}

.banner-desc {
  font-size: 18px;
  color: rgba(255,255,255,0.9);
  margin-bottom: 60px;
  max-width: 480px;
  line-height: 1.7;
}

.security-tips {
  background: rgba(255,255,255,0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 20px;
  padding: 28px 32px;
  max-width: 420px;
}

.tip-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 16px;
}

.security-tips ul {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.security-tips li {
  font-size: 15px;
  color: rgba(255,255,255,0.85);
  padding-left: 24px;
  position: relative;
}

.security-tips li::before {
  content: '•';
  position: absolute;
  left: 8px;
  color: #34d399;
  font-weight: bold;
}

/* 面板区 */
.auth-panel-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

@media (min-width: 1024px) {
  .auth-panel-container { max-width: 650px; }
}

.panel-header { padding: 40px 50px 20px; }

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

.back-link:hover { color: #3b82f6; }

.form-card-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 40px 60px;
}

.form-card {
  background: #ffffff;
  border-radius: 24px;
  padding: 48px;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 10px 40px -10px rgba(15, 23, 42, 0.08);
  border: 1px solid #f1f5f9;
}

/* 步骤引导条 */
.step-progress {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40px;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.step-item span {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
  transition: color 0.3s;
}

.step-item.active span { color: #0f172a; }
.step-item.done span { color: #10b981; }

.step-dot {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  background: #f1f5f9;
  color: #94a3b8;
  border: 2px solid #e2e8f0;
  transition: all 0.3s;
}

.step-item.active .step-dot {
  background: #eef2ff;
  color: #4f46e5;
  border-color: #4f46e5;
}

.step-item.done .step-dot {
  background: #d1fae5;
  color: #10b981;
  border-color: #10b981;
}

.step-line {
  width: 60px;
  height: 2px;
  background: #e2e8f0;
  margin: 0 12px;
  margin-bottom: 24px;
  transition: background 0.3s;
}

.step-line.active { background: #10b981; }

/* 步骤标题 */
.step-header {
  text-align: center;
  margin-bottom: 32px;
}

.step-header h2 {
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 12px;
}

.step-header p {
  font-size: 15px;
  color: #64748b;
  line-height: 1.6;
}

.step-header strong {
  color: #4f46e5;
}

/* 输入框 */
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

:deep(.modern-input .el-input__inner::placeholder) { color: #94a3b8; }

/* 验证码输入框加大字号居中（并修复 Placeholder 间距过大问题） */
:deep(.code-input .el-input__inner) {
  font-size: 24px;
  text-align: center;
  font-weight: 600;
}
/* 仅在有值时拉大间距，不在 placeholder 时拉大 */
:deep(.code-input .el-input__inner:not(:placeholder-shown)) {
  letter-spacing: 12px;
}

/* 重发验证码行 */
.resend-row {
  text-align: center;
  margin-bottom: 24px;
}

.countdown-text {
  font-size: 14px;
  color: #94a3b8;
}

.resend-link {
  font-size: 14px;
  color: #4f46e5;
  text-decoration: none;
  font-weight: 600;
}

.resend-link:hover { text-decoration: underline; }

/* 主按钮 */
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

.submit-btn:active { transform: translateY(0); }

/* 完成区域 */
.success-area {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 64px;
  margin-bottom: 24px;
  animation: bounce 0.6s ease;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

.success-area h2 {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 16px;
}

.success-area p {
  font-size: 15px;
  color: #64748b;
  margin-bottom: 36px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .form-card {
    padding: 32px 24px;
    box-shadow: none;
    background: transparent;
    border: none;
  }
  .auth-panel-container { background: #ffffff; }
  .step-line { width: 40px; }
}
</style>

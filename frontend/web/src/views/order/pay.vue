<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail, payOrder } from '@/api/order'
import { createLearningTask } from '@/api/learningTask'

const route = useRoute()
const router = useRouter()

const orderNo = computed(() => route.params.orderNo as string)
const loading = ref(false)
const pageLoading = ref(true)
const payStatus = ref<'pending' | 'success' | 'failed'>('pending')
const countdown = ref(30 * 60)
const orderAmount = ref(0)
const courseTitle = ref('')

let timer: number | null = null

onMounted(async () => {
  try {
    const res: any = await getOrderDetail(orderNo.value)
    const order = res.data
    orderAmount.value = order.amount
    courseTitle.value = order.courseTitle || '课程'
    if (order.status === 1) {
      payStatus.value = 'success'
    }
  } catch {
    ElMessage.error('获取订单信息失败')
  } finally {
    pageLoading.value = false
  }

  timer = window.setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer!)
      ElMessage.warning('订单已超时')
      router.push('/user/orders')
    }
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function formatCountdown(seconds: number): string {
  const min = Math.floor(seconds / 60)
  const sec = seconds % 60
  return `${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`
}

async function handlePay() {
  loading.value = true
  try {
    const payRes: any = await payOrder({ orderNo: orderNo.value, payType: 'alipay' })
    payStatus.value = 'success'
    ElMessage.success('支付成功！')

    // 支付成功后自动创建学习任务
    try {
      // payOrder 返回: { data: { success: true, order: { courseId, userId, ... } } }
      // 经过 axios 拦截器后 payRes = { code:0, data: { success, order: {...} } }
      const order = payRes.data?.order || payRes.order || payRes
      const userId = order.userId || Number(localStorage.getItem('userId')) || 1
      await createLearningTask(userId, {
        courseId: order.courseId,
        title: `学习：${courseTitle.value}`,
        priority: 2,
        dueDate: null,
        note: null,
      })
    } catch {
      // 学习任务创建失败不影响支付结果
      console.warn('自动创建学习任务失败，可手动添加')
    }
  } catch {
    payStatus.value = 'failed'
    ElMessage.error('支付失败')
  } finally {
    loading.value = false
  }
}

function goToMyCourses() { router.push('/user/courses') }
function goBack() { router.push('/user/orders') }
</script>

<template>
  <div class="order-pay-page">
    <div class="container">
      <div class="pay-content">
        <div v-if="pageLoading" style="text-align:center;padding:80px 0;color:#999;">加载中...</div>

        <!-- 待支付状态 -->
        <template v-else-if="payStatus === 'pending'">
          <div class="pay-header">
            <div class="warning-icon">⏳</div>
            <div class="info">
              <h2>订单待支付</h2>
              <p>请在 <span class="countdown">{{ formatCountdown(countdown) }}</span> 内完成支付</p>
            </div>
          </div>

          <div class="order-info">
            <div class="info-row">
              <span class="label">订单号</span>
              <span class="value">{{ orderNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">课程</span>
              <span class="value">{{ courseTitle }}</span>
            </div>
            <div class="info-row">
              <span class="label">应付金额</span>
              <span class="value price">¥{{ orderAmount }}</span>
            </div>
          </div>

          <div class="qrcode-area">
            <div class="qrcode-placeholder">
              <div style="font-size:48px">📱</div>
              <p>模拟支付</p>
            </div>
            <p class="tip">点击下方按钮模拟支付成功</p>
          </div>

          <div class="action-area">
            <button class="pay-btn" :disabled="loading" @click="handlePay">
              {{ loading ? '支付中...' : '模拟支付' }}
            </button>
            <button class="cancel-btn" @click="goBack">取消支付</button>
          </div>
        </template>

        <!-- 支付成功状态 -->
        <template v-else-if="payStatus === 'success'">
          <div class="result-section success">
            <div class="result-icon">✅</div>
            <h2>支付成功</h2>
            <p>您已成功购买课程，快去学习吧！</p>
            <div class="result-actions">
              <button class="pay-btn" @click="goToMyCourses">开始学习</button>
              <button class="cancel-btn" @click="router.push('/courses')">继续选课</button>
            </div>
          </div>
        </template>

        <!-- 支付失败状态 -->
        <template v-else>
          <div class="result-section failed">
            <div class="result-icon">❌</div>
            <h2>支付失败</h2>
            <p>支付遇到问题，请重试</p>
            <div class="result-actions">
              <button class="pay-btn" @click="payStatus = 'pending'">重新支付</button>
              <button class="cancel-btn" @click="goBack">返回订单</button>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-pay-page { padding: 48px 0; min-height: calc(100vh - 200px); }
.pay-content {
  max-width: 500px; margin: 0 auto;
  background: #fff; border-radius: 16px; padding: 48px;
}
.pay-header { display: flex; gap: 16px; margin-bottom: 32px; }
.warning-icon { font-size: 48px; }
.pay-header h2 { font-size: 20px; margin-bottom: 8px; }
.pay-header p { color: #666; }
.countdown { color: #ff4d4f; font-weight: 600; }
.order-info { background: #f9fafb; border-radius: 8px; padding: 16px; margin-bottom: 32px; }
.info-row { display: flex; justify-content: space-between; padding: 8px 0; }
.info-row .label { color: #666; }
.info-row .price { font-size: 24px; font-weight: 600; color: #ff4d4f; }
.qrcode-area { text-align: center; margin-bottom: 32px; }
.qrcode-placeholder {
  width: 200px; height: 200px; border: 2px dashed #e8e8e8;
  border-radius: 12px; margin: 0 auto 16px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: #999;
}
.tip { font-size: 13px; color: #999; }
.action-area { display: flex; gap: 16px; justify-content: center; }
.pay-btn {
  padding: 12px 32px; font-size: 16px; background: #4f46e5; color: white;
  border: none; border-radius: 8px; cursor: pointer;
}
.pay-btn:hover { background: #4338ca; }
.pay-btn:disabled { background: #a5a3d8; cursor: not-allowed; }
.cancel-btn {
  padding: 12px 32px; font-size: 16px; background: #f3f4f6; color: #333;
  border: none; border-radius: 8px; cursor: pointer;
}
.cancel-btn:hover { background: #e5e7eb; }
.result-section { text-align: center; padding: 32px 0; }
.result-icon { font-size: 72px; margin-bottom: 24px; }
.result-section h2 { font-size: 24px; margin-bottom: 8px; }
.result-section p { color: #666; margin-bottom: 32px; }
.result-actions { display: flex; gap: 16px; justify-content: center; }
</style>

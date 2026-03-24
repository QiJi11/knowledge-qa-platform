<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCourseDetail, type Course } from '@/api/course'
import { createOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()

const courseId = computed(() => route.params.courseId as string)
const loading = ref(false)
const pageLoading = ref(true)
const course = ref<Course | null>(null)
const payType = ref<'alipay' | 'wechat'>('alipay')

onMounted(async () => {
  try {
    const courseData: any = await getCourseDetail(courseId.value)
    course.value = courseData
  } catch {
    ElMessage.error('获取课程信息失败')
  } finally {
    pageLoading.value = false
  }
})

async function handleSubmit() {
  if (!course.value) return
  loading.value = true
  try {
    const res: any = await createOrder({ courseId: course.value.id })
    const orderNo = res.data.orderNo
    ElMessage.success('订单创建成功')
    router.push(`/order/pay/${orderNo}`)
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '创建订单失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="order-confirm-page">
    <div class="container">
      <div class="page-header">
        <h1>确认订单</h1>
      </div>

      <div v-if="pageLoading" style="text-align:center;padding:80px 0;color:#999;">加载中...</div>

      <div v-else-if="course" class="order-content">
        <!-- 课程信息 -->
        <div class="section course-section">
          <h3>课程信息</h3>
          <div class="course-item">
            <img :src="course.coverUrl" class="course-cover" />
            <div class="course-info">
              <h4>{{ course.title }}</h4>
              <p>{{ course.category }} · {{ ['入门', '进阶', '高级'][course.level - 1] }}</p>
            </div>
            <div class="course-price">
              <span class="price">¥{{ course.price }}</span>
              <span class="original" v-if="course.originalPrice && course.originalPrice > (course.price || 0)">
                ¥{{ course.originalPrice }}
              </span>
            </div>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="section pay-section">
          <h3>支付方式</h3>
          <div class="pay-options">
            <div class="pay-option" :class="{ active: payType === 'alipay' }" @click="payType = 'alipay'">
              <div class="pay-icon alipay">支</div>
              <span>支付宝</span>
              <span v-if="payType === 'alipay'" class="check">✓</span>
            </div>
            <div class="pay-option" :class="{ active: payType === 'wechat' }" @click="payType = 'wechat'">
              <div class="pay-icon wechat">微</div>
              <span>微信支付</span>
              <span v-if="payType === 'wechat'" class="check">✓</span>
            </div>
          </div>
        </div>

        <!-- 订单汇总 -->
        <div class="section summary-section">
          <div class="summary-row">
            <span>课程价格</span>
            <span>¥{{ course.price }}</span>
          </div>
          <div class="summary-row" v-if="course.originalPrice && course.originalPrice > (course.price || 0)">
            <span>优惠</span>
            <span class="discount">-¥{{ ((course.originalPrice || 0) - (course.price || 0)).toFixed(2) }}</span>
          </div>
          <hr style="border: none; border-top: 1px solid #eee; margin: 12px 0;" />
          <div class="summary-row total">
            <span>应付金额</span>
            <span class="total-price">¥{{ course.price }}</span>
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="submit-area">
          <button class="submit-btn" :disabled="loading" @click="handleSubmit">
            {{ loading ? '提交中...' : '提交订单' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-confirm-page {
  padding: 24px 0;
  background: #f5f7fa;
  min-height: calc(100vh - 200px);
}
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 24px; }
.order-content { max-width: 800px; }
.section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
}
.section h3 { font-size: 16px; margin-bottom: 16px; }
.course-item { display: flex; gap: 16px; align-items: center; }
.course-cover { width: 160px; height: 90px; border-radius: 8px; object-fit: cover; }
.course-info { flex: 1; }
.course-info h4 { font-size: 16px; margin-bottom: 8px; }
.course-info p { font-size: 13px; color: #666; }
.course-price { text-align: right; }
.course-price .price { font-size: 20px; font-weight: 600; color: #ff4d4f; }
.course-price .original { display: block; font-size: 14px; color: #999; text-decoration: line-through; }
.pay-options { display: flex; gap: 16px; }
.pay-option {
  display: flex; align-items: center; gap: 12px;
  padding: 16px 24px; border: 2px solid #e8e8e8;
  border-radius: 8px; cursor: pointer; transition: all 0.3s;
  position: relative;
}
.pay-option.active { border-color: #4f46e5; background: #f0f5ff; }
.pay-icon {
  width: 32px; height: 32px; border-radius: 4px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-weight: 600;
}
.pay-icon.alipay { background: #1677ff; }
.pay-icon.wechat { background: #07c160; }
.pay-option .check { position: absolute; top: 8px; right: 8px; color: #4f46e5; font-weight: bold; }
.summary-row { display: flex; justify-content: space-between; margin-bottom: 12px; font-size: 14px; }
.summary-row.total { margin-bottom: 0; font-size: 16px; font-weight: 500; }
.discount { color: #ff4d4f; }
.total-price { font-size: 24px; font-weight: 600; color: #ff4d4f; }
.submit-area { text-align: right; padding: 24px; background: #fff; border-radius: 12px; }
.submit-btn {
  padding: 12px 48px; font-size: 16px; background: #4f46e5; color: white;
  border: none; border-radius: 8px; cursor: pointer; transition: background 0.3s;
}
.submit-btn:hover { background: #4338ca; }
.submit-btn:disabled { background: #a5a3d8; cursor: not-allowed; }
</style>

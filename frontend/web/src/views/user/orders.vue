<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { Order } from '@/types/order'
import { OrderStatus } from '@/types/order'

const router = useRouter()
const activeTab = ref('all')

// 订单状态文本映射
const statusTextMap: Record<number, string> = {
  [OrderStatus.PENDING]: '待支付',
  [OrderStatus.PAID]: '已完成',
  [OrderStatus.CANCELLED]: '已取消',
  [OrderStatus.REFUNDED]: '已退款'
}

// 订单状态类型映射
const statusTypeMap: Record<number, string> = {
  [OrderStatus.PENDING]: 'warning',
  [OrderStatus.PAID]: 'success',
  [OrderStatus.CANCELLED]: 'info',
  [OrderStatus.REFUNDED]: 'danger'
}

// 模拟订单数据
const orders = ref<Order[]>([
  {
    id: 1,
    orderNo: '202401150001',
    userId: 1,
    courseId: 1,
    course: {
      id: 1,
      title: 'Spring Cloud Alibaba 微服务架构实战',
      coverUrl: 'https://picsum.photos/120/68?random=40',
      price: 299
    },
    amount: 299,
    status: OrderStatus.PAID,
    payTime: '2024-01-15 14:30:00',
    createdAt: '2024-01-15 14:25:00',
    updatedAt: '2024-01-15 14:30:00'
  },
  {
    id: 2,
    orderNo: '202401200002',
    userId: 1,
    courseId: 2,
    course: {
      id: 2,
      title: 'Vue3 + TypeScript 从入门到企业级实战',
      coverUrl: 'https://picsum.photos/120/68?random=41',
      price: 199
    },
    amount: 199,
    status: OrderStatus.PENDING,
    createdAt: '2024-01-20 10:00:00',
    updatedAt: '2024-01-20 10:00:00'
  }
])

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待支付', value: 'pending' },
  { label: '已完成', value: 'paid' }
]

function formatDate(dateStr: string) {
  return dateStr.replace('T', ' ').slice(0, 19)
}

function goToPay(orderNo: string) {
  router.push(`/order/pay/${orderNo}`)
}

function goToCourse(courseId: number) {
  router.push(`/course/${courseId}`)
}
</script>

<template>
  <div class="my-orders">
    <h2 class="page-title">我的订单</h2>

    <el-tabs v-model="activeTab">
      <el-tab-pane
        v-for="tab in tabs"
        :key="tab.value"
        :label="tab.label"
        :name="tab.value"
      >
        <div class="order-list" v-if="orders.length > 0">
          <div v-for="order in orders" :key="order.id" class="order-item">
            <div class="order-header">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ formatDate(order.createdAt) }}</span>
              <el-tag :type="statusTypeMap[order.status]" size="small">
                {{ statusTextMap[order.status] }}
              </el-tag>
            </div>
            <div class="order-content">
              <div class="course-info" @click="goToCourse(order.course!.id)">
                <img :src="order.course?.coverUrl" class="course-cover" />
                <div class="course-detail">
                  <h4>{{ order.course?.title }}</h4>
                </div>
              </div>
              <div class="order-amount">
                <span class="label">订单金额</span>
                <span class="price">¥{{ order.amount }}</span>
              </div>
              <div class="order-action">
                <template v-if="order.status === OrderStatus.PENDING">
                  <el-button type="primary" size="small" @click="goToPay(order.orderNo)">
                    立即支付
                  </el-button>
                  <el-button size="small">取消订单</el-button>
                </template>
                <template v-else-if="order.status === OrderStatus.PAID">
                  <el-button type="primary" size="small" @click="goToCourse(order.course!.id)">
                    开始学习
                  </el-button>
                </template>
              </div>
            </div>
          </div>
        </div>

        <el-empty v-else description="暂无订单" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.my-orders {
  min-height: 400px;
}

.page-title {
  font-size: 18px;
  margin-bottom: 24px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.order-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #f9fafb;
  font-size: 13px;
}

.order-no {
  color: var(--text-secondary);
}

.order-time {
  color: var(--text-secondary);
  margin-left: auto;
}

.order-content {
  display: flex;
  align-items: center;
  padding: 16px;
  gap: 24px;
}

.course-info {
  display: flex;
  gap: 12px;
  flex: 1;
  cursor: pointer;
}

.course-cover {
  width: 120px;
  height: 68px;
  border-radius: 4px;
  object-fit: cover;
}

.course-detail h4 {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
}

.course-detail h4:hover {
  color: var(--primary-color);
}

.order-amount {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 24px;
  border-left: 1px solid var(--border-color);
}

.order-amount .label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.order-amount .price {
  font-size: 18px;
  font-weight: 600;
  color: #ff4d4f;
}

.order-action {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-left: 24px;
  border-left: 1px solid var(--border-color);
}
</style>

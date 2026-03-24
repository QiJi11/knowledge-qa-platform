import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页 - 学习平台' }
  },
  {
    path: '/courses',
    name: 'CourseList',
    component: () => import('@/views/CourseList.vue'),
    meta: { title: '课程列表 - 学习平台' }
  },
  {
    path: '/course/:id',
    name: 'CourseDetail',
    component: () => import('@/views/CourseDetail.vue'),
    meta: { title: '课程详情 - 学习平台' }
  },
  {
    path: '/plan',
    name: 'LearningPlan',
    component: () => import('@/views/LearningPlan.vue'),
    meta: { title: '学习计划 - 学习平台' }
  },
  {
    path: '/stats',
    name: 'LearningStats',
    component: () => import('@/views/LearningStats.vue'),
    meta: { title: '学习统计 - 学习平台' }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/Chat.vue'),
    meta: { title: 'AI 知识问答 - 学习平台' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/login.vue'),
    meta: { title: '登录 - 学习平台' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/register.vue'),
    meta: { title: '注册 - 学习平台' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/auth/forgot-password.vue'),
    meta: { title: '找回密码 - 学习平台' }
  },
  {
    path: '/order/confirm/:courseId',
    name: 'OrderConfirm',
    component: () => import('@/views/order/confirm.vue'),
    meta: { title: '确认订单 - 学习平台' }
  },
  {
    path: '/order/pay/:orderNo',
    name: 'OrderPay',
    component: () => import('@/views/order/pay.vue'),
    meta: { title: '订单支付 - 学习平台' }
  },
  {
    path: '/user/orders',
    name: 'UserOrders',
    component: () => import('@/views/user/orders.vue'),
    meta: { title: '我的订单 - 学习平台' }
  },
  {
    path: '/user/courses',
    name: 'UserCourses',
    component: () => import('@/views/user/courses.vue'),
    meta: { title: '我的课程 - 学习平台' }
  },
  // ========== 管理员后台 ==========
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/AdminDashboard.vue'),
    meta: { title: '管理后台 - 学习平台', requiresAdmin: true },
    children: [
      {
        path: 'courses',
        name: 'AdminCourses',
        component: () => import('@/views/admin/CourseManage.vue'),
        meta: { title: '课程管理 - 学习平台', requiresAdmin: true }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理 - 学习平台', requiresAdmin: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫：管理员权限校验
router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAdmin) {
    const role = localStorage.getItem('role')
    if (role !== 'admin') {
      next('/')
      return
    }
  }
  next()
})

router.afterEach((to) => {
  const title = typeof to.meta.title === 'string' ? to.meta.title : '学习平台'
  document.title = title
})

export default router


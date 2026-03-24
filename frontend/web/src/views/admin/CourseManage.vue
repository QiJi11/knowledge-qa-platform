<template>
  <div class="course-manage">
    <div class="manage-header">
      <h1 class="page-header">课程管理</h1>
      <button class="btn btn-primary" @click="openAddDialog">+ 新增课程</button>
    </div>

    <!-- 搜索筛选 -->
    <div class="filter-bar">
      <input v-model="keyword" placeholder="搜索课程标题..." class="filter-input" @keyup.enter="loadCourses" />
      <select v-model="filterCategory" class="filter-select" @change="loadCourses">
        <option value="">全部分类</option>
        <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
      </select>
      <button class="btn btn-secondary" @click="loadCourses">搜索</button>
    </div>

    <!-- 课程表格 -->
    <div class="table-card">
      <div v-if="loading" class="loading-text">加载中...</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>分类</th>
            <th>难度</th>
            <th>价格</th>
            <th>浏览量</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in courses" :key="c.id">
            <td>{{ c.id }}</td>
            <td class="title-cell">{{ c.title }}</td>
            <td>{{ c.category }}</td>
            <td>{{ levelText(c.level) }}</td>
            <td class="price">¥{{ c.price }}</td>
            <td>{{ c.viewCount }}</td>
            <td>
              <span class="badge" :class="statusClass(c.status)">{{ statusText(c.status) }}</span>
            </td>
            <td>
              <div class="ops">
                <button class="op-btn edit" @click="openEditDialog(c)">编辑</button>
                <button class="op-btn" :class="c.status === 1 ? 'warn' : 'success'" @click="toggleStatus(c)">
                  {{ c.status === 1 ? '下架' : '上架' }}
                </button>
                <button class="op-btn danger" @click="deleteCourse(c)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="!loading && courses.length === 0" class="empty-text">暂无课程数据</div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showDialog" class="dialog-overlay" @click.self="showDialog = false">
      <div class="dialog">
        <h2>{{ editingId ? '编辑课程' : '新增课程' }}</h2>
        <div class="form-grid">
          <div class="field">
            <label>标题 *</label>
            <input v-model="form.title" placeholder="课程标题" />
          </div>
          <div class="field">
            <label>分类 *</label>
            <select v-model="form.category">
              <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
            </select>
          </div>
          <div class="field">
            <label>难度 *</label>
            <select v-model.number="form.level">
              <option :value="1">入门</option>
              <option :value="2">进阶</option>
              <option :value="3">高级</option>
            </select>
          </div>
          <div class="field">
            <label>价格</label>
            <input v-model.number="form.price" type="number" placeholder="0" />
          </div>
          <div class="field full">
            <label>简介</label>
            <input v-model="form.summary" placeholder="课程简介" />
          </div>
          <div class="field">
            <label>总课时</label>
            <input v-model.number="form.totalLessons" type="number" placeholder="0" />
          </div>
          <div class="field">
            <label>总时长(分钟)</label>
            <input v-model.number="form.totalMinutes" type="number" placeholder="0" />
          </div>
        </div>
        <div class="dialog-actions">
          <button class="btn btn-secondary" @click="showDialog = false">取消</button>
          <button class="btn btn-primary" @click="submitForm" :disabled="submitting">
            {{ submitting ? '提交中...' : '确认' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const token = localStorage.getItem('token') || ''
const headers = { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }

const loading = ref(false)
const submitting = ref(false)
const showDialog = ref(false)
const editingId = ref<number | null>(null)
const keyword = ref('')
const filterCategory = ref('')

const categories = ['后端开发', 'Java', '前端开发', 'Python', '数据库', '大数据', '人工智能', '云计算', '移动开发']

interface CourseRow {
  id: number; title: string; category: string; level: number
  price: number; viewCount: number; status: number
  summary?: string; totalLessons?: number; totalMinutes?: number
}

const courses = ref<CourseRow[]>([])

const form = reactive({
  title: '', category: '后端开发', level: 2,
  price: 0, summary: '', totalLessons: 0, totalMinutes: 0
})

function levelText(l: number) {
  return l === 1 ? '入门' : l === 2 ? '进阶' : '高级'
}

function statusText(s: number) {
  return s === 0 ? '草稿' : s === 1 ? '已发布' : '已下线'
}

function statusClass(s: number) {
  return s === 0 ? 'draft' : s === 1 ? 'published' : 'offline'
}

async function loadCourses() {
  loading.value = true
  try {
    const params = new URLSearchParams({ page: '1', pageSize: '50' })
    if (keyword.value) params.set('keyword', keyword.value)
    if (filterCategory.value) params.set('category', filterCategory.value)
    const res = await fetch(`/api/v1/courses?${params}`)
    const result = await res.json()
    if (result.code === 0) {
      courses.value = result.data.items
    }
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  editingId.value = null
  form.title = ''; form.category = '后端开发'; form.level = 2
  form.price = 0; form.summary = ''; form.totalLessons = 0; form.totalMinutes = 0
  showDialog.value = true
}

function openEditDialog(c: CourseRow) {
  editingId.value = c.id
  form.title = c.title; form.category = c.category; form.level = c.level
  form.price = c.price; form.summary = c.summary || ''
  form.totalLessons = c.totalLessons || 0; form.totalMinutes = c.totalMinutes || 0
  showDialog.value = true
}

async function submitForm() {
  if (!form.title.trim()) { ElMessage.warning('请填写标题'); return }
  submitting.value = true
  try {
    const body = JSON.stringify({
      title: form.title, category: form.category, level: form.level,
      summary: form.summary || null, totalLessons: form.totalLessons, totalMinutes: form.totalMinutes, status: 1
    })
    const url = editingId.value ? `/api/v1/courses/${editingId.value}` : '/api/v1/courses'
    const method = editingId.value ? 'PUT' : 'POST'
    const res = await fetch(url, { method, headers, body })
    const result = await res.json()
    if (result.code === 0) {
      ElMessage.success(editingId.value ? '修改成功' : '创建成功')
      showDialog.value = false
      loadCourses()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(c: CourseRow) {
  const newStatus = c.status === 1 ? 2 : 1
  const res = await fetch(`/api/v1/courses/${c.id}`, {
    method: 'PUT', headers,
    body: JSON.stringify({ title: c.title, category: c.category, level: c.level, status: newStatus })
  })
  const result = await res.json()
  if (result.code === 0) {
    ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
    loadCourses()
  }
}

async function deleteCourse(c: CourseRow) {
  try {
    await ElMessageBox.confirm(`确定删除课程「${c.title}」吗？`, '确认删除', { type: 'warning' })
  } catch { return }

  const res = await fetch(`/api/v1/courses/${c.id}`, { method: 'DELETE', headers })
  const result = await res.json()
  if (result.code === 0) {
    ElMessage.success('已删除')
    loadCourses()
  } else {
    ElMessage.error(result.message || '删除失败')
  }
}

onMounted(loadCourses)
</script>

<style scoped>
.manage-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;
}
.page-header { font-size: 24px; font-weight: 700; }

.filter-bar {
  display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap;
}
.filter-input, .filter-select {
  border: 1px solid #ddd; border-radius: 8px; padding: 10px 14px; font-size: 14px; outline: none;
}
.filter-input { flex: 1; min-width: 200px; }
.filter-select { min-width: 140px; }

.btn { padding: 10px 20px; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; border: none; transition: all 0.2s; }
.btn-primary { background: var(--primary-color, #4361ee); color: #fff; }
.btn-primary:hover { opacity: 0.9; }
.btn-secondary { background: #eef2ff; color: #1f2a37; }

.table-card {
  background: #fff; border-radius: 16px; padding: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); overflow: auto;
}
.data-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.data-table th { text-align: left; padding: 12px 10px; border-bottom: 2px solid #e5e7eb; color: var(--text-secondary, #666); font-weight: 600; white-space: nowrap; }
.data-table td { padding: 14px 10px; border-bottom: 1px solid #f0f0f0; vertical-align: middle; }
.title-cell { max-width: 240px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-weight: 600; }
.price { color: #e74c3c; font-weight: 600; }

.badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 12px; font-weight: 500; }
.badge.published { background: #ecfdf5; color: #047857; }
.badge.draft { background: #eef2ff; color: #4361ee; }
.badge.offline { background: #fef2f2; color: #b91c1c; }

.ops { display: flex; gap: 8px; white-space: nowrap; }
.op-btn { padding: 4px 12px; border-radius: 6px; font-size: 13px; cursor: pointer; border: none; transition: all 0.2s; font-weight: 500; }
.op-btn.edit { background: #eef2ff; color: #4361ee; }
.op-btn.success { background: #ecfdf5; color: #047857; }
.op-btn.warn { background: #fffbeb; color: #d97706; }
.op-btn.danger { background: #fef2f2; color: #b91c1c; }
.op-btn:hover { opacity: 0.8; }

.loading-text, .empty-text { padding: 40px 0; text-align: center; color: var(--text-tertiary, #999); }

/* 弹窗 */
.dialog-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.dialog {
  background: #fff; border-radius: 16px; padding: 28px 32px; width: 560px; max-width: 90vw; max-height: 80vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
}
.dialog h2 { font-size: 20px; font-weight: 700; margin-bottom: 20px; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field.full { grid-column: 1 / -1; }
.field label { font-size: 13px; color: var(--text-secondary, #666); font-weight: 500; }
.field input, .field select {
  border: 1px solid #ddd; border-radius: 8px; padding: 10px 12px; font-size: 14px; outline: none; transition: border-color 0.2s;
}
.field input:focus, .field select:focus { border-color: var(--primary-color, #4361ee); }
.dialog-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 24px; }
</style>

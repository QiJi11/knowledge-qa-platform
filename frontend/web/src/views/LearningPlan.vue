<template>
  <div class="plan-page">
    <div class="container">
      <h1 class="page-title">我的学习计划</h1>

      <!-- 未登录提示 -->
      <div v-if="!isLoggedIn" class="card login-prompt">
        <p class="login-text">🔒 请先登录后再查看和管理学习计划</p>
        <button class="btn btn-primary" @click="router.push('/login')">去登录</button>
      </div>

      <div v-if="isLoggedIn" class="card">
        <h2 class="card-title">{{ editTaskId ? '编辑任务' : '创建任务' }}</h2>

        <div class="form-grid">
          <label class="field">
            <span class="label">关联课程</span>
            <select v-model.number="form.courseId" class="input">
              <option :value="0">请选择课程</option>
              <option v-for="c in courses" :key="c.id" :value="c.id">
                {{ c.title }}
              </option>
            </select>
          </label>

          <label class="field">
            <span class="label">任务标题</span>
            <input v-model="form.title" class="input" placeholder="例如：完成第1章学习" />
          </label>

          <label class="field">
            <span class="label">优先级</span>
            <select v-model.number="form.priority" class="input">
              <option :value="1">低</option>
              <option :value="2">中</option>
              <option :value="3">高</option>
            </select>
          </label>

          <label class="field">
            <span class="label">截止日期</span>
            <el-date-picker
              v-model="form.dueDate"
              type="date"
              placeholder="请选择日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </label>

          <label class="field field-full">
            <span class="label">备注（可选）</span>
            <textarea v-model="form.note" class="input textarea" rows="2" placeholder="例如：重点看状态机设计" />
          </label>
        </div>

        <div class="actions">
          <button class="btn btn-primary" :disabled="loading" @click="submitForm">
            {{ editTaskId ? '保存修改' : '创建任务' }}
          </button>
          <button v-if="editTaskId" class="btn btn-secondary" :disabled="loading" @click="cancelEdit">
            取消编辑
          </button>
          <button class="btn btn-ghost" :disabled="loading" @click="refresh">
            刷新列表
          </button>
        </div>

        <div class="tip-box">
          <p class="tip">
            💡 <strong>说明：</strong>点击【开始】后任务变为进行中，可以标记完成或取消。<br/>
            已完成的任务可以恢复进行中。已取消的任务无法再操作。
          </p>
        </div>
      </div>

      <div v-if="isLoggedIn" class="card mt">
        <div class="card-head">
          <h2 class="card-title">我的任务</h2>
          <span class="muted">共 {{ tasks.length }} 条</span>
        </div>

        <div v-if="loading" class="muted">加载中...</div>

        <div v-else-if="tasks.length === 0" class="empty">
          暂无任务。你可以先创建一条任务试试。
        </div>

        <div v-else class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>任务</th>
                <th>课程</th>
                <th>状态</th>
                <th>优先级</th>
                <th>截止</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="t in tasks" :key="t.id">
                <td class="title-cell">
                  <div class="title">{{ t.title }}</div>
                  <div v-if="t.note" class="note">{{ t.note }}</div>
                </td>
                <td class="mono">{{ courseTitle(t.courseId) }}</td>
                <td>
                  <span class="badge" :class="statusClass(t.status)">{{ statusText(t.status) }}</span>
                </td>
                <td>{{ priorityText(t.priority) }}</td>
                <td class="mono">{{ t.dueDate || '-' }}</td>
                <td>
                  <div class="ops">
                    <el-button type="primary" link size="small" @click="startEdit(t)">编辑</el-button>
                    <el-button type="danger" link size="small" @click="removeTask(t)">删除</el-button>

                    <el-button v-if="t.status === 0" type="success" link size="small" @click="setStatus(t, 1)">开始</el-button>
                    <el-button v-if="t.status === 0" type="danger" link size="small" @click="setStatus(t, 3)">取消</el-button>

                    <el-button v-if="t.status === 1" type="success" link size="small" @click="setStatus(t, 2)">完成</el-button>
                    <el-button v-if="t.status === 1" type="danger" link size="small" @click="setStatus(t, 3)">取消</el-button>

                    <el-button v-if="t.status === 2" type="warning" link size="small" @click="setStatus(t, 1)">恢复进行中</el-button>

                    <span v-if="t.status === 3" class="muted ml-2">已取消</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCourseList, type Course } from '@/api/course'
import {
  createLearningTask,
  deleteLearningTask,
  listLearningTasks,
  patchLearningTask,
  type LearningTask,
} from '@/api/learningTask'

const learnerId = computed(() => Number(localStorage.getItem('userId')) || 1)
const router = useRouter()

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const loading = ref(false)
const courses = ref<Course[]>([])
const tasks = ref<LearningTask[]>([])

const editTaskId = ref<number | null>(null)

const form = reactive({
  courseId: 0,
  title: '',
  priority: 2,
  dueDate: '',
  note: '',
})

function statusText(status: number) {
  if (status === 0) return '待开始'
  if (status === 1) return '进行中'
  if (status === 2) return '已完成'
  return '已取消'
}

function statusClass(status: number) {
  if (status === 0) return 's0'
  if (status === 1) return 's1'
  if (status === 2) return 's2'
  return 's3'
}

function priorityText(priority: number) {
  if (priority === 1) return '低'
  if (priority === 2) return '中'
  return '高'
}

function courseTitle(courseId: number) {
  const c = courses.value.find((x) => x.id === courseId)
  return c ? c.title : `#${courseId}`
}

async function refresh() {
  loading.value = true
  try {
    const [courseRes, taskRes] = await Promise.all([
      getCourseList({ page: 1, pageSize: 50, sortBy: 'publishedAt', order: 'desc' }),
      listLearningTasks(learnerId.value, { page: 1, pageSize: 50, sortBy: 'dueDate', order: 'asc' }),
    ])
    courses.value = courseRes.items
    tasks.value = taskRes.items
  } catch (e: any) {
    ElMessage.error(`加载失败：${e?.message || '未知错误'}`)
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.courseId = 0
  form.title = ''
  form.priority = 2
  form.dueDate = ''
  form.note = ''
}

function startEdit(t: LearningTask) {
  editTaskId.value = t.id
  form.courseId = t.courseId
  form.title = t.title
  form.priority = t.priority
  form.dueDate = t.dueDate || ''
  form.note = t.note || ''
}

function cancelEdit() {
  editTaskId.value = null
  resetForm()
}

async function submitForm() {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录再操作')
    router.push('/login')
    return
  }
  
  if (form.courseId <= 0) {
    ElMessage.warning('请先选择课程')
    return
  }
  const title = form.title.trim() || `学习：${courseTitle(form.courseId)}`

  loading.value = true
  try {
    if (!editTaskId.value) {
      await createLearningTask(learnerId.value, {
        courseId: form.courseId,
        title,
        priority: form.priority,
        dueDate: form.dueDate || null,
        note: form.note.trim() ? form.note.trim() : null,
      })
      ElMessage.success('创建成功 ✅')
    } else {
      await patchLearningTask(learnerId.value, editTaskId.value, {
        title,
        priority: form.priority,
        dueDate: form.dueDate || null,
        note: form.note.trim() ? form.note.trim() : null,
      })
      ElMessage.success('更新成功 ✅')
    }

    cancelEdit()
    await refresh()
  } catch (e: any) {
    ElMessage.error(`操作失败：${e?.message || '未知错误'}`)
  } finally {
    loading.value = false
  }
}

async function setStatus(t: LearningTask, status: number) {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录再操作')
    router.push('/login')
    return
  }

  loading.value = true
  try {
    await patchLearningTask(learnerId.value, t.id, { status })
    await refresh()
  } catch (e: any) {
    ElMessage.error(`状态更新失败：${e?.message || '未知错误'}`)
  } finally {
    loading.value = false
  }
}

async function removeTask(t: LearningTask) {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录再操作')
    router.push('/login')
    return
  }

  const ok = confirm(`确认删除任务？\n\n${t.title}`)
  if (!ok) return

  loading.value = true
  try {
    await deleteLearningTask(learnerId.value, t.id)
    await refresh()
  } catch (e: any) {
    ElMessage.error(`删除失败：${e?.message || '未知错误'}`)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (isLoggedIn.value) {
    refresh()
  }
})
</script>

<style scoped>
.plan-page {
  padding: 24px 0 60px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 16px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--card-shadow);
}

.mt {
  margin-top: 16px;
}

.card-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-full {
  grid-column: 1 / -1;
}

.label {
  font-size: 13px;
  color: var(--text-secondary);
}

.input {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
}

.textarea {
  resize: vertical;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.btn-secondary {
  background: #eef2ff;
  color: #1f2a37;
}

.btn-ghost {
  background: transparent;
  border: 1px solid var(--border-color);
}

.tip {
  margin-top: 10px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.muted {
  color: var(--text-tertiary);
  font-size: 13px;
}

.empty {
  padding: 18px 0;
  color: var(--text-tertiary);
}

.table-wrap {
  overflow: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.table th,
.table td {
  padding: 10px 8px;
  border-bottom: 1px solid var(--border-color);
  vertical-align: top;
}

.title-cell .title {
  font-weight: 600;
}

.title-cell .note {
  margin-top: 4px;
  color: var(--text-tertiary);
  font-size: 12px;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 18px;
}

.badge.s0 {
  background: #eef2ff;
  color: #1d4ed8;
}
.badge.s1 {
  background: #ecfeff;
  color: #0e7490;
}
.badge.s2 {
  background: #ecfdf5;
  color: #047857;
}
.badge.s3 {
  background: #fef2f2;
  color: #b91c1c;
}

.ops {
  white-space: nowrap;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.ops .el-button {
  margin-left: 0;
}

.ml-2 {
  margin-left: 8px;
}

.login-prompt {
  text-align: center;
  padding: 48px 20px;
}

.login-text {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>


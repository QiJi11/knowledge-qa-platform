<template>
  <div class="user-manage">
    <h1 class="page-header">用户管理</h1>

    <div class="table-card">
      <div v-if="loading" class="loading-text">加载中...</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>昵称</th>
            <th>角色</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td class="mono">{{ u.username }}</td>
            <td>{{ u.nickname }}</td>
            <td>
              <span class="badge" :class="u.role === 'admin' ? 'admin-badge' : 'student-badge'">
                {{ u.role === 'admin' ? '管理员' : '学生' }}
              </span>
            </td>
            <td class="mono">{{ formatTime(u.created_at) }}</td>
            <td>
              <div class="ops">
                <button
                  v-if="u.role === 'student'"
                  class="op-btn promote"
                  @click="changeRole(u, 'admin')"
                >设为管理员</button>
                <button
                  v-if="u.role === 'admin' && u.id !== 1"
                  class="op-btn demote"
                  @click="changeRole(u, 'student')"
                >设为学生</button>
                <span v-if="u.id === 1 && u.role === 'admin'" class="muted">超级管理员</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const token = localStorage.getItem('token') || ''
const loading = ref(false)

interface UserRow {
  id: number; username: string; nickname: string; role: string; created_at: string
}

const users = ref<UserRow[]>([])

function formatTime(t: string) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await fetch('/api/v1/admin/users', {
      headers: { Authorization: `Bearer ${token}` }
    })
    const result = await res.json()
    if (result.code === 0) {
      users.value = result.data
    }
  } finally {
    loading.value = false
  }
}

async function changeRole(u: UserRow, newRole: string) {
  const label = newRole === 'admin' ? '管理员' : '学生'
  try {
    await ElMessageBox.confirm(`确定将「${u.nickname}」的角色改为 ${label} 吗？`, '确认修改')
  } catch { return }

  const res = await fetch(`/api/v1/admin/users/${u.id}/role`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify({ role: newRole })
  })
  const result = await res.json()
  if (result.code === 0) {
    ElMessage.success('角色修改成功')
    loadUsers()
  } else {
    ElMessage.error(result.message || '修改失败')
  }
}

onMounted(loadUsers)
</script>

<style scoped>
.page-header { font-size: 24px; font-weight: 700; margin-bottom: 20px; }

.table-card {
  background: #fff; border-radius: 16px; padding: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); overflow: auto;
}
.data-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.data-table th { text-align: left; padding: 12px 10px; border-bottom: 2px solid #e5e7eb; color: var(--text-secondary, #666); font-weight: 600; white-space: nowrap; }
.data-table td { padding: 14px 10px; border-bottom: 1px solid #f0f0f0; vertical-align: middle; }
.mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; }

.badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 12px; font-weight: 500; }
.admin-badge { background: linear-gradient(135deg, #eef2ff, #e0e7ff); color: #4338ca; }
.student-badge { background: #ecfdf5; color: #047857; }

.ops { display: flex; gap: 8px; white-space: nowrap; }
.op-btn { padding: 4px 12px; border-radius: 6px; font-size: 13px; cursor: pointer; border: none; transition: all 0.2s; font-weight: 500; }
.op-btn.promote { background: #eef2ff; color: #4361ee; }
.op-btn.demote { background: #fffbeb; color: #d97706; }
.op-btn:hover { opacity: 0.8; }

.muted { color: var(--text-tertiary, #999); font-size: 13px; }
.loading-text { padding: 40px 0; text-align: center; color: var(--text-tertiary, #999); }
</style>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import { createTodo, deleteTodo, listTodos, patchTodo, type TodoItem } from '@/api/todosDemo'

const loading = ref(false)
const title = ref('')
const items = ref<TodoItem[]>([])
const errorMessage = ref<string | null>(null)

const remainingCount = computed(() => items.value.filter((t) => !t.done).length)

function setError(err: unknown, fallback: string) {
  const msg = typeof (err as any)?.message === 'string' ? (err as any).message : ''
  errorMessage.value = msg || fallback
}

async function reload() {
  loading.value = true
  errorMessage.value = null
  try {
    const res = await listTodos()
    items.value = res.data.items
  } catch (err) {
    setError(err, '加载失败')
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  const t = title.value.trim()
  if (!t) {
    errorMessage.value = '请输入标题'
    return
  }

  loading.value = true
  errorMessage.value = null
  try {
    await createTodo({ title: t, done: false })
    title.value = ''
    await reload()
  } catch (err) {
    setError(err, '创建失败')
  } finally {
    loading.value = false
  }
}

async function handleToggle(row: TodoItem, done: boolean) {
  loading.value = true
  errorMessage.value = null
  try {
    await patchTodo(row.id, { done })
    await reload()
  } catch (err) {
    setError(err, '更新失败')
  } finally {
    loading.value = false
  }
}

async function handleDelete(row: TodoItem) {
  loading.value = true
  errorMessage.value = null
  try {
    await deleteTodo(row.id)
    await reload()
  } catch (err) {
    setError(err, '删除失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await reload()
})
</script>

<template>
  <main class="page">
    <header class="header">
      <div>
        <h1 class="title">待办联调</h1>
        <p class="sub">后端接口：<span class="mono">/api/todos</span></p>
      </div>

      <div class="meta">
        <div class="count">未完成：{{ remainingCount }}</div>
        <button class="btn" type="button" :disabled="loading" @click="reload">刷新</button>
      </div>
    </header>

    <section class="create">
      <input
        v-model="title"
        class="input"
        placeholder="输入 todo 标题，回车创建"
        :disabled="loading"
        @keyup.enter="handleCreate"
      />
      <button class="btn primary" type="button" :disabled="loading" @click="handleCreate">
        创建
      </button>
    </section>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <section class="tableWrap">
      <div v-if="loading" class="loading">加载中…</div>

      <table v-else class="table">
        <thead>
          <tr>
            <th style="width: 72px">完成</th>
            <th>标题</th>
            <th style="width: 230px">创建时间</th>
            <th style="width: 90px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in items" :key="row.id">
            <td>
              <input
                type="checkbox"
                :checked="row.done"
                :disabled="loading"
                @change="(e) => handleToggle(row, (e.target as HTMLInputElement).checked)"
              />
            </td>
            <td class="titleCell" :class="{ done: row.done }">{{ row.title }}</td>
            <td class="mono">{{ row.createdAt }}</td>
            <td>
              <button class="btn danger" type="button" :disabled="loading" @click="handleDelete(row)">
                删除
              </button>
            </td>
          </tr>

          <tr v-if="items.length === 0">
            <td colspan="4" class="empty">还没有数据，先创建一条试试。</td>
          </tr>
        </tbody>
      </table>
    </section>
  </main>
</template>

<style scoped>
.page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.title {
  font-size: 20px;
  font-weight: 800;
}

.sub {
  color: #666;
  margin-top: 6px;
  font-size: 13px;
}

.meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.count {
  color: #333;
  font-size: 13px;
}

.create {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.input {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  outline: none;
}

.input:focus {
  border-color: #4f46e5;
}

.btn {
  height: 36px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn.primary {
  border-color: #4f46e5;
  background: #4f46e5;
  color: #fff;
}

.btn.danger {
  border-color: #ef4444;
  background: #ef4444;
  color: #fff;
}

.error {
  color: #b91c1c;
  background: #fef2f2;
  border: 1px solid #fecaca;
  padding: 10px 12px;
  border-radius: 10px;
  margin: 10px 0 14px;
}

.loading {
  padding: 16px 0;
  color: #666;
}

.tableWrap {
  border: 1px solid #eee;
  border-radius: 12px;
  overflow: hidden;
}

.table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
}

.table th,
.table td {
  padding: 10px 12px;
  border-bottom: 1px solid #f1f1f1;
  text-align: left;
  font-size: 14px;
}

.table thead th {
  background: #fafafa;
  color: #555;
  font-weight: 700;
}

.titleCell.done {
  color: #999;
  text-decoration: line-through;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 12px;
  color: #444;
}

.empty {
  text-align: center;
  padding: 22px 12px;
  color: #777;
}
</style>

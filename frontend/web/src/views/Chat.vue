<template>
  <div class="chat-page container">
    <div class="chat-container">
      <div class="chat-header">
        <h2>✨ AI 知识问答助手</h2>
        <span class="muted">有任何学习问题都可以问我</span>
      </div>

      <div class="chat-messages" ref="messagesRef">
        <div 
          v-for="(msg, index) in messages" 
          :key="index"
          class="message"
          :class="msg.role"
        >
          <div class="avatar">{{ msg.role === 'user' ? '🙋‍♂️' : '🤖' }}</div>
          
          <div class="content bubble" v-if="msg.content">
            <div v-html="formatMessage(msg.content)"></div>
          </div>
          <div class="content bubble loading" v-else>
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <textarea 
          v-model="inputRaw" 
          class="input-box" 
          placeholder="输入你的问题，Enter 发送，Shift + Enter 换行..."
          @keydown.enter.prevent="handleEnter"
          :disabled="loading"
        ></textarea>
        <button class="btn btn-primary" :disabled="loading || !inputRaw.trim()" @click="sendMessage">
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'

// 配置 marked
marked.setOptions({
  breaks: true,
  gfm: true
})

const router = useRouter()

interface Message {
  role: 'user' | 'ai'
  content: string
}

const messages = ref<Message[]>([
  { role: 'ai', content: '你好！我是 AI 知识问答助手，有什么可以帮你的吗？' }
])
const inputRaw = ref('')
const loading = ref(false)
const messagesRef = ref<HTMLElement | null>(null)
// 生成会话ID，用于后端 Redis 滑动窗口保留最近8轮对话
const sessionId = 'session-' + Date.now() + '-' + Math.random().toString(36).slice(2, 8)

const formatMessage = (text: string) => {
  if (!text) return ''
  return marked.parse(text) as string
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const handleEnter = (e: KeyboardEvent) => {
  if (e.shiftKey) {
    // shift+enter 换行
    inputRaw.value += '\n'
  } else {
    sendMessage()
  }
}

const sendMessage = async () => {
  if (!localStorage.getItem('token')) {
    ElMessageBox.confirm(
      '登录后即可使用 AI 知识问答功能，是否前往登录？',
      '需要登录',
      {
        confirmButtonText: '去登录',
        cancelButtonText: '再看看',
        type: 'info',
      }
    ).then(() => {
      router.push('/login')
    }).catch(() => {})
    return
  }

  const content = inputRaw.value.trim()
  if (!content || loading.value) return

  // 添加用户消息
  messages.value.push({ role: 'user', content })
  inputRaw.value = ''
  scrollToBottom()

  // 准备接受 AI 回复
  loading.value = true
  const aiMessageIndex = messages.value.length
  messages.value.push({ role: 'ai', content: '' })

  try {
    // 调用 SSE 流式接口
    const res = await fetch(`/api/v1/chat/stream?message=${encodeURIComponent(content)}&sessionId=${encodeURIComponent(sessionId)}`)
    
    if (!res.ok) {
        const errorMsg = messages.value[aiMessageIndex]
        if (errorMsg) errorMsg.content = '抱歉，服务异常，请稍后再试。'
        throw new Error('网络请求错误')
    }

    if (!res.body) throw new Error('流获取失败')
    const reader = res.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    
    if (!reader) throw new Error('流读取失败')

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      
      // 按换行符切分 buffer
      let newlineIndex;
      while ((newlineIndex = buffer.indexOf('\n')) !== -1) {
        const line = buffer.slice(0, newlineIndex)
        buffer = buffer.slice(newlineIndex + 1)
        
        if (line.startsWith('data:')) {
            // 去掉 'data:' 前缀和可能紧跟的一个空格（SSE 规范）
            let data = line.slice(5)
            if (data.startsWith(' ')) {
                data = data.slice(1)
            }
            
            if (data === '[DONE]') continue
            
            // 直接追加内容，不做 trim() 以保留空格和内部空行
            const aiMsg = messages.value[aiMessageIndex]
            if (aiMsg) {
                aiMsg.content += data
                scrollToBottom()
            }
        }
      }
    }
    // 处理最后一个可能没有换行符的 buffer
    if (buffer.startsWith('data:')) {
        let data = buffer.slice(5)
        if (data.startsWith(' ')) data = data.slice(1)
        if (data && data !== '[DONE]') {
            const aiMsg = messages.value[aiMessageIndex]
            if (aiMsg) aiMsg.content += data
        }
    }
  } catch (e) {
    console.error('AI chat error:', e)
    // 超时或网络异常时，给 AI 气泡设置兜底提示
    const aiMsg = messages.value[aiMessageIndex]
    if (aiMsg && !aiMsg.content) {
      aiMsg.content = '系统正在思考中，请稍后再试 🤔'
    }
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.chat-page {
  padding: 24px 0 60px;
  height: calc(100vh - 64px);
  display: flex;
  justify-content: center;
}

.chat-container {
  width: 100%;
  max-width: 800px;
  background: white;
  border-radius: 12px;
  box-shadow: var(--card-shadow);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
}

.chat-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  background: #f8fafc;
}

.chat-header h2 {
  font-size: 18px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--primary-color);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: #f1f5f9;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  flex-shrink: 0;
}

.bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.6;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  word-break: break-word;
}

.message.ai .bubble {
  background: white;
  color: var(--text-primary);
  border-top-left-radius: 2px;
}

.message.user .bubble {
  background: var(--primary-color);
  color: white;
  border-top-right-radius: 2px;
}

.chat-input-area {
  padding: 16px 24px;
  background: white;
  border-top: 1px solid var(--border-color);
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-box {
  flex: 1;
  min-height: 48px;
  max-height: 120px;
  padding: 12px 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  resize: vertical;
  outline: none;
  font-family: inherit;
  font-size: 15px;
  line-height: 1.5;
  transition: border-color 0.3s;
}

.input-box:focus {
  border-color: var(--primary-color);
}

.btn {
  height: 48px;
  padding: 0 24px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s;
  white-space: nowrap;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Loading Dots */
.loading {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 18px 20px !important;
}

.dot {
  width: 6px;
  height: 6px;
  background: var(--text-tertiary);
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* Markdown 渲染样式 */
.bubble :deep(h1),
.bubble :deep(h2),
.bubble :deep(h3) {
  margin: 12px 0 8px;
  font-weight: 700;
  line-height: 1.4;
}
.bubble :deep(h1) { font-size: 18px; }
.bubble :deep(h2) { font-size: 16px; }
.bubble :deep(h3) { font-size: 15px; }

.bubble :deep(p) {
  margin: 6px 0;
}

.bubble :deep(strong) {
  font-weight: 700;
  color: #1e293b;
}

.bubble :deep(code) {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'Consolas', 'Monaco', monospace;
  color: #e11d48;
}

.bubble :deep(pre) {
  background: #1e293b;
  color: #e2e8f0;
  padding: 14px 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 10px 0;
  font-size: 13px;
  line-height: 1.6;
}

.bubble :deep(pre code) {
  background: none;
  color: inherit;
  padding: 0;
}

.bubble :deep(ul),
.bubble :deep(ol) {
  padding-left: 20px;
  margin: 8px 0;
}

.bubble :deep(li) {
  margin: 4px 0;
}

.bubble :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 10px 0;
  font-size: 14px;
}

.bubble :deep(th),
.bubble :deep(td) {
  border: 1px solid #e2e8f0;
  padding: 8px 12px;
  text-align: left;
}

.bubble :deep(th) {
  background: #f8fafc;
  font-weight: 600;
}

.bubble :deep(blockquote) {
  border-left: 3px solid var(--primary-color);
  padding-left: 12px;
  margin: 8px 0;
  color: #64748b;
}
</style>

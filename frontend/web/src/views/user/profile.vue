<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const userInfo = computed(() => userStore.userInfo)

const form = reactive({
  nickname: userInfo.value?.nickname || '',
  avatar: userInfo.value?.avatar || '',
  phone: userInfo.value?.phone || ''
})

async function handleSave() {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="profile-page">
    <h2 class="page-title">个人信息</h2>

    <el-form
      ref="formRef"
      :model="form"
      label-width="100px"
      class="profile-form"
    >
      <el-form-item label="头像">
        <div class="avatar-upload">
          <el-avatar :size="80" :src="form.avatar">
            {{ form.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <el-button size="small" style="margin-left: 16px">更换头像</el-button>
        </div>
      </el-form-item>

      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" placeholder="请输入昵称" style="max-width: 300px" />
      </el-form-item>

      <el-form-item label="手机号">
        <el-input :value="form.phone" disabled style="max-width: 300px" />
        <span class="tip">手机号不可修改</span>
      </el-form-item>

      <el-form-item label="账号角色">
        <el-tag>{{ userInfo?.role === 1 ? '讲师' : '学员' }}</el-tag>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSave">
          保存修改
        </el-button>
      </el-form-item>
    </el-form>

    <el-divider />

    <h3>账号安全</h3>
    <div class="security-list">
      <div class="security-item">
        <div class="info">
          <div class="title">登录密码</div>
          <div class="desc">定期更换密码可以保护账号安全</div>
        </div>
        <el-button text type="primary">修改密码</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 600px;
}

.page-title {
  font-size: 18px;
  margin-bottom: 24px;
}

.avatar-upload {
  display: flex;
  align-items: center;
}

.tip {
  margin-left: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

h3 {
  font-size: 16px;
  margin-bottom: 16px;
}

.security-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.security-item .title {
  font-weight: 500;
  margin-bottom: 4px;
}

.security-item .desc {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>

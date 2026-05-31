<template>
  <div class="admin-login">
    <div class="login-card card">
      <div class="admin-mark">管</div>
      <h2>管理后台</h2>
      <p class="subtitle">管理员专用通道</p>
      <el-form size="large">
        <el-form-item>
          <el-input v-model="username" placeholder="管理员账号" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" type="password" placeholder="密码" prefix-icon="Lock" show-password @keyup.enter="login" />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="login">登 录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const password = ref('')
const loading = ref(false)

async function login() {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await userStore.adminLogin(username.value, password.value)
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } catch {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at 18% 18%, rgba(255,208,0,0.28), transparent 28%),
    radial-gradient(circle at 82% 76%, rgba(255,85,0,0.24), transparent 30%),
    linear-gradient(135deg, #181818 0%, #2d211b 100%);
  padding: 24px;
}
.login-card {
  width: min(380px, 100%);
  padding: 42px 32px 34px;
  text-align: center;
  border-color: rgba(255,255,255,0.18);
  box-shadow: 10px 10px 0 rgba(0,0,0,0.24), 0 24px 80px rgba(0,0,0,0.22);
}
.admin-mark {
  width: 58px;
  height: 58px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  border-radius: 18px;
  background: var(--color-ink);
  color: var(--color-accent);
  font-family: var(--font-display);
  font-size: 24px;
  box-shadow: 4px 4px 0 rgba(255,85,0,0.28);
}
.login-card h2 { font-size: 26px; margin-bottom: 6px; }
.subtitle { color: var(--color-muted); margin-bottom: 24px; font-size: 13px; }
.login-btn { width: 100%; border-radius: 999px; min-height: 44px; }
</style>

<template>
  <div class="register-page">
    <div class="register-card">
      <div class="brand">
        <div class="brand-logo">校园二手</div>
        <p class="brand-desc">注册账号，开始交易</p>
      </div>

      <div class="form-section">
        <div class="input-group">
          <label class="input-label">手机号</label>
          <input v-model="phone" class="field-input" type="tel" placeholder="输入11位手机号" maxlength="11" />
        </div>
        <div class="input-group">
          <label class="input-label">密码</label>
          <input v-model="password" class="field-input" type="password" placeholder="设置密码 (6位以上)" />
        </div>
        <div class="input-group">
          <label class="input-label">昵称</label>
          <input v-model="nickname" class="field-input" type="text" placeholder="给自己取个名字 (选填)" />
        </div>

        <button class="register-btn" :disabled="loading" @click="handleRegister">
          <span v-if="!loading">注 册</span>
          <span v-else>注册中...</span>
        </button>

        <div class="switch-row">
          <span class="switch-text">已有账号?</span>
          <router-link to="/login" class="switch-link">去登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const phone = ref('')
const password = ref('')
const nickname = ref('')
const loading = ref(false)

function isValidPhone(p) { return /^1[3-9]\d{9}$/.test(p) }
function isValidPassword(p) { return p.length >= 8 && /[a-z]/.test(p) && /[A-Z]/.test(p) && /[!@#$%^&*(),.?":{}|<>_\-]/.test(p) }

async function handleRegister() {
  if (!isValidPhone(phone.value)) {
    ElMessage.warning('请输入正确的中国大陆手机号 (11位, 1开头)')
    return
  }
  if (!isValidPassword(password.value)) {
    ElMessage.warning('密码至少8位，需包含大小写字母和特殊符号')
    return
  }
  loading.value = true
  try {
    const res = await authApi.register({
      phone: phone.value,
      password: password.value,
      nickname: nickname.value || undefined
    })
    userStore.user = res.data?.user
    userStore.token = res.data?.token
    userStore.role = 'USER'
    localStorage.setItem('token', userStore.token)
    localStorage.setItem('role', 'USER')
    ElMessage.success('注册成功')
    router.push('/home')
  } catch {} finally { loading.value = false }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-warm-white);
  padding: 20px;
  position: relative;
  overflow: hidden;
}
.register-page::before {
  content: ''; position: absolute; top: -80px; right: -60px;
  width: 200px; height: 200px;
  background: var(--color-primary);
  border-radius: 40% 60% 60% 40% / 40% 40% 60% 60%;
  opacity: 0.08;
}
.register-page::after {
  content: ''; position: absolute; bottom: -40px; left: -30px;
  width: 120px; height: 120px;
  background: var(--color-accent);
  border-radius: 50%; opacity: 0.1;
}
.register-card {
  width: 100%; max-width: 360px;
  background: white;
  border: 2px solid var(--color-ink);
  border-radius: var(--radius-lg);
  padding: 40px 28px 36px;
  box-shadow: 6px 6px 0px rgba(0,0,0,0.08);
  position: relative; z-index: 1;
}
.brand { text-align: center; margin-bottom: 24px; }
.brand-logo { font-family: var(--font-display); font-size: 28px; font-weight: 800; color: var(--color-primary); letter-spacing: 4px; margin-bottom: 8px; }
.brand-desc { font-size: 14px; color: var(--color-muted); }
.form-section { display: flex; flex-direction: column; gap: 14px; }
.input-group { display: flex; flex-direction: column; gap: 4px; }
.input-label { font-size: 12px; font-weight: 700; color: var(--color-ink-light); text-transform: uppercase; letter-spacing: 1px; }
.field-input {
  padding: 12px 16px; border: 2px solid var(--color-border);
  border-radius: var(--radius); font-size: 15px; font-family: var(--font-body);
  outline: none; transition: border-color 0.2s; background: var(--color-warm-white);
}
.field-input:focus { border-color: var(--color-primary); }
.register-btn {
  width: 100%; padding: 14px; margin-top: 8px;
  background: var(--color-ink); color: white; border: none;
  border-radius: 50px; font-family: var(--font-display);
  font-size: 16px; letter-spacing: 4px; cursor: pointer;
  transition: all 0.15s; box-shadow: 3px 3px 0px rgba(0,0,0,0.12);
}
.register-btn:active { transform: translate(1px, 1px); box-shadow: none; }
.register-btn:disabled { background: #ccc; box-shadow: none; cursor: not-allowed; }
.switch-row { display: flex; justify-content: center; align-items: center; gap: 6px; margin-top: 8px; }
.switch-text { font-size: 13px; color: var(--color-muted); }
.switch-link { font-size: 13px; color: var(--color-primary); font-weight: 700; }
</style>

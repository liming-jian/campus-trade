<template>
  <div class="login-page">
    <div class="login-card">
      <!-- Logo mark -->
      <div class="login-mark">
        <div class="mark-graphic">
          <span class="mark-letter">X</span>
        </div>
      </div>

      <h2 class="login-heading">校园二手</h2>
      <p class="login-desc">让闲置在校园里流转</p>

      <div class="form-section">
        <div class="input-group">
          <label class="input-label">手机号</label>
          <input v-model="phone" class="field-input" type="tel" placeholder="输入11位手机号" maxlength="11" />
        </div>
        <div class="input-group">
          <label class="input-label">密码</label>
          <input v-model="password" class="field-input" type="password" placeholder="输入密码" @keyup.enter="handleLogin" />
        </div>

        <div class="form-row">
          <router-link to="/forgot-password" class="forgot-link">忘记密码?</router-link>
        </div>

        <button class="login-submit" :disabled="loading" @click="handleLogin">
          <span v-if="!loading">登 录</span>
          <span v-else class="loading-dots">登录中<span class="dot-anim">...</span></span>
        </button>

        <router-link to="/admin/login" class="admin-entry">
          <span>管理员入口</span>
        </router-link>

        <div class="switch-row">
          <span class="switch-text">还没有账号?</span>
          <router-link to="/register" class="switch-link">立即注册</router-link>
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

const router = useRouter()
const userStore = useUserStore()
const phone = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (phone.value.trim().toLowerCase() === 'admin') {
    ElMessage.info('管理员请从管理员入口登录')
    router.push('/admin/login')
    return
  }
  if (!phone.value || phone.value.length !== 11) { ElMessage.warning('请输入正确的手机号'); return }
  if (!password.value) { ElMessage.warning('请输入密码'); return }
  loading.value = true
  try {
    await userStore.login(phone.value, password.value)
    ElMessage.success('登录成功')
    router.push('/home')
  } catch {} finally { loading.value = false }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-warm-white);
  padding: 20px;
  position: relative;
  overflow: hidden;
}
.login-page::before {
  content: '';
  position: absolute;
  top: -80px;
  right: -60px;
  width: 200px;
  height: 200px;
  background: var(--color-primary);
  border-radius: 40% 60% 60% 40% / 40% 40% 60% 60%;
  opacity: 0.08;
}
.login-page::after {
  content: '';
  position: absolute;
  bottom: -40px;
  left: -30px;
  width: 120px;
  height: 120px;
  background: var(--color-accent);
  border-radius: 50%;
  opacity: 0.1;
}

.login-card {
  width: 100%;
  max-width: 360px;
  background: white;
  border: 2px solid var(--color-ink);
  border-radius: var(--radius-lg);
  padding: 40px 28px 36px;
  box-shadow: 6px 6px 0px rgba(0,0,0,0.08);
  position: relative;
  z-index: 1;
}

.login-mark {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}
.mark-graphic {
  width: 52px;
  height: 52px;
  background: var(--color-ink);
  border-radius: 30% 70% 50% 50% / 30% 35% 65% 70%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.mark-letter {
  color: var(--color-accent);
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 700;
}

.login-heading {
  text-align: center;
  font-family: var(--font-display);
  font-size: 24px;
  letter-spacing: 3px;
  color: var(--color-ink);
  margin-bottom: 6px;
}
.login-desc {
  text-align: center;
  font-size: 13px;
  color: var(--color-muted);
  margin-bottom: 28px;
}

.form-section { display: flex; flex-direction: column; gap: 14px; }
.input-group { display: flex; flex-direction: column; gap: 4px; }
.input-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--color-ink-light);
  text-transform: uppercase;
  letter-spacing: 1px;
}
.field-input {
  padding: 12px 16px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 15px;
  font-family: var(--font-body);
  outline: none;
  transition: border-color 0.2s;
  background: var(--color-warm-white);
}
.field-input:focus { border-color: var(--color-primary); }

.form-row { display: flex; justify-content: flex-end; }
.forgot-link { font-size: 12px; color: var(--color-muted); font-weight: 500; }

.login-submit {
  width: 100%;
  padding: 14px;
  background: var(--color-ink);
  color: white;
  border: none;
  border-radius: 50px;
  font-family: var(--font-display);
  font-size: 16px;
  letter-spacing: 4px;
  cursor: pointer;
  transition: all 0.15s;
  box-shadow: 3px 3px 0px rgba(0,0,0,0.12);
  margin-top: 4px;
}
.login-submit:active { transform: translate(1px, 1px); box-shadow: none; }
.login-submit:disabled { background: #ccc; box-shadow: none; cursor: not-allowed; }

.admin-entry {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 46px;
  padding: 10px 14px;
  border: 2px dashed var(--color-primary);
  border-radius: var(--radius);
  background: rgba(255, 85, 0, 0.07);
  color: var(--color-ink);
  font-size: 13px;
  font-weight: 700;
  transition: transform 0.15s, background 0.15s, border-color 0.15s;
}
.admin-entry:hover {
  transform: translateY(-2px);
  background: rgba(255, 85, 0, 0.12);
  border-color: var(--color-ink);
}
.admin-entry:active { transform: translateY(0); }

.switch-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
}
.switch-text { font-size: 13px; color: var(--color-muted); }
.switch-link { font-size: 13px; color: var(--color-primary); font-weight: 700; }

.loading-dots { font-family: var(--font-display); }
.dot-anim { animation: pulse 1s infinite; display: inline-block; }

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>

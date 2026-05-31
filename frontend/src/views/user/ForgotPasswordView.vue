<template>
  <div class="forgot-page">
    <div class="forgot-card">
      <div class="brand">
        <div class="brand-logo">重置密码</div>
        <p class="brand-desc">输入手机号和新密码</p>
      </div>

      <div class="form-section">
        <div class="input-group">
          <label class="input-label">手机号</label>
          <input v-model="phone" class="field-input" type="tel" placeholder="输入11位手机号" maxlength="11" />
        </div>
        <div class="input-group">
          <label class="input-label">新密码</label>
          <input v-model="password" class="field-input" type="password" placeholder="至少8位，含大小写字母和特殊符号" />
        </div>
        <div class="input-group">
          <label class="input-label">确认密码</label>
          <input v-model="confirmPwd" class="field-input" type="password" placeholder="再次输入新密码" />
        </div>

        <button class="submit-btn" :disabled="loading" @click="handleReset">
          {{ loading ? '提交中...' : '重置密码' }}
        </button>

        <div class="switch-row">
          <router-link to="/login" class="switch-link">返回登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'

const router = useRouter()
const phone = ref('')
const password = ref('')
const confirmPwd = ref('')
const loading = ref(false)

function isValidPhone(p) { return /^1[3-9]\d{9}$/.test(p) }
function isValidPassword(p) { return p.length >= 8 && /[a-z]/.test(p) && /[A-Z]/.test(p) && /[!@#$%^&*(),.?":{}|<>_\-]/.test(p) }

async function handleReset() {
  if (!isValidPhone(phone.value)) { ElMessage.warning('请输入正确的手机号'); return }
  if (!isValidPassword(password.value)) { ElMessage.warning('密码至少8位，需包含大小写字母和特殊符号'); return }
  if (password.value !== confirmPwd.value) { ElMessage.warning('两次密码不一致'); return }
  loading.value = true
  try {
    await authApi.resetPassword({ phone: phone.value, newPassword: password.value })
    ElMessage.success('密码重置成功')
    router.push('/login')
  } catch {} finally { loading.value = false }
}
</script>

<style scoped>
.forgot-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: var(--color-warm-white); padding: 20px; }
.forgot-card { width: 100%; max-width: 360px; background: white; border: 2px solid var(--color-ink); border-radius: var(--radius-lg); padding: 40px 28px 36px; box-shadow: 6px 6px 0px rgba(0,0,0,0.08); }
.brand { text-align: center; margin-bottom: 24px; }
.brand-logo { font-family: var(--font-display); font-size: 24px; font-weight: 800; color: var(--color-primary); letter-spacing: 3px; margin-bottom: 8px; }
.brand-desc { font-size: 14px; color: var(--color-muted); }
.form-section { display: flex; flex-direction: column; gap: 14px; }
.input-group { display: flex; flex-direction: column; gap: 4px; }
.input-label { font-size: 12px; font-weight: 700; color: var(--color-ink-light); text-transform: uppercase; letter-spacing: 1px; }
.field-input { padding: 12px 16px; border: 2px solid var(--color-border); border-radius: var(--radius); font-size: 15px; outline: none; background: var(--color-warm-white); }
.field-input:focus { border-color: var(--color-primary); }
.submit-btn { width: 100%; padding: 14px; background: var(--color-ink); color: white; border: none; border-radius: 50px; font-family: var(--font-display); font-size: 16px; letter-spacing: 2px; cursor: pointer; box-shadow: 3px 3px 0px rgba(0,0,0,0.12); margin-top: 8px; }
.submit-btn:active { transform: translate(1px, 1px); box-shadow: none; }
.submit-btn:disabled { background: #ccc; box-shadow: none; cursor: not-allowed; }
.switch-row { text-align: center; margin-top: 8px; }
.switch-link { font-size: 13px; color: var(--color-primary); font-weight: 700; }
</style>

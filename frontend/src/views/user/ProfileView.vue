<template>
  <div class="page-container">
    <!-- Profile Header -->
    <div class="profile-top">
      <div class="avatar-section">
        <div class="avatar-wrap" @click="triggerUpload" title="点击更换头像">
          <img v-if="user?.avatar" :src="user.avatar" class="avatar-img" />
          <div v-else class="avatar-placeholder">
            <span class="avatar-letter">{{ (user?.nickname || user?.phone || '?').charAt(0).toUpperCase() }}</span>
          </div>
          <div class="avatar-overlay">
            <el-icon :size="20"><Camera /></el-icon>
          </div>
        </div>
        <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />
        <div class="avatar-hint">点击更换头像</div>
      </div>

      <div class="user-basic">
        <div class="nickname-row">
          <span v-if="!editingNick" class="nickname-text">{{ user?.nickname || user?.phone || '未登录' }}</span>
          <input v-else ref="nickInput" v-model="nicknameEdit" class="nickname-input" maxlength="12" @blur="saveNickname" @keyup.enter="saveNickname" />
          <span class="edit-icon" @click="startEditNick" v-if="!editingNick && isLoggedIn">
            <el-icon :size="14"><EditPen /></el-icon>
          </span>
        </div>
        <div class="school-row" v-if="user">
          <span v-if="user.schoolName" class="school-name">{{ user.schoolName }}</span>
          <span class="verify-badge" :class="verifyClass">{{ verifyText }}</span>
        </div>
        <div class="phone-row" v-if="user">{{ maskedPhone }}</div>
      </div>
    </div>

    <!-- Login prompt -->
    <div v-if="!isLoggedIn" class="login-prompt">
      <p>登录后查看更多精彩内容</p>
      <el-button type="primary" size="large" round @click="$router.push('/login')">登录 / 注册</el-button>
    </div>

    <!-- Menu -->
    <div class="menu-section" v-if="isLoggedIn">
      <div class="menu-group">
        <div class="menu-item" @click="$router.push('/orders?role=BUYER')">
          <span class="menu-label">我买到的</span>
          <span class="menu-hint">查看已购商品</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="menu-item" @click="$router.push('/orders?role=SELLER')">
          <span class="menu-label">我卖出的</span>
          <span class="menu-hint">管理售出订单</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="menu-item" @click="$router.push('/publish')">
          <span class="menu-label">发布闲置</span>
          <span class="menu-hint">让旧物找到新主人</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="menu-item" @click="$router.push('/my-reports')">
          <span class="menu-label">我的举报</span>
          <span class="menu-hint">查看管理员审核进度</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      <div class="menu-group">
        <div class="menu-item" @click="$router.push('/address')">
          <span class="menu-label">收货地址</span>
          <span class="menu-hint">管理邮寄地址</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="menu-item" @click="$router.push('/verify')">
          <span class="menu-label">学生认证</span>
          <span class="menu-hint" v-if="user?.verifyStatus !== 2">认证后获得更多信任</span>
          <span class="menu-hint verified-hint" v-else>已认证 - {{ user.schoolName }}</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      <div class="menu-group">
        <div class="menu-item logout-item" @click="handleLogout">
          <span class="menu-label">退出登录</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <TabBar />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Camera, EditPen } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'
import TabBar from '@/components/TabBar.vue'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()
const { user, isLoggedIn } = storeToRefs(userStore)

const fileInput = ref(null)
const nickInput = ref(null)
const editingNick = ref(false)
const nicknameEdit = ref('')

const verifyText = computed(() => {
  const s = user.value?.verifyStatus
  if (s === 0) return '未认证'
  if (s === 1) return '审核中'
  if (s === 2) return '已认证'
  if (s === 3) return '认证被拒'
  return ''
})

const verifyClass = computed(() => {
  const s = user.value?.verifyStatus
  return { verified: s === 2, pending: s === 1, rejected: s === 3, unverified: s === 0 }
})

const maskedPhone = computed(() => {
  const p = user.value?.phone || ''
  if (p.length === 11) return p.substring(0, 3) + '****' + p.substring(7)
  return p
})

function triggerUpload() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  fileInput.value?.click()
}

async function handleAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { ElMessage.warning('图片不能超过5MB'); return }
  try {
    const res = await authApi.uploadAvatar(file)
    user.value = res.data
    ElMessage.success('头像更新成功')
  } catch { ElMessage.error('上传失败') }
}

function startEditNick() {
  nicknameEdit.value = user.value?.nickname || ''
  editingNick.value = true
  nextTick(() => nickInput.value?.focus())
}

async function saveNickname() {
  editingNick.value = false
  const val = nicknameEdit.value.trim()
  if (!val || val === user.value?.nickname) return
  try {
    const res = await authApi.updateProfile({ nickname: val })
    user.value = res.data
    ElMessage.success('昵称已更新')
  } catch { ElMessage.error('修改失败') }
}

function handleLogout() {
  userStore.logout()
  chatStore.disconnect()
  ElMessage.success('已退出登录')
  router.push('/home')
}

onMounted(() => {
  if (isLoggedIn.value) userStore.fetchUser()
})
</script>

<style scoped>
/* Profile Header */
.profile-top {
  background: white;
  margin: 12px;
  border-radius: var(--radius-lg);
  padding: 28px 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  border: 2px solid var(--color-border);
  box-shadow: var(--shadow-sticker);
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.avatar-wrap {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  border: 3px solid var(--color-ink);
  box-shadow: 2px 2px 0px rgba(0,0,0,0.1);
  transition: all 0.2s;
}
.avatar-wrap:hover { transform: scale(1.05); }
.avatar-wrap:active { transform: scale(0.95); }
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, var(--color-primary), #FF8F00);
}
.avatar-letter {
  font-family: var(--font-display);
  font-size: 32px;
  color: white;
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.2s;
}
.avatar-wrap:hover .avatar-overlay { opacity: 1; }
.avatar-hint {
  font-size: 11px;
  color: var(--color-muted);
}
.user-basic { flex: 1; min-width: 0; }
.nickname-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.nickname-text {
  font-size: 20px;
  font-weight: 700;
  font-family: var(--font-display);
  letter-spacing: 1px;
}
.nickname-input {
  font-size: 20px;
  font-weight: 700;
  font-family: var(--font-display);
  border: none;
  border-bottom: 2px solid var(--color-primary);
  outline: none;
  width: 140px;
  background: transparent;
}
.edit-icon {
  cursor: pointer;
  color: var(--color-muted);
  transition: color 0.2s;
}
.edit-icon:hover { color: var(--color-primary); }
.school-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.school-name { font-size: 13px; color: var(--color-ink-light); }
.verify-badge {
  font-size: 10px;
  padding: 1px 8px;
  border-radius: 10px;
  font-weight: 600;
}
.verify-badge.verified { background: #E8F5E9; color: #4CAF50; }
.verify-badge.pending { background: #FFF8E1; color: #F57F17; }
.verify-badge.rejected { background: #FFEBEE; color: #F44336; }
.verify-badge.unverified { background: #F5F5F5; color: #999; }
.phone-row { font-size: 12px; color: var(--color-muted); letter-spacing: 1px; }

/* Login Prompt */
.login-prompt {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-muted);
}
.login-prompt p { margin-bottom: 16px; font-size: 15px; }

/* Menu */
.menu-section { padding: 0 12px 80px; }
.menu-group {
  background: white;
  border-radius: var(--radius);
  margin-bottom: 12px;
  overflow: hidden;
  border: 2px solid var(--color-border);
  box-shadow: var(--shadow-sticker);
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 16px;
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  transition: background 0.15s;
}
.menu-item:last-child { border-bottom: none; }
.menu-item:active { background: var(--color-warm-white); }
.menu-label { font-weight: 600; font-size: 14px; flex-shrink: 0; }
.menu-hint { flex: 1; font-size: 12px; color: var(--color-muted); text-align: right; }
.menu-item > .el-icon { color: var(--color-muted); flex-shrink: 0; }
.logout-item .menu-label { color: var(--color-danger); }
.verified-hint { color: #4CAF50 !important; }
</style>

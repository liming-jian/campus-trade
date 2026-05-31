<template>
  <!-- Mobile hamburger -->
  <div class="mobile-nav" @click="open = !open">
    <el-icon :size="22"><Operation /></el-icon>
    <span class="mobile-nav-title">管理后台</span>
  </div>
  <!-- Overlay -->
  <div v-if="open" class="overlay" @click="open = false"></div>
  <!-- Sidebar -->
  <div class="sidebar" :class="{ open }">
    <div class="sidebar-header">
      <router-link to="/admin/dashboard" class="logo">管理后台</router-link>
    </div>
    <div class="sidebar-menu">
      <router-link v-for="m in menus" :key="m.to" :to="m.to" class="menu-item" active-class="active" @click="open = false">
        <el-icon><component :is="m.icon" /></el-icon>
        <span>{{ m.label }}</span>
      </router-link>
    </div>
    <div class="sidebar-footer">
      <div class="menu-item" @click="logout">
        <el-icon><SwitchButton /></el-icon>
        <span>退出</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { DataAnalysis, UserFilled, Goods, Grid, Document, WarningFilled, SwitchButton, Operation } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const open = ref(false)

const menus = [
  { to: '/admin/dashboard', label: '数据概览', icon: 'DataAnalysis' },
  { to: '/admin/users', label: '用户管理', icon: 'UserFilled' },
  { to: '/admin/products', label: '商品审核', icon: 'Goods' },
  { to: '/admin/categories', label: '分类管理', icon: 'Grid' },
  { to: '/admin/orders', label: '订单管理', icon: 'Document' },
  { to: '/admin/reports', label: '举报管理', icon: 'WarningFilled' }
]

function logout() {
  userStore.logout()
  ElMessage.success('已退出')
  router.push('/admin/login')
}
</script>

<style scoped>
.mobile-nav {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  padding: 10px 16px;
  background: #1a1a2e;
  color: white;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.mobile-nav-title { font-weight: 600; font-size: 15px; }

.overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  z-index: 40;
}

.sidebar {
  width: 200px;
  min-height: 100vh;
  background: #1a1a2e;
  color: #ccc;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: relative;
  z-index: 45;
}
.sidebar-header { padding: 20px 16px; border-bottom: 1px solid rgba(255,255,255,0.1); }
.logo { color: var(--color-primary); font-size: 18px; font-weight: 700; text-decoration: none; }
.sidebar-menu { flex: 1; padding: 8px 0; }
.menu-item {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 20px; color: #999;
  text-decoration: none; font-size: 14px;
  cursor: pointer; transition: all 0.2s;
}
.menu-item:hover { color: white; background: rgba(255,255,255,0.05); }
.menu-item.active { color: var(--color-primary); background: rgba(255,107,0,0.1); border-right: 3px solid var(--color-primary); }
.sidebar-footer { border-top: 1px solid rgba(255,255,255,0.1); padding: 8px 0; }

@media (max-width: 768px) {
  .mobile-nav { display: flex; }
  .overlay { display: block; }
  .sidebar {
    position: fixed; top: 0; left: -220px; bottom: 0;
    transition: left 0.25s; z-index: 45; width: 200px;
  }
  .sidebar.open { left: 0; }
}
</style>

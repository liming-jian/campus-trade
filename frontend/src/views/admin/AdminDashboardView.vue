<template>
  <div class="admin-shell">
    <AdminSidebar />
    <div class="admin-main">
      <h2>数据概览</h2>
      <div class="stat-grid">
        <div class="stat-card" v-for="s in stats" :key="s.label">
          <div class="stat-value">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
      <div class="quick-actions">
        <el-button type="warning" @click="$router.push('/admin/products?status=REVIEW')">
          待审核商品 ({{ dashboard.pendingReviewCount || 0 }})
        </el-button>
        <el-button type="danger" @click="$router.push('/admin/reports?status=PENDING')">
          待处理举报 ({{ dashboard.pendingReportCount || 0 }})
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import AdminSidebar from './AdminSidebar.vue'

const dashboard = reactive({})
const stats = ref([])

onMounted(async () => {
  try {
    const res = await adminApi.getDashboard()
    Object.assign(dashboard, res.data)
    stats.value = [
      { label: '总用户数', value: dashboard.userCount || 0 },
      { label: '总商品数', value: dashboard.productCount || 0 },
      { label: '总订单数', value: dashboard.orderCount || 0 },
      { label: '今日新增用户', value: dashboard.todayNewUsers || 0 },
      { label: '今日新增商品', value: dashboard.todayNewProducts || 0 },
      { label: '今日新增订单', value: dashboard.todayNewOrders || 0 }
    ]
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.admin-shell { display: flex; min-height: 100vh; }
.admin-main { flex: 1; padding: 20px; background: var(--color-bg); }
.admin-main h2 { margin-bottom: 20px; font-size: 20px; }
.stat-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card {
  background: white;
  border-radius: var(--radius);
  padding: 20px;
  text-align: center;
  box-shadow: var(--shadow-card);
}
.stat-value { font-size: 28px; font-weight: 700; color: var(--color-primary); }
.stat-label { font-size: 13px; color: var(--color-text-muted); margin-top: 4px; }
.quick-actions { display: flex; gap: 12px; }
</style>

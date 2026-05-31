<template>
  <div class="admin-shell">
    <AdminSidebar />
    <div class="admin-main">
      <h2>订单管理</h2>
      <div class="toolbar">
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="search" style="width:140px;">
          <el-option label="待付款" value="PENDING_PAY" />
          <el-option label="待发货" value="PENDING_SHIP" />
          <el-option label="待收货" value="PENDING_RECEIVE" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索订单号" clearable @keyup.enter="search" style="width:200px;" />
      </div>
      <el-table :data="orders" border stripe size="small">
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="productTitle" label="商品" min-width="150" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="金额" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="orderStatusType(row.status)" size="small">{{ orderStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="100">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="prev, pager, next"
        @current-change="fetchOrders"
        style="margin-top:16px;justify-content:center;"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import AdminSidebar from './AdminSidebar.vue'

const orders = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statusFilter = ref(null)
const keyword = ref('')

function orderStatusType(s) {
  return s === 'PENDING_PAY' ? 'warning' : s === 'PENDING_SHIP' ? '' : s === 'PENDING_RECEIVE' ? 'primary' : s === 'COMPLETED' ? 'success' : 'info'
}
function orderStatusText(s) {
  return s === 'PENDING_PAY' ? '待付款' : s === 'PENDING_SHIP' ? '待发货' : s === 'PENDING_RECEIVE' ? '待收货' : s === 'COMPLETED' ? '已完成' : s === 'CANCELLED' ? '已取消' : s
}
function formatDate(d) { return d ? new Date(d).toLocaleDateString() : '-' }

async function fetchOrders() {
  const res = await adminApi.getOrders({ page: page.value, size: size.value, status: statusFilter.value, keyword: keyword.value })
  orders.value = res.data?.records || []
  total.value = res.data?.total || 0
}

function search() { page.value = 1; fetchOrders() }

onMounted(fetchOrders)
</script>

<style scoped>
.admin-shell { display: flex; min-height: 100vh; }
.admin-main { flex: 1; padding: 20px; background: var(--color-bg); overflow-x: auto; }
.admin-main h2 { margin-bottom: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>

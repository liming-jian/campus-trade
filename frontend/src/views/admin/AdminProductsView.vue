<template>
  <div class="admin-shell">
    <AdminSidebar />
    <div class="admin-main">
      <h2>商品审核</h2>
      <div class="toolbar">
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="search" style="width:140px;">
          <el-option label="审核中" value="REVIEW" />
          <el-option label="已通过" value="PASS" />
          <el-option label="已拒绝" value="REJECT" />
          <el-option label="已下架" value="OFF" />
          <el-option label="已售出" value="SOLD" />
        </el-select>
      </div>
      <el-table :data="products" border stripe size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image :src="row.images?.[0] || ''" style="width:50px;height:50px;" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="80" />
        <el-table-column label="AI审核" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.aiAuditResult === 'PASS'" type="success" size="small">通过</el-tag>
            <el-tag v-else-if="row.aiAuditResult === 'REVIEW'" type="warning" size="small">待复审</el-tag>
            <el-tag v-else-if="row.aiAuditResult === 'REJECT'" type="danger" size="small">违规</el-tag>
            <span v-else style="color:#999;font-size:12px;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <template v-if="row.status === 'REVIEW'">
              <el-button size="small" type="success" @click="audit(row, 'PASS')">通过</el-button>
              <el-button size="small" type="danger" @click="auditWithReason(row, 'REJECT')">拒绝</el-button>
            </template>
            <el-button v-if="row.status === 'PASS'" size="small" type="warning" @click="auditWithReason(row, 'OFF')">下架</el-button>
            <span v-else style="color:#999;font-size:12px;">-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="prev, pager, next"
        @current-change="fetchProducts"
        style="margin-top:16px;justify-content:center;"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import AdminSidebar from './AdminSidebar.vue'

const products = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statusFilter = ref(null)

function statusType(s) { return s === 'PASS' ? 'success' : s === 'REVIEW' ? 'warning' : s === 'REJECT' ? 'danger' : s === 'SOLD' ? 'info' : 'info' }
function statusText(s) { return s === 'PASS' ? '已通过' : s === 'REVIEW' ? '审核中' : s === 'REJECT' ? '已拒绝' : s === 'SOLD' ? '已售出' : s === 'OFF' ? '已下架' : s }

async function fetchProducts() {
  const res = await adminApi.getProducts({ page: page.value, size: size.value, status: statusFilter.value })
  products.value = res.data?.records || []
  total.value = res.data?.total || 0
}

function search() { page.value = 1; fetchProducts() }

async function audit(row, action) {
  await adminApi.auditProduct(row.id, action, '')
  ElMessage.success(action === 'PASS' ? '已通过' : '已处理')
  fetchProducts()
}

async function auditWithReason(row, action) {
  await ElMessageBox.prompt('请输入原因', action === 'REJECT' ? '拒绝原因' : '下架原因', { type: 'warning' })
    .then(async ({ value }) => {
      await adminApi.auditProduct(row.id, action, value || '违规')
      ElMessage.success('已处理')
      fetchProducts()
    })
    .catch(() => {})
}

onMounted(fetchProducts)
</script>

<style scoped>
.admin-shell { display: flex; min-height: 100vh; }
.admin-main { flex: 1; padding: 20px; background: var(--color-bg); overflow-x: auto; }
.admin-main h2 { margin-bottom: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>

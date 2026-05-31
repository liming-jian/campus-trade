<template>
  <div class="admin-shell">
    <AdminSidebar />
    <div class="admin-main">
      <h2>举报管理</h2>
      <div class="toolbar">
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="search" style="width:140px;">
          <el-option label="待处理" value="PENDING" />
          <el-option label="已处理" value="RESOLVED" />
          <el-option label="已驳回" value="DISMISSED" />
        </el-select>
      </div>
      <el-table :data="reports" border stripe size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="举报人ID" width="80" prop="reporterId" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">{{ row.targetType === 'PRODUCT' ? '商品' : '用户' }}</template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="80" />
        <el-table-column prop="reason" label="举报原因" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="reportStatusType(row.status)" size="small">{{ reportStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button size="small" type="success" @click="handle(row, 'RESOLVE')">处理</el-button>
              <el-button size="small" type="info" @click="handle(row, 'DISMISS')">驳回</el-button>
            </template>
            <span v-else style="color:#999;font-size:12px;">-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="prev, pager, next"
        @current-change="fetchReports"
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

const reports = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statusFilter = ref(null)

function reportStatusType(s) { return s === 'PENDING' ? 'warning' : s === 'RESOLVED' ? 'success' : 'info' }
function reportStatusText(s) { return s === 'PENDING' ? '待处理' : s === 'RESOLVED' ? '已处理' : '已驳回' }

async function fetchReports() {
  const res = await adminApi.getReports({ page: page.value, size: size.value, status: statusFilter.value })
  reports.value = res.data?.records || []
  total.value = res.data?.total || 0
}

function search() { page.value = 1; fetchReports() }

async function handle(row, action) {
  await ElMessageBox.prompt('处理说明', action === 'RESOLVE' ? '处理举报' : '驳回举报')
    .then(async ({ value }) => {
      await adminApi.handleReport(row.id, action, value || '已处理')
      ElMessage.success('已完成')
      fetchReports()
    })
    .catch(() => {})
}

onMounted(fetchReports)
</script>

<style scoped>
.admin-shell { display: flex; min-height: 100vh; }
.admin-main { flex: 1; padding: 20px; background: var(--color-bg); overflow-x: auto; }
.admin-main h2 { margin-bottom: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>

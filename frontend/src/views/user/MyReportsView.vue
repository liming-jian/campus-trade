<template>
  <div class="page-container reports-page">
    <div class="page-header">
      <h2>我的举报</h2>
      <p>查看管理员处理进度与审核结果</p>
    </div>

    <div v-if="loading" class="loading-wrap">
      <el-icon class="is-loading" :size="28"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="!reports.length" class="empty-wrap">
        <el-empty description="还没有举报记录" />
      </div>

      <div v-else class="report-list">
        <div v-for="item in reports" :key="item.id" class="report-card">
          <div class="report-head">
            <div class="report-meta">
              <span class="report-id">举报单 #{{ item.id }}</span>
              <span class="target-type">{{ targetTypeText(item.targetType) }}</span>
              <span class="target-id">目标ID：{{ item.targetId }}</span>
            </div>
            <el-tag :type="reportStatusType(item.status)" round>
              {{ reportStatusText(item.status) }}
            </el-tag>
          </div>

          <div class="report-block">
            <div class="block-label">举报原因</div>
            <div class="block-content">{{ item.reason }}</div>
          </div>

          <div class="report-block">
            <div class="block-label">审核进度</div>
            <div class="block-content">
              <template v-if="item.status === 'PENDING'">
                管理员暂未处理，你的举报已提交成功，请耐心等待喵~
              </template>
              <template v-else-if="item.status === 'RESOLVED'">
                管理员已处理完成{{ item.handleResult ? '：' + item.handleResult : '。' }}
              </template>
              <template v-else>
                管理员已驳回{{ item.handleResult ? '：' + item.handleResult : '。' }}
              </template>
            </div>
          </div>

          <div class="report-foot">
            <span>提交时间：{{ formatTime(item.createdAt) }}</span>
            <span v-if="item.updatedAt">更新时间：{{ formatTime(item.updatedAt) }}</span>
          </div>
        </div>
      </div>

      <el-pagination
        v-if="total > size"
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="prev, pager, next"
        @current-change="fetchReports"
        class="pager"
      />
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { productApi } from '@/api/product'

const reports = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

function reportStatusType(status) {
  return status === 'PENDING' ? 'warning' : status === 'RESOLVED' ? 'success' : 'info'
}

function reportStatusText(status) {
  return status === 'PENDING' ? '待审核' : status === 'RESOLVED' ? '已处理' : '已驳回'
}

function targetTypeText(targetType) {
  return targetType === 'PRODUCT' ? '商品举报' : '用户举报'
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ')
}

async function fetchReports() {
  loading.value = true
  try {
    const res = await productApi.getMyReports({ page: page.value, size: size.value })
    reports.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(fetchReports)
</script>

<style scoped>
.reports-page {
  padding: 16px 12px 84px;
}

.page-header {
  background: #fff;
  border-radius: 16px;
  padding: 18px 16px;
  margin-bottom: 12px;
  border: 2px solid var(--color-border);
  box-shadow: var(--shadow-sticker);
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}

.page-header p {
  margin: 8px 0 0;
  color: var(--color-text-muted);
  font-size: 13px;
}

.loading-wrap,
.empty-wrap {
  background: #fff;
  border-radius: 16px;
  min-height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--color-border);
  box-shadow: var(--shadow-sticker);
}

.report-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.report-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  border: 2px solid var(--color-border);
  box-shadow: var(--shadow-sticker);
}

.report-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.report-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  color: var(--color-text-secondary);
  font-size: 12px;
}

.report-id {
  color: var(--color-text-primary);
  font-weight: 700;
}

.target-type {
  color: var(--color-primary);
}

.report-block + .report-block {
  margin-top: 14px;
}

.block-label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 6px;
}

.block-content {
  font-size: 14px;
  color: var(--color-text-primary);
  line-height: 1.7;
  white-space: pre-wrap;
}

.report-foot {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.pager {
  margin-top: 16px;
  justify-content: center;
}
</style>

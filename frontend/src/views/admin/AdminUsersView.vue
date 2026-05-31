<template>
  <div class="admin-shell">
    <AdminSidebar />
    <div class="admin-main">
      <h2>用户管理</h2>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索手机号或昵称" clearable @clear="search" @keyup.enter="search" style="width:240px;" />
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="search" style="width:120px;">
          <el-option label="正常" :value="1" />
          <el-option label="封禁" :value="0" />
        </el-select>
        <el-select v-model="verifyFilter" placeholder="认证筛选" clearable @change="search" style="width:120px;">
          <el-option label="审核中" :value="1" />
          <el-option label="已认证" :value="2" />
          <el-option label="已拒绝" :value="3" />
        </el-select>
      </div>
      <el-table :data="users" border stripe size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column label="认证" width="100">
          <template #default="{ row }">
            <el-tag :type="verifyType(row.verifyStatus)" size="small">{{ verifyText(row.verifyStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="学生证" width="80">
          <template #default="{ row }">
            <el-image v-if="row.studentCardImg" :src="row.studentCardImg" style="width:40px;height:40px;" fit="cover" :preview-src-list="[row.studentCardImg]" preview-teleported />
            <span v-else style="color:#999;font-size:12px;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '封禁' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button v-if="row.verifyStatus === 1" size="small" type="success" @click="approveVerify(row)">通过认证</el-button>
            <el-button v-if="row.verifyStatus === 1" size="small" type="danger" @click="rejectVerify(row)">拒绝认证</el-button>
            <el-button v-if="row.status === 1" size="small" type="warning" @click="warnUser(row)">警告</el-button>
            <el-button v-if="row.status === 1" size="small" type="danger" @click="banUser(row)">封禁</el-button>
            <el-button v-else size="small" type="success" @click="unbanUser(row)">解封</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="prev, pager, next"
        @current-change="fetchUsers"
        style="margin-top:16px;justify-content:center;"
      />
    </div>

    <el-dialog v-model="warnDialog" title="发送警告" width="400px">
      <el-input v-model="warnTitle" placeholder="警告标题" style="margin-bottom:12px;" />
      <el-input v-model="warnContent" type="textarea" :rows="3" placeholder="警告内容" />
      <template #footer>
        <el-button @click="warnDialog = false">取消</el-button>
        <el-button type="primary" @click="doWarn">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import AdminSidebar from './AdminSidebar.vue'

const users = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const statusFilter = ref(null)
const verifyFilter = ref(null)

const warnDialog = ref(false)
const warnTarget = ref(null)
const warnTitle = ref('')
const warnContent = ref('')

function verifyType(s) {
  return s === 2 ? 'success' : s === 1 ? 'warning' : s === 3 ? 'danger' : 'info'
}
function verifyText(s) {
  return s === 0 ? '未认证' : s === 1 ? '审核中' : s === 2 ? '已认证' : '已拒绝'
}

async function fetchUsers() {
  const res = await adminApi.getUsers({ page: page.value, size: size.value, keyword: keyword.value, status: statusFilter.value, verifyStatus: verifyFilter.value })
  users.value = res.data?.records || []
  total.value = res.data?.total || 0
}

function search() {
  page.value = 1
  fetchUsers()
}

async function banUser(row) {
  await ElMessageBox.prompt('请输入封禁原因', '封禁用户', { type: 'warning' })
    .then(async ({ value }) => {
      await adminApi.banUser(row.id, value || '违规行为')
      ElMessage.success('已封禁')
      fetchUsers()
    })
    .catch(() => {})
}

async function unbanUser(row) {
  await adminApi.unbanUser(row.id)
  ElMessage.success('已解封')
  fetchUsers()
}

function warnUser(row) {
  warnTarget.value = row
  warnTitle.value = ''
  warnContent.value = ''
  warnDialog.value = true
}

async function doWarn() {
  if (!warnContent.value) { ElMessage.warning('请输入警告内容'); return }
  await adminApi.warnUser(warnTarget.value.id, warnTitle.value || '系统警告', warnContent.value)
  ElMessage.success('已发送警告')
  warnDialog.value = false
  fetchUsers()
}

async function approveVerify(row) {
  await ElMessageBox.confirm('确认通过该用户的学生认证？', '认证审核')
  await adminApi.verifyUser(row.id, 2, '')
  ElMessage.success('已通过认证')
  fetchUsers()
}

async function rejectVerify(row) {
  await ElMessageBox.prompt('请输入拒绝原因', '拒绝认证', { type: 'warning' })
    .then(async ({ value }) => {
      await adminApi.verifyUser(row.id, 3, value || '材料不符')
      ElMessage.success('已拒绝认证')
      fetchUsers()
    })
    .catch(() => {})
}

onMounted(fetchUsers)
</script>

<style scoped>
.admin-shell { display: flex; min-height: 100vh; }
.admin-main { flex: 1; padding: 20px; background: var(--color-bg); overflow-x: auto; }
.admin-main h2 { margin-bottom: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
</style>

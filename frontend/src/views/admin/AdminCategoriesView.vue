<template>
  <div class="admin-shell">
    <AdminSidebar />
    <div class="admin-main">
      <h2>分类管理</h2>
      <el-button type="primary" @click="openAdd" style="margin-bottom:16px;">添加分类</el-button>
      <el-table :data="categories" border stripe size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="icon" label="图标标识" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑分类' : '添加分类'" width="400px">
      <el-input v-model="form.name" placeholder="分类名称" style="margin-bottom:12px;" />
      <el-input v-model="form.icon" placeholder="图标标识" style="margin-bottom:12px;" />
      <el-input-number v-model="form.sortOrder" :min="0" placeholder="排序" />
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import AdminSidebar from './AdminSidebar.vue'

const categories = ref([])
const dialog = ref(false)
const editing = ref(null)
const form = reactive({ name: '', icon: '', sortOrder: 0 })

async function fetchCategories() {
  const res = await adminApi.getCategories()
  categories.value = res.data || []
}

function openAdd() {
  editing.value = null
  form.name = ''; form.icon = ''; form.sortOrder = 0
  dialog.value = true
}

function openEdit(row) {
  editing.value = row.id
  form.name = row.name; form.icon = row.icon; form.sortOrder = row.sortOrder
  dialog.value = true
}

async function save() {
  if (!form.name) { ElMessage.warning('请输入分类名称'); return }
  if (editing.value) {
    await adminApi.updateCategory(editing.value, { ...form })
  } else {
    await adminApi.createCategory({ ...form })
  }
  ElMessage.success('已保存')
  dialog.value = false
  fetchCategories()
}

async function del(row) {
  await ElMessageBox.confirm('确定删除?', '提示', { type: 'warning' })
  await adminApi.deleteCategory(row.id)
  ElMessage.success('已删除')
  fetchCategories()
}

onMounted(fetchCategories)
</script>

<style scoped>
.admin-shell { display: flex; min-height: 100vh; }
.admin-main { flex: 1; padding: 20px; background: var(--color-bg); overflow-x: auto; }
.admin-main h2 { margin-bottom: 16px; }
</style>

<template>
  <div class="page-container">
    <NavBar title="收货地址" :showBack="true" />
    <div class="page-content">
      <div v-if="addresses.length === 0" class="empty-state">
        <el-icon :size="48" color="#ddd"><MapLocation /></el-icon>
        <p style="margin-top:12px;color:#999;">暂无收货地址</p>
      </div>
      <div v-for="addr in addresses" :key="addr.id" class="address-card" :class="{ default: addr.isDefault }">
        <div class="addr-tag" v-if="addr.isDefault">默认</div>
        <div class="addr-info">
          <div class="addr-contact">
            <span class="name">{{ addr.receiverName }}</span>
            <span class="phone">{{ addr.receiverPhone }}</span>
          </div>
          <div class="addr-detail">{{ formatRegion(addr) }} {{ addr.detail }}</div>
        </div>
        <div class="addr-actions">
          <el-button text type="primary" @click="editAddress(addr)">编辑</el-button>
          <el-button v-if="!addr.isDefault" text type="primary" @click="setDefault(addr.id)">设为默认</el-button>
          <el-button text type="danger" @click="deleteAddress(addr.id)">删除</el-button>
        </div>
      </div>
    </div>

    <div class="bottom-btn-wrapper">
      <el-button type="primary" class="add-btn" @click="showDialog = true">添加收货地址</el-button>
    </div>

    <el-dialog v-model="showDialog" :title="editing ? '编辑地址' : '添加地址'" width="90%" destroy-on-close>
      <el-form :model="form" label-width="80px" size="large">
        <el-form-item label="收货人">
          <el-input v-model="form.receiverName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="地区">
          <el-cascader
            v-model="regionValue"
            :options="regionOptions"
            :props="{ expandTrigger: 'hover' }"
            placeholder="请选择省 / 市 / 区县"
            filterable
            clearable
            style="width: 100%;"
            @change="handleRegionChange"
          />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.detail" type="textarea" placeholder="街道、楼栋、门牌号" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MapLocation } from '@element-plus/icons-vue'
import { pcaTextArr } from 'element-china-area-data'
import { addressApi } from '@/api/address'
import NavBar from '@/components/NavBar.vue'

const regionOptions = pcaTextArr
const addresses = ref([])
const showDialog = ref(false)
const editing = ref(null)
const regionValue = ref([])

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0
})

async function fetchAddresses() {
  const res = await addressApi.getList()
  addresses.value = res.data || []
}

function handleRegionChange(value) {
  form.province = value?.[0] || ''
  form.city = value?.[1] || ''
  form.district = value?.[2] || ''
}

function resetForm() {
  form.receiverName = ''
  form.receiverPhone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.detail = ''
  form.isDefault = 0
  regionValue.value = []
  editing.value = null
}

function formatRegion(addr) {
  return [addr.province, addr.city, normalizeDistrict(addr.district)].filter(Boolean).join('')
}

function normalizeDistrict(district) {
  return district === '市辖区' ? '' : district
}

function hasDistrict(province, city, district) {
  const provinceNode = regionOptions.find(item => item.label === province)
  const cityNode = provinceNode?.children?.find(item => item.label === city)
  return !!cityNode?.children?.some(item => item.label === district)
}

function getRegionPath(addr) {
  if (hasDistrict(addr.province, addr.city, addr.district)) {
    return [addr.province, addr.city, addr.district]
  }
  if (addr.province && addr.city) {
    return [addr.province, addr.city]
  }
  return [addr.province, addr.city, addr.district].filter(Boolean)
}

function editAddress(addr) {
  Object.assign(form, {
    receiverName: addr.receiverName,
    receiverPhone: addr.receiverPhone,
    province: addr.province,
    city: addr.city,
    district: normalizeDistrict(addr.district),
    detail: addr.detail,
    isDefault: addr.isDefault
  })
  regionValue.value = getRegionPath(addr)
  editing.value = addr.id
  showDialog.value = true
}

async function saveAddress() {
  if (!form.receiverName || !form.receiverPhone || !form.province || !form.city || !form.district || !form.detail) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (editing.value) {
    await addressApi.update(editing.value, { ...form })
    ElMessage.success('地址已更新')
  } else {
    await addressApi.create({ ...form })
    ElMessage.success('地址已添加')
  }
  showDialog.value = false
  resetForm()
  fetchAddresses()
}

async function setDefault(id) {
  await addressApi.setDefault(id)
  ElMessage.success('已设为默认地址')
  fetchAddresses()
}

async function deleteAddress(id) {
  await ElMessageBox.confirm('确定要删除这个地址吗?', '提示', { type: 'warning' })
  await addressApi.delete(id)
  ElMessage.success('已删除')
  fetchAddresses()
}

watch(showDialog, (visible) => {
  if (!visible) resetForm()
})

onMounted(fetchAddresses)
</script>

<style scoped>
.address-card {
  background: white;
  border-radius: var(--radius);
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: var(--shadow-card);
  position: relative;
}
.address-card.default {
  border: 1px solid var(--color-primary);
}
.addr-tag {
  position: absolute;
  top: 0;
  left: 0;
  background: var(--color-primary);
  color: white;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 0 0 var(--radius) 0;
}
.addr-contact {
  margin-bottom: 6px;
}
.name { font-weight: 600; margin-right: 16px; }
.phone { color: var(--color-text-secondary); }
.addr-detail {
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.5;
}
.addr-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--color-border);
}
.bottom-btn-wrapper {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: white;
  border-top: 1px solid var(--color-border);
}
.add-btn {
  width: 100%;
  border-radius: 20px;
}
</style>

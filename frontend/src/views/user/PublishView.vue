<template>
  <div class="publish-page">
    <div class="pub-header">
      <span class="pub-back" @click="$router.back()">返回</span>
      <h2 class="pub-title">发布闲置</h2>
      <span></span>
    </div>

    <div class="pub-body">
      <!-- Images -->
      <div class="pub-section">
        <label class="pub-label">添加图片 (最多9张)</label>
        <div class="img-grid">
          <div v-for="(img, i) in previews" :key="i" class="img-cell" @click="removeImg(i)">
            <img :src="img" />
            <span class="img-remove">x</span>
          </div>
          <div v-if="previews.length < 9" class="img-add" @click="triggerUpload">
            <span class="add-icon">+</span>
            <span class="add-text">{{ previews.length }}/9</span>
          </div>
        </div>
        <input ref="fileInput" type="file" accept="image/*" multiple hidden @change="handleFiles" />
      </div>

      <!-- Title -->
      <div class="pub-field">
        <label class="pub-label">标题</label>
        <input v-model="form.title" class="pub-input" placeholder="请输入商品标题 (最多30字)" maxlength="30" />
      </div>

      <!-- Category -->
      <div class="pub-field">
        <label class="pub-label">分类</label>
        <select v-model="form.categoryId" class="pub-select">
          <option :value="null" disabled>选择分类</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
      </div>

      <!-- Condition -->
      <div class="pub-field">
        <label class="pub-label">成色</label>
        <div class="cond-row">
          <span v-for="c in conditions" :key="c.value" class="cond-chip" :class="{ active: form.condition === c.value }" @click="form.condition = c.value">{{ c.label }}</span>
        </div>
      </div>

      <!-- Price -->
      <div class="pub-field-row">
        <div class="pub-field" style="flex:1;">
          <label class="pub-label">售价</label>
          <input v-model="form.price" class="pub-input" type="number" placeholder="0.00" step="0.01" min="0" />
        </div>
        <div class="pub-field" style="flex:1;">
          <label class="pub-label">原价 (选填)</label>
          <input v-model="form.originalPrice" class="pub-input" type="number" placeholder="0.00" step="0.01" min="0" />
        </div>
      </div>

      <!-- Shipping -->
      <div class="pub-field">
        <label class="pub-label">运费</label>
        <div class="ship-row">
          <span class="cond-chip" :class="{ active: freeShip }" @click="freeShip = true">包邮</span>
          <span class="cond-chip" :class="{ active: !freeShip }" @click="freeShip = false">自付运费</span>
          <input v-if="!freeShip" v-model="form.shippingFee" class="pub-input" style="width:100px;" type="number" placeholder="0.00" step="0.01" min="0" />
        </div>
      </div>

      <!-- Description -->
      <div class="pub-field">
        <label class="pub-label">描述</label>
        <textarea v-model="form.description" class="pub-textarea" placeholder="描述商品成色、购买渠道、使用时长、转手原因..." rows="5" maxlength="500"></textarea>
      </div>

      <button class="pub-submit" :disabled="loading || !canSubmit" @click="submit">
        <span v-if="!loading">发 布</span>
        <span v-else>发布中...</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productApi } from '@/api/product'
import request from '@/api/request'

const router = useRouter()
const fileInput = ref(null)
const previews = ref([])
const files = ref([])
const categories = ref([])
const loading = ref(false)
const freeShip = ref(true)

const form = reactive({
  title: '', categoryId: null, condition: 'NEW',
  price: '', originalPrice: '', shippingFee: '0',
  description: ''
})

const conditions = [
  { value: 'NEW', label: '几乎全新' },
  { value: 'USED', label: '轻微使用' },
  { value: 'OLD', label: '老旧' }
]

const canSubmit = computed(() => form.title && form.categoryId && form.price && previews.value.length > 0)

function triggerUpload() { fileInput.value?.click() }

function handleFiles(e) {
  const newFiles = Array.from(e.target.files || [])
  for (const f of newFiles) {
    if (previews.value.length >= 9) break
    files.value.push(f)
    previews.value.push(URL.createObjectURL(f))
  }
  e.target.value = ''
}

function removeImg(i) {
  previews.value.splice(i, 1)
  files.value.splice(i, 1)
}

async function fetchCategories() {
  try { const res = await request.get('/categories'); categories.value = res.data || []; } catch {}
}

async function submit() {
  if (!canSubmit.value) { ElMessage.warning('请填写必填项并上传图片'); return }
  loading.value = true
  try {
    const data = {
      categoryId: form.categoryId, title: form.title,
      description: form.description || '', price: parseFloat(form.price),
      originalPrice: form.originalPrice ? parseFloat(form.originalPrice) : null,
      condition: form.condition,
      shippingFree: freeShip.value ? 1 : 0,
      shippingFee: freeShip.value ? 0 : parseFloat(form.shippingFee || '0')
    }
    await productApi.create(data, files.value)
    ElMessage.success('发布成功')
    router.push('/home')
  } catch {} finally { loading.value = false }
}

onMounted(fetchCategories)
</script>

<style scoped>
.publish-page { min-height: 100vh; background: var(--color-warm-white); padding-bottom: 40px; }
.pub-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: var(--color-ink); color: white; position: sticky; top: 0; z-index: 50; }
.pub-back { font-size: 14px; cursor: pointer; color: var(--color-accent); }
.pub-title { font-family: var(--font-display); font-size: 18px; letter-spacing: 2px; }
.pub-body { padding: 16px; display: flex; flex-direction: column; gap: 16px; }
.pub-section, .pub-field { display: flex; flex-direction: column; gap: 6px; }
.pub-label { font-size: 12px; font-weight: 700; color: var(--color-ink-light); text-transform: uppercase; letter-spacing: 1px; }
.img-grid { display: flex; flex-wrap: wrap; gap: 10px; }
.img-cell { width: 80px; height: 80px; border-radius: var(--radius-sm); overflow: hidden; position: relative; border: 2px solid var(--color-border); cursor: pointer; }
.img-cell img { width: 100%; height: 100%; object-fit: cover; }
.img-remove { position: absolute; top: 2px; right: 4px; color: var(--color-hot); font-weight: 700; font-size: 14px; }
.img-add { width: 80px; height: 80px; border-radius: var(--radius-sm); border: 2px dashed var(--color-border); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 2px; cursor: pointer; color: var(--color-muted); }
.add-icon { font-size: 24px; }
.add-text { font-size: 10px; }
.pub-input, .pub-select, .pub-textarea { padding: 12px; border: 2px solid var(--color-border); border-radius: var(--radius); font-size: 14px; font-family: var(--font-body); outline: none; background: white; transition: border-color 0.2s; resize: vertical; }
.pub-input:focus, .pub-select:focus, .pub-textarea:focus { border-color: var(--color-primary); }
.pub-select { appearance: none; background: white url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath d='M6 8L1 3h10z' fill='%23999'/%3E%3C/svg%3E") no-repeat right 12px center; padding-right: 32px; }
.pub-field-row { display: flex; gap: 12px; }
.cond-row, .ship-row { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.cond-chip { padding: 6px 16px; border-radius: 50px; font-size: 13px; font-weight: 600; border: 2px solid var(--color-border); cursor: pointer; white-space: nowrap; }
.cond-chip.active { background: var(--color-ink); color: white; border-color: var(--color-ink); }
.pub-submit { width: 100%; padding: 14px; background: var(--color-primary); color: white; border: 2px solid var(--color-ink); border-radius: 50px; font-family: var(--font-display); font-size: 16px; letter-spacing: 4px; cursor: pointer; box-shadow: 3px 3px 0px rgba(0,0,0,0.12); margin-top: 8px; }
.pub-submit:active { transform: translate(1px, 1px); box-shadow: none; }
.pub-submit:disabled { background: #ccc; border-color: #bbb; box-shadow: none; cursor: not-allowed; }
</style>

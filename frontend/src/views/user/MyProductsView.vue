<template>
  <div class="page-container">
    <div class="page-header">
      <span class="back-btn" @click="$router.back()">
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </span>
      <h2>我的发布</h2>
    </div>

    <div v-if="loading" class="loading-state">加载中...</div>

    <div v-else-if="products.length === 0" class="empty-state">
      <p>还没有发布过商品</p>
      <el-button type="primary" round @click="$router.push('/publish')">去发布</el-button>
    </div>

    <div v-else class="product-list">
      <div v-for="p in products" :key="p.id" class="product-item" @click="$router.push('/product/' + p.id)">
        <img :src="p.coverImage || '/sample-products/iphone-13-midnight.png'" class="item-img" />
        <div class="item-info">
          <div class="item-title">{{ p.title }}</div>
          <div class="item-price">￥{{ p.price }}</div>
        </div>
        <div class="item-side">
          <div class="item-status" :class="'status-' + p.status">
            {{ statusText(p.status) }}
          </div>
          <el-button
            type="danger"
            plain
            size="small"
            class="delete-btn"
            @click.stop="removeProduct(p)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

    <TabBar />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { productApi } from '@/api/product'
import TabBar from '@/components/TabBar.vue'

const products = ref([])
const loading = ref(true)

function statusText(s) {
  if (s === 'REVIEW') return '审核中'
  if (s === 'PASS') return '已通过'
  if (s === 'REJECT') return '未通过'
  if (s === 'SOLD') return '已售出'
  if (s === 'OFF') return '已下架'
  return s
}

async function fetchMyProducts() {
  loading.value = true
  try {
    const res = await productApi.getMyProducts({ page: 1, size: 50 })
    products.value = res.data?.records || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function removeProduct(product) {
  try {
    await ElMessageBox.confirm(`确定删除“${product.title}”吗？删除后将从列表中移除。`, '删除商品', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await productApi.delete(product.id)
    products.value = products.value.filter(item => item.id !== product.id)
    ElMessage.success('商品已删除')
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      console.error(e)
    }
  }
}

onMounted(fetchMyProducts)
</script>

<style scoped>
.page-container { padding-bottom: 80px; }
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid var(--color-border);
  background: white;
  position: sticky;
  top: 0;
  z-index: 10;
}
.page-header h2 { font-size: 17px; font-weight: 700; }
.back-btn { cursor: pointer; display: flex; }

.loading-state, .empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--color-muted);
}

.product-list { padding: 12px; display: flex; flex-direction: column; gap: 10px; }
.product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  cursor: pointer;
}
.item-img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}
.item-info { flex: 1; min-width: 0; }
.item-title {
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.item-price { font-size: 13px; color: var(--color-primary); margin-top: 4px; }

.item-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.item-status {
  font-size: 12px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 20px;
  white-space: nowrap;
}
.delete-btn {
  min-width: 64px;
}
.status-REVIEW { background: #FFF3E0; color: #E65100; }
.status-PASS { background: #E8F5E9; color: #2E7D32; }
.status-REJECT { background: #FFEBEE; color: #C62828; }
.status-SOLD { background: #E3F2FD; color: #1565C0; }
.status-OFF { background: #F5F5F5; color: #757575; }
</style>

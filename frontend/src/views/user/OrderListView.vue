<template>
  <div class="order-page">
    <div class="order-header">
      <h2 class="order-title">我的订单</h2>
    </div>

    <!-- Role Tabs -->
    <div class="role-tabs">
      <span v-for="r in roles" :key="r.value" class="role-tab" :class="{ active: activeRole === r.value }" @click="switchRole(r.value)">{{ r.label }}</span>
    </div>

    <!-- Status Filter -->
    <div class="status-row">
      <span v-for="s in statusFilters" :key="s.value" class="status-chip" :class="{ active: activeStatus === s.value }" @click="switchStatus(s.value)">{{ s.label }}</span>
    </div>

    <!-- Orders -->
    <div class="order-list">
      <div v-for="o in orders" :key="o.id" class="order-card anim-fade-in">
        <div class="order-card-top">
          <span class="order-seller">{{ activeRole === 'BUYER' ? '卖家: ' + (o.sellerNickname || '同学') : '买家: ' + (o.buyerNickname || '同学') }}</span>
          <span class="order-status" :style="{ color: statusColor(o.status) }">{{ statusText(o.status) }}</span>
        </div>
        <div class="order-card-body" @click="$router.push('/product/' + o.productId)">
          <img v-if="o.productImages?.length" :src="o.productImages[0]" class="order-img" />
          <div class="order-info">
            <div class="order-name">{{ o.productTitle }}</div>
            <div class="order-price"><span class="yen">￥</span>{{ o.totalAmount }}</div>
          </div>
        </div>
        <div class="order-actions" v-if="o.status !== 'COMPLETED' && o.status !== 'CANCELLED'">
          <button v-if="activeRole === 'BUYER' && o.status === 'PENDING_PAY'" class="act-btn primary" @click="pay(o)">去付款</button>
          <button v-if="activeRole === 'BUYER' && o.status === 'PENDING_RECEIVE'" class="act-btn primary" @click="confirm(o)">确认收货</button>
          <button v-if="activeRole === 'SELLER' && o.status === 'PENDING_SHIP'" class="act-btn primary" @click="ship(o)">去发货</button>
          <button v-if="canCancel(o)" class="act-btn" @click="cancelOrder(o)">取消订单</button>
        </div>
      </div>
    </div>

    <div v-if="orders.length === 0 && !loading" class="empty-state">
      <div class="empty-icon">0</div>
      <p>暂无订单</p>
    </div>

    <div v-if="loading" class="feed-status">加载中...</div>

    <TabBar />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/order'
import TabBar from '@/components/TabBar.vue'

const activeRole = ref('BUYER')
const activeStatus = ref(null)
const orders = ref([])
const loading = ref(false)

const roles = [{ label: '我买到的', value: 'BUYER' }, { label: '我卖出的', value: 'SELLER' }]
const statusFilters = [
  { label: '全部', value: null },
  { label: '待付款', value: 'PENDING_PAY' },
  { label: '待发货', value: 'PENDING_SHIP' },
  { label: '待收货', value: 'PENDING_RECEIVE' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

function statusText(s) {
  const m = { PENDING_PAY: '待付款', PENDING_SHIP: '待发货', PENDING_RECEIVE: '待收货', COMPLETED: '已完成', CANCELLED: '已取消' }
  return m[s] || s
}
function statusColor(s) {
  const m = { PENDING_PAY: '#FF5500', PENDING_SHIP: '#4A90D9', PENDING_RECEIVE: '#9B59B6', COMPLETED: '#00C853', CANCELLED: '#999' }
  return m[s] || '#999'
}
function canCancel(o) { return (o.status === 'PENDING_PAY' || o.status === 'PENDING_SHIP') }

async function fetchOrders() {
  loading.value = true
  try {
    const res = await orderApi.getList({ role: activeRole.value, status: activeStatus.value, page: 1, size: 50 })
    orders.value = res.data?.records || []
  } catch {} finally { loading.value = false }
}

function switchRole(v) { activeRole.value = v; fetchOrders() }
function switchStatus(v) { activeStatus.value = v; fetchOrders() }

async function pay(o) { await orderApi.pay(o.id); ElMessage.success('付款成功'); fetchOrders() }
async function ship(o) { await orderApi.ship(o.id); ElMessage.success('已发货'); fetchOrders() }

async function confirm(o) {
  await ElMessageBox.confirm('确认已收到商品?', '确认收货', { type: 'warning' })
  await orderApi.confirm(o.id)
  ElMessage.success('交易完成')
  fetchOrders()
}

async function cancelOrder(o) {
  const { value } = await ElMessageBox.prompt('取消原因', '取消订单', { inputType: 'textarea' })
  await orderApi.cancel(o.id, value || '无')
  ElMessage.success('已取消')
  fetchOrders()
}

onMounted(fetchOrders)
</script>

<style scoped>
.order-page { min-height: 100vh; background: var(--color-warm-white); padding-bottom: 80px; }
.order-header { background: var(--color-ink); padding: 16px; }
.order-title { color: white; font-family: var(--font-display); font-size: 20px; letter-spacing: 2px; margin: 0; }

.role-tabs { display: flex; background: white; border-bottom: 2px solid var(--color-border); }
.role-tab { flex: 1; text-align: center; padding: 12px; font-weight: 600; cursor: pointer; font-size: 14px; color: var(--color-muted); transition: all 0.15s; }
.role-tab.active { color: var(--color-primary); border-bottom: 3px solid var(--color-primary); margin-bottom: -2px; }

.status-row { display: flex; gap: 6px; padding: 10px 12px; overflow-x: auto; scrollbar-width: none; background: white; border-bottom: 1px solid var(--color-border); }
.status-row::-webkit-scrollbar { display: none; }
.status-chip { flex-shrink: 0; padding: 5px 14px; border-radius: 50px; font-size: 12px; font-weight: 600; border: 1.5px solid var(--color-border); cursor: pointer; white-space: nowrap; }
.status-chip.active { background: var(--color-ink); color: white; border-color: var(--color-ink); }

.order-list { padding: 10px 12px; display: flex; flex-direction: column; gap: 10px; }
.order-card { background: white; border: 2px solid var(--color-border); border-radius: var(--radius); padding: 12px; box-shadow: var(--shadow-sticker); }
.order-card-top { display: flex; justify-content: space-between; margin-bottom: 10px; }
.order-seller { font-size: 12px; color: var(--color-muted); }
.order-status { font-size: 12px; font-weight: 700; }
.order-card-body { display: flex; gap: 12px; cursor: pointer; }
.order-img { width: 80px; height: 80px; border-radius: var(--radius-sm); object-fit: cover; border: 1.5px solid var(--color-border); flex-shrink: 0; }
.order-info { flex: 1; min-width: 0; }
.order-name { font-size: 14px; line-height: 1.4; margin-bottom: 8px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.order-price { font-family: var(--font-display); font-size: 18px; color: var(--color-primary); }
.yen { font-size: 11px; }

.order-actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 10px; padding-top: 10px; border-top: 1px solid var(--color-border); }
.act-btn { padding: 6px 18px; border-radius: 50px; font-size: 12px; font-weight: 600; border: 1.5px solid var(--color-border); background: white; cursor: pointer; transition: all 0.15s; }
.act-btn.primary { background: var(--color-primary); color: white; border-color: var(--color-ink); }

.empty-icon { width: 64px; height: 64px; border-radius: 50%; background: var(--color-surface); border: 3px dashed var(--color-border); display: flex; align-items: center; justify-content: center; font-family: var(--font-display); font-size: 24px; color: var(--color-muted); margin-bottom: 12px; }
.feed-status { text-align: center; padding: 24px; color: var(--color-muted); }
</style>

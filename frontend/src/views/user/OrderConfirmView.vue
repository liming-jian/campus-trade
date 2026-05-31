<template>
  <div class="confirm-page">
    <div class="confirm-header">
      <span class="confirm-back" @click="$router.back()">返回</span>
      <h2 class="confirm-title">确认订单</h2>
      <span></span>
    </div>

    <div v-if="loading" class="loading-box">加载中...</div>
    <template v-else-if="product">
      <!-- Address -->
      <div class="confirm-section" @click="showAddress = true">
        <div class="section-label">收货地址</div>
        <div v-if="selectedAddr" class="addr-card">
          <div class="addr-line1">{{ selectedAddr.receiverName }} {{ selectedAddr.receiverPhone }}</div>
          <div class="addr-line2">{{ selectedAddr.province }}{{ selectedAddr.city }}{{ selectedAddr.district }} {{ selectedAddr.detail }}</div>
        </div>
        <div v-else class="addr-empty">点击选择收货地址</div>
        <span class="arrow">&gt;</span>
      </div>

      <!-- Product -->
      <div class="confirm-section">
        <div class="section-label">商品信息</div>
        <div class="product-row">
          <img v-if="product.images?.length" :src="product.images[0]" class="product-thumb" />
          <div class="product-info">
            <div class="product-title">{{ product.title }}</div>
            <div class="product-price"><span class="yen">￥</span>{{ product.price }}</div>
          </div>
        </div>
      </div>

      <!-- Price Summary -->
      <div class="confirm-section">
        <div class="summary-row"><span>商品金额</span><span>￥{{ product.price }}</span></div>
        <div class="summary-row"><span>运费</span><span>{{ shippingText }}</span></div>
        <div class="summary-row total"><span>合计</span><span class="total-price">￥{{ totalAmount }}</span></div>
      </div>

      <!-- Remark -->
      <div class="confirm-section">
        <input v-model="remark" class="remark-input" placeholder="备注 (选填)" maxlength="100" />
      </div>

      <!-- Submit -->
      <div class="confirm-bottom">
        <div class="bottom-total">
          <span>合计: </span>
          <span class="bottom-price">￥{{ totalAmount }}</span>
        </div>
        <button class="submit-btn" :disabled="!selectedAddr || submitting" @click="submitOrder">
          {{ submitting ? '提交中...' : '提交订单' }}
        </button>
      </div>
    </template>

    <!-- Address Picker Modal -->
    <div v-if="showAddress" class="modal-mask" @click.self="showAddress = false">
      <div class="modal-sheet">
        <div class="modal-header">
          <span>选择地址</span>
          <span class="modal-close" @click="showAddress = false">关闭</span>
        </div>
        <div v-if="addresses.length === 0" class="addr-empty" style="padding:24px;">
          暂无地址，<router-link to="/address" style="color:var(--color-primary);">去添加</router-link>
        </div>
        <div v-for="addr in addresses" :key="addr.id" class="addr-option" :class="{ selected: selectedAddr?.id === addr.id }" @click="selectAddr(addr)">
          <div class="addr-tick">{{ selectedAddr?.id === addr.id ? 'V' : '' }}</div>
          <div>
            <div class="addr-line1">{{ addr.receiverName }} {{ addr.receiverPhone }}</div>
            <div class="addr-line2">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { orderApi } from '@/api/order'
import { addressApi } from '@/api/address'
import { productApi } from '@/api/product'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const addresses = ref([])
const selectedAddr = ref(null)
const remark = ref('')
const loading = ref(true)
const submitting = ref(false)
const showAddress = ref(false)

const shippingFee = computed(() => product.value?.shippingFree ? 0 : (product.value?.shippingFee || 0))
const shippingText = computed(() => shippingFee.value === 0 ? '包邮' : '￥' + shippingFee.value)
const totalAmount = computed(() => {
  const price = parseFloat(product.value?.price || 0)
  return (price + parseFloat(shippingFee.value || 0)).toFixed(2)
})

function selectAddr(addr) {
  selectedAddr.value = addr
  showAddress.value = false
}

async function submitOrder() {
  if (!selectedAddr.value) { ElMessage.warning('请选择收货地址'); return }
  submitting.value = true
  try {
    await orderApi.create({ productId: product.value.id, addressId: selectedAddr.value.id, remark: remark.value })
    ElMessage.success('下单成功')
    router.push('/orders')
  } catch {} finally { submitting.value = false }
}

onMounted(async () => {
  try {
    const pid = route.query.productId
    const [prodRes, addrRes] = await Promise.all([
      productApi.getDetail(pid),
      addressApi.getList()
    ])
    product.value = prodRes.data
    addresses.value = addrRes.data || []
    selectedAddr.value = addresses.value.find(a => a.isDefault) || addresses.value[0] || null
  } catch {} finally { loading.value = false }
})
</script>

<style scoped>
.confirm-page { min-height: 100vh; background: var(--color-warm-white); padding-bottom: 80px; }
.confirm-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: var(--color-ink); color: white; position: sticky; top: 0; z-index: 50; }
.confirm-back { font-size: 14px; cursor: pointer; color: var(--color-accent); }
.confirm-title { font-family: var(--font-display); font-size: 18px; letter-spacing: 2px; }

.confirm-section { background: white; margin: 10px 12px; border-radius: var(--radius); border: 2px solid var(--color-border); padding: 14px; box-shadow: var(--shadow-sticker); position: relative; cursor: pointer; }
.section-label { font-size: 12px; font-weight: 700; color: var(--color-ink-light); text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; }
.arrow { position: absolute; right: 14px; top: 50%; transform: translateY(-50%); color: var(--color-muted); }
.addr-line1 { font-weight: 600; margin-bottom: 2px; }
.addr-line2 { font-size: 13px; color: var(--color-muted); }
.addr-empty { color: var(--color-muted); font-size: 14px; }

.product-row { display: flex; gap: 12px; }
.product-thumb { width: 80px; height: 80px; border-radius: var(--radius-sm); object-fit: cover; border: 1.5px solid var(--color-border); }
.product-info { flex: 1; }
.product-title { font-size: 14px; line-height: 1.5; margin-bottom: 8px; }
.product-price { font-family: var(--font-display); font-size: 18px; color: var(--color-primary); }
.yen { font-size: 11px; }

.summary-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; }
.summary-row.total { border-top: 1px dashed var(--color-border); margin-top: 6px; padding-top: 10px; font-weight: 700; }
.total-price { font-family: var(--font-display); font-size: 20px; color: var(--color-primary); }

.remark-input { width: 100%; padding: 10px 14px; border: 2px solid var(--color-border); border-radius: var(--radius); font-size: 14px; outline: none; }
.remark-input:focus { border-color: var(--color-primary); }

.confirm-bottom { position: fixed; bottom: 0; left: 0; right: 0; background: white; border-top: 2px solid var(--color-border); padding: 12px 16px; display: flex; justify-content: space-between; align-items: center; z-index: 50; }
.bottom-price { font-family: var(--font-display); font-size: 22px; color: var(--color-primary); }
.submit-btn { padding: 12px 28px; background: var(--color-primary); color: white; border: 2px solid var(--color-ink); border-radius: 50px; font-family: var(--font-display); font-size: 15px; letter-spacing: 2px; cursor: pointer; box-shadow: 3px 3px 0px rgba(0,0,0,0.12); }
.submit-btn:active { transform: translate(1px, 1px); box-shadow: none; }
.submit-btn:disabled { background: #ccc; border-color: #bbb; box-shadow: none; cursor: not-allowed; }

.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); z-index: 100; display: flex; align-items: flex-end; }
.modal-sheet { width: 100%; background: white; border-radius: var(--radius-lg) var(--radius-lg) 0 0; max-height: 60vh; overflow-y: auto; padding-bottom: 20px; }
.modal-header { display: flex; justify-content: space-between; padding: 16px; border-bottom: 1px solid var(--color-border); font-weight: 700; }
.modal-close { color: var(--color-primary); cursor: pointer; }
.addr-option { display: flex; gap: 12px; padding: 14px 16px; border-bottom: 1px solid var(--color-border); cursor: pointer; }
.addr-option.selected { background: var(--color-surface); }
.addr-tick { width: 22px; height: 22px; border-radius: 50%; border: 2px solid var(--color-border); display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; color: var(--color-primary); flex-shrink: 0; }
.addr-option.selected .addr-tick { background: var(--color-primary); color: white; border-color: var(--color-primary); }

.loading-box { text-align: center; padding: 60px; color: var(--color-muted); }
</style>

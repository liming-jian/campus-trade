<template>
  <div class="page-container">
    <!-- Loading -->
    <div v-if="loading" class="loading-page">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-page">
      <el-icon :size="64" color="#ddd"><WarningFilled /></el-icon>
      <p style="margin-top:16px;color:#999;">{{ error }}</p>
      <el-button type="primary" round style="margin-top:16px;" @click="$router.push('/home')">返回首页</el-button>
    </div>

    <template v-else-if="product">
      <!-- Image Carousel -->
      <div class="img-carousel">
        <div class="image-scroll" v-if="displayImages.length > 1">
          <img v-for="(img, i) in displayImages" :key="i" :src="img" class="carousel-img" @error="handleImageError" />
        </div>
        <img v-else :src="displayImages[0]" class="carousel-img" @error="handleImageError" />
        <div class="image-count" v-if="displayImages.length > 1">{{ displayImages.length }} 张图片，左右滑动查看</div>
        <!-- Back button overlay -->
        <span class="back-overlay" @click="$router.back()">
          <el-icon :size="22"><ArrowLeft /></el-icon>
        </span>
      </div>

      <!-- Price & Title -->
      <div class="info-section card-section">
        <div class="price-row">
          <span class="price-main">
            <span class="price-sym">￥</span>{{ product.price }}
          </span>
          <span v-if="product.originalPrice" class="price-origin">￥{{ product.originalPrice }}</span>
          <span class="condition-tag" :class="conditionClass">{{ conditionText }}</span>
        </div>
        <h1 class="product-title">{{ product.title }}</h1>
        <div class="meta-row">
          <span v-if="product.shippingFree !== false" class="free-ship">包邮</span>
          <span class="view-count">
            <el-icon :size="14"><View /></el-icon> {{ product.viewCount || 0 }} 次浏览
          </span>
        </div>
      </div>

      <!-- Seller Card -->
      <div class="card-section seller-card" @click="!isOwner && contactSeller()">
        <div class="seller-left">
          <el-avatar :size="44" :src="product.sellerAvatar">
            <el-icon :size="24"><UserFilled /></el-icon>
          </el-avatar>
          <div class="seller-info">
            <div class="seller-name">
              {{ product.sellerNickname || '匿名用户' }}
              <span v-if="product.sellerVerified" class="verified-tag">已认证</span>
            </div>
            <div class="seller-school" v-if="product.sellerVerified && product.sellerSchoolName">
              {{ product.sellerSchoolName }}
            </div>
            <div class="seller-hint">信用良好 | 回复较快</div>
          </div>
        </div>
        <el-button v-if="!isOwner" size="small" round plain type="primary" @click.stop="contactSeller">联系卖家</el-button>
        <el-button v-else size="small" round plain type="danger" @click.stop="removeProduct">删除商品</el-button>
      </div>

      <!-- Description -->
      <div class="card-section">
        <div class="section-title">商品描述</div>
        <div class="desc-content">{{ product.description || '卖家很懒，什么都没写~' }}</div>
      </div>

      <!-- Safety Tip -->
      <div class="safety-tip">
        <el-icon :size="16"><Warning /></el-icon>
        <span>安全提示：请当面验货后交易，谨防上当受骗</span>
        <span class="report-link" @click="reportProduct" v-if="product.userId !== currentUserId">举报</span>
      </div>

      <!-- Bottom Action Bar -->
      <div class="bottom-bar" v-if="!isOwner">
        <div class="bar-left">
          <div class="fav-btn" @click="toggleFavorite">
            <el-icon :size="22" :color="isFavorited ? '#FF6B00' : '#999'">
              <StarFilled v-if="isFavorited" />
              <Star v-else />
            </el-icon>
            <span :style="{ color: isFavorited ? '#FF6B00' : '#999' }">{{ isFavorited ? '已收藏' : '收藏' }}</span>
          </div>
        </div>
        <div class="bar-right">
          <el-button class="want-btn" round @click="contactSeller">我想要</el-button>
          <el-button type="primary" class="buy-btn" round @click="buyNow">立即购买</el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, View, UserFilled, Warning, WarningFilled, Loading, Star, StarFilled } from '@element-plus/icons-vue'
import { productApi } from '@/api/product'
import { favoriteApi } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const currentUserId = computed(() => userStore.user?.id)
const isOwner = computed(() => product.value?.userId === currentUserId.value)

const product = ref(null)
const loading = ref(true)
const error = ref(null)
const isFavorited = ref(false)
const imageFailed = ref(false)

const conditionText = computed(() => {
  if (!product.value) return ''
  const c = product.value.condition
  if (c === 'NEW') return '几乎全新'
  if (c === 'USED') return '轻微使用'
  return '老旧'
})

const conditionClass = computed(() => {
  if (!product.value) return ''
  const c = product.value.condition
  if (c === 'NEW') return 'cond-new'
  if (c === 'USED') return 'cond-used'
  return 'cond-old'
})

const fallbackCovers = [
  { keywords: ['iphone', '手机'], url: 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?auto=format&fit=crop&w=1200&q=80' },
  { keywords: ['高等数学', '教材', '英语', '书'], url: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?auto=format&fit=crop&w=1200&q=80' },
  { keywords: ['macbook', '电脑', '笔记本'], url: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1200&q=80' },
  { keywords: ['键盘'], url: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=1200&q=80' },
  { keywords: ['冰箱'], url: 'https://images.unsplash.com/photo-1571175443880-49e1d25b2bc5?auto=format&fit=crop&w=1200&q=80' },
  { keywords: ['椅', '凳'], url: 'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=1200&q=80' },
  { keywords: ['nike', 'air force', '鞋'], url: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=80' },
  { keywords: ['化妆', '粉底', '兰蔻'], url: 'https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=1200&q=80' }
]

const defaultCover = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80'

const displayImages = computed(() => {
  if (!imageFailed.value && product.value?.images?.length) {
    return product.value.images.slice(0, 9)
  }
  return [fallbackCover()]
})

function fallbackCover() {
  const title = (product.value?.title || '').toLowerCase()
  const matched = fallbackCovers.find(item => item.keywords.some(keyword => title.includes(keyword.toLowerCase())))
  return matched?.url || defaultCover
}

function handleImageError(event) {
  imageFailed.value = true
  event.target.src = fallbackCover()
}

async function fetchProduct() {
  loading.value = true
  error.value = null
  imageFailed.value = false
  try {
    const id = route.params.id
    const res = await productApi.getDetail(id)
    product.value = res.data
    isFavorited.value = res.data.isFavorited || false
  } catch (e) {
    error.value = '商品不存在或已下架'
  } finally {
    loading.value = false
  }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const res = await favoriteApi.toggle(product.value.id)
    isFavorited.value = res.data?.favorited || !isFavorited.value
    ElMessage.success(isFavorited.value ? '已收藏' : '已取消收藏')
  } catch (e) {
    console.error(e)
  }
}

async function contactSeller() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const conversation = await chatStore.createConversation(product.value.userId)
    if (conversation?.conversationId) {
      router.push({ path: '/chat', query: { conversationId: conversation.conversationId } })
    } else {
      router.push('/chat')
    }
  } catch (e) {
    router.push('/chat')
  }
}

function buyNow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/order-confirm?productId=' + product.value.id)
}

async function reportProduct() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请描述举报原因', '举报商品', { inputType: 'textarea', inputPlaceholder: '如：虚假信息、违禁品等' })
    if (!value?.trim()) { ElMessage.warning('请输入举报原因'); return }
    await productApi.report(product.value.id, value)
    ElMessage.success('举报已提交，可在“我的举报”里查看管理员审核进度')
  } catch {}
}

async function removeProduct() {
  try {
    await ElMessageBox.confirm(`确定删除“${product.value.title}”吗？删除后将从我的发布中移除。`, '删除商品', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await productApi.delete(product.value.id)
    ElMessage.success('商品已删除')
    router.replace('/my-products')
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      console.error(e)
    }
  }
}

onMounted(fetchProduct)
</script>

<style scoped>
/* Image Carousel */
.img-carousel {
  position: relative;
  background: #000;
  height: 375px;
  overflow: hidden;
}
.image-scroll {
  display: flex;
  height: 375px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  scrollbar-width: none;
}
.image-scroll::-webkit-scrollbar { display: none; }
.carousel-img {
  width: 100%;
  min-width: 100%;
  height: 375px;
  object-fit: contain;
  scroll-snap-align: start;
}
.image-count {
  position: absolute;
  right: 12px;
  bottom: 12px;
  background: rgba(0,0,0,0.55);
  color: white;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
}
.back-overlay {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(0,0,0,0.4);
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  z-index: 10;
}

/* Card Sections */
.card-section {
  background: white;
  margin: 8px 12px;
  border-radius: 12px;
  padding: 16px;
}
.info-section {
  margin-top: -12px;
  position: relative;
  z-index: 5;
  border-radius: 12px 12px 0 0;
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}
.price-main {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-primary);
}
.price-sym { font-size: 14px; }
.price-origin {
  font-size: 13px;
  color: var(--color-text-muted);
  text-decoration: line-through;
}
.condition-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  margin-left: auto;
}
.cond-new { background: #E8F5E9; color: #4CAF50; }
.cond-used { background: #FFF3E0; color: #FF9800; }
.cond-old { background: #F5F5F5; color: #999; }

.product-title {
  font-size: 15px;
  line-height: 1.5;
  margin-bottom: 8px;
  color: var(--color-text-primary);
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--color-text-muted);
}
.free-ship {
  color: #4CAF50;
  background: #E8F5E9;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
}
.view-count { display: flex; align-items: center; gap: 4px; }

/* Seller Card */
.seller-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}
.seller-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.seller-name {
  font-weight: 600;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.verified-tag {
  font-size: 10px;
  color: var(--color-primary);
  border: 1px solid var(--color-primary);
  padding: 1px 6px;
  border-radius: 8px;
}
.seller-school {
  font-size: 12px;
  color: var(--color-primary);
  opacity: 0.8;
  margin-bottom: 2px;
}
.seller-hint { font-size: 12px; color: var(--color-text-muted); }

/* Description */
.section-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--color-border);
}
.desc-content {
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
  color: var(--color-text-secondary);
}

/* Safety */
.safety-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  margin: 8px 12px 80px;
  background: #FFF8E1;
  border-radius: 8px;
  font-size: 12px;
  color: #F57F17;
}
.report-link {
  margin-left: auto;
  color: #C62828;
  cursor: pointer;
  font-weight: 600;
  white-space: nowrap;
}

/* Bottom Bar */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  border-top: 1px solid var(--color-border);
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom, 8px);
}
.bar-left { display: flex; }
.fav-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  font-size: 11px;
  padding: 4px 8px;
}
.bar-right { display: flex; gap: 10px; }
.want-btn {
  border: 1.5px solid var(--color-primary);
  color: var(--color-primary);
}
.buy-btn {
  background: linear-gradient(135deg, #FF6B00, #FF8F00);
  border: none;
}

/* States */
.loading-page, .error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}
</style>

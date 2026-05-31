<template>
  <div class="home-page">
    <!-- Hero Strip -->
    <div class="hero-strip">
      <div class="hero-tag">校园闲置交易</div>
      <h1 class="hero-title">闲 置 游 起 来</h1>
      <p class="hero-sub">好物不毕业 / 每件都有新故事</p>
    </div>

    <!-- Search - bold pill -->
    <div class="search-strip">
      <div class="search-pill" @click="showSearch = true">
        <el-icon :size="18"><Search /></el-icon>
        <span class="search-placeholder">搜搜看，也许有惊喜...</span>
      </div>
    </div>

    <!-- Search dialog -->
    <div v-if="showSearch" class="search-overlay" @click.self="showSearch = false">
      <div class="search-active">
        <el-input
          ref="searchInput"
          v-model="keyword"
          placeholder="输入关键词搜索"
          size="large"
          clearable
          @keyup.enter="runSearch"
          @clear="runSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
          <template #append>
            <el-button type="primary" @click="runSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- Banner Grid - clickable 3-column -->
    <div class="banner-grid" v-if="!keyword">
      <div
        v-for="(banner, i) in banners"
        :key="i"
        class="banner-card"
        :style="{ background: banner.bg }"
        @click="handleBannerClick(banner)"
      >
        <div class="banner-badge" :style="{ background: banner.badgeBg, color: banner.badgeColor }">
          {{ banner.badge }}
        </div>
        <div class="banner-icon">{{ banner.icon }}</div>
        <div class="banner-title">{{ banner.title }}</div>
        <div class="banner-sub">{{ banner.sub }}</div>
        <div class="banner-arrow">&rarr;</div>
      </div>
    </div>

    <!-- Category Stamps -->
    <div class="stamp-strip" v-if="!keyword">
      <div class="stamp-label">浏览分类</div>
      <div class="stamp-row">
        <div
          v-for="(cat, i) in categoryItems"
          :key="cat.id ?? 'all'"
          class="stamp"
          :class="{ active: activeCategoryId === (cat.id ?? null) }"
          :style="{ animationDelay: i * 0.05 + 's' }"
          @click="handleCategoryClick(cat.id ?? null)"
        >
          <span class="stamp-icon">{{ cat.icon }}</span>
          <span class="stamp-name">{{ cat.name }}</span>
        </div>
      </div>
    </div>

    <!-- Sort chips -->
    <div class="sort-row">
      <span
        v-for="sort in sorts"
        :key="sort.key"
        class="sort-chip"
        :class="{ active: activeSort === sort.key }"
        @click="handleSortClick(sort.key)"
      >{{ sort.label }}</span>
    </div>

    <!-- Product Feed -->
    <div class="feed" v-if="products.length > 0">
      <div
        v-for="(product, i) in products"
        :key="product.id"
        class="feed-card anim-fade-in"
        :style="{ animationDelay: (i % 10) * 0.06 + 's' }"
        @click="$router.push('/product/' + product.id)"
      >
        <div class="feed-img-wrap">
          <img :src="coverImage(product)" :alt="product.title" class="feed-img" loading="lazy" @error="handleImageError($event, product)" />
          <span class="feed-photo-mark">实拍参考</span>
          <span class="feed-condition">{{ condText(product.condition) }}</span>
        </div>
        <div class="feed-body">
          <div class="feed-title">{{ product.title }}</div>
          <div class="feed-meta">
            <span class="feed-price">
              <span class="yen">￥</span>{{ product.price }}
            </span>
            <span class="feed-seller">{{ product.sellerNickname || '同学' }}</span>
          </div>
          <div class="feed-school" v-if="product.sellerVerified && product.sellerSchoolName">
            {{ product.sellerSchoolName }}
          </div>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="feed-status">
      <span class="loading-dots">加载中<span class="dot-anim">...</span></span>
    </div>

    <!-- Empty -->
    <div v-if="!loading && products.length === 0 && !keyword" class="empty-state">
      <div class="empty-icon">0</div>
      <p style="font-family:var(--font-display);font-size:18px;">还没有人发布商品</p>
      <p style="font-size:13px;color:var(--color-muted);">去发布第一个闲置吧</p>
    </div>

    <div v-if="!loading && products.length === 0 && keyword" class="empty-state">
      <div class="empty-icon">?</div>
      <p>没有找到相关商品</p>
    </div>

    <div v-if="noMore && products.length > 0" class="feed-status">- 已经到底了 -</div>

    <TabBar />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { productApi } from '@/api/product'
import request from '@/api/request'
import TabBar from '@/components/TabBar.vue'

const keyword = ref('')
const showSearch = ref(false)
const searchInput = ref(null)
const products = ref([])
const categories = ref([])
const activeCategoryId = ref(null)
const activeSort = ref('newest')
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)

const sorts = [
  { key: 'newest', label: '最新' },
  { key: 'popular', label: '热门' },
  { key: 'price_asc', label: '价格从低到高' },
  { key: 'price_desc', label: '价格从高到低' }
]

const banners = [
  { badge: 'HOT', title: '热门推荐', sub: '大家都在看的好物', bg: 'linear-gradient(135deg, #FF5500, #FF8F00)', badgeBg: '#FFD000', badgeColor: '#1A1A1A', icon: '热', action: 'popular' },
  { badge: 'NEW', title: '最新发布', sub: '刚上架的闲置宝贝', bg: 'linear-gradient(135deg, #1A1A1A, #333333)', badgeBg: '#FF5500', badgeColor: 'white', icon: '新', action: 'newest' },
  { badge: 'TIP', title: '教材专区', sub: '考研考证学习资料', bg: 'linear-gradient(135deg, #2D1B69, #4A2C8A)', badgeBg: '#FFD000', badgeColor: '#1A1A1A', icon: '书', action: 'book' },
]

const catIcons = { '全部':'all','手机数码':'phone','书籍教材':'book','服饰鞋包':'cloth','美妆护肤':'makeup','运动户外':'sport','生活用品':'daily','家电家具':'home','其他':'other' }
const catEmoji = { all:'全',phone:'机',book:'书',cloth:'衣',makeup:'妆',sport:'动',daily:'日',home:'家',other:'物' }

function handleBannerClick(banner) {
  if (banner.action === 'popular') {
    activeSort.value = 'popular'
    activeCategoryId.value = null
    keyword.value = ''
    fetchProducts()
  } else if (banner.action === 'newest') {
    activeSort.value = 'newest'
    activeCategoryId.value = null
    keyword.value = ''
    fetchProducts()
  } else if (banner.action === 'book') {
    activeCategoryId.value = 2
    activeSort.value = 'newest'
    keyword.value = ''
    fetchProducts()
  }
  // Scroll to feed
  setTimeout(() => {
    window.scrollTo({ top: 400, behavior: 'smooth' })
  }, 200)
}

const fallbackCovers = [
  { keywords: ['iphone', 'iPhone', '苹果', '13', '午夜色'], url: '/sample-products/iphone-13-midnight.png' },
  { keywords: ['macbook', 'MacBook', 'm1 pro', 'M1 Pro'], url: '/sample-products/macbook-pro-2021.png' },
  { keywords: ['nike', 'Nike', '空军一号', 'Air Force'], url: '/sample-products/nike-air-force-1.png' },
  { keywords: ['冰箱', '小冰箱', '迷你'], url: '/sample-products/mini-fridge.png' },
  { keywords: ['高等数学', '教材'], url: '/sample-products/advanced-math-7.png' },
  { keywords: ['考研英语', '黄皮书'], url: '/sample-products/kaoyan-english-yellow-book.png' },
  { keywords: ['四级', '词汇'], url: '/sample-products/cet4-vocabulary.png' },
  { keywords: ['键盘', 'ikbc'], url: '/sample-products/ikbc-keyboard.png' },
  { keywords: ['椅', '折叠椅', '凳'], url: '/sample-products/folding-chair.png' },
  { keywords: ['化妆', '粉底', '兰蔻'], url: '/sample-products/lancome-foundation.png' }
]

const defaultCover = '/sample-products/advanced-math-7.png'

const brokenCoverIds = ref(new Set())

function fallbackCover(product) {
  const title = (product.title || '').toLowerCase()
  const matched = fallbackCovers.find(item => item.keywords.some(keyword => title.includes(keyword.toLowerCase())))
  return matched?.url || defaultCover
}

function coverImage(product) {
  if (!brokenCoverIds.value.has(product.id) && product.images?.length) {
    return product.images[0]
  }
  return fallbackCover(product)
}

function handleImageError(event, product) {
  if (brokenCoverIds.value.has(product.id)) return
  brokenCoverIds.value = new Set([...brokenCoverIds.value, product.id])
  event.target.src = fallbackCover(product)
}

const categoryItems = computed(() => {
  if (categories.value.length > 0) {
    return [{ id: null, name: '全部', icon: '全' }, ...categories.value.map(c => {
      const key = catIcons[c.name] || 'other'
      return { ...c, icon: catEmoji[key] || '物' }
    })]
  }
  return [
    { id: null, name: '全部', icon: '全' },
    { id: 1, name: '手机数码', icon: '机' },
    { id: 2, name: '书籍教材', icon: '书' },
    { id: 3, name: '服饰鞋包', icon: '衣' },
    { id: 4, name: '美妆护肤', icon: '妆' },
    { id: 5, name: '运动户外', icon: '动' },
    { id: 6, name: '生活用品', icon: '日' },
    { id: 7, name: '家电家具', icon: '家' },
    { id: 8, name: '其他', icon: '物' }
  ]
})

function condText(c) { return c === 'NEW' ? '全新' : c === 'USED' ? '使用过' : '老旧' }

async function fetchCategories() {
  try {
    const res = await request.get('/categories')
    if (res.data) categories.value = res.data
  } catch {}
}

async function fetchProducts(append = false) {
  if (loading.value) return
  loading.value = true
  if (!append) { page.value = 1; noMore.value = false }
  try {
    const params = { page: page.value, size: 10, sort: activeSort.value }
    if (activeCategoryId.value) params.categoryId = activeCategoryId.value
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const res = await productApi.getList(params)
    const recs = res.data?.records || []
    if (append) products.value.push(...recs)
    else products.value = recs
    if (recs.length < 10) noMore.value = true
  } catch {} finally { loading.value = false }
}

function runSearch() {
  showSearch.value = false
  fetchProducts()
}

function handleCategoryClick(catId) {
  activeCategoryId.value = catId
  keyword.value = ''
  fetchProducts()
}

function handleSortClick(key) {
  activeSort.value = key
  fetchProducts()
}

function handleScroll() {
  if (loading.value || noMore.value) return
  if (window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 300) {
    page.value++
    fetchProducts(true)
  }
}

onMounted(async () => {
  await fetchCategories()
  await fetchProducts()
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => window.removeEventListener('scroll', handleScroll))
</script>

<style scoped>
/* ============================================
   Hero Strip
   ============================================ */
.hero-strip {
  background: var(--color-ink);
  padding: 28px 20px 32px;
  position: relative;
  overflow: hidden;
}
@media (min-width: 768px) { .hero-strip { padding: 40px 40px 48px; } }
.hero-strip::before {
  content: '';
  position: absolute;
  top: -40px;
  right: -40px;
  width: 160px;
  height: 160px;
  background: var(--color-primary);
  border-radius: 50%;
  opacity: 0.2;
}
.hero-strip::after {
  content: '';
  position: absolute;
  bottom: -20px;
  left: 60%;
  width: 80px;
  height: 80px;
  background: var(--color-accent);
  border-radius: 50%;
  opacity: 0.3;
}
.hero-tag {
  display: inline-block;
  background: rgba(255,255,255,0.12);
  color: var(--color-accent);
  font-size: 10px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 4px;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin-bottom: 12px;
}
.hero-title {
  font-family: var(--font-display);
  font-size: 32px;
  color: white;
  letter-spacing: 6px;
  line-height: 1.3;
  margin-bottom: 8px;
  position: relative;
  z-index: 1;
}
.hero-sub {
  color: rgba(255,255,255,0.6);
  font-size: 13px;
  letter-spacing: 1px;
  position: relative;
  z-index: 1;
}

/* ============================================
   Search
   ============================================ */
.search-strip {
  padding: 12px 16px;
  margin-top: -16px;
  position: relative;
  z-index: 10;
}
.search-pill {
  background: white;
  border: 2px solid var(--color-ink);
  border-radius: 50px;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--color-muted);
  font-size: 14px;
  cursor: pointer;
  box-shadow: 3px 3px 0px rgba(0,0,0,0.1);
  transition: all 0.15s;
}
.search-pill:active {
  transform: translate(1px, 1px);
  box-shadow: 1px 1px 0px rgba(0,0,0,0.1);
}
.search-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  z-index: 200;
  display: flex;
  padding: 20px;
  padding-top: 60px;
}
.search-active {
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
}

/* ============================================
   Banner Grid - 3 columns, clickable
   ============================================ */
.banner-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 0 16px 16px;
}
@media (max-width: 640px) {
  .banner-grid { grid-template-columns: repeat(3, 1fr); gap: 6px; padding: 0 8px 12px; }
}
.banner-card {
  aspect-ratio: 1;
  border-radius: var(--radius-lg);
  padding: 14px 12px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: all 0.2s;
  border: 2px solid transparent;
}
.banner-card:hover {
  transform: translateY(-3px);
  box-shadow: 6px 6px 0px rgba(0,0,0,0.15);
}
.banner-card:active {
  transform: scale(0.96);
}
.banner-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image: var(--grain);
  opacity: 0.4;
  pointer-events: none;
}
.banner-badge {
  display: inline-block;
  font-size: 10px;
  font-weight: 800;
  padding: 3px 10px;
  border-radius: 20px;
  letter-spacing: 1px;
  width: fit-content;
  position: relative;
  z-index: 1;
}
.banner-icon {
  font-family: var(--font-display);
  font-size: 48px;
  color: rgba(255,255,255,0.15);
  position: absolute;
  right: 8px;
  bottom: 4px;
  line-height: 1;
}
.banner-title {
  color: white;
  font-family: var(--font-display);
  font-size: 16px;
  letter-spacing: 1px;
  position: relative;
  z-index: 1;
}
.banner-sub {
  color: rgba(255,255,255,0.7);
  font-size: 11px;
  position: relative;
  z-index: 1;
  line-height: 1.3;
}
.banner-arrow {
  color: rgba(255,255,255,0.5);
  font-size: 18px;
  position: relative;
  z-index: 1;
  transition: transform 0.2s;
}
.banner-card:hover .banner-arrow {
  transform: translateX(4px);
}

/* ============================================
   Category Stamps
   ============================================ */
.stamp-strip {
  padding: 16px 16px 12px;
}
.stamp-label {
  font-family: var(--font-display);
  font-size: 14px;
  color: var(--color-ink-light);
  margin-bottom: 12px;
  letter-spacing: 1px;
}
.stamp-row {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 4px;
  scrollbar-width: none;
}
.stamp-row::-webkit-scrollbar { display: none; }
.stamp {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 8px 6px;
  animation: fadeInUp 0.4s ease-out both;
}
.stamp-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: white;
  border: 2px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 700;
  color: var(--color-ink);
  transition: all 0.2s;
}
.stamp.active .stamp-icon {
  background: var(--color-ink);
  border-color: var(--color-ink);
  color: var(--color-accent);
  transform: scale(1.1);
  box-shadow: 3px 3px 0px rgba(0,0,0,0.15);
}
.stamp-name {
  font-size: 11px;
  color: var(--color-ink-light);
  font-weight: 600;
}
.stamp.active .stamp-name {
  color: var(--color-ink);
}

/* ============================================
   Sort Chips
   ============================================ */
.sort-row {
  display: flex;
  gap: 8px;
  padding: 10px 16px;
  overflow-x: auto;
  scrollbar-width: none;
}
.sort-row::-webkit-scrollbar { display: none; }
.sort-chip {
  flex-shrink: 0;
  padding: 6px 16px;
  border-radius: 50px;
  font-size: 12px;
  font-weight: 600;
  border: 1.5px solid var(--color-border);
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s;
}
.sort-chip.active {
  background: var(--color-ink);
  color: white;
  border-color: var(--color-ink);
}

/* ============================================
   Product Feed
   ============================================ */
.feed {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 0 16px;
  max-width: 1200px;
  margin: 0 auto;
}
@media (min-width: 640px) { .feed { grid-template-columns: repeat(3, 1fr); } }
@media (min-width: 900px) { .feed { grid-template-columns: repeat(4, 1fr); } }
@media (min-width: 1200px) { .feed { grid-template-columns: repeat(5, 1fr); } }
.feed-card {
  background: white;
  border-radius: var(--radius);
  border: 2px solid var(--color-border);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.22s cubic-bezier(.2,.8,.2,1), box-shadow 0.22s cubic-bezier(.2,.8,.2,1), border-color 0.22s;
  box-shadow: var(--shadow-sticker);
  opacity: 0;
  animation: fadeInUp 0.4s ease-out forwards;
  will-change: transform;
}
.feed-card:hover {
  transform: translateY(-6px) rotate(-0.6deg);
  border-color: var(--color-ink);
  box-shadow: 7px 7px 0px rgba(26,26,26,0.14);
}
.feed-card:active {
  transform: scale(0.97);
  box-shadow: 1px 1px 0px rgba(0,0,0,0.06);
}
.feed-img-wrap {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
  background: linear-gradient(135deg, #fff7ed, #f4f4f5);
}
.feed-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease, filter 0.35s ease;
}
.feed-card:hover .feed-img {
  transform: scale(1.06);
  filter: saturate(1.08) contrast(1.03);
}
.feed-photo-mark {
  position: absolute;
  right: 8px;
  bottom: 8px;
  z-index: 1;
  background: rgba(255,255,255,0.88);
  color: var(--color-ink);
  font-size: 10px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 999px;
  border: 1px solid rgba(26,26,26,0.12);
  backdrop-filter: blur(8px);
}
.feed-card:hover .feed-photo-mark { animation: pulse 0.7s ease-out; }
.feed-img-wrap::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0) 55%, rgba(0,0,0,0.2));
  pointer-events: none;
}
.feed-condition {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(0,0,0,0.65);
  color: white;
  font-size: 10px;
  padding: 2px 7px;
  border-radius: 4px;
  font-weight: 600;
}
.feed-body { padding: 10px 10px 12px; }
.feed-title {
  font-size: 13px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
  min-height: 36px;
  color: var(--color-ink);
}
.feed-meta {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.feed-price {
  color: var(--color-primary);
  font-family: var(--font-display);
  font-size: 17px;
}
.yen { font-size: 11px; }
.feed-seller {
  font-size: 11px;
  color: var(--color-muted);
  max-width: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.feed-school {
  font-size: 10px;
  color: var(--color-primary);
  margin-top: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  opacity: 0.8;
}

/* ============================================
   States
   ============================================ */
.feed-status {
  text-align: center;
  padding: 24px;
  color: var(--color-muted);
  font-size: 13px;
  padding-bottom: 80px;
}
.loading-dots { font-family: var(--font-display); font-size: 15px; }
.dot-anim { animation: pulse 1s infinite; display: inline-block; }
.empty-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--color-surface);
  border: 3px dashed var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 28px;
  color: var(--color-muted);
  margin-bottom: 16px;
}
@media (prefers-reduced-motion: reduce) {
  .feed-card,
  .stamp,
  .anim-fade-in {
    animation: none;
    opacity: 1;
  }
  .feed-card,
  .feed-img,
  .banner-card,
  .banner-arrow,
  .sort-chip,
  .stamp-icon {
    transition: none;
  }
  .feed-card:hover,
  .feed-card:hover .feed-img,
  .banner-card:hover,
  .banner-card:hover .banner-arrow {
    transform: none;
  }
}
</style>

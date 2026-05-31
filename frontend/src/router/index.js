import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // User routes
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/user/HomeView.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/RegisterView.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/user/ForgotPasswordView.vue'),
    meta: { title: '忘记密码' }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/user/ProductDetailView.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: '/publish',
    name: 'Publish',
    component: () => import('@/views/user/PublishView.vue'),
    meta: { title: '发布商品', requiresAuth: true }
  },
  {
    path: '/order-confirm',
    name: 'OrderConfirm',
    component: () => import('@/views/user/OrderConfirmView.vue'),
    meta: { title: '确认订单', requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/user/OrderListView.vue'),
    meta: { title: '我的订单', requiresAuth: true }
  },
  {
    path: '/address',
    name: 'Address',
    component: () => import('@/views/user/AddressView.vue'),
    meta: { title: '收货地址', requiresAuth: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/user/ChatView.vue'),
    meta: { title: '消息', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/user/ProfileView.vue'),
    meta: { title: '我的', requiresAuth: true }
  },
  {
    path: '/verify',
    name: 'Verify',
    component: () => import('@/views/user/VerifyView.vue'),
    meta: { title: '学生认证', requiresAuth: true }
  },
  {
    path: '/my-products',
    name: 'MyProducts',
    component: () => import('@/views/user/MyProductsView.vue'),
    meta: { title: '我的发布', requiresAuth: true }
  },
  {
    path: '/my-reports',
    name: 'MyReports',
    component: () => import('@/views/user/MyReportsView.vue'),
    meta: { title: '我的举报', requiresAuth: true }
  },
  // Admin routes
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLoginView.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/admin',
    redirect: '/admin/dashboard'
  },
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/AdminDashboardView.vue'),
    meta: { title: '管理后台', requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/AdminUsersView.vue'),
    meta: { title: '用户管理', requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/products',
    name: 'AdminProducts',
    component: () => import('@/views/admin/AdminProductsView.vue'),
    meta: { title: '商品审核', requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/categories',
    name: 'AdminCategories',
    component: () => import('@/views/admin/AdminCategoriesView.vue'),
    meta: { title: '分类管理', requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/orders',
    name: 'AdminOrders',
    component: () => import('@/views/admin/AdminOrdersView.vue'),
    meta: { title: '订单管理', requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/reports',
    name: 'AdminReports',
    component: () => import('@/views/admin/AdminReportsView.vue'),
    meta: { title: '举报管理', requiresAuth: true, role: 'ADMIN' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? to.meta.title + ' - 校园二手' : '校园二手'
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.requiresAuth && !token) {
    if (to.path.startsWith('/admin')) {
      next('/admin/login')
    } else {
      next('/login')
    }
    return
  }

  if (to.meta.role && to.meta.role === 'ADMIN' && role !== 'ADMIN') {
    next('/admin/login')
    return
  }

  next()
})

export default router

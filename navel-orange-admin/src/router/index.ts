import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginLanding.vue'),
    meta: { title: '登录', public: true },
  },
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '首页看板', icon: 'Odometer' },
      },
      {
        path: 'orchard-dashboard',
        name: 'OrchardDashboard',
        component: () => import('@/views/dashboard/OrchardDashboard.vue'),
        meta: { title: '首页看板', icon: 'Odometer', hidden: true },
      },
      {
        path: 'orchard',
        name: 'OrchardList',
        component: () => import('@/views/orchard/OrchardList.vue'),
        meta: { title: '果园管理', icon: 'Location' },
      },
      {
        path: 'orchard/:id',
        name: 'OrchardDetail',
        component: () => import('@/views/orchard/OrchardDetail.vue'),
        meta: { title: '果园详情', hidden: true },
      },
      {
        path: 'orchard/:id/devices',
        name: 'OrchardDeviceManage',
        component: () => import('@/views/orchard/DeviceManage.vue'),
        meta: { title: '果园设备', hidden: true },
      },
      {
        path: 'orchard/members',
        name: 'MemberManage',
        component: () => import('@/views/orchard/MemberManage.vue'),
        meta: { title: '成员管理', hidden: true },
      },
      {
        path: 'devices',
        name: 'DeviceList',
        component: () => import('@/views/device/DeviceList.vue'),
        meta: { title: '设备管理', hidden: true },
      },
      {
        path: 'orchard/:id/monitor',
        name: 'VideoMonitor',
        component: () => import('@/views/monitor/VideoMonitor.vue'),
        meta: { title: '视频监控', hidden: true },
      },
      {
        path: 'detection',
        name: 'DetectionStats',
        component: () => import('@/views/detection/DetectionStats.vue'),
        meta: { title: '识别统计', icon: 'Search' },
      },
      {
        path: 'alert/rules',
        name: 'AlertRules',
        component: () => import('@/views/alert/AlertRules.vue'),
        meta: { title: '预警规则', icon: 'Bell', roles: ['ROLE_ADMIN'] },
      },
      {
        path: 'alert/messages',
        name: 'AlertMessages',
        component: () => import('@/views/alert/AlertMessages.vue'),
        meta: { title: '预警消息', hidden: true },
      },
      {
        path: 'knowledge/docs',
        name: 'KnowledgeDocs',
        component: () => import('@/views/knowledge/KnowledgeDocs.vue'),
        meta: { title: '文献管理', icon: 'Document', roles: ['ROLE_ADMIN'] },
      },
      {
        path: 'knowledge/search',
        name: 'KnowledgeSearch',
        component: () => import('@/views/knowledge/KnowledgeSearch.vue'),
        meta: { title: '知识检索', hidden: true },
      },
      {
        path: 'calendar',
        name: 'Calendar',
        component: () => import('@/views/calendar/CalendarView.vue'),
        meta: { title: '农事日历', icon: 'Calendar' },
      },
      {
        path: 'ota',
        name: 'OtaManage',
        component: () => import('@/views/device/OtaManage.vue'),
        meta: { title: 'OTA 升级', icon: 'Upload' },
      },
      {
        path: 'feedback',
        name: 'FeedbackList',
        component: () => import('@/views/feedback/FeedbackList.vue'),
        meta: { title: '用户反馈', icon: 'ChatDotRound', roles: ['ROLE_ADMIN'] },
      },
      {
        path: 'feedback/:id',
        name: 'FeedbackReview',
        component: () => import('@/views/feedback/FeedbackReview.vue'),
        meta: { title: '反馈审核', hidden: true, roles: ['ROLE_ADMIN'] },
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('@/views/message/MessageView.vue'),
        meta: { title: '消息中心', icon: 'Message' },
      },
      {
        path: 'welcome',
        name: 'Welcome',
        component: () => import('@/views/welcome/WelcomeView.vue'),
        meta: { title: '欢迎', hidden: true },
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('@/views/user/UserProfile.vue'),
        meta: { title: '个人中心', hidden: true },
      },
      {
        path: 'system/users',
        name: 'UserManage',
        component: () => import('@/views/system/UserManage.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ROLE_ADMIN'] },
      },
      {
        path: 'system/logs',
        name: 'OperationLog',
        component: () => import('@/views/system/OperationLog.vue'),
        meta: { title: '操作日志', icon: 'Notebook', roles: ['ROLE_ADMIN'] },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  document.title = `${to.meta.title || ''} - 脐橙病虫害监测系统`

  if (to.meta.public) {
    if (userStore.isLoggedIn && to.path === '/login') {
      return next('/dashboard')
    }
    return next()
  }

  if (!userStore.isLoggedIn) {
    return next('/login')
  }

  // 非管理员首次加载时检查果园关联状态
  if (!userStore.isAdmin && !userStore.orchardChecked) {
    await userStore.checkOrchard()
  }

  const requiredRoles = to.meta.roles as string[] | undefined
  if (requiredRoles && !requiredRoles.some(r => userStore.roles.includes(r))) {
    return next('/dashboard')
  }

  // 根据角色重定向首页
  if (to.path === '/dashboard') {
    if (userStore.isAdmin) {
      // 管理员使用管理后台首页
      return next()
    } else if (!userStore.hasOrchard) {
      // 无果园用户使用引导页
      return next('/welcome')
    } else {
      // 有果园用户使用果园首页
      return next('/orchard-dashboard')
    }
  }

  // /welcome 页面：有果园的用户不允许访问
  if (to.path === '/welcome' && userStore.hasOrchard) {
    return next('/orchard-dashboard')
  }

  next()
})

export default router

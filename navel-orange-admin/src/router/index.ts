import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
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
        name: 'DeviceManage',
        component: () => import('@/views/orchard/DeviceManage.vue'),
        meta: { title: '设备管理', hidden: true },
      },
      {
        path: 'detection',
        name: 'DetectionList',
        component: () => import('@/views/detection/DetectionList.vue'),
        meta: { title: '识别记录', icon: 'Search' },
      },
      {
        path: 'detection/stats',
        name: 'DetectionStats',
        component: () => import('@/views/detection/DetectionStats.vue'),
        meta: { title: '识别统计', hidden: true },
      },
      {
        path: 'alert/rules',
        name: 'AlertRules',
        component: () => import('@/views/alert/AlertRules.vue'),
        meta: { title: '预警规则', icon: 'Bell' },
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
        meta: { title: '文献管理', icon: 'Document' },
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
        path: 'feedback',
        name: 'FeedbackList',
        component: () => import('@/views/feedback/FeedbackList.vue'),
        meta: { title: '用户反馈', icon: 'ChatDotRound' },
      },
      {
        path: 'feedback/:id',
        name: 'FeedbackReview',
        component: () => import('@/views/feedback/FeedbackReview.vue'),
        meta: { title: '反馈审核', hidden: true },
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('@/views/message/MessageView.vue'),
        meta: { title: '消息中心', icon: 'Message' },
      },
      {
        path: 'system/users',
        name: 'UserManage',
        component: () => import('@/views/system/UserManage.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ROLE_ADMIN'] },
      },
      {
        path: 'system/roles',
        name: 'RoleManage',
        component: () => import('@/views/system/RoleManage.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', roles: ['ROLE_ADMIN'] },
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

router.beforeEach((to, _from, next) => {
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

  const requiredRoles = to.meta.roles as string[] | undefined
  if (requiredRoles && !requiredRoles.some(r => userStore.roles.includes(r))) {
    return next('/dashboard')
  }

  next()
})

export default router

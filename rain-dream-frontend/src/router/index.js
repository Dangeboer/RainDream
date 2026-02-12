import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/LoginView.vue') },
  {
    path: '/',
    component: () => import('../components/LayoutShell.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: () => import('../views/DashboardView.vue') },
      { path: 'items', name: 'items', component: () => import('../views/ItemListView.vue') },
      { path: 'items/new', name: 'item-new', component: () => import('../views/ItemFormView.vue') },
      { path: 'items/edit/:id', name: 'item-edit', component: () => import('../views/ItemFormView.vue') },
      { path: 'items/:id', name: 'item-detail', component: () => import('../views/ItemDetailView.vue') },
      { path: 'fanfic', name: 'fanfic-list', component: () => import('../views/FanficListView.vue') },
      { path: 'fanfic/:id', name: 'fanfic-detail', component: () => import('../views/FanficDetailView.vue') },
      { path: 'tags', name: 'tags', component: () => import('../views/TagManageView.vue') },
      { path: 'plts', name: 'plts', component: () => import('../views/PltManageView.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('rd_token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
  if (to.path === '/login' && token) {
    return '/dashboard'
  }
  return true
})

export default router

<template>
  <div class="layout page-shell">
    <aside class="sidebar">
      <div class="logo">RainDream</div>
      <el-button type="primary" class="new-btn" @click="$router.push('/items/new')">+ Add New Work</el-button>
      <nav class="menu">
        <router-link v-for="entry in menu" :key="entry.path" :to="entry.path" class="menu-item">{{ entry.label }}</router-link>
      </nav>
      <div class="user card-panel">
        <div>{{ auth.username || 'Archivist' }}</div>
        <el-button text @click="onLogout">退出登录</el-button>
      </div>
    </aside>
    <main class="content">
      <header class="topbar card-panel">
        <el-input v-model="keyword" placeholder="Search title, author, tags..." clearable />
      </header>
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const menu = [
  { label: 'Dashboard', path: '/dashboard' },
  { label: 'All Works', path: '/items' },
  { label: 'Fanfic', path: '/fanfic' },
  { label: 'Tags', path: '/tags' },
  { label: 'Platforms', path: '/plts' }
]

const keyword = ref('')
const auth = useAuthStore()
const router = useRouter()

const onLogout = () => {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { display: grid; grid-template-columns: 260px 1fr; min-height: 100vh; }
.sidebar { padding: 24px 16px; border-right: 1px solid var(--line); }
.logo { font-size: 28px; font-weight: 700; margin: 10px 8px 24px; }
.new-btn { width: 100%; margin-bottom: 18px; }
.menu { display: flex; flex-direction: column; gap: 8px; }
.menu-item { padding: 12px 14px; border-radius: 10px; color: var(--text-secondary); }
.menu-item.router-link-active { background: #2b1a49; color: #fff; }
.user { margin-top: 24px; padding: 14px; display: flex; justify-content: space-between; align-items: center; }
.content { padding: 24px; }
.topbar { padding: 12px 16px; margin-bottom: 16px; }
@media (max-width: 900px) {
  .layout { grid-template-columns: 1fr; }
  .sidebar { border-right: none; border-bottom: 1px solid var(--line); }
}
</style>

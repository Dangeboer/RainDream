<template>
  <div class="layout page-shell">
    <aside class="sidebar">
      <SidebarNav />
    </aside>
    <main class="content">
      <header class="topbar card-panel">
        <el-input v-model="keyword" placeholder="搜索……" clearable />
      </header>
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref } from "vue";
import SidebarNav from "./SidebarNav.vue";

const keyword = ref("");
</script>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  height: 100vh;
  overflow: hidden;
}
.sidebar {
  border-right: 1px solid var(--line);
  min-height: 0;
}
.content {
  padding: 24px;
  min-height: 0;
  overflow-y: auto;
}
.topbar {
  padding: 12px 16px;
  margin-bottom: 16px;
}
@media (max-width: 900px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .sidebar {
    border-right: none;
    border-bottom: 1px solid var(--line);
  }
}
</style>

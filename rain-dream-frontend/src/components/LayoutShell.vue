<template>
  <div class="layout page-shell">
    <aside class="sidebar">
      <SidebarNav />
    </aside>
    <main class="content">
      <el-scrollbar class="content-scroll">
        <div class="content-inner">
          <header class="topbar card-panel">
            <el-input v-model="keyword" placeholder="搜索……" clearable />
          </header>
          <router-view />
        </div>
      </el-scrollbar>
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
  min-height: 0;
}
.content-scroll {
  height: 100%;
}
.content-inner {
  padding: 24px;
}
.topbar {
  padding: 12px 16px;
  margin-bottom: 16px;
}
:deep(.content-scroll .el-scrollbar__wrap) {
  overflow-x: hidden;
}
:deep(.content-scroll .el-scrollbar__bar.is-vertical) {
  width: 6px;
  right: 2px;
}
:deep(.content-scroll .el-scrollbar__thumb) {
  background: rgba(38, 38, 38, 0.38) !important;
  border-radius: 999px;
}
:deep(
  .content-scroll .el-scrollbar__bar.is-vertical:hover .el-scrollbar__thumb
),
:deep(.content-scroll .el-scrollbar__thumb:hover),
:deep(.content-scroll .el-scrollbar__thumb:active) {
  background: rgba(38, 38, 38, 0.56) !important;
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

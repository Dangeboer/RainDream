<template>
  <div class="layout page-shell">
    <aside class="sidebar">
      <SidebarNav />
    </aside>
    <main class="content">
      <el-scrollbar class="content-scroll">
        <div class="content-inner">
          <header class="topbar card-panel">
            <div class="search-wrap">
              <el-input
                v-model="keyword"
                placeholder="搜索标题，作者，标签……"
                clearable
              />
            </div>
            <div class="topbar-actions">
              <el-button
                :class="{ 'is-active': isMetaRoute }"
                @click="router.push('/meta')"
              >
                标签/平台管理
              </el-button>
              <el-dropdown trigger="click" @command="onUserCommand">
                <div class="user-chip user-chip-trigger">
                  <el-avatar :size="28">
                    {{ (auth.username || "A").slice(0, 1).toUpperCase() }}
                  </el-avatar>
                  <span>{{ auth.username || "Archivist" }}</span>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </header>
          <router-view />
        </div>
      </el-scrollbar>
    </main>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import SidebarNav from "./SidebarNav.vue";

const keyword = ref("");
const auth = useAuthStore();
const router = useRouter();
const route = useRoute();
const isMetaRoute = computed(() => route.path === "/meta");

const onLogout = () => {
  auth.logout();
  router.push("/login");
};

const onUserCommand = (command) => {
  if (command === "logout") onLogout();
};
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
  min-width: 0;
}
.content-scroll {
  height: 100%;
}
.content-inner {
  padding: 24px;
  min-width: 0;
}
.topbar {
  padding: 12px 16px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.search-wrap {
  width: min(520px, 100%);
}
.topbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding-left: 8px;
  color: var(--text-main);
  font-weight: 600;
}
.user-chip-trigger {
  cursor: pointer;
}
.topbar-link {
  border-radius: 999px;
  padding: 6px 12px;
  font-weight: 600;
}
.topbar-link.is-active {
  color: var(--dark);
  background: var(--bg-panel-strong);
  box-shadow: inset 0 0 0 1px var(--line);
}
.topbar-link.is-active:hover,
.topbar-link.is-active:focus-visible,
.topbar-link.is-active:active {
  color: var(--dark);
  background: var(--bg-panel-strong);
  box-shadow: inset 0 0 0 1px var(--line);
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
  .topbar {
    flex-direction: column;
    align-items: stretch;
  }
  .search-wrap {
    width: 100%;
  }
  .topbar-actions {
    justify-content: space-between;
  }
}
</style>

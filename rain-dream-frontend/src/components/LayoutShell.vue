<template>
  <div class="layout page-shell">
    <aside class="sidebar">
      <div class="logo">无数</div>
      <el-button
        type="primary"
        class="new-btn"
        @click="$router.push('/items/new')"
        >+ 上传新资源</el-button
      >
      <nav class="menu home-menu">
        <router-link
          to="/dashboard"
          class="menu-item"
          :class="{ 'is-active': route.path === '/dashboard' }"
          >首页</router-link
        >
      </nav>
      <section class="menu-group">
        <div class="menu-title">内容</div>
        <nav class="menu">
          <router-link
            v-for="entry in contentMenu"
            :key="entry.label"
            :to="entry.to"
            class="menu-item"
            :class="{ 'is-active': isEntryActive(entry) }"
            >{{ entry.label }}</router-link
          >
        </nav>
      </section>
      <section class="menu-group">
        <div class="menu-title">格式</div>
        <nav class="menu">
          <router-link
            v-for="entry in mediaMenu"
            :key="entry.label"
            :to="entry.to"
            class="menu-item"
            :class="{ 'is-active': isEntryActive(entry) }"
            >{{ entry.label }}</router-link
          >
        </nav>
      </section>
      <div class="user card-panel">
        <div>{{ auth.username || "Archivist" }}</div>
        <el-button text @click="onLogout">退出登录</el-button>
      </div>
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
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const contentMenu = [
  {
    label: "文章",
    to: { path: "/items", query: { contentType: 1 } },
    group: "content",
    value: 1,
  },
  {
    label: "图绘",
    to: { path: "/items", query: { contentType: 2 } },
    group: "content",
    value: 2,
  },
  {
    label: "精修",
    to: { path: "/items", query: { contentType: 3 } },
    group: "content",
    value: 3,
  },
  {
    label: "混剪",
    to: { path: "/items", query: { contentType: 4 } },
    group: "content",
    value: 4,
  },
  {
    label: "解析",
    to: { path: "/items", query: { contentType: 5 } },
    group: "content",
    value: 5,
  },
  {
    label: "吐槽",
    to: { path: "/items", query: { contentType: 6 } },
    group: "content",
    value: 6,
  },
  {
    label: "主创说",
    to: { path: "/items", query: { contentType: 7 } },
    group: "content",
    value: 7,
  },
  {
    label: "RPS",
    to: { path: "/items", query: { contentType: 8 } },
    group: "content",
    value: 8,
  },
  {
    label: "其他",
    to: { path: "/items", query: { contentType: 9 } },
    group: "content",
    value: 9,
  },
];

const mediaMenu = [
  {
    label: "文本",
    to: { path: "/items", query: { mediaType: 1 } },
    group: "media",
    value: 1,
  },
  {
    label: "图片",
    to: { path: "/items", query: { mediaType: 2 } },
    group: "media",
    value: 2,
  },
  {
    label: "视频",
    to: { path: "/items", query: { mediaType: 3 } },
    group: "media",
    value: 3,
  },
  {
    label: "链接",
    to: { path: "/items", query: { mediaType: 4 } },
    group: "media",
    value: 4,
  },
];

const keyword = ref("");
const auth = useAuthStore();
const router = useRouter();
const route = useRoute();

const isEntryActive = (entry) => {
  if (entry.group === "content") {
    return (
      route.path === "/items" && route.query.contentType === String(entry.value)
    );
  }
  if (entry.group === "media") {
    return (
      route.path === "/items" && route.query.mediaType === String(entry.value)
    );
  }
  return false;
};

const onLogout = () => {
  auth.logout();
  router.push("/login");
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
  --sb-size: 6px;
  --sb-thumb: rgba(38, 38, 38, 0.38);
  --sb-track: var(--bg-main);
  padding: 24px 16px;
  border-right: 1px solid var(--line);
  overflow-y: auto;
  min-height: 0;
  scrollbar-width: thin;
  scrollbar-color: var(--sb-thumb) var(--sb-track);
}
.sidebar::-webkit-scrollbar {
  width: var(--sb-size);
}
.sidebar::-webkit-scrollbar-track {
  background: var(--sb-track);
  border-radius: 999px;
}
.sidebar::-webkit-scrollbar-thumb {
  background: var(--sb-thumb);
  border-radius: 999px;
}
.sidebar:hover::-webkit-scrollbar-thumb {
  background: rgba(38, 38, 38, 0.56);
}
.sidebar::-webkit-scrollbar-thumb:hover,
.sidebar::-webkit-scrollbar-thumb:active {
  background: rgba(38, 38, 38, 0.56);
}

.logo {
  font-size: 32px;
  font-weight: 700;
  margin: 10px 8px 24px;
}

.new-btn {
  width: 100%;
  height: 44px;
  font-size: 20px;
  margin-bottom: 32px;
  font-weight: 700;
}

.menu {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.home-menu {
  margin-bottom: 14px;
}
.home-menu .menu-item {
  padding: 12px 8px;
  margin-bottom: 6px;
  color: var(--dark);
  font-size: 18px;
  font-weight: 700;
}
.home-menu .menu-item:hover:not(.is-active) {
  background: transparent;
  color: var(--dark);
}
.menu-group + .menu-group {
  margin-top: 16px;
}
.menu-title {
  padding: 0 8px;
  margin-bottom: 6px;
  color: var(--dark);
  font-size: 18px;
  font-weight: 700;
}
.menu-item {
  padding: 12px 24px;
  border-radius: 10px;
  color: var(--text-secondary);
  position: relative;
}

/* hover */
.menu-item:hover {
  background: rgba(38, 38, 38, 0.06);
  color: var(--text-main);
}

/* 选中态：深色 #262626 + 左侧黄色高亮条 */
.menu-item.is-active {
  background: var(--highlight);
  color: var(--dark);
  font-weight: 700;
}

.user {
  margin-top: 20px;
  padding: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
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

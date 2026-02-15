<template>
  <el-scrollbar class="sidebar-scroll">
    <div class="sidebar-inner">
      <div class="logo">RainDream</div>
      <el-button
        type="primary"
        class="new-btn"
        @click="$router.push('/items/new')"
        >+ 上传新资源</el-button
      >
      <el-button
        type="primary"
        class="new-btn"
        @click="$router.push('/items/batch/new')"
        >+ 批量上传</el-button
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
    </div>
  </el-scrollbar>
</template>

<script setup>
import { useRoute } from "vue-router";

const contentMenu = [
  {
    label: "文章",
    to: { path: "/fanfic" },
    group: "content",
    value: 1,
  },
  {
    label: "图绘",
    to: { path: "/content", query: { mode: "content", contentType: 2 } },
    group: "content",
    value: 2,
  },
  {
    label: "精修",
    to: { path: "/content", query: { mode: "content", contentType: 3 } },
    group: "content",
    value: 3,
  },
  {
    label: "混剪",
    to: { path: "/content", query: { mode: "content", contentType: 4 } },
    group: "content",
    value: 4,
  },
  {
    label: "解析",
    to: { path: "/content", query: { mode: "content", contentType: 5 } },
    group: "content",
    value: 5,
  },
  {
    label: "吐槽",
    to: { path: "/content", query: { mode: "content", contentType: 6 } },
    group: "content",
    value: 6,
  },
  {
    label: "主创说",
    to: { path: "/content", query: { mode: "content", contentType: 7 } },
    group: "content",
    value: 7,
  },
  {
    label: "RPS",
    to: { path: "/content", query: { mode: "content", contentType: 8 } },
    group: "content",
    value: 8,
  },
  {
    label: "其他",
    to: { path: "/content", query: { mode: "content", contentType: 9 } },
    group: "content",
    value: 9,
  },
];

const mediaMenuBase = [
  { label: "文本", value: "text", group: "media" },
  { label: "图片", value: "image", group: "media" },
  { label: "视频", value: "video", group: "media" },
  { label: "链接", value: "link", group: "media" },
];

const mediaGroupByType = {
  1: "text",
  2: "image",
  3: "image",
  4: "image",
  5: "video",
  6: "link",
};
const mediaTypeByGroup = { text: 1, image: 2, video: 5, link: 6 };

const route = useRoute();
const mediaMenu = mediaMenuBase.map((entry) => ({
  ...entry,
  to: {
    path: "/content",
    query: {
      mode: "media",
      mediaGroup: entry.value,
      mediaType: mediaTypeByGroup[entry.value],
    },
  },
}));

const isEntryActive = (entry) => {
  if (entry.group === "content") {
    if (entry.value === 1) {
      return route.path === "/fanfic" || route.path.startsWith("/fanfic/");
    }
    return (
      route.path === "/content" &&
      route.query.mode === "content" &&
      route.query.contentType === String(entry.value)
    );
  }
  if (entry.group === "media") {
    const mediaGroup = String(route.query.mediaGroup || "");
    const currentMediaType = Number.parseInt(route.query.mediaType, 10);
    return (
      route.path === "/content" &&
      route.query.mode === "media" &&
      (mediaGroup === entry.value ||
        mediaGroupByType[currentMediaType] === entry.value)
    );
  }
  return false;
};
</script>

<style scoped>
.sidebar-scroll {
  height: 100%;
}
.sidebar-inner {
  padding: 24px 16px;
}
:deep(.sidebar-scroll .el-scrollbar__wrap) {
  overflow-x: hidden;
}

/* 自定义滚动条样式 */
:deep(.sidebar-scroll .el-scrollbar__bar.is-vertical) {
  width: 6px;
  right: 2px;
}
:deep(.sidebar-scroll .el-scrollbar__thumb) {
  background: rgba(38, 38, 38, 0.38) !important;
  border-radius: 999px;
}
:deep(
  .sidebar-scroll .el-scrollbar__bar.is-vertical:hover .el-scrollbar__thumb
),
:deep(.sidebar-scroll .el-scrollbar__thumb:hover),
:deep(.sidebar-scroll .el-scrollbar__thumb:active) {
  background: rgba(38, 38, 38, 0.56) !important;
}

.logo {
  font-size: 32px;
  font-weight: 700;
  margin: 10px 8px 24px;
}

.new-btn {
  width: 100%;
  height: 44px;
  font-size: 18px;
  font-weight: 700;
}
.new-btn + .new-btn {
  margin-left: 0;
  margin-top: 8px;
  margin-bottom: 32px;
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
  border-radius: 10px;
}
.home-menu .menu-item:hover:not(.is-active) {
  background: rgba(38, 38, 38, 0.06);
  color: var(--xhs-dark);
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
  color: var(--grey);
  position: relative;
}

.menu-item:hover {
  background: rgba(38, 38, 38, 0.06);
  color: var(--xhs-dark);
}

.menu-item.is-active {
  background: var(--xhs-yellow);
  color: var(--dark);
  font-weight: 700;
}
</style>

<template>
  <div class="media-grid-wrap">
    <el-skeleton v-if="loading" :rows="4" animated />
    <el-empty v-else-if="rows.length === 0" description="暂无内容" />

    <div v-else class="mixed-grid">
      <article
        v-for="row in rows"
        :key="row.id"
        class="mixed-card card-panel"
        :class="`type-${resolveMediaGroup(row)}`"
        @click="openDetail(row.id)"
      >
        <div class="card-top">
          <span class="media-chip">
            {{ resolveMediaLabel(row) }}
          </span>
          <span v-if="hasRating(row)" class="rating-chip">
            评分 {{ row.rating }}
          </span>
        </div>

        <h3 class="card-title">{{ row.title || "未命名资源" }}</h3>
        <p class="card-author">{{ row.author ? "@" + row.author : "未知作者" }}</p>

        <template v-if="resolveMediaGroup(row) === 'image'">
          <img
            class="cover-media"
            :src="getStoreUrl(row)"
            :alt="row.title || 'image'"
            loading="lazy"
          />
        </template>

        <template v-else-if="resolveMediaGroup(row) === 'video'">
          <video
            class="cover-media"
            :src="getStoreUrl(row)"
            muted
            playsinline
            preload="metadata"
          />
        </template>

        <template v-else-if="resolveMediaGroup(row) === 'text'">
          <p class="text-excerpt">
            {{ buildTextExcerpt(row) }}
          </p>
        </template>

        <template v-else>
          <div class="link-block">
            <p class="link-host">{{ resolveLinkHost(row) || "未知来源" }}</p>
            <p class="link-url">{{ formatLinkForDisplay(resolvePrimaryUrl(row)) }}</p>
          </div>
        </template>

        <div class="card-actions" @click.stop>
          <button class="action" type="button" @click="openDetail(row.id)">详情</button>
          <button class="action" type="button" @click="$emit('edit', row.id)">编辑</button>
          <a
            v-if="resolvePrimaryUrl(row)"
            class="action"
            :href="resolvePrimaryUrl(row)"
            target="_blank"
            rel="noopener noreferrer"
          >
            打开
          </a>
          <button
            class="action danger"
            type="button"
            @click="$emit('remove', row.id)"
          >
            删除
          </button>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import {
  inferMediaTypeFromStoreUrl,
  mediaTypeLabelMap,
} from "../composables/contentManageConfig";

defineProps({
  rows: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["detail", "edit", "remove"]);

const mediaGroupByType = {
  1: "text",
  2: "image",
  3: "image",
  5: "video",
  6: "link",
};

const pickFirstNonEmptyString = (values) => {
  for (const value of values) {
    if (value === null || value === undefined) continue;
    const text = String(value).trim();
    if (text) return text;
  }
  return "";
};

const resolveMediaType = (row) => {
  const parsed = Number.parseInt(row?.mediaType, 10);
  if (Number.isInteger(parsed) && mediaGroupByType[parsed]) return parsed;
  return inferMediaTypeFromStoreUrl(row?.storeUrl || "");
};

const resolveMediaGroup = (row) => mediaGroupByType[resolveMediaType(row)] || "text";

const resolveMediaLabel = (row) => mediaTypeLabelMap[resolveMediaType(row)] || "内容";

const buildTextExcerpt = (row) => {
  const raw = pickFirstNonEmptyString([row?.content, row?.summary, row?.notes]);
  if (!raw) return "暂无内容";
  return raw.replace(/\r\n/g, "\n").trim();
};

const getStoreUrl = (row) => row?.storeUrl || "";

const resolvePrimaryUrl = (row) => {
  const source = String(row?.sourceUrl || "").trim();
  if (source) return source;
  const store = String(row?.storeUrl || "").trim();
  return store;
};

const resolveLinkHost = (row) => {
  const url = resolvePrimaryUrl(row);
  if (!url) return "";
  try {
    return new URL(url).hostname.replace(/^www\./, "");
  } catch {
    return "";
  }
};

const formatLinkForDisplay = (url) => {
  if (!url) return "暂无链接";
  return url.length > 56 ? `${url.slice(0, 53)}...` : url;
};

const hasRating = (row) => row?.rating !== null && row?.rating !== undefined;

const openDetail = (id) => {
  if (!id) return;
  emit("detail", id);
};
</script>

<style scoped>
.media-grid-wrap {
  min-width: 0;
  margin-bottom: 12px;
}

.mixed-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

.mixed-card {
  min-height: 220px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
}

.mixed-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(38, 38, 38, 0.12);
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.media-chip,
.rating-chip {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: var(--xhs-black);
  background: rgba(247, 212, 76, 0.45);
}

.rating-chip {
  background: rgba(235, 122, 83, 0.2);
}

.card-title {
  margin: 0;
  font-size: 16px;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.card-author {
  margin: 0;
  color: var(--grey);
  font-size: 13px;
  line-height: 1.2;
}

.cover-media {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  border-radius: 10px;
  background: rgba(38, 38, 38, 0.05);
}

.text-excerpt {
  margin: 0;
  color: var(--xhs-black);
  line-height: 1.55;
  flex: 1;
  white-space: pre-wrap;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.link-block {
  border: 1px dashed var(--line);
  border-radius: 10px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.45);
  min-height: 88px;
}

.link-host {
  margin: 0 0 6px;
  font-size: 13px;
  color: var(--xhs-black);
  font-weight: 700;
}

.link-url {
  margin: 0;
  color: var(--grey);
  font-size: 12px;
  line-height: 1.4;
  word-break: break-all;
}

.card-actions {
  margin-top: auto;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action {
  border: 0;
  background: transparent;
  color: var(--grey);
  font-size: 13px;
  font-weight: 600;
  padding: 0;
  cursor: pointer;
}

.action:hover {
  color: var(--xhs-black);
}

.action.danger {
  color: #ef4444;
}
</style>

<template>
  <div class="media-grid-wrap">
    <el-skeleton v-if="loading" :rows="4" animated />
    <el-empty v-else-if="rows.length === 0" description="暂无内容" />

    <div v-else class="mixed-grid">
      <article
        v-for="row in rows"
        :key="row.id"
        class="mixed-card card-panel"
        :class="[
          `type-${resolveMediaGroup(row)}`,
          { 'is-static-card': resolveMediaGroup(row) === 'link' },
        ]"
        @click="handleCardClick(row)"
      >
        <div class="card-top">
          <span class="media-chip">{{ resolveMediaLabel(row) }}</span>
          <span v-if="hasRating(row)" class="rating-chip"
            >评分 {{ row.rating }}</span
          >
        </div>

        <h3 class="card-title">{{ row.title || "未命名资源" }}</h3>
        <p class="card-author">
          {{ row.author ? "@" + row.author : "未知作者" }}
        </p>

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
          <p class="text-excerpt">{{ buildTextExcerpt(row) }}</p>
        </template>

        <template v-else>
          <div class="link-block">
            <p class="link-host">{{ resolveLinkHost(row) || "未知来源" }}</p>
            <p class="link-url">
              {{ formatLinkForDisplay(resolvePrimaryUrl(row)) }}
            </p>
          </div>
        </template>

        <div class="card-actions" @click.stop>
          <button class="action" type="button" @click="openDetailDialog(row)">
            详情
          </button>
          <button class="action" type="button" @click="$emit('edit', row.id)">
            编辑
          </button>
          <a
            v-if="resolvePrimaryUrl(row)"
            class="action"
            :href="resolvePrimaryUrl(row)"
            :download="isImageCard(row) ? '' : null"
            target="_blank"
            rel="noopener noreferrer"
          >
            {{ isImageCard(row) ? "下载" : "打开" }}
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

    <el-dialog
      v-model="imagePreviewVisible"
      width="min(820px, 84vw)"
      class="preview-dialog"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-head">
          <span>{{ imagePreviewItem?.title || "图片预览" }}</span>
          <span class="dialog-sub">{{
            imagePreviewItem?.author ? "@" + imagePreviewItem.author : ""
          }}</span>
        </div>
      </template>
      <div v-if="imagePreviewItem" class="image-preview-body">
        <img
          class="image-preview"
          :src="getStoreUrl(imagePreviewItem)"
          :alt="imagePreviewItem.title || 'preview'"
        />
      </div>
    </el-dialog>

    <el-dialog
      v-model="videoPlayerVisible"
      width="min(960px, 92vw)"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-head">
          <span>{{ videoPlayerItem?.title || "视频播放" }}</span>
          <span class="dialog-sub">{{
            videoPlayerItem?.author ? "@" + videoPlayerItem.author : ""
          }}</span>
        </div>
      </template>
      <div v-if="videoPlayerItem" class="video-player-body">
        <video
          class="video-player"
          :src="getStoreUrl(videoPlayerItem)"
          controls
          autoplay
          playsinline
        />
      </div>
    </el-dialog>

    <el-dialog
      v-model="textPreviewVisible"
      width="min(840px, 92vw)"
      destroy-on-close
      class="text-preview-dialog"
    >
      <template #header>
        <div class="dialog-head">
          <span>{{ textPreviewItem?.title || "文本预览" }}</span>
          <span class="dialog-sub">{{
            textPreviewItem?.author ? "@" + textPreviewItem.author : ""
          }}</span>
        </div>
      </template>

      <div v-if="textPreviewItem" class="dialog-body">
        <p class="dialog-content">{{ buildFullText(textPreviewItem) }}</p>
        <div class="dialog-meta">
          <a
            v-if="resolvePrimaryUrl(textPreviewItem)"
            class="source-link"
            :href="resolvePrimaryUrl(textPreviewItem)"
            target="_blank"
            rel="noopener noreferrer"
          >
            打开源链接
          </a>
          <span v-else class="meta-muted">无源链接</span>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="detailVisible"
      width="min(760px, 92vw)"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-head">
          <span>{{ detailItem?.title || "资源详情" }}</span>
          <span class="dialog-sub">{{ detailItem?.author || "" }}</span>
        </div>
      </template>

      <el-skeleton v-if="detailLoading" :rows="6" animated />

      <el-descriptions
        v-else-if="detailItem && detailMediaGroup === 'image'"
        :column="2"
        border
      >
        <el-descriptions-item label="标题">{{
          detailItem.title || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{
          detailItem.author || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{
          detailItem.contentTypeLabel || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="评分">{{
          detailItem.rating ?? "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="大小">{{
          formatSize(detailItem.sizeBytes ?? 0)
        }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{
          detailItem.notes || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="总结" :span="2">{{
          detailItem.summary || "-"
        }}</el-descriptions-item>
      </el-descriptions>

      <el-descriptions
        v-else-if="detailItem && detailMediaGroup === 'video'"
        :column="2"
        border
      >
        <el-descriptions-item label="标题">{{
          detailItem.title || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{
          detailItem.author || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{
          detailItem.contentTypeLabel || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="评分">{{
          detailItem.rating ?? "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="总结" :span="2">{{
          detailItem.summary || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{
          detailItem.notes || "-"
        }}</el-descriptions-item>
      </el-descriptions>

      <el-descriptions v-else-if="detailItem" :column="2" border>
        <el-descriptions-item label="标题">{{
          detailItem.title || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{
          detailItem.author || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{
          detailItem.contentTypeLabel || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="年份">{{
          resolveReleaseYear(detailItem) || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="源链接" :span="2">
          <el-link
            v-if="detailItem.sourceUrl"
            :href="detailItem.sourceUrl"
            target="_blank"
            rel="noopener noreferrer"
          >
            打开链接
          </el-link>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="总结" :span="2">{{
          detailItem.summary || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{
          detailItem.notes || "-"
        }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import { getItemDetailApi } from "../api/item";
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

defineEmits(["edit", "remove"]);

const mediaGroupByType = {
  1: "text",
  2: "image",
  3: "image",
  5: "video",
  6: "link",
};

const imagePreviewVisible = ref(false);
const imagePreviewItem = ref(null);
const videoPlayerVisible = ref(false);
const videoPlayerItem = ref(null);
const textPreviewVisible = ref(false);
const textPreviewItem = ref(null);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detailItem = ref(null);
const detailMediaType = ref(1);

const detailMediaGroup = computed(
  () => mediaGroupByType[Number.parseInt(detailMediaType.value, 10)] || "text",
);

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

const resolveMediaGroup = (row) =>
  mediaGroupByType[resolveMediaType(row)] || "text";

const resolveMediaLabel = (row) =>
  mediaTypeLabelMap[resolveMediaType(row)] || "内容";

const buildTextExcerpt = (row) => {
  const raw = pickFirstNonEmptyString([row?.content, row?.summary, row?.notes]);
  if (!raw) return "暂无内容";
  return raw.replace(/\r\n/g, "\n").trim();
};

const buildFullText = (row) => {
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
const isImageCard = (row) => resolveMediaGroup(row) === "image";

const handleCardClick = (row) => {
  const group = resolveMediaGroup(row);
  if (group === "image") {
    imagePreviewItem.value = row;
    imagePreviewVisible.value = true;
    return;
  }
  if (group === "video") {
    videoPlayerItem.value = row;
    videoPlayerVisible.value = true;
    return;
  }
  if (group === "text") {
    textPreviewItem.value = row;
    textPreviewVisible.value = true;
  }
};

const normalizeDetail = (data = {}) => ({
  ...data,
  id: data?.id,
  mediaType: data?.mediaType,
  mediaTypeLabel: data?.mediaTypeLabel,
  contentType: data?.contentType,
  contentTypeLabel: data?.contentTypeLabel,
  releaseYear: data?.releaseYear ?? null,
  storeUrl: data?.storeUrl ?? "",
  sourceUrl: data?.sourceUrl ?? "",
  trackingType: data?.trackingType,
});

const openDetailDialog = async (row) => {
  if (!row?.id) return;
  detailMediaType.value = resolveMediaType(row);
  detailVisible.value = true;
  detailLoading.value = true;
  detailItem.value = null;
  try {
    const data = await getItemDetailApi(row.id);
    detailItem.value = normalizeDetail(data);
    const fetchedMediaType = Number.parseInt(data?.mediaType, 10);
    if (Number.isInteger(fetchedMediaType)) {
      detailMediaType.value = fetchedMediaType;
    }
  } finally {
    detailLoading.value = false;
  }
};

const resolveReleaseYear = (row) => {
  const year = row?.releaseYear;
  if (year === null || year === undefined || String(year).trim() === "")
    return "";
  return String(year);
};

const formatSize = (bytes) => {
  const size = Number(bytes);
  if (!size || size <= 0) return "-";
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  if (size < 1024 * 1024 * 1024)
    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  return `${(size / (1024 * 1024 * 1024)).toFixed(1)} GB`;
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

.mixed-card.is-static-card {
  cursor: default;
}

.mixed-card.is-static-card:hover {
  transform: none;
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
  font-size: 14px;
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

.dialog-head {
  display: flex;
  gap: 10px;
  align-items: baseline;
}

.dialog-sub {
  color: var(--grey);
  font-size: 13px;
}

.image-preview-body {
  display: flex;
  justify-content: center;
}

.image-preview {
  max-width: 100%;
  max-height: min(72vh, 720px);
  object-fit: contain;
}

.video-player-body {
  display: flex;
}

.video-player {
  width: 100%;
  border-radius: 10px;
}

.dialog-content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
}

.dialog-meta {
  margin-top: 10px;
}

.source-link {
  color: var(--xhs-orange);
  font-weight: 600;
}

.meta-muted {
  color: var(--grey);
}
</style>

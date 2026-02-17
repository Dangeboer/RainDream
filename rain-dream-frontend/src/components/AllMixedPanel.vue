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
          <div class="video-cover-wrap">
            <video
              class="cover-media"
              :src="getStoreUrl(row)"
              muted
              playsinline
              preload="metadata"
            />
            <button
              class="play-btn"
              type="button"
              @click.stop="handleCardClick(row)"
            >
              <span class="play-triangle"></span>
            </button>
          </div>
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
            :download="canDownloadCard(row) ? '' : null"
            target="_blank"
            rel="noopener noreferrer"
          >
            {{ canDownloadCard(row) ? "下载" : "打开" }}
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
      :lock-scroll="true"
      destroy-on-close
      @closed="onPreviewClosed"
    >
      <template #header>
        <div class="dialog-head">
          <span>{{ imagePreviewItem?.title || "图片预览" }}</span>
          <span class="dialog-sub">{{
            imagePreviewItem?.author ? "@" + imagePreviewItem.author : ""
          }}</span>
        </div>
      </template>
      <div class="preview-body" v-if="imagePreviewItem">
        <div class="preview-toolbar">
          <button class="preview-tool" type="button" @click="zoomOut">-</button>
          <span class="preview-scale"
            >{{ Math.round(previewScale * 100) }}%</span
          >
          <button class="preview-tool" type="button" @click="zoomIn">+</button>
        </div>
        <div
          ref="previewCanvasRef"
          class="preview-canvas"
          @wheel="onPreviewWheel"
        >
          <div class="preview-stage" :style="previewStageStyle">
            <img
              class="preview-image"
              :src="getStoreUrl(imagePreviewItem)"
              :alt="imagePreviewItem.title || 'preview'"
              @load="onPreviewImageLoad"
              @click="togglePreviewQuickZoom"
            />
          </div>
        </div>
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
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
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
const previewScale = ref(1);
const previewCanvasRef = ref(null);
const previewImageRatio = ref(0);
const previewCanvasWidth = ref(0);
const previewCanvasHeight = ref(0);
const previewQuickZoomed = ref(false);
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
  // return url.length > 56 ? `${url.slice(0, 53)}...` : url;
  return url;
};

const hasRating = (row) => row?.rating !== null && row?.rating !== undefined;
const isImageCard = (row) => resolveMediaGroup(row) === "image";
const canDownloadCard = (row) => {
  const group = resolveMediaGroup(row);
  return group === "image" || group === "video";
};
const PREVIEW_MIN_SCALE = 0.5;
const PREVIEW_MAX_SCALE = 10;
const WHEEL_ZOOM_STEP = 0.004;

const handleCardClick = (row) => {
  const group = resolveMediaGroup(row);
  if (group === "image") {
    if (!getStoreUrl(row)) return;
    previewScale.value = 1;
    previewImageRatio.value = 0;
    previewQuickZoomed.value = false;
    imagePreviewItem.value = row;
    imagePreviewVisible.value = true;
    nextTick(() => {
      updatePreviewCanvasSize();
    });
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

const setPreviewScale = (next) => {
  const clamped = Math.min(
    PREVIEW_MAX_SCALE,
    Math.max(PREVIEW_MIN_SCALE, next),
  );
  previewScale.value = Number(clamped.toFixed(4));
};

const zoomIn = () => {
  setPreviewScale(previewScale.value + 0.2);
  previewQuickZoomed.value = false;
};

const zoomOut = () => {
  setPreviewScale(previewScale.value - 0.2);
  previewQuickZoomed.value = false;
};

const resetZoom = () => {
  previewScale.value = 1;
  previewQuickZoomed.value = false;
};

const onPreviewImageLoad = (event) => {
  const img = event?.target;
  const naturalWidth = img?.naturalWidth || 0;
  const naturalHeight = img?.naturalHeight || 0;
  if (!naturalWidth || !naturalHeight) return;
  previewImageRatio.value = naturalWidth / naturalHeight;
  updatePreviewCanvasSize();
};

const updatePreviewCanvasSize = () => {
  const canvasEl = previewCanvasRef.value;
  if (!canvasEl) return;
  previewCanvasWidth.value = canvasEl.clientWidth;
  previewCanvasHeight.value = canvasEl.clientHeight;
};

const getBasePreviewSize = () => {
  const imageRatio = previewImageRatio.value;
  const canvasWidth = previewCanvasWidth.value;
  const canvasHeight = previewCanvasHeight.value;
  if (!imageRatio || !canvasWidth || !canvasHeight) return null;

  const canvasRatio = canvasWidth / canvasHeight;
  if (imageRatio >= canvasRatio) {
    return {
      width: canvasWidth,
      height: canvasWidth / imageRatio,
    };
  }
  return {
    width: canvasHeight * imageRatio,
    height: canvasHeight,
  };
};

const fitShortSide = () => {
  updatePreviewCanvasSize();
  const baseSize = getBasePreviewSize();
  if (!baseSize) return false;
  const fitShortScale = Math.max(
    previewCanvasWidth.value / baseSize.width,
    previewCanvasHeight.value / baseSize.height,
  );
  setPreviewScale(fitShortScale * 0.95);
  previewQuickZoomed.value = true;
  return true;
};

const togglePreviewQuickZoom = () => {
  if (previewQuickZoomed.value) {
    resetZoom();
    return;
  }
  fitShortSide();
};

const onPreviewWheel = (event) => {
  const shouldZoom = event.metaKey || event.ctrlKey;
  if (!shouldZoom) return;
  event.preventDefault();
  previewQuickZoomed.value = false;
  const next = previewScale.value - event.deltaY * WHEEL_ZOOM_STEP;
  setPreviewScale(next);
};

const onPreviewClosed = () => {
  imagePreviewItem.value = null;
  previewScale.value = 1;
  previewImageRatio.value = 0;
  previewCanvasWidth.value = 0;
  previewCanvasHeight.value = 0;
  previewQuickZoomed.value = false;
};

const previewStageStyle = computed(() => {
  const baseSize = getBasePreviewSize();
  if (!baseSize) {
    return {
      width: "100%",
      height: "100%",
    };
  }
  const scale = previewScale.value;
  return {
    width: `${baseSize.width * scale}px`,
    height: `${baseSize.height * scale}px`,
  };
});

onMounted(() => {
  window.addEventListener("resize", updatePreviewCanvasSize);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", updatePreviewCanvasSize);
});

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

.video-cover-wrap {
  position: relative;
}

.play-btn {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 46px;
  height: 46px;
  transform: translate(-50%, -50%);
  border-radius: 999px;
  border: 0;
  background: rgba(38, 38, 38, 0.66);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.play-btn:hover {
  background: rgba(38, 38, 38, 0.78);
}

.play-triangle {
  width: 0;
  height: 0;
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
  border-left: 13px solid #fff;
  margin-left: 3px;
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

.preview-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  height: 70vh;
  max-height: 70vh;
  overflow: hidden;
}

.preview-toolbar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
}

.preview-tool {
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  color: var(--xhs-black);
  min-width: 32px;
  height: 30px;
  padding: 0 10px;
  cursor: pointer;
}

.preview-tool:hover {
  border-color: var(--line);
}

.preview-scale {
  min-width: 48px;
  text-align: center;
  font-size: 13px;
  color: var(--grey);
}

.preview-canvas {
  flex: 1;
  min-height: 0;
  overflow: auto;
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  scrollbar-width: thin;
  scrollbar-color: rgba(120, 120, 120, 0.55) transparent;
}

.preview-canvas::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.preview-canvas::-webkit-scrollbar-track {
  background: transparent;
}

.preview-canvas::-webkit-scrollbar-thumb {
  background: rgba(120, 120, 120, 0.55);
  border-radius: 999px;
}

.preview-canvas::-webkit-scrollbar-corner {
  background: transparent;
}

.preview-stage {
  flex: 0 0 auto;
  min-width: 0;
  min-height: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
}

.preview-image {
  width: 100%;
  height: 100%;
  max-width: 100%;
  max-height: 100%;
  border-radius: 8px;
  object-fit: contain;
  transition: none;
  cursor: pointer;
}

:deep(.preview-dialog .el-dialog__body) {
  padding-top: 8px;
  overflow: hidden;
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

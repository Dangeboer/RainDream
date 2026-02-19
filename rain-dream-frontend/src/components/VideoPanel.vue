<template>
  <div class="media-grid-wrap">
    <el-empty v-if="rows.length === 0" description="暂无视频" />

    <div v-else class="media-grid video-grid">
      <article
        v-for="row in rows"
        :key="row.id"
        class="video-card"
        @click="openPlayer(row)"
      >
        <video
          class="video-bg"
          :src="getStoreUrl(row)"
          preload="metadata"
          muted
          playsinline
          @loadedmetadata="onVideoMetadataLoaded($event, row)"
        />

        <button class="play-btn" type="button" @click.stop="openPlayer(row)">
          <span class="play-triangle"></span>
        </button>

        <div class="video-hover-layer">
          <div class="video-mask"></div>

          <div class="video-info">
            <h3 class="title">{{ resolveDisplayTitle(row) }}</h3>
            <p class="sub">
              {{ row.author ? "@" + row.author : "未知作者" }}
              <span v-if="getDuration(row)">· {{ getDuration(row) }}</span>
              <span v-else-if="row.rating">· 评分 {{ row.rating }}</span>
            </p>

            <div class="actions" @click.stop>
              <button class="action" type="button" @click="openDetail(row.id)">
                详情
              </button>
              <a
                class="action"
                :href="getStoreUrl(row)"
                target="_blank"
                rel="noopener noreferrer"
                @click.prevent="handleDownload(row)"
              >
                下载
              </a>
              <button class="action" type="button" @click="openEdit(row.id)">
                编辑
              </button>
              <button
                class="action danger"
                type="button"
                @click="$emit('remove', row.id)"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      </article>
    </div>

    <el-dialog
      v-model="playerVisible"
      width="min(960px, 92vw)"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-head">
          <span>{{ resolveDisplayTitle(playerItem, "视频播放") }}</span>
          <span class="dialog-sub">{{
            playerItem?.author ? "@" + playerItem.author : ""
          }}</span>
        </div>
      </template>
      <div class="player-body" v-if="playerItem">
        <video
          class="video-player"
          :src="getStoreUrl(playerItem)"
          controls
          autoplay
          playsinline
        />
      </div>
    </el-dialog>

    <el-dialog
      v-model="detailVisible"
      width="min(760px, 92vw)"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-head">
          <span>{{ resolveDisplayTitle(detailItem, "资源详情") }}</span>
          <span class="dialog-sub">{{ detailItem?.author || "未知作者" }}</span>
        </div>
      </template>
      <el-skeleton v-if="detailLoading" :rows="6" animated />
      <el-descriptions v-else-if="detailItem" :column="2" border>
        <el-descriptions-item label="标题">{{
          detailItem.title || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{
          detailItem.author || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          {{ detailItem.contentTypeLabel || "-" }}
        </el-descriptions-item>
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
    </el-dialog>

    <el-dialog
      v-model="editVisible"
      width="min(860px, 94vw)"
      class="edit-dialog"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-head">
          <span>{{ resolveDisplayTitle(editForm, "编辑资源") }}</span>
        </div>
      </template>

      <el-skeleton v-if="editLoading" :rows="8" animated />
      <el-form v-else label-position="top" class="edit-form" size="small">
        <div class="form-grid">
          <el-form-item label="媒体类型">
            <el-select v-model="editForm.mediaType" disabled>
              <el-option
                v-for="opt in mediaTypeOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="内容类型">
            <el-select v-model="editForm.contentType">
              <el-option
                v-for="opt in contentTypeOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="原作">
            <el-input v-model="editForm.fandom" />
          </el-form-item>
          <el-form-item label="CP">
            <el-input v-model="editForm.cp" />
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="editForm.title" />
          </el-form-item>
          <el-form-item label="作者">
            <el-input v-model="editForm.author" />
          </el-form-item>
          <el-form-item label="追踪状态">
            <el-select v-model="editForm.trackingType" clearable>
              <el-option
                v-for="opt in trackingTypeOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="评分">
            <el-input-number
              v-model="editForm.rating"
              :min="0"
              :max="10"
              :step="0.1"
              :precision="1"
            />
          </el-form-item>
        </div>
        <el-form-item label="来源链接">
          <el-input v-model="editForm.sourceUrl" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.notes" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="总结">
          <el-input v-model="editForm.summary" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-actions">
          <el-button @click="editVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="editSubmitting"
            @click="submitEdit"
            >保存</el-button
          >
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { getItemDetailApi, updateItemApi } from "../api/item";

const contentTypeOptions = [
  { value: 1, label: "文章" },
  { value: 2, label: "图绘" },
  { value: 3, label: "精修" },
  { value: 4, label: "混剪" },
  { value: 5, label: "解析" },
  { value: 6, label: "吐槽" },
  { value: 7, label: "主创说" },
  { value: 8, label: "RPS" },
  { value: 9, label: "其他" },
];

const mediaTypeOptions = [
  { value: 1, label: "文本" },
  { value: 2, label: "静图" },
  { value: 3, label: "动图" },
  { value: 5, label: "视频" },
  { value: 6, label: "链接" },
];

const trackingTypeOptions = [
  { value: 1, label: "未看" },
  { value: 2, label: "在看" },
  { value: 3, label: "追平" },
  { value: 4, label: "看过" },
  { value: 5, label: "归档" },
];

defineProps({
  rows: {
    type: Array,
    default: () => [],
  },
  mediaTypeLabelMap: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(["detail", "edit", "remove", "updated"]);

const playerVisible = ref(false);
const playerItem = ref(null);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detailItem = ref(null);
const editVisible = ref(false);
const editLoading = ref(false);
const editSubmitting = ref(false);
const editingId = ref(null);

const editForm = reactive({
  mediaType: undefined,
  contentType: undefined,
  storeUrl: "",
  content: null,
  fileName: "",
  title: "",
  fandom: "",
  cp: "",
  author: "",
  sourceUrl: "",
  releaseYear: null,
  sizeBytes: null,
  trackingType: null,
  rating: null,
  notes: "",
  summary: "",
  tags: [],
  plts: [],
  fanficForm: null,
  mediaForm: null,
});

const getStoreUrl = (row) => row?.storeUrl || "";
const pickText = (value) => {
  if (value === null || value === undefined) return "";
  return String(value).trim();
};
const stripFileExtension = (name) => {
  const text = pickText(name);
  if (!text) return "";
  const match = text.match(/^(.*)\.([a-z0-9]{1,10})$/i);
  if (!match || !match[1]) return text;
  return match[1];
};
const resolveDisplayTitle = (row, fallback = "未命名资源") => {
  const title = pickText(row?.title);
  if (title) return title;
  const fileName = stripFileExtension(row?.fileName);
  if (fileName) return fileName;
  return fallback;
};
const resolveDownloadName = (row) => {
  const title = pickText(row?.title);
  if (title) return title;
  const fileName = pickText(row?.fileName);
  if (fileName) return fileName;
  const storeUrl = pickText(row?.storeUrl);
  if (!storeUrl) return "download";
  try {
    const pathname = new URL(storeUrl).pathname || "";
    const fromUrl = pathname.split("/").filter(Boolean).pop() || "";
    return fromUrl || "download";
  } catch {
    return storeUrl.split("/").filter(Boolean).pop() || "download";
  }
};
const triggerBrowserDownload = (href, fileName) => {
  const anchor = document.createElement("a");
  anchor.href = href;
  anchor.download = fileName || "download";
  anchor.rel = "noopener noreferrer";
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);
};
const handleDownload = async (row) => {
  const storeUrl = getStoreUrl(row);
  if (!storeUrl) return;
  const fileName = resolveDownloadName(row);
  try {
    const response = await fetch(storeUrl);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const blob = await response.blob();
    const blobUrl = URL.createObjectURL(blob);
    triggerBrowserDownload(blobUrl, fileName);
    setTimeout(() => URL.revokeObjectURL(blobUrl), 1000);
  } catch {
    triggerBrowserDownload(storeUrl, fileName);
    ElMessage.warning("未能直接命名下载，已回退原链接下载");
  }
};

const durationCache = reactive({});

const getDurationCacheKey = (row) => {
  if (!row) return "";
  if (row.id !== null && row.id !== undefined) return `id:${row.id}`;
  const url = String(row.storeUrl || "").trim();
  return url ? `url:${url}` : "";
};

const formatDuration = (value) => {
  let seconds = Number(value);
  if (!Number.isFinite(seconds) || seconds <= 0) return "";
  if (seconds > 24 * 60 * 60) seconds /= 1000;
  const total = Math.max(1, Math.round(seconds));
  const hour = Math.floor(total / 3600);
  const minute = Math.floor((total % 3600) / 60);
  const second = total % 60;
  if (hour > 0) {
    return `${String(hour).padStart(2, "0")}:${String(minute).padStart(
      2,
      "0",
    )}:${String(second).padStart(2, "0")}`;
  }
  return `${String(minute).padStart(2, "0")}:${String(second).padStart(
    2,
    "0",
  )}`;
};

const normalizeDuration = (value) => {
  if (value === null || value === undefined) return "";
  if (typeof value === "number") return formatDuration(value);
  const raw = String(value).trim();
  if (!raw) return "";
  if (raw.includes(":")) return raw;
  const numeric = Number(raw);
  if (Number.isFinite(numeric)) return formatDuration(numeric);
  return raw;
};

const getDuration = (row) => {
  const direct = normalizeDuration(row?.duration ?? row?.videoDuration);
  if (direct) return direct;
  const key = getDurationCacheKey(row);
  if (!key) return "";
  return normalizeDuration(durationCache[key]);
};

const onVideoMetadataLoaded = (event, row) => {
  const seconds = event?.target?.duration;
  if (!Number.isFinite(seconds) || seconds <= 0) return;
  const key = getDurationCacheKey(row);
  if (!key) return;
  durationCache[key] = seconds;
};

const normalizeDetail = (data = {}) => ({
  ...data,
  id: data?.id,
  mediaType: data?.mediaType,
  mediaTypeLabel: data?.mediaTypeLabel,
  contentType: data?.contentType,
  contentTypeLabel: data?.contentTypeLabel,
  storeUrl: data?.storeUrl ?? "",
  sourceUrl: data?.sourceUrl ?? "",
  trackingType: data?.trackingType,
  tags: (data?.tags || data?.tagVOS || [])
    .map((item) => item?.name || item?.tagName || item)
    .filter(Boolean),
  plts: (data?.plts || data?.pltVOS || [])
    .map((item) => item?.name || item?.pltName || item)
    .filter(Boolean),
});

const openPlayer = (row) => {
  if (!getStoreUrl(row)) return;
  playerItem.value = row;
  playerVisible.value = true;
};

const openDetail = async (itemId) => {
  if (!itemId) return;
  detailVisible.value = true;
  detailLoading.value = true;
  detailItem.value = null;
  try {
    const data = await getItemDetailApi(itemId);
    detailItem.value = normalizeDetail(data);
  } finally {
    detailLoading.value = false;
  }
};

const openEdit = async (itemId) => {
  if (!itemId) return;
  editVisible.value = true;
  editLoading.value = true;
  editingId.value = itemId;
  try {
    const data = normalizeDetail(await getItemDetailApi(itemId));
    editForm.mediaType = data.mediaType;
    editForm.contentType = data.contentType;
    editForm.storeUrl = data.storeUrl;
    editForm.content = data.content ?? null;
    editForm.fileName = data.fileName ?? "";
    editForm.title = data.title ?? "";
    editForm.fandom = data.fandom ?? "";
    editForm.cp = data.cp ?? "";
    editForm.author = data.author ?? "";
    editForm.sourceUrl = data.sourceUrl ?? "";
    editForm.releaseYear = data.releaseYear ?? null;
    editForm.sizeBytes = data.sizeBytes ?? null;
    editForm.trackingType = data.trackingType ?? null;
    editForm.rating = data.rating ?? null;
    editForm.notes = data.notes ?? "";
    editForm.summary = data.summary ?? "";
    editForm.tags = data.tags || [];
    editForm.plts = data.plts || [];
    editForm.fanficForm = data.fanficForm ?? null;
    editForm.mediaForm = data.mediaForm ?? null;
  } finally {
    editLoading.value = false;
  }
};

const toNullableString = (value) => {
  if (value === null || value === undefined) return null;
  const text = String(value).trim();
  return text === "" ? null : text;
};

const submitEdit = async () => {
  if (!editingId.value) return;
  if (
    !editForm.mediaType ||
    !editForm.contentType ||
    !editForm.fandom ||
    !editForm.cp
  ) {
    ElMessage.warning("请先填写必填项");
    return;
  }
  editSubmitting.value = true;
  try {
    const payload = {
      mediaType: editForm.mediaType,
      contentType: editForm.contentType,
      storeUrl: toNullableString(editForm.storeUrl),
      content:
        editForm.mediaType === 1 ? toNullableString(editForm.content) : null,
      title: toNullableString(editForm.title),
      fandom: toNullableString(editForm.fandom),
      cp: toNullableString(editForm.cp),
      author: toNullableString(editForm.author),
      sourceUrl: toNullableString(editForm.sourceUrl),
      releaseYear: editForm.releaseYear,
      sizeBytes: editForm.sizeBytes,
      fileName: toNullableString(editForm.fileName),
      trackingType: editForm.trackingType,
      rating: editForm.rating,
      notes: toNullableString(editForm.notes),
      summary: toNullableString(editForm.summary),
      fanficForm:
        editForm.contentType === 1 && editForm.mediaType === 1
          ? editForm.fanficForm
          : null,
      mediaForm:
        editForm.mediaType === 4
          ? editForm.mediaForm || { liveUrl: null }
          : null,
      tags: editForm.tags || [],
      plts: editForm.plts || [],
    };
    await updateItemApi(editingId.value, payload);
    ElMessage.success("更新成功");
    editVisible.value = false;
    emit("updated");
  } finally {
    editSubmitting.value = false;
  }
};
</script>

<style scoped>
.media-grid-wrap {
  min-width: 0;
  margin-bottom: 12px;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

.video-card {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  background: transparent;
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
  aspect-ratio: 4 / 3;
  cursor: pointer;
}

.video-bg {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  filter: saturate(0.92);
}

.video-hover-layer {
  position: absolute;
  inset: 0;
  z-index: 2;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.video-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(38, 38, 38, 0.78) 0%,
    rgba(38, 38, 38, 0.45) 46%,
    rgba(38, 38, 38, 0.14) 100%
  );
}

.play-btn {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  z-index: 3;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 0;
  background: rgba(38, 38, 38, 0.66);
  display: grid;
  place-items: center;
  cursor: pointer;
  opacity: 1;
  transition:
    opacity 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease,
    background-color 0.2s ease;
}

.video-card:hover .play-btn {
  opacity: 0;
  pointer-events: none;
}

.video-card:hover .video-hover-layer {
  opacity: 1;
}

@media (hover: none) {
  .video-hover-layer {
    opacity: 1;
  }

  .play-btn {
    box-shadow: 0 8px 24px rgba(38, 38, 38, 0.3);
  }
}

.play-triangle {
  width: 0;
  height: 0;
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
  border-left: 12px solid var(--white);
  margin-left: 2px;
}

.video-info {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  color: #fff;
  padding: 12px 14px;
}

.title {
  margin: 0;
  font-size: 15px;
  line-height: 1.3;
  padding-right: 2px;
  font-weight: 700;
}

.sub {
  margin: 4px 0 0;
  font-size: 10px;
  opacity: 0.92;
}

.actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.action {
  border: 0;
  border-radius: 999px;
  padding: 5px 12px;
  font-size: 10px;
  line-height: 1;
  color: #fff;
  background: rgba(38, 38, 38, 0.55);
  text-decoration: none;
  cursor: pointer;
}

.action:hover {
  background: rgba(38, 38, 38, 0.75);
}

.action:focus {
  outline: none;
}

.action:focus-visible {
  outline: 2px solid transparent;
  outline-offset: 0;
  box-shadow: none;
}

.action.danger {
  background: rgba(235, 122, 83, 0.82);
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

.player-body {
  max-height: 72vh;
}

.video-player {
  width: 100%;
  max-height: 72vh;
  border-radius: 8px;
  background: #000;
}

.edit-form {
  margin-top: 2px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px 10px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.edit-dialog .el-dialog__body) {
  padding-top: 8px;
  padding-bottom: 10px;
}

:deep(.edit-form .el-form-item) {
  margin-bottom: 8px;
}

:deep(.edit-form .el-form-item__label) {
  line-height: 18px;
  padding-bottom: 4px;
  font-size: 13px;
}

:deep(.edit-dialog .el-textarea__inner) {
  min-height: 72px !important;
  border: 1px solid var(--line) !important;
}

@media (max-width: 980px) {
  .form-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>

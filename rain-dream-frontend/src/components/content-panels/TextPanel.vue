<template>
  <div class="text-panel-wrap">
    <el-empty v-if="rows.length === 0" description="暂无文本" />

    <div v-else class="text-card-list">
      <article
        v-for="row in rows"
        :key="row.id"
        class="text-card"
        @click="openPreview(row)"
      >
        <header
          v-if="isFieldVisible('title') || isFieldVisible('author')"
          class="card-head"
        >
          <h3 v-if="isFieldVisible('title')" class="card-title">
            {{ row.title || "未命名文本" }}
          </h3>
          <p v-if="isFieldVisible('author')" class="card-author">
            {{ row.author ? "@" + row.author : "未知作者" }}
          </p>
        </header>

        <p v-if="isFieldVisible('content')" class="card-content">
          {{ buildTextExcerpt(row) }}
        </p>

        <div
          v-if="
            isFieldVisible('sourceLink') ||
            isFieldVisible('rating') ||
            isFieldVisible('releaseYear')
          "
          class="card-meta"
        >
          <a
            v-if="isFieldVisible('sourceLink') && getSourceUrl(row)"
            class="source-link"
            :href="getSourceUrl(row)"
            target="_blank"
            rel="noopener noreferrer"
            @click.stop
          >
            源链接
          </a>
          <span v-else-if="isFieldVisible('sourceLink')" class="meta-muted">
            无源链接
          </span>
          <span
            v-if="
              isFieldVisible('rating') &&
              row.rating !== null &&
              row.rating !== undefined
            "
            class="meta-rating"
          >
            评分 {{ row.rating }}
          </span>
          <span
            v-if="
              isFieldVisible('releaseYear') &&
              resolveReleaseYear(row) !== ''
            "
            class="meta-year"
          >
            年份 {{ resolveReleaseYear(row) }}
          </span>
        </div>

        <div v-if="isFieldVisible('actions')" class="card-actions" @click.stop>
          <button class="action" type="button" @click="openDetail(row.id)">
            详情
          </button>
          <button class="action" type="button" @click="$emit('edit', row.id)">
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
      </article>
    </div>

    <el-dialog
      v-model="previewVisible"
      width="min(840px, 92vw)"
      destroy-on-close
      class="text-preview-dialog"
    >
      <template #header>
        <div class="dialog-head">
          <span>{{ previewRow?.title || "文本预览" }}</span>
          <span class="dialog-sub">
            {{ previewRow?.author ? "@" + previewRow.author : "" }}
          </span>
        </div>
      </template>

      <div v-if="previewRow" class="dialog-body">
        <p class="dialog-content">
          {{ buildFullText(previewRow) }}
        </p>
        <div class="dialog-meta">
          <a
            v-if="getSourceUrl(previewRow)"
            class="source-link"
            :href="getSourceUrl(previewRow)"
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
      <el-descriptions v-else-if="detailItem" :column="2" border>
        <el-descriptions-item
          v-for="field in normalizedDetailFields"
          :key="field.key || field.label"
          :label="field.label"
          :span="field.span || 1"
        >
          <template v-if="field.type === 'link'">
            <el-link
              v-if="resolveFieldValue(detailItem, field)"
              :href="resolveFieldValue(detailItem, field)"
              target="_blank"
              rel="noopener noreferrer"
            >
              {{ field.linkText || "点击查看" }}
            </el-link>
            <span v-else>-</span>
          </template>
          <template v-else>
            <span class="desc-text">{{
              resolveFieldValue(detailItem, field) || "-"
            }}</span>
          </template>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import { getItemDetailApi } from "../../api/item";

defineProps({
  rows: {
    type: Array,
    default: () => [],
  },
});

defineEmits(["edit", "remove"]);

const previewVisible = ref(false);
const previewRow = ref(null);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detailItem = ref(null);

// 文本卡片展示字段开关，可按需修改。
const defaultVisibleFields = {
  title: true,
  content: true,
  releaseYear: true,
  author: true,
  sourceLink: true,
  rating: true,
  actions: true,
};

// 文本详情弹窗标题与字段配置，可按需修改 label/key/span/type/linkText。
const detailDialogTitle = "文本详情";

const defaultDetailFields = [
  { key: "title", label: "标题" },
  { key: "author", label: "作者" },
  { key: "contentTypeLabel", label: "类型" },
  { keys: ["releaseYear", "release_year"], label: "年份" },
  {
    label: "源链接",
    keys: ["sourceUrl", "source_url"],
    span: 2,
    type: "link",
    linkText: "打开链接",
  },
  { key: "summary", label: "简介", span: 2 },
  { key: "notes", label: "备注", span: 2 },
];

const normalizedDetailFields = computed(() => defaultDetailFields);

const getSourceUrl = (row) => row?.sourceUrl || row?.source_url || "";

const pickFirstNonEmptyString = (values) => {
  for (const value of values) {
    if (value === null || value === undefined) continue;
    const text = String(value).trim();
    if (text) return text;
  }
  return "";
};

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

const openPreview = (row) => {
  previewRow.value = row;
  previewVisible.value = true;
};

const normalizeDetail = (data = {}) => ({
  ...data,
  id: data?.id,
  mediaType: data?.mediaType ?? data?.media_type,
  mediaTypeLabel: data?.mediaTypeLabel ?? data?.media_type_label,
  contentType: data?.contentType ?? data?.content_type,
  contentTypeLabel: data?.contentTypeLabel ?? data?.content_type_label,
  releaseYear: data?.releaseYear ?? data?.release_year ?? null,
  storeUrl: data?.storeUrl ?? data?.store_url ?? "",
  sourceUrl: data?.sourceUrl ?? data?.source_url ?? "",
  trackingType: data?.trackingType ?? data?.tracking_type,
});

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

const isFieldVisible = (key) => {
  return defaultVisibleFields[key] !== false;
};

const resolveReleaseYear = (row) => {
  const year = row?.releaseYear ?? row?.release_year;
  if (year === null || year === undefined || String(year).trim() === "") return "";
  return String(year);
};

const resolveFieldValue = (row, field) => {
  if (!row || !field) return "";
  const keys = Array.isArray(field.keys)
    ? field.keys
    : field.key
      ? [field.key]
      : [];
  for (const key of keys) {
    const value = row?.[key];
    if (value !== undefined && value !== null && String(value).trim() !== "") {
      return String(value);
    }
  }
  return "";
};
</script>

<style scoped>
.text-panel-wrap {
  min-width: 0;
  margin-bottom: 12px;
}

.text-card-list {
  display: grid;
  gap: 12px;
}

.text-card {
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 14px;
  background: var(--bg-panel-strong);
  box-shadow: var(--shadow);
  cursor: pointer;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    border-color 0.18s ease;
}

.text-card:hover {
  transform: translateY(-2px);
  border-color: var(--xhs-yellow);
  box-shadow: 0 10px 20px rgba(38, 38, 38, 0.08);
}

.card-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.card-title {
  margin: 0;
  font-size: 17px;
  line-height: 1.35;
  font-weight: 700;
  color: var(--xhs-black);
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-author {
  margin: 0;
  font-size: 12px;
  line-height: 1.2;
  color: var(--grey);
  flex-shrink: 0;
}

.card-content {
  margin: 10px 0;
  font-size: 14px;
  line-height: 1.65;
  color: var(--xhs-black);
  opacity: 0.9;
  white-space: pre-line;
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  min-height: calc(1.65em * 2);
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
}

.source-link {
  color: var(--xhs-orange);
  text-decoration: none;
  border-bottom: 1px dashed currentColor;
}

.source-link:hover {
  opacity: 0.8;
}

.meta-muted {
  color: var(--grey);
}

.meta-rating {
  color: var(--grey);
}

.card-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action {
  border: 0;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  line-height: 1;
  color: #fff;
  background: rgba(38, 38, 38, 0.55);
  cursor: pointer;
}

.action:hover {
  background: rgba(38, 38, 38, 0.75);
}

.action.danger {
  background: rgba(235, 122, 83, 0.86);
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

.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dialog-content {
  margin: 0;
  font-size: 14px;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--xhs-black);
  max-height: 62vh;
  overflow: auto;
  padding-right: 4px;
}

.dialog-meta {
  font-size: 12px;
}

.desc-text {
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 640px) {
  .card-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}
</style>

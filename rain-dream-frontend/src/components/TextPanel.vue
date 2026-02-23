<template>
  <div class="text-panel-wrap">
    <el-skeleton v-if="loading" :rows="4" animated />
    <el-empty v-else-if="rows.length === 0" description="暂无文本" />

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
          <div v-if="isFieldVisible('title')" class="card-title-wrap">
            <h3 class="card-title">
              {{ row.title || "未命名文本" }}
            </h3>
            <button
              :class="[
                'favorite-toggle',
                Number(row?.isFavorite) === 1 ? 'is-favorite' : 'is-outline',
              ]"
              type="button"
              :title="Number(row?.isFavorite) === 1 ? '取消收藏' : '收藏'"
              @click.stop="toggleFavorite(row)"
            >
              <el-icon>
                <StarFilled v-if="Number(row?.isFavorite) === 1" />
                <Star v-else />
              </el-icon>
            </button>
          </div>
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
              isFieldVisible('releaseYear') && resolveReleaseYear(row) !== ''
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
import { ElMessage } from "element-plus";
import { Star, StarFilled } from "@element-plus/icons-vue";
import { getItemDetailApi, setItemFavoriteApi } from "../api/item";

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
  { key: "releaseYear", label: "年份" },
  {
    label: "源链接",
    key: "sourceUrl",
    span: 2,
    type: "link",
    linkText: "打开链接",
  },
  { key: "summary", label: "总结", span: 2 },
  { key: "notes", label: "备注", span: 2 },
];

const normalizedDetailFields = computed(() => defaultDetailFields);

const getSourceUrl = (row) => row?.sourceUrl || "";

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
  mediaType: data?.mediaType,
  mediaTypeLabel: data?.mediaTypeLabel,
  contentType: data?.contentType,
  contentTypeLabel: data?.contentTypeLabel,
  releaseYear: data?.releaseYear ?? null,
  storeUrl: data?.storeUrl ?? "",
  sourceUrl: data?.sourceUrl ?? "",
  trackingType: data?.trackingType,
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
  const year = row?.releaseYear;
  if (year === null || year === undefined || String(year).trim() === "")
    return "";
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

const toggleFavorite = async (row) => {
  const id = row?.id;
  if (!id) return;
  const current = Number(row?.isFavorite) === 1 ? 1 : 0;
  const next = current === 1 ? 0 : 1;
  await setItemFavoriteApi(id, next);
  row.isFavorite = next;
  ElMessage.success(next === 1 ? "已收藏" : "已取消收藏");
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

.card-title-wrap {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
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

.favorite-toggle {
  width: 24px;
  height: 24px;
  border: 0;
  background: transparent;
  color: var(--xhs-orange);
  font-size: 20px;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
}

.favorite-toggle :deep(.el-icon) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--xhs-orange);
}

.favorite-toggle.is-outline :deep(.el-icon) {
  color: var(--xhs-orange);
}

.favorite-toggle.is-favorite :deep(.el-icon) {
  color: var(--xhs-orange);
}

.favorite-toggle:hover :deep(.el-icon) {
  color: var(--xhs-orange-hover);
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

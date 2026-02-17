<template>
  <div class="table-wrap" @wheel.capture="onTableWheel">
    <el-table ref="tableRef" :data="rows" stripe v-loading="loading">
      <el-table-column
        prop="title"
        label="标题"
        min-width="180"
        show-overflow-tooltip
      />
      <el-table-column
        prop="author"
        label="作者"
        min-width="120"
        show-overflow-tooltip
      />

      <el-table-column label="来源" min-width="110">
        <template #default="{ row }">
          <el-link
            v-if="row.sourceUrl"
            :href="row.sourceUrl"
            target="_blank"
            rel="noopener noreferrer"
            @click.stop
            >点击跳转</el-link
          >
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" min-width="150">
        <template #default="{ row }">
          <div class="action-cell">
            <el-link @click.stop="openDetail(row.id)">详情</el-link>
            <el-link @click.stop="$emit('edit', row.id)">编辑</el-link>
            <el-link type="danger" @click.stop="$emit('remove', row.id)"
              >删除</el-link
            >
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog v-model="detailVisible" width="min(760px, 92vw)" destroy-on-close>
    <template #header>
      <div class="dialog-head">
        <span>{{ detailItem?.title || "链接详情" }}</span>
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
</template>

<script setup>
import { computed, ref } from "vue";
import { getItemDetailApi } from "../api/item";

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

const tableRef = ref(null);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detailItem = ref(null);

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

const onTableWheel = (event) => {
  if (!event.shiftKey) return;
  const tableEl = tableRef.value?.$el;
  if (!tableEl) return;
  const bodyWrap =
    tableEl.querySelector(".el-table__body-wrapper .el-scrollbar__wrap") ||
    tableEl.querySelector(".el-table__body-wrapper");
  if (!bodyWrap || bodyWrap.scrollWidth <= bodyWrap.clientWidth) return;
  const delta = Math.abs(event.deltaX) > 0 ? event.deltaX : event.deltaY;
  bodyWrap.scrollLeft += delta;
  event.preventDefault();
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
</script>

<style scoped>
.table-wrap {
  min-width: 0;
  margin-bottom: 12px;
}

.action-cell {
  display: flex;
  justify-content: left;
  gap: 18px;
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

.desc-text {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>

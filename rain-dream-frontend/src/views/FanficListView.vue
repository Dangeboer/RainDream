<template>
  <section ref="panelRef" class="card-panel panel">
    <h2>文章 / {{ activeTabLabel }}</h2>

    <div class="media-tabs">
      <el-button
        v-for="entry in tabs"
        :key="entry.value"
        size="small"
        plain
        :class="{ 'is-main-tab-active': activeTab === entry.value }"
        @click="onTabChange(entry.value)"
      >
        {{ entry.label }}
      </el-button>
    </div>

    <div
      v-if="activeTab === 'list'"
      class="table-wrap"
      @wheel.capture="onTableWheel"
    >
      <el-table
        ref="tableRef"
        :data="rows"
        v-loading="loading"
        stripe
        fit
        table-layout="fixed"
        style="width: 100%"
        @row-click="onRowClick"
      >
        <!-- <el-table-column prop="id" label="ID" width="80" /> -->
        <el-table-column
          prop="eraLabel"
          label="年代"
          min-width="80"
          show-overflow-tooltip
        />
        <el-table-column
          prop="title"
          label="名称"
          min-width="160"
          show-overflow-tooltip
        />
        <!-- <el-table-column prop="cp" label="CP" width="80" /> -->
        <el-table-column
          prop="author"
          label="作者"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="trackingTypeLabel"
          label="追踪"
          min-width="80"
          show-overflow-tooltip
        />
        <el-table-column prop="rating" label="评分" min-width="70" />
        <el-table-column
          prop="lengthTypeLabel"
          label="篇幅"
          min-width="80"
          show-overflow-tooltip
        />
        <el-table-column
          prop="workTypeLabel"
          label="状态"
          min-width="80"
          show-overflow-tooltip
        />
        <el-table-column
          prop="endingTypeLabel"
          label="结局"
          min-width="80"
          show-overflow-tooltip
        />
        <el-table-column
          prop="updateDate"
          label="上次更新"
          min-width="110"
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
            >
              点击跳转
            </el-link>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" class-name="op-col" width="120">
          <template #default="{ row }">
            <div class="action-cell">
              <el-link @click.stop="onEdit(row.id)">编辑</el-link>
              <el-link type="danger" @click.stop="remove(row.id)">删除</el-link>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div v-else v-loading="loading">
      <ImagePanel :rows="rows" @remove="remove" @updated="fetchData" />
    </div>

    <el-pagination
      layout="prev, pager, next"
      :total="total"
      :page-size="query.size"
      :current-page="query.page"
      @current-change="onPageChange"
    />
  </section>
</template>

<script setup>
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
  watch,
} from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import ImagePanel from "../components/ImagePanel.vue";
import { deleteItemApi, getFanficListApi, getItemListApi } from "../api/item";
import {
  DEFAULT_PAGE_SIZE,
  GRID_CARD_GAP,
  GRID_CARD_MIN_WIDTH,
  clampPageSize,
  extractListPayload,
  resolveItemMediaType,
} from "../composables/contentManageConfig";

const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const tableRef = ref(null);
const panelRef = ref(null);
const route = useRoute();
const router = useRouter();
let requestSeq = 0;
let panelResizeObserver = null;
let resizeTimer = null;
const activeTab = ref("list");
const tabs = [
  { value: "list", label: "列表" },
  { value: "image", label: "图片" },
];
const LIST_PAGE_SIZE = 10;
const query = reactive({
  page: 1,
  size: LIST_PAGE_SIZE,
});
const imageCache = reactive({
  nextRawPage: 1,
  rawTotal: 0,
  rawLoadedCount: 0,
  rawExhausted: false,
  records: [],
});
let imageCacheLoadingPromise = null;

const activeTabLabel = computed(
  () => tabs.find((item) => item.value === activeTab.value)?.label || "列表",
);

const resetImageCache = () => {
  imageCache.nextRawPage = 1;
  imageCache.rawTotal = 0;
  imageCache.rawLoadedCount = 0;
  imageCache.rawExhausted = false;
  imageCache.records = [];
  imageCacheLoadingPromise = null;
};

const appendUniqueImageRecords = (records) => {
  if (!Array.isArray(records) || records.length === 0) return;
  const exists = new Set(imageCache.records.map((item) => item.id));
  for (const record of records) {
    const key = record?.id;
    if (!key || exists.has(key)) continue;
    imageCache.records.push(record);
    exists.add(key);
  }
};

const hasMoreImageRawPages = () => !imageCache.rawExhausted;

const fetchNextImageRawPage = async () => {
  const raw = await getItemListApi({
    page: imageCache.nextRawPage,
    size: 24,
    contentType: 1,
  });
  const normalizedList = extractListPayload(raw);
  const imageRows = normalizedList.filter((item) =>
    [2, 3].includes(Number(resolveItemMediaType(item))),
  );

  appendUniqueImageRecords(imageRows);
  imageCache.rawTotal = Number(raw?.total || imageCache.rawTotal || 0);
  imageCache.rawLoadedCount += normalizedList.length;
  imageCache.nextRawPage += 1;

  if (
    imageCache.rawTotal > 0 &&
    imageCache.rawLoadedCount >= imageCache.rawTotal
  ) {
    imageCache.rawExhausted = true;
  }
  if (normalizedList.length === 0 || normalizedList.length < 24) {
    imageCache.rawExhausted = true;
  }
};

const ensureImageCacheCount = async (requiredCount) => {
  while (imageCache.records.length < requiredCount && hasMoreImageRawPages()) {
    if (!imageCacheLoadingPromise) {
      imageCacheLoadingPromise = fetchNextImageRawPage().finally(() => {
        imageCacheLoadingPromise = null;
      });
    }
    await imageCacheLoadingPromise;
  }
};

const calculateImageAutoPageSize = () => {
  const panelEl = panelRef.value;
  if (!panelEl) return query.size || DEFAULT_PAGE_SIZE;
  const gridWrap = panelEl.querySelector(".media-grid-wrap");
  const paginationEl = panelEl.querySelector(".el-pagination");
  if (!gridWrap || !paginationEl) return query.size || DEFAULT_PAGE_SIZE;

  const availableWidth = Math.max(
    panelEl.clientWidth - 32,
    GRID_CARD_MIN_WIDTH,
  );
  const columns = Math.max(
    1,
    Math.floor(
      (availableWidth + GRID_CARD_GAP) / (GRID_CARD_MIN_WIDTH + GRID_CARD_GAP),
    ),
  );
  const cardWidth = (availableWidth - (columns - 1) * GRID_CARD_GAP) / columns;
  const cardHeight = cardWidth * 0.75;

  const gridTop = gridWrap.getBoundingClientRect().top;
  const paginationHeight = paginationEl.getBoundingClientRect().height || 32;
  const availableHeight = Math.max(
    120,
    window.innerHeight - gridTop - paginationHeight - 24,
  );
  const rowsCount = Math.max(
    1,
    Math.floor(
      (availableHeight + GRID_CARD_GAP) / (cardHeight + GRID_CARD_GAP),
    ),
  );

  return clampPageSize(columns * rowsCount);
};

const syncAdaptivePageSize = async () => {
  if (activeTab.value !== "image") return false;
  await nextTick();
  const targetSize = calculateImageAutoPageSize();
  if (targetSize === query.size) return false;
  query.size = targetSize;
  query.page = 1;
  return true;
};

const scheduleAdaptivePageSizeSync = () => {
  if (resizeTimer) {
    clearTimeout(resizeTimer);
  }
  resizeTimer = setTimeout(async () => {
    const replaced = await syncAdaptivePageSize();
    if (replaced) {
      fetchData();
    }
  }, 120);
};

const fetchData = async ({ reset = false } = {}) => {
  const currentSeq = ++requestSeq;
  if (reset) {
    rows.value = [];
    total.value = 0;
    if (activeTab.value === "image") {
      resetImageCache();
    }
  }
  loading.value = true;
  try {
    if (activeTab.value === "list") {
      const resp = await getFanficListApi(query);
      const list = extractListPayload(resp).filter(
        (item) =>
          Number(item?.contentType) === 1 && Number(item?.mediaType) === 1,
      );
      const nextRows = list.map((item) => ({
        id: item.id,
        storeUrl: item.storeUrl ?? "",
        content: item.content ?? "",
        title: item.title ?? "",
        cp: item.cp,
        author: item.author ?? "-",
        sourceUrl: item.sourceUrl ?? "",
        trackingTypeLabel: item.trackingTypeLabel ?? "",
        rating: item.rating ?? "-",
        itemId: item.fanficVO?.itemId ?? "",
        eraLabel: item.fanficVO?.eraLabel ?? "",
        charSetting: item.fanficVO?.charSetting ?? "",
        lengthTypeLabel: item.fanficVO?.lengthTypeLabel ?? "",
        workTypeLabel: item.fanficVO?.workTypeLabel ?? "",
        updateDate: item.fanficVO?.updateDate ?? "-",
        endingTypeLabel: item.fanficVO?.endingTypeLabel ?? "-",
        readCount: item.fanficVO?.readCount ?? 0,
      }));
      const nextTotal = Number(resp?.total ?? nextRows.length);
      if (currentSeq !== requestSeq) return;
      rows.value = nextRows;
      total.value = nextTotal;
      return;
    }

    const requiredCount = query.page * query.size;
    await ensureImageCacheCount(requiredCount);
    const mergedRows = [...imageCache.records].sort(
      (a, b) => Number(b?.id || 0) - Number(a?.id || 0),
    );
    const offset = (query.page - 1) * query.size;
    const nextRows = mergedRows.slice(offset, offset + query.size);
    const nextTotal = imageCache.rawExhausted
      ? mergedRows.length
      : mergedRows.length + query.size;
    if (currentSeq !== requestSeq) return;
    rows.value = nextRows;
    total.value = nextTotal;
  } finally {
    if (currentSeq === requestSeq) {
      loading.value = false;
    }
  }
};

const onTableWheel = (event) => {
  const tableEl = tableRef.value?.$el;
  if (!tableEl) return;
  const bodyWrap =
    tableEl.querySelector(".el-table__body-wrapper .el-scrollbar__wrap") ||
    tableEl.querySelector(".el-table__body-wrapper");
  if (!bodyWrap || bodyWrap.scrollWidth <= bodyWrap.clientWidth) return;

  const hasHorizontalDelta = Math.abs(event.deltaX) > 0;
  if (hasHorizontalDelta) {
    bodyWrap.scrollLeft += event.deltaX;
    event.preventDefault();
    return;
  }

  if (event.shiftKey && Math.abs(event.deltaY) > 0) {
    bodyWrap.scrollLeft += event.deltaY;
    event.preventDefault();
  }
};

const onRowClick = (row) => {
  if (!row?.id) return;
  router.push(`/fanfic/${row.id}`);
};

const onEdit = (id) => {
  if (!id) return;
  router.push(`/items/edit/${id}`);
};

const remove = async (id) => {
  if (!id) return;
  try {
    await ElMessageBox.confirm("确认删除该作品吗？", "提示", {
      type: "warning",
      confirmButtonText: "确认",
      cancelButtonText: "取消",
      beforeClose: async (action, instance, done) => {
        if (action !== "confirm") {
          done();
          return;
        }
        instance.confirmButtonLoading = true;
        instance.confirmButtonText = "确认...";
        try {
          await deleteItemApi(id);
          ElMessage.success("删除成功");
          await fetchData({ reset: true });
          done();
        } finally {
          instance.confirmButtonLoading = false;
          instance.confirmButtonText = "确认";
        }
      },
    });
  } catch (error) {
    if (error !== "cancel" && error !== "close") {
      throw error;
    }
  }
};

const onPageChange = (page) => {
  query.page = page;
  fetchData();
};

const onTabChange = async (tab) => {
  if (activeTab.value === tab) return;
  await router.push({
    path: "/fanfic",
    query: tab === "image" ? { tab: "image" } : undefined,
  });
};

watch(
  () => activeTab.value,
  () => {
    scheduleAdaptivePageSizeSync();
  },
);

watch(
  () => route.query.tab,
  async (tabValue) => {
    const nextTab = String(tabValue || "") === "image" ? "image" : "list";
    activeTab.value = nextTab;
    query.page = 1;
    if (nextTab !== "image") {
      query.size = LIST_PAGE_SIZE;
    } else {
      await syncAdaptivePageSize();
    }
    await fetchData({ reset: true });
  },
  { immediate: true },
);

onMounted(async () => {
  panelResizeObserver = new ResizeObserver(() => {
    scheduleAdaptivePageSizeSync();
  });
  if (panelRef.value) {
    panelResizeObserver.observe(panelRef.value);
  }
  window.addEventListener("resize", scheduleAdaptivePageSizeSync);
  scheduleAdaptivePageSizeSync();
});

onBeforeUnmount(() => {
  if (panelResizeObserver) {
    panelResizeObserver.disconnect();
    panelResizeObserver = null;
  }
  window.removeEventListener("resize", scheduleAdaptivePageSizeSync);
  if (resizeTimer) {
    clearTimeout(resizeTimer);
    resizeTimer = null;
  }
});
</script>

<style scoped>
.panel {
  --fanfic-table-pagination-gap: 10px;
  padding: 16px;
}

.table-wrap {
  min-width: 0;
  margin-bottom: var(--fanfic-table-pagination-gap);
}

.panel :deep(.el-pagination) {
  margin-top: 0;
}

.media-tabs {
  display: flex;
  gap: 2px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.media-tabs .el-button {
  font-size: 14px;
}

.media-tabs .el-button.is-main-tab-active {
  --el-button-bg-color: var(--xhs-yellow);
  --el-button-border-color: var(--xhs-yellow);
  --el-button-text-color: var(--xhs-black);
}

:deep(.el-table .el-table__body tr) {
  cursor: pointer;
}

:deep(.el-table .op-col .cell) {
  padding-left: 0;
  padding-right: 0;
}

.action-cell {
  display: flex;
  gap: 18px;
}
</style>

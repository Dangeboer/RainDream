<template>
  <section class="card-panel panel">
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
        <el-table-column label="操作" class-name="op-col" min-width="140">
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
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import ImagePanel from "../components/ImagePanel.vue";
import { deleteItemApi, getFanficListApi, getItemListApi } from "../api/item";
import { resolveItemMediaType } from "../composables/contentManageConfig";

const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const tableRef = ref(null);
const router = useRouter();
let requestSeq = 0;
const activeTab = ref("list");
const tabs = [
  { value: "list", label: "列表" },
  { value: "image", label: "图片" },
];
const query = reactive({
  page: 1,
  size: 10,
});

const activeTabLabel = computed(
  () => tabs.find((item) => item.value === activeTab.value)?.label || "列表",
);

const fetchData = async ({ reset = false } = {}) => {
  const currentSeq = ++requestSeq;
  if (reset) {
    rows.value = [];
    total.value = 0;
  }
  loading.value = true;
  try {
    if (activeTab.value === "list") {
      const resp = await getFanficListApi(query);
      const list = Array.isArray(resp?.data) ? resp.data : [];
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
    const baseParams = {
      page: 1,
      size: requiredCount,
      contentType: 1,
    };

    const [staticResp, gifResp] = await Promise.all([
      getItemListApi({ ...baseParams, mediaType: 2 }),
      getItemListApi({ ...baseParams, mediaType: 3 }),
    ]);

    const staticRows = Array.isArray(staticResp?.records)
      ? staticResp.records
      : [];
    const gifRows = Array.isArray(gifResp?.records) ? gifResp.records : [];
    const mergedRows = [...staticRows, ...gifRows]
      .filter((item) => [2, 3].includes(Number(resolveItemMediaType(item))))
      .sort((a, b) => Number(b?.id || 0) - Number(a?.id || 0));
    const offset = (query.page - 1) * query.size;
    const nextRows = mergedRows.slice(offset, offset + query.size);
    const nextTotal =
      Number(staticResp?.total || 0) + Number(gifResp?.total || 0);
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
  await ElMessageBox.confirm("确认删除该作品吗？", "提示", { type: "warning" });
  await deleteItemApi(id);
  ElMessage.success("删除成功");
  fetchData();
};

const onPageChange = (page) => {
  query.page = page;
  fetchData();
};

const onTabChange = (tab) => {
  if (activeTab.value === tab) return;
  activeTab.value = tab;
  query.page = 1;
  fetchData({ reset: true });
};

onMounted(fetchData);
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

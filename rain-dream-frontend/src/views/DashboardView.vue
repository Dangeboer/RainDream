<template>
  <section class="dashboard" v-loading="loading">
    <h1>首页</h1>

    <section class="card-panel panel">
      <div class="head">
        <h2>收藏文章</h2>
      </div>

      <div class="table-wrap" @wheel.capture="onTableWheel">
        <el-table
          ref="tableRef"
          :data="fanficRows"
          stripe
          fit
          table-layout="fixed"
          style="width: 100%"
          @row-click="onRowClick"
        >
          <el-table-column label="收藏" class-name="favorite-col" width="60">
            <template #default="{ row }">
              <button
                :class="[
                  'favorite-toggle',
                  Number(row?.isFavorite) === 1 ? 'is-favorite' : 'is-outline',
                ]"
                type="button"
                :title="Number(row?.isFavorite) === 1 ? '取消收藏' : '收藏'"
                @click.stop="toggleFavoriteFanfic(row)"
              >
                <el-icon>
                  <StarFilled v-if="Number(row?.isFavorite) === 1" />
                  <Star v-else />
                </el-icon>
              </button>
            </template>
          </el-table-column>
          <el-table-column
            prop="eraLabel"
            label="年代"
            min-width="80"
            show-overflow-tooltip
          />
          <el-table-column
            prop="title"
            label="名称"
            min-width="120"
            show-overflow-tooltip
          />
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
          <el-table-column label="来源" min-width="90">
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
                <el-link type="danger" @click.stop="remove(row.id)"
                  >删除</el-link
                >
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-pagination
        layout="prev, pager, next"
        :total="fanficTotal"
        :page-size="fanficQuery.size"
        :current-page="fanficQuery.page"
        @current-change="onFanficPageChange"
      />

      <el-dialog
        v-model="detailVisible"
        width="min(880px, 94vw)"
        destroy-on-close
      >
        <template #header>
          <div class="dialog-head">
            <span>{{ detailItem?.title || "文章详情" }}</span>
          </div>
        </template>
        <el-skeleton v-if="detailLoading" :rows="8" animated />
        <el-descriptions
          v-else-if="detailItem"
          :column="2"
          border
          :label-width="110"
        >
          <el-descriptions-item label="标题">{{
            showDetail(detailItem.title)
          }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{
            showDetail(detailItem.author)
          }}</el-descriptions-item>
          <el-descriptions-item label="原作">{{
            showDetail(detailItem.fandom)
          }}</el-descriptions-item>
          <el-descriptions-item label="CP">{{
            showDetail(detailItem.cp)
          }}</el-descriptions-item>
          <el-descriptions-item label="内容类型">{{
            showDetail(detailItem.contentTypeLabel)
          }}</el-descriptions-item>
          <el-descriptions-item label="媒介类型">{{
            showDetail(detailItem.mediaTypeLabel)
          }}</el-descriptions-item>
          <el-descriptions-item label="追踪状态">{{
            showDetail(detailItem.trackingTypeLabel)
          }}</el-descriptions-item>
          <el-descriptions-item label="评分">{{
            showDetail(detailItem.rating)
          }}</el-descriptions-item>
          <el-descriptions-item label="发布年份">{{
            showDetail(detailItem.releaseYear)
          }}</el-descriptions-item>
          <el-descriptions-item label="阅读次数">{{
            showDetail(detailItem.fanficVO?.readCount)
          }}</el-descriptions-item>
          <el-descriptions-item label="年代">{{
            showDetail(detailItem.fanficVO?.eraLabel)
          }}</el-descriptions-item>
          <el-descriptions-item label="篇幅">{{
            showDetail(detailItem.fanficVO?.lengthTypeLabel)
          }}</el-descriptions-item>
          <el-descriptions-item label="作品状态">{{
            showDetail(detailItem.fanficVO?.workTypeLabel)
          }}</el-descriptions-item>
          <el-descriptions-item label="结局">{{
            showDetail(detailItem.fanficVO?.endingTypeLabel)
          }}</el-descriptions-item>
          <el-descriptions-item label="设定">{{
            showDetail(detailItem.fanficVO?.charSetting)
          }}</el-descriptions-item>
          <el-descriptions-item label="上次更新日期">{{
            showDetail(detailItem.fanficVO?.updateDate)
          }}</el-descriptions-item>
          <el-descriptions-item label="下载链接">
            <el-link
              v-if="detailItem.storeUrl"
              :href="detailItem.storeUrl"
              :download="resolveDownloadName(detailItem)"
              target="_blank"
              rel="noopener noreferrer"
              @click.prevent="handleDetailDownload"
            >
              点击下载
            </el-link>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="文件大小">{{
            formatFileSize(detailItem.sizeBytes)
          }}</el-descriptions-item>
          <el-descriptions-item label="平台" :span="2">{{
            detailPlatformText
          }}</el-descriptions-item>
          <el-descriptions-item label="标签" :span="2">{{
            detailTagText
          }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{
            showDetail(detailItem.notes)
          }}</el-descriptions-item>
          <el-descriptions-item label="总结" :span="2">{{
            showDetail(detailItem.summary)
          }}</el-descriptions-item>
          <el-descriptions-item label="来源" :span="2">
            <el-link
              v-if="detailItem.sourceUrl"
              :href="detailItem.sourceUrl"
              target="_blank"
              rel="noopener noreferrer"
            >
              {{ detailItem.sourceUrl }}
            </el-link>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="内容" :span="2">{{
            showDetail(detailItem.content)
          }}</el-descriptions-item>
        </el-descriptions>
      </el-dialog>
    </section>

    <section class="card-panel panel">
      <div class="head">
        <h2>收藏图片</h2>
      </div>

      <ImagePanel :rows="imageRows" @remove="remove" @updated="loadData" />

      <el-pagination
        layout="prev, pager, next"
        :total="imageTotal"
        :page-size="imageQuery.size"
        :current-page="imageQuery.page"
        @current-change="onImagePageChange"
      />
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Star, StarFilled } from "@element-plus/icons-vue";
import ImagePanel from "../components/ImagePanel.vue";
import {
  deleteItemApi,
  getFanficDetailApi,
  getFanficListApi,
  getItemListApi,
  setItemFavoriteApi,
} from "../api/item";
import { extractListPayload } from "../composables/contentManageConfig";

const router = useRouter();
const tableRef = ref(null);

const loading = ref(false);

const allFavoriteFanfics = ref([]);
const fanficQuery = reactive({ page: 1, size: 10 });
const fanficTotal = computed(() => allFavoriteFanfics.value.length);
const fanficRows = computed(() => {
  const start = (fanficQuery.page - 1) * fanficQuery.size;
  return allFavoriteFanfics.value.slice(start, start + fanficQuery.size);
});

const allFavoriteImages = ref([]);
const imageQuery = reactive({ page: 1, size: 12 });
const imageTotal = computed(() => allFavoriteImages.value.length);
const imageRows = computed(() => {
  const start = (imageQuery.page - 1) * imageQuery.size;
  return allFavoriteImages.value.slice(start, start + imageQuery.size);
});

const detailVisible = ref(false);
const detailLoading = ref(false);
const detailItem = ref(null);

const detailTagText = computed(() => {
  const tags = detailItem.value?.tagVOS;
  if (!Array.isArray(tags) || tags.length === 0) return "-";
  return (
    tags
      .map((tag) => tag.tagName)
      .filter(Boolean)
      .join(" / ") || "-"
  );
});

const detailPlatformText = computed(() => {
  const platforms = detailItem.value?.pltVOS;
  if (!Array.isArray(platforms) || platforms.length === 0) return "-";
  return (
    platforms
      .map((platform) => platform.pltName)
      .filter(Boolean)
      .join(" / ") || "-"
  );
});

const showDetail = (value) =>
  value === null || value === undefined || value === "" ? "-" : value;

const pickText = (value) => {
  if (value === null || value === undefined) return "";
  return String(value).trim();
};

const resolveDownloadName = (item) => {
  const title = pickText(item?.title);
  if (title) return title;
  const fileName = pickText(item?.fileName);
  if (fileName) return fileName;
  const storeUrl = pickText(item?.storeUrl);
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

const handleDetailDownload = async () => {
  const item = detailItem.value;
  const storeUrl = pickText(item?.storeUrl);
  if (!storeUrl) return;
  const fileName = resolveDownloadName(item);
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

const formatFileSize = (bytes) => {
  const size = Number(bytes);
  if (!Number.isFinite(size) || size <= 0) return "-";
  const KB = 1000;
  const MB = 1000 * KB;
  const GB = 1000 * MB;
  if (size >= GB) return `${(size / GB).toFixed(2)} GB`;
  if (size >= MB) return `${(size / MB).toFixed(2)} MB`;
  return `${Math.max(size / KB, 0.01).toFixed(2)} KB`;
};

const normalizeFanficListRow = (item) => ({
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
  isFavorite: Number(item?.isFavorite) === 1 ? 1 : 0,
});

const loadData = async () => {
  loading.value = true;
  try {
    const [fanficResp, imageResp] = await Promise.all([
      getFanficListApi({}),
      getItemListApi({ mediaType: 2, isFavorite: 1 }),
    ]);

    allFavoriteFanfics.value = extractListPayload(fanficResp)
      .filter(
        (item) =>
          Number(item?.contentType) === 1 &&
          Number(item?.mediaType) === 1 &&
          Number(item?.isFavorite) === 1,
      )
      .map(normalizeFanficListRow);

    allFavoriteImages.value = extractListPayload(imageResp);

    const fanficMaxPage = Math.max(
      1,
      Math.ceil(fanficTotal.value / fanficQuery.size),
    );
    if (fanficQuery.page > fanficMaxPage) fanficQuery.page = fanficMaxPage;

    const imageMaxPage = Math.max(
      1,
      Math.ceil(imageTotal.value / imageQuery.size),
    );
    if (imageQuery.page > imageMaxPage) imageQuery.page = imageMaxPage;
  } finally {
    loading.value = false;
  }
};

const openDetailDialog = async (id) => {
  if (!id) return;
  detailVisible.value = true;
  detailLoading.value = true;
  detailItem.value = null;
  try {
    detailItem.value = await getFanficDetailApi(id);
  } finally {
    detailLoading.value = false;
  }
};

const onRowClick = async (row) => {
  if (!row?.id) return;
  await openDetailDialog(row.id);
};

const onEdit = (id) => {
  if (!id) return;
  router.push(`/items/edit/${id}`);
};

const toggleFavoriteFanfic = async (row) => {
  const id = row?.id;
  if (!id) return;
  const current = Number(row?.isFavorite) === 1 ? 1 : 0;
  const next = current === 1 ? 0 : 1;
  await setItemFavoriteApi(id, next);
  row.isFavorite = next;
  if (next === 0) {
    allFavoriteFanfics.value = allFavoriteFanfics.value.filter(
      (item) => item.id !== id,
    );
    const fanficMaxPage = Math.max(
      1,
      Math.ceil(fanficTotal.value / fanficQuery.size),
    );
    if (fanficQuery.page > fanficMaxPage) fanficQuery.page = fanficMaxPage;
  }
  ElMessage.success(next === 1 ? "已收藏" : "已取消收藏");
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
        instance.confirmButtonText = "确认";
        try {
          await deleteItemApi(id);
          ElMessage.success("删除成功");
          await loadData();
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

const onFanficPageChange = (page) => {
  fanficQuery.page = page;
};

const onImagePageChange = (page) => {
  imageQuery.page = page;
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

onMounted(loadData);
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

h1 {
  margin: 0;
}

.panel {
  --fanfic-table-pagination-gap: 10px;
  padding: 16px;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.table-wrap {
  min-width: 0;
  margin-bottom: var(--fanfic-table-pagination-gap);
}

.panel :deep(.el-pagination) {
  margin-top: 0;
}

:deep(.el-table .el-table__body tr) {
  cursor: pointer;
}

:deep(.el-table .op-col .cell) {
  padding-left: 0;
  padding-right: 0;
}

:deep(.el-table .favorite-col .cell) {
  display: flex;
  justify-content: center;
}

.action-cell {
  display: flex;
  gap: 18px;
}

.favorite-toggle {
  width: 24px;
  height: 24px;
  border: 0;
  background: transparent;
  color: var(--xhs-orange);
  font-size: 18px;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
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

.dialog-head {
  display: flex;
  gap: 10px;
  align-items: baseline;
}
</style>

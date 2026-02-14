<template>
  <section class="card-panel panel">
    <h2>文章列表</h2>
    <div class="table-wrap" @wheel.capture="onTableWheel">
      <el-table ref="tableRef" :data="rows" stripe fit table-layout="fixed" style="width: 100%">
      <!-- <el-table-column prop="id" label="ID" width="80" /> -->
      <el-table-column prop="eraLabel" label="年代" min-width="80" show-overflow-tooltip />
      <el-table-column prop="title" label="名称" min-width="160" show-overflow-tooltip />
      <!-- <el-table-column prop="cp" label="CP" width="80" /> -->
      <el-table-column prop="author" label="作者" min-width="120" show-overflow-tooltip />
      <el-table-column prop="trackingTypeLabel" label="追踪" min-width="80" show-overflow-tooltip />
      <el-table-column prop="rating" label="评分" min-width="70" />
      <el-table-column prop="lengthTypeLabel" label="篇幅" min-width="80" show-overflow-tooltip />
      <el-table-column prop="workTypeLabel" label="状态" min-width="80" show-overflow-tooltip />
      <el-table-column prop="endingTypeLabel" label="结局" min-width="80" show-overflow-tooltip />
      <el-table-column prop="updateDate" label="上次更新" min-width="110" show-overflow-tooltip />
      <el-table-column label="来源" min-width="110">
        <template #default="{ row }">
          <el-link
            v-if="row.sourceUrl"
            :href="row.sourceUrl"
            target="_blank"
            rel="noopener noreferrer"
          >
            点击跳转
          </el-link>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" class-name="op-col" min-width="80">
        <template #default="{ row }">
          <el-link @click="$router.push(`/fanfic/${row.id}`)">详情</el-link>
        </template>
      </el-table-column>
      </el-table>
    </div>
    <el-pagination
      layout="prev, pager, next"
      :total="total"
      :page-size="query.size"
      :current-page="query.page"
      @current-change="
        (p) => {
          query.page = p;
          fetchData();
        }
      "
    />
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { getFanficListApi } from "../api/item";

const rows = ref([]);
const total = ref(0);
const tableRef = ref(null);
const query = reactive({});

const fetchData = async () => {
  const resp = await getFanficListApi(query); // 这里拿到的是后端外层 data（因为 http.js 已解包）

  const list = Array.isArray(resp?.data) ? resp.data : [];
  rows.value = list.map((item) => ({
    id: item.id,
    storeUrl: item.store_url ?? "",
    content: item.content ?? "",
    title: item.title ?? "",
    cp: item.cp,
    author: item.author ?? "",
    sourceUrl: item.source_url ?? "",
    trackingTypeLabel: item.tracking_type_label ?? "",
    rating: item.rating ?? "",
    itemId: item.fanfic_vo?.item_id ?? "",
    eraLabel: item.fanfic_vo?.era_label ?? "",
    charSetting: item.fanfic_vo?.char_setting ?? "",
    lengthTypeLabel: item.fanfic_vo?.length_type_label ?? "",
    workTypeLabel: item.fanfic_vo?.work_type_label ?? "",
    updateDate: item.fanfic_vo?.update_date ?? "-",
    endingTypeLabel: item.fanfic_vo?.ending_type_label ?? "",
    readCount: item.fanfic_vo?.read_count ?? 0,
  }));

  total.value = Number(resp?.total ?? rows.value.length);
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

onMounted(fetchData);
</script>

<style scoped>
.panel {
  padding: 16px;
}

.table-wrap {
  min-width: 0;
}

:deep(.el-table .op-col .cell) {
  padding-left: 0;
  padding-right: 0;
}

:deep(.el-table .op-col .el-button.is-text) {
  padding: 0;
  height: auto;
}
</style>

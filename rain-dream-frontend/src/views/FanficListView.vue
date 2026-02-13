<template>
  <section class="card-panel panel">
    <h2>文章列表</h2>
    <el-table :data="rows" stripe fit table-layout="auto">
      <!-- <el-table-column prop="id" label="ID" width="80" /> -->
      <el-table-column prop="eraLabel" label="年代" />
      <el-table-column prop="title" label="名称" />
      <!-- <el-table-column prop="cp" label="CP" width="80" /> -->
      <el-table-column prop="author" label="作者" />
      <el-table-column prop="trackingTypeLabel" label="追踪" />
      <el-table-column prop="rating" label="评分" />
      <el-table-column prop="lengthTypeLabel" label="篇幅" />
      <el-table-column prop="workTypeLabel" label="状态" />
      <el-table-column prop="endingTypeLabel" label="结局" />
      <el-table-column prop="updateDate" label="上次更新" />
      <el-table-column label="来源">
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
      <el-table-column label="操作" class-name="op-col">
        <template #default="{ row }">
          <el-link @click="$router.push(`/fanfic/${row.id}`)">详情</el-link>
        </template>
      </el-table-column>
    </el-table>
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

onMounted(fetchData);
</script>

<style scoped>
.panel {
  padding: 16px;
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

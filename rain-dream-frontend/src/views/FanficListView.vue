<template>
  <section class="card-panel panel">
    <h2>文章列表</h2>
    <el-table :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="fandom" label="Fandom" width="140" />
      <el-table-column prop="cp" label="CP" width="140" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button text @click="$router.push(`/fanfic/${row.id}`)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      layout="prev, pager, next"
      :total="total"
      :page-size="query.size"
      :current-page="query.page"
      @current-change="(p) => { query.page = p; fetchData() }"
    />
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getFanficListApi } from '../api/item'

const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })

const fetchData = async () => {
  const data = await getFanficListApi(query)
  rows.value = data?.records || []
  total.value = Number(data?.total || 0)
}

onMounted(fetchData)
</script>

<style scoped>
.panel { padding: 16px; }
</style>

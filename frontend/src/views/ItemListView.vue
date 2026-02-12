<template>
  <section class="card-panel panel">
    <div class="head">
      <h2>All Works</h2>
      <el-button type="primary" @click="$router.push('/items/new')">新增作品</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="fandom" label="Fandom" width="140" />
      <el-table-column prop="cp" label="CP" width="140" />
      <el-table-column prop="rating" label="评分" width="100" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button text @click="$router.push(`/items/${row.id}`)">详情</el-button>
          <el-button text @click="$router.push(`/items/edit/${row.id}`)">编辑</el-button>
          <el-button text type="danger" @click="remove(row.id)">删除</el-button>
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
import { ElMessageBox, ElMessage } from 'element-plus'
import { getItemListApi, deleteItemApi } from '../api/item'

const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })

const fetchData = async () => {
  const data = await getItemListApi(query)
  rows.value = data?.records || []
  total.value = Number(data?.total || 0)
}

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该作品吗？', '提示', { type: 'warning' })
  await deleteItemApi(id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.panel { padding: 16px; }
.head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
</style>

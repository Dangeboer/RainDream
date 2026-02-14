<template>
  <section class="card-panel panel">
    <div class="head">
      <h2>{{ pageTitle }}</h2>
      <el-button type="primary" @click="$router.push('/items/new')">新增作品</el-button>
    </div>
    <div class="table-wrap" @wheel.capture="onTableWheel">
      <el-table ref="tableRef" :data="rows" stripe>
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
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getItemListApi, deleteItemApi } from '../api/item'

const route = useRoute()
const router = useRouter()
const rows = ref([])
const total = ref(0)
const tableRef = ref(null)
const query = reactive({
  page: 1,
  size: 10,
  contentType: undefined,
  mediaType: undefined
})

const contentTypeLabelMap = {
  1: '文章',
  2: '图绘',
  3: '精修',
  4: '混剪',
  5: '解析',
  6: '吐槽',
  7: '主创说',
  8: 'RPS',
  9: '其他'
}

const mediaTypeLabelMap = {
  1: '文本',
  2: '图片',
  3: '视频',
  4: '链接'
}

const pageTitle = computed(() => {
  const parts = ['全部资源']
  if (query.contentType) parts.push(contentTypeLabelMap[query.contentType] || `内容类型 ${query.contentType}`)
  if (query.mediaType) parts.push(mediaTypeLabelMap[query.mediaType] || `格式 ${query.mediaType}`)
  return parts.join(' / ')
})

const parsePositiveInt = (value) => {
  const parsed = Number.parseInt(value, 10)
  return Number.isNaN(parsed) || parsed <= 0 ? undefined : parsed
}

const syncQueryFromRoute = () => {
  query.page = parsePositiveInt(route.query.page) || 1
  query.contentType = parsePositiveInt(route.query.contentType)
  query.mediaType = parsePositiveInt(route.query.mediaType)
}

const fetchData = async () => {
  const params = {
    page: query.page,
    size: query.size
  }
  if (query.contentType) params.contentType = query.contentType
  if (query.mediaType) params.mediaType = query.mediaType

  const data = await getItemListApi(params)
  rows.value = data?.records || []
  total.value = Number(data?.total || 0)
}

const onPageChange = async (page) => {
  await router.push({
    path: '/items',
    query: {
      ...route.query,
      page
    }
  })
}

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该作品吗？', '提示', { type: 'warning' })
  await deleteItemApi(id)
  ElMessage.success('删除成功')
  fetchData()
}

const onTableWheel = (event) => {
  if (!event.shiftKey) return
  const tableEl = tableRef.value?.$el
  if (!tableEl) return
  const bodyWrap = tableEl.querySelector('.el-table__body-wrapper .el-scrollbar__wrap')
    || tableEl.querySelector('.el-table__body-wrapper')
  if (!bodyWrap || bodyWrap.scrollWidth <= bodyWrap.clientWidth) return
  const delta = Math.abs(event.deltaX) > 0 ? event.deltaX : event.deltaY
  bodyWrap.scrollLeft += delta
  event.preventDefault()
}

watch(
  () => route.query,
  () => {
    syncQueryFromRoute()
    fetchData()
  },
  { immediate: true }
)
</script>

<style scoped>
.panel { padding: 16px; }
.head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.table-wrap { min-width: 0; }
</style>

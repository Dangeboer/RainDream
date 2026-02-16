<template>
  <section class="card-panel panel">
    <div class="head">
      <h2>{{ loading ? '加载中...' : detail.title || '作品详情' }}</h2>
      <el-button text @click="$router.back()">返回</el-button>
    </div>
    <el-skeleton v-if="loading" :rows="6" animated />
    <el-descriptions v-else :column="2" border>
      <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
      <el-descriptions-item label="作者">{{ detail.author }}</el-descriptions-item>
      <el-descriptions-item label="Fandom">{{ detail.fandom }}</el-descriptions-item>
      <el-descriptions-item label="CP">{{ detail.cp }}</el-descriptions-item>
      <el-descriptions-item label="Source URL" :span="2">{{ detail.sourceUrl }}</el-descriptions-item>
      <el-descriptions-item label="资源链接" :span="2">
        <el-link
          v-if="detail.storeUrl"
          :href="detail.storeUrl"
          target="_blank"
          rel="noopener noreferrer"
        >
          点击查看
        </el-link>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="评分">{{ detail.rating }}</el-descriptions-item>
      <el-descriptions-item label="年份">{{ detail.releaseYear }}</el-descriptions-item>
      <el-descriptions-item label="简介" :span="2">{{ detail.summary }}</el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">{{ detail.notes }}</el-descriptions-item>
    </el-descriptions>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getItemDetailApi } from '../api/item'

const route = useRoute()
const detail = ref({})
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    detail.value = await getItemDetailApi(route.params.id)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.panel { padding: 16px; }
.head { display: flex; justify-content: space-between; align-items: center; }
</style>

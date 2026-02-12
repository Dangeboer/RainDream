<template>
  <section class="card-panel panel">
    <div class="head">
      <h2>{{ detail.title || 'Fanfic 详情' }}</h2>
      <el-button text @click="$router.back()">返回</el-button>
    </div>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
      <el-descriptions-item label="作者">{{ detail.author }}</el-descriptions-item>
      <el-descriptions-item label="Fandom">{{ detail.fandom }}</el-descriptions-item>
      <el-descriptions-item label="CP">{{ detail.cp }}</el-descriptions-item>
      <el-descriptions-item label="章节">{{ detail.currentChapter }}/{{ detail.totalChapter }}</el-descriptions-item>
      <el-descriptions-item label="字数">{{ detail.wordCount }}</el-descriptions-item>
      <el-descriptions-item label="简介" :span="2">{{ detail.summary }}</el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">{{ detail.notes }}</el-descriptions-item>
    </el-descriptions>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getFanficDetailApi } from '../api/item'

const route = useRoute()
const detail = ref({})

onMounted(async () => {
  detail.value = await getFanficDetailApi(route.params.id)
})
</script>

<style scoped>
.panel { padding: 16px; }
.head { display: flex; justify-content: space-between; align-items: center; }
</style>
